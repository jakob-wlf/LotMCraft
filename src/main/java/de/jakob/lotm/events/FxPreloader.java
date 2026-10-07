package de.jakob.lotm.events;

import com.lowdragmc.photon.client.fx.FXHelper;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.FxWarmup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class FxPreloader {

    private static final String FOLDER = "fx";

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) rm -> {
            var files = rm.listResources(FOLDER, loc -> loc.getNamespace().equals(LOTMCraft.MOD_ID));

            int loaded = 0;
            for (ResourceLocation file : files.keySet()) {
                String path = file.getPath();
                int dot = path.lastIndexOf('.');
                if (dot < 0) continue;

                String name = path.substring(FOLDER.length() + 1, dot);
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, name);

                try {
                    FxWarmup.queue(id);
                    loaded++;
                } catch (Exception e) {
                    LOTMCraft.LOGGER.warn("Failed to preload FX {}", id, e);
                }
            }
            LOTMCraft.LOGGER.info("Preloaded {} Photon FX", loaded);
        });
    }
}