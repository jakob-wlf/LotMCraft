package de.jakob.lotm.events;

import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.BeyonderData;
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
import java.util.UUID;

@EventBusSubscriber
public class SanityEventHandler {

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if(!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if(entity.level().isClientSide || entity.tickCount % 20 != 0) {
            return;
        }

        SanityComponent sanityComp = entity.getData(ModAttachments.SANITY_COMPONENT);

        boolean isBeyonder = BeyonderData.isBeyonder(entity);
        boolean isHighSequence = isBeyonder && BeyonderData.getSequence(entity) <= 2;
        boolean hasSwitched = BeyonderData.hasSwitchedPathway(entity);
        boolean hasUndigestedStack = isBeyonder
                && entity instanceof Player digestionPlayer
                && BeyonderData.getCurrentCharacteristicCount(entity) > 0
                && BeyonderData.getDigestionProgress(digestionPlayer) < 1.0f;

        boolean shouldDrain = isHighSequence || sanityComp.getSanity() < .2f || hasUndigestedStack || hasSwitched;
        float sanityIncrease = shouldDrain ? 0 : 0.0025f;
        if (isHighSequence || sanityComp.getSanity() < .2f) sanityIncrease -= 0.00025f;
        if (hasUndigestedStack) sanityIncrease -= 0.00025f;
        if (hasSwitched) sanityIncrease -= 0.00025f;
        sanityComp.increaseSanityAndSync(sanityIncrease, entity);

        applySpiritualityExhaustionDrain(entity, sanityComp);
        applyInjuryDrain(entity, sanityComp);

        float sanity = sanityComp.getSanity();
        int sanityValue = (int)(sanity * 100); // Convert to 0-100 scale

        if(sanityValue >= 80) {
            return;
        }

        // Clear all sanity effects if above threshold

        // ---------------- SANITY ----------------
        if (BeyonderData.isBeyonder(entity)) {
            Random random = new Random();
            double sanityMultiplier = getSanityMultiplier(entity, sanity, sanityValue);

            // Always refresh for 2000 ms
            BeyonderData.addModifier(entity, "sanity_loss", sanityMultiplier);

            // ----- RANDOM ABILITY DISABLING -----
            if (!entity.level().isClientSide) {

                int disableChance; // lower = more frequent
                int disableDuration = 20;

                if (sanityValue >= 64) {
                    disableChance = -1; // disabled
                } else if (sanityValue >= 50) {
                    disableChance = 120;
                    disableDuration = 1500;
                } else if (sanityValue >= 35) {
                    disableChance = 80;
                    disableDuration = 2500;
                } else if (sanityValue >= 20) {
                    disableChance = 40;
                    disableDuration = 3500;
                } else {
                    disableChance = 15;
                    disableDuration = 5000;
                }

                if (disableChance > 0 && random.nextInt(disableChance) == 0) {
                    DisabledAbilitiesComponent component = entity.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
                    component.disableAbilityUsageForTime("sanity_instability", disableDuration, entity);
                }
            }
        }


        Random random = new Random();

        if(sanityValue >= 50) {
            if(random.nextInt(10) == 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, false));
            }
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 0, false, true));
        }

        else if(sanityValue >= 35) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 1, false, true));

            if(random.nextInt(3) == 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false));
            }

            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 0, false, true));

            if(random.nextInt(30) == 0) {
                entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), 1.0f);
            }
        }

        else if(sanityValue >= 20) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 1, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 2, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 1, false, true));

            if(random.nextInt(5) == 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));
            }

            if(entity instanceof Player player && random.nextInt(10) == 0) {
                player.getFoodData().addExhaustion(2.0f);
            }

            if(random.nextInt(100) == 0) {
                double offsetX = (random.nextDouble() - 0.5) * 8;
                double offsetZ = (random.nextDouble() - 0.5) * 8;
                entity.teleportTo(entity.getX() + offsetX, entity.getY(), entity.getZ() + offsetZ);
            }

            if(random.nextInt(20) == 0) {
                entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), 2.0f);
            }
        }

        else if(sanityValue >= 5) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 2, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 3, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 2, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, false));

            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 2, 0, false, true));

            if(random.nextInt(10) == 0) {
                entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), 3.0f);
            }

            if(random.nextInt(40) == 0 && entity instanceof Player player) {
                entity.level().playSound(null, entity.blockPosition(),
                        SoundEvents.ZOMBIE_AMBIENT, SoundSource.HOSTILE, 1.0f, 1.0f);
            }

            if(entity instanceof Player player && random.nextInt(60) == 0) {
                int slot = random.nextInt(player.getInventory().getContainerSize());
                if(!player.getInventory().getItem(slot).isEmpty()) {
                    player.drop(player.getInventory().getItem(slot).split(1), true);
                }
            }

            if(random.nextInt(100) >= 97) {
                entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), Float.MAX_VALUE);
            }
        }

        else {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 3, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 4, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 1, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 3, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, false));

            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 25, 1, false, true));

            if(random.nextInt(5) == 0) {
                entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), 4.0f);
            }

            if(random.nextInt(30) == 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40,
                        random.nextBoolean() ? 2 : -2, false, false));
            }

            if(entity instanceof Player player && random.nextInt(10) == 0) {
                ClientHandler.applyCameraShakeToPlayer(1, 20, player);
            }

            if(random.nextInt(100) >= 80) {
                entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), Float.MAX_VALUE);
            }

            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 2, 0, false, true));

        }
    }

    private static final String LOW_SPIRIT_SECONDS_KEY = "lotm_low_spirituality_seconds";

    // Sitting at or below 10% max spirituality drains sanity, ramping up the longer it lasts and resetting on recovery.
    private static void applySpiritualityExhaustionDrain(LivingEntity entity, SanityComponent sanityComp) {
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            entity.getPersistentData().putInt(LOW_SPIRIT_SECONDS_KEY, 0);
            return;
        }
        if (!BeyonderData.isBeyonder(entity)) {
            entity.getPersistentData().putInt(LOW_SPIRIT_SECONDS_KEY, 0);
            return;
        }

        float maxSpirit = BeyonderData.getMaxSpirituality(BeyonderData.getPathway(entity), BeyonderData.getSequence(entity));
        if (maxSpirit <= 0) {
            entity.getPersistentData().putInt(LOW_SPIRIT_SECONDS_KEY, 0);
            return;
        }

        if (BeyonderData.getSpirituality(entity) <= maxSpirit * 0.1f) {
            int seconds = entity.getPersistentData().getInt(LOW_SPIRIT_SECONDS_KEY) + 1;
            entity.getPersistentData().putInt(LOW_SPIRIT_SECONDS_KEY, seconds);

            float drain = getSpiritualityExhaustionDrain(seconds);
            if (drain > 0f) {
                sanityComp.increaseSanityAndSync(-drain, entity);
            }
        } else {
            entity.getPersistentData().putInt(LOW_SPIRIT_SECONDS_KEY, 0);
        }
    }

    private static float getSpiritualityExhaustionDrain(int seconds) {
        if (seconds >= 60) return 0.1f;
        if (seconds >= 35) return 0.01f;
        if (seconds >= 25) return 0.001f;
        if (seconds >= 15) return 0.0005f;
        if (seconds >= 5)  return 0.00025f;
        return 0f;
    }

    // Being injured drains sanity for sequences 9-5; lower health drains more, and sequences 9-8 drain more than 7-5.
    private static void applyInjuryDrain(LivingEntity entity, SanityComponent sanityComp) {
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) return;
        if (!BeyonderData.isBeyonder(entity)) return;

        int sequence = BeyonderData.getSequence(entity);
        if (sequence < 5 || sequence > 9) return;

        float maxHealth = entity.getMaxHealth();
        if (maxHealth <= 0) return;
        float healthFraction = entity.getHealth() / maxHealth;
        if (healthFraction > 0.5f) return;

        boolean higherTier = sequence >= 8;

        float drain;
        if (healthFraction <= 0.1f) {
            drain = higherTier ? 0.01f : 0.0075f;
        } else if (healthFraction <= 0.3f) {
            drain = higherTier ? 0.0065f : 0.0035f;
        } else {
            drain = higherTier ? 0.0015f : 0.0008f;
        }

        sanityComp.increaseSanityAndSync(-drain, entity);
    }

    private static double getSanityMultiplier(LivingEntity entity, float sanity, int sanityValue) {
        float sanityLoss = 1.0f - sanity;
        UUID uuid = entity.getUUID();

        // ----- MULTIPLIER SCALING -----
        double sanityMultiplier;

        if (sanityValue >= 64) {
            sanityMultiplier = 1.0;
        } else if (sanityValue >= 50) {
            sanityMultiplier = 0.95;
        } else if (sanityValue >= 35) {
            sanityMultiplier = 0.85;
        } else if (sanityValue >= 20) {
            sanityMultiplier = 0.7;
        } else {
            sanityMultiplier = Math.max(0.2, 0.5 - sanityLoss);
        }
        return sanityMultiplier;
    }

}
