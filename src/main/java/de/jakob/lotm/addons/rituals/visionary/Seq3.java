package de.jakob.lotm.addons.rituals.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
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
public class Seq3 {
    private static final int AMOUNT = 5;
    private static final float PERCENT = 0.4f;
    private static final Set<UUID> set = new HashSet<>();

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!(player.level() instanceof ServerLevel level))return;
        if(!BeyonderData.getPathway(player).equals("visionary") ||
                BeyonderData.getSequence(player) != 4) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        var targetUuids = component.getTargetUuids();

        for (var target : level.getServer().getPlayerList().getPlayers()) {
            if ((target.isSleeping() || target.hasEffect(ModEffects.ASLEEP))
                    && BeyonderData.getSequence(target) <= 7) {
                targetUuids.add(target.getUUID());
            } else {
                targetUuids.remove(target.getUUID());
            }
        }

        int online = level.players().size();

        if (targetUuids.size() >= AMOUNT && targetUuids.size() >= (online * PERCENT)) {
            component.setCompleted(true);
        }
    }


}
