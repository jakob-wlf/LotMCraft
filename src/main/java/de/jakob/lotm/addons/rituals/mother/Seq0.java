package de.jakob.lotm.addons.rituals.mother;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AllyUtil;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq0 {
    private static Map<UUID, Integer> timer = new HashMap<>();
    private static final int TIME_SEC = 20 * 60 * 60;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("mother") ||
        BeyonderData.getSequence(player) != 1) return;
        if(!BeyonderData.hasUniqueness(player)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(!timer.containsKey(player.getUUID())){
            timer.put(player.getUUID(), 0);
        }

        timer.put(player.getUUID(), timer.get(player.getUUID()) + 1);

        var list = AllyUtil.getAllAllies(player);

        for(var obj : list){
            var data = BeyonderData.playerMap.get(obj.uuid());
            if(data == null || data.isEmpty()) continue;

            if(data.get().sequence() <= 0
            && timer.get(player.getUUID()) >= TIME_SEC){
                component.setCompleted(true);
                return;
            }
        }

    }

}
