package de.jakob.lotm.beyonders.abilities.fool.marionettes;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.EntityControllingComponent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.attachments.MarionetteComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.goals.*;
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
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MarionetteUtils {

    public static void killFromSun(LivingEntity sun, LivingEntity hit) {
        Player owner = ownerOf(hit);
        if (owner == null) return;
        if (BeyonderData.getSequence(owner, false, true) < BeyonderData.getSequence(sun, false, true)) return;
        int sequence = BeyonderData.getSequence(sun, false, true);
        if (sequence <= 1) killControlledMarionettes(owner);
        else if (sequence == 2) harmControlledMarionettes(owner, 0.5f);
        else if (sequence == 3) harmControlledMarionettes(owner, 0.35f);
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

    private static void harmControlledMarionettes(Player player, float fraction) {
        if (!(player.level() instanceof ServerLevel) || player.getServer() == null) return;
        String playerUUID = player.getStringUUID();
        for (ServerLevel level : player.getServer().getAllLevels()) {
            for (Entity e : level.getAllEntities()) {
                if (!(e instanceof LivingEntity livingEntity) || !isMarionette(livingEntity)) continue;
                String ownerUUID = livingEntity.getData(ModAttachments.MARIONETTE_COMPONENT).getControllerUUID();
                if (!playerUUID.equals(ownerUUID)) continue;
                livingEntity.invulnerableTime = 0;
                livingEntity.hurt(livingEntity.damageSources().generic(), livingEntity.getMaxHealth() * fraction);
            }
        }
        for (ServerPlayer other : player.getServer().getPlayerList().getPlayers()) {
            EntityControllingComponent controlling = other.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT);
            if (!controlling.isControlling()) continue;
            LivingEntity controlled = controlling.getControlledEntity();
            if (controlled == null || !isMarionette(controlled)) continue;
            String ownerUUID = controlled.getData(ModAttachments.MARIONETTE_COMPONENT).getControllerUUID();
            if (!playerUUID.equals(ownerUUID)) continue;
            other.invulnerableTime = 0;
            other.hurt(other.damageSources().generic(), other.getMaxHealth() * fraction);
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

        for(Entity entity : player.serverLevel().getAllEntities()) {
            if(!(entity instanceof LivingEntity)) continue;

            if(!isMarionette((LivingEntity) entity)) continue;

            String ownerUUID = entity.getData(ModAttachments.MARIONETTE_COMPONENT).getControllerUUID();
            if(ownerUUID.equals(player.getStringUUID())) {
                player.getData(ModAttachments.MARIONETTE_OWNER_COMPONENT).addMarionette(entity.getUUID());
            }
        }
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
}