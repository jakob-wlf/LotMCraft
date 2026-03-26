package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class GlobalTickHandler {

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if(event.getLevel().isClientSide()) {
            return;
        }

        InteractionHandler.cleanupInteractions();
    }

}
