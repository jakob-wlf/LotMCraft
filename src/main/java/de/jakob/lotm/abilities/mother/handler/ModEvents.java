package de.jakob.lotm.abilities.mother.handler;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ModEvents {
    
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register the hybrid renderer for all living entities
        // This will be checked at runtime via the HybridMobData
    }
    
    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        if(event.getEntity() instanceof LivingEntity living) {
            CompoundTag persistentData = living.getPersistentData();
            
            if(persistentData.contains("HybridMobData")) {
                HybridMobData hybridData = HybridMobData.load(persistentData.getCompound("HybridMobData"));
                EntityDimensions customDimensions = hybridData.getDimensions();
                
                // Override the entity's dimensions with the hybrid dimensions
                event.setNewSize(customDimensions);
            }
        }
    }
}