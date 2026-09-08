package de.jakob.lotm.addons.rituals.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.Prophecy;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.LinkedList;
import java.util.List;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq0 {
    private static final int AMOUNT = 20;
    private static final int MIX_PLAYERS = 20;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!(player.level() instanceof ServerLevel level))return;
        if(!BeyonderData.getPathway(player).equals("visionary") ||
                BeyonderData.getSequence(player) != 1) return;
        if(!BeyonderData.hasUniqueness(player)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        var playerList = level.getServer().getPlayerList().getPlayers();
        component.setStage(0);
        for(var target : playerList){
            if(player.getUUID().equals(target.getUUID())) continue;

            var data = BeyonderData.playerMap.get(target);
            if(data.isEmpty()) continue;

            List<Prophecy> all = new LinkedList<>(
                    data.get().prophecies());

            all = all.stream().filter(obj -> obj.casterId().equals(player.getUUID())).toList();

            if(all.size() >= AMOUNT)
                component.setStage(component.getStage() + 1);
        }

        if(playerList.size() <= MIX_PLAYERS || component.getStage() == 0) return;

        if(component.getStage() >= playerList.size()-1){
            component.setCompleted(true);
        }
    }


}
