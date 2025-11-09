package de.jakob.lotm.rendering.effectRendering;

import com.mojang.blaze3d.vertex.PoseStack;

public abstract class ActiveEffect {
    public final double x;
    public final double y;
    public final double z;
    
    protected int currentTick = 0;
    protected int maxDuration;

    public ActiveEffect(double x, double y, double z, int maxDuration) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.maxDuration = maxDuration;
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

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
    
    protected float getProgress() {
        return (float) currentTick / maxDuration;
    }
}