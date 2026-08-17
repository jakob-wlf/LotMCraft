package de.jakob.lotm.addons.rituals.darkness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.rituals.RitualEffectHandlerEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq3 {

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("darkness") ||
                BeyonderData.getSequence(player) != 4) return;
        if(!(player.level() instanceof ServerLevel level)) return;

        var component = player.getData(ModAttachments.RITUALS.get());


        if(player.hasEffect(ModEffects.LOOSING_CONTROL) && player.getHealth() <= player.getMaxHealth() * 0.2f)
            component.setCompleted(true);
        else if(component.isCompleted()){
            RitualEffectHandlerEvent.removeRitualWithMessage(player);
        }
    }

}
