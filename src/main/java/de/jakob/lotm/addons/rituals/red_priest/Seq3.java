package de.jakob.lotm.addons.rituals.red_priest;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq3 {
    private static Map<Integer, Integer> previous = new HashMap<>();

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("red_priest") ||
        BeyonderData.getSequence(player) != 4) return;
        if(!(player.level() instanceof ServerLevel level)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        String name = player.getName().getString();
        var factions = BeyonderData.factionStorage.getPartOfFaction(name);

        for(var obj : factions){
            if(!previous.containsKey(obj.getId())){
                if(obj.getTotalWins() > 0) {
                    component.setCompleted(true);
                    return;
                }
            }
            else{
                int previousValue = previous.get(obj.getId());
                if(obj.getTotalWins() > previousValue){
                    component.setCompleted(true);
                    return;
                }
            }
        }
    }


    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        var factions = BeyonderData.factionStorage.getAllFactions();

        for(var faction : factions){
            previous.put(faction.getKey(),
                    faction.getValue().getTotalWins());
        }
    }

}
