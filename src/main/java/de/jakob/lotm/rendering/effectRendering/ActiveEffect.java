package de.jakob.lotm.rendering.effectRendering;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.function.Supplier;

public abstract class ActiveEffect {
    public final double x;
    public final double y;
    public final double z;

    /**
     * Accumulated scaled ticks. Stored as a double so fractional advances
     * from time multipliers < 1 are accumulated smoothly rather than
     * being truncated. All threshold comparisons are against this value.
     */
    protected float currentTick = 0;
    protected int maxDuration;

    /**
     * Queried every tick to determine how much scaled time to advance.
     * Defaults to {@code () -> 1.0} (normal speed).
     * <p>
     * Set via {@link #setTimeMultiplier} — individual subclasses never need
     * to know about this.
     */
    private Supplier<Double> timeMultiplier = () -> 1.0;

    public ActiveEffect(double x, double y, double z, int maxDuration) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.maxDuration = maxDuration;
    }

    // -------------------------------------------------------------------------
    // Time multiplier
    // -------------------------------------------------------------------------

    /**
     * Override the time multiplier for this effect. Called by
     * {@link EffectFactory} when an entity is provided so that the effect
     * automatically slows or speeds up based on the entity's position inside
     * a {@code TimeChangeEntity} area.
     * <p>
     * Example: {@code effect.setTimeMultiplier(
     *     () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level))
     * )}
     */
    public void setTimeMultiplier(Supplier<Double> timeMultiplier) {
        this.timeMultiplier = timeMultiplier;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    public void update(PoseStack poseStack, float partialTick) {
        // Cast to float for subclass rendering; partialTick is already in [0,1]
        float interpolatedTick = (float) currentTick + partialTick;
        render(poseStack, interpolatedTick);
    }

    /**
     * Advances scaled time by the current multiplier.
     * <ul>
     *   <li>Multiplier > 1 → effect finishes faster (time compressed)</li>
     *   <li>Multiplier < 1 → effect finishes slower (time stretched)</li>
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

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }

    protected float getProgress() {
        return (float) (currentTick / maxDuration);
    }
}