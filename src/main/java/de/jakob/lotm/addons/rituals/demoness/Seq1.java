package de.jakob.lotm.addons.rituals.demoness;

import de.jakob.lotm.LOTMCraft;
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
public class Seq1 {
    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("demoness") ||
                BeyonderData.getSequence(player) != 2) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if (component.isCompleted()) return;

        int amount = 0;
        int ritualAmount = 0;

        var factions = BeyonderData.factionStorage.getAllFactions();
        for (var faction : factions) {
            if (faction.getValue().getLevel() >= 1) {
                amount++;

                if(faction.getValue().isAtAnyWar()){
                    ritualAmount++;
                }
            }
        }

        if(ritualAmount >= amount * 0.8f){
            component.setCompleted(true);
        }
    }
}
