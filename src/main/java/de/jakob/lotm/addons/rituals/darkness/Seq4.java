package de.jakob.lotm.addons.rituals.darkness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq4 {
    private static int NEEDED = 6;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("darkness") ||
                BeyonderData.getSequence(player) != 5) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(component.getStage() >= NEEDED){
            component.setCompleted(true);
        }
    }

    @SubscribeEvent
    private static void onDeath(LivingDeathEvent event){
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("darkness") ||
                BeyonderData.getSequence(player) != 5) return;

        if(!(event.getEntity() instanceof ServerPlayer target)) return;
        if(BeyonderData.getSequence(target) > 7) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        var factions = BeyonderData.factionStorage.getPartOfFaction(target.getName().getString());
        for(var faction : factions){
            var id = BeyonderData.playerMap.getKeyByName(faction.getLeader());
            if(id == null) continue;

            var data = BeyonderData.playerMap.get(id);
            if(data.isEmpty()) continue;

            int seq = data.get().sequence();
            if(seq <= 4){
                component.setStage(component.getStage() + 1);
                return;
            }
        }
    }

}
