package de.jakob.lotm.addons.rituals.fool;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.rituals.RitualEffectHandlerEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq1 {
    private static final int AMOUNT = 24; // dont forget to sync with puppeteering max

    private static Map<UUID, Integer> timer = new HashMap<>();
    private static final int TIME_SEC = 20 * 60 * 120;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!(player.level() instanceof ServerLevel level))return;
        if(!BeyonderData.getPathway(player).equals("fool") ||
                BeyonderData.getSequence(player) != 2) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(!timer.containsKey(player.getUUID())){
            timer.put(player.getUUID(), 0);
        }

        var marionetteList = player.getData(ModAttachments.MARIONETTE_COMPONENT.get()).marionettes;
        int count = 0;
        var nearby = AbilityUtil.getNearbyEntities(null, level, player.position(), 100);
        for(var obj : nearby){
            if(marionetteList.contains(obj.getUUID()))
                count++;
        }

        if(count >= AMOUNT){
            timer.put(player.getUUID(), timer.get(player.getUUID()) + 1);

            if(timer.get(player.getUUID()) >= TIME_SEC) {
                component.setCompleted(true);
                timer.remove(player.getUUID());
            }
        }
        else{
            timer.remove(player.getUUID());
        }
    }


}
