package de.jakob.lotm.util.scheduling;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    /**
     * Schedule a task to run once after a delay
     * @param delay Delay in ticks
     * @param task Task to execute
     * @return Task ID for cancellation
     */
    public static UUID scheduleDelayed(int delay, Runnable task) {
        return scheduleDelayed(delay, task, null);
    }

    /**
     * Schedule a task to run once after a delay with level context
     * @param delay Delay in ticks
     * @param task Task to execute
     * @param level Server level context
     * @return Task ID for cancellation
     */
    public static UUID scheduleDelayed(int delay, Runnable task, ServerLevel level) {
        UUID id = UUID.randomUUID();
        ScheduledTask scheduledTask = new ScheduledTask(
                id, task, delay, 0, 1, level, () -> true
        );
        tasks.put(id, scheduledTask);
        return id;
    }

    /**
     * Schedule a task to run repeatedly with intervals
     * @param initialDelay Initial delay in ticks
     * @param interval Interval between executions in ticks
     * @param maxExecutions Maximum number of executions (-1 for infinite)
     * @param task Task to execute
     * @return Task ID for cancellation
     */
    public static UUID scheduleRepeating(int initialDelay, int interval, int maxExecutions, Runnable task) {
        return scheduleRepeating(initialDelay, interval, maxExecutions, task, null, () -> true);
    }

    /**
     * Schedule a task to run repeatedly with intervals and conditions
     * @param initialDelay Initial delay in ticks
     * @param interval Interval between executions in ticks
     * @param maxExecutions Maximum number of executions (-1 for infinite)
     * @param task Task to execute
     * @param level Server level context
     * @param condition Condition to check before each execution
     * @return Task ID for cancellation
     */
    public static UUID scheduleRepeating(int initialDelay, int interval, int maxExecutions,
                                         Runnable task, ServerLevel level, Supplier<Boolean> condition) {
        UUID id = UUID.randomUUID();
        ScheduledTask scheduledTask = new ScheduledTask(
                id, task, initialDelay, interval, maxExecutions, level, condition
        );
        tasks.put(id, scheduledTask);
        return id;
    }

    /**
     * Schedule a task to run repeatedly for a specific duration
     * @param initialDelay Initial delay in ticks
     * @param interval Interval between executions in ticks
     * @param duration Total duration in ticks
     * @param task Task to execute
     * @return Task ID for cancellation
     */
    public static UUID scheduleForDuration(int initialDelay, int interval, int duration, Runnable task) {
        return scheduleForDuration(initialDelay, interval, duration, task, null);
    }

    /**
     * Schedule a task to run repeatedly for a specific duration with level context
     * @param initialDelay Initial delay in ticks
     * @param interval Interval between executions in ticks
     * @param duration Total duration in ticks
     * @param task Task to execute
     * @param level Server level context
     * @return Task ID for cancellation
     */
    public static UUID scheduleForDuration(int initialDelay, int interval, int duration,
                                           Runnable task, ServerLevel level) {
        UUID id = UUID.randomUUID();
        ScheduledTask scheduledTask = new ScheduledTask(
                id, task, initialDelay, interval, -1, level, () -> true
        );
        scheduledTask.setEndTime(duration);
        tasks.put(id, scheduledTask);
        return id;
    }

    /**
     * Schedule a task to run repeatedly for a specific duration with level context
     * @param initialDelay Initial delay in ticks
     * @param interval Interval between executions in ticks
     * @param duration Total duration in ticks
     * @param task Task to execute
     * @param onFinish Task to execute when the duration ends
     * @param level Server level context
     * @return Task ID for cancellation
     */
    public static UUID scheduleForDuration(int initialDelay, int interval, int duration,
                                           Runnable task, @Nullable Runnable onFinish, ServerLevel level) {
        UUID id = UUID.randomUUID();
        ScheduledTask scheduledTask = new ScheduledTask(
                id, task, initialDelay, interval, -1, level, () -> true
        );
        scheduledTask.setEndTime(duration);
        tasks.put(id, scheduledTask);

        if (onFinish != null) {
            UUID finishId = UUID.randomUUID();
            ScheduledTask finishTask = new ScheduledTask(
                    finishId, onFinish, duration + 1, 0, 1, level, () -> true
            );
            tasks.put(finishId, finishTask);
        }

        return id;
    }

    /**
     * Schedule a task that runs every tick until the breakCondition becomes true.
     * Executes onFinish once when the condition is met.
     *
     * @param level Server level context
     * @param task Task to execute each tick
     * @param onFinish Task to execute when condition is met
     * @param breakCondition Condition to stop execution
     * @return Task ID for cancellation
     */
    public static UUID scheduleUntil(ServerLevel level, Runnable task,
                                     @Nullable Runnable onFinish,
                                     java.util.concurrent.atomic.AtomicBoolean breakCondition) {
        UUID id = UUID.randomUUID();
        ScheduledTask scheduledTask = new ScheduledTask(
                id, task, 0, 1, -1, level, () -> !breakCondition.get()
        );
        tasks.put(id, scheduledTask);

        if (onFinish != null) {
            UUID finishId = UUID.randomUUID();
            ScheduledTask finishTask = new ScheduledTask(
                    finishId, onFinish, 0, 1, 1, level, breakCondition::get
            );
            tasks.put(finishId, finishTask);
        }

        return id;
    }    /**
     * Schedule a task that runs repeatedly at a given interval until the breakCondition becomes true.
     * Executes onFinish once when the condition is met.
     *
     * @param level Server level context
     * @param task Task to execute each interval
     * @param interval Interval between executions in ticks
     * @param onFinish Task to execute when condition is met
     * @param breakCondition Condition to stop execution
     * @return Task ID for cancellation
     */
    public static UUID scheduleUntil(ServerLevel level, Runnable task, int interval,
                                     @Nullable Runnable onFinish,
                                     java.util.concurrent.atomic.AtomicBoolean breakCondition) {
        UUID id = UUID.randomUUID();
        ScheduledTask scheduledTask = new ScheduledTask(
                id, task, 0, interval, -1, level, () -> !breakCondition.get()
        );
        tasks.put(id, scheduledTask);

        if (onFinish != null) {
            UUID finishId = UUID.randomUUID();
            ScheduledTask finishTask = new ScheduledTask(
                    finishId, onFinish, 0, 1, 1, level, breakCondition::get
            );
            tasks.put(finishId, finishTask);
        }

        return id;
    }


    /**
     * Cancel a scheduled task
     * @param taskId Task ID to cancel
     * @return true if task was found and cancelled
     */
    public static boolean cancel(UUID taskId) {
        return tasks.remove(taskId) != null;
    }

    /**
     * Check if a task is still scheduled
     * @param taskId Task ID to check
     * @return true if task exists and is scheduled
     */
    public static boolean isScheduled(UUID taskId) {
        return tasks.containsKey(taskId);
    }

    /**
     * Get the number of currently scheduled tasks
     * @return Number of scheduled tasks
     */
    public static int getScheduledTaskCount() {
        return tasks.size();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        Iterator<Map.Entry<UUID, ScheduledTask>> iterator = tasks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, ScheduledTask> entry = iterator.next();
            ScheduledTask task = entry.getValue();

            // Check if level context is still valid
            if (task.level != null && task.level.isClientSide()) {
                iterator.remove();
                continue;
            }

            // Check end time condition
            if (task.hasEndTime() && task.getElapsedTime() >= task.getEndTime()) {
                iterator.remove();
                continue;
            }

            // Check if it's time to execute
            if (task.tick()) {
                // Check condition before execution
                if (task.condition.get()) {
                    try {
                        task.task.run();
                    } catch (Exception e) {
                        System.err.println("Error executing scheduled task: " + e.getMessage());
                        e.printStackTrace();
                    }

                    task.incrementExecutions();

                    // Remove if max executions reached
                    if (task.maxExecutions > 0 && task.executionCount >= task.maxExecutions) {
                        iterator.remove();
                    }
                } else {
                    // Condition failed, remove task
                    iterator.remove();
                }
            }
        }
    }

    private static class ScheduledTask {
        private final UUID id;
        private final Runnable task;
        private final int initialDelay;
        private final int interval;
        private final int maxExecutions;
        private final ServerLevel level;
        private final Supplier<Boolean> condition;

        private int ticksElapsed = 0;
        private int executionCount = 0;
        private int nextExecutionTick;
        private int endTime = -1;

        public ScheduledTask(UUID id, Runnable task, int initialDelay, int interval,
                             int maxExecutions, ServerLevel level, Supplier<Boolean> condition) {
            this.id = id;
            this.task = task;
            this.initialDelay = initialDelay;
            this.interval = interval;
            this.maxExecutions = maxExecutions;
            this.level = level;
            this.condition = condition;
            this.nextExecutionTick = initialDelay;
        }

        public boolean tick() {
            ticksElapsed++;
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

        public boolean hasEndTime() {
            return endTime > 0;
        }

        public int getEndTime() {
            return endTime;
        }

        public int getElapsedTime() {
            return ticksElapsed;
        }
    }
}