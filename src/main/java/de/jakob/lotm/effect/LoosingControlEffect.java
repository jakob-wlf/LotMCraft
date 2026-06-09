package de.jakob.lotm.effect;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
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
        }

        var sanity = livingEntity.getData(ModAttachments.SANITY_COMPONENT.get());
        sanity.decreaseSanityAndSync((float) (0.0025 * amplifier), livingEntity);

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

}
