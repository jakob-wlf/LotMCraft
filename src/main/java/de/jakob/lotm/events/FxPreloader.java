package de.jakob.lotm.events;

import com.lowdragmc.photon.client.fx.FXHelper;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gamerule.ClientGameruleCache;
import de.jakob.lotm.util.helper.FxWarmup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class FxPreloader {

    private static final String[] heavyEffects = new String[]{"space_fragmentation", "space_time_storm", "flamevortex", "inferno", "ice_age"};

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) rm -> {
            if(!ClientGameruleCache.isPreLoadEffectsEnabled) return;

            int loaded = 0;
            for (String path : heavyEffects) {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, path);

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