package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class WeaknessDetectionRenderer {

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        event.getSkins().forEach(name -> addLayerIfPossible(event.getSkin(name)));
        event.getEntityTypes().forEach(type -> addLayerIfPossible(event.getRenderer(type)));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addLayerIfPossible(Object renderer) {
        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new WeaknessDetectionRenderLayer(livingRenderer));
        }
    }
}