package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import de.jakob.lotm.util.data.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MarionetteThreadsEffect extends ActiveEffect {

    private static final float[] THREAD_COLOR = {0.02f, 0.02f, 0.02f};
    private static final float HALF_WIDTH = 0.02f; // thread thickness

    public MarionetteThreadsEffect(Location location, int duration, boolean infinite) {
        super(location, duration, infinite);
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float progress = tick / maxDuration;
        float fadeIn  = Mth.clamp(progress / 0.1f, 0f, 1f);
        float fadeOut = Mth.clamp(1f - (progress - 0.9f) / 0.1f, 0f, 1f);
        float intensity = fadeIn * fadeOut;
        if (intensity <= 0f) return;

        poseStack.pushPose();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.leash());
        Matrix4f matrix = poseStack.last().pose();

        Vector3f start = new Vector3f((float) getStartPos().x, (float) getStartPos().y, (float) getStartPos().z);
        Vector3f end   = new Vector3f((float) getEndPos().x, (float) getEndPos().y, (float) getEndPos().z);

        Vector3f dir = new Vector3f(end).sub(start).normalize();

        Vector3f worldUp = Math.abs(dir.y) > 0.99f ? new Vector3f(1, 0, 0) : new Vector3f(0, 1, 0);
        Vector3f perp1 = new Vector3f(dir).cross(worldUp).normalize().mul(HALF_WIDTH);
        Vector3f perp2 = new Vector3f(dir).cross(perp1).normalize().mul(HALF_WIDTH);

        int lightStart = LevelRenderer.getLightColor(mc.level, BlockPos.containing(getStartPos().x, getStartPos().y, getStartPos().z));
        int lightEnd   = LevelRenderer.getLightColor(mc.level, BlockPos.containing(getEndPos().x, getEndPos().y, getEndPos().z));

        addRibbon(consumer, matrix, start, end, perp1, intensity, lightStart, lightEnd);
        addRibbon(consumer, matrix, start, end, perp2, intensity, lightStart, lightEnd);

        poseStack.popPose();
    }

    private void addRibbon(VertexConsumer consumer, Matrix4f matrix, Vector3f start, Vector3f end,
                           Vector3f perp, float a, int lightStart, int lightEnd) {
        Vector3f s1 = new Vector3f(start).sub(perp);
        Vector3f s2 = new Vector3f(start).add(perp);
        Vector3f e1 = new Vector3f(end).sub(perp);
        Vector3f e2 = new Vector3f(end).add(perp);

        addVertex(consumer, matrix, s1, a, lightStart);
        addVertex(consumer, matrix, s2, a, lightStart);
        addVertex(consumer, matrix, e2, a, lightEnd);

        addVertex(consumer, matrix, s1, a, lightStart);
        addVertex(consumer, matrix, e2, a, lightEnd);
        addVertex(consumer, matrix, e1, a, lightEnd);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, Vector3f pos, float a, int light) {
        consumer.addVertex(matrix, pos.x, pos.y, pos.z)
                .setColor(THREAD_COLOR[0], THREAD_COLOR[1], THREAD_COLOR[2], a)
                .setUv2(LightTexture.block(light), LightTexture.sky(light));
    }
}