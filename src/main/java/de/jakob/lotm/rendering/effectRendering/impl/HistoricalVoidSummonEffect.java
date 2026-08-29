package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import de.jakob.lotm.util.data.Location;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class HistoricalVoidSummonEffect extends ActiveEffect {
    private final List<VoidShard> shards = new ArrayList<>();
    private final List<FogLayer> fogLayers = new ArrayList<>();
    private final RandomSource random = RandomSource.create();

    private static final int SHARD_COUNT = 30;
    private static final int FOG_LAYER_COUNT = 14;
    private static final float SPAWN_RADIUS = 2.2f;
    private static final float SHARD_SIZE = 0.09f;

    public HistoricalVoidSummonEffect(Location location, int duration, boolean infinite) {
        super(location, duration, infinite);
        initializeShards();
        initializeFog();
    }

    private void initializeShards() {
        for (int i = 0; i < SHARD_COUNT; i++) {
            float angle = random.nextFloat() * (float) Math.PI * 2;
            float radius = SPAWN_RADIUS * (0.5f + random.nextFloat() * 0.5f);

            float offsetX = (float) Math.cos(angle) * radius;
            float offsetZ = (float) Math.sin(angle) * radius;
            float offsetY = random.nextFloat() * 1.6f - 0.2f;

            float spiralSpeed = 0.03f + random.nextFloat() * 0.04f;
            float inwardSpeed = 0.01f + random.nextFloat() * 0.015f;
            float verticalDrift = -0.01f - random.nextFloat() * 0.015f;

            float rotationSpeed = (random.nextFloat() - 0.5f) * 4.0f;
            float phaseOffset = random.nextFloat() * (float) Math.PI * 2;

            float brightness = random.nextFloat();
            float[] color = pickShardColor(brightness);

            shards.add(new VoidShard(offsetX, offsetY, offsetZ, angle, radius,
                    spiralSpeed, inwardSpeed, verticalDrift,
                    rotationSpeed, phaseOffset, color));
        }
    }

    private void initializeFog() {
        for (int i = 0; i < FOG_LAYER_COUNT; i++) {
            float angle = random.nextFloat() * (float) Math.PI * 2;
            float radius = random.nextFloat() * SPAWN_RADIUS * 0.9f;
            float offsetX = (float) Math.cos(angle) * radius;
            float offsetZ = (float) Math.sin(angle) * radius;
            float offsetY = random.nextFloat() * 0.6f;

            float driftSpeed = (random.nextFloat() - 0.5f) * 0.006f;
            float scale = 1.2f + random.nextFloat() * 1.8f;
            float phaseOffset = random.nextFloat() * (float) Math.PI * 2;

            fogLayers.add(new FogLayer(offsetX, offsetY, offsetZ, driftSpeed, scale, phaseOffset));
        }
    }

    private float[] pickShardColor(float brightness) {
        float shade = 0.75f + brightness * 0.25f;
        return new float[]{shade, shade, shade};
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        float progress = tick / maxDuration;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();

        renderFog(poseStack, tesselator, tick, progress);
        renderShards(poseStack, tesselator, tick, progress);

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderFog(PoseStack poseStack, Tesselator tesselator, float tick, float progress) {
        float fadeInDuration = 0.1f;
        float fadeOutDuration = 0.35f;
        float baseAlpha;

        if (progress < fadeInDuration) {
            baseAlpha = progress / fadeInDuration;
        } else if (progress > (1.0f - fadeOutDuration)) {
            baseAlpha = (1.0f - progress) / fadeOutDuration;
        } else {
            baseAlpha = 1.0f;
        }

        for (FogLayer fog : fogLayers) {
            poseStack.pushPose();

            fog.update(tick);

            poseStack.translate(getX(), getY(), getZ());
            poseStack.translate(fog.x, fog.y, fog.z);

            poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(tick * 0.6f + fog.phaseOffset * 20f), 0, 1, 0));

            float pulse = fog.scale * (0.85f + (float) Math.sin((tick + fog.phaseOffset) * 0.04f) * 0.15f);
            poseStack.scale(pulse, pulse * 0.4f, pulse);

            renderFogPlane(poseStack, tesselator, baseAlpha * 0.22f);

            poseStack.popPose();
        }
    }

    private void renderFogPlane(PoseStack poseStack, Tesselator tesselator, float alpha) {
        Matrix4f matrix = poseStack.last().pose();
        float size = 0.5f;

        float r = 0.85f, g = 0.85f, b = 0.88f;

        float[][] vertices = {
                {-size, 0, -size}, {size, 0, -size}, {size, 0, size}, {-size, 0, size}
        };

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (float[] v : vertices) {
            Vector4f vec = new Vector4f(v[0], v[1], v[2], 1.0f);
            vec.mul(matrix);
            buffer.addVertex(vec.x(), vec.y(), vec.z()).setColor(r, g, b, alpha);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private void renderShards(PoseStack poseStack, Tesselator tesselator, float tick, float progress) {
        float fadeInDuration = 0.12f;
        float fadeOutDuration = 0.2f;

        for (VoidShard shard : shards) {
            poseStack.pushPose();

            float alpha = 1.0f;
            if (progress < fadeInDuration) {
                alpha = progress / fadeInDuration;
            } else if (progress > (1.0f - fadeOutDuration)) {
                alpha = (1.0f - progress) / fadeOutDuration;
            }

            float collapse = progress > (1.0f - fadeOutDuration)
                    ? Math.max(0.0f, (1.0f - progress) / fadeOutDuration)
                    : 1.0f;

            shard.update(tick, progress);

            poseStack.translate(getX(), getY(), getZ());
            poseStack.translate(shard.x, shard.y, shard.z);

            poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(shard.rotation), 0, 1, 0));
            poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(shard.rotation * 0.6f), 1, 0, 1));

            float flicker = 0.85f + (float) Math.sin((tick + shard.phaseOffset) * 0.15f) * 0.15f;
            float scale = SHARD_SIZE * flicker * collapse;
            poseStack.scale(scale / SHARD_SIZE, scale / SHARD_SIZE, scale / SHARD_SIZE);

            renderShard(poseStack, tesselator, alpha, shard.color);

            poseStack.popPose();
        }
    }

    private void renderShard(PoseStack poseStack, Tesselator tesselator, float alpha, float[] color) {
        Matrix4f matrix = poseStack.last().pose();
        float size = SHARD_SIZE;

        float r = color[0];
        float g = color[1];
        float b = color[2];
        float a = alpha * 0.75f;

        float[][] vertices = {
                {-size, -size, -size}, {size, -size, -size}, {size, size, -size}, {-size, size, -size},
                {-size, -size, size}, {size, -size, size}, {size, size, size}, {-size, size, size}
        };

        int[][] faces = {
                {0, 1, 2, 3},
                {5, 4, 7, 6},
                {4, 0, 3, 7},
                {1, 5, 6, 2},
                {3, 2, 6, 7},
                {4, 5, 1, 0}
        };

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int[] face : faces) {
            for (int i : face) {
                float[] v = vertices[i];
                Vector4f vec = new Vector4f(v[0], v[1], v[2], 1.0f);
                vec.mul(matrix);
                buffer.addVertex(vec.x(), vec.y(), vec.z()).setColor(r, g, b, a);
            }
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static class VoidShard {
        float x, y, z;
        float angle, radius;
        float spiralSpeed, inwardSpeed, verticalDrift;
        float rotation, rotationSpeed;
        float phaseOffset;
        float[] color;

        VoidShard(float x, float y, float z, float angle, float radius,
                  float spiralSpeed, float inwardSpeed, float verticalDrift,
                  float rotationSpeed, float phaseOffset, float[] color) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.angle = angle;
            this.radius = radius;
            this.spiralSpeed = spiralSpeed;
            this.inwardSpeed = inwardSpeed;
            this.verticalDrift = verticalDrift;
            this.rotation = 0;
            this.rotationSpeed = rotationSpeed;
            this.phaseOffset = phaseOffset;
            this.color = color;
        }

        void update(float tick, float progress) {
            angle += spiralSpeed;
            radius = Math.max(0.0f, radius - inwardSpeed * (0.5f + progress * 1.5f));

            x = (float) Math.cos(angle) * radius + (float) Math.sin((tick + phaseOffset) * 0.05f) * 0.008f;
            z = (float) Math.sin(angle) * radius + (float) Math.cos((tick + phaseOffset) * 0.05f) * 0.008f;
            y += verticalDrift * (0.5f + progress);

            rotation += rotationSpeed;
        }
    }

    private static class FogLayer {
        float x, y, z;
        float driftSpeed;
        float scale;
        float phaseOffset;

        FogLayer(float x, float y, float z, float driftSpeed, float scale, float phaseOffset) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.driftSpeed = driftSpeed;
            this.scale = scale;
            this.phaseOffset = phaseOffset;
        }

        void update(float tick) {
            x += (float) Math.sin((tick + phaseOffset) * 0.03f) * driftSpeed;
            z += (float) Math.cos((tick + phaseOffset) * 0.03f) * driftSpeed;
        }
    }
}