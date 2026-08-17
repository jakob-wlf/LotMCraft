package de.jakob.lotm.addons.rituals.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.rituals.RitualEffectHandlerEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq3 {

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("door") ||
                BeyonderData.getSequence(player) != 4) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

       if(player.level().dimension() == ModDimensions.SPIRIT_WORLD_DIMENSION_KEY){
           component.setStage(1);
       }

       if(component.getStage() == 1 && player.level().dimension()
               == Level.NETHER){
           component.setStage(2);
       }

        if(component.getStage() == 2 && player.level().dimension()
                == Level.END){
            component.setStage(3);
        }

        if(component.getStage() == 3){
            component.setCompleted(true);
            component.setStage(0);
        }
    }

}
