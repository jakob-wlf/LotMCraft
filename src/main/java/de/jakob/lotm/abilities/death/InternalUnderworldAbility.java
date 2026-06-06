package de.jakob.lotm.abilities.death;

import com.mojang.authlib.GameProfile;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.PhysicalEnhancementsAbility;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.entity.custom.spirits.*;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.util.helper.AbilityBarHelper;
import de.jakob.lotm.util.pathways.PathwayInfos;

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
import net.minecraft.core.registries.BuiltInRegistries;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.UseQueuedSoulAbilityPacket;
import de.jakob.lotm.network.packets.toClient.OpenInternalUnderworldAbilityScreenPacket;
import de.jakob.lotm.network.packets.toClient.UpdateAbilityBarPacket;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber
public class InternalUnderworldAbility extends SelectableAbility {

    // Player NBT keys and runtime flags used by the Internal Underworld system.
    private static final String STORED_SOULS_TAG = "InternalUnderworldSouls";
    private static final String CAPTURE_MODE_TAG = "InternalUnderworldCaptureMode";
    private static final String PENDING_ABILITY_TAG = "InternalUnderworldPendingAbility";
    private static final String INTERNAL_UNDERWORLD_LOCKED_TAG = "InternalUnderworldLocked";
    private static final String INTERNAL_UNDERWORLD_CAPTURED_TAG = "InternalUnderworldCaptured";
    private static final String SOUL_KEY_TAG = "SoulKey";
    private static final String FAVORITED_SOUL_TAG = "IsFavorited";
    private static final int FAVORITED_ROW_START_SLOT = 45;
    private static final int FAVORITED_ROW_END_SLOT = 53;
    // Base passive timings; scaled per sequence below.
    private static final int SOUL_PASSIVE_DURATION_TICKS = 20 * 60;
    private static final int SOUL_PASSIVE_COOLDOWN_TICKS = 20 * 60 * 5;

    private record ActiveSoul(LivingEntity entity, CompoundTag soulData) {}
    private record ActiveSoulPassive(PassiveAbilityItem ability, int remainingTicks) {}
    public record FreedSoulSlots(Set<String> seq0Paths, Set<String> seq1Paths) {}
    // Runtime-only tracking for released souls and passive timers per player.
    private static final Map<UUID, List<ActiveSoul>> activeSouls = new ConcurrentHashMap<>();
    private static final Map<UUID, ActiveSoulPassive> activeSoulPassives = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> soulPassiveCooldowns = new ConcurrentHashMap<>();

    private static boolean isCapturableEntity(LivingEntity entity) {
        // Only undead/spirit-like entities can be absorbed into the underworld.
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
        // Capacity scales with sequence; lower sequence gets a bigger vault.
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

        // Block the ability when purified by a stronger effect.
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
        // If we're not capturing, a right click can consume a queued soul ability.
        if (!isInCaptureMode(player)) {
            if (tryConsumePendingAbility(player, serverLevel)) {
                event.setCanceled(true);
            }
            return;
        }
        if (getActiveSoulForEntity(player, target) != null) return;

        // Capture mode consumes the click and attempts to absorb the target.
        event.setCanceled(true);
        clearCaptureMode(player);

        if (isInternalUnderworldLocked(player)) {
            player.sendSystemMessage(Component.literal("Your Internal Underworld is locked")
                .withStyle(ChatFormatting.RED));
            return;
        }

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

        // Auto-capture beyonder souls on death when the killer is eligible.
        boolean isBeyonderPlayer = victim instanceof ServerPlayer && BeyonderData.isBeyonder(victim);
        if (!(victim instanceof BeyonderNPCEntity) && !isBeyonderPlayer) return;

        ServerPlayer player = resolveCapturePlayer(event, victim);
        if (player == null) return;
        if (victim.getUUID().equals(player.getUUID())) return;
        if (!BeyonderData.isBeyonder(player) || !BeyonderData.getPathway(player).equals("death")) return;

        int playerSeq = BeyonderData.getSequence(player);
        if (playerSeq > 5) return;

        if (isInternalUnderworldLocked(player) && !isBeyonderPlayer) return;

        String victimPathway = BeyonderData.getPathway(victim);
        int victimSeq = BeyonderData.getSequence(victim);
        boolean slotAvailable = BeyonderData.hasSequenceSlotAvailable(serverLevel, victimPathway, victimSeq);
        if (!slotAvailable && isBeyonderPlayer) {
            slotAvailable = BeyonderData.hasSequenceSlotAvailableWithAdjustment(serverLevel, victimPathway, victimSeq, victimSeq, -1);
        }
        if (!slotAvailable) {
            player.sendSystemMessage(Component.literal("No sequence slots available for that soul")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        if (getStoredSouls(player).size() >= getMaxSouls(playerSeq)) {
            if (isBeyonderPlayer) {
                removeLowestSequenceSoul(player);
            } else {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.full")
                        .withStyle(ChatFormatting.RED));
                return;
            }
        }

        spawnCaptureAttemptParticles(serverLevel, victim);

        if (!isBeyonderPlayer && player.getRandom().nextFloat() >= 0.5f) {
            spawnFailureParticles(serverLevel, victim);
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.escaped")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        CompoundTag soulData = buildSoulData(victim);
        if (soulData == null) return;
        victim.getPersistentData().putBoolean(INTERNAL_UNDERWORLD_CAPTURED_TAG, true);
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

        // Right-click a summoned soul to open its ability menu.
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

        // Allow queued soul abilities to trigger on block interactions.
        if (tryConsumePendingAbility(player, serverLevel)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        // Allow queued soul abilities to trigger on item interactions.
        if (tryConsumePendingAbility(player, serverLevel)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        if (!event.getLevel().isClientSide()) return;

        // Client sends a packet so queued abilities can trigger on empty click.
        PacketHandler.sendToServer(new UseQueuedSoulAbilityPacket());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Integer cooldown = soulPassiveCooldowns.get(player.getUUID());
        if (cooldown != null) {
            int remaining = cooldown - 1;
            if (remaining <= 0) {
                soulPassiveCooldowns.remove(player.getUUID());
            } else {
                soulPassiveCooldowns.put(player.getUUID(), remaining);
            }
        }

        ActiveSoulPassive activePassive = activeSoulPassives.get(player.getUUID());

        if (player.tickCount % 20 == 0 && (activePassive != null || cooldown != null)) {
            updateSoulPassiveMenu(player);
        }

        if (activePassive == null) return;

        if (player.tickCount % 5 == 0) {
            activePassive.ability().tick(player.level(), player);
        }

        int remainingTicks = activePassive.remainingTicks() - 1;
        if (remainingTicks <= 0) {
            if (BeyonderData.getSequence(player) <= 0) {
                activeSoulPassives.put(player.getUUID(), new ActiveSoulPassive(activePassive.ability(), 1));
                return;
            }

            activePassive.ability().onPassiveAbilityRemoved(player, player.serverLevel());
            activeSoulPassives.remove(player.getUUID());
            int cooldownTicks = getPassiveCooldownTicks(BeyonderData.getSequence(player));
            soulPassiveCooldowns.put(player.getUUID(), cooldownTicks);
            player.sendSystemMessage(Component.literal("Your Passive Has Ended Your On Cooldown For " + formatRemainingTime(cooldownTicks))
                    .withStyle(ChatFormatting.RED));
            updateSoulPassiveMenu(player);
        } else {
            activeSoulPassives.put(player.getUUID(), new ActiveSoulPassive(activePassive.ability(), remainingTicks));
        }
    }

    private static ServerPlayer resolveCapturePlayer(LivingDeathEvent event, LivingEntity victim) {
        // Resolve kill ownership through direct source, projectiles, and ownership chains.
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
                // Subordinates inherit their controller for capture credit.
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
                // Marionettes inherit their controller for capture credit.
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
        // Capture mode waits for the next right click on a valid target.
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

    private static boolean isInternalUnderworldLocked(ServerPlayer player) {
        return player.getPersistentData().getBoolean(INTERNAL_UNDERWORLD_LOCKED_TAG);
    }

    private static void toggleInternalUnderworldLock(ServerPlayer player) {
        // Lock prevents new souls from being captured into storage.
        CompoundTag data = player.getPersistentData();
        boolean locked = data.getBoolean(INTERNAL_UNDERWORLD_LOCKED_TAG);
        data.putBoolean(INTERNAL_UNDERWORLD_LOCKED_TAG, !locked);
        player.sendSystemMessage(Component.literal(!locked ? "Internal Underworld locked" : "Internal Underworld unlocked")
                .withStyle(!locked ? ChatFormatting.RED : ChatFormatting.GREEN));
    }

    private static CompoundTag buildSoulData(LivingEntity entity) {
        if (entity == null) return null;
        ResourceLocation typeKey = EntityType.getKey(entity.getType());
        if (typeKey == null) return null;
        // Store a snapshot that can recreate the entity without UUID conflicts.
        CompoundTag soulData = new CompoundTag();
        soulData.putString(SOUL_KEY_TAG, UUID.randomUUID().toString());
        soulData.putString("EntityType", typeKey.toString());
        soulData.putString("DisplayName", entity.hasCustomName() ? entity.getCustomName().getString() : entity.getName().getString());

        CompoundTag entityNBT = new CompoundTag();
        entity.save(entityNBT);
        entityNBT.remove("UUID");
        entityNBT.remove("Health");
        soulData.put("EntityNBT", entityNBT);

        if (entity instanceof BeyonderNPCEntity npc) {
            soulData.putBoolean("IsBeyonderNPC", true);
            soulData.putString("BeyonderSkin", npc.getSkinName());
            soulData.putBoolean("BeyonderHostile", npc.isHostile());
            // NPCs store their pathway/sequence on the entity directly, not in playerMap,
            // so isBeyonder() returns false for them — read the values directly here.
            String npcPathway = BeyonderData.getPathway(npc);
            int npcSequence = BeyonderData.getSequence(npc);
            if (!npcPathway.isEmpty() && !npcPathway.equals("none")) {
                soulData.putString("Pathway", npcPathway);
                soulData.putInt("Sequence", npcSequence);
                // Give the NPC soul a descriptive display name so it isn't anonymous
                String seqName;
                if (npcSequence >= 0 && npcSequence < 10 && BeyonderData.pathwayInfos.containsKey(npcPathway)) {
                    seqName = BeyonderData.pathwayInfos.get(npcPathway).sequenceNames()[npcSequence];
                } else {
                    seqName = "Seq " + npcSequence;
                }
                soulData.putString("DisplayName", seqName);
            }
        } else {
            soulData.putBoolean("IsBeyonderNPC", false);
        }

        if (entity instanceof ServerPlayer player) {
            soulData.putBoolean("IsPlayerSoul", true);
            soulData.putUUID("OriginalPlayerUUID", player.getUUID());
            soulData.putString("OriginalPlayerName", player.getGameProfile().getName());
        } else {
            soulData.putBoolean("IsPlayerSoul", false);
        }

        // For players, read pathway/sequence from BeyonderComponent directly
        // (isBeyonder uses playerMap which may not be populated for all entity types)
        if (!soulData.contains("Pathway")) {
            String soulPathway = BeyonderData.getPathway(entity);
            int soulSequence = BeyonderData.getSequence(entity);
            if (!soulPathway.isEmpty() && !soulPathway.equals("none")) {
                soulData.putString("Pathway", soulPathway);
                soulData.putInt("Sequence", soulSequence);
            }
        }

        return soulData;
    }

    private static void openReleaseGui(ServerLevel level, ServerPlayer player) {
        // Release GUI lets you summon, discard, or lock the underworld.
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

        container.setItem(2, createUnderworldLockItem(player));

        populateSoulListContainer(container, player, storedSouls);

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

                        if (tag.contains("IsUnderworldLock")) {
                            toggleInternalUnderworldLock(player);
                            player.closeContainer();
                            level.getServer().execute(() -> openReleaseGui(level, player));
                            return;
                        }

                        if (tag.contains("IsReleaseAll")) {
                            player.closeContainer();
                            level.getServer().execute(() -> releaseAllSouls(level, player));
                            return;
                        }

                        if (!tag.contains("SoulData")) return;
                        CompoundTag soulData = tag.getCompound("SoulData");

                        boolean isMiddleClick = clickType == net.minecraft.world.inventory.ClickType.CLONE || button == 2;
                        if (isMiddleClick) {
                            if (toggleSoulFavorite(player, soulData)) {
                                refreshSoulListContainer(container, player);
                                ((ChestMenu) player.containerMenu).broadcastChanges();
                            }
                            return;
                        }

                        boolean isRightClick = clickType == net.minecraft.world.inventory.ClickType.PICKUP && button == 1;
                        if (isRightClick) {
                            player.closeContainer();
                            level.getServer().execute(() -> openSoulAbilityGui(level, player, soulData));
                            return;
                        }

                        if (discardMode) {
                            removeStoredSoul(player, soulData);
                            refreshSoulListContainer(container, player);
                            ((ChestMenu) player.containerMenu).broadcastChanges();
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
            // Swap the vanilla chest screen for the custom underworld UI.
            PacketHandler.sendToPlayer(player, new OpenInternalUnderworldAbilityScreenPacket());
    }

    private static void openSoulAbilityGui(ServerLevel level, ServerPlayer player, CompoundTag soulData) {
        // Build a menu for active and passive abilities this soul can grant.
        String pathway = soulData.contains("Pathway") ? soulData.getString("Pathway") : "";
        int sequence = soulData.contains("Sequence", Tag.TAG_INT) ? soulData.getInt("Sequence") : -1;
        int playerSequence = BeyonderData.getSequence(player);

        if (pathway.isEmpty() || sequence < 0) {
            player.sendSystemMessage(Component.literal("This soul has no abilities").withStyle(ChatFormatting.RED));
            return;
        }

        List<Ability> abilities = LOTMCraft.abilityHandler.getByPathwayAndSequenceOrderedBySequence(pathway, sequence)
            .stream()
            .filter(ability -> !(ability instanceof ToggleAbility))
            .filter(ability -> isAbilityAllowedFor(pathway, playerSequence, ability))
            .toList();
        List<PassiveAbilityItem> passiveAbilities = getSoulPassives(pathway, sequence, playerSequence);
        if (abilities.isEmpty() && passiveAbilities.isEmpty()) {
            player.sendSystemMessage(Component.literal("This soul has no abilities").withStyle(ChatFormatting.RED));
            return;
        }

        SimpleContainer container = new SimpleContainer(54) {
            @Override
            public boolean canTakeItem(Container target, int index, ItemStack stack) {
                return false;
            }
        };

        int maxAbilitySlots = 45;
        for (int i = 0; i < Math.min(abilities.size(), maxAbilitySlots); i++) {
            Ability ability = abilities.get(i);
            boolean showSubSelect = false;
            if (ability instanceof SelectableAbility selectableAbility) {
                showSubSelect = countAllowedSubAbilities(selectableAbility, player) > 1;
            }
            ItemStack item = createAbilityDisplayItem(ability, showSubSelect);
            CompoundTag tag = new CompoundTag();
            tag.putString("AbilityId", ability.getId());
            item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            container.setItem(i, item);
        }

        ActiveSoulPassive activePassive = activeSoulPassives.get(player.getUUID());
        int cooldownTicks = soulPassiveCooldowns.getOrDefault(player.getUUID(), 0);
        int passiveSlot = 45;
        if (playerSequence > 2) {
            ItemStack lockedItem = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
            lockedItem.set(DataComponents.CUSTOM_NAME, Component.literal("Passives Locked")
                    .withStyle(ChatFormatting.RED));
            lockedItem.set(DataComponents.LORE, new ItemLore(List.of(
                    Component.literal("Unlocks at Sequence 2").withStyle(ChatFormatting.GRAY)
            )));
            CompoundTag lockedTag = new CompoundTag();
            lockedTag.putBoolean("PassivesLocked", true);
            lockedItem.set(DataComponents.CUSTOM_DATA, CustomData.of(lockedTag));
            container.setItem(passiveSlot, lockedItem);
        } else {
            for (int i = 0; i < passiveAbilities.size() && passiveSlot < 53; i++) {
                PassiveAbilityItem passiveAbility = passiveAbilities.get(i);
                ItemStack item = createPassiveAbilityDisplayItem(passiveAbility, activePassive, cooldownTicks, playerSequence);
                CompoundTag tag = new CompoundTag();
                ResourceLocation passiveId = BuiltInRegistries.ITEM.getKey(passiveAbility);
                if (passiveId != null) {
                    tag.putString("PassiveId", passiveId.toString());
                    item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    container.setItem(passiveSlot, item);
                    passiveSlot++;
                }
            }
        }

        ItemStack backItem = new ItemStack(Items.ARROW);
        backItem.set(DataComponents.CUSTOM_NAME, Component.literal("Back").withStyle(ChatFormatting.YELLOW));
        backItem.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Return to soul list").withStyle(ChatFormatting.GRAY)
        )));
        CompoundTag backTag = new CompoundTag();
        backTag.putBoolean("BackToSouls", true);
        backItem.set(DataComponents.CUSTOM_DATA, CustomData.of(backTag));
        container.setItem(53, backItem);

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
                        if (tag.contains("BackToSouls")) {
                            player.closeContainer();
                            level.getServer().execute(() -> openReleaseGui(level, player));
                            return;
                        }

                        if (tag.contains("PassiveId")) {
                            PassiveAbilityItem passiveAbility = resolvePassiveAbility(tag.getString("PassiveId"));
                            if (passiveAbility != null) {
                                level.getServer().execute(() -> {
                                    activateSoulPassive(player, passiveAbility);
                                    updateSoulPassiveMenu(player);
                                });
                            }
                            return;
                        }
                        if (!tag.contains("AbilityId")) return;

                        String abilityId = tag.getString("AbilityId");
                        Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
                        boolean isMiddleClick = clickType == net.minecraft.world.inventory.ClickType.CLONE || button == 2;
                        if (isMiddleClick) {
                            if (ability instanceof SelectableAbility selectableAbility) {
                                int allowedCount = countAllowedSubAbilities(selectableAbility, player);
                                if (allowedCount > 1) {
                                    player.closeContainer();
                                    CompoundTag soulDataCopy = soulData.copy();
                                    level.getServer().execute(() -> openSoulSubAbilityGui(level, player, abilityId, soulDataCopy, true));
                                    return;
                                }
                                if (allowedCount == 1) {
                                    int onlyIndex = firstAllowedSubAbilityIndex(selectableAbility, player);
                                    if (onlyIndex >= 0) {
                                        selectableAbility.setSelectedAbility(player, onlyIndex);
                                    }
                                }
                            }

                            player.closeContainer();
                            level.getServer().execute(() -> openSoulBindSlotGui(level, player, abilityId));
                            return;
                        }
                        if (ability instanceof SelectableAbility selectableAbility) {
                            int allowedCount = countAllowedSubAbilities(selectableAbility, player);
                            if (allowedCount > 1) {
                                player.closeContainer();
                                CompoundTag soulDataCopy = soulData.copy();
                                level.getServer().execute(() -> openSoulSubAbilityGui(level, player, abilityId, soulDataCopy, false));
                                return;
                            }
                            if (allowedCount == 1) {
                                int onlyIndex = firstAllowedSubAbilityIndex(selectableAbility, player);
                                if (onlyIndex >= 0) {
                                    selectableAbility.setSelectedAbility(player, onlyIndex);
                                }
                            }
                        }

                        player.closeContainer();
                        level.getServer().execute(() -> queueSoulAbility(player, abilityId));
                    }
                },
                Component.literal("Internal Underworld - Soul Abilities")
        ));
            // Swap the vanilla chest screen for the custom underworld UI.
            PacketHandler.sendToPlayer(player, new OpenInternalUnderworldAbilityScreenPacket());
    }

    private static void openSoulSubAbilityGui(ServerLevel level, ServerPlayer player, String abilityId, CompoundTag soulData, boolean bindOnSelect) {
        // Sub-ability picker for SelectableAbility entries.
        Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
        if (!(ability instanceof SelectableAbility selectableAbility)) {
            queueSoulAbility(player, abilityId);
            return;
        }

        String[] subAbilities = selectableAbility.getAbilityNamesCopy();
        if (subAbilities.length == 0) {
            queueSoulAbility(player, abilityId);
            return;
        }

        SimpleContainer container = new SimpleContainer(54) {
            @Override
            public boolean canTakeItem(Container target, int index, ItemStack stack) {
                return false;
            }
        };

        int slot = 0;
        for (int i = 0; i < subAbilities.length; i++) {
            if (!selectableAbility.isSubAbilityAllowed(player, i)) {
                continue;
            }
            String subAbility = subAbilities[i];
            if (subAbility == null || subAbility.isEmpty()) continue;

            ItemStack item = createSubAbilityDisplayItem(subAbility, selectableAbility);
            CompoundTag tag = new CompoundTag();
            tag.putString("AbilityId", abilityId);
            tag.putInt("SubAbilityIndex", i);
            item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            if (slot < 53) {
                container.setItem(slot, item);
                slot++;
            } else {
                break;
            }
        }

        ItemStack backItem = new ItemStack(Items.ARROW);
        backItem.set(DataComponents.CUSTOM_NAME, Component.literal("Back").withStyle(ChatFormatting.YELLOW));
        backItem.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Return to ability list").withStyle(ChatFormatting.GRAY)
        )));
        CompoundTag backTag = new CompoundTag();
        backTag.putBoolean("BackToSoulAbilities", true);
        backItem.set(DataComponents.CUSTOM_DATA, CustomData.of(backTag));
        container.setItem(53, backItem);

        final int containerSize = container.getContainerSize();
        Component title = Component.literal("Internal Underworld - " + prettyAbilityName(abilityId));

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
                        if (tag.contains("BackToSoulAbilities")) {
                            player.closeContainer();
                            level.getServer().execute(() -> openSoulAbilityGui(level, player, soulData));
                            return;
                        }
                        if (!tag.contains("AbilityId") || !tag.contains("SubAbilityIndex", Tag.TAG_INT)) return;

                        String selectedAbilityId = tag.getString("AbilityId");
                        int selectedIndex = tag.getInt("SubAbilityIndex");
                        Ability selected = LOTMCraft.abilityHandler.getById(selectedAbilityId);
                        if (!(selected instanceof SelectableAbility selectable)) return;
                        if (!selectable.isSubAbilityAllowed(player, selectedIndex)) {
                            player.sendSystemMessage(Component.literal("That sub-ability is locked for your sequence")
                                    .withStyle(ChatFormatting.RED));
                            return;
                        }

                        selectable.setSelectedAbility(player, selectedIndex);
                        player.closeContainer();

                        String[] nameKeys = selectable.getAbilityNamesCopy();
                        if (nameKeys.length > 0) {
                            String nameKey = nameKeys[Math.min(selectedIndex, nameKeys.length - 1)];
                            if (nameKey != null && !nameKey.isEmpty()) {
                                player.sendSystemMessage(Component.literal("Sub-ability selected: ")
                                        .append(Component.translatable(nameKey))
                                        .withStyle(ChatFormatting.AQUA));
                            }
                        }

                        if (bindOnSelect) {
                            level.getServer().execute(() -> openSoulBindSlotGui(level, player, selectedAbilityId));
                        } else {
                            level.getServer().execute(() -> queueSoulAbility(player, selectedAbilityId));
                        }
                    }
                },
                title
        ));
            // Swap the vanilla chest screen for the custom underworld UI.
            PacketHandler.sendToPlayer(player, new OpenInternalUnderworldAbilityScreenPacket());
    }

    private static void openSoulBindSlotGui(ServerLevel level, ServerPlayer player, String abilityId) {
        Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
        if (ability == null) return;

        ArrayList<String> abilities = new ArrayList<>(AbilityBarHelper.getAbilities(player));
        int maxSlots = 6;
        int currentCount = Math.min(abilities.size(), maxSlots);

        SimpleContainer container = new SimpleContainer(9) {
            @Override
            public boolean canTakeItem(Container target, int index, ItemStack stack) {
                return false;
            }
        };

        for (int i = 0; i < maxSlots; i++) {
            boolean enabled = i < currentCount || (i == currentCount && currentCount < maxSlots);
            ItemStack item = new ItemStack(enabled ? Items.LIME_STAINED_GLASS_PANE : Items.GRAY_STAINED_GLASS_PANE);
            item.set(DataComponents.CUSTOM_NAME, Component.literal("Bind to Slot " + (i + 1))
                    .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.literal(enabled ? "Assign to this keybind" : "Fill previous slots first")
                    .withStyle(ChatFormatting.GRAY));
            item.set(DataComponents.LORE, new ItemLore(lore));

            CompoundTag tag = new CompoundTag();
            tag.putInt("BindSlot", i);
            if (!enabled) {
                tag.putBoolean("BindLocked", true);
            }
            item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            container.setItem(i, item);
        }

        Component title = Component.literal("Bind Ability - " + prettyAbilityName(abilityId));
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x1, id, inv, container, 1) {
                    @Override
                    public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, Player clickPlayer) {
                        if (slotId < 0 || slotId >= container.getContainerSize()) return;
                        ItemStack clicked = container.getItem(slotId);
                        if (clicked.isEmpty()) return;

                        CustomData customData = clicked.get(DataComponents.CUSTOM_DATA);
                        if (customData == null) return;
                        CompoundTag tag = customData.copyTag();
                        if (!tag.contains("BindSlot", Tag.TAG_INT)) return;

                        if (tag.getBoolean("BindLocked")) {
                            player.sendSystemMessage(Component.literal("Fill previous slots first").withStyle(ChatFormatting.RED));
                            return;
                        }

                        int bindSlot = tag.getInt("BindSlot");
                        ArrayList<String> updated = new ArrayList<>(AbilityBarHelper.getAbilities(player));
                        if (updated.size() > maxSlots) {
                            updated = new ArrayList<>(updated.subList(0, maxSlots));
                        }

                        if (bindSlot < updated.size()) {
                            updated.set(bindSlot, abilityId);
                        } else if (bindSlot == updated.size() && updated.size() < maxSlots) {
                            updated.add(abilityId);
                        } else {
                            player.sendSystemMessage(Component.literal("Cannot skip empty slots").withStyle(ChatFormatting.RED));
                            return;
                        }

                        AbilityBarHelper.setAbilities(player, updated);
                        PacketHandler.sendToPlayer(player, new UpdateAbilityBarPacket(updated));
                        player.sendSystemMessage(Component.literal("Bound to slot " + (bindSlot + 1))
                                .withStyle(ChatFormatting.GREEN));
                        player.closeContainer();
                    }
                },
                title
        ));

        PacketHandler.sendToPlayer(player, new OpenInternalUnderworldAbilityScreenPacket());
    }

    private static ItemStack createAbilityDisplayItem(Ability ability) {
        ItemStack item = new ItemStack(Items.BOOK);
        item.set(DataComponents.CUSTOM_NAME, Component.literal(prettyAbilityName(ability.getId()))
                .withStyle(ChatFormatting.AQUA));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("Middle click to bind").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Cast as yourself").withStyle(ChatFormatting.GRAY));
        appendAbilityCostLore(ability, lore);
        item.set(DataComponents.LORE, new ItemLore(lore));
        return item;
    }

    private static ItemStack createAbilityDisplayItem(Ability ability, boolean showSubSelect) {
        ItemStack item = new ItemStack(Items.BOOK);
        item.set(DataComponents.CUSTOM_NAME, Component.literal(prettyAbilityName(ability.getId()))
                .withStyle(ChatFormatting.AQUA));
        List<Component> lore = new ArrayList<>();
        if (showSubSelect) {
            lore.add(Component.literal("Select sub-ability").withStyle(ChatFormatting.GRAY));
        }
        lore.add(Component.literal("Middle click to bind").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("Cast as yourself").withStyle(ChatFormatting.GRAY));
        appendAbilityCostLore(ability, lore);
        item.set(DataComponents.LORE, new ItemLore(lore));
        return item;
    }

    private static ItemStack createPassiveAbilityDisplayItem(PassiveAbilityItem abilityItem, ActiveSoulPassive activePassive, int cooldownTicks, int playerSequence) {
        // Show remaining duration/cooldown state for soul passives.
        ItemStack item = new ItemStack(abilityItem);
        List<Component> lore = new ArrayList<>();
        boolean isActive = activePassive != null && activePassive.ability() == abilityItem;
        int durationTicks = getPassiveDurationTicks(playerSequence);
        int baseCooldownTicks = getPassiveCooldownTicks(playerSequence);
        if (isActive) {
            item.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            String activeLabel = playerSequence <= 0
                    ? "Active: Infinite"
                    : "Active: " + formatRemainingTime(activePassive.remainingTicks());
            lore.add(Component.literal(activeLabel).withStyle(ChatFormatting.GRAY));
        } else if (cooldownTicks > 0) {
            lore.add(Component.literal("Duration: " + formatDurationLabel(durationTicks))
                .withStyle(ChatFormatting.GRAY));
            lore.add(Component.literal("Cooldown: " + formatRemainingTime(cooldownTicks))
                    .withStyle(ChatFormatting.RED));
        } else {
            if (playerSequence <= 0) {
                lore.add(Component.literal("Activate for Infinite").withStyle(ChatFormatting.GRAY));
            } else {
                lore.add(Component.literal("Activate for " + formatDurationLabel(durationTicks))
                        .withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("Cooldown: " + formatDurationLabel(baseCooldownTicks))
                    .withStyle(ChatFormatting.GRAY));
            }
        }
        item.set(DataComponents.LORE, new ItemLore(lore));
        return item;
    }

    private static ItemStack createSubAbilityDisplayItem(String subAbilityKey, Ability ability) {
        ItemStack item = new ItemStack(Items.PAPER);
        item.set(DataComponents.CUSTOM_NAME, Component.translatable(subAbilityKey)
            .withStyle(ChatFormatting.AQUA));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("Queue this ability").withStyle(ChatFormatting.GRAY));
        appendAbilityCostLore(ability, lore);
        item.set(DataComponents.LORE, new ItemLore(lore));
        return item;
    }

    private static void appendAbilityCostLore(Ability ability, List<Component> lore) {
        if (ability == null) {
            return;
        }
        int cooldownTicks = ability.getCooldown();
        if (cooldownTicks > 0) {
            int seconds = (cooldownTicks + 19) / 20;
            lore.add(Component.literal("Cooldown: " + seconds + "s").withStyle(ChatFormatting.GRAY));
        }
        float cost = ability.spiritualityCost();
        if (cost > 0) {
            lore.add(Component.literal("Spirituality Cost: " + formatSpiritualityCost(cost))
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    private static String formatSpiritualityCost(float cost) {
        if (Math.abs(cost - Math.round(cost)) < 0.01f) {
            return Integer.toString(Math.round(cost));
        }
        return String.format(Locale.US, "%.1f", cost);
    }

    private static int countAllowedSubAbilities(SelectableAbility ability, LivingEntity entity) {
        String[] names = ability.getAbilityNamesCopy();
        int count = 0;
        for (int i = 0; i < names.length; i++) {
            if (ability.isSubAbilityAllowed(entity, i)) {
                count++;
            }
        }
        return count;
    }

    private static int firstAllowedSubAbilityIndex(SelectableAbility ability, LivingEntity entity) {
        String[] names = ability.getAbilityNamesCopy();
        for (int i = 0; i < names.length; i++) {
            if (ability.isSubAbilityAllowed(entity, i)) {
                return i;
            }
        }
        return -1;
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

    private static boolean isAbilityAllowedFor(String pathway, int minSequence, Ability ability) {
        Integer reqSeq = ability.getRequirements().get(pathway);
        return reqSeq != null && minSequence <= reqSeq;
    }

    private static List<PassiveAbilityItem> getSoulPassives(String pathway, int soulSequence, int playerSequence) {
        if (pathway == null || pathway.isEmpty() || soulSequence < 0) return Collections.emptyList();
        if (playerSequence > 2) return Collections.emptyList();
        int minSequence = soulSequence;

        return PassiveAbilityHandler.ITEMS.getEntries().stream()
                .map(entry -> (PassiveAbilityItem) entry.get())
            .filter(item -> !(item instanceof PhysicalEnhancementsAbility))
            .filter(item -> isPassiveAllowedFor(pathway, minSequence, item))
                .toList();
    }

    private static boolean isPassiveAllowedFor(String pathway, int minSequence, PassiveAbilityItem item) {
        Integer reqSeq = item.getRequirements().get(pathway);
        return reqSeq != null && minSequence <= reqSeq;
    }

    private static PassiveAbilityItem resolvePassiveAbility(String passiveId) {
        if (passiveId == null || passiveId.isEmpty()) return null;
        ResourceLocation id = ResourceLocation.tryParse(passiveId);
        if (id == null) return null;
        if (BuiltInRegistries.ITEM.get(id) instanceof PassiveAbilityItem passiveAbility) {
            return passiveAbility;
        }
        return null;
    }

    private static void activateSoulPassive(ServerPlayer player, PassiveAbilityItem passiveAbility) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        ActiveSoulPassive existing = activeSoulPassives.get(player.getUUID());
        if (existing != null) {
            if (existing.ability() == passiveAbility) {
                cancelSoulPassive(player, existing);
                return;
            }
            player.sendSystemMessage(Component.literal("A passive is already active").withStyle(ChatFormatting.RED));
            return;
        }

        if (soulPassiveCooldowns.containsKey(player.getUUID()) && BeyonderData.getSequence(player) > 0) {
            player.sendSystemMessage(Component.literal("Your Passive Is On Cooldown")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        int sequence = BeyonderData.getSequence(player);
        int durationTicks = getPassiveDurationTicks(sequence);
        if (sequence <= 0) {
            durationTicks = 1;
        }
        passiveAbility.onPassiveAbilityGained(player, serverLevel);
        activeSoulPassives.put(player.getUUID(), new ActiveSoulPassive(passiveAbility, durationTicks));
        String activeLabel = sequence <= 0
            ? "Passive activated (Infinite): "
            : "Passive activated for " + formatDurationLabel(durationTicks) + ": ";
        player.sendSystemMessage(Component.literal(activeLabel)
            .append(passiveAbility.getName(new ItemStack(passiveAbility)))
            .withStyle(ChatFormatting.AQUA));
    }

    private static void cancelSoulPassive(ServerPlayer player, ActiveSoulPassive activePassive) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        activePassive.ability().onPassiveAbilityRemoved(player, serverLevel);
        activeSoulPassives.remove(player.getUUID());
        int sequence = BeyonderData.getSequence(player);
        if (sequence > 0) {
            int cooldownTicks = getPassiveCooldownTicks(sequence);
            soulPassiveCooldowns.put(player.getUUID(), cooldownTicks);
            player.sendSystemMessage(Component.literal("Passive canceled. Cooldown: " + formatDurationLabel(cooldownTicks))
                    .withStyle(ChatFormatting.RED));
        } else {
            player.sendSystemMessage(Component.literal("Passive canceled.").withStyle(ChatFormatting.RED));
        }
        updateSoulPassiveMenu(player);
    }

    private static void updateSoulPassiveMenu(ServerPlayer player) {
        if (!(player.containerMenu instanceof ChestMenu menu)) return;
        int playerSequence = BeyonderData.getSequence(player);
        if (playerSequence > 2) return;

        ActiveSoulPassive activePassive = activeSoulPassives.get(player.getUUID());
        boolean foundPassive = false;

        int cooldownTicks = soulPassiveCooldowns.getOrDefault(player.getUUID(), 0);
        for (int slotId = 45; slotId < 53; slotId++) {
            ItemStack stack = menu.getSlot(slotId).getItem();
            if (stack.isEmpty()) continue;

            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData == null) continue;
            CompoundTag tag = customData.copyTag();
            if (!tag.contains("PassiveId")) continue;

            PassiveAbilityItem passiveAbility = resolvePassiveAbility(tag.getString("PassiveId"));
            if (passiveAbility == null) continue;

            ItemStack updated = createPassiveAbilityDisplayItem(passiveAbility, activePassive, cooldownTicks, playerSequence);
            CompoundTag newTag = new CompoundTag();
            newTag.putString("PassiveId", tag.getString("PassiveId"));
            updated.set(DataComponents.CUSTOM_DATA, CustomData.of(newTag));
            menu.getSlot(slotId).set(updated);
            foundPassive = true;
        }

        if (foundPassive) {
            menu.broadcastChanges();
        }
    }

    private static String formatRemainingTime(int remainingTicks) {
        int totalSeconds = Math.max(0, (remainingTicks + 19) / 20);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    private static String formatDurationLabel(int ticks) {
        int minutes = Math.max(1, (ticks + 1199) / 1200);
        return minutes + "m";
    }

    private static int getPassiveDurationTicks(int sequence) {
        if (sequence <= 0) return 20 * 60 * 10;
        if (sequence == 1) return 20 * 60 * 5;
        if (sequence == 2) return 20 * 60;
        return SOUL_PASSIVE_DURATION_TICKS;
    }

    private static int getPassiveCooldownTicks(int sequence) {
        if (sequence <= 0) return 20 * 60 * 3;
        if (sequence == 1) return 20 * 60 * 5;
        if (sequence == 2) return 20 * 60 * 10;
        return SOUL_PASSIVE_COOLDOWN_TICKS;
    }

    private static void castSoulAbility(ServerLevel level, ServerPlayer player, String abilityId) {
        Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
        if (ability == null) return;

        ability.useAbility(level, player, true, false, false);
    }

    private static void queueSoulAbility(ServerPlayer player, String abilityId) {
        if (abilityId == null || abilityId.isEmpty()) return;
        // Queue the ability for the next right-click action in the world.
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

        // Consume and immediately cast as the player.
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
        // Prune dead souls and match by entity UUID.
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


    private static void releaseAllSouls(ServerLevel level, ServerPlayer player) {
        // Summon every stored soul as an active underworld entity.
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

    private static void releaseSoul(ServerLevel level, ServerPlayer player, CompoundTag soulData) {
        try {
            // Recreate the entity and mark it as an underworld summon.
            String entityTypeId = soulData.getString("EntityType");
            boolean isPlayerSoul = "minecraft:player".equals(entityTypeId) || soulData.getBoolean("IsPlayerSoul");
            boolean isBeyonderNpc = soulData.getBoolean("IsBeyonderNPC");
            Optional<EntityType<?>> optionalType = EntityType.byString(entityTypeId);
            if (!isPlayerSoul && !isBeyonderNpc && optionalType.isEmpty()) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.failed").withStyle(ChatFormatting.RED));
                return;
            }
            net.minecraft.world.entity.Entity entity;

            if (isPlayerSoul || isBeyonderNpc) {
                String pathway = soulData.contains("Pathway") ? soulData.getString("Pathway") : "none";
                int sequence = soulData.contains("Sequence", Tag.TAG_INT)
                        ? soulData.getInt("Sequence")
                        : LOTMCraft.NON_BEYONDER_SEQ;
                boolean hostile = soulData.getBoolean("BeyonderHostile");
                String skin = soulData.getString("BeyonderSkin");

                EntityType<? extends BeyonderNPCEntity> npcType = ModEntities.BEYONDER_NPC.get();
                if (skin != null && !skin.isEmpty()) {
                    entity = new BeyonderNPCEntity(npcType, level, hostile, skin, pathway, sequence, true);
                } else {
                    entity = new BeyonderNPCEntity(npcType, level, hostile, pathway, sequence, true);
                }

                if (entity instanceof BeyonderNPCEntity npc) {
                    npc.setQuestId("");
                    if (isPlayerSoul && soulData.contains("OriginalPlayerUUID")) {
                        npc.setTargetPlayerUUID(soulData.getUUID("OriginalPlayerUUID"));
                    }
                }
                if (soulData.contains("DisplayName")) {
                    entity.setCustomName(Component.literal(soulData.getString("DisplayName")));
                    entity.setCustomNameVisible(true);
                }
            } else {
                entity = optionalType.get().create(level);
                if (entity == null) {
                    player.sendSystemMessage(Component.translatable("ability.lotmcraft.internal_underworld.failed").withStyle(ChatFormatting.RED));
                    return;
                }

                if (soulData.contains("EntityNBT")) {
                    CompoundTag nbt = soulData.getCompound("EntityNBT").copy();
                    nbt.remove("UUID");
                    entity.load(nbt);
                }
            }

            Vec3 look = player.getLookAngle();
            Vec3 pos = player.position().add(look.x * 2, 0, look.z * 2);
            entity.moveTo(pos.x, pos.y, pos.z, player.getYRot(), 0);
            entity.setUUID(UUID.randomUUID());
            entity.getPersistentData().putBoolean("VoidSummoned", true);
            entity.getPersistentData().putBoolean("UnderworldSummonedSoul", true);

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
        // Recall active souls back into storage and remove their entities.
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
        // Safety cleanup when the owner leaves the server.
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

    public static void clearStoredSouls(ServerPlayer player) {
        player.getPersistentData().remove(STORED_SOULS_TAG);
    }

    /**
     * Guaranteed soul capture triggered when a player dies inside River's Call.
     * Bypasses the normal random roll — the River itself delivers the soul.
     * If the captor's Internal Underworld is full the lowest-sequence soul is displaced.
     *
     * @param captor the death-path player who receives the soul
     * @param victim the player who died inside River's Call
     * @return true if the soul was successfully stored
     */
    public static boolean tryCaptureRiverVictim(ServerPlayer captor, ServerPlayer victim) {
        if (!BeyonderData.isBeyonder(captor)) return false;
        if (!BeyonderData.getPathway(captor).equals("death")) return false;
        int seq = BeyonderData.getSequence(captor);
        if (seq > 5) return false;

        if (getStoredSouls(captor).size() >= getMaxSouls(seq)) {
            removeLowestSequenceSoul(captor);
        }

        CompoundTag soulData = buildSoulData(victim);
        if (soulData == null) return false;
        victim.getPersistentData().putBoolean(INTERNAL_UNDERWORLD_CAPTURED_TAG, true);
        addStoredSoul(captor, soulData);
        ServerLevel level = (ServerLevel) victim.level();
        spawnCaptureSuccessParticles(level, victim);
        level.playSound(null, victim.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0f, 0.7f);
        captor.sendSystemMessage(Component.literal(
                "The River delivered " + victim.getName().getString() + "'s soul to your Internal Underworld.")
                .withStyle(ChatFormatting.DARK_AQUA));
        return true;
    }

    public static FreedSoulSlots clearStoredSoulsAndCollectFreedPaths(ServerPlayer player) {
        // Used on death to free global seq0/seq1 slots from stored souls.
        CompoundTag data = player.getPersistentData();
        if (!data.contains(STORED_SOULS_TAG, Tag.TAG_LIST)) {
            return new FreedSoulSlots(Collections.emptySet(), Collections.emptySet());
        }

        ListTag list = data.getList(STORED_SOULS_TAG, Tag.TAG_COMPOUND);
        Set<String> seq0Paths = new HashSet<>();
        Set<String> seq1Paths = new HashSet<>();

        for (int i = 0; i < list.size(); i++) {
            CompoundTag soul = list.getCompound(i);
            if (!soul.contains("Sequence", Tag.TAG_INT)) {
                continue;
            }
            int sequence = soul.getInt("Sequence");
            if (sequence != 0 && sequence != 1) {
                continue;
            }
            String pathway = soul.getString("Pathway");
            if (pathway == null || pathway.isEmpty() || "none".equals(pathway)) {
                continue;
            }
            if (sequence == 0) {
                seq0Paths.add(pathway);
            } else {
                seq1Paths.add(pathway);
            }
        }

        data.remove(STORED_SOULS_TAG);
        return new FreedSoulSlots(seq0Paths, seq1Paths);
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

    private static ItemStack createUnderworldLockItem(ServerPlayer player) {
        // Toggle to prevent new souls from entering via death capture.
        boolean locked = isInternalUnderworldLocked(player);
        ItemStack item = new ItemStack(locked ? Items.IRON_DOOR : Items.OAK_DOOR);
        item.set(DataComponents.CUSTOM_NAME, Component.literal("Underworld Lock: " + (locked ? "ON" : "OFF"))
                .withStyle(locked ? ChatFormatting.RED : ChatFormatting.GREEN));
        item.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Prevents new souls from entering").withStyle(ChatFormatting.GRAY)
        )));
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("IsUnderworldLock", true);
        item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return item;
    }

    private static void refreshSoulListContainer(SimpleContainer container, ServerPlayer player) {
        int startSlot = 3;
        for (int i = startSlot; i < container.getContainerSize(); i++) {
            container.setItem(i, ItemStack.EMPTY);
        }

        List<CompoundTag> storedSouls = getStoredSouls(player);
        populateSoulListContainer(container, player, storedSouls);
    }

    private static void populateSoulListContainer(SimpleContainer container, ServerPlayer player, List<CompoundTag> storedSouls) {
        List<CompoundTag> favorited = new ArrayList<>();
        List<CompoundTag> normal = new ArrayList<>();
        for (CompoundTag soul : storedSouls) {
            if (soul.getBoolean(FAVORITED_SOUL_TAG)) {
                favorited.add(soul);
            } else {
                normal.add(soul);
            }
        }

        int favoriteRowEnd = Math.min(FAVORITED_ROW_END_SLOT, container.getContainerSize() - 1);
        if (FAVORITED_ROW_START_SLOT <= favoriteRowEnd) {
            int favIndex = 0;
            for (int slot = FAVORITED_ROW_START_SLOT; slot <= favoriteRowEnd; slot++) {
                if (favIndex < favorited.size()) {
                    container.setItem(slot, createSoulDisplayItem(favorited.get(favIndex++)));
                } else {
                    container.setItem(slot, createFavoritedPlaceholderItem());
                }
            }
        }

        int normalIndex = 0;
        for (int slot = 3; slot < container.getContainerSize() && normalIndex < normal.size(); slot++) {
            if (slot >= FAVORITED_ROW_START_SLOT && slot <= FAVORITED_ROW_END_SLOT) {
                continue;
            }
            container.setItem(slot, createSoulDisplayItem(normal.get(normalIndex++)));
        }
    }

    private static ItemStack createFavoritedPlaceholderItem() {
        ItemStack item = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
        item.set(DataComponents.CUSTOM_NAME, Component.literal("Favorited Souls")
                .withStyle(ChatFormatting.DARK_GRAY));
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("IsFavoritedSlot", true);
        item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return item;
    }

    private static ItemStack createSoulDisplayItem(CompoundTag soulData) {
        // Display entry for the soul list GUI.
        ItemStack item = createSoulIconItem(soulData);
        String pathway = soulData.contains("Pathway") ? soulData.getString("Pathway") : "none";
        int sequence = soulData.contains("Sequence", Tag.TAG_INT) ? soulData.getInt("Sequence") : -1;
        String sequenceText = sequence >= 0 ? Integer.toString(sequence) : "-";

        int nameColor = 0xFFFFFFFF;
        int detailColor = 0xFFCCCCCC;
        int pathwayColor = "none".equals(pathway) ? detailColor : getPathwayColorOrDefault(pathway, detailColor);
        int separatorColor = 0xFF2B2B2B;
        Component entityLabel = getSoulEntityLabel(soulData);
        boolean simpleEntityLabel = soulData.getBoolean("IsBeyonderNPC") || soulData.getBoolean("IsPlayerSoul");
        Component entityLine = simpleEntityLabel
            ? entityLabel.copy().withStyle(style -> style.withColor(detailColor).withItalic(false))
            : buildLabelValue("Entity: ", entityLabel, detailColor);
        boolean isFavorited = soulData.getBoolean(FAVORITED_SOUL_TAG);
        Component favoriteHint = Component.literal(isFavorited ? "Middle click to unfavorite" : "Middle click to favorite")
            .withStyle(style -> style.withColor(detailColor).withItalic(false));

        item.set(DataComponents.CUSTOM_NAME, Component.literal(soulData.getString("DisplayName"))
            .withStyle(style -> style.withColor(nameColor).withItalic(false)));
        item.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("-------------------").withStyle(style -> style.withColor(separatorColor).withItalic(false)),
            entityLine,
            buildLabelValue("Pathway: ", Component.literal(pathway), pathwayColor),
            buildLabelValue("Sequence: ", Component.literal(sequenceText), pathwayColor),
            favoriteHint
        )));

        CompoundTag tag = new CompoundTag();
        tag.put("SoulData", soulData);
        item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        return item;
    }

    private static ItemStack createSoulIconItem(CompoundTag soulData) {
        if (soulData.getBoolean("IsPlayerSoul")) {
            return createPlayerSoulHead(soulData);
        }

        if (soulData.getBoolean("IsBeyonderNPC")) {
            return createPathwaySymbolItem(soulData);
        }

        return new ItemStack(Items.PLAYER_HEAD);
    }

    private static Component getSoulEntityLabel(CompoundTag soulData) {
        if (soulData.getBoolean("IsBeyonderNPC")) {
            return Component.literal("Beyonder NPC");
        }
        if (soulData.getBoolean("IsPlayerSoul")) {
            return Component.literal("Player");
        }

        String entityTypeId = soulData.getString("EntityType");
        Optional<EntityType<?>> entityType = EntityType.byString(entityTypeId);
        if (entityType.isPresent()) {
            return Component.translatable(entityType.get().getDescriptionId());
        }
        if (entityTypeId != null && !entityTypeId.isEmpty()) {
            return Component.literal(prettyEntityName(entityTypeId));
        }
        return Component.literal("Unknown");
    }

    private static Component buildLabelValue(String label, Component value, int valueColor) {
        int labelColor = 0xFFAAAAAA;
        return Component.literal(label)
                .withStyle(style -> style.withColor(labelColor).withItalic(false))
                .append(value.copy().withStyle(style -> style.withColor(valueColor).withItalic(false)));
    }

    private static String prettyEntityName(String id) {
        String raw = id;
        int colonIndex = raw.indexOf(':');
        if (colonIndex >= 0 && colonIndex + 1 < raw.length()) {
            raw = raw.substring(colonIndex + 1);
        }
        raw = raw.replace('_', ' ');
        String[] parts = raw.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) sb.append(part.substring(1));
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    private static ItemStack createPlayerSoulHead(CompoundTag soulData) {
        ItemStack item = new ItemStack(Items.PLAYER_HEAD);
        if (soulData.hasUUID("OriginalPlayerUUID")) {
            UUID playerId = soulData.getUUID("OriginalPlayerUUID");
            String profileName = soulData.getString("OriginalPlayerName");
            if (profileName == null || profileName.trim().isEmpty()) {
                profileName = soulData.getString("DisplayName");
            }
            if (profileName != null && profileName.trim().isEmpty()) {
                profileName = null;
            }

            GameProfile profile = new GameProfile(playerId, profileName);
            item.set(DataComponents.PROFILE, new ResolvableProfile(profile));
        }
        return item;
    }

    private static ItemStack createPathwaySymbolItem(CompoundTag soulData) {
        String pathway = soulData.contains("Pathway") ? soulData.getString("Pathway") : "";
        int sequence = soulData.contains("Sequence", Tag.TAG_INT) ? soulData.getInt("Sequence") : -1;

        if (pathway.isEmpty()) {
            return new ItemStack(Items.NAME_TAG);
        }

        if (sequence == 0) {
            ItemStack uniqueness = getUniquenessItemStack(pathway);
            if (!uniqueness.isEmpty()) {
                return uniqueness;
            }
        }

        if (sequence >= 1) {
            ItemStack characteristic = createCharacteristicStack(pathway, sequence);
            if (!characteristic.isEmpty()) {
                return characteristic;
            }
        }

        ItemStack fallbackCharacteristic = createCharacteristicStack(pathway, sequence);
        if (!fallbackCharacteristic.isEmpty()) {
            return fallbackCharacteristic;
        }

        return new ItemStack(Items.NAME_TAG);
    }

    private static ItemStack createCharacteristicStack(String pathway, int sequence) {
        BeyonderCharacteristicItem characteristic = null;
        if (sequence > 0) {
            characteristic = BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(pathway, sequence);
        } else {
            List<BeyonderCharacteristicItem> all = BeyonderCharacteristicItemHandler.selectAllOfPathway(pathway);
            if (!all.isEmpty()) {
                characteristic = all.get(0);
            }
        }

        if (characteristic == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(characteristic);
        applySequenceCount(stack, sequence);
        return stack;
    }

    private static void applySequenceCount(ItemStack stack, int sequence) {
        if (sequence <= 1) {
            return;
        }
        int max = Math.max(1, stack.getMaxStackSize());
        int count = Math.min(sequence, max);
        if (count > 1) {
            stack.setCount(count);
        }
    }

    private static ItemStack getUniquenessItemStack(String pathway) {
        try {
            Item item = BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, pathway + "_uniqueness")
            );
            if (item == Items.AIR) return ItemStack.EMPTY;
            return new ItemStack(item);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    private static int getPathwayColorOrDefault(String pathway, int fallback) {
        if (pathway == null || pathway.isEmpty()) return fallback;
        PathwayInfos infos = BeyonderData.pathwayInfos.get(pathway);
        return infos != null ? infos.color() : fallback;
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
        // Persist the snapshot in player NBT, trimming to capacity.
        CompoundTag data = player.getPersistentData();
        ListTag list = data.contains(STORED_SOULS_TAG)
                ? data.getList(STORED_SOULS_TAG, Tag.TAG_COMPOUND)
                : new ListTag();

        list.add(soulData);

        int maxSouls = getMaxSouls(BeyonderData.getSequence(player));
        while (list.size() > maxSouls) list.remove(0);

        data.put(STORED_SOULS_TAG, list);
    }

    private static void removeLowestSequenceSoul(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(STORED_SOULS_TAG)) return;

        ListTag list = data.getList(STORED_SOULS_TAG, Tag.TAG_COMPOUND);
        if (list.isEmpty()) return;

        // Remove the weakest soul (highest sequence number) first.
        int worstIndex = -1;
        int worstSeq = -1;
        for (int i = 0; i < list.size(); i++) {
            CompoundTag soul = list.getCompound(i);
            int seq = soul.contains("Sequence", Tag.TAG_INT)
                    ? soul.getInt("Sequence")
                    : LOTMCraft.NON_BEYONDER_SEQ;
            if (seq > worstSeq) {
                worstSeq = seq;
                worstIndex = i;
            }
        }

        if (worstIndex >= 0) {
            list.remove(worstIndex);
            data.put(STORED_SOULS_TAG, list);
        }
    }

    private static void removeStoredSoul(ServerPlayer player, CompoundTag soulData) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(STORED_SOULS_TAG)) return;
        ListTag list = data.getList(STORED_SOULS_TAG, Tag.TAG_COMPOUND);
        list.remove(soulData);
        data.put(STORED_SOULS_TAG, list);
    }

    private static boolean toggleSoulFavorite(ServerPlayer player, CompoundTag soulData) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(STORED_SOULS_TAG, Tag.TAG_LIST)) return false;
        ListTag list = data.getList(STORED_SOULS_TAG, Tag.TAG_COMPOUND);

        String soulKey = soulData.getString(SOUL_KEY_TAG);
        int favoritedCount = countFavoritedSouls(list);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag stored = list.getCompound(i);
            if (!soulKey.isEmpty() && soulKey.equals(stored.getString(SOUL_KEY_TAG))) {
                return applyFavoriteToggle(player, data, list, stored, favoritedCount);
            }
        }

        for (int i = 0; i < list.size(); i++) {
            CompoundTag stored = list.getCompound(i);
            if (matchesSoul(stored, soulData)) {
                if (!stored.contains(SOUL_KEY_TAG, Tag.TAG_STRING)) {
                    stored.putString(SOUL_KEY_TAG, UUID.randomUUID().toString());
                }
                return applyFavoriteToggle(player, data, list, stored, favoritedCount);
            }
        }

        return false;
    }

    private static boolean applyFavoriteToggle(ServerPlayer player, CompoundTag data, ListTag list, CompoundTag stored, int favoritedCount) {
        boolean isFavorited = stored.getBoolean(FAVORITED_SOUL_TAG);
        if (!isFavorited && favoritedCount >= 9) {
            player.sendSystemMessage(Component.literal("Favorited souls row is full")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        boolean newValue = !isFavorited;
        stored.putBoolean(FAVORITED_SOUL_TAG, newValue);
        data.put(STORED_SOULS_TAG, list);
        player.sendSystemMessage(Component.literal(newValue ? "Soul favorited" : "Soul unfavorited")
                .withStyle(newValue ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        return true;
    }

    private static int countFavoritedSouls(ListTag list) {
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.getCompound(i).getBoolean(FAVORITED_SOUL_TAG)) {
                count++;
            }
        }
        return count;
    }

    private static boolean matchesSoul(CompoundTag stored, CompoundTag soulData) {
        boolean storedPlayer = stored.getBoolean("IsPlayerSoul");
        boolean targetPlayer = soulData.getBoolean("IsPlayerSoul");
        if (storedPlayer != targetPlayer) return false;

        if (storedPlayer && stored.hasUUID("OriginalPlayerUUID") && soulData.hasUUID("OriginalPlayerUUID")) {
            return stored.getUUID("OriginalPlayerUUID").equals(soulData.getUUID("OriginalPlayerUUID"));
        }

        if (!stored.getString("EntityType").equals(soulData.getString("EntityType"))) return false;
        if (!stored.getString("DisplayName").equals(soulData.getString("DisplayName"))) return false;

        if (stored.getBoolean("IsBeyonderNPC") != soulData.getBoolean("IsBeyonderNPC")) return false;

        if (stored.contains("Pathway") || soulData.contains("Pathway")) {
            if (!stored.getString("Pathway").equals(soulData.getString("Pathway"))) return false;
        }

        if (stored.contains("Sequence", Tag.TAG_INT) || soulData.contains("Sequence", Tag.TAG_INT)) {
            if (stored.getInt("Sequence") != soulData.getInt("Sequence")) return false;
        }

        String storedSkin = stored.getString("BeyonderSkin");
        String targetSkin = soulData.getString("BeyonderSkin");
        return storedSkin.equals(targetSkin);
    }

    public static int countActiveSouls(String pathway, int sequence) {
        // Used by global slot checks to count summoned underworld souls.
        if (pathway == null || pathway.isEmpty()) return 0;
        int count = 0;
        for (List<ActiveSoul> souls : activeSouls.values()) {
            for (ActiveSoul soul : souls) {
                if (!soul.entity().isAlive()) continue;
                CompoundTag data = soul.soulData();
                if (!pathway.equals(data.getString("Pathway"))) continue;
                if (!data.contains("Sequence", Tag.TAG_INT)) continue;
                if (data.getInt("Sequence") == sequence) {
                    count++;
                }
            }
        }
        return count;
    }
}