package de.jakob.lotm.util.scheduling;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class ClientScheduler {
    private static final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // scheduleDelayed
    // -------------------------------------------------------------------------

    /** Schedule a task to run once after a delay. */
    public static UUID scheduleDelayed(int delay, Runnable task) {
        return scheduleDelayed(delay, task, null, () -> 1.0);
    }

    /** Schedule a task to run once after a delay with level context. */
    public static UUID scheduleDelayed(int delay, Runnable task, ClientLevel level) {
        return scheduleDelayed(delay, task, level, () -> 1.0);
    }

    /**
     * Schedule a task to run once after a delay, scaled by a time multiplier.
     * <p>
     * The supplier is queried every tick. A multiplier > 1 makes time pass
     * faster (shorter real delay); < 1 makes time pass slower (longer real
     * delay). Pass {@code () -> AbilityUtil.getTimeInArea(entity, location)}
     * to hook into the world's time-change areas.
     *
     * @param delay           Nominal delay in ticks (at multiplier 1.0)
     * @param task            Task to execute
     * @param level           Client level context (may be null)
     * @param timeMultiplier  Supplier returning the current time multiplier
     * @return Task ID for cancellation
     */
    public static UUID scheduleDelayed(int delay, Runnable task,
                                       @Nullable ClientLevel level,
                                       Supplier<Double> timeMultiplier) {
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
                                         Runnable task, ClientLevel level,
                                         Supplier<Boolean> condition) {
        return scheduleRepeating(initialDelay, interval, maxExecutions, task, level, condition, () -> 1.0);
    }

    /**
     * Schedule a task to run repeatedly, scaled by a time multiplier.
     * <p>
     * Both the initial delay and the interval between executions are affected
     * by the multiplier. A multiplier > 1 compresses real time (executions
     * arrive sooner); < 1 stretches it (executions arrive later).
     *
     * @param initialDelay   Nominal initial delay in ticks (at multiplier 1.0)
     * @param interval       Nominal interval in ticks (at multiplier 1.0)
     * @param maxExecutions  Max executions (-1 for infinite)
     * @param task           Task to execute
     * @param level          Client level context (may be null)
     * @param condition      Condition checked before each execution
     * @param timeMultiplier Supplier returning the current time multiplier.
     *                       Pass {@code () -> AbilityUtil.getTimeInArea(entity, location)}
     *                       to apply world time-change effects.
     * @return Task ID for cancellation
     */
    public static UUID scheduleRepeating(int initialDelay, int interval, int maxExecutions,
                                         Runnable task, @Nullable ClientLevel level,
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
                                           Runnable task, ClientLevel level) {
        return scheduleForDuration(initialDelay, interval, duration, task, null, level, () -> 1.0);
    }

    /** Schedule a task for a duration with an onFinish callback. */
    public static UUID scheduleForDuration(int initialDelay, int interval, int duration,
                                           Runnable task, @Nullable Runnable onFinish,
                                           ClientLevel level) {
        return scheduleForDuration(initialDelay, interval, duration, task, onFinish, level, () -> 1.0);
    }

    /**
     * Schedule a task for a nominal duration, scaled by a time multiplier.
     * <p>
     * The duration counts down in "scaled ticks": when the multiplier is 2.0
     * the task finishes in half the real time; when it is 0.5 it takes twice
     * as long. {@code onFinish} is fired once the scaled duration expires.
     *
     * @param initialDelay   Nominal initial delay in ticks (at multiplier 1.0)
     * @param interval       Nominal interval in ticks (at multiplier 1.0)
     * @param duration       Nominal total duration in ticks (at multiplier 1.0)
     * @param task           Task to execute each interval
     * @param onFinish       Optional callback fired once when the duration ends
     * @param level          Client level context (may be null)
     * @param timeMultiplier Supplier returning the current time multiplier.
     *                       Pass {@code () -> AbilityUtil.getTimeInArea(entity, location)}
     *                       to apply world time-change effects.
     * @return Task ID for cancellation
     */
    public static UUID scheduleForDuration(int initialDelay, int interval, int duration,
                                           Runnable task, @Nullable Runnable onFinish,
                                           @Nullable ClientLevel level,
                                           Supplier<Double> timeMultiplier) {
        UUID id = UUID.randomUUID();
        ScheduledTask scheduledTask = new ScheduledTask(
                id, task, initialDelay, interval, -1, level, () -> true, timeMultiplier);
        scheduledTask.setEndTime(duration);
        tasks.put(id, scheduledTask);

        if (onFinish != null) {
            UUID finishId = UUID.randomUUID();
            // The finish task fires at the same nominal duration threshold,
            // also obeying the same time multiplier.
            ScheduledTask finishTask = new ScheduledTask(
                    finishId, onFinish, duration, 0, 1, level, () -> true, timeMultiplier);
            tasks.put(finishId, finishTask);
        }

        return id;
    }

    // -------------------------------------------------------------------------
    // Particle helpers
    // -------------------------------------------------------------------------

    /**
     * Spawn particles at a location for a duration (no time scaling).
     */
    public static UUID spawnParticlesForDuration(ParticleOptions particleType, Vec3 position,
                                                 int duration, int interval, int particlesPerSpawn,
                                                 double spread) {
        return spawnParticlesForDuration(particleType, position, duration, interval,
                particlesPerSpawn, spread, () -> 1.0);
    }

    /**
     * Spawn particles at a location for a duration, optionally time-scaled.
     *
     * @param timeMultiplier Supplier returning the current time multiplier
     */
    public static UUID spawnParticlesForDuration(ParticleOptions particleType, Vec3 position,
                                                 int duration, int interval, int particlesPerSpawn,
                                                 double spread, Supplier<Double> timeMultiplier) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;

        RandomSource random = level.random;

        return scheduleForDuration(0, interval, duration, () -> {
            for (int i = 0; i < particlesPerSpawn; i++) {
                double offsetX = (random.nextDouble() - 0.5) * spread;
                double offsetY = (random.nextDouble() - 0.5) * spread;
                double offsetZ = (random.nextDouble() - 0.5) * spread;
                level.addParticle(particleType,
                        position.x + offsetX, position.y + offsetY, position.z + offsetZ,
                        0, 0, 0);
            }
        }, null, level, timeMultiplier);
    }

    /**
     * Spawn particles in a horizontal circle pattern (no time scaling).
     */
    public static UUID spawnCircleParticles(ParticleOptions particleType, Vec3 center,
                                            double radius, int duration, int interval,
                                            int particleCount) {
        return spawnCircleParticles(particleType, center, radius, duration, interval,
                particleCount, () -> 1.0);
    }

    /**
     * Spawn particles in a horizontal circle pattern, optionally time-scaled.
     *
     * @param timeMultiplier Supplier returning the current time multiplier
     */
    public static UUID spawnCircleParticles(ParticleOptions particleType, Vec3 center,
                                            double radius, int duration, int interval,
                                            int particleCount, Supplier<Double> timeMultiplier) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;

        return scheduleForDuration(0, interval, duration, () -> {
            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI * i) / particleCount;
                double x = center.x + radius * Math.cos(angle);
                double z = center.z + radius * Math.sin(angle);
                level.addParticle(particleType, x, center.y, z, 0, 0, 0);
            }
        }, null, level, timeMultiplier);
    }

    /**
     * Spawn particles in a 3-D oriented circle pattern (no time scaling).
     */
    public static UUID spawnCircleParticles(ParticleOptions particleType, Vec3 center,
                                            Vec3 direction, double radius, int duration,
                                            int interval, int particleCount) {
        return spawnCircleParticles(particleType, center, direction, radius, duration,
                interval, particleCount, () -> 1.0);
    }

    /**
     * Spawn particles in a 3-D oriented circle pattern, optionally time-scaled.
     *
     * @param timeMultiplier Supplier returning the current time multiplier
     */
    public static UUID spawnCircleParticles(ParticleOptions particleType, Vec3 center,
                                            Vec3 direction, double radius, int duration,
                                            int interval, int particleCount,
                                            Supplier<Double> timeMultiplier) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;

        Vec3 normal = direction.normalize();
        Vec3 tangent1 = (Math.abs(normal.x) < 0.9)
                ? new Vec3(1, 0, 0).cross(normal).normalize()
                : new Vec3(0, 1, 0).cross(normal).normalize();
        Vec3 tangent2 = normal.cross(tangent1).normalize();

        return scheduleForDuration(0, interval, duration, () -> {
            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI * i) / particleCount;
                Vec3 offset = tangent1.scale(radius * Math.cos(angle))
                        .add(tangent2.scale(radius * Math.sin(angle)));
                Vec3 pos = center.add(offset);
                level.addParticle(particleType, pos.x, pos.y, pos.z, 0, 0, 0);
            }
        }, null, level, timeMultiplier);
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /** Cancel a scheduled task. */
    public static boolean cancel(UUID taskId) {
        return tasks.remove(taskId) != null;
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
    public static void onClientTick(ClientTickEvent.Pre event) {
        Iterator<Map.Entry<UUID, ScheduledTask>> iterator = tasks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, ScheduledTask> entry = iterator.next();
            ScheduledTask task = entry.getValue();

            if (task.level != null && !task.level.isClientSide()) {
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
        private final ClientLevel level;
        private final Supplier<Boolean> condition;
        /** Supplies the current time multiplier (queried every real tick). */
        private final Supplier<Double> timeMultiplier;

        /**
         * Accumulated "scaled ticks". Advanced each real tick by the current
         * time multiplier, so multiplier > 1 makes time pass faster and < 1
         * makes it pass slower. All threshold comparisons use this value.
         */
        private double ticksElapsed = 0;
        private int executionCount = 0;
        /** Next scaled-tick threshold at which the task fires. */
        private double nextExecutionTick;
        /** Nominal duration expressed as a scaled-tick target (-1 = no limit). */
        private double endTime = -1;

        public ScheduledTask(UUID id, Runnable task, int initialDelay, int interval,
                             int maxExecutions, ClientLevel level,
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

        /**
         * Advances scaled time by the current multiplier and returns whether
         * the next execution threshold has been reached.
         */
        public boolean tick() {
            double multiplier = Math.max(0.0, timeMultiplier.get()); // guard negative values
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