package de.jakob.lotm.effect;


import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class AsleepEffect extends MobEffect {

    private static boolean wasAsleepLastTick = false;

    protected AsleepEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        livingEntity.setDeltaMovement(Vec3.ZERO);
        return true;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        boolean isAsleep = mc.player.hasEffect(ModEffects.ASLEEP);
        boolean isFirstPerson = mc.options.getCameraType().isFirstPerson();
        boolean shouldHaveBlur = isAsleep && isFirstPerson;

        // Only apply/remove effect when state changes to avoid unnecessary operations
        if (shouldHaveBlur && !wasAsleepLastTick) {
            // Apply blur effect
            applyBlurEffect(mc);
        } else if (!shouldHaveBlur && wasAsleepLastTick) {
            // Remove blur effect
            removeBlurEffect(mc);
        }

        wasAsleepLastTick = shouldHaveBlur;
    }

    @SubscribeEvent
    public static void jump(LivingEvent.LivingJumpEvent event) {
        if(event.getEntity().hasEffect(ModEffects.ASLEEP)) {
            event.getEntity().setDeltaMovement(new Vec3(0, 0, 0));
        }
    }

    // New event handler for damage - wakes up sleeping entities
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(ModEffects.ASLEEP)) {
            // Remove the asleep effect when the entity takes damage
            entity.removeEffect(ModEffects.ASLEEP);
        }
    }

    private static void applyBlurEffect(Minecraft mc) {
        if (mc.gameRenderer.currentEffect() == null) {
            try {
                ResourceLocation blurShader = ResourceLocation.fromNamespaceAndPath( LOTMCraft.MOD_ID, "shaders/post/blur.json");
                mc.gameRenderer.loadEffect(blurShader);
            } catch (Exception e) {
                LOTMCraft.LOGGER.error("Failed to load blur shader", e);
            }
        }
    }

    private static void removeBlurEffect(Minecraft mc) {
        if (mc.gameRenderer.currentEffect() != null) {
            mc.gameRenderer.shutdownEffect();
        }
    }
}