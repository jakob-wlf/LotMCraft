package de.jakob.lotm.beyonders.acting;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.PlayActingEffectS2CPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class ActingHandler {

    public static void onActingEvent(Player player, String taskId) {
        String pathway = BeyonderData.getPathway(player);
        int sequence = BeyonderData.getSequence(player);

        List<ActingTask> tasks = ActingTaskRegistry.getTasksFor(pathway, sequence);

        tasks.stream()
                .filter(task -> task.getId().equals(taskId))
                .filter(task -> !ActingHelper.isOnCooldown(player, task.getId()))
                .findFirst()
                .ifPresent(task -> {
                    float amount = task.getScaledAmount(sequence);
                    BeyonderData.digest(player, amount, true);
                    ActingHelper.setCooldown(player, task.getId(), task.getCooldownTicks());

                    if(!ActingHelper.isTriggerUnlocked(pathway, sequence, player, task.getId())) {
                        if(!player.level().isClientSide) {
                            ActingHelper.unlockTrigger(pathway, sequence, player, task.getId());
                            ActingCapHelper.onActingUnlocked(player, pathway, sequence, task.getId());
                            PacketHandler.sendToPlayer((ServerPlayer) player, new PlayActingEffectS2CPacket());
                        }
                    }
                });

        if (!player.level().isClientSide) {
            ActingCapHelper.tryCompleteMissedActing(player, taskId);
        }
    }
}