package de.jakob.lotm.abilities.death;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.entity.custom.spirits.*;

import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.helper.subordinates.SubordinateUtils;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.helper.subordinates.SubordinateComponent;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.UseQueuedSoulAbilityPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber
public class InternalUnderworldAbility extends SelectableAbility {

    private static final String STORED_SOULS_TAG = "InternalUnderworldSouls";
    private static final String CAPTURE_MODE_TAG = "InternalUnderworldCaptureMode";
    private static final String PENDING_ABILITY_TAG = "InternalUnderworldPendingAbility";

    private record ActiveSoul(LivingEntity entity, CompoundTag soulData) {}
    private static final Map<UUID, List<ActiveSoul>> activeSouls = new ConcurrentHashMap<>();

    private static boolean isCapturableEntity(LivingEntity entity) {
        if (    entity instanceof Zombie
                || entity instanceof ZombieVillager
                || entity instanceof Husk
                || entity instanceof Drowned
                || entity instanceof ZombifiedPiglin
                || entity instanceof Skeleton
                || entity instanceof WitherSkeleton
                || entity instanceof Stray
                || entity instanceof Phantom
                || entity instanceof Vex
                || entity instanceof Hoglin
                || entity instanceof SpiritDervishEntity
                || entity instanceof SpiritBlueWizardEntity
                || entity instanceof SpiritMalmouthEntity
                || entity instanceof SpiritTranslucentWizardEntity
                || entity instanceof SpiritGhostEntity
        ) {
            return true;
        }
        return false;
    }

    private static int getMaxSouls(int sequence) {
        return switch (sequence) {
            case 5 -> 5;
            case 4 -> 15;
            case 3 -> 20;
            case 2 -> 35;
            case 1 -> 45;
            case 0 -> 53;
            default -> 5;
        };
    }

