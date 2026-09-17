package de.jakob.lotm.addons.rituals.demoness;

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
public class Seq2 {
    private static Map<Integer, Integer> previous = new HashMap<>();

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("demoness") ||
        BeyonderData.getSequence(player) != 3) return;
        if(!(player.level() instanceof ServerLevel level)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if (component.getStage() == 2) {
            component.setCompleted(true);
        }

        String name = player.getName().getString();
        var factions = BeyonderData.factionStorage.getPartOfFaction(name);

        for(var obj : factions){
            int currentWins = obj.getTotalWins();

            if(!previous.containsKey(obj.getId())){
                previous.put(obj.getId(), currentWins);
                continue;
            }

            int previousWins = previous.get(obj.getId());

            if (currentWins > previousWins) {
                int newWins = currentWins - previousWins;

                previous.put(obj.getId(), currentWins);
                component.setStage(component.getStage() + newWins);

                if (component.getStage() >= 2) {
                    component.setCompleted(true);
                }
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        var factions = BeyonderData.factionStorage.getAllFactions();

        for(var faction : factions){
            previous.put(faction.getKey(), faction.getValue().getTotalWins());
        }
    }

}
