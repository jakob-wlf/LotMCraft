package de.jakob.lotm.addons.rituals.darkness;

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
public class Seq5 {
    private static Map<UUID, Integer> timer = new HashMap<>();
    private static final int TIME_SEC = 20 * 240;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("darkness") ||
                BeyonderData.getSequence(player) != 6) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(!timer.containsKey(player.getUUID())){
            timer.put(player.getUUID(), 0);
        }

       if(player.level().dimension() == ModDimensions.SPIRIT_WORLD_DIMENSION_KEY){
           timer.put(player.getUUID(), timer.get(player.getUUID()) + 1);

           if(timer.get(player.getUUID()) >= TIME_SEC) {
               component.setCompleted(true);
               timer.remove(player.getUUID());
           }
       }
       else{
           timer.remove(player.getUUID());
       }
    }

}
