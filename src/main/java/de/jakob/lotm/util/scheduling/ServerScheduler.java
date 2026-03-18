package de.jakob.lotm.util.scheduling;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ServerScheduler {
    private static final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();
    private static boolean registered = false;

    public static void initialize() {
        if (!registered) {
            NeoForge.EVENT_BUS.register(ServerScheduler.class);
            registered = true;
        }
    }

    // -------------------------------------------------------------------------
    // scheduleDelayed
    // -------------------------------------------------------------------------

    /** Schedule a task to run once after a delay. */
    public static UUID scheduleDelayed(int delay, Runnable task) {
        if (delay < 0) delay = 0;
        return scheduleDelayed(delay, task, null, () -> 1.0);
    }

    /** Schedule a task to run once after a delay with level context. */
    public static UUID scheduleDelayed(int delay, Runnable task, ServerLevel level) {
        if (delay < 0) delay = 0;
        return scheduleDelayed(delay, task, level, () -> 1.0);
    }

    public static UUID scheduleDelayed(int delay, Runnable task,
                                       @Nullable ServerLevel level,
                                       Supplier<Double> timeMultiplier) {
        if (delay < 0) delay = 0;
        UUID id = UUID.randomUUID();
        tasks.put(id, new ScheduledTask(id, task, delay, 0, 1, level, () -> true, timeMultiplier));
        return id;
    }

    // -------------------------------------------------------------------------
    // scheduleRepeating
    // -------------------------------------------------------------------------

    /** Schedule a task to run repeatedly with intervals. */
    public static UUID scheduleRepeating(int initialDelay, int interval,
                                         int maxExecutions, Runnable task) {
        return scheduleRepeating(initialDelay, interval, maxExecutions, task, null, () -> true, () -> 1.0);
    }

    /** Schedule a task to run repeatedly with intervals and conditions. */
    public static UUID scheduleRepeating(int initialDelay, int interval, int maxExecutions,
                                         Runnable task, ServerLevel level,
                                         Supplier<Boolean> condition) {
        return scheduleRepeating(initialDelay, interval, maxExecutions, task, level, condition, () -> 1.0);
    }

    public static UUID scheduleRepeating(int initialDelay, int interval, int maxExecutions,
                                         Runnable task, @Nullable ServerLevel level,
                                         Supplier<Boolean> condition,
                                         Supplier<Double> timeMultiplier) {
        UUID id = UUID.randomUUID();
        tasks.put(id, new ScheduledTask(id, task, initialDelay, interval, maxExecutions,
                level, condition, timeMultiplier));
        return id;
    }

    // -------------------------------------------------------------------------
    // scheduleForDuration
    // -------------------------------------------------------------------------

    /** Schedule a task to run repeatedly for a specific duration. */
    public static UUID scheduleForDuration(int initialDelay, int interval,
                                           int duration, Runnable task) {
        return scheduleForDuration(initialDelay, interval, duration, task, null, null, () -> 1.0);
    }

    /** Schedule a task to run repeatedly for a specific duration with level context. */
    public static UUID scheduleForDuration(int initialDelay, int interval, int duration,
                                           Runnable task, ServerLevel level) {
        return scheduleForDuration(initialDelay, interval, duration, task, null, level, () -> 1.0);
    }

    /** Schedule a task for a duration with an onFinish callback. */
    public static UUID scheduleForDuration(int initialDelay, int interval, int duration,
                                           Runnable task, @Nullable Runnable onFinish,
                                           ServerLevel level) {
        return scheduleForDuration(initialDelay, interval, duration, task, onFinish, level, () -> 1.0);
    }

    /**
     * Schedule a task for a nominal duration, scaled by a time multiplier.
     * <p>
     * Returns the main task's UUID. Cancelling this UUID via {@link #cancel(UUID)}
     * will also cancel the associated {@code onFinish} task, preventing it from
     * firing after the effect has been externally interrupted (e.g. purification).
     */
    public static UUID scheduleForDuration(int initialDelay, int interval, int duration,
                                           Runnable task, @Nullable Runnable onFinish,
                                           @Nullable ServerLevel level,
                                           Supplier<Double> timeMultiplier) {
        UUID id = UUID.randomUUID();
        ScheduledTask scheduledTask = new ScheduledTask(
                id, task, initialDelay, interval, -1, level, () -> true, timeMultiplier);
        scheduledTask.setEndTime(duration);

        if (onFinish != null) {
            UUID finishId = UUID.randomUUID();
            ScheduledTask finishTask = new ScheduledTask(
                    finishId, onFinish, duration + 1, 0, 1, level, () -> true, timeMultiplier);
            tasks.put(finishId, finishTask);

            // Link the finish task to the main task so cancel(id) removes both
            scheduledTask.setLinkedTaskId(finishId);
        }

        tasks.put(id, scheduledTask);
        return id;
    }

    // -------------------------------------------------------------------------
    // scheduleUntil
    // -------------------------------------------------------------------------

    public static UUID scheduleUntil(ServerLevel level, Runnable task,
                                     @Nullable Runnable onFinish,
                                     AtomicBoolean breakCondition) {
        return scheduleUntil(level, task, 1, onFinish, breakCondition, () -> 1.0);
    }

    public static UUID scheduleUntil(ServerLevel level, Runnable task, int interval,
                                     @Nullable Runnable onFinish,
                                     AtomicBoolean breakCondition) {
        return scheduleUntil(level, task, interval, onFinish, breakCondition, () -> 1.0);
    }

    public static UUID scheduleUntil(ServerLevel level, Runnable task, int interval,
                                     @Nullable Runnable onFinish,
                                     AtomicBoolean breakCondition,
                                     Supplier<Double> timeMultiplier) {
        UUID id = UUID.randomUUID();
        ScheduledTask scheduledTask = new ScheduledTask(
                id, task, 0, interval, -1, level, () -> !breakCondition.get(), timeMultiplier);
        tasks.put(id, scheduledTask);

        if (onFinish != null) {
            UUID finishId = UUID.randomUUID();
            ScheduledTask finishTask = new ScheduledTask(
                    finishId, onFinish, 0, 1, 1, level, breakCondition::get, timeMultiplier);
            tasks.put(finishId, finishTask);

            scheduledTask.setLinkedTaskId(finishId);
        }

        return id;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Cancel a scheduled task.
     * <p>
     * If the task was created by {@link #scheduleForDuration} or
     * {@link #scheduleUntil} with an {@code onFinish} callback, the linked
     * finish task is cancelled automatically so it never fires.
     */
    public static boolean cancel(UUID taskId) {
        ScheduledTask task = tasks.remove(taskId);
        if (task == null) return false;

        // Also cancel the linked onFinish task if present
        if (task.linkedTaskId != null) {
            tasks.remove(task.linkedTaskId);
        }

        return true;
    }

    /** Check if a task is still scheduled. */
    public static boolean isScheduled(UUID taskId) {
        return tasks.containsKey(taskId);
    }

    /** Get the number of currently scheduled tasks. */
    public static int getScheduledTaskCount() {
        return tasks.size();
    }

    // -------------------------------------------------------------------------
    // Tick handler
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        Iterator<Map.Entry<UUID, ScheduledTask>> iterator = tasks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, ScheduledTask> entry = iterator.next();
            ScheduledTask task = entry.getValue();

            if (task.level != null && task.level.isClientSide()) {
                iterator.remove();
                continue;
            }

            if (task.hasEndTime() && task.getElapsedTime() >= task.getEndTime()) {
                iterator.remove();
                continue;
            }

            if (task.tick()) {
                if (task.condition.get()) {
                    try {
                        task.task.run();
                    } catch (Exception e) {
                        System.err.println("Error executing scheduled task: " + e.getMessage());
                        e.printStackTrace();
                    }

                    task.incrementExecutions();

                    if (task.maxExecutions > 0 && task.executionCount >= task.maxExecutions) {
                        iterator.remove();
                    }
                } else {
                    iterator.remove();
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // ScheduledTask
    // -------------------------------------------------------------------------

    private static class ScheduledTask {
        private final UUID id;
        private final Runnable task;
        private final int interval;
        private final int maxExecutions;
        private final ServerLevel level;
        private final Supplier<Boolean> condition;
        private final Supplier<Double> timeMultiplier;

        private double ticksElapsed = 0;
        private int executionCount = 0;
        private double nextExecutionTick;
        private double endTime = -1;

        /**
         * UUID of the companion task (e.g. the onFinish task spawned by
         * scheduleForDuration). Set once at construction time via
         * {@link #setLinkedTaskId}. When this task is cancelled, the linked
         * task is removed from the map as well.
         */
        @Nullable
        private UUID linkedTaskId = null;

        public ScheduledTask(UUID id, Runnable task, int initialDelay, int interval,
                             int maxExecutions, ServerLevel level,
                             Supplier<Boolean> condition, Supplier<Double> timeMultiplier) {
            this.id = id;
            this.task = task;
            this.interval = interval;
            this.maxExecutions = maxExecutions;
            this.level = level;
            this.condition = condition;
            this.timeMultiplier = timeMultiplier;
            this.nextExecutionTick = initialDelay;
        }

        public boolean tick() {
            double multiplier = Math.max(0.0, timeMultiplier.get());
            ticksElapsed += multiplier;
            return ticksElapsed >= nextExecutionTick;
        }

        public void incrementExecutions() {
            executionCount++;
            if (interval > 0) {
                nextExecutionTick = ticksElapsed + interval;
            }
        }

        public void setEndTime(int endTime) {
            this.endTime = endTime;
        }

        public void setLinkedTaskId(UUID linkedTaskId) {
            this.linkedTaskId = linkedTaskId;
        }

        public boolean hasEndTime() {
            return endTime > 0;
        }

        public double getEndTime() {
            return endTime;
        }

        public double getElapsedTime() {
            return ticksElapsed;
        }
    }
}