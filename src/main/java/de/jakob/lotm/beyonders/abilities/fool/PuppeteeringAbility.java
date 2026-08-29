package de.jakob.lotm.beyonders.abilities.fool;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.rendering.effectRendering.EffectParams;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.attachments.MarionetteComponent;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.MarionetteUtils;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PuppeteeringAbility extends Ability {

    private final HashMap<UUID, LivingEntity> entitiesBeingManipulated = new HashMap<>();

    public PuppeteeringAbility(String id) {
        super(id, 1);

        onHoldTickInverval = 1;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 5));
    }

    @Override
    public float getSpiritualityCost() {
        return 40;
    }

    private int getManipulationDistance(int sequence) {
        return switch (sequence) {
            default -> 7;
            case 4 -> 130;
            case 3 -> 250;
            case 0, 1, 2 -> 500;
        };
    }


    private int getManipulationTimeBySequenceAndSequenceDifference(int sequence, int targetSequence) {
        int playerPower = 0;
        int targetPower = 0;

        switch (sequence){
            case 9,8,7,6,5 -> playerPower = (10 - sequence); // should be 1, 2, 3, 4, 5
            case 4 -> playerPower = 7;
            case 3 -> playerPower = 9;
            case 2 -> playerPower = 12;
            case 1 -> playerPower = 15;
            case 0 -> playerPower = 20;
        }

        switch (targetSequence){
            case 9,8,7,6,5 -> targetPower = (10 - targetSequence);
            case 4 -> targetPower = 7;
            case 3 -> targetPower = 9;
            case 2 -> targetPower = 12;
            case 1 -> targetPower = 15;
            case 0 -> targetPower = 20;
        }

        // 20 - 15
        int difference = playerPower - targetPower;

        int manipulationTime ;

        if (difference == 0) {
            if (targetSequence == 0) {
                manipulationTime = 20 * 100;
            }
            else if (targetSequence <= 2) {
                manipulationTime = 20 * 80;
            }
            else if (targetSequence <= 4) {
                manipulationTime = 20 * 60;
            }
            else {
                manipulationTime = 20 * 30;
            }
        }
        else if (difference < 0) {
            if (difference >= -2) {
                manipulationTime = 20 * 180 * (difference * -1); // around 3 mins
            } else {
                manipulationTime = -1; // pass -1 for the impossible puppeteering
            }
        }
        else {
            if (targetSequence >= 10) {
                return switch (sequence) {
                    case 4 -> 20 * 4;
                    case 3 -> 20 * 2;
                    case 2, 1, 0 -> 20;
                    default -> 20 * 20;
                };
            } else if (targetSequence >= 5) {
                manipulationTime = 20 * (120 / (2 * difference));
            } else {
                manipulationTime = 20 * (120 / difference);
            }
        }

        return manipulationTime;
    }



    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(entitiesBeingManipulated.containsKey(entity.getUUID())) {
            entitiesBeingManipulated.remove(entity.getUUID());
            return;
        }

        int sequence = AbilityUtil.getSeqWithArt(entity, this);

        if(!BeyonderData.isBeyonder(entity) || sequence < 0 || sequence > 9)
            return;


        LivingEntity target = AbilityUtil.getTargetEntity(entity, getManipulationDistance(sequence), 3);
        if(target == null || target == entity || target instanceof Phantom) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("ability.lotmcraft.puppeteering.no_entity_found").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }
        int targetSequence = BeyonderData.getSequence(target);
        int time = getManipulationTimeBySequenceAndSequenceDifference(sequence, targetSequence);

        if(BeyonderData.isBeyonder(target)) {
            if (time < 0) {
                entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 8, 5, false, false, false));
                return;
            }
        }

        entitiesBeingManipulated.put(entity.getUUID(), target);

        AtomicBoolean stopped = new AtomicBoolean(false);

        if(target instanceof Mob mob) {
            mob.setTarget(entity);
        }

        AtomicDouble health = new AtomicDouble(target.getHealth());
        AtomicDouble casterHealth = new AtomicDouble(entity.getHealth());

        ServerScheduler.scheduleForDuration(0, 1, time, () -> {
            if(stopped.get()) {
                return;
            }

            if(!target.isAlive() || target.isRemoved() || target.level() != level) {
                entitiesBeingManipulated.remove(entity.getUUID());
                stopped.set(true);
                return;
            }

            if(target.distanceTo(entity) >= getManipulationDistance(sequence) * 1.75f) {
                entitiesBeingManipulated.remove(entity.getUUID());
                stopped.set(true);
                return;
            }

            if(target.getHealth() < health.get()) {
                entitiesBeingManipulated.remove(entity.getUUID());
                stopped.set(true);
                return;
            }

            if(entity.getHealth() < casterHealth.get() * 0.5) {
                entitiesBeingManipulated.remove(entity.getUUID());
                stopped.set(true);
                return;
            }

            if(!entitiesBeingManipulated.containsKey(entity.getUUID())) {
                entitiesBeingManipulated.remove(entity.getUUID());
                stopped.set(true);
                return;
            }

            Vec3 start = VectorUtil.getRelativePosition(entity.getEyePosition(), new Vec3(entity.getLookAngle().x, 0, entity.getLookAngle().z), .1, .35, -.5);
            Vec3 end = target.getEyePosition();
            if(entity instanceof ServerPlayer serverPlayer)
                EffectManager.playEffect(EffectIds.MARIONETTE_THREADS, start.x(), start.y(), start.z(), serverPlayer, EffectParams.directionWithParams(2, start.x(), start.y(), start.z(), end.x(), end.y(), end.z(), 0.5f, 0.1f, 0.7f));

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 4, false, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 5, false, false, false));

            health.set(target.getHealth());
        }, () -> {
            entitiesBeingManipulated.remove(entity.getUUID());
            if(stopped.get()) {
                return;
            }
            MarionetteComponent component = entity.getData(ModAttachments.MARIONETTE_COMPONENT.get());
            if(entity instanceof ServerPlayer player)
                EffectManager.playEffect(EffectIds.RING_PULSE, target.getX(), target.getY() + 1, target.getZ(), (ServerLevel) level, player, EffectParams.ofParams(0.5f, 0.1f, 0.7f));
            if(entity instanceof Player player && !component.isMarionette()) {
                turnIntoMarionette(target, player);
            }
            else {
                target.hurt(target.damageSources().generic(), Float.MAX_VALUE);
            }
        }, (ServerLevel) level);
    }

    private void turnIntoMarionette(LivingEntity target, Player player) {
        if(target instanceof Player) {
            Vec3 pos = target.position();
            if(BeyonderData.isBeyonder(target)) {
                int sequence = BeyonderData.getSequence(target);
                String pathway = BeyonderData.getPathway(target);
                target.hurt(target.damageSources().generic(), Float.MAX_VALUE);
                target = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), target.level(), false, pathway, sequence);
            }
            else {
                target.hurt(target.damageSources().generic(), Float.MAX_VALUE);
                target = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), target.level(), false, "none", 10);
            }

            target.setPos(pos);
            target.level().addFreshEntity(target);
        }
        target.setHealth(target.getMaxHealth());
        if(target instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        if (MarionetteUtils.turnEntityIntoMarionette(target, player)) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.puppeteering.entity_turned").withColor(0xa26fc9));
        } else {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.puppeteering.entity_turned_failed").withColor(0xa26fc9));
        }
    }

    @Override
    public void onHold(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer serverPlayer)) return;
        if(entitiesBeingManipulated.containsKey(entity.getUUID())) return;

        for(LivingEntity target : AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 35)) {
            Vec3 start = VectorUtil.getRelativePosition(entity.getEyePosition(), new Vec3(entity.getLookAngle().x, 0, entity.getLookAngle().z), .1, .35, -.5);
            Vec3 end = target.getEyePosition();
            EffectManager.playDirectionalEffect(EffectIds.MARIONETTE_THREADS, start.x(), start.y(), start.z(), end.x(), end.y(), end.z(), 2, serverPlayer);
        }
    }
}
