package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class PetrificationRenderer {

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        // Add the petrification layer to all living entity renderers
        event.getSkins().forEach(skin -> {
            addLayerIfPossible(event.getSkin(skin));
        });
        
        event.getEntityTypes().forEach(entityType -> {
            addLayerIfPossible(event.getRenderer(entityType));
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addLayerIfPossible(Object renderer) {
        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new PetrificationRenderLayer(livingRenderer));
        }
    }
}