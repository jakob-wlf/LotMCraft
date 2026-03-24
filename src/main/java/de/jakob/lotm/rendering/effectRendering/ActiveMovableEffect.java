package de.jakob.lotm.rendering.effectRendering;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.util.data.Location;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public abstract class ActiveMovableEffect {
    protected Location location;

    /**
     * Accumulated scaled ticks. Stored as a float so fractional advances
     * from time multipliers < 1 are accumulated smoothly.
     */
    protected float currentTick = 0;
    protected int maxDuration;
    protected boolean infinite;

    /** Defaults to normal speed; overridden via {@link #setTimeMultiplier}. */
    private Supplier<Double> timeMultiplier = () -> 1.0;

    public ActiveMovableEffect(Location location, int maxDuration, boolean infinite) {
        this.location = location;
        this.maxDuration = maxDuration;
        this.infinite = infinite;
    }

    public ActiveMovableEffect(Location location, int maxDuration) {
        this(location, maxDuration, false);
    }

    // -------------------------------------------------------------------------
    // Time multiplier
    // -------------------------------------------------------------------------

    /**
     * Override the time multiplier. Called by {@link MovableEffectFactory}
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
        if (cancelled) return true;
        if (infinite) return false;
        return currentTick >= maxDuration;
    }

    private boolean cancelled = false;

    public void cancel() {
        cancelled = true;
    }

    // -------------------------------------------------------------------------
    // Position
    // -------------------------------------------------------------------------

    public double getX() { return location.getPosition().x; }
    public double getY() { return location.getPosition().y; }
    public double getZ() { return location.getPosition().z; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public void setPosition(Vec3 position) { this.location.setPosition(position); }
    public void setPosition(double x, double y, double z) {
        this.location.setPosition(new Vec3(x, y, z));
    }

    protected float getProgress() {
        if (infinite) return (float) currentTick / 100f;
        return currentTick / maxDuration;
    }

    public float getCurrentTick() { return currentTick; }
    public boolean isInfinite() { return infinite; }
}