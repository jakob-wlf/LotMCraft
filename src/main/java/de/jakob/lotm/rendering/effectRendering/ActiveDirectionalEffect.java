package de.jakob.lotm.rendering.effectRendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;

public abstract class ActiveDirectionalEffect {
    public final double startX;
    public final double startY;
    public final double startZ;
    public final double endX;
    public final double endY;
    public final double endZ;
    
    protected int currentTick = 0;
    protected int maxDuration;
    
    // Cached calculations
    protected final Vec3 direction;
    protected final double distance;

    public ActiveDirectionalEffect(double startX, double startY, double startZ, 
                                   double endX, double endY, double endZ, int maxDuration) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
        this.maxDuration = maxDuration;
        
        // Pre-calculate direction and distance
        Vec3 start = new Vec3(startX, startY, startZ);
        Vec3 end = new Vec3(endX, endY, endZ);
        this.direction = end.subtract(start).normalize();
        this.distance = start.distanceTo(end);
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
        return currentTick >= maxDuration;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getStartZ() {
        return startZ;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }

    public double getEndZ() {
        return endZ;
    }
    
    protected float getProgress() {
        return (float) currentTick / maxDuration;
    }
    
    // Helper method to get interpolated position along the path
    protected Vec3 getInterpolatedPosition(float progress) {
        return new Vec3(
            startX + (endX - startX) * progress,
            startY + (endY - startY) * progress,
            startZ + (endZ - startZ) * progress
        );
    }
}