package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.tyrant.LightningStormAbility;
import de.jakob.lotm.abilities.wheel_of_fortune.calamities.Tornado;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class MagnifyAbility extends SelectableAbility {

    // Weather magnify flag for LightningStormAbility to read.
    private static final String MAGNIFY_STORM_WEAKEN_KEY = "lotm_magnify_storm_weaken";

    // Grab effect duration.
    private static final String MAGNIFY_GRAB_UNTIL_KEY = "lotm_magnify_grab_until";
    private static final int MAGNIFY_GRAB_STUN_TICKS = 30; // 1.5 sec

    // Execution mark.
    private static final String MAGNIFY_EXECUTION_UNTIL_KEY = "lotm_magnify_execution_until";
    private static final String MAGNIFY_EXECUTION_CASTER_KEY = "lotm_magnify_execution_caster";

    public MagnifyAbility(String id) {
        super(id, 20.0f);
        canBeCopied = false;
        canBeReplicated = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("black_emperor", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[] {
                "ability.lotmcraft.magnify.self",
                "ability.lotmcraft.magnify.weather",
                "ability.lotmcraft.magnify.grab",
                "ability.lotmcraft.magnify.execution"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        BeyonderData.reduceSpirituality(entity, BeyonderData.getMaxSpirituality(BeyonderData.getPathway(entity), BeyonderData.getSequence(entity)) * 0.20f);

        switch (abilityIndex) {
            case 0 -> magnifySelf(serverLevel, entity);
            case 1 -> magnifyWeather(serverLevel, entity);
            case 2 -> {
                LivingEntity target = AbilityUtil.getTargetEntity(entity, 96, 1.5f);
                if (target == null) {
                    AbilityUtil.sendActionBar(entity,
                            Component.literal("No target in range.").withColor(0xFF5555));
                    return;
                }
                if (!canMagnify(entity, target)) {
                    AbilityUtil.sendActionBar(entity,
                            Component.literal("Target resists Magnify.").withColor(0xFF5555));
                    return;
                }
                magnifyGrab(serverLevel, entity, target);
            }
            case 3 -> {
                LivingEntity target = AbilityUtil.getTargetEntity(entity, 96, 1.5f);
                if (target == null) {
                    AbilityUtil.sendActionBar(entity,
                            Component.literal("No target in range.").withColor(0xFF5555));
                    return;
                }
                if (!canMagnify(entity, target)) {
                    AbilityUtil.sendActionBar(entity,
                            Component.literal("Target resists Magnify.").withColor(0xFF5555));
                    return;
                }
                magnifyExecution(serverLevel, entity, target);
            }
            default -> {
            }
        }
    }

    /**
     * Stronger self-buff for a demigod-tier feel.
     */
    private void magnifySelf(ServerLevel level, LivingEntity caster) {
        RingEffectManager.createRingForAll(caster.position(), 3.2f, 22,
                0.12f, 0.0f, 0.18f, 1.0f, 0.10f, 0.85f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                caster.position().add(0, 1, 0), 1.25f, 20);

        caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 12, 2, false, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.JUMP, 20 * 12, 2, false, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 12, 2, false, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 12, 1, false, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 10, 2, false, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 12, 1, false, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 12, 2, false, false, true));

        AbilityUtil.sendActionBar(caster,
                Component.literal("Order magnified.").withColor(0xAA77FF));
    }

    /**
     * Uses the existing thunderstorm / tornado systems from your mod.
     * Thunderstorm damage is weakened only when triggered through Magnify.
     */
    private void magnifyWeather(ServerLevel level, LivingEntity caster) {
        RingEffectManager.createRingForAll(caster.position(), 3.2f, 22,
                0.12f, 0.0f, 0.18f, 1.0f, 0.10f, 0.85f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                caster.position().add(0, 1, 0), 1.25f, 18);

        if (level.isRaining() || level.isThundering()) {
            caster.getPersistentData().putBoolean(MAGNIFY_STORM_WEAKEN_KEY, true);
            try {
                new LightningStormAbility("magnify_weather_temp").onAbilityUse(level, caster);
            } finally {
                caster.getPersistentData().remove(MAGNIFY_STORM_WEAKEN_KEY);
            }

            AbilityUtil.sendActionBar(caster,
                    Component.literal("Storm magnified.").withColor(0xAA77FF));
            return;
        }

        Vec3 targetPos = AbilityUtil.getTargetLocation(caster, 25, 2, true);
        new Tornado().spawnCalamity(
                level,
                targetPos,
                (float) BeyonderData.getMultiplier(caster),
                BeyonderData.isGriefingEnabled(caster)
        );

        AbilityUtil.sendActionBar(caster,
                Component.literal("Wind magnified.").withColor(0xAA77FF));
    }

    /**
     * Pulls a target in, then stuns them once they reach the caster.
     */
    private void magnifyGrab(ServerLevel level, LivingEntity caster, LivingEntity target) {
        long until = level.getGameTime() + 20 * 4;
        target.getPersistentData().putLong(MAGNIFY_GRAB_UNTIL_KEY, until);

        RingEffectManager.createRingForAll(target.position(), 2.2f, 18,
                0.12f, 0.0f, 0.18f, 0.9f, 0.10f, 0.6f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                target.position().add(0, 1, 0), 1.05f, 16);

        AbilityUtil.sendActionBar(caster,
                Component.literal("Target seized.").withColor(0xAA77FF));

        if (target instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }

        ServerScheduler.scheduleForDuration(
                0, 1, 20 * 4,
                () -> {
                    if (!caster.isAlive() || !target.isAlive()) return;

                    long now = level.getGameTime();
                    if (target.getPersistentData().getLong(MAGNIFY_GRAB_UNTIL_KEY) <= now) {
                        return;
                    }

                    if (target instanceof Mob mob) {
                        mob.setTarget(null);
                        mob.getNavigation().stop();
                    }

                    Vec3 toCaster = caster.position().subtract(target.position());
                    double distance = toCaster.length();

                    if (distance <= 2.1D) {
                        target.setDeltaMovement(Vec3.ZERO);
                        target.hasImpulse = true;

                        Vec3 shake = new Vec3(
                                (level.random.nextDouble() - 0.5D) * 0.35D,
                                0.18D,
                                (level.random.nextDouble() - 0.5D) * 0.35D
                        );
                        target.setDeltaMovement(target.getDeltaMovement().add(shake));
                        target.hasImpulse = true;

                        if (target instanceof Mob mob) {
                            mob.setTarget(null);
                            mob.getNavigation().stop();
                        }

                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, MAGNIFY_GRAB_STUN_TICKS, 5, false, false, true));
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, MAGNIFY_GRAB_STUN_TICKS, 1, false, false, true));
                        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, MAGNIFY_GRAB_STUN_TICKS, 0, false, false, true));
                        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, MAGNIFY_GRAB_STUN_TICKS, 0, false, false, true));

                        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                                target.position().add(0, 1, 0), 0.8f, 10);

                        target.getPersistentData().remove(MAGNIFY_GRAB_UNTIL_KEY);
                        return;
                    }

                    if (toCaster.lengthSqr() < 0.01D) return;

                    Vec3 dir = toCaster.normalize();
                    double strength = Math.min(3.2D, 1.35D + (distance / 12.0D));

                    if (target instanceof ServerPlayer player) {
                        Vec3 nextPos = player.position().add(
                                dir.x * strength,
                                0.06D,
                                dir.z * strength
                        );

                        player.teleportTo(level, nextPos.x, nextPos.y, nextPos.z, player.getYRot(), player.getXRot());
                        player.setDeltaMovement(Vec3.ZERO);
                        player.hasImpulse = true;
                        player.hurtMarked = true;
                    } else {
                        target.setDeltaMovement(target.getDeltaMovement().add(
                                dir.x * strength,
                                0.12D,
                                dir.z * strength
                        ));
                        target.hasImpulse = true;
                        target.hurtMarked = true;
                    }

                    if (level.getGameTime() % 3 == 0) {
                        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                                target.position().add(0, 1, 0), 0.45f, 6);
                    }
                },
                () -> target.getPersistentData().remove(MAGNIFY_GRAB_UNTIL_KEY),
                level
        );
    }

    /**
     * Execution scaling:
     * - 2 or more sequences below the caster: execution
     * - same sequence: 50% max HP
     * - 1 sequence below: 65% max HP
     * - each sequence above the caster reduces damage by 25%
     */
    private void magnifyExecution(ServerLevel level, LivingEntity caster, LivingEntity target) {
        RingEffectManager.createRingForAll(target.position(), 2.9f, 22,
                0.12f, 0.0f, 0.18f, 1.0f, 0.10f, 0.85f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                target.position().add(0, 1, 0), 1.2f, 18);

        int casterSeq = BeyonderData.getSequence(caster);
        int targetSeq = BeyonderData.isBeyonder(target) ? BeyonderData.getSequence(target) : casterSeq;

        int seqGap = targetSeq - casterSeq;

        if (seqGap >= 2) {
            target.hurt(caster.damageSources().mobAttack(caster),
                    Math.max(target.getHealth() + 20.0f, target.getMaxHealth() * 2.0f));

            AbilityUtil.sendActionBar(caster,
                    Component.literal("Execution magnified.").withColor(0xAA77FF));
            return;
        }

        float percent;
        if (seqGap == 1) {
            percent = 0.30f;
        } else if (seqGap == 0) {
            percent = 0.20f;
        } else {
            percent = 0.20f - (0.10f * (casterSeq - targetSeq));
            if (percent < 0.0f) percent = 0.0f;
        }

        float damage = target.getMaxHealth() * percent;
        target.hurt(caster.damageSources().mobAttack(caster), damage);

        AbilityUtil.sendActionBar(caster,
                Component.literal("Execution magnified.").withColor(0xAA77FF));
    }

    private boolean canMagnify(LivingEntity caster, LivingEntity target) {
        if (!BeyonderData.isBeyonder(target)) return true;

        int casterSeq = BeyonderData.getSequence(caster);
        int targetSeq = BeyonderData.getSequence(target);

        if (targetSeq >= casterSeq) return true;

        int diff = casterSeq - targetSeq;
        if (diff == 1) return random.nextFloat() < 0.35f;
        if (diff == 2) return random.nextFloat() < 0.20f;
        return false;

    }


    private int getMagnifyGrabRange(LivingEntity caster) {
        if (!BeyonderData.isBeyonder(caster)) {
            return 50;
        }

        int seq = BeyonderData.getSequence(caster);

        // Seq 4 -> 50, Seq 3 -> 67, Seq 2 -> 83, Seq 1 -> 100
        int range = 50 + (4 - seq) * 17;
        return Math.max(50, Math.min(100, range));
    }

    private int getMagnifyExecutionRange(LivingEntity caster) {
        if (!BeyonderData.isBeyonder(caster)) {
            return 50;
        }

        int seq = BeyonderData.getSequence(caster);
        int range = 50 + (4 - seq) * 17;
        return Math.max(50, Math.min(100, range));
    }

}