    public InternalUnderworldAbility(String id) {
        super(id, 1);
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        canBeUsedByNPC = false;
        canBeUsedInArtifact = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 400;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.internal_underworld.capture",
                "ability.lotmcraft.internal_underworld.release",
                "ability.lotmcraft.internal_underworld.release_all",
                "ability.lotmcraft.internal_underworld.recall"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer player)) return;

        if (InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(entity.position(), serverLevel), "purification", BeyonderData.getSequence(entity), -1)) return;

        switch (selectedAbility) {
            case 0 -> activateCaptureMode(player);
            case 1 -> openReleaseGui(serverLevel, player);
            case 2 -> releaseAllSouls(serverLevel, player);
            case 3 -> recallAllSouls(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof LivingEntity target)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (!isInCaptureMode(player)) {
            if (tryConsumePendingAbility(player, serverLevel)) {
                event.setCanceled(true);
            }
            return;
        }
        if (getActiveSoulForEntity(player, target) != null) return;

        event.setCanceled(true);
        clearCaptureMode(player);

        if (!isCapturableEntity(target)) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.not_capturable")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        if (!BeyonderData.isBeyonder(player) || !BeyonderData.getPathway(player).equals("death") || BeyonderData.getSequence(player) > 5) return;

        int playerSeq = BeyonderData.getSequence(player);

        if (getStoredSouls(player).size() >= getMaxSouls(playerSeq)) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.full")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        spawnCaptureAttemptParticles(serverLevel, target);

        float captureChance = 0.55f + (playerSeq * 0.05f);
        if (player.getRandom().nextFloat() >= captureChance) {
            spawnFailureParticles(serverLevel, target);
            serverLevel.playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.8f);
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.escaped")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        CompoundTag soulData = buildSoulData(target);
        if (soulData == null) return;
        addStoredSoul(player, soulData);
        spawnCaptureSuccessParticles(serverLevel, target);
        serverLevel.playSound(null, target.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0f, 0.7f);
        target.discard();

        player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.captured",
                target.getName().getString()).withStyle(ChatFormatting.DARK_AQUA));
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if (!(victim.level() instanceof ServerLevel serverLevel)) return;
        if (!(victim instanceof BeyonderNPCEntity)) return;

        ServerPlayer player = resolveCapturePlayer(event, victim);
        if (player == null) return;
        if (victim.getUUID().equals(player.getUUID())) return;
        if (!BeyonderData.isBeyonder(player) || !BeyonderData.getPathway(player).equals("death")) return;

        int playerSeq = BeyonderData.getSequence(player);
        if (playerSeq > 5) return;

        if (getStoredSouls(player).size() >= getMaxSouls(playerSeq)) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.full")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        spawnCaptureAttemptParticles(serverLevel, victim);

        if (player.getRandom().nextFloat() >= 0.5f) {
            spawnFailureParticles(serverLevel, victim);
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.escaped")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        CompoundTag soulData = buildSoulData(victim);
        if (soulData == null) return;
        addStoredSoul(player, soulData);
        spawnCaptureSuccessParticles(serverLevel, victim);
        serverLevel.playSound(null, victim.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0f, 0.7f);

        player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.captured",
                victim.getName().getString()).withStyle(ChatFormatting.DARK_AQUA));
        player.sendSystemMessage(Component.literal("A beyonder has entered your Internal Underworld")
            .withStyle(ChatFormatting.DARK_AQUA));
    }

    @SubscribeEvent
    public static void onPlayerInteractSummonedSoul(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof LivingEntity target)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        ActiveSoul activeSoul = getActiveSoulForEntity(player, target);
        if (activeSoul == null) return;

        if (tryConsumePendingAbility(player, serverLevel)) {
            event.setCanceled(true);
            return;
        }

        event.setCanceled(true);
        openSoulAbilityGui(serverLevel, player, activeSoul.soulData());
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        if (tryConsumePendingAbility(player, serverLevel)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        if (tryConsumePendingAbility(player, serverLevel)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        if (!event.getLevel().isClientSide()) return;

        PacketHandler.sendToServer(new UseQueuedSoulAbilityPacket());
        event.setCanceled(true);
    }

    private static ServerPlayer resolveCapturePlayer(LivingDeathEvent event, LivingEntity victim) {
        ServerLevel level = (ServerLevel) victim.level();

        ServerPlayer fromSource = resolvePlayerFromEntity(event.getSource().getEntity(), level);
        if (fromSource != null) return fromSource;

        ServerPlayer fromDirect = resolvePlayerFromEntity(event.getSource().getDirectEntity(), level);
        if (fromDirect != null) return fromDirect;

        LivingEntity lastHurt = victim.getLastHurtByMob();
        ServerPlayer fromLastHurt = resolvePlayerFromEntity(lastHurt, level);
        if (fromLastHurt != null) return fromLastHurt;

        ServerPlayer fromKillCredit = resolvePlayerFromEntity(victim.getKillCredit(), level);
        if (fromKillCredit != null) return fromKillCredit;

        if (level.players().size() == 1) {
            return level.players().get(0);
        }

        return null;
    }

    private static ServerPlayer resolvePlayerFromEntity(Entity entity, ServerLevel level) {
        if (entity == null) return null;

        if (entity instanceof Projectile projectile) {
            return resolvePlayerFromEntity(projectile.getOwner(), level);
        }

        if (entity instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }

        if (entity instanceof LivingEntity living) {
            SubordinateComponent subordinateComponent = living.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
            if (subordinateComponent.isSubordinate()) {
                try {
                    UUID controllerId = UUID.fromString(subordinateComponent.getControllerUUID());
                    ServerPlayer controller = level.getServer().getPlayerList().getPlayer(controllerId);
                    if (controller != null) return controller;
                } catch (IllegalArgumentException ignored) {
                    return null;
                }
            }

            MarionetteComponent marionetteComponent = living.getData(ModAttachments.MARIONETTE_COMPONENT.get());
            if (marionetteComponent.isMarionette()) {
                try {
                    UUID controllerId = UUID.fromString(marionetteComponent.getControllerUUID());
                    return level.getServer().getPlayerList().getPlayer(controllerId);
                } catch (IllegalArgumentException ignored) {
                    return null;
                }
            }
        }

        return null;
    }

    private static void activateCaptureMode(ServerPlayer player) {
        player.getPersistentData().putBoolean(CAPTURE_MODE_TAG, true);
        player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.capture_mode_on")
                .withStyle(ChatFormatting.DARK_AQUA));
    }

    private static void clearCaptureMode(ServerPlayer player) {
        player.getPersistentData().remove(CAPTURE_MODE_TAG);
    }

    private static boolean isInCaptureMode(ServerPlayer player) {
        return player.getPersistentData().getBoolean(CAPTURE_MODE_TAG);
    }

    private static CompoundTag buildSoulData(LivingEntity entity) {
        if (entity == null) return null;
        ResourceLocation typeKey = EntityType.getKey(entity.getType());
        if (typeKey == null) return null;
        CompoundTag soulData = new CompoundTag();
        soulData.putString("EntityType", typeKey.toString());
        soulData.putString("DisplayName", entity.hasCustomName() ? entity.getCustomName().getString() : entity.getName().getString());

        CompoundTag entityNBT = new CompoundTag();
        entity.save(entityNBT);
        entityNBT.remove("UUID");
        entityNBT.remove("Health");
        soulData.put("EntityNBT", entityNBT);

        if (BeyonderData.isBeyonder(entity)) {
            soulData.putString("Pathway", BeyonderData.getPathway(entity));
            soulData.putInt("Sequence", BeyonderData.getSequence(entity));
        }

        return soulData;
    }

    private void openReleaseGui(ServerLevel level, ServerPlayer player) {
        List<CompoundTag> storedSouls = getStoredSouls(player);

        if (storedSouls.isEmpty()) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.no_souls")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        SimpleContainer container = new SimpleContainer(54) {
            @Override
            public boolean canTakeItem(Container target, int index, ItemStack stack) {
                return false;
            }
        };

        ItemStack releaseAllItem = new ItemStack(Items.NETHER_STAR);
        releaseAllItem.set(DataComponents.CUSTOM_NAME, Component.literal("Release All").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        releaseAllItem.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Summons all stored spirits").withStyle(ChatFormatting.GRAY)
        )));
        CompoundTag releaseAllTag = new CompoundTag();
        releaseAllTag.putBoolean("IsReleaseAll", true);
        releaseAllItem.set(DataComponents.CUSTOM_DATA, CustomData.of(releaseAllTag));
        container.setItem(0, releaseAllItem);

        ItemStack discardItem = new ItemStack(Items.BARRIER);
        discardItem.set(DataComponents.CUSTOM_NAME, Component.literal("Discard Mode").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        discardItem.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Click a soul to permanently discard it").withStyle(ChatFormatting.GRAY)
        )));
        CompoundTag discardTag = new CompoundTag();
        discardTag.putBoolean("IsDiscardMode", true);
        discardItem.set(DataComponents.CUSTOM_DATA, CustomData.of(discardTag));
        container.setItem(1, discardItem);

        int slot = 2;
        for (int i = 0; i < storedSouls.size() && slot < container.getContainerSize(); i++) {
            container.setItem(slot++, createSoulDisplayItem(storedSouls.get(i)));
        }

        final int containerSize = container.getContainerSize();

        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x6, id, inv, container, 6) {
                    private boolean discardMode = false;

                    @Override
                    public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, Player clickPlayer) {
                        if (slotId < 0 || slotId >= containerSize) return;
                        ItemStack clicked = container.getItem(slotId);
                        if (clicked.isEmpty()) return;

                        CustomData customData = clicked.get(DataComponents.CUSTOM_DATA);
                        if (customData == null) return;
                        CompoundTag tag = customData.copyTag();

                        if (tag.contains("IsDiscardMode")) {
                            discardMode = !discardMode;
                            return;
                        }

                        if (tag.contains("IsReleaseAll")) {
                            player.closeContainer();
                            level.getServer().execute(() -> releaseAllSouls(level, player));
                            return;
                        }

                        if (!tag.contains("SoulData")) return;
                        CompoundTag soulData = tag.getCompound("SoulData");

                        if (discardMode) {
                            removeStoredSoul(player, soulData);
                            player.closeContainer();
                            player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.discarded")
                                    .withStyle(ChatFormatting.GRAY));
                        } else {
                            player.closeContainer();
                            level.getServer().execute(() -> releaseSoul(level, player, soulData));
                        }
                    }
                },
                Component.translatable("ability.lotmcraft.internal_underworld.select_soul")
        ));
    }

    private static void openSoulAbilityGui(ServerLevel level, ServerPlayer player, CompoundTag soulData) {
        String pathway = soulData.contains("Pathway") ? soulData.getString("Pathway") : "";
        int sequence = soulData.contains("Sequence", Tag.TAG_INT) ? soulData.getInt("Sequence") : -1;

        if (pathway.isEmpty() || sequence < 0) {
            player.sendSystemMessage(Component.literal("This soul has no abilities").withStyle(ChatFormatting.RED));
            return;
        }

        List<Ability> abilities = LOTMCraft.abilityHandler.getByPathwayAndSequenceOrderedBySequence(pathway, sequence)
            .stream()
            .filter(ability -> !(ability instanceof ToggleAbility))
            .toList();
        if (abilities.isEmpty()) {
            player.sendSystemMessage(Component.literal("This soul has no abilities").withStyle(ChatFormatting.RED));
            return;
        }

        SimpleContainer container = new SimpleContainer(54) {
            @Override
            public boolean canTakeItem(Container target, int index, ItemStack stack) {
                return false;
            }
        };

        for (int i = 0; i < Math.min(abilities.size(), 54); i++) {
            Ability ability = abilities.get(i);
            ItemStack item = createAbilityDisplayItem(ability);
            CompoundTag tag = new CompoundTag();
            tag.putString("AbilityId", ability.getId());
            item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            container.setItem(i, item);
        }

        final int containerSize = container.getContainerSize();

        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x6, id, inv, container, 6) {
                    @Override
                    public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, Player clickPlayer) {
                        if (slotId < 0 || slotId >= containerSize) return;
                        ItemStack clicked = container.getItem(slotId);
                        if (clicked.isEmpty()) return;

                        CustomData customData = clicked.get(DataComponents.CUSTOM_DATA);
                        if (customData == null) return;
                        CompoundTag tag = customData.copyTag();
                        if (!tag.contains("AbilityId")) return;

                        String abilityId = tag.getString("AbilityId");
                        player.closeContainer();
                        level.getServer().execute(() -> queueSoulAbility(player, abilityId));
                    }
                },
                Component.literal("Internal Underworld - Soul Abilities")
        ));
    }

    private static ItemStack createAbilityDisplayItem(Ability ability) {
        ItemStack item = new ItemStack(Items.BOOK);
        item.set(DataComponents.CUSTOM_NAME, Component.literal(prettyAbilityName(ability.getId()))
                .withStyle(ChatFormatting.AQUA));
        item.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Cast as yourself").withStyle(ChatFormatting.GRAY)
        )));
        return item;
    }

    private static String prettyAbilityName(String id) {
        if (id == null || id.isEmpty()) return "Ability";
        String cleaned = id.replace("_ability", "").replace('_', ' ');
        String[] parts = cleaned.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) sb.append(part.substring(1));
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    private static void castSoulAbility(ServerLevel level, ServerPlayer player, String abilityId) {
        Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
        if (ability == null) return;

        ability.useAbility(level, player, true, false, false);
    }

    private static void queueSoulAbility(ServerPlayer player, String abilityId) {
        if (abilityId == null || abilityId.isEmpty()) return;
        player.getPersistentData().putString(PENDING_ABILITY_TAG, abilityId);
        player.sendSystemMessage(Component.literal("Ability queued: " + prettyAbilityName(abilityId))
                .withStyle(ChatFormatting.AQUA));
    }

    private static boolean tryConsumePendingAbility(ServerPlayer player, ServerLevel level) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(PENDING_ABILITY_TAG, Tag.TAG_STRING)) return false;
        String abilityId = data.getString(PENDING_ABILITY_TAG);
        if (abilityId == null || abilityId.isEmpty()) {
            data.remove(PENDING_ABILITY_TAG);
            return false;
        }

        data.remove(PENDING_ABILITY_TAG);
        castSoulAbility(level, player, abilityId);
        return true;
    }

    public static void consumeQueuedSoulAbility(ServerPlayer player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            tryConsumePendingAbility(player, serverLevel);
        }
    }

    private static ActiveSoul getActiveSoulForEntity(ServerPlayer player, LivingEntity target) {
        List<ActiveSoul> souls = activeSouls.get(player.getUUID());
        if (souls == null || souls.isEmpty()) return null;

        ActiveSoul match = null;
        Iterator<ActiveSoul> iterator = souls.iterator();
        while (iterator.hasNext()) {
            ActiveSoul soul = iterator.next();
            if (!soul.entity().isAlive()) {
                iterator.remove();
                continue;
            }
            if (soul.entity().getUUID().equals(target.getUUID())) {
                match = soul;
            }
        }

        if (souls.isEmpty()) {
            activeSouls.remove(player.getUUID());
        }

        return match;
    }


    private void releaseAllSouls(ServerLevel level, ServerPlayer player) {
        List<CompoundTag> storedSouls = getStoredSouls(player);

        if (storedSouls.isEmpty()) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.no_souls")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        int summoned = 0;
        for (CompoundTag soulData : new ArrayList<>(storedSouls)) {
            try {
                releaseSoul(level, player, soulData);
                summoned++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.summoned_all", summoned)
                .withStyle(ChatFormatting.DARK_AQUA));
    }

    private void releaseSoul(ServerLevel level, ServerPlayer player, CompoundTag soulData) {
        try {
            Optional<EntityType<?>> optionalType = EntityType.byString(soulData.getString("EntityType"));
            if (optionalType.isEmpty()) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.failed").withStyle(ChatFormatting.RED));
                return;
            }

            net.minecraft.world.entity.Entity entity = optionalType.get().create(level);
            if (entity == null) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.failed").withStyle(ChatFormatting.RED));
                return;
            }

            if (soulData.contains("EntityNBT")) {
                CompoundTag nbt = soulData.getCompound("EntityNBT").copy();
                nbt.remove("UUID");
                entity.load(nbt);
            }

            Vec3 look = player.getLookAngle();
            Vec3 pos = player.position().add(look.x * 2, 0, look.z * 2);
            entity.moveTo(pos.x, pos.y, pos.z, player.getYRot(), 0);
            entity.setUUID(UUID.randomUUID());
            entity.getPersistentData().putBoolean("VoidSummoned", true);

            boolean spawned = level.addFreshEntity(entity);

            if (spawned && entity instanceof LivingEntity livingEntity) {
                SubordinateUtils.turnEntityIntoSubordinate(livingEntity, player, false);

                List<ActiveSoul> existing = activeSouls.computeIfAbsent(player.getUUID(), k -> new ArrayList<>());
                existing.removeIf(e -> !e.entity().isAlive());
                for (ActiveSoul other : existing) {
                    AllyUtil.makeAllies(livingEntity, other.entity(), false);
                }
                existing.add(new ActiveSoul(livingEntity, soulData));
                removeStoredSoul(player, soulData);

                spawnReleaseParticles(level, livingEntity);
                level.playSound(null, livingEntity.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.4f, 1.6f);

                player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.summoned",
                        entity.getName().getString()).withStyle(ChatFormatting.DARK_AQUA));
            } else {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.failed").withStyle(ChatFormatting.RED));
            }

        } catch (Exception e) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.failed").withStyle(ChatFormatting.RED));
            e.printStackTrace();
        }
    }

    private void recallAllSouls(ServerPlayer player) {
        List<ActiveSoul> souls = activeSouls.remove(player.getUUID());
        if (souls == null || souls.isEmpty()) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.no_active_souls")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        int count = 0;
        for (ActiveSoul soul : souls) {
            if (soul.entity().isAlive()) {
                if (soul.entity().level() instanceof ServerLevel serverLevel) {
                    spawnRecallParticles(serverLevel, soul.entity());
                    serverLevel.playSound(null, soul.entity().blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 0.8f, 1.4f);
                }
                addStoredSoul(player, soul.soulData());
                soul.entity().discard();
                count++;
            }
        }

        player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.retrieved", count)
                .withStyle(ChatFormatting.DARK_AQUA));
    }

    public static void recallSoulsOnLogout(ServerPlayer player) {
        List<ActiveSoul> souls = activeSouls.remove(player.getUUID());
        if (souls == null) return;
        for (ActiveSoul soul : souls) {
            if (soul.entity().isAlive()) {
                addStoredSoul(player, soul.soulData());
                soul.entity().discard();
            }
        }
    }

    public static void despawnSoulsOnDeath(ServerPlayer player) {
        List<ActiveSoul> souls = activeSouls.remove(player.getUUID());
        if (souls == null) return;
        for (ActiveSoul soul : souls) {
            if (soul.entity().isAlive()) soul.entity().discard();
        }
    }

    private static void spawnCaptureAttemptParticles(ServerLevel level, LivingEntity target) {
        Vec3 pos = target.position().add(0, target.getBbHeight() / 2.0, 0);
        for (int i = 0; i < 20; i++) {
            double angle = (2 * Math.PI / 20) * i;
            level.sendParticles(ParticleTypes.SOUL, pos.x + Math.cos(angle) * 0.8, pos.y, pos.z + Math.sin(angle) * 0.8, 1, 0, 0.05, 0, 0.01);
        }
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y, pos.z, 10, 0.3, 0.3, 0.3, 0.02);
    }

    private static void spawnFailureParticles(ServerLevel level, LivingEntity target) {
        Vec3 pos = target.position().add(0, target.getBbHeight() / 2.0, 0);
        level.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 25, 0.4, 0.4, 0.4, 0.05);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, 8, 0.2, 0.2, 0.2, 0.01);
    }

    private static void spawnCaptureSuccessParticles(ServerLevel level, LivingEntity target) {
        Vec3 pos = target.position().add(0, target.getBbHeight() / 2.0, 0);
        for (int i = 0; i < 30; i++) {
            double angle = (2 * Math.PI / 30) * i;
            double r = 0.5 + (i % 3) * 0.2;
            level.sendParticles(ParticleTypes.SOUL, pos.x + Math.cos(angle) * r, pos.y + i * 0.05, pos.z + Math.sin(angle) * r, 1, 0, 0.02, 0, 0.0);
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.x, pos.y, pos.z, 20, 0.3, 0.5, 0.3, 0.1);
    }

    private static void spawnReleaseParticles(ServerLevel level, LivingEntity entity) {
        Vec3 pos = entity.position().add(0, entity.getBbHeight() / 2.0, 0);
        level.sendParticles(ParticleTypes.SOUL, pos.x, pos.y, pos.z, 25, 0.5, 0.5, 0.5, 0.05);
        level.sendParticles(ParticleTypes.PORTAL, pos.x, pos.y, pos.z, 30, 0.4, 0.6, 0.4, 0.1);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y + 1, pos.z, 10, 0.2, 0.3, 0.2, 0.02);
    }

    private static void spawnRecallParticles(ServerLevel level, LivingEntity entity) {
        Vec3 pos = entity.position().add(0, entity.getBbHeight() / 2.0, 0);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.x, pos.y, pos.z, 20, 0.4, 0.4, 0.4, 0.08);
        level.sendParticles(ParticleTypes.SOUL, pos.x, pos.y, pos.z, 15, 0.3, 0.3, 0.3, 0.03);
    }

    private ItemStack createSoulDisplayItem(CompoundTag soulData) {
        ItemStack item = new ItemStack(Items.PLAYER_HEAD);
        item.set(DataComponents.CUSTOM_NAME, Component.literal(soulData.getString("DisplayName")).withStyle(ChatFormatting.LIGHT_PURPLE));
        String pathway = soulData.contains("Pathway") ? soulData.getString("Pathway") : "none";
        int sequence = soulData.contains("Sequence", Tag.TAG_INT) ? soulData.getInt("Sequence") : -1;
        String sequenceText = sequence >= 0 ? Integer.toString(sequence) : "-";
        item.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("-------------------").withStyle(style -> style.withColor(0xFF7ECFCF).withItalic(false)),
            Component.literal(soulData.getString("EntityType")).withStyle(style -> style.withColor(0x7ECFCF).withItalic(false)),
            Component.literal("Pathway: " + pathway).withStyle(style -> style.withColor(0x7ECFCF).withItalic(false)),
            Component.literal("Sequence: " + sequenceText).withStyle(style -> style.withColor(0x7ECFCF).withItalic(false))
        )));

        CompoundTag tag = new CompoundTag();
        tag.put("SoulData", soulData);
        item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        return item;
    }

    private static List<CompoundTag> getStoredSouls(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        List<CompoundTag> souls = new ArrayList<>();
        if (data.contains(STORED_SOULS_TAG)) {
            ListTag list = data.getList(STORED_SOULS_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) souls.add(list.getCompound(i));
        }
        return souls;
    }

    private static void addStoredSoul(ServerPlayer player, CompoundTag soulData) {
        if (soulData == null) return;
        CompoundTag data = player.getPersistentData();
        ListTag list = data.contains(STORED_SOULS_TAG)
                ? data.getList(STORED_SOULS_TAG, Tag.TAG_COMPOUND)
                : new ListTag();

        list.add(soulData);

        int maxSouls = getMaxSouls(BeyonderData.getSequence(player));
        while (list.size() > maxSouls) list.remove(0);

        data.put(STORED_SOULS_TAG, list);
    }

    private static void removeStoredSoul(ServerPlayer player, CompoundTag soulData) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(STORED_SOULS_TAG)) return;
        ListTag list = data.getList(STORED_SOULS_TAG, Tag.TAG_COMPOUND);
        list.remove(soulData);
        data.put(STORED_SOULS_TAG, list);
    }
}