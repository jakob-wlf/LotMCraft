package de.jakob.lotm.acting;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class ActingHandler {

    public static void onActingEvent(Player player, String taskId) {
        String pathway = BeyonderData.getPathway(player);
        int sequence = BeyonderData.getSequence(player);

        List<ActingTask> tasks = ActingTaskRegistry.getTasksFor(pathway, sequence);

        tasks.stream()
                .filter(task -> task.getId().equals(taskId))
                .filter(task -> !ActingCooldownHelper.isOnCooldown(player, task.getId()))
                .findFirst()
                .ifPresent(task -> {
                    float amount = task.getScaledAmount(sequence);
                    BeyonderData.digest(player, amount, true);
                    player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, .025f, 1);
                    ActingCooldownHelper.setCooldown(player, task.getId(), task.getCooldownTicks());
                });
    }
}