package de.jakob.lotm.util.helper;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.scheduling.ClientScheduler;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ParticleUtil {

    /**
     * Helper method to spawn particles at a location for a duration
     *
     * @param particleType      Type of particle to spawn
     * @param position          Position to spawn particles
     * @param duration          Duration in ticks
     * @param interval          Interval between particle spawns
     * @param particlesPerSpawn Number of particles to spawn each time
     * @param spread            Spread radius for particles
     */
    public static void spawnParticlesForDuration(ServerLevel level, ParticleOptions particleType, Vec3 position,
                                                 int duration, int interval, int particlesPerSpawn,
                                                 double spread) {
        if (level == null) return;

        RandomSource random = level.random;

        ServerScheduler.scheduleForDuration(0, interval, duration, () -> {
            for (int i = 0; i < particlesPerSpawn; i++) {
                double offsetX = (random.nextDouble() - 0.5) * spread;
                double offsetY = (random.nextDouble() - 0.5) * spread;
                double offsetZ = (random.nextDouble() - 0.5) * spread;

                level.sendParticles(particleType,
                        position.x,
                        position.y,
                        position.z,
                        particlesPerSpawn, offsetX, offsetY, offsetZ, 0);
            }
        }, level);
    }

    public static void drawParticleLine(ServerLevel level, ParticleOptions particleType, Vec3 start, Vec3 end, double step, int particleCount) {
        if (level == null) return;

        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        int steps = (int) Math.ceil(distance / step);

        for (int i = 0; i < steps; i++) {
            double t = (double) i / (steps - 1);
            Vec3 position = start.add(direction.scale(t * distance));

            level.sendParticles(particleType, position.x, position.y, position.z, particleCount, 0, 0, 0, 0);
        }
    }

    public static void drawParticleLine(ServerLevel level, ParticleOptions particleType, Vec3 start, Vec3 end, double step, int particleCount, double offset) {
        if (level == null) return;

        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        int steps = (int) Math.ceil(distance / step);

        for (int i = 0; i < steps; i++) {
            double t = (double) i / (steps - 1);
            Vec3 position = start.add(direction.scale(t * distance));

            level.sendParticles(particleType, position.x, position.y, position.z, particleCount, offset, offset, offset, 0);
        }
    }

    public static void drawParticleLine(ServerLevel level, ParticleOptions particleType, Vec3 start, Vec3 direction, double length, double step, int particleCount) {
        if (level == null) return;

        Vec3 end = start.add(direction.normalize().scale(length));
        drawParticleLine(level, particleType, start, end, step, particleCount);
    }

    public static void drawParticleLine(ServerLevel level, ParticleOptions particleType, Vec3 start, Vec3 direction, double length, double step, int particleCount, double offset) {
        if (level == null) return;

        Vec3 end = start.add(direction.normalize().scale(length));
        drawParticleLine(level, particleType, start, end, step, particleCount, offset);
    }


    /**
     * Helper method to spawn particles in a circle pattern
     *
     * @param particleType  Type of particle to spawn
     * @param center        Center position
     * @param radius        Circle radius
     * @param duration      Duration in ticks
     * @param interval      Interval between particle spawns
     * @param particleCount Number of particles in circle
     */
    public static void spawnCircleParticlesForDuration(ServerLevel level, ParticleOptions particleType, Vec3 center,
                                                       double radius, int duration, int interval,
                                                       int particleCount) {
        if (level == null) return;

        ServerScheduler.scheduleForDuration(0, interval, duration, () -> {
            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI * i) / particleCount;
                double x = center.x + radius * Math.cos(angle);
                double z = center.z + radius * Math.sin(angle);

                level.sendParticles(particleType, x, center.y, z, 1, 0, 0, 0, 0);
            }
        }, level);
    }

    /**
     * Helper method to spawn particles in a circle pattern
     *
     * @param particleType  Type of particle to spawn
     * @param center        Center position
     * @param direction     Direction vector (normal to the circle plane)
     * @param radius        Circle radius
     * @param particleCount Number of particles in circle
     */
    public static void spawnCircleParticles(ServerLevel level, ParticleOptions particleType, Vec3 center, Vec3 direction,
                                                       double radius, int particleCount) {
        if (level == null) return;

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

        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;
            double cosAngle = Math.cos(angle);
            double sinAngle = Math.sin(angle);

            // Calculate position on the circle in 3D space
            Vec3 offset = tangent1.scale(radius * cosAngle).add(tangent2.scale(radius * sinAngle));
            Vec3 particlePos = center.add(offset);

            level.sendParticles(particleType, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Helper method to spawn particles at a location for a duration
     *
     * @param particleType      Type of particle to spawn
     * @param position          Position to spawn particles
     * @param particlesPerSpawn Number of particles to spawn each time
     * @param spread            Spread radius for particles
     */
    public static void spawnParticles(ServerLevel level, ParticleOptions particleType, Vec3 position,
                                                 int particlesPerSpawn, double spread) {
        if (level == null) return;

        RandomSource random = level.random;

        level.sendParticles(particleType,
                position.x,
                position.y,
                position.z,
                particlesPerSpawn, spread, spread, spread, 0);
    }

    public static List<AtomicBoolean> createParticleSpirals(ServerLevel level, ParticleOptions particleType, Vec3 centerPosition, double starRadius, double endRadius, double height, double speed, double density, int duration, int spiralCount, int delayBetweenSpirals) {
        ArrayList<AtomicBoolean> stopConditions = new ArrayList<>();

        int degreeIncrease = 360 / spiralCount;

        for(int i = 0; i < spiralCount; i++) {
            stopConditions.add(createParticleSpiral(level, particleType, centerPosition, starRadius, endRadius, duration, i * degreeIncrease, height, speed, density, delayBetweenSpirals * i));
        }

        return stopConditions;
    }

    public static AtomicBoolean createParticleSpiral(ServerLevel level, ParticleOptions particleType, Vec3 centerPosition, double starRadius, double endRadius, int duration, int startingAngle, double height, double speed, double density, int delay) {
        if(level == null)
            return new AtomicBoolean(true);

        int particleCount = (int) (((int) Math.round(Math.max(starRadius, endRadius))) * 5 * density);

        double startRadians = Math.toRadians(startingAngle);
        double stepSize = (2 * Math.PI) / particleCount;
        int startingI = (int) Math.round(startRadians / stepSize);

        AtomicBoolean shouldStop = new AtomicBoolean(false);

        AtomicDouble radius = new AtomicDouble(starRadius);
        AtomicDouble yPos = new AtomicDouble(centerPosition.y);

        double yIncreaseStep = .15 * speed;
        double radiusIncreaseStep = (Math.abs(endRadius - starRadius)) / (height / yIncreaseStep);

        AtomicInteger i = new AtomicInteger(startingI);
        AtomicInteger delayCountdown = new AtomicInteger(delay);

        ServerScheduler.scheduleForDuration(0, 1, duration, () -> {
            if(shouldStop.get())
                return;

            double angle = (2 * Math.PI * i.get()) / particleCount;
            double x = centerPosition.x + radius.get() * Math.cos(angle);
            double z = centerPosition.z + radius.get() * Math.sin(angle);

            level.sendParticles(particleType, x, yPos.get(), z, 0, 0, 0, 0, 0);

            if(delayCountdown.get() <= 0) {
                yPos.addAndGet(yIncreaseStep);
            }

            radius.addAndGet(radiusIncreaseStep);
            i.addAndGet(1);

            if((yPos.get() - centerPosition.y) > height) {
                yPos.set(centerPosition.y);
                radius.set(starRadius);
            }

            if(delayCountdown.get() > 0)
                delayCountdown.decrementAndGet();

            if(i.get() > particleCount)
                i.set(0);

        }, level);

        return shouldStop;
    }

    public static List<AtomicBoolean> createParticleSpirals(ParticleOptions particleType, Location centerPosition, double starRadius, double endRadius, double height, double speed, double density, int duration, int spiralCount, int delayBetweenSpirals) {
        ArrayList<AtomicBoolean> stopConditions = new ArrayList<>();

        int degreeIncrease = 360 / spiralCount;

        for(int i = 0; i < spiralCount; i++) {
            stopConditions.add(createParticleSpiral(particleType, centerPosition, starRadius, endRadius, duration, i * degreeIncrease, height, speed, density, delayBetweenSpirals * i));
        }

        return stopConditions;
    }

    public static List<AtomicBoolean> createParticleCocoons(ParticleOptions particleType, Location centerPosition, double starRadius, double endRadius, double height, double speed, double density, int duration, int spiralCount, int delayBetweenSpirals) {
        ArrayList<AtomicBoolean> stopConditions = new ArrayList<>();

        int degreeIncrease = 360 / spiralCount;

        for(int i = 0; i < spiralCount; i++) {
            stopConditions.add(createParticleCocoon(particleType, centerPosition, starRadius, endRadius, duration, i * degreeIncrease, height, speed, density, delayBetweenSpirals * i));
        }

        return stopConditions;
    }

    public static AtomicBoolean createParticleCocoon(ParticleOptions particleType, Location positionSupplier, double starRadius, double maxRadius, int duration, int startingAngle, double height, double speed, double density, int delay) {
        int particleCount = (int) (((int) Math.round(Math.max(starRadius, maxRadius))) * 5 * density);

        double startRadians = Math.toRadians(startingAngle);
        double stepSize = (2 * Math.PI) / particleCount;
        int startingI = (int) Math.round(startRadians / stepSize);

        AtomicBoolean shouldStop = new AtomicBoolean(false);

        AtomicDouble radius = new AtomicDouble(starRadius);
        AtomicDouble yPosIncrease = new AtomicDouble(0);

        double yIncreaseStep = .15 * speed;
        double radiusIncreaseStep = (Math.abs(maxRadius - starRadius)) / ((height / 2) / yIncreaseStep);

        AtomicInteger i = new AtomicInteger(startingI);
        AtomicInteger delayCountdown = new AtomicInteger(delay);

        ServerScheduler.scheduleForDuration(0, 1, duration, () -> {
            if(shouldStop.get())
                return;

            if(positionSupplier.getLevel() == null || positionSupplier.getLevel().isClientSide)
                return;

            ServerLevel level = (ServerLevel) positionSupplier.getLevel();

            Vec3 centerPosition = positionSupplier.getPosition();

            double angle = (2 * Math.PI * i.get()) / particleCount;
            double x = centerPosition.x + radius.get() * Math.cos(angle);
            double z = centerPosition.z + radius.get() * Math.sin(angle);

            level.sendParticles(particleType, x, yPosIncrease.get() + centerPosition.y, z,0, 0, 0, 0, 0);

            if(yPosIncrease.get() > height / 2)
                radius.addAndGet(-radiusIncreaseStep);
            else
                radius.addAndGet(radiusIncreaseStep);


            if(delayCountdown.get() <= 0)
                yPosIncrease.addAndGet(yIncreaseStep);

            i.addAndGet(1);

            if(yPosIncrease.get() > height) {
                yPosIncrease.set(0);
                radius.set(starRadius);
            }

            if(delayCountdown.get() > 0)
                delayCountdown.decrementAndGet();

            if(i.get() > particleCount)
                i.set(0);

        });

        return shouldStop;
    }

    public static AtomicBoolean createParticleSpiral(ParticleOptions particleType, Location positionSupplier, double starRadius, double endRadius, int duration, int startingAngle, double height, double speed, double density, int delay) {
        int particleCount = (int) (((int) Math.round(Math.max(starRadius, endRadius))) * 5 * density);

        double startRadians = Math.toRadians(startingAngle);
        double stepSize = (2 * Math.PI) / particleCount;
        int startingI = (int) Math.round(startRadians / stepSize);

        AtomicBoolean shouldStop = new AtomicBoolean(false);

        AtomicDouble radius = new AtomicDouble(starRadius);
        AtomicDouble yPosIncrease = new AtomicDouble(0);

        double yIncreaseStep = .15 * speed;
        double radiusIncreaseStep = (Math.abs(endRadius - starRadius)) / (height / yIncreaseStep);

        AtomicInteger i = new AtomicInteger(startingI);
        AtomicInteger delayCountdown = new AtomicInteger(delay);

        ServerScheduler.scheduleForDuration(0, 1, duration, () -> {
            if(shouldStop.get())
                return;

            if(positionSupplier.getLevel() == null || positionSupplier.getLevel().isClientSide)
                return;

            ServerLevel level = (ServerLevel) positionSupplier.getLevel();

            Vec3 centerPosition = positionSupplier.getPosition();

            double angle = (2 * Math.PI * i.get()) / particleCount;
            double x = centerPosition.x + radius.get() * Math.cos(angle);
            double z = centerPosition.z + radius.get() * Math.sin(angle);

            level.sendParticles(particleType, x, yPosIncrease.get() + centerPosition.y, z,0, 0, 0, 0, 0);

            radius.addAndGet(radiusIncreaseStep);

            if(delayCountdown.get() <= 0)
                yPosIncrease.addAndGet(yIncreaseStep);

            i.addAndGet(1);

            if(yPosIncrease.get() > height) {
                yPosIncrease.set(0);
                radius.set(starRadius);
            }

            if(delayCountdown.get() > 0)
                delayCountdown.decrementAndGet();

            if(i.get() > particleCount)
                i.set(0);

        });

        return shouldStop;
    }

    public static List<AtomicBoolean> createExpandingParticleSpirals(ParticleOptions particleType, Location centerPosition, double starRadius, double endRadius, double height, double speed, double density, int duration, int spiralCount, int delayBetweenSpirals) {
        ArrayList<AtomicBoolean> stopConditions = new ArrayList<>();

        int degreeIncrease = 360 / spiralCount;

        for(int i = 0; i < spiralCount; i++) {
            stopConditions.add(createExpandingParticleSpiral(particleType, centerPosition, starRadius, endRadius, duration, i * degreeIncrease, height, speed, density, delayBetweenSpirals * i));
        }

        return stopConditions;
    }

    public static AtomicBoolean createExpandingParticleSpiral(ParticleOptions particleType, Location positionSupplier, double starRadius, double endRadius, int duration, int startingAngle, double height, double speed, double density, int delay) {
        int particleCount = (int) (((int) Math.round(Math.max(starRadius, endRadius))) * 5 * density);

        double startRadians = Math.toRadians(startingAngle);
        double stepSize = (2 * Math.PI) / particleCount;
        int startingI = (int) Math.round(startRadians / stepSize);

        AtomicBoolean shouldStop = new AtomicBoolean(false);

        AtomicDouble radius = new AtomicDouble(starRadius);
        AtomicDouble yPosIncrease = new AtomicDouble(0);

        double yIncreaseStep = .15 * speed;
        double radiusIncreaseStep = .15 * speed;

        AtomicInteger i = new AtomicInteger(startingI);
        AtomicInteger delayCountdown = new AtomicInteger(delay);

        ServerScheduler.scheduleForDuration(0, 1, duration, () -> {
            if(shouldStop.get())
                return;

            if(positionSupplier.getLevel() == null || positionSupplier.getLevel().isClientSide)
                return;

            ServerLevel level = (ServerLevel) positionSupplier.getLevel();

            Vec3 centerPosition = positionSupplier.getPosition();

            double angle = (2 * Math.PI * i.get()) / particleCount;
            double x = centerPosition.x + radius.get() * Math.cos(angle);
            double z = centerPosition.z + radius.get() * Math.sin(angle);

            level.sendParticles(particleType, x, yPosIncrease.get() + centerPosition.y, z,0, 0, 0, 0, 0);

            radius.addAndGet(radiusIncreaseStep);

            if(delayCountdown.get() <= 0)
                yPosIncrease.addAndGet(yIncreaseStep);

            i.addAndGet(1);

            if(yPosIncrease.get() > height) {
                yPosIncrease.set(0);
            }

            if(radius.get() >= endRadius) {
                radius.set(starRadius);
            }

            if(delayCountdown.get() > 0)
                delayCountdown.decrementAndGet();

            if(i.get() > particleCount)
                i.set(0);

        });

        return shouldStop;
    }

    /**
     * Helper method to spawn particles at a location for a duration
     *
     * @param particleType      Type of particle to spawn
     * @param position          Position to spawn particles
     * @param particlesPerSpawn Number of particles to spawn each time
     * @param spread            Spread radius for particles
     */
    public static void spawnParticles(ServerLevel level, ParticleOptions particleType, Vec3 position,
                                      int particlesPerSpawn, double spread, double speed) {
        if (level == null) return;

        RandomSource random = level.random;

        level.sendParticles(particleType,
                position.x,
                position.y,
                position.z,
                particlesPerSpawn, spread, spread, spread, speed);
    }

    /**
     * Helper method to spawn particles at a location for a duration
     *
     * @param particleType      Type of particle to spawn
     * @param position          Position to spawn particles
     * @param particlesPerSpawn Number of particles to spawn each time
     */
    public static void spawnParticles(ServerLevel level, ParticleOptions particleType, Vec3 position,
                                      int particlesPerSpawn, double spreadX, double spreadY, double spreadZ, double speed) {
        if (level == null) return;

        RandomSource random = level.random;


        level.sendParticles(particleType,
                position.x,
                position.y,
                position.z,
                particlesPerSpawn, spreadX, spreadY, spreadZ, speed);
    }

    /**
     * Helper method to spawn particles in a circle pattern
     *
     * @param particleType  Type of particle to spawn
     * @param center        Center position
     * @param radius        Circle radius
     * @param particleCount Number of particles in circle
     */
    public static void spawnCircleParticles(ServerLevel level, ParticleOptions particleType, Vec3 center,
                                                       double radius, int particleCount) {
        if (level == null) return;

        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);

            level.sendParticles(particleType, x, center.y, z,0, 0, 0, 0, 0);
        }
    }

    /**
     * Helper method to spawn particles in a circle pattern
     *
     * @param particleType  Type of particle to spawn
     * @param center        Center position
     * @param direction     Direction vector (normal to the circle plane)
     * @param radius        Circle radius
     * @param duration      Duration in ticks
     * @param interval      Interval between particle spawns
     * @param particleCount Number of particles in circle
     */
    public static void spawnCircleParticlesForDuration(ServerLevel level, ParticleOptions particleType, Vec3 center, Vec3 direction,
                                                       double radius, int duration, int interval,
                                                       int particleCount) {
        if (level == null) return;

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

        ServerScheduler.scheduleForDuration(0, interval, duration, () -> {
            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI * i) / particleCount;
                double cosAngle = Math.cos(angle);
                double sinAngle = Math.sin(angle);

                // Calculate position on the circle in 3D space
                Vec3 offset = tangent1.scale(radius * cosAngle).add(tangent2.scale(radius * sinAngle));
                Vec3 particlePos = center.add(offset);

                level.sendParticles(particleType, particlePos.x, particlePos.y, particlePos.z, particleCount, 0, 0, 0, 0);
            }
        }, level);
    }

    /**
     * Helper method to spawn particles at a location for a duration
     *
     * @param particleType      Type of particle to spawn
     * @param position          Position to spawn particles
     * @param duration          Duration in ticks
     * @param interval          Interval between particle spawns
     * @param particlesPerSpawn Number of particles to spawn each time
     * @param spread            Spread radius for particles
     */
    public static void spawnParticlesForDuration(ClientLevel level, ParticleOptions particleType, Vec3 position,
                                                 int duration, int interval, int particlesPerSpawn,
                                                 double spread) {
        if (level == null) return;

        RandomSource random = level.random;

        ClientScheduler.scheduleForDuration(0, interval, duration, () -> {
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
        });
    }

    /**
     * Helper method to spawn particles in a circle pattern
     *
     * @param particleType  Type of particle to spawn
     * @param center        Center position
     * @param radius        Circle radius
     * @param duration      Duration in ticks
     * @param interval      Interval between particle spawns
     * @param particleCount Number of particles in circle
     */
    public static void spawnCircleParticlesForDuration(ClientLevel level, ParticleOptions particleType, Vec3 center,
                                                       double radius, int duration, int interval,
                                                       int particleCount) {
        if (level == null) return;

        ClientScheduler.scheduleForDuration(0, interval, duration, () -> {
            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI * i) / particleCount;
                double x = center.x + radius * Math.cos(angle);
                double z = center.z + radius * Math.sin(angle);

                level.addParticle(particleType, x, center.y, z, 0, 0, 0);
            }
        });
    }

    /**
     * Helper method to spawn particles in a circle pattern
     *
     * @param particleType  Type of particle to spawn
     * @param center        Center position
     * @param direction     Direction vector (normal to the circle plane)
     * @param radius        Circle radius
     * @param particleCount Number of particles in circle
     */
    public static void spawnCircleParticles(ClientLevel level, ParticleOptions particleType, Vec3 center, Vec3 direction,
                                            double radius, int particleCount) {
        if (level == null) return;

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

        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;
            double cosAngle = Math.cos(angle);
            double sinAngle = Math.sin(angle);

            // Calculate position on the circle in 3D space
            Vec3 offset = tangent1.scale(radius * cosAngle).add(tangent2.scale(radius * sinAngle));
            Vec3 particlePos = center.add(offset);

            level.addParticle(particleType, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
        }
    }

    /**
     * Helper method to spawn particles at a location
     *
     * @param particleType      Type of particle to spawn
     * @param position          Position to spawn particles
     * @param particlesPerSpawn Number of particles to spawn each time
     * @param spread            Spread radius for particles
     */
    public static void spawnParticles(ClientLevel level, ParticleOptions particleType, Vec3 position,
                                      int particlesPerSpawn, double spread) {
        if (level == null) return;

        RandomSource random = level.random;

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
    }

    public static void spawnParticles(ClientLevel level, ParticleOptions particleType, Vec3 position,
                                      int particlesPerSpawn, double spreadX, double spreadY, double spreadZ, int speed) {
        if (level == null) return;

        RandomSource random = level.random;

        for (int i = 0; i < particlesPerSpawn; i++) {
            double offsetX = (random.nextDouble() - 0.5) * spreadX;
            double offsetY = (random.nextDouble() - 0.5) * spreadY;
            double offsetZ = (random.nextDouble() - 0.5) * spreadZ;

            level.addParticle(particleType,
                    position.x + offsetX,
                    position.y + offsetY,
                    position.z + offsetZ,
                    0, 0, speed);
        }
    }

    /**
     * Helper method to spawn particles at a location with velocity
     *
     * @param particleType      Type of particle to spawn
     * @param position          Position to spawn particles
     * @param particlesPerSpawn Number of particles to spawn each time
     * @param spread            Spread radius for particles
     * @param speed             Initial velocity for particles
     */
    public static void spawnParticles(ClientLevel level, ParticleOptions particleType, Vec3 position,
                                      int particlesPerSpawn, double spread, double speed) {
        if (level == null) return;

        RandomSource random = level.random;

        for (int i = 0; i < particlesPerSpawn; i++) {
            double offsetX = (random.nextDouble() - 0.5) * spread;
            double offsetY = (random.nextDouble() - 0.5) * spread;
            double offsetZ = (random.nextDouble() - 0.5) * spread;

            double velocityX = (random.nextDouble() - 0.5) * speed;
            double velocityY = (random.nextDouble() - 0.5) * speed;
            double velocityZ = (random.nextDouble() - 0.5) * speed;

            level.addParticle(particleType,
                    position.x + offsetX,
                    position.y + offsetY,
                    position.z + offsetZ,
                    velocityX, velocityY, velocityZ);
        }
    }

    /**
     * Helper method to spawn particles in a circle pattern
     *
     * @param particleType  Type of particle to spawn
     * @param center        Center position
     * @param radius        Circle radius
     * @param particleCount Number of particles in circle
     */
    public static void spawnCircleParticles(ClientLevel level, ParticleOptions particleType, Vec3 center,
                                            double radius, int particleCount) {
        if (level == null) return;

        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);

            level.addParticle(particleType, x, center.y, z, 0, 0, 0);
        }
    }

    /**
     * Helper method to spawn particles in a circle pattern for a duration
     *
     * @param particleType  Type of particle to spawn
     * @param center        Center position
     * @param direction     Direction vector (normal to the circle plane)
     * @param radius        Circle radius
     * @param duration      Duration in ticks
     * @param interval      Interval between particle spawns
     * @param particleCount Number of particles in circle
     */
    public static void spawnCircleParticlesForDuration(ClientLevel level, ParticleOptions particleType, Vec3 center, Vec3 direction,
                                                       double radius, int duration, int interval,
                                                       int particleCount) {
        if (level == null) return;

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

        ClientScheduler.scheduleForDuration(0, interval, duration, () -> {
            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI * i) / particleCount;
                double cosAngle = Math.cos(angle);
                double sinAngle = Math.sin(angle);

                // Calculate position on the circle in 3D space
                Vec3 offset = tangent1.scale(radius * cosAngle).add(tangent2.scale(radius * sinAngle));
                Vec3 particlePos = center.add(offset);

                level.addParticle(particleType, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
            }
        });
    }

}
