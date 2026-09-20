package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public final class MercuryBodyRenderer {
    private MercuryBodyRenderer() {}

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        event.getSkins().forEach(skin -> addLayer(event.getSkin(skin)));
        event.getEntityTypes().forEach(entityType -> addLayer(event.getRenderer(entityType)));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addLayer(Object renderer) {
        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new MercuryBodyRenderLayer(livingRenderer));
        }
    }
}