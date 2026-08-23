package de.jakob.lotm.addons.rituals.red_priest;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.factions.FactionCore;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq1 {
    private static Map<Integer, Integer> previous = new HashMap<>();

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("red_priest") ||
                BeyonderData.getSequence(player) != 2) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        String name = player.getName().getString();
        var faction = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
        if(faction == null || !faction.getLeader().equals(name)) return;

        var defeated = faction.getDefeatedLevels();
        if(defeated.isEmpty()) return;

        if(defeated.getLast() >= faction.getLevel() + 2) {
            if (!previous.containsKey(faction.getId())){
                component.setCompleted(true);
            }
            else if(previous.get(faction.getId()) < faction.getTotalWins()){
                component.setCompleted(true);
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
