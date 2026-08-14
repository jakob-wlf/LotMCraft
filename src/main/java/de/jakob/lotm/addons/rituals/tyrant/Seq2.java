package de.jakob.lotm.addons.rituals.tyrant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq2 {
    private static final int SECONDS_PER_STAGE = 120;
    private static Map<Integer, Integer> previous = new HashMap<>();
    private static Set<Integer> available = new HashSet<>();

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("tyrant") ||
        BeyonderData.getSequence(player) != 3) return;
        if(!(player.level() instanceof ServerLevel level)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        String name = player.getName().getString();
        var factions = BeyonderData.factionStorage.getPartOfFaction(name);

        for(var obj : factions){
            if(!previous.containsKey(obj.getId())){
                if(obj.getTotalWins()-obj.getTotalWinsAggressor() > 0) {
                    component.setCompleted(true);
                    return;
                }
            }
            else{
                int previousValue = previous.get(obj.getId());
                if(obj.getTotalWins()-obj.getTotalWinsAggressor() > previousValue){
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
                    faction.getValue().getTotalWins()-faction.getValue().getTotalWinsAggressor());
        }
    }

}
