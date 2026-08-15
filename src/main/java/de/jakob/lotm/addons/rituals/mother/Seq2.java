package de.jakob.lotm.addons.rituals.mother;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.factions.FactionCore;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq2 {

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("mother") ||
                BeyonderData.getSequence(player) != 3) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if (component.isCompleted()) return;

        String name = player.getName().getString();
        var factions = BeyonderData.factionStorage.getPartOfFaction(name);

        for(var faction : factions){
            if(faction.getLeader().equals(name)
                    && faction.getLevel() >= FactionCore.getMaxLevel()){
                component.setCompleted(true);
            }
        }
    }


}
