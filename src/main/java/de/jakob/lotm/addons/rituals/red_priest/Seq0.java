package de.jakob.lotm.addons.rituals.red_priest;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.factions.FactionCore;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq0 {

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("red_priest") ||
                BeyonderData.getSequence(player) != 1) return;
        if(!BeyonderData.hasUniqueness(player)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        int amount = 0;
        var factions = BeyonderData.factionStorage.getAllFactions();
        for(var faction : factions){
            if(faction.getValue().getLevel() >= 1) {
                amount++;
            }
        }

        String name = player.getName().getString();
        var factionPart = BeyonderData.factionStorage.getPartOfFactionType(name, 1);
        if(factionPart != null){
            if(factionPart.getLeader().equals(name)){
                if(factionPart.getAllAtWar().size() >= amount){
                    component.setCompleted(true);
                }
            }
        }
    }

}
