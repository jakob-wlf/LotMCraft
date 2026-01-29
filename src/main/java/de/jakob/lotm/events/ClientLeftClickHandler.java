package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.MiracleWheelOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class ClientLeftClickHandler {
    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Post event) {
        // When left mouse button is released (button 0, action 0 = release)
        if (event.getButton() == 0) {
            if (event.getAction() == 1) {
                if(MiracleWheelOverlay.getInstance().isOpen()) {
                    MiracleWheelOverlay.getInstance().handleMouseRelease();
                }
            }
        }
    }
}