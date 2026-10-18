package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public final class DawnWeaponClient {
    private DawnWeaponClient() {
    }

    @SubscribeEvent
    public static void registerGeometry(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "dawn_weapon"), DawnWeaponGeometry.LOADER);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(EntityType.TRIDENT, DawnSpearTridentRenderer::new);
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        DawnSpearThrows.clear(event.getEntity().getId());
    }
}
