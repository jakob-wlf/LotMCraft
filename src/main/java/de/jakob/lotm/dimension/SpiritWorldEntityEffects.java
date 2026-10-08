package de.jakob.lotm.dimension;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
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
    
    private static final Map<UUID, Long> effectCooldowns = new HashMap<>();
    
    private static final float EFFECT_CHECK_CHANCE = 0.02f;
    private static final int MIN_COOLDOWN = 100;
    private static final int MAX_COOLDOWN = 400;
    
    @SubscribeEvent
    public static void onLivingUpdate(EntityTickEvent.Pre event) {
        if(!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (!entity.level().dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) {
            return;
        }


        UUID entityId = entity.getUUID();
        long currentTime = entity.level().getGameTime();
        
        if (effectCooldowns.containsKey(entityId)) {
            long cooldownEnd = effectCooldowns.get(entityId);
            if (currentTime < cooldownEnd) {
                return;
            }
        }

        if(BeyonderData.getSequence(entity) <= 2) return;
        
        if (entity.getRandom().nextFloat() > EFFECT_CHECK_CHANCE) {
            return;
        }
        
        int effectChoice = entity.getRandom().nextInt(100);
        
        if (effectChoice < 70) {
            int duration = 60;
            
            entity.addEffect(new MobEffectInstance(
                MobEffects.SLOW_FALLING,
                duration,
                0,
                false,
                true,
                true
            ));
            
            int cooldown = MIN_COOLDOWN + entity.getRandom().nextInt(MAX_COOLDOWN - MIN_COOLDOWN);
            effectCooldowns.put(entityId, currentTime + cooldown);
            
        } else {
            int duration = 120;
            int amplifier = 1 + entity.getRandom().nextInt(3);
            
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
    
    @SubscribeEvent
    public static void onLivingDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        effectCooldowns.remove(event.getEntity().getUUID());
    }
}