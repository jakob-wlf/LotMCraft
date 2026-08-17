package de.jakob.lotm.addons.rituals.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq1 {
    public static HashMap<UUID, Set<UUID>> map = new HashMap<>();
    private static int AMOUNT = 15;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!(player.level() instanceof ServerLevel level))return;
        if(!BeyonderData.getPathway(player).equals("error") ||
                BeyonderData.getSequence(player) != 2) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(!map.containsKey(player.getUUID())){
            map.put(player.getUUID(), new HashSet<>());
        }

        if(map.get(player.getUUID()).size() >= AMOUNT){
            component.setCompleted(true);
            map.remove(player.getUUID());
        }
    }


}
