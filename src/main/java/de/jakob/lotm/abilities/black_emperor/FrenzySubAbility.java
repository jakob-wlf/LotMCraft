package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FrenzySubAbility {

    // S0: separate Frenzy cooldown.
    public static final String FRENZY_COOLDOWN_KEY = "lotm_frenzy_cooldown_until";

    // S1: Frenzy mark on affected entities.
    public static final String FRENZY_MARK_KEY = "lotm_frenzy_mark";
    public static final String FRENZY_UNTIL_KEY = "lotm_frenzy_until";

    private static final int FRENZY_COOLDOWN_TICKS = 20 * 60;   // 1 minute
    private static final int FRENZY_DURATION_TICKS = 20 * 60;    // 1 minute
    private static final int FRENZY_PULSE_INTERVAL = 20 * 20;    // every 20 seconds
    private static final int FRENZY_PULSES_PER_TARGET = 3;       // 3 random events per target
    private static final int FRENZY_RANGE = 18;

    private FrenzySubAbility() {
    }

    public static void cast(ServerLevel level, LivingEntity caster) {
        int seq = BeyonderData.getSequence(caster);
        if (seq > 3) {
            AbilityUtil.sendActionBar(caster,
                    Component.literal("Frenzy awakens at Seq 3.").withColor(0xFF5555));
            return;
        }

        long now = level.getGameTime();
        long lockUntil = caster.getPersistentData().getLong(FRENZY_COOLDOWN_KEY);
        if (lockUntil > now) {
            AbilityUtil.sendActionBar(caster,
                    Component.literal("Frenzy is cooling down.").withColor(0xFF5555));
            return;
        }

        caster.getPersistentData().putLong(FRENZY_COOLDOWN_KEY, now + FRENZY_COOLDOWN_TICKS);

        double scale = 1.0D + Math.max(0, 3 - seq) * 0.20D;

        // S2: cast burst
        RingEffectManager.createRingForAll(caster.position(), (float) (3.0D * scale), 22,
                0.12f, 0.0f, 0.22f, 1.0f, 0.18f, 1.1f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                caster.position().add(0, 1, 0), 1.4D * scale, 28);

        AbilityUtil.sendActionBar(caster,
                Component.literal("Frenzy unleashed.").withColor(0xAA77FF));

        // S3: mark nearby targets once, then pulse them three times over the minute.
        Map<UUID, FrenzyMark> markedTargets = new HashMap<>();
        List<LivingEntity> nearby = level.getEntitiesOfClass(
                LivingEntity.class,
                caster.getBoundingBox().inflate(FRENZY_RANGE),
                target -> target != caster && target.isAlive() && canAffect(caster, target)
        );

        long until = now + FRENZY_DURATION_TICKS;
        for (LivingEntity target : nearby) {
            target.getPersistentData().putBoolean(FRENZY_MARK_KEY, true);
            target.getPersistentData().putLong(FRENZY_UNTIL_KEY, until);
            markedTargets.put(target.getUUID(), new FrenzyMark(FRENZY_PULSES_PER_TARGET));
        }

        ServerScheduler.scheduleForDuration(
                0,
                FRENZY_PULSE_INTERVAL,
                FRENZY_DURATION_TICKS,
                () -> {
                    if (!caster.isAlive()) return;

                    // S4: caster mostly gets boon/neutral outcomes.
                    applyCasterPulse(level, caster, seq, scale);

                    // S5: each marked target gets one random event per pulse.
                    Iterator<Map.Entry<UUID, FrenzyMark>> it = markedTargets.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<UUID, FrenzyMark> entry = it.next();
                        LivingEntity target = (LivingEntity) level.getEntity(entry.getKey());

                        if (target == null || !target.isAlive()) {
                            it.remove();
                            continue;
                        }

                        if (target.getPersistentData().getLong(FRENZY_UNTIL_KEY) <= level.getGameTime()) {
                            clearFrenzyTags(target);
                            it.remove();
                            continue;
                        }

                        applyTargetPulse(level, caster, target, seq, scale);
                        entry.getValue().remainingPulses--;

                        if (entry.getValue().remainingPulses <= 0) {
                            clearFrenzyTags(target);
                            it.remove();
                        }
                    }
                },
                () -> {
                    // S6: cleanup
                    for (UUID uuid : markedTargets.keySet()) {
                        if (level.getEntity(uuid) instanceof LivingEntity living) {
                            clearFrenzyTags(living);
                        }
                    }
                    markedTargets.clear();
                },
                level
        );
    }

    // S7: only same-seq or weaker targets can be affected.
    private static boolean canAffect(LivingEntity caster, LivingEntity target) {
        if (!BeyonderData.isBeyonder(target)) return true;

        int casterSeq = BeyonderData.getSequence(caster);
        int targetSeq = BeyonderData.getSequence(target);

        return targetSeq >= casterSeq;
    }

    // S8: caster pulse, mostly good.
    private static void applyCasterPulse(ServerLevel level, LivingEntity caster, int casterSeq, double scale) {
        double boonChance = Math.min(0.88D, 0.70D + Math.max(0, 3 - casterSeq) * 0.03D);
        double roll = level.random.nextDouble();

        if (roll < boonChance) {
            applyCasterBoon(level, caster, scale);
        } else if (roll < 0.92D) {
            applyCasterNeutral(level, caster, scale);
        } else if (roll < 0.98D) {
            applyCasterMinorBad(level, caster, scale);
        } else {
            applyCasterMajorBad(level, caster, scale);
        }
    }

    // S9: caster positive outcomes.
    private static void applyCasterBoon(ServerLevel level, LivingEntity caster, double scale) {
        int roll = level.random.nextInt(6);

        switch (roll) {
            case 0 -> caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 8, 1, false, false, true));
            case 1 -> caster.addEffect(new MobEffectInstance(MobEffects.JUMP, 20 * 8, 1, false, false, true));
            case 2 -> caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 8, 1, false, false, true));
            case 3 -> caster.addEffect(new MobEffectInstance(MobEffects.LUCK, 20 * 10, 1, false, false, true));
            case 4 -> caster.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 10, 1, false, false, true));
            default -> {
                caster.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 5, 1, false, false, true));
                caster.heal((float) (1.25D * scale));
            }
        }

        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                caster.position().add(0, 1, 0), 0.35D * scale, 10);
    }

    // S10: caster neutral.
    private static void applyCasterNeutral(ServerLevel level, LivingEntity caster, double scale) {
        caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 0, false, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 5, 0, false, false, true));
    }

    // S11: caster minor bad.
    private static void applyCasterMinorBad(ServerLevel level, LivingEntity caster, double scale) {
        caster.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 2, 0, false, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 3, 0, false, false, true));
    }

    // S12: caster rare major bad.
    private static void applyCasterMajorBad(ServerLevel level, LivingEntity caster, double scale) {
        if (BeyonderData.isBeyonder(caster)) {
            caster.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 3, 0, false, false, true));
        }

    }

    // S13: target pulse.
    private static void applyTargetPulse(ServerLevel level, LivingEntity caster, LivingEntity target, int casterSeq, double scale) {
        int targetSeq = BeyonderData.isBeyonder(target) ? BeyonderData.getSequence(target) : Integer.MAX_VALUE;
        int seqGap = Math.max(0, targetSeq - casterSeq);

        int roll = level.random.nextInt(100) + Math.min(12, seqGap * 3);
        if (roll > 99) roll = 99;

        if (roll < 28) {
            applyMinorDisorder(level, caster, target, scale, seqGap);
            return;
        }

        if (roll < 50) {
            applyControlLoss(level, caster, target, scale, seqGap);
            return;
        }

        applySeal(level, caster, target, scale, seqGap);
    }

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

    // S14: small disorder — apply 3 random harmful vanilla effects.
    private static void applyMinorDisorder(ServerLevel level, LivingEntity caster, LivingEntity target, double scale, int seqGap) {
        List<Holder<MobEffect>> pool = new ArrayList<>(getHarmfulEffects());
        Collections.shuffle(pool, new java.util.Random(level.random.nextLong()));

        int count = 0;
        for (Holder<MobEffect> effect : pool) {
            if (count >= 3) break;
            target.addEffect(new MobEffectInstance(effect, 20 * (2 + seqGap), 0, false, false, false));
            count++;
        }

        target.setDeltaMovement(target.getDeltaMovement().add(
                (level.random.nextDouble() - 0.5D) * 0.30D * scale,
                0.05D,
                (level.random.nextDouble() - 0.5D) * 0.30D * scale
        ));
        target.hurtMarked = true;
    }

    // S15: heavier control loss — shuffle the target's inventory.
    private static void applyControlLoss(ServerLevel level, LivingEntity caster, LivingEntity target, double scale, int seqGap) {
        if (target instanceof Player player) {
            List<ItemStack> items = player.getInventory().items;
            List<ItemStack> copy = new ArrayList<>(items);
            Collections.shuffle(copy, new java.util.Random(level.random.nextLong()));
            for (int i = 0; i < items.size(); i++) {
                items.set(i, copy.get(i));
            }
            player.getInventory().setChanged();
        }

        int duration = 20 * (3 + seqGap);
        int amplifier = Math.min(2, 1 + seqGap / 2);
        if (BeyonderData.isBeyonder(target)) {
            target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, duration, amplifier, false, false, true));
        } else {
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0, false, false, true));
        }
    }

    // S16: seal.
    private static void applySeal(ServerLevel level, LivingEntity caster, LivingEntity target, double scale, int seqGap) {
        int duration = 20 * (4 + seqGap);

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 2, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0, false, false, true));

        if (BeyonderData.isBeyonder(target)) {
            DisabledAbilitiesComponent component = target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
            component.disableAbilityUsageForTime("frenzy_seal", duration, target);
        } else {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 2, 0, false, false, true));
        }

        if (target instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }

        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;
    }

    // S20: cleanup.
    private static void clearFrenzyTags(LivingEntity target) {
        target.getPersistentData().remove(FRENZY_MARK_KEY);
        target.getPersistentData().remove(FRENZY_UNTIL_KEY);
    }

    private static final class FrenzyMark {
        private int remainingPulses;

        private FrenzyMark(int remainingPulses) {
            this.remainingPulses = remainingPulses;
        }
    }
}
