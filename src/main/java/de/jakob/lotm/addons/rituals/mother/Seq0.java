package de.jakob.lotm.addons.rituals.mother;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AllyUtil;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq0 {

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("mother") ||
        BeyonderData.getSequence(player) != 1) return;
        if(!BeyonderData.hasUniqueness(player)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        var list = AllyUtil.getAllAllies(player);

        for(var obj : list){
            var data = BeyonderData.playerMap.get(UUID.fromString(obj));
            if(data.isEmpty()) continue;

            if(data.get().sequence() <= 0){
                component.setCompleted(true);
                return;
            }
        }

    }

}
