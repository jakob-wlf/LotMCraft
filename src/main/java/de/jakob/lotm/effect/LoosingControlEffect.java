package de.jakob.lotm.effect;

import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

//@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class LoosingControlEffect extends MobEffect {
    protected LoosingControlEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    Random random = new Random();

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        if(livingEntity.level().isClientSide) return true;

        var personas = livingEntity.getData(ModAttachments.VIRTUAL_PERSONAS);

        if (!personas.hasOnSelf()) {
            float yaw = random.nextFloat() * 360f - 180f;
            float pitch = random.nextFloat() * 60f - 30f;

            livingEntity.setYRot(yaw);
            livingEntity.setXRot(pitch);

            livingEntity.yBodyRot = yaw;
            livingEntity.yHeadRot = yaw;
            livingEntity.hurtMarked = true;
        }

        if(amplifier >= 1) {
            var sanity = livingEntity.getData(ModAttachments.SANITY_COMPONENT.get());
            sanity.decreaseSanityAndSync((float) (0.0025 * amplifier), livingEntity);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

}
