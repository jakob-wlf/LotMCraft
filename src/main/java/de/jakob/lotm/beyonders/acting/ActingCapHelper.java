package de.jakob.lotm.beyonders.acting;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncActingCapS2CPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class ActingCapHelper {

    public static final String CAP_REDUCTION_KEY = "lotm_acting_cap_reduction";
    public static final String MISSED_ACTING_KEY = "lotm_missed_acting";

    static final float[] INCREMENTS = {0.05f, 0.04f, 0.03f, 0.02f, 0.01f};
    public static final float CAP_PER_SEQUENCE_UP = 0.15f;

    // Set true immediately before a command-driven setBeyonder call; cleared in onSequenceUp.
    public static boolean skipNextCapApplication = false;

    public static float getCapReduction(Player player) {
        return player.getPersistentData().getFloat(CAP_REDUCTION_KEY);
    }

    static void setCapReduction(Player player, float amount) {
        player.getPersistentData().putFloat(CAP_REDUCTION_KEY, Math.clamp(amount, 0f, 1f));
    }

    public static float getEffectiveCap(Player player) {
        if (BeyonderData.getSequence(player) == 0) return 1f;
        return Math.max(0f, 1f - getCapReduction(player));
    }

    public static float getEffectiveCap(LivingEntity entity) {
        if (entity instanceof Player player) return getEffectiveCap(player);
        return 1f;
    }

    public static CompoundTag getMissedActing(Player player) {
        return player.getPersistentData().getCompound(MISSED_ACTING_KEY);
    }

    public static void clearCap(Player player) {
        player.getPersistentData().remove(CAP_REDUCTION_KEY);
        player.getPersistentData().remove(MISSED_ACTING_KEY);
        syncToClient(player);
    }

    /**
     * Called when a player sequences up. Applies flat 15% cap and records missed acting.
     * Only the first INCREMENTS.length (5) tasks matter for cap; extras are ignored.
     * Must be called BEFORE the player's sequence/pathway data is updated.
     */
    public static void onSequenceUp(LivingEntity entity, String oldPathway, int oldSeq) {
        if (!(entity instanceof Player player)) return;
        if (player.isCreative()) return;

        if (skipNextCapApplication) {
            skipNextCapApplication = false;
            return;
        }

        setCapReduction(player, getCapReduction(player) + CAP_PER_SEQUENCE_UP);

        List<ActingTask> tasks = ActingTaskRegistry.getTasksFor(oldPathway, oldSeq);
        if (!tasks.isEmpty()) {
            int completedCount = 0;
            for (ActingTask task : tasks) {
                if (completedCount >= INCREMENTS.length) break;
                if (ActingHelper.isTriggerUnlocked(oldPathway, oldSeq, player, task.getId())) {
                    completedCount++;
                }
            }

            // Only store missed tasks that still have a restoration slot (up to 5 total)
            int maxMissed = INCREMENTS.length - completedCount;
            CompoundTag tasksTag = new CompoundTag();
            int missedCount = 0;
            for (ActingTask task : tasks) {
                if (missedCount >= maxMissed) break;
                if (!ActingHelper.isTriggerUnlocked(oldPathway, oldSeq, player, task.getId())) {
                    tasksTag.putString(task.getId(), "");
                    missedCount++;
                }
            }

            if (!tasksTag.isEmpty()) {
                CompoundTag group = new CompoundTag();
                group.putInt("startIndex", completedCount);
                group.putInt("initialCount", tasksTag.size());
                group.put("tasks", tasksTag);

                CompoundTag missed = getMissedActing(player);
                missed.put(oldPathway + "/" + oldSeq, group);
                player.getPersistentData().put(MISSED_ACTING_KEY, missed);
            }
        }

        syncToClient(player);
    }

    /**
     * Called after a task is newly unlocked at the player's current sequence.
     * Only the first 5 completions affect the cap.
     */
    public static void onActingUnlocked(Player player, String pathway, int sequence, String taskId) {
        if (player.isCreative()) return;

        List<ActingTask> tasks = ActingTaskRegistry.getTasksFor(pathway, sequence);

        int totalUnlocked = 0;
        for (ActingTask task : tasks) {
            if (ActingHelper.isTriggerUnlocked(pathway, sequence, player, task.getId())) {
                totalUnlocked++;
            }
        }

        int position = totalUnlocked - 1;
        if (position >= 0 && position < INCREMENTS.length) {
            setCapReduction(player, Math.max(0f, getCapReduction(player) - INCREMENTS[position]));
            syncToClient(player);
        }
    }

    /**
     * Checks if taskId matches any missed acting group for the player's current pathway.
     * Restoration is determined dynamically by completion order within the group.
     */
    public static boolean tryCompleteMissedActing(Player player, String taskId) {
        CompoundTag missed = getMissedActing(player);
        if (missed.isEmpty()) return false;

        String playerPathway = BeyonderData.getPathway(player);
        boolean found = false;
        float totalRestore = 0f;

        for (String groupKey : missed.getAllKeys()) {
            String[] parts = groupKey.split("/", 2);
            if (parts.length != 2 || !parts[0].equals(playerPathway)) continue;

            CompoundTag group = missed.getCompound(groupKey);
            CompoundTag tasks = group.getCompound("tasks");

            if (!tasks.contains(taskId)) continue;

            int startIndex = group.getInt("startIndex");
            int initialCount = group.getInt("initialCount");
            int retroCompleted = initialCount - tasks.size();
            int restorationIndex = startIndex + retroCompleted;

            if (restorationIndex < INCREMENTS.length) {
                totalRestore += INCREMENTS[restorationIndex];
            }

            tasks.remove(taskId);
            if (tasks.isEmpty()) {
                missed.remove(groupKey);
            } else {
                group.put("tasks", tasks);
                missed.put(groupKey, group);
            }

            found = true;
            break;
        }

        if (found) {
            player.getPersistentData().put(MISSED_ACTING_KEY, missed);
            if (totalRestore > 0f) {
                setCapReduction(player, Math.max(0f, getCapReduction(player) - totalRestore));
            }
            syncToClient(player);
        }

        return found;
    }

    /**
     * On death/respawn: wipe all cap data and reinstate fresh for the player's current sequence only.
     * This clears missed acting from sequences the player no longer holds, avoiding stale data.
     */
    public static void reinstateCapForCurrentSequence(Player player) {
        player.getPersistentData().remove(CAP_REDUCTION_KEY);
        player.getPersistentData().remove(MISSED_ACTING_KEY);

        String pathway = BeyonderData.getPathway(player);
        int seq = BeyonderData.getSequence(player);

        if (seq == 0 || !BeyonderData.isBeyonder(player)) {
            syncToClient(player);
            return;
        }

        // Re-apply cap for current sequence
        setCapReduction(player, CAP_PER_SEQUENCE_UP);

        // Reduce for each completed task (first 5 only)
        List<ActingTask> tasks = ActingTaskRegistry.getTasksFor(pathway, seq);
        int completed = 0;
        for (ActingTask task : tasks) {
            if (completed >= INCREMENTS.length) break;
            if (ActingHelper.isTriggerUnlocked(pathway, seq, player, task.getId())) {
                setCapReduction(player, Math.max(0f, getCapReduction(player) - INCREMENTS[completed]));
                completed++;
            }
        }

        // Store remaining missed tasks (only slots that still exist)
        int maxMissed = INCREMENTS.length - completed;
        if (maxMissed > 0 && !tasks.isEmpty()) {
            CompoundTag tasksTag = new CompoundTag();
            int missedCount = 0;
            for (ActingTask task : tasks) {
                if (missedCount >= maxMissed) break;
                if (!ActingHelper.isTriggerUnlocked(pathway, seq, player, task.getId())) {
                    tasksTag.putString(task.getId(), "");
                    missedCount++;
                }
            }
            if (!tasksTag.isEmpty()) {
                CompoundTag group = new CompoundTag();
                group.putInt("startIndex", completed);
                group.putInt("initialCount", tasksTag.size());
                group.put("tasks", tasksTag);
                CompoundTag missed = new CompoundTag();
                missed.put(pathway + "/" + seq, group);
                player.getPersistentData().put(MISSED_ACTING_KEY, missed);
            }
        }

        syncToClient(player);
    }

    public static void syncToClient(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendToPlayer(serverPlayer, new SyncActingCapS2CPacket(
                    getCapReduction(player),
                    getMissedActing(player)
            ));
        }
    }
}
