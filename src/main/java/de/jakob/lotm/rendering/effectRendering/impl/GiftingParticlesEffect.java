package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class GiftingParticlesEffect extends ActiveEffect {
    private final List<MysticalCube> cubes = new ArrayList<>();
    private final RandomSource random = RandomSource.create();
    private static final int PARTICLE_COUNT = 25;
    private static final float CUBE_SIZE = 0.08f;
    private static final float SPAWN_RADIUS = 2.5f;
    private static final float FLOAT_SPEED = 0.02f;

    public GiftingParticlesEffect(double x, double y, double z) {
        super(x, y, z, 20 * 3);
        initializeCubes();
    }

    private void initializeCubes() {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float offsetX = (random.nextFloat() - 0.5f) * SPAWN_RADIUS;
            float offsetY = random.nextFloat() * 2.0f;
            float offsetZ = (random.nextFloat() - 0.5f) * SPAWN_RADIUS;

            float velocityX = (random.nextFloat() - 0.5f) * FLOAT_SPEED;
            float velocityY = random.nextFloat() * FLOAT_SPEED * 0.5f + FLOAT_SPEED * 0.3f;
            float velocityZ = (random.nextFloat() - 0.5f) * FLOAT_SPEED;

            float rotationSpeed = (random.nextFloat() - 0.5f) * 2.0f;
            float phaseOffset = random.nextFloat() * (float) Math.PI * 2;

            cubes.add(new MysticalCube(offsetX, offsetY, offsetZ, velocityX, velocityY, velocityZ, rotationSpeed, phaseOffset));
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        float progress = tick / maxDuration;

        // Setup render state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();

        for (MysticalCube cube : cubes) {
            poseStack.pushPose();

            // Calculate fade in/out
            float alpha = 1.0f;
            float fadeInDuration = 0.15f;
            float fadeOutDuration = 0.25f;

            if (progress < fadeInDuration) {
                alpha = progress / fadeInDuration;
            } else if (progress > (1.0f - fadeOutDuration)) {
                alpha = (1.0f - progress) / fadeOutDuration;
            }

            // Update cube position with floating motion
            cube.update(tick);

            // Translate to effect origin first
            poseStack.translate(x, y, z);

            // Then position the cube relative to origin
            poseStack.translate(cube.x, cube.y, cube.z);

            // Add mystical floating rotation
            poseStack.mulPose(new org.joml.Quaternionf().rotationAxis((float) Math.toRadians(cube.rotation), 0, 1, 0));
            poseStack.mulPose(new org.joml.Quaternionf().rotationAxis((float) Math.toRadians(cube.rotation * 0.7f), 1, 0, 0));

            // Add pulsing scale effect
            float pulse = 0.9f + (float) Math.sin((tick + cube.phaseOffset) * 0.1f) * 0.1f;
            poseStack.scale(pulse, pulse, pulse);

            // Render the cube with mystical white glow
            renderCube(poseStack, tesselator, alpha);

            poseStack.popPose();
        }

        // Restore render state
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderCube(PoseStack poseStack, Tesselator tesselator, float alpha) {
        Matrix4f matrix = poseStack.last().pose();
        float size = CUBE_SIZE;

        // White mystical color with glow
        float r = 0.22f;
        float g = 0.262f;
        float b = 0.82f;
        float a = alpha * 0.85f;

        // Define cube vertices
        float[][] vertices = {
                {-size, -size, -size}, {size, -size, -size}, {size, size, -size}, {-size, size, -size}, // Front
                {-size, -size, size}, {size, -size, size}, {size, size, size}, {-size, size, size}      // Back
        };

        // Define cube faces (as quads)
        int[][] faces = {
                {0, 1, 2, 3}, // Front
                {5, 4, 7, 6}, // Back
                {4, 0, 3, 7}, // Left
                {1, 5, 6, 2}, // Right
                {3, 2, 6, 7}, // Top
                {4, 5, 1, 0}  // Bottom
        };

        // Begin rendering
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int[] face : faces) {
            for (int i : face) {
                float[] v = vertices[i];
                // Transform vertex by matrix
                org.joml.Vector4f vec = new org.joml.Vector4f(v[0], v[1], v[2], 1.0f);
                vec.mul(matrix);

                buffer.addVertex(vec.x(), vec.y(), vec.z())
                        .setColor(r, g, b, a);
            }
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static class MysticalCube {
        float x, y, z;
        float vx, vy, vz;
        float rotation;
        float rotationSpeed;
        float phaseOffset;

        MysticalCube(float x, float y, float z, float vx, float vy, float vz, float rotationSpeed, float phaseOffset) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            this.rotation = 0;
            this.rotationSpeed = rotationSpeed;
            this.phaseOffset = phaseOffset;
        }

        void update(float tick) {
            // Floating motion with sine wave
            x += vx + (float) Math.sin((tick + phaseOffset) * 0.05f) * 0.01f;
            y += vy + (float) Math.sin((tick + phaseOffset) * 0.08f) * 0.005f;
            z += vz + (float) Math.cos((tick + phaseOffset) * 0.05f) * 0.01f;

            // Rotate the cube
            rotation += rotationSpeed;
        }
    }
}