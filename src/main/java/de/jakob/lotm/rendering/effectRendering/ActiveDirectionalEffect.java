package de.jakob.lotm.rendering.effectRendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public abstract class ActiveDirectionalEffect {
    public final double startX;
    public final double startY;
    public final double startZ;
    public final double endX;
    public final double endY;
    public final double endZ;

    /**
     * Accumulated scaled ticks. Stored as a float so fractional advances
     * from time multipliers < 1 are accumulated smoothly.
     */
    protected float currentTick = 0;
    protected int maxDuration;

    protected final Vec3 direction;
    protected final double distance;

    /** Defaults to normal speed; overridden via {@link #setTimeMultiplier}. */
    private Supplier<Double> timeMultiplier = () -> 1.0;

    public ActiveDirectionalEffect(double startX, double startY, double startZ,
                                   double endX, double endY, double endZ, int maxDuration) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
        this.maxDuration = maxDuration;

        Vec3 start = new Vec3(startX, startY, startZ);
        Vec3 end = new Vec3(endX, endY, endZ);
        this.direction = end.subtract(start).normalize();
        this.distance = start.distanceTo(end);
    }

    // -------------------------------------------------------------------------
    // Time multiplier
    // -------------------------------------------------------------------------

    /**
     * Override the time multiplier. Called by {@link DirectionalEffectFactory}
     * when an entity is provided so the effect automatically slows or speeds
     * up based on the entity's position inside a {@code TimeChangeEntity} area.
     */
    public void setTimeMultiplier(Supplier<Double> timeMultiplier) {
        this.timeMultiplier = timeMultiplier;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    public void update(PoseStack poseStack, float partialTick) {
        float interpolatedTick = currentTick + partialTick;
        render(poseStack, interpolatedTick);
    }

    /**
     * Advances scaled time by the current multiplier.
     * <ul>
     *   <li>Multiplier > 1 → effect finishes faster</li>
     *   <li>Multiplier < 1 → effect finishes slower</li>
     *   <li>Multiplier = 0 → effect is frozen</li>
     * </ul>
     */
    public void tick() {
        currentTick += (float) Math.max(0.0, timeMultiplier.get());
    }

    protected abstract void render(PoseStack poseStack, float tick);

    public boolean isFinished() {
        return currentTick >= maxDuration || cancelled;
    }

    private boolean cancelled = false;

    public void cancel() {
        cancelled = true;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public double getStartX() { return startX; }
    public double getStartY() { return startY; }
    public double getStartZ() { return startZ; }
    public double getEndX()   { return endX; }
    public double getEndY()   { return endY; }
    public double getEndZ()   { return endZ; }

    protected float getProgress() {
        return currentTick / maxDuration;
    }

    protected Vec3 getInterpolatedPosition(float progress) {
        return new Vec3(
                startX + (endX - startX) * progress,
                startY + (endY - startY) * progress,
                startZ + (endZ - startZ) * progress
        );
    }
}