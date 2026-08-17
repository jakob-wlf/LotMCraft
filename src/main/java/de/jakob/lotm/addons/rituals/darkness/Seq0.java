package de.jakob.lotm.addons.rituals.darkness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq0 {
    private static int NEEDED = 20 * 20;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("darkness") ||
                BeyonderData.getSequence(player) != 1) return;
        if(!BeyonderData.hasUniqueness(player)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if (component.isCompleted()) return;

        var target = AbilityUtil.getTargetEntity(player, 10, 0.1f, true);
        if(!(target instanceof ServerPlayer targetPlayer)) return;

        if(BeyonderData.getSequence(targetPlayer) != 0) return;

        if(target.hasEffect(ModEffects.ASLEEP)){
            component.setStage(component.getStage() + 1);
        }
        else{
            component.setStage(0);
        }

        if(component.getStage() >= NEEDED){
            component.setCompleted(true);
        }
    }

}
