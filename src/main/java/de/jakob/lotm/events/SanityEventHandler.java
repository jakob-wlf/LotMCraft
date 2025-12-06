package de.jakob.lotm.events;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.effect.ModEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Random;

@EventBusSubscriber
public class SanityEventHandler {

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if(!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        // Only process server-side and every 20 ticks (1 second)
        if(entity.level().isClientSide || entity.tickCount % 20 != 0) {
            return;
        }

        SanityComponent sanityComp = entity.getData(ModAttachments.SANITY_COMPONENT);

        float sanity = sanityComp.getSanity();
        int sanityValue = (int)(sanity * 100); // Convert to 0-100 scale

        // Clear all sanity effects if above threshold
        if(sanityValue >= 64) {
            entity.removeEffect(MobEffects.CONFUSION);
            entity.removeEffect(MobEffects.BLINDNESS);
            entity.removeEffect(MobEffects.WEAKNESS);
            entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            entity.removeEffect(MobEffects.DARKNESS);
            entity.removeEffect(ModEffects.LOOSING_CONTROL);
            return;
        }

        Random random = new Random();

        // PHASE 1: Sanity 50-63 - Early symptoms
        if(sanityValue >= 50) {
            // Occasional confusion and visual distortions
            if(random.nextInt(10) == 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, false));
            }
            // Slight weakness
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 0, false, true));
        }

        // PHASE 2: Sanity 35-49 - Moderate symptoms
        else if(sanityValue >= 35) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 1, false, true));

            // Darkness flickers
            if(random.nextInt(3) == 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false));
            }

            // Occasional slowness
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 0, false, true));

            // Random damage spikes (psychosomatic)
            if(random.nextInt(30) == 0) {
                entity.hurt(entity.damageSources().magic(), 1.0f);
            }
        }

        // PHASE 3: Sanity 20-34 - Severe symptoms
        else if(sanityValue >= 20) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 1, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 2, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 1, false, true));

            // Frequent blindness episodes
            if(random.nextInt(5) == 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));
            }

            // Hunger drain (paranoia eating away)
            if(entity instanceof Player player && random.nextInt(10) == 0) {
                player.getFoodData().addExhaustion(2.0f);
            }

            // Random teleportation (short distance - losing grip on reality)
            if(random.nextInt(100) == 0) {
                double offsetX = (random.nextDouble() - 0.5) * 8;
                double offsetZ = (random.nextDouble() - 0.5) * 8;
                entity.teleportTo(entity.getX() + offsetX, entity.getY(), entity.getZ() + offsetZ);
            }

            // Regular damage
            if(random.nextInt(20) == 0) {
                entity.hurt(entity.damageSources().magic(), 2.0f);
            }
        }

        // PHASE 4: Sanity 5-19 - Critical state, starting to lose control
        else if(sanityValue >= 5 && sanityValue < 20) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 2, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 3, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 2, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, false));

            // Apply losing control effect at lower amplifiers
            int amplifier = (int)((20 - sanityValue) / 3.0); // 0-4 amplifier range
            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 100, amplifier, false, true));

            // Frequent damage
            if(random.nextInt(10) == 0) {
                entity.hurt(entity.damageSources().magic(), 3.0f);
            }

            // Random hostile mob sounds to induce paranoia
            if(random.nextInt(40) == 0 && entity instanceof Player player) {
                entity.level().playSound(null, entity.blockPosition(),
                        SoundEvents.ZOMBIE_AMBIENT, SoundSource.HOSTILE, 1.0f, 1.0f);
            }

            // Involuntary drops
            if(entity instanceof Player player && random.nextInt(60) == 0) {
                int slot = random.nextInt(player.getInventory().getContainerSize());
                if(!player.getInventory().getItem(slot).isEmpty()) {
                    player.drop(player.getInventory().getItem(slot).split(1), true);
                }
            }
        }

        // PHASE 5: Sanity 0-4 - Complete loss of control, near certain death
        else if(sanityValue < 5) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 3, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 4, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 1, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 3, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, false));

            // High amplifier losing control - almost certain death
            int amplifier = 5 + (5 - sanityValue); // 5-9 amplifier range
            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 100, amplifier, false, true));

            // Wither effect (mind deteriorating)
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 25, 1, false, true));

            // Constant damage
            if(random.nextInt(5) == 0) {
                entity.hurt(entity.damageSources().magic(), 4.0f);
            }

            // Levitation at random (losing sense of gravity/reality)
            if(random.nextInt(30) == 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40,
                        random.nextBoolean() ? 2 : -2, false, false));
            }

            // Screen shake for players (if you have that capability)
            if(entity instanceof Player player && random.nextInt(10) == 0) {
                entity.hurt(entity.damageSources().magic(), 1.0f);
            }

            // Very high chance of death
            if(random.nextInt(100) < 15) { // 15% chance per second at sanity < 5
                MobEffectInstance controlEffect = entity.getEffect(ModEffects.LOOSING_CONTROL);
                if(controlEffect != null && controlEffect.getAmplifier() >= 8) {
                    // Let the LoosingControlEffect handle the final death
                }
            }
        }
    }

}
