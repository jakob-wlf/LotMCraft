package de.jakob.lotm.beyonders.abilities.fool.marionettes;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.EntityControllingComponent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.attachments.MarionetteComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.goals.MarionetteMaxDistanceGoal;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.goals.MarionetteMovementGoal;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.goals.MarionetteTargetGoal;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.goals.NonBeyonderMarionetteUseAbilityGoal;
import de.jakob.lotm.beyonders.abilities.abyss.MaliceSeed.MaliceSeedAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.goals.EntityLoadChunksGoal;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MarionetteUtils {

    public static void killFromSun(LivingEntity sun, LivingEntity hit) {
        Player owner = ownerOf(hit);
        if (owner == null) return;
        if (BeyonderData.getSequence(owner, false, true) < BeyonderData.getSequence(sun, false, true)) return;
        killControlledMarionettes(owner);
    }

    public static void killFromSunAround(LivingEntity sun, ServerLevel level, Vec3 center, double radius) {
        for (LivingEntity hit : AbilityUtil.getNearbyEntities(sun, level, center, radius)) {
            killFromSun(sun, hit);
        }
    }

    private static Player ownerOf(LivingEntity hit) {
        if (isMarionette(hit)) {
            return findPlayerAcrossAllLevels(hit.getData(ModAttachments.MARIONETTE_COMPONENT).getControllerUUID(), hit);
        }
        if (!(hit instanceof Player player)) return null;
        if (ControllingUtils.isControlling(player)) {
            LivingEntity controlled = player.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT).getControlledEntity();
            if (controlled != null && isMarionette(controlled)) {
                Player owner = findPlayerAcrossAllLevels(controlled.getData(ModAttachments.MARIONETTE_COMPONENT).getControllerUUID(), controlled);
                if (owner != null) return owner;
            }
        }
        return player;
    }

    public static void killControlledMarionettes(Player player) {
        if (!(player.level() instanceof ServerLevel) || player.getServer() == null) return;
        String playerUUID = player.getStringUUID();
        for (ServerLevel level : player.getServer().getAllLevels()) {
            for (Entity e : level.getAllEntities()) {
                if (!(e instanceof LivingEntity livingEntity) || !isMarionette(livingEntity)) continue;
                String ownerUUID = livingEntity.getData(ModAttachments.MARIONETTE_COMPONENT).getControllerUUID();
                if (playerUUID.equals(ownerUUID)) {
                    livingEntity.invulnerableTime = 0;
                    livingEntity.hurt(livingEntity.damageSources().generic(), Float.MAX_VALUE);
                }
            }
        }
        for (ServerPlayer other : player.getServer().getPlayerList().getPlayers()) {
            EntityControllingComponent controlling = other.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT);
            if (!controlling.isControlling()) continue;
            LivingEntity controlled = controlling.getControlledEntity();
            if (controlled == null || !isMarionette(controlled)) continue;
            String ownerUUID = controlled.getData(ModAttachments.MARIONETTE_COMPONENT).getControllerUUID();
            if (!playerUUID.equals(ownerUUID)) continue;
            ControllingUtils.cancel(other, 0f, true, true);
        }
    }

    /**
     * Kills every marionette the player owns and severs the link, e.g. when they stop being a Beyonder.
     * Marionettes in unloaded chunks are handled when they load (see onEntityJoinLevel / onPlayerJoin).
     */
    public static void releaseAllMarionettes(Player player) {
        if (!(player.level() instanceof ServerLevel) || player.getServer() == null) return;
        killControlledMarionettes(player);

        // Anything that survived the killing blow (revival effects etc.) is removed outright
        String playerUUID = player.getStringUUID();
        List<LivingEntity> survivors = new ArrayList<>();
        for (ServerLevel level : player.getServer().getAllLevels()) {
            for (Entity e : level.getAllEntities()) {
                if (e instanceof LivingEntity livingEntity && livingEntity.isAlive() && isOwnedBy(livingEntity, playerUUID)) {
                    survivors.add(livingEntity);
                }
            }
        }
        survivors.forEach(MarionetteUtils::removeMarionette);

        player.getData(ModAttachments.MARIONETTE_OWNER_COMPONENT).getMarionettes().clear();
    }

    private static boolean isOwnedBy(LivingEntity entity, String playerUUID) {
        return isMarionette(entity) && playerUUID.equals(entity.getData(ModAttachments.MARIONETTE_COMPONENT).getControllerUUID());
    }

    private static void removeMarionette(LivingEntity marionette) {
        MarionetteComponent component = marionette.getData(ModAttachments.MARIONETTE_COMPONENT);
        component.setMarionette(false);
        component.setControllerUUID("");
        marionette.discard();
    }

    // A marionette whose owner is no longer a Beyonder shouldn't come back when its chunk loads
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof LivingEntity entity) || entity instanceof Player) return;
        if (!isMarionette(entity) || entity.getServer() == null) return;

        String ownerUUID = entity.getData(ModAttachments.MARIONETTE_COMPONENT).getControllerUUID();
        try {
            ServerPlayer owner = entity.getServer().getPlayerList().getPlayer(UUID.fromString(ownerUUID));
            if (owner != null && !BeyonderData.isBeyonder(owner)) {
                owner.getData(ModAttachments.MARIONETTE_OWNER_COMPONENT).removeMarionette(entity.getUUID());
                event.setCanceled(true);
            }
        } catch (IllegalArgumentException ignored) {
            // malformed controller UUID
        }
    }

    public static boolean isMarionette(LivingEntity entity) {
        MarionetteComponent component = entity.getData(ModAttachments.MARIONETTE_COMPONENT);
        return component.isMarionette();
    }

    public static boolean turnEntityIntoMarionette(LivingEntity entity, Player controller) {
        if (entity instanceof Player) {
            return false;
        }

        MarionetteComponent component = entity.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (component.isMarionette()) {
            return false;
        }

        component.setMarionette(true);
        component.setControllerUUID(controller.getStringUUID());
        if (controller instanceof LivingEntity livingController) {
            MaliceSeedAbility.registerManifestation(MaliceSeedAbility.getMaliceIdentity(controller), entity);
        }
        component.setCurrentMode(MarionetteComponent.MarionetteMode.FOLLOW);
        component.setShouldAttack(true);

        if (entity instanceof Mob mob) {
            mob.targetSelector.removeAllGoals(goal ->
                    goal instanceof StrollThroughVillageGoal ||
                    goal instanceof BreedGoal ||
                    goal instanceof MoveToBlockGoal ||
                    goal instanceof PanicGoal ||
                    goal instanceof RandomStrollGoal ||
                    goal instanceof TargetGoal
            );

            mob.goalSelector.addGoal(0, new MarionetteMovementGoal(mob));
            mob.goalSelector.addGoal(0, new EntityLoadChunksGoal(mob));
            mob.goalSelector.addGoal(1, new MarionetteMovementGoal(mob));
            mob.goalSelector.addGoal(1, new NonBeyonderMarionetteUseAbilityGoal(mob));
            mob.targetSelector.addGoal(0, new MarionetteTargetGoal(mob));
            mob.goalSelector.addGoal(10, new MarionetteMaxDistanceGoal(mob));
            mob.setTarget(null);

            controller.getData(ModAttachments.MARIONETTE_OWNER_COMPONENT).addMarionette(entity.getUUID());
        }

        return true;
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if(!(entity.level() instanceof ServerLevel)) {
            return;
        }
        if (!isMarionette(entity)) {
            if (entity instanceof Player player) killControlledMarionettes(player);
            return;
        }

        String ownerUUID = entity.getData(ModAttachments.MARIONETTE_COMPONENT).getControllerUUID();
        Player owner = findPlayerAcrossAllLevels(ownerUUID, entity);
        if(owner != null) {
            owner.getData(ModAttachments.MARIONETTE_OWNER_COMPONENT).removeMarionette(entity.getUUID());
        }
    }

    private static Player findPlayerAcrossAllLevels(String uuidString, LivingEntity marionette) {
        try {
            UUID uuid = UUID.fromString(uuidString);

            if (marionette.getServer() != null) {
                for (ServerLevel level : marionette.getServer().getAllLevels()) {
                    Player player = level.getPlayerByUUID(uuid);
                    if (player != null) {
                        return player;
                    }
                }
            }
        } catch (IllegalArgumentException e) {
        }

        return null;
    }

    @SubscribeEvent
    public static void onSanityDrop(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        if (entity.tickCount % 60 == 0) return;

        if (!isMarionette(entity)) {
            return;
        }

        SanityComponent sanityComponent = entity.getData(ModAttachments.SANITY_COMPONENT);
        sanityComponent.setSanity(1.0f);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        List<LivingEntity> orphaned = new ArrayList<>();
        for(Entity entity : player.serverLevel().getAllEntities()) {
            if(!(entity instanceof LivingEntity)) continue;

            if(!isMarionette((LivingEntity) entity)) continue;

            String ownerUUID = entity.getData(ModAttachments.MARIONETTE_COMPONENT).getControllerUUID();
            if(ownerUUID.equals(player.getStringUUID())) {
                if (!BeyonderData.isBeyonder(player)) {
                    // Loaded while the owner was offline after losing Beyonder status
                    orphaned.add((LivingEntity) entity);
                    continue;
                }
                player.getData(ModAttachments.MARIONETTE_OWNER_COMPONENT).addMarionette(entity.getUUID());
            }
        }
        orphaned.forEach(MarionetteUtils::removeMarionette);
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();

        if (!isMarionette(entity)) {
            return;
        }

        MobEffectInstance newEffect = event.getEffectInstance();

        if (newEffect.getEffect().value() == ModEffects.MENTAL_PLAGUE || newEffect.getEffect().value() == ModEffects.LOOSING_CONTROL || newEffect.getEffect().value() == ModEffects.ASLEEP) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }
    public static void linkMarionetteToController(LivingEntity controller, LivingEntity marionette) {
        if (!isMarionette(marionette)) return;
        String ownerUUID = marionette.getData(ModAttachments.MARIONETTE_COMPONENT).getControllerUUID();
        if (!controller.getStringUUID().equals(ownerUUID)) return;
        MaliceSeedAbility.registerManifestation(MaliceSeedAbility.getMaliceIdentity(controller), marionette);
    }
}