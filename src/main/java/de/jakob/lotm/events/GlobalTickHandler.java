package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.attachments.DoorAuthorityData;
import de.jakob.lotm.attachments.SealedDimensionData;
import net.minecraft.server.level.ServerLevel;
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
        DoorAuthorityData doorData = DoorAuthorityData.get((ServerLevel) event.getLevel());

        if (doorData.isActive()) {
            doorData.tick();
        }

        SealedDimensionData sealedDimensionData = SealedDimensionData.get((ServerLevel) event.getLevel());

        if (sealedDimensionData.isActive()) {
            sealedDimensionData.tick();
        }
    }

}
