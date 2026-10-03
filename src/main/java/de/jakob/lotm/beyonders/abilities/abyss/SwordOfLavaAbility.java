package de.jakob.lotm.beyonders.abilities.abyss;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.Map;

public class SwordOfLavaAbility extends Ability {

    private static final double ARC_HALF_SPAN = Math.toRadians(70);
    private static final double ARC_TILT = Math.toRadians(38);

    public SwordOfLavaAbility(String id) {
        super(id, 8f, "burning");
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 120;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 hand = entity.getEyePosition().add(0, -0.3, 0)
                .add(entity.getLookAngle().normalize().scale(0.6));
        Vec3 forward = entity.getLookAngle().normalize();
        Vec3 right = new Vec3(-forward.z, 0, forward.x).normalize();
        Vec3 up = computeUp(forward, right);
        Vec3 sweepAxis = right.scale(Math.cos(ARC_TILT)).add(up.scale(Math.sin(ARC_TILT))).normalize();
        boolean griefing = BeyonderData.isGriefingEnabled(entity);

        serverLevel.playSound(null, BlockPos.containing(hand),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 3.0f, 0.45f);
        serverLevel.playSound(null, BlockPos.containing(hand),
                SoundEvents.LAVA_AMBIENT, SoundSource.PLAYERS, 1.5f, 1.0f);

        int totalSteps = 5;
        for (int step = 0; step < totalSteps; step++) {
            final int s = step;
            ServerScheduler.scheduleDelayed(s, () ->
                    drawLavaArcSlice(serverLevel, hand, sweepAxis, forward, s, totalSteps, griefing), serverLevel);
        }

        ServerScheduler.scheduleDelayed(totalSteps, () ->
                performSlashDamage(serverLevel, entity, hand, forward, griefing), serverLevel);

        ServerScheduler.scheduleDelayed(totalSteps, () -> {
            Vec3 impactMid = hand.add(forward.scale(6.0));
            serverLevel.playSound(null, BlockPos.containing(impactMid),
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 2.0f, 0.75f);
            serverLevel.playSound(null, BlockPos.containing(impactMid),
                    SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 2.5f, 0.65f);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.LAVA, impactMid, 20, 1.0, 0.1);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.LARGE_SMOKE, impactMid, 14, 1.2, 0.05);
        }, serverLevel);

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, hand, entity, this,
                interactionFlags, 9.0 * multiplier(entity), 20));
    }

    private Vec3 computeUp(Vec3 forward, Vec3 right) {
        Vec3 worldUp = new Vec3(0, 1, 0);
        Vec3 up = worldUp.subtract(forward.scale(forward.dot(worldUp)));
        if (up.lengthSqr() < 1.0E-4) {
            up = right.cross(forward);
        }
        return up.normalize();
    }

    private void drawLavaArcSlice(ServerLevel level, Vec3 origin, Vec3 sweepAxis, Vec3 forward,
                                  int step, int totalSteps, boolean griefing) {
        double armLength = 12.0 * multiplier(null);
        double outerR = armLength;
        double innerR = armLength * 0.68;

        double thetaFrom = -ARC_HALF_SPAN + (2 * ARC_HALF_SPAN) * ((double) step / totalSteps);
        double thetaTo = -ARC_HALF_SPAN + (2 * ARC_HALF_SPAN) * ((double) (step + 1) / totalSteps);

        double angleStep = Math.toRadians(2.2);
        double radialStep = 0.4;

        for (double theta = thetaFrom; theta <= thetaTo; theta += angleStep) {
            double s = Math.sin(theta);
            double c = Math.cos(theta);

            for (double r = innerR; r <= outerR; r += radialStep) {
                Vec3 p = origin.add(sweepAxis.scale(r * s)).add(forward.scale(r * c));

                level.sendParticles(ParticleTypes.LAVA, p.x, p.y, p.z, 1, 0, 0.02, 0, 0.0);

                if (random.nextInt(3) == 0)
                    level.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 1, 0.05, 0.06, 0.05, 0.004);

                if (random.nextInt(8) == 0)
                    level.sendParticles(ModParticles.BLACK_FLAME.get(), p.x, p.y, p.z, 1, 0.03, 0.05, 0.03, 0.002);

                if (random.nextInt(5) == 0)
                    level.sendParticles(ParticleTypes.SMOKE, p.x, p.y + 0.1, p.z, 1, 0.02, 0.05, 0.02, 0.003);

                if (random.nextInt(4) == 0)
                    level.sendParticles(ParticleTypes.DRIPPING_LAVA, p.x, p.y, p.z, 1, 0, -0.02, 0, 0.0);

                if (griefing)
                    igniteBlockAt(level, BlockPos.containing(p));
            }
        }

        if (step == totalSteps - 1) {
            for (int i = 0; i < 20; i++) {
                double theta = -ARC_HALF_SPAN + random.nextDouble() * 2 * ARC_HALF_SPAN;
                double r = innerR + random.nextDouble() * (outerR - innerR);
                Vec3 sp = origin.add(sweepAxis.scale(r * Math.sin(theta))).add(forward.scale(r * Math.cos(theta)));
                Vec3 sparkDir = sweepAxis.scale(Math.sin(theta)).add(forward.scale(Math.cos(theta))).normalize();
                level.sendParticles(ParticleTypes.LAVA, sp.x, sp.y, sp.z,
                        1, sparkDir.x * 0.2, 0.18 + random.nextDouble() * 0.2, sparkDir.z * 0.2, 0.0);
            }
        }
    }

    private void igniteBlockAt(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir()) return;
        BlockPos below = pos.below();
        if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) return;
        level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
    }

    private void performSlashDamage(ServerLevel level, LivingEntity entity,
                                    Vec3 origin, Vec3 forward, boolean griefing) {
        double slashRange = 12.0 * multiplier(entity);
        double baseDamage = DamageLookup.lookupDamage(6, 1.0) * multiplier(entity) * 1.4;
        int seq = AbilityUtil.getSeqWithArt(entity, this);

        AbilityUtil.getNearbyEntities(entity, level, origin, slashRange + 1)
                .stream()
                .filter(t -> AbilityUtil.mayDamage(entity, t))
                .filter(t -> {
                    Vec3 toT = t.position().subtract(origin).normalize();
                    return forward.dot(toT) > 0.25;
                })
                .forEach(t -> {
                    if (AbilityUtil.isTargetSignificantlyStronger(seq, BeyonderData.getSequence(t)))
                        return;
                    t.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity),
                            (float) baseDamage);
                    if (griefing || !(t instanceof net.minecraft.world.entity.player.Player))
                        t.setRemainingFireTicks(20 * 5);
                    Vec3 kb = forward.scale(1.4).add(0, 0.35, 0);
                    t.setDeltaMovement(t.getDeltaMovement().add(kb));
                    t.hurtMarked = true;
                    ParticleUtil.spawnParticles(level, ParticleTypes.LAVA, t.getEyePosition(), 18, 0.4, 0.07);
                    ParticleUtil.spawnParticles(level, ParticleTypes.FLAME, t.getEyePosition(), 10, 0.3, 0.08);
                });
    }
}