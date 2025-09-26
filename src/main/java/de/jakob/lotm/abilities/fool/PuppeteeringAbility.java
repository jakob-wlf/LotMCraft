package de.jakob.lotm.abilities.fool;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.helper.marionettes.MarionetteUtils;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PuppeteeringAbility extends AbilityItem {

    private final HashMap<UUID, LivingEntity> entitiesBeingManipulated = new HashMap<>();

    public PuppeteeringAbility(Properties properties) {
        super(properties, 1);

        canBeCopied = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 5));
    }

    @Override
    protected float getSpiritualityCost() {
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

    private int getManipulationTime(int sequence) {
        return switch (sequence) {
            default -> 20 * 11;
            case 4 -> 80;
            case 3 -> 60;
            case 0, 1, 2 -> 20;
        };
    }

    private int getManipulationTimeBySequenceAndSequenceDifference(int sequence, int targetSequence) {
        int difference = targetSequence - sequence;

        int baseTime = 900; // ~45 seconds (20 ticks = 1 second)

        int manipulationTime;

        if (difference == 0) {
            // Same sequence - faster depending on how low the controller's sequence is
            if (sequence <= 2) {
                manipulationTime = 20 * 10; // ~10s for seq 0-2
            } else if (sequence == 3) {
                manipulationTime = 20 * 12; // ~12s for seq 3
            } else if (sequence == 4) {
                manipulationTime = 20 * 15; // ~15s for seq 4
            } else {
                manipulationTime = baseTime; // ~45s for seq 5+
            }
        } else {
            // Stronger advantage when higher sequence controls lower sequence
            if (sequence <= 2 && targetSequence >= 3) {
                manipulationTime = 20 * 8; // ~8s for seq 1-2 vs 3+
            } else if ((sequence == 3 || sequence == 4) && targetSequence >= 5) {
                manipulationTime = 20 * 10; // ~10s for seq 3-4 vs 5+
            } else {
                // Default scaling: faster with bigger difference
                int reduction = difference * (12 * 20); // ~12s less per level difference
                manipulationTime = Math.max(baseTime - reduction, 20 * 5); // minimum 5s
            }
        }

        return manipulationTime;
    }


    private final DustParticleOptions particleOptions = new DustParticleOptions(new Vector3f(.4f, .4f, .4f), 1.35f);

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(entitiesBeingManipulated.containsKey(entity.getUUID())) {
            entitiesBeingManipulated.remove(entity.getUUID());
            return;
        }

        if(!BeyonderData.isBeyonder(entity) || BeyonderData.getSequence(entity) < 0 || BeyonderData.getSequence(entity) > 9)
            return;

        int sequence = BeyonderData.getSequence(entity);

        LivingEntity target = AbilityUtil.getTargetEntity(entity, getManipulationDistance(sequence), 3);
        if(target == null || target == entity) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("ability.lotmcraft.puppeteering.no_entity_found").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        int time = getManipulationTime(BeyonderData.getSequence(entity));

        if(BeyonderData.isBeyonder(target)) {
            int targetSequence = BeyonderData.getSequence(target);
            if(BeyonderData.getSequence(target) < BeyonderData.getSequence(entity)) {
                if((targetSequence <= 4 && sequence >= 5) || (targetSequence <= 2 && sequence >= 3) || (targetSequence == 0)) {
                    entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 8, 4, false, false, false));
                    return;
                }
                time *= 6;
            } else {
                time = getManipulationTimeBySequenceAndSequenceDifference(sequence, targetSequence);
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
                float bezierSteps = .25f / distance;

                int maxPoints = Math.max(2, Math.min(10, (int) Math.ceil(distance * 1.5)));

                List<Vec3> points = VectorUtil.createBezierCurve(startLoc, end, perp, bezierSteps, random.nextInt(1, maxPoints + 1));

                for(Vec3 point : points) {
                    ParticleUtil.spawnParticles((ServerLevel) level, particleOptions, point, 1, 0, 0, 0, 0);
                }
            }

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 4, false, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 5, false, false, false));

            health.set(target.getHealth());
        }, () -> {
            entitiesBeingManipulated.remove(entity.getUUID());
            if(entity instanceof Player player)
                turnIntoMarionette(target, player);
            else
                target.setHealth(0);
        }, (ServerLevel) level);
    }

    private void turnIntoMarionette(LivingEntity target, Player player) {
        target.setHealth(target.getMaxHealth());
        if (MarionetteUtils.turnEntityIntoMarionette(target, player)) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.puppeteering.entity_turned").withColor(0xa26fc9));
        } else {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.puppeteering.entity_turned_failed").withColor(0xa26fc9));
        }
    }
}
