package de.jakob.lotm.addons.rituals.mother;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.factions.FactionCore;
import de.jakob.lotm.addons.rituals.RitualEffectHandlerEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq1 {
    private static Map<UUID, Integer> timer = new HashMap<>();
    private static final int TIME_SEC = 20 * 60 * 10;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("mother") ||
                BeyonderData.getSequence(player) != 2) return;

        var component = player.getData(ModAttachments.RITUALS.get());

        if(!timer.containsKey(player.getUUID())){
            timer.put(player.getUUID(), 0);
        }

       if(player.level().dimension() == ModDimensions.SPACE_DIMENSION_KEY){
           timer.put(player.getUUID(), timer.get(player.getUUID()) + 1);

           if(timer.get(player.getUUID()) >= TIME_SEC) {
               component.setStage(1);
               timer.remove(player.getUUID());
           }
       }

       if(component.getStage() == 1 && player.level().dimension()
               == ModDimensions.SPIRIT_WORLD_DIMENSION_KEY){
           component.setCompleted(true);
           timer.remove(player.getUUID());
       }

       if(player.level().dimension()
               != ModDimensions.SPIRIT_WORLD_DIMENSION_KEY
       && component.isCompleted()){
           RitualEffectHandlerEvent.removeRitualWithMessage(player);
           timer.remove(player.getUUID());
       }
    }

}
