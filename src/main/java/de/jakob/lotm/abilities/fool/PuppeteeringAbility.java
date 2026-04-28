package de.jakob.lotm.abilities.fool;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import de.jakob.lotm.util.helper.marionettes.MarionetteUtils;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
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
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PuppeteeringAbility extends Ability {

    private final HashMap<UUID, LivingEntity> entitiesBeingManipulated = new HashMap<>();

    public PuppeteeringAbility(String id) {
        super(id, 1);

        canBeCopied = false;
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
            case 9,8,7,6,5 -> targetPower = (10 - targetSequence); // should be 1, 2, 3, 4, 5
            case 4 -> targetPower = 7;
            case 3 -> targetPower = 9;
            case 2 -> targetPower = 12;
            case 1 -> targetPower = 15;
            case 0 -> targetPower = 20;
        }

        // 20 - 15
        int difference = playerPower - targetPower;

        int manipulationTime ;

        // same sequence, time depends on what sequence
        if (difference == 0) {
            if (targetSequence == 0) {
                manipulationTime = 20 * 100; // ~100s for seq 0
            }
            else if (targetSequence <= 2) {
                manipulationTime = 20 * 80; // ~80s for seq 1, 2
            }
            else if (targetSequence <= 4) {
                manipulationTime = 20 * 60; // ~60s for seq 3, 4
            }
            else {
                manipulationTime = 20 * 30; // ~30s for seq 5+
            }
        }
        // if the target sequence is higher than the player
        else if (difference < 0) {
            if (difference >= -2) {
                // this works when (seq6 vs target seq5, seq5 (5 power) vs target seq4 (7 power) or seq4 (7 power) vs seq3 (9 power), but doesnt work for others)
                manipulationTime = 20 * 180 * (difference * -1); // around 3 mins
            } else {
                manipulationTime = -1; // pass -1 for the impossible puppeteering
            }
        }
        // last case, when player sequence is higher than the target
        else {
            if (targetSequence >= 10) {
                manipulationTime = 20 * 2;
            } else if (targetSequence >= 5) {
                manipulationTime = 20 * (120 / (2 * difference));
            } else {
                manipulationTime = 20 * (120 / difference);
            }
        }

        return manipulationTime;
    }


    private final DustParticleOptions particleOptions = new DustParticleOptions(new Vector3f(.4f, .4f, .4f), 1.35f);

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
            // if time < 0 means the control is impossible
            if (time < 0) {
                entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 8, 5, false, false, false));
                return;
            }
        }

        entitiesBeingManipulated.put(entity.getUUID(), target);

        AtomicBoolean stopped = new AtomicBoolean(false);

        Vec3 startTemp = entity.getEyePosition().add(entity.getLookAngle().normalize());
        Vec3 endTemp = target.getEyePosition();

        final Vec3 perp1 = VectorUtil.getRandomPerpendicular(endTemp.subtract(startTemp));
        final Vec3 perp2 = VectorUtil.getRandomPerpendicular(endTemp.subtract(startTemp));
        final Vec3 perp3 = VectorUtil.getRandomPerpendicular(endTemp.subtract(startTemp));

        if(target instanceof Mob mob) {
            mob.setTarget(entity);
        }

        AtomicDouble health = new AtomicDouble(target.getHealth());
        AtomicDouble casterHealth = new AtomicDouble(entity.getHealth());

        ServerScheduler.scheduleForDuration(0, 2, time, () -> {
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

            Vec3 end = target.getEyePosition();

            for(int i = 0; i < 3; i++) {
                double right = i == 0 ? -2 : (i == 1 ? 1.4 : 2.2);
                double up = i == 2 ? -.2 : (i == 1 ? 0 : 1.2);
                Vec3 perp = i == 0 ? perp1 : (i == 1 ? perp2 : perp3);
                Vec3 startLoc = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, right, up);

                float distance = (float) end.distanceTo(startLoc);
                float bezierSteps = .025f;

                int maxPoints = Math.max(2, Math.min(10, (int) Math.ceil(distance * 1.5)));

                List<Vec3> points = VectorUtil.createBezierCurve(startLoc, end, perp, bezierSteps, random.nextInt(1, maxPoints + 1));

                for(Vec3 point : points) {
                    ParticleUtil.spawnParticles((ServerLevel) level, particleOptions, point, 1, 0, 0, 0, 0);
                }
            }

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 4, false, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 5, false, false, false));

            health.set(target.getHealth());
        }, () -> {
            entitiesBeingManipulated.remove(entity.getUUID());
            if(stopped.get()) {
                return;
            }
            MarionetteComponent component = entity.getData(ModAttachments.MARIONETTE_COMPONENT.get());
            if(entity instanceof Player player && !component.isMarionette()) {
                turnIntoMarionette(target, player);
            }
            else
                target.setHealth(0);
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
                target.setHealth(0);
                target = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), target.level(), false);
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
}
