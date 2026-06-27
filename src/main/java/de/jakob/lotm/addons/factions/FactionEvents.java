package de.jakob.lotm.addons.factions;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class FactionEvents {

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
//        if(!(event.getPlayer() instanceof ServerPlayer player)) return;
//
//        var pos = player.level().getChunkAt(event.getPos()).getPos();
//        FactionCore faction = BeyonderData.factionStorage.getFaction(pos);
//
//        if(faction == null) return;
//
//        var name = player.getName().getString();
//        if(faction.isPartOfFaction(name) && faction.canDoAnything(name, pos)) return;
//        else{
//            var data = BeyonderData.playerMap.get(BeyonderData.playerMap.getKeyByName(faction.getLeader())).get();
//            int seq = BeyonderData.getSequence(player);
//
//            int targetSeq = data.pathway().equals("justicar") ? data.sequence()-1 : data.sequence();
//
//            if(seq <= 2 && seq <= targetSeq) return;
//        }
//
//        event.setCanceled(true);
    }
}
