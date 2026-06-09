package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.AbilityWheelComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncAbilitySelectionPacket;
import de.jakob.lotm.network.packets.toClient.SyncAbilityWheelPacket;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EntropySubAbility {

    // S1: per-target mark and stack
    public static final String ENTROPY_MARK_KEY = "lotm_entropy_mark";
    public static final String ENTROPY_STACK_KEY = "lotm_entropy_stack";
    public static final String ENTROPY_UNTIL_KEY = "lotm_entropy_until";

    // Sensory decay cooldown inflation: stores the multiplier and its expiry tick
    public static final String SENSORY_DECAY_COOLDOWN_MULT_KEY = "lotm_sensory_decay_cd_mult";
    public static final String SENSORY_DECAY_COOLDOWN_UNTIL_KEY = "lotm_sensory_decay_cd_until";

    // Entropy drain spirituality inflation: stores the multiplier and its expiry tick
    public static final String ENTROPY_DRAIN_SPIRIT_MULT_KEY = "lotm_entropy_drain_spirit_mult";
    public static final String ENTROPY_DRAIN_SPIRIT_UNTIL_KEY = "lotm_entropy_drain_spirit_until";

    private static final int ENTROPY_DURATION_TICKS = 20 * 60;    // 1 minute
    private static final int ENTROPY_PULSE_INTERVAL = 20 * 20;     // every 20 seconds
    private static final int ENTROPY_PULSES_PER_TARGET = 3;
    private static final int ENTROPY_RANGE = 25;

    // How often we check for targets leaving range.
    private static final int ENTROPY_RANGE_CHECK_INTERVAL = 5;

    private static final List<Holder<MobEffect>> HARMFUL_EFFECTS = new ArrayList<>();

    private static List<Holder<MobEffect>> getHarmfulEffects() {
        if (HARMFUL_EFFECTS.isEmpty()) {
            for (Holder<MobEffect> holder : BuiltInRegistries.MOB_EFFECT.holders().toList()) {
                if (holder.value().getCategory() == MobEffectCategory.HARMFUL) {
                    HARMFUL_EFFECTS.add(holder);
                }
            }
        }
        return HARMFUL_EFFECTS;
    }

    private EntropySubAbility() {
    }

    public static void cast(ServerLevel level, LivingEntity caster) {
        int seq = BeyonderData.getSequence(caster);
        if (seq > 2) {
            AbilityUtil.sendActionBar(caster,
                    Component.literal("Entropy awakens at Seq 2.").withColor(0xFF5555));
            return;
        }

        double scale = 1.0D + Math.max(0, 2 - seq) * 0.25D;

        // S2: opening burst — a collapsing black flare.
        RingEffectManager.createRingForAll(caster.position(), (float) (3.2D * scale), 24,
                0.10f, 0.00f, 0.18f, 1.0f, 0.18f, 1.15f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                caster.position().add(0, 1, 0), 1.55D * scale, 30);

        ParticleUtil.createParticleSpirals(level, ModParticles.LIGHTNING.get(),
                caster.position().add(0, 0.55, 0),
                0.30, 1.05, 1.9, 0.07, 14, 32, 2, 3);

        AbilityUtil.sendActionBar(caster,
                Component.literal("Entropy unleashed.").withColor(0xAA77FF));

        // S3: mark targets once, then pulse them three times over a minute.
        Map<UUID, EntropyMark> markedTargets = new HashMap<>();
        List<LivingEntity> nearby = level.getEntitiesOfClass(
                LivingEntity.class,
                caster.getBoundingBox().inflate(ENTROPY_RANGE),
                target -> target != caster && target.isAlive() && canAffect(caster, target)
        );

        long now = level.getGameTime();
        long until = now + ENTROPY_DURATION_TICKS;

        for (LivingEntity target : nearby) {
            target.getPersistentData().putBoolean(ENTROPY_MARK_KEY, true);
            target.getPersistentData().putInt(ENTROPY_STACK_KEY, 0);
            target.getPersistentData().putLong(ENTROPY_UNTIL_KEY, until);

            // First pulse lands immediately.
            applyEntropyPulse(level, caster, target, seq, scale, 1);

            markedTargets.put(target.getUUID(),
                    new EntropyMark(target, now + ENTROPY_PULSE_INTERVAL, ENTROPY_PULSES_PER_TARGET - 1));
        }

        ServerScheduler.scheduleForDuration(
                0,
                ENTROPY_RANGE_CHECK_INTERVAL,
                ENTROPY_DURATION_TICKS,
                () -> {
                    if (!caster.isAlive()) return;

                    long gameTime = level.getGameTime();

                    // S4: every marked target gets stronger entropy pulses while in range.
                    for (Map.Entry<UUID, EntropyMark> entry : markedTargets.entrySet()) {
                        EntropyMark mark = entry.getValue();
                        LivingEntity target = mark.target;

                        if (target == null || !target.isAlive()) {
                            mark.remainingPulses = -1;
                            continue;
                        }

                        // If the overall entropy window is over, strip everything.
                        if (target.getPersistentData().getLong(ENTROPY_UNTIL_KEY) <= gameTime) {
                            clearEntropyAppliedEffects(target);
                            clearEntropyTags(target);
                            mark.remainingPulses = -1;
                            continue;
                        }

                        // If they leave the range, remove the effects immediately.
                        if (target.distanceToSqr(caster) > (double) (ENTROPY_RANGE * ENTROPY_RANGE)) {
                            clearEntropyAppliedEffects(target);
                            clearEntropyTags(target);
                            mark.remainingPulses = -1;
                            continue;
                        }

                        // Next stronger pulse replaces the old one.
                        if (mark.remainingPulses > 0 && gameTime >= mark.nextPulseTick) {
                            clearEntropyAppliedEffects(target);

                            int stack = target.getPersistentData().getInt(ENTROPY_STACK_KEY) + 1;
                            applyEntropyPulse(level, caster, target, seq, scale, stack);

                            mark.remainingPulses--;
                            mark.nextPulseTick += ENTROPY_PULSE_INTERVAL;
                        }
                    }

                    markedTargets.entrySet().removeIf(e -> e.getValue().remainingPulses < 0);
                },
                () -> {
                    // S5: cleanup.
                    for (EntropyMark mark : markedTargets.values()) {
                        if (mark.target != null) {
                            clearEntropyAppliedEffects(mark.target);
                            clearEntropyTags(mark.target);
                        }
                    }
                    markedTargets.clear();
                },
                level
        );
    }

    // S6: same or weaker targets are valid, stronger ones may resist.
    private static boolean canAffect(LivingEntity caster, LivingEntity target) {
        if (!BeyonderData.isBeyonder(target)) return true;

        int casterSeq = BeyonderData.getSequence(caster);
        int targetSeq = BeyonderData.getSequence(target);

        if (targetSeq >= casterSeq) return true;

        int diff = casterSeq - targetSeq;
        if (diff == 1) return caster.level().random.nextFloat() < 0.30f;
        return false;
    }

    // S7: main pulse logic.
    private static void applyEntropyPulse(ServerLevel level, LivingEntity caster, LivingEntity target, int casterSeq, double scale, int stack) {
        int targetSeq = BeyonderData.isBeyonder(target) ? BeyonderData.getSequence(target) : Integer.MAX_VALUE;
        int seqGap = Math.max(0, targetSeq - casterSeq);

        // Higher stacks and weaker targets drift harder into chaos.
        int roll = level.random.nextInt(100) + (stack * 10) + (seqGap * 4);
        if (roll > 99) roll = 99;

        BeyonderData.reduceSpirituality(caster, 800.0f);

        // Always drain a little spirituality on Beyonders.
        if (BeyonderData.isBeyonder(target)) {
            float drain = 0.75f + (stack * 0.35f) + (seqGap * 0.20f);
            BeyonderData.reduceSpirituality(target, drain);
        }

        if (target instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }

        target.getPersistentData().putInt(ENTROPY_STACK_KEY, stack);

        if (roll < 25) {
            applyMinorEntropy(level, target, scale, stack);
        } else if (roll < 50) {
            applyControlCollapse(level, target, scale, stack);
        } else if (roll < 70) {
            applySensoryDecay(level, target, scale, stack);
        } else if (roll < 86) {
            applyEntropyDrain(level, target, scale, stack);
        } else if (roll < 96) {
            applyEntropyDamage(level, caster, target, stack, false);
        } else {
            applyEntropyDamage(level, caster, target, stack, true);
        }

        // S8: distinct hit effect, not the same as the other auras.
        spawnEntropyHitFX(level, target, scale, stack);
    }

    // S9: softer disorder — randomize the selected mode of the target's currently active wheel ability.
    private static void applyMinorEntropy(ServerLevel level, LivingEntity target, double scale, int stack) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * (4 + stack), Math.min(2, stack / 2), false, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 3, 0, false, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 3, 0, false, false, false));

        if (target instanceof ServerPlayer player) {
            AbilityWheelComponent wheel = player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
            int selectedIndex = wheel.getSelectedAbility();
            if (selectedIndex >= 0 && selectedIndex < wheel.getAbilities().size()) {
                String abilityId = wheel.getAbilities().get(selectedIndex);
                if (LOTMCraft.abilityHandler.getById(abilityId) instanceof SelectableAbility selectable) {
                    String[] names = selectable.getAbilityNamesCopy();
                    if (names.length > 1) {
                        int current = selectable.getSelectedAbilityIndex(player.getUUID());
                        int randomIndex;
                        do {
                            randomIndex = level.random.nextInt(names.length);
                        } while (randomIndex == current);
                        selectable.setSelectedAbility(player, randomIndex);
                        PacketHandler.sendToPlayer(player, new SyncAbilitySelectionPacket(abilityId, randomIndex));
                    }
                }
            }
        }

        target.setDeltaMovement(target.getDeltaMovement().add(
                (level.random.nextDouble() - 0.5D) * 0.22D * scale,
                0.03D,
                (level.random.nextDouble() - 0.5D) * 0.22D * scale
        ));
        target.hurtMarked = true;
    }

    // S10: stronger mental and spiritual collapse — shuffle the target's ability wheel order.
    private static void applyControlCollapse(ServerLevel level, LivingEntity target, double scale, int stack) {
        int duration = 20 * (6 + stack);

        if (BeyonderData.isBeyonder(target)) {
            target.addEffect(new MobEffectInstance(
                    ModEffects.LOOSING_CONTROL,
                    duration,
                    Math.min(4, 1 + (stack / 2)),
                    false,
                    false,
                    true
            ));
        } else {
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.POISON, duration, 0, false, false, true));
        }

        if (target instanceof ServerPlayer player) {
            AbilityWheelComponent wheel = player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
            if (wheel.getAbilities().size() > 1) {
                ArrayList<String> shuffled = new ArrayList<>(wheel.getAbilities());
                Collections.shuffle(shuffled, new java.util.Random(level.random.nextLong()));
                wheel.setAbilities(shuffled);
                wheel.setSelectedAbility(0);
                PacketHandler.sendToPlayer(player, new SyncAbilityWheelPacket(shuffled, 0));
            }
        }

        target.setDeltaMovement(target.getDeltaMovement().multiply(0.60D, 0.85D, 0.60D));
        target.hurtMarked = true;
    }

    // S11: the field itself starts crushing their state down.
    private static void applyEntropyDrain(ServerLevel level, LivingEntity target, double scale, int stack) {
        int duration = 20 * (5 + stack);

        if (BeyonderData.isBeyonder(target)) {
            target.addEffect(new MobEffectInstance(
                    ModEffects.LOOSING_CONTROL,
                    duration,
                    Math.min(4, 2 + (stack / 2)),
                    false,
                    false,
                    true
            ));
        }

        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 0, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, Math.min(3, 1 + stack / 2), false, false, true));

        // Random spirituality cost inflation 10–50% for the duration
        float spiritMult = 1.1f + level.random.nextFloat() * 0.4f;
        target.getPersistentData().putFloat(ENTROPY_DRAIN_SPIRIT_MULT_KEY, spiritMult);
        target.getPersistentData().putLong(ENTROPY_DRAIN_SPIRIT_UNTIL_KEY, level.getGameTime() + duration);

        target.setDeltaMovement(target.getDeltaMovement().multiply(0.50D, 0.80D, 0.50D));
        target.hurtMarked = true;
    }

    // S12: heavier sensory collapse.
    private static void applySensoryDecay(ServerLevel level, LivingEntity target, double scale, int stack) {
        int duration = 20 * (4 + stack);

        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 0, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, Math.min(3, 1 + stack / 2), false, false, true));

        // Random cooldown inflation 10–50% for the duration
        float mult = 1.1f + level.random.nextFloat() * 0.4f;
        target.getPersistentData().putFloat(SENSORY_DECAY_COOLDOWN_MULT_KEY, mult);
        target.getPersistentData().putLong(SENSORY_DECAY_COOLDOWN_UNTIL_KEY, level.getGameTime() + duration);

        target.setDeltaMovement(target.getDeltaMovement().multiply(0.35D, 0.70D, 0.35D));
        target.hurtMarked = true;

        RingEffectManager.createRingForAll(target.position(), 2.4f + stack * 0.1f, 18 + stack,
                0.08f, 0.00f, 0.18f, 1.0f, 0.16f, 1.05f, level);

        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                target.position().add(0, 1, 0), 0.55D + stack * 0.08D, 14 + stack);
    }

    // Damage reduced by armor and resistance for both rolls.
    private static void applyEntropyDamage(ServerLevel level, LivingEntity caster, LivingEntity target, int stack, boolean annihilation) {
        float dmg = (float) (target.getMaxHealth() * (annihilation ? 0.12D : 0.06D) + (stack * (annihilation ? 2.5D : 1.25D)));

        if (!annihilation) {
            // Entropy Damage: apply every harmful vanilla effect at max potion-obtainable level (II / amplifier 1)
            for (Holder<MobEffect> effect : getHarmfulEffects()) {
                target.addEffect(new MobEffectInstance(effect, 20 * 45, 1, false, false, true));
            }
        }

        if (annihilation) {
            if (BeyonderData.isBeyonder(target)) {
                target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 4, 1, false, false, true));
            }
            target.hurt(caster.damageSources().mobAttack(caster), dmg);
        }

        target.hurtMarked = true;
    }

    // S14: distinct entropy hit visuals.
    private static void spawnEntropyHitFX(ServerLevel level, LivingEntity target, double scale, int stack) {
        Vec3 center = target.position().add(0, 1, 0);

        RingEffectManager.createRingForAll(target.position(), (float) (2.1D + stack * 0.15D), 18 + stack * 2,
                0.08f, 0.00f, 0.18f, 1.0f, 0.16f, 1.05f, level);

        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                center, 0.50D + stack * 0.10D, 16 + stack * 2);

        ParticleUtil.createParticleSpirals(level, ModParticles.LIGHTNING.get(),
                target.position().add(0, 0.45, 0),
                0.18 + stack * 0.02,
                1.00,
                1.50,
                0.06,
                12 + stack,
                26 + stack * 2,
                2,
                2);

        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                center.x, center.y, center.z,
                16, 0.55D, 0.55D, 0.55D, 0.02D);

        level.sendParticles(ParticleTypes.PORTAL,
                center.x, center.y, center.z,
                10, 0.35D, 0.35D, 0.35D, 0.08D);

        level.sendParticles(ParticleTypes.DRAGON_BREATH,
                center.x, center.y, center.z,
                6, 0.25D, 0.20D, 0.25D, 0.01D);

        level.sendParticles(ParticleTypes.SMOKE,
                center.x, center.y, center.z,
                8, 0.30D, 0.15D, 0.30D, 0.01D);
    }

    // S15: remove only the entropy-applied debuffs, not unrelated buffs.
    private static void clearEntropyAppliedEffects(LivingEntity target) {
        target.removeEffect(ModEffects.LOOSING_CONTROL);
        target.removeEffect(MobEffects.BLINDNESS);
        target.removeEffect(MobEffects.CONFUSION);
        target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        target.removeEffect(MobEffects.WEAKNESS);
        target.removeEffect(MobEffects.POISON);
        target.getPersistentData().remove(SENSORY_DECAY_COOLDOWN_MULT_KEY);
        target.getPersistentData().remove(SENSORY_DECAY_COOLDOWN_UNTIL_KEY);
        target.getPersistentData().remove(ENTROPY_DRAIN_SPIRIT_MULT_KEY);
        target.getPersistentData().remove(ENTROPY_DRAIN_SPIRIT_UNTIL_KEY);
    }

    // S16: cleanup tags.
    private static void clearEntropyTags(LivingEntity target) {
        target.getPersistentData().remove(ENTROPY_MARK_KEY);
        target.getPersistentData().remove(ENTROPY_STACK_KEY);
        target.getPersistentData().remove(ENTROPY_UNTIL_KEY);
    }

    private static final class EntropyMark {
        private final LivingEntity target;
        private long nextPulseTick;
        private int remainingPulses;

        private EntropyMark(LivingEntity target, long nextPulseTick, int remainingPulses) {
            this.target = target;
            this.nextPulseTick = nextPulseTick;
            this.remainingPulses = remainingPulses;
        }
    }
}
