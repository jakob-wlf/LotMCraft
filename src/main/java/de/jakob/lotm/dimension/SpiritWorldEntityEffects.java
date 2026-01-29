package de.jakob.lotm.dimension;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SpiritWorldEntityEffects {
    
    // Track cooldowns for each entity
    private static final Map<UUID, Long> effectCooldowns = new HashMap<>();
    
    // Effect chances and durations
    private static final float EFFECT_CHECK_CHANCE = 0.02f; // 2% chance per tick (~once per 2.5 seconds)
    private static final int MIN_COOLDOWN = 100; // 5 seconds minimum between effects
    private static final int MAX_COOLDOWN = 400; // 20 seconds maximum between effects
    
    // Effect durations (in ticks)
    private static final int LEVITATION_MIN_DURATION = 40; // 2 seconds
    private static final int LEVITATION_MAX_DURATION = 100; // 5 seconds
    private static final int SLOW_FALLING_MIN_DURATION = 60; // 3 seconds
    private static final int SLOW_FALLING_MAX_DURATION = 200; // 10 seconds
    private static final int JUMP_BOOST_MIN_DURATION = 60; // 3 seconds
    private static final int JUMP_BOOST_MAX_DURATION = 140; // 7 seconds

    @SubscribeEvent
    public static void onLivingUpdate(EntityTickEvent.Pre event) {
        if(!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        // Only apply in Spirit World dimension
        if (!entity.level().dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) {
            return;
        }

        // Check cooldown
        UUID entityId = entity.getUUID();
        long currentTime = entity.level().getGameTime();
        
        if (effectCooldowns.containsKey(entityId)) {
            long cooldownEnd = effectCooldowns.get(entityId);
            if (currentTime < cooldownEnd) {
                return; // Still on cooldown
            }
        }
        
        // Random chance to apply effect
        if (entity.getRandom().nextFloat() > EFFECT_CHECK_CHANCE) {
            return;
        }
        
        // Randomly choose which effect to apply
        int effectChoice = entity.getRandom().nextInt(100);
        
        if (effectChoice < 3) {
            // 40% chance - Levitation
            int duration = 30;
            int amplifier = entity.getRandom().nextInt(2); // 0-1 amplifier
            
            entity.addEffect(new MobEffectInstance(
                MobEffects.LEVITATION,
                duration,
                amplifier,
                false, // ambient
                true,  // visible particles
                true   // show icon
            ));
            
            // Set cooldown
            int cooldown = MIN_COOLDOWN + entity.getRandom().nextInt(MAX_COOLDOWN - MIN_COOLDOWN);
            effectCooldowns.put(entityId, currentTime + cooldown);
            
        } else if (effectChoice < 70) {
            // 30% chance - Slow Falling
            int duration = 60;
            
            entity.addEffect(new MobEffectInstance(
                MobEffects.SLOW_FALLING,
                duration,
                0,     // no amplifier needed
                false,
                true,
                true
            ));
            
            int cooldown = MIN_COOLDOWN + entity.getRandom().nextInt(MAX_COOLDOWN - MIN_COOLDOWN);
            effectCooldowns.put(entityId, currentTime + cooldown);
            
        } else {
            // 30% chance - Jump Boost
            int duration = 120;
            int amplifier = 1 + entity.getRandom().nextInt(3); // 1-3 amplifier
            
            entity.addEffect(new MobEffectInstance(
                MobEffects.JUMP,
                duration,
                amplifier,
                false,
                true,
                true
            ));
            
            int cooldown = MIN_COOLDOWN + entity.getRandom().nextInt(MAX_COOLDOWN - MIN_COOLDOWN);
            effectCooldowns.put(entityId, currentTime + cooldown);
        }
    }
    
    // Clean up cooldown map periodically to prevent memory leaks
    @SubscribeEvent
    public static void onLivingDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        effectCooldowns.remove(event.getEntity().getUUID());
    }
}