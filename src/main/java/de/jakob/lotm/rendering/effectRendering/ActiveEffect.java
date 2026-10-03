package de.jakob.lotm.rendering.effectRendering;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.util.data.Location;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.function.Supplier;

public abstract class ActiveEffect {

    private UUID id = UUID.randomUUID();
    protected Location location;
    protected float[] params = EffectParams.defaultParamsArray();
    protected int maxDuration;
    protected boolean infinite;

    protected float currentTick = 0;

    private Supplier<Double> timeMultiplier = () -> 1.0;
    private boolean cancelled = false;

    protected ActiveEffect(Location location, int maxDuration) {
        this(location, maxDuration, false);
    }

    protected ActiveEffect(Location location, int maxDuration, boolean infinite) {
        this.location = location;
        this.maxDuration = maxDuration;
        this.infinite = infinite;
    }


    public UUID getId() { return id; }
    void setId(UUID id) { this.id = id; }


    public void setMaxDuration(int maxDuration) { this.maxDuration = maxDuration; }
    public int getMaxDuration() { return maxDuration; }

    public void setInfinite(boolean infinite) { this.infinite = infinite; }
    public boolean isInfinite() { return infinite; }

    public void setParams(float[] params) { this.params = params; }
    public float[] getParams() { return params; }

    public void setTimeMultiplier(Supplier<Double> timeMultiplier) { this.timeMultiplier = timeMultiplier; }


    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public void setPosition(Vec3 pos) { location.setPosition(pos); }
    public void setPosition(double x, double y, double z) { location.setPosition(new Vec3(x, y, z)); }

    public double getX() { return location.getPosition().x; }
    public double getY() { return location.getPosition().y; }
    public double getZ() { return location.getPosition().z; }


    public Vec3 getStartPos() {
        return new Vec3(params[EffectParams.START_X], params[EffectParams.START_Y], params[EffectParams.START_Z]);
    }

    public Vec3 getEndPos() {
        return new Vec3(params[EffectParams.END_X], params[EffectParams.END_Y], params[EffectParams.END_Z]);
    }

    protected Vec3 getDirection() { return getEndPos().subtract(getStartPos()).normalize(); }
    protected double getSegmentDistance() { return getStartPos().distanceTo(getEndPos()); }

    protected Vec3 getInterpolatedPosition(float progress) {
        Vec3 start = getStartPos(), end = getEndPos();
        return start.lerp(end, progress);
    }


    public void update(PoseStack poseStack, float partialTick) {
        render(poseStack, currentTick + partialTick);
    }

    public void tick() {
        currentTick += (float) Math.max(0.0, timeMultiplier.get());
    }

    protected abstract void render(PoseStack poseStack, float tick);

    public boolean isFinished() {
        if (cancelled) return true;
        if (infinite) return false;
        return currentTick >= maxDuration;
    }

    public void cancel() { cancelled = true; }

    protected float getProgress() {
        int loopLength = maxDuration > 0 ? maxDuration : 100;
        if (infinite) return (currentTick % loopLength) / loopLength;
        return currentTick / maxDuration;
    }

    public float getCurrentTick() { return currentTick; }
}