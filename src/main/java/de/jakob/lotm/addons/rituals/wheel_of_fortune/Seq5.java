package de.jakob.lotm.addons.rituals.wheel_of_fortune;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq5 {
    private static final int SECONDS_PER_STAGE = 120;
    private static Map<UUID, Integer> timer = new HashMap<>();

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("wheel_of_fortune") ||
        BeyonderData.getSequence(player) != 6) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(!timer.containsKey(player.getUUID())){
            timer.put(player.getUUID(), 0);
        }

        var luck = player.getData(ModAttachments.LUCK_COMPONENT.get());
        if(luck.getLuck() > -100){
            component.setStage(0);
            component.setCompleted(false);
            timer.remove(player.getUUID());
        }
        else{
            timer.put(player.getUUID(), timer.get(player.getUUID()) + 1);

            if(timer.get(player.getUUID()) % (20 * SECONDS_PER_STAGE) == 0){
                component.setStage(component.getStage() + 1);
            }
        }

        if(component.getStage() >= 30){
            component.setStage(0);
            component.setCompleted(true);
            timer.remove(player.getUUID());
        }
    }

}
