package de.jakob.lotm.util.scheduling;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ClientScheduler {
    private static final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();
    private static boolean registered = false;

    public static void initialize() {
        if (!registered) {
            NeoForge.EVENT_BUS.register(ClientScheduler.class);
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
     * @param level Client level context
     * @return Task ID for cancellation
     */
    public static UUID scheduleDelayed(int delay, Runnable task, ClientLevel level) {
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
     * @param level Client level context
     * @param condition Condition to check before each execution
     * @return Task ID for cancellation
     */
    public static UUID scheduleRepeating(int initialDelay, int interval, int maxExecutions,
                                         Runnable task, ClientLevel level, Supplier<Boolean> condition) {
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
     * @param level Client level context
     * @return Task ID for cancellation
     */
    public static UUID scheduleForDuration(int initialDelay, int interval, int duration,
                                           Runnable task, ClientLevel level) {
        UUID id = UUID.randomUUID();
        ScheduledTask scheduledTask = new ScheduledTask(
                id, task, initialDelay, interval, -1, level, () -> true
        );
        scheduledTask.setEndTime(duration);
        tasks.put(id, scheduledTask);
        return id;
    }

    public static UUID scheduleForDuration(int initialDelay, int interval, int duration,
                                           Runnable task, @Nullable Runnable onFinish, ClientLevel level) {
        UUID id = UUID.randomUUID();
        ClientScheduler.ScheduledTask scheduledTask = new ClientScheduler.ScheduledTask(
                id, task, initialDelay, interval, -1, level, () -> true
        );
        scheduledTask.setEndTime(duration);
        tasks.put(id, scheduledTask);

        if (onFinish != null) {
            UUID finishId = UUID.randomUUID();
            ClientScheduler.ScheduledTask finishTask = new ClientScheduler.ScheduledTask(
                    finishId, onFinish, duration, 0, 1, level, () -> true
            );
            tasks.put(finishId, finishTask);
        }

        return id;
    }

    /**
     * Helper method to spawn particles at a location for a duration
     * @param particleType Type of particle to spawn
     * @param position Position to spawn particles
     * @param duration Duration in ticks
     * @param interval Interval between particle spawns
     * @param particlesPerSpawn Number of particles to spawn each time
     * @param spread Spread radius for particles
     * @return Task ID for cancellation
     */
    public static UUID spawnParticlesForDuration(ParticleOptions particleType, Vec3 position,
                                                 int duration, int interval, int particlesPerSpawn,
                                                 double spread) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;

        RandomSource random = level.random;

        return scheduleForDuration(0, interval, duration, () -> {
            for (int i = 0; i < particlesPerSpawn; i++) {
                double offsetX = (random.nextDouble() - 0.5) * spread;
                double offsetY = (random.nextDouble() - 0.5) * spread;
                double offsetZ = (random.nextDouble() - 0.5) * spread;

                level.addParticle(particleType,
                        position.x + offsetX,
                        position.y + offsetY,
                        position.z + offsetZ,
                        0, 0, 0);
            }
        }, level);
    }

    /**
     * Helper method to spawn particles in a circle pattern
     * @param particleType Type of particle to spawn
     * @param center Center position
     * @param radius Circle radius
     * @param duration Duration in ticks
     * @param interval Interval between particle spawns
     * @param particleCount Number of particles in circle
     * @return Task ID for cancellation
     */
    public static UUID spawnCircleParticles(ParticleOptions particleType, Vec3 center,
                                            double radius, int duration, int interval,
                                            int particleCount) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;

        return scheduleForDuration(0, interval, duration, () -> {
            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI * i) / particleCount;
                double x = center.x + radius * Math.cos(angle);
                double z = center.z + radius * Math.sin(angle);

                level.addParticle(particleType, x, center.y, z, 0, 0, 0);
            }
        }, level);
    }

    /**
     * Helper method to spawn particles in a circle pattern
     * @param particleType Type of particle to spawn
     * @param center Center position
     * @param direction Direction vector (normal to the circle plane)
     * @param radius Circle radius
     * @param duration Duration in ticks
     * @param interval Interval between particle spawns
     * @param particleCount Number of particles in circle
     * @return Task ID for cancellation
     */
    public static UUID spawnCircleParticles(ParticleOptions particleType, Vec3 center, Vec3 direction,
                                            double radius, int duration, int interval,
                                            int particleCount) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;

        // Normalize the direction vector
        Vec3 normal = direction.normalize();

        // Create two perpendicular vectors in the plane of the circle
        Vec3 tangent1, tangent2;

        // Find a vector that's not parallel to the normal
        if (Math.abs(normal.x) < 0.9) {
            tangent1 = new Vec3(1, 0, 0).cross(normal).normalize();
        } else {
            tangent1 = new Vec3(0, 1, 0).cross(normal).normalize();
        }

        // Get the second tangent vector perpendicular to both normal and tangent1
        tangent2 = normal.cross(tangent1).normalize();

        return scheduleForDuration(0, interval, duration, () -> {
            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI * i) / particleCount;
                double cosAngle = Math.cos(angle);
                double sinAngle = Math.sin(angle);

                // Calculate position on the circle in 3D space
                Vec3 offset = tangent1.scale(radius * cosAngle).add(tangent2.scale(radius * sinAngle));
                Vec3 particlePos = center.add(offset);

                level.addParticle(particleType, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
            }
        }, level);
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
    public static void onClientTick(ClientTickEvent.Pre event) {
        Iterator<Map.Entry<UUID, ScheduledTask>> iterator = tasks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, ScheduledTask> entry = iterator.next();
            ScheduledTask task = entry.getValue();

            // Check if level context is still valid
            if (task.level != null && !task.level.isClientSide()) {
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
        private final ClientLevel level;
        private final Supplier<Boolean> condition;

        private int ticksElapsed = 0;
        private int executionCount = 0;
        private int nextExecutionTick;
        private int endTime = -1;

        public ScheduledTask(UUID id, Runnable task, int initialDelay, int interval,
                             int maxExecutions, ClientLevel level, Supplier<Boolean> condition) {
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