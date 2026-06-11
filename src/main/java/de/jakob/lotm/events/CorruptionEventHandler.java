package de.jakob.lotm.events;

import de.jakob.lotm.attachments.CorruptionComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Random;

@EventBusSubscriber
public class CorruptionEventHandler {

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.level().isClientSide || entity.tickCount % 20 != 0) {
            return;
        }

        CorruptionComponent corruptionComp = entity.getData(ModAttachments.CORRUPTION_COMPONENT);
        float corruption = corruptionComp.getCorruption();
        int corruptionValue = (int) (corruption * 100);

        if (corruptionValue <= 20) {
            return;
        }

        Random random = new Random();

        // PHASE 1: Corruption 21-35 - Early symptoms
        if (corruptionValue <= 35) {
            if (random.nextInt(10) == 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, false));
            }
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 0, false, true));
        }
        // PHASE 2: Corruption 36-50 - Moderate symptoms
        else if (corruptionValue <= 50) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 1, false, true));

            if (random.nextInt(3) == 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false));
            }
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 0, false, true));

            if (random.nextInt(30) == 0) {
                entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), 1.0f);
            }
        }
        // PHASE 3: Corruption 51-65 - Severe symptoms
        else if (corruptionValue <= 65) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 1, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 2, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 1, false, true));

            if (random.nextInt(5) == 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));
            }

            if (entity instanceof Player player && random.nextInt(10) == 0) {
                player.getFoodData().addExhaustion(2.0f);
            }

            if (random.nextInt(20) == 0) {
                entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), 2.0f);
            }
        }
        // PHASE 4: Corruption 66-85 - Critical state
        else if (corruptionValue <= 85) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 2, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 3, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 2, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, false));

            if (random.nextInt(10) == 0) {
                entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), 3.0f);
            }
        }
        // PHASE 5: Corruption 86-100 - Near complete corruption
        else {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 3, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 4, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 1, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 3, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 25, 1, false, true));

            if (random.nextInt(5) == 0) {
                entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), 4.0f);
            }
        }
    }
}
