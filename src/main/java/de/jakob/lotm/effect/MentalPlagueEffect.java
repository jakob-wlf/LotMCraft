package de.jakob.lotm.effect;


import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class MentalPlagueEffect extends MobEffect {

    protected MentalPlagueEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(250 / 255f, 201 / 255f, 102 / 255f),
            1f
    );

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        if(livingEntity.level().isClientSide) return true;

        SanityComponent sanityComponent = livingEntity.getData(ModAttachments.SANITY_COMPONENT);
        sanityComponent.increaseSanityAndSync(-Math.max(0.001f, 0.001f * amplifier), livingEntity);

        AbilityUtil.getNearbyEntities(livingEntity, (ServerLevel) livingEntity.level(), livingEntity.position(), 15).forEach(e -> {
            if(BeyonderData.isBeyonder(e) && BeyonderData.getPathway(e).equals("visionary") && BeyonderData.getSequence(e) <= 4) return;
            if(livingEntity.hasEffect(ModEffects.MENTAL_PLAGUE)) return;

            e.addEffect(new MobEffectInstance(ModEffects.MENTAL_PLAGUE, 20 * 60 * 8, amplifier, false, false, false));
            ParticleUtil.spawnParticles((ServerLevel) e.level(), dust, e.getEyePosition(), 200, .4);
        });

        return true;
    }


}