package de.jakob.lotm.addons.rituals.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq2 {
    private static Map<UUID, Integer> timer = new HashMap<>();
    private static final int TIME_SEC = 20 * 60 * 5;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("door") ||
                BeyonderData.getSequence(player) != 3) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

       if(player.level().dimension() == ModDimensions.SPIRIT_WORLD_DIMENSION_KEY){
           timer.put(player.getUUID(), timer.get(player.getUUID()) + 1);

           if(timer.get(player.getUUID()) >= TIME_SEC) {
               timer.remove(player.getUUID());
               component.setStage(1);
           }
       }

       if(component.getStage() == 1 && player.level().dimension()
               == Level.NETHER){
           timer.put(player.getUUID(), timer.get(player.getUUID()) + 1);

           if(timer.get(player.getUUID()) >= TIME_SEC) {
               timer.remove(player.getUUID());
               component.setStage(2);
           }
       }

        if(component.getStage() == 2 && player.level().dimension()
                == Level.END){
            timer.put(player.getUUID(), timer.get(player.getUUID()) + 1);

            if(timer.get(player.getUUID()) >= TIME_SEC) {
                timer.remove(player.getUUID());
                component.setStage(3);
            }
        }

        if(component.getStage() == 3 && player.level().dimension()
                == ModDimensions.CONCEALMENT_WORLD_DIMENSION_KEY){
            component.setStage(4);
        }

        if(component.getStage() == 4 && player.level().dimension()
                == ModDimensions.DREAM_MAZE_DIMENSION_KEY){
            component.setStage(5);
        }

        if(component.getStage() == 5){
            component.setCompleted(true);
            component.setStage(0);
        }
    }

}
