package de.jakob.lotm.addons.rituals.darkness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq1 {

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("darkness") ||
                BeyonderData.getSequence(player) != 2) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if (component.isCompleted()) return;

        var target = AbilityUtil.getTargetEntity(player, 10, 0.1f, true);
        if(!(target instanceof ServerPlayer targetPlayer)) return;

        if(BeyonderData.getSequence(targetPlayer) > 1) return;

        var luck = targetPlayer.getData(ModAttachments.LUCK_COMPONENT.get());
        if(luck.getLuck() <= -5000
                && targetPlayer.getHealth() <= (targetPlayer.getMaxHealth() * 0.5f)){
            component.setCompleted(true);
        }
    }

}
