package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.*;

public class DesireControlAbility extends SelectableAbility {
    private final Random random = new Random();
    private final DustParticleOptions desireDust = new DustParticleOptions(new Vector3f(0.9f, 0.3f, 0.6f), 1.3f);
    private static final Set<UUID> targetedEntities = new HashSet<>();

    public DesireControlAbility(String id) {
        super(id, 2.0f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 150;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.desire_control.single_target",
                "ability.lotmcraft.desire_control.aoe"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        switch (abilityIndex) {
            case 0 -> castSingleTargetDesireControl(serverLevel, entity);
            case 1 -> castAoeDesireControl(serverLevel, entity);
        }
    }

    private void castSingleTargetDesireControl(ServerLevel level, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 25, 2.0f);

        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.literal("No target found").withColor(0xFF6699));
            return;
        }

        if (!AbilityUtil.mayDamage(entity, target)) {
            return;
        }

        ParticleUtil.spawnSphereParticles(level, desireDust, target.position().add(0, target.getEyeHeight() / 2, 0), 8, 150);
        level.playSound(null, target.blockPosition(), SoundEvents.BEACON_ACTIVATE, entity.getSoundSource(), 1.2f, 1.5f);

        int duration = calculateDuration(target);
        int amplifier = calculateAmplifier(entity, target);

        target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, duration, amplifier, false, false));

        if (target.hasData(ModAttachments.SANITY_COMPONENT)) {
            target.getData(ModAttachments.SANITY_COMPONENT)
                    .increaseSanityAndSync(-0.3f * (float) multiplier(entity), target);
        }

        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 2, false, false));

        targetedEntities.add(target.getUUID());
        ServerScheduler.scheduleForDuration(0, 10, duration, () -> {
            if (target.isAlive() && targetedEntities.contains(target.getUUID())) {
                ParticleUtil.spawnParticles(level, desireDust,
                        target.position().add(0, target.getEyeHeight() / 2, 0), 5, 0.5, 0.05);
            }
        }, () -> {
            targetedEntities.remove(target.getUUID());
        }, level);
    }

    private void castAoeDesireControl(ServerLevel level, LivingEntity entity) {
        double aoeRadius = 20;

        ParticleUtil.spawnSphereParticles(level, desireDust, entity.position().add(0, 1, 0), 12, 200);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.EFFECT, entity.position().add(0, 1, 0), 10, 100);
        level.playSound(null, entity.blockPosition(), SoundEvents.WITHER_AMBIENT, entity.getSoundSource(), 2.0f, 1.2f);

        AbilityUtil.getNearbyEntities(entity, level, entity.position(), aoeRadius)
                .stream()
                .filter(target -> AbilityUtil.mayDamage(entity, target))
                .forEach(target -> {
                    int amplifier = Math.max(0, calculateAmplifier(entity, target) - 1);
                    target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 6, amplifier, false, false));

                    if (target.hasData(ModAttachments.SANITY_COMPONENT)) {
                        target.getData(ModAttachments.SANITY_COMPONENT)
                                .increaseSanityAndSync(-0.15f * (float) multiplier(entity), target);
                    }

                    target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 4, 1, false, false));
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 4, 2, false, false));
                });
    }

    private int calculateDuration(LivingEntity target) {
        if (target.hasData(ModAttachments.SANITY_COMPONENT)) {
            return 20 * 8;
        }
        return 20 * 6;
    }

    private int calculateAmplifier(LivingEntity caster, LivingEntity target) {
        if (AbilityUtil.isTargetSignificantlyWeaker(caster, target)) {
            return 4;
        }

        int castersSeq = BeyonderData.getSequence(caster);
        int targetSeq = BeyonderData.getSequence(target);
        int diff = targetSeq - castersSeq;

        if (diff >= 2) {
            return 1;
        } else if (diff >= 1) {
            return 2;
        } else if (diff <= -1) {
            return 3;
        }
        return 2;
    }
}