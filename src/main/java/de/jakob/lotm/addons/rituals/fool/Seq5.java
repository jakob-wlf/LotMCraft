package de.jakob.lotm.addons.rituals.fool;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.rituals.RitualEffectHandlerEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq5 {
    public static Set<UUID> affected = new HashSet<>();

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("fool") ||
        BeyonderData.getSequence(player) != 6) return;

        var component = player.getData(ModAttachments.RITUALS.get());

        if(affected.contains(player.getUUID())){
            component.setCompleted(true);
        }
        else{
            if(component.isCompleted()){
                RitualEffectHandlerEvent.removeRitualWithMessage(player);
            }
        }
    }

}
