package de.jakob.lotm.rendering.effectRendering;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.util.data.Location;
import net.minecraft.world.phys.Vec3;

public abstract class ActiveMovableEffect {
    protected Location location;
    protected int currentTick = 0;
    protected int maxDuration;
    protected boolean infinite; // If true, effect never finishes until explicitly removed

    public ActiveMovableEffect(Location location, int maxDuration, boolean infinite) {
        this.location = location;
        this.maxDuration = maxDuration;
        this.infinite = infinite;
    }

    public ActiveMovableEffect(Location location, int maxDuration) {
        this(location, maxDuration, false);
    }

    public void update(PoseStack poseStack, float partialTick) {
        float interpolatedTick = currentTick + partialTick;
        render(poseStack, interpolatedTick);
    }

    public void tick() {
        currentTick++;
    }

    protected abstract void render(PoseStack poseStack, float tick);

    public boolean isFinished() {
        if (infinite) return false;
        return currentTick >= maxDuration;
    }

    // Getters and setters for location
    public double getX() {
        return location.getPosition().x;
    }

    public double getY() {
        return location.getPosition().y;
    }

    public double getZ() {
        return location.getPosition().z;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setPosition(Vec3 position) {
        this.location.setPosition(position);
    }

    public void setPosition(double x, double y, double z) {
        this.location.setPosition(new Vec3(x, y, z));
    }

    protected float getProgress() {
        if (infinite) {
            return (float) currentTick / 100f; // Normalized progress for infinite effects
        }
        return (float) currentTick / maxDuration;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public boolean isInfinite() {
        return infinite;
    }
}