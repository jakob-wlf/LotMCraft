package de.jakob.lotm.acting;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class ActingHandler {
    
    public static void onActingEvent(Player player, String taskId) {
        String pathway = BeyonderData.getPathway(player);
        int sequence = BeyonderData.getSequence(player);
        
        List<ActingTask> tasks = ActingTaskRegistry.getTasksFor(pathway, sequence);
        System.out.println("Available Tasks: " + tasks.stream().map(ActingTask::getId).toList());
        
        tasks.stream()
            .filter(task -> task.getId().equals(taskId))
            .findFirst()
            .ifPresent(task -> {
                float amount = task.getScaledAmount(sequence);
                BeyonderData.digest(player, amount, true);
            });
    }
}