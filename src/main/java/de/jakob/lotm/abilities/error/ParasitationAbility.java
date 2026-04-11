package de.jakob.lotm.abilities.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityHandler;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.error.handler.TheftHandler;
import de.jakob.lotm.attachments.ControllingDataComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.ParasitationComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.custom.ability_entities.OriginalBodyEntity;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ControllingUtil;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.marionettes.MarionetteUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ParasitationAbility extends SelectableAbility {

    private static final HashMap<UUID, UUID> concealedMap = new HashMap<>();
    private static final HashMap<UUID, UUID> controllingMap = new HashMap<>();
    private static final HashMap<UUID, Integer> controllingTimer = new HashMap<>();
    private static final HashMap<UUID, Boolean> controllingLowerSeq = new HashMap<>();
    private static final HashMap<UUID, UUID> originalBodyMap = new HashMap<>();

    public ParasitationAbility(String id) {
        super(id, 10f, "corruption");
        canBeUsedByNPC = false;
        canBeCopied = false;
        canBeReplicated = false;
        canBeUsedInArtifact = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 1;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.parasitation.controlling",
                "ability.lotmcraft.parasitation.concealed"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch (abilityIndex) {
            case 0 -> controlling(level, entity);
            case 1 -> concealed(level, entity);
        }
    }


    private void controlling(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer player)) return;

        // If currently concealed, switch to controlling
        if (concealedMap.containsKey(player.getUUID())) {
            LivingEntity currentHost = resolveHost(serverLevel, concealedMap.get(player.getUUID()));
            LivingEntity target = AbilityUtil.getTargetEntity(entity, 8, 2);
            LivingEntity newHost = (target != null && (currentHost == null || !target.getUUID().equals(currentHost.getUUID()))) ? target : currentHost;

            if (newHost != null && !(newHost instanceof Player)) {
                cancelConcealed(serverLevel, player, false);
                attemptControl(serverLevel, player, newHost);
            }
            return;
        }

        // Already controlling — exit
        if (controllingMap.containsKey(player.getUUID())) {
            exitControl(serverLevel, player);
            return;
        }

        LivingEntity host = AbilityUtil.getTargetEntity(entity, 8, 2);
        if (host == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.parasitation.no_target").withColor(0x3240bf));
            return;
        }
        if (host instanceof Player) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.parasitation.target_too_strong").withColor(0xbf3232));
            return;
        }

        attemptControl(serverLevel, player, host);
    }

    private void attemptControl(ServerLevel serverLevel, ServerPlayer player, LivingEntity target) {
        // 0.05 heart to trigger substitutes/swaps
        target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, player), 0.1f);

        if (!target.isAlive() || target.isRemoved()) {
            AbilityUtil.sendActionBar(player, Component.literal("§cTarget evaded!"));
            return;
        }

        int userSeq = BeyonderData.getSequence(player);
        int targetSeq = BeyonderData.isBeyonder(target) ? BeyonderData.getSequence(target) : 10;
        boolean lowerSeq = targetSeq > userSeq;

        // 10% vs lower seq, 33% against same/higher
        float chance = lowerSeq ? 0.10f : 0.33f;
        if (random.nextFloat() >= chance) {
            AbilityUtil.sendActionBar(player, Component.literal(lowerSeq
                    ? "§cControl failed!"
                    : "§cControl failed — resistance too strong!"));
            return;
        }

        startControl(serverLevel, player, target, lowerSeq);
    }

    private void startControl(ServerLevel serverLevel, ServerPlayer player, LivingEntity target, boolean lowerSeq) {
        UUID hostUUID = target.getUUID();
        controllingMap.put(player.getUUID(), hostUUID);
        controllingLowerSeq.put(player.getUUID(), lowerSeq);

        if (!lowerSeq) {
            controllingTimer.put(player.getUUID(), 100);
        }

        ParasitationComponent pc = target.getData(ModAttachments.PARASITE_COMPONENT);
        pc.setParasited(true);
        pc.setParasiteUUID(player.getUUID());

        //make them a marionette first, then possess (i need to change it to hostutil or something cuz i dont want marionette item
        if (lowerSeq) {
            MarionetteUtils.turnEntityIntoMarionette(target, player);
        }

        ControllingUtil.possess(player, target);
        ControllingDataComponent data = player.getData(ModAttachments.CONTROLLING_DATA);
        if (data.getBodyUUID() != null) {
            originalBodyMap.put(player.getUUID(), data.getBodyUUID());
            final UUID bodyId = data.getBodyUUID();
            serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                    serverLevel.getServer().getTickCount() + 3, () -> {
                Entity bodyEntity = serverLevel.getEntity(bodyId);
                if (bodyEntity instanceof OriginalBodyEntity body) {
                    body.setParasiteControlled(true);
                }
            }));
        }

        serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                serverLevel.getServer().getTickCount() + 3, () -> {
            ControllingDataComponent d = player.getData(ModAttachments.CONTROLLING_DATA);
            if (d.getBodyUUID() != null) {
                Entity bodyEntity = serverLevel.getEntity(d.getBodyUUID());
                if (bodyEntity instanceof OriginalBodyEntity body) {
                    body.setInvisible(true);
                    body.noPhysics = true;
                    body.setNoGravity(true);
                }
            }
        }));
    }

    private static void exitControl(ServerLevel serverLevel, ServerPlayer player) {
        if (!controllingMap.containsKey(player.getUUID())) return;
        boolean lowerSeq = controllingLowerSeq.getOrDefault(player.getUUID(), false);
        Vec3 exitPos = null;
        UUID bodyUUID = originalBodyMap.get(player.getUUID());
        UUID hostUUID = controllingMap.get(player.getUUID());

        if (bodyUUID != null) {
            Entity bodyEntity = serverLevel.getEntity(bodyUUID);
            if (bodyEntity != null) exitPos = bodyEntity.position();
        }

// back to behind the host if body position unavailable
        if (exitPos == null) {
            Entity hostEntity = serverLevel.getEntity(hostUUID);
            if (hostEntity != null) {
                Vec3 hostLook = new Vec3(hostEntity.getLookAngle().x(), 0, hostEntity.getLookAngle().z()).normalize();
                exitPos = hostEntity.position().subtract(hostLook.scale(hostEntity.getBbWidth() + 1.0));
            }
        }

        controllingMap.remove(player.getUUID());
        controllingTimer.remove(player.getUUID());
        controllingLowerSeq.remove(player.getUUID());
        originalBodyMap.remove(player.getUUID());

        ControllingUtil.reset(player, serverLevel, true);

        player.getInventory().items.removeIf(stack -> {
            if (!stack.is(ModItems.MARIONETTE_CONTROLLER.get())) return false;
            var tag = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (tag == null) return false;
            return tag.copyTag().getString("MarionetteUUID").equals(hostUUID.toString());
        });
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(ModItems.MARIONETTE_CONTROLLER.get())) {
            var tag = offhand.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (tag != null && tag.copyTag().getString("MarionetteUUID").equals(hostUUID.toString())) {
                player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            }
        }

        if (exitPos != null) {
            player.teleportTo(exitPos.x, exitPos.y, exitPos.z);
        }

        Entity hostEntity = serverLevel.getEntity(hostUUID);
        if (hostEntity instanceof LivingEntity host) {
            host.setInvisible(false);
            host.removeEffect(MobEffects.INVISIBILITY);
            host.setDeltaMovement(Vec3.ZERO);
            if (host instanceof Mob mob) mob.getNavigation().stop();

            ParasitationComponent pc = host.getData(ModAttachments.PARASITE_COMPONENT);
            pc.setParasited(false);
            pc.setParasiteUUID(null);

            if (!lowerSeq) {
                boolean killedBySteal = performExitSteal(serverLevel, player, host);
                if (killedBySteal) MarionetteUtils.turnEntityIntoMarionette(host, player);
            }
        }
    }

    private static boolean performExitSteal(ServerLevel serverLevel, ServerPlayer player, LivingEntity host) {
        float roll = new Random().nextFloat();

        Ability instance = LOTMCraft.abilityHandler.getById("parasitation_ability");

        if (roll < 0.50f) {
            stealArmor(player, host);
        } else if (roll < 0.75f) {
            TheftHandler.stealItemsFromEntity(host, player, instance);
        } else if (roll < 0.90f) {
            TheftHandler.performAbilityTheft(serverLevel, player, host, new Random(), true, instance);
        } else {
            // Health drain — check if it kills
            float drain = host.getMaxHealth() * 0.2f;
            host.setHealth(host.getHealth() - drain);
            if (host.getHealth() <= 0) {
                host.kill();
                return true;
            }
        }
        return false;
    }

    private static void stealArmor(ServerPlayer player, LivingEntity host) {
        Ability instance = LOTMCraft.abilityHandler.getById("parasitation_ability");


        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armor = host.getItemBySlot(slot);
            if (!armor.isEmpty()) {
                if (!player.getInventory().add(armor.copy())) {
                    player.drop(armor.copy(), false);
                }
                host.setItemSlot(slot, ItemStack.EMPTY);
                return;
            }
        }
        TheftHandler.stealItemsFromEntity(host, player, instance);
    }

    //concealmedode
    private void concealed(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        // If currently controlling, switch to concealed (this needs to be updated but will be useful when i can figure out how to grant the original bodies ability to controll selfs
        if (controllingMap.containsKey(entity.getUUID())) {
            if (!(entity instanceof ServerPlayer player)) return;
            LivingEntity currentHost = resolveHost(serverLevel, controllingMap.get(player.getUUID()));
            LivingEntity target = AbilityUtil.getTargetEntity(entity, 8, 2);
            LivingEntity newHost = (target != null && (currentHost == null || !target.getUUID().equals(currentHost.getUUID()))) ? target : currentHost;

            if (newHost != null && isValidConcealedTarget(entity, newHost)) {
                exitControl(serverLevel, player);
                startConcealed(serverLevel, entity, newHost);
            } else {
                AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.parasitation.target_too_strong").withColor(0xbf3232));
            }
            return;
        }

        // Already concealed
        if (concealedMap.containsKey(entity.getUUID())) {
            UUID currentHostUUID = concealedMap.get(entity.getUUID());
            LivingEntity target = AbilityUtil.getTargetEntity(entity, 8, 2);

            if (target != null && !target.getUUID().equals(currentHostUUID) && isValidConcealedTarget(entity, target)) {
                switchConcealedHost(serverLevel, entity, target);
            } else {
                cancelConcealed(serverLevel, entity, true);
            }
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 8, 2);
        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.parasitation.no_target").withColor(0x3240bf));
            return;
        }
        if (!isValidConcealedTarget(entity, target)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.parasitation.target_too_strong").withColor(0xbf3232));
            return;
        }

        startConcealed(serverLevel, entity, target);
    }

    private boolean isValidConcealedTarget(LivingEntity entity, LivingEntity target) {
        if (!BeyonderData.isBeyonder(target)) return true;
        return BeyonderData.getSequence(target) > BeyonderData.getSequence(entity);
    }

    private void startConcealed(ServerLevel serverLevel, LivingEntity entity, LivingEntity host) {
        concealedMap.put(entity.getUUID(), host.getUUID());

        ParasitationComponent pc = host.getData(ModAttachments.PARASITE_COMPONENT);
        pc.setParasited(true);
        pc.setParasiteUUID(entity.getUUID());

        entity.setInvisible(true);
        if (entity instanceof Player player) {
            player.setBoundingBox(new AABB(
                    player.getX(), player.getY(), player.getZ(),
                    player.getX(), player.getY(), player.getZ()
            ));
            player.onUpdateAbilities();
            player.hurtMarked = true;
        }
    }

    private void switchConcealedHost(ServerLevel serverLevel, LivingEntity entity, LivingEntity newHost) {
        Entity oldHostEntity = serverLevel.getEntity(concealedMap.get(entity.getUUID()));
        if (oldHostEntity instanceof LivingEntity oldHost) {
            ParasitationComponent pc = oldHost.getData(ModAttachments.PARASITE_COMPONENT);
            pc.setParasited(false);
            pc.setParasiteUUID(null);
        }
        concealedMap.put(entity.getUUID(), newHost.getUUID());
        ParasitationComponent pc = newHost.getData(ModAttachments.PARASITE_COMPONENT);
        pc.setParasited(true);
        pc.setParasiteUUID(entity.getUUID());
    }

    private void cancelConcealed(ServerLevel serverLevel, LivingEntity entity, boolean restoreVisibility) {
        if (concealedMap.containsKey(entity.getUUID())) {
            Entity hostEntity = serverLevel.getEntity(concealedMap.get(entity.getUUID()));
            if (hostEntity instanceof LivingEntity host) {
                ParasitationComponent pc = host.getData(ModAttachments.PARASITE_COMPONENT);
                pc.setParasited(false);
                pc.setParasiteUUID(null);
            }
        }
        concealedMap.remove(entity.getUUID());

        if (restoreVisibility) {
            entity.setInvisible(false);
            entity.removeEffect(MobEffects.INVISIBILITY);
            restorePlayer(entity);
        }
    }


    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        tickControlling(serverLevel, entity);
        tickConcealed(serverLevel, entity);
    }

    private static void tickControlling(ServerLevel serverLevel, LivingEntity entity) {
        if (!controllingMap.containsKey(entity.getUUID())) return;
        if (!(entity instanceof ServerPlayer player)) return;


        //BLEGGHHHHH change this (REMEMBERS TO DO THAT!!)
        ControllingDataComponent data = player.getData(ModAttachments.CONTROLLING_DATA);
        if (data.getTargetUUID() == null) {
            // Ended externally — clean up without calling reset again
            controllingMap.remove(player.getUUID());
            controllingTimer.remove(player.getUUID());
            controllingLowerSeq.remove(player.getUUID());
            originalBodyMap.remove(player.getUUID());
            return;
        }

        // Move the original body (invisible) to float just behind the current host
        UUID bodyUUID = originalBodyMap.get(player.getUUID());
        UUID hostUUID = controllingMap.get(player.getUUID());
        if (bodyUUID != null && hostUUID != null) {
            Entity bodyEntity = serverLevel.getEntity(bodyUUID);
            Entity hostEntity = serverLevel.getEntity(hostUUID);

            if (bodyEntity instanceof OriginalBodyEntity body) {
                // During possession, player IS the host — track player position
                Vec3 playerLook = new Vec3(player.getLookAngle().x(), 0, player.getLookAngle().z()).normalize();
                Vec3 behindPlayer = player.position()
                        .subtract(playerLook.scale(player.getBbWidth() + 0.5))
                        .add(0, player.getBbHeight() * 0.5, 0);
                body.teleportTo(behindPlayer.x, behindPlayer.y, behindPlayer.z);
                body.setInvisible(true);
                body.setNoGravity(true);
                body.noPhysics = true;
                body.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            }
        }

        // Tick down timer for same/higher seq (5s but we can extend if too weak ig?
        boolean lowerSeq = controllingLowerSeq.getOrDefault(player.getUUID(), false);
        if (!lowerSeq) {
            int ticks = controllingTimer.getOrDefault(player.getUUID(), 0) - 1;
            if (ticks <= 0) {
                serverLevel.getServer().execute(() -> exitControl(serverLevel, player));
                return;
            }
            controllingTimer.put(player.getUUID(), ticks);
        }
    }

    private static void tickConcealed(ServerLevel serverLevel, LivingEntity entity) {
        if (!concealedMap.containsKey(entity.getUUID())) return;

        Entity hostEntity = serverLevel.getEntity(concealedMap.get(entity.getUUID()));

        if (hostEntity == null || hostEntity.isRemoved()
                || !(hostEntity instanceof LivingEntity host) || !host.isAlive()) {
            if (hostEntity instanceof LivingEntity host2) {
                ParasitationComponent pc = host2.getData(ModAttachments.PARASITE_COMPONENT);
                pc.setParasited(false);
                pc.setParasiteUUID(null);
            }
            concealedMap.remove(entity.getUUID());
            entity.setInvisible(false);
            entity.removeEffect(MobEffects.INVISIBILITY);
            restorePlayer(entity);
            return;
        }

        entity.setInvisible(true);
        entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20, 0, false, false, false));

        Vec3 floatPos = host.position().add(0, host.getBbHeight() + 0.3, 0);
        entity.teleportTo(floatPos.x, floatPos.y, floatPos.z);
        entity.setDeltaMovement(Vec3.ZERO);

        if (entity instanceof Player player) {
            player.setBoundingBox(new AABB(
                    player.getX(), player.getY(), player.getZ(),
                    player.getX(), player.getY(), player.getZ()
            ));
            player.hurtMarked = true;
        }
    }


    private static void restorePlayer(LivingEntity entity) {
        if (entity instanceof Player player) {
            player.setBoundingBox(player.getDimensions(player.getPose()).makeBoundingBox(
                    player.getX(), player.getY(), player.getZ()
            ));
            player.onUpdateAbilities();
            player.hurtMarked = true;
        }
    }

    private static LivingEntity resolveHost(ServerLevel serverLevel, UUID uuid) {
        if (uuid == null) return null;
        Entity entity = serverLevel.getEntity(uuid);
        return entity instanceof LivingEntity living ? living : null;
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget == null) return;
        if (concealedMap.containsKey(newTarget.getUUID()) || controllingMap.containsKey(newTarget.getUUID())) {
            event.setCanceled(true);
        }
    }

    public static LivingEntity getHostForEntity(ServerLevel serverLevel, LivingEntity parasite) {
        UUID hostUUID = controllingMap.get(parasite.getUUID());
        if (hostUUID == null) hostUUID = concealedMap.get(parasite.getUUID());
        if (hostUUID == null) return null;
        Entity host = serverLevel.getEntity(hostUUID);
        return host instanceof LivingEntity living ? living : null;
    }

    public static boolean isConcealed(UUID uuid) {
        return concealedMap.containsKey(uuid);
    }

    public static boolean isControlling(UUID uuid) {
        return controllingMap.containsKey(uuid);
    }
}
