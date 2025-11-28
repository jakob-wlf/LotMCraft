package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class SefirahCastleEffect extends ActiveEffect {
    private final List<MysticalCube> cubes = new ArrayList<>();
    private final List<EnergyParticle> particles = new ArrayList<>();
    private final List<LightningArc> arcs = new ArrayList<>();
    private final RandomSource random = RandomSource.create();

    private static final int PARTICLE_COUNT = 40;
    private static final int ENERGY_PARTICLE_COUNT = 60;
    private static final int ARC_COUNT = 12;
    private static final float CUBE_SIZE = 0.08f;
    private static final float SPAWN_RADIUS = 3.5f;
    private static final float FLOAT_SPEED = 0.025f;

    public SefirahCastleEffect(double x, double y, double z) {
        super(x, y, z, 20 * 4); // 80 ticks = 4 seconds
        initializeEffect();
    }

    private void initializeEffect() {
        // Initialize cube particles
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float offsetX = (random.nextFloat() - 0.5f) * SPAWN_RADIUS;
            float offsetY = random.nextFloat() * 3.0f;
            float offsetZ = (random.nextFloat() - 0.5f) * SPAWN_RADIUS;

            float velocityX = (random.nextFloat() - 0.5f) * FLOAT_SPEED;
            float velocityY = random.nextFloat() * FLOAT_SPEED * 0.5f + FLOAT_SPEED * 0.3f;
            float velocityZ = (random.nextFloat() - 0.5f) * FLOAT_SPEED;

            float rotationSpeed = (random.nextFloat() - 0.5f) * 2.5f;
            float phaseOffset = random.nextFloat() * (float) Math.PI * 2;

            cubes.add(new MysticalCube(offsetX, offsetY, offsetZ, velocityX, velocityY, velocityZ, rotationSpeed, phaseOffset));
        }

        // Initialize energy particles orbiting the sphere
        for (int i = 0; i < ENERGY_PARTICLE_COUNT; i++) {
            float theta = random.nextFloat() * (float) Math.PI;
            float phi = random.nextFloat() * 2 * (float) Math.PI;
            float speed = 0.02f + random.nextFloat() * 0.03f;
            particles.add(new EnergyParticle(theta, phi, speed));
        }

        // Initialize lightning arcs
        for (int i = 0; i < ARC_COUNT; i++) {
            float startTheta = random.nextFloat() * (float) Math.PI;
            float startPhi = random.nextFloat() * 2 * (float) Math.PI;
            arcs.add(new LightningArc(startTheta, startPhi));
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

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // Render solid transparent sphere shell
        renderSolidSphere(poseStack, tick, progress);

        // Render energy particles orbiting the sphere
        renderEnergyParticles(poseStack, tick, progress);

        // Render lightning arcs across the sphere
        renderLightningArcs(poseStack, tick, progress);

        // Render cube particles
        renderCubeParticles(poseStack, tick, progress);

        // Render core glow
        renderCoreGlow(poseStack, tick, progress);

        poseStack.popPose();

        // Restore render state
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderSolidSphere(PoseStack poseStack, float tick, float progress) {
        Tesselator tesselator = Tesselator.getInstance();

        float radius = 1.5f + progress * 1.5f;
        float alpha = calculateAlpha(progress) * 0.25f; // Semi-transparent

        int segments = 20;

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        for (int lat = 0; lat < segments; lat++) {
            float theta1 = lat * (float) Math.PI / segments;
            float theta2 = (lat + 1) * (float) Math.PI / segments;

            for (int lon = 0; lon < segments; lon++) {
                float phi1 = lon * 2 * (float) Math.PI / segments;
                float phi2 = (lon + 1) * 2 * (float) Math.PI / segments;

                // Create quad vertices
                float[][] coords = {
                        {theta1, phi1}, {theta2, phi1}, {theta2, phi2}, {theta1, phi2}
                };

                for (float[] coord : coords) {
                    float x = radius * (float) (Math.sin(coord[0]) * Math.cos(coord[1]));
                    float y = radius * (float) Math.cos(coord[0]);
                    float z = radius * (float) (Math.sin(coord[0]) * Math.sin(coord[1]));

                    org.joml.Vector4f vec = new org.joml.Vector4f(x, y, z, 1.0f);
                    vec.mul(matrix);

                    float pulse = 0.9f + (float) Math.sin(tick * 0.1f + coord[0] * 2) * 0.1f;
                    buffer.addVertex(vec.x(), vec.y(), vec.z())
                            .setColor(1.0f, 1.0f, 1.0f, alpha * pulse);
                }
            }
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private void renderEnergyParticles(PoseStack poseStack, float tick, float progress) {
        Tesselator tesselator = Tesselator.getInstance();
        float alpha = calculateAlpha(progress);
        float radius = 1.5f + progress * 1.5f;

        for (EnergyParticle particle : particles) {
            particle.update(tick);

            float x = radius * (float) (Math.sin(particle.theta) * Math.cos(particle.phi));
            float y = radius * (float) Math.cos(particle.theta);
            float z = radius * (float) (Math.sin(particle.theta) * Math.sin(particle.phi));

            poseStack.pushPose();
            poseStack.translate(x, y, z);

            float size = 0.06f;
            renderGlowPoint(poseStack, tesselator, size, alpha * 0.9f);

            poseStack.popPose();
        }
    }

    private void renderLightningArcs(PoseStack poseStack, float tick, float progress) {
        Tesselator tesselator = Tesselator.getInstance();
        float alpha = calculateAlpha(progress);
        float radius = 1.5f + progress * 1.5f;

        for (LightningArc arc : arcs) {
            arc.update(tick);

            if (tick % 20 < 15) { // Arcs flicker on and off
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
                Matrix4f matrix = poseStack.last().pose();

                int points = 12;
                for (int i = 0; i <= points; i++) {
                    float t = (float) i / points;

                    // Interpolate between start and end points on sphere surface
                    float theta = arc.startTheta + (arc.endTheta - arc.startTheta) * t;
                    float phi = arc.startPhi + (arc.endPhi - arc.startPhi) * t;

                    // Add jagged variation
                    float variance = (random.nextFloat() - 0.5f) * 0.15f;
                    float currentRadius = radius + variance;

                    float x = currentRadius * (float) (Math.sin(theta) * Math.cos(phi));
                    float y = currentRadius * (float) Math.cos(theta);
                    float z = currentRadius * (float) (Math.sin(theta) * Math.sin(phi));

                    org.joml.Vector4f vec = new org.joml.Vector4f(x, y, z, 1.0f);
                    vec.mul(matrix);

                    float brightness = 0.9f + random.nextFloat() * 0.1f;
                    buffer.addVertex(vec.x(), vec.y(), vec.z())
                            .setColor(1.0f, 1.0f, 1.0f, alpha * 0.9f);
                }

                BufferUploader.drawWithShader(buffer.buildOrThrow());
            }
        }
    }

    private void renderCoreGlow(PoseStack poseStack, float tick, float progress) {
        Tesselator tesselator = Tesselator.getInstance();
        float alpha = calculateAlpha(progress) * 0.5f;

        float pulse = 0.8f + (float) Math.sin(tick * 0.15f) * 0.2f;
        float size = 0.3f * pulse;

        renderGlowPoint(poseStack, tesselator, size, alpha);
    }

    private void renderGlowPoint(PoseStack poseStack, Tesselator tesselator, float size, float alpha) {
        Matrix4f matrix = poseStack.last().pose();

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float[][] offsets = {
                {-size, -size, 0}, {size, -size, 0}, {size, size, 0}, {-size, size, 0}
        };

        for (float[] offset : offsets) {
            org.joml.Vector4f vec = new org.joml.Vector4f(offset[0], offset[1], offset[2], 1.0f);
            vec.mul(matrix);

            buffer.addVertex(vec.x(), vec.y(), vec.z())
                    .setColor(1.0f, 1.0f, 1.0f, alpha);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private void renderCubeParticles(PoseStack poseStack, float tick, float progress) {
        Tesselator tesselator = Tesselator.getInstance();
        float alpha = calculateAlpha(progress);

        for (MysticalCube cube : cubes) {
            poseStack.pushPose();

            cube.update(tick);

            poseStack.translate(cube.x, cube.y, cube.z);
            poseStack.mulPose(new org.joml.Quaternionf().rotationAxis((float) Math.toRadians(cube.rotation), 0, 1, 0));
            poseStack.mulPose(new org.joml.Quaternionf().rotationAxis((float) Math.toRadians(cube.rotation * 0.7f), 1, 0, 0));

            float pulse = 0.9f + (float) Math.sin((tick + cube.phaseOffset) * 0.1f) * 0.1f;
            poseStack.scale(pulse, pulse, pulse);

            renderCube(poseStack, tesselator, alpha * 0.8f);

            poseStack.popPose();
        }
    }

    private void renderCube(PoseStack poseStack, Tesselator tesselator, float alpha) {
        Matrix4f matrix = poseStack.last().pose();
        float size = CUBE_SIZE;

        float r = 1.0f;
        float g = 1.0f;
        float b = 1.0f;

        float[][] vertices = {
                {-size, -size, -size}, {size, -size, -size}, {size, size, -size}, {-size, size, -size},
                {-size, -size, size}, {size, -size, size}, {size, size, size}, {-size, size, size}
        };

        int[][] faces = {
                {0, 1, 2, 3}, {5, 4, 7, 6}, {4, 0, 3, 7},
                {1, 5, 6, 2}, {3, 2, 6, 7}, {4, 5, 1, 0}
        };

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int[] face : faces) {
            for (int i : face) {
                float[] v = vertices[i];
                org.joml.Vector4f vec = new org.joml.Vector4f(v[0], v[1], v[2], 1.0f);
                vec.mul(matrix);

                buffer.addVertex(vec.x(), vec.y(), vec.z())
                        .setColor(r, g, b, alpha);
            }
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private float calculateAlpha(float progress) {
        float fadeInDuration = 0.15f;
        float fadeOutDuration = 0.25f;

        if (progress < fadeInDuration) {
            return progress / fadeInDuration;
        } else if (progress > (1.0f - fadeOutDuration)) {
            return (1.0f - progress) / fadeOutDuration;
        }
        return 1.0f;
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
            x += vx + (float) Math.sin((tick + phaseOffset) * 0.05f) * 0.01f;
            y += vy + (float) Math.sin((tick + phaseOffset) * 0.08f) * 0.005f;
            z += vz + (float) Math.cos((tick + phaseOffset) * 0.05f) * 0.01f;
            rotation += rotationSpeed;
        }
    }

    private static class EnergyParticle {
        float theta;
        float phi;
        float speed;

        EnergyParticle(float theta, float phi, float speed) {
            this.theta = theta;
            this.phi = phi;
            this.speed = speed;
        }

        void update(float tick) {
            phi += speed;
            theta += (float) Math.sin(tick * 0.03f) * 0.01f;
        }
    }

    private static class LightningArc {
        float startTheta, startPhi;
        float endTheta, endPhi;
        int lifetime;

        LightningArc(float startTheta, float startPhi) {
            this.startTheta = startTheta;
            this.startPhi = startPhi;
            regenerateEndpoint();
            this.lifetime = 0;
        }

        void update(float tick) {
            lifetime++;
            if (lifetime > 30) { // Regenerate arc every 30 ticks
                regenerateEndpoint();
                lifetime = 0;
            }
        }

        void regenerateEndpoint() {
            // Pick a random point roughly opposite on the sphere
            this.endTheta = (float) (Math.PI - startTheta + (Math.random() - 0.5) * 0.5);
            this.endPhi = (float) (startPhi + Math.PI + (Math.random() - 0.5) * 0.5);
        }
    }
}