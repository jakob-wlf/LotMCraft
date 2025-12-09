package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveDirectionalEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class FateSiphoningEffect extends ActiveDirectionalEffect {

    private final RandomSource random = RandomSource.create();
    private final List<FateThread> fateThreads = new ArrayList<>();
    private final List<FateParticle> fateParticles = new ArrayList<>();
    private final List<SiphonOrb> siphonOrbs = new ArrayList<>();
    private float intensity;

    // Color schemes
    private static final float[] GOLD_COLOR = {1f, 0.843f, 0f}; // Golden
    private static final float[] PURPLE_COLOR = {0.6f, 0.2f, 0.8f}; // Purple
    private static final float[] DARK_PURPLE_COLOR = {0.4f, 0.1f, 0.6f}; // Dark purple

    public FateSiphoningEffect(double startX, double startY, double startZ,
                               double endX, double endY, double endZ, int duration) {
        super(startX, startY, startZ, endX, endY, endZ, duration);

        // Create multiple fate threads that spiral from target to caster
        for (int i = 0; i < 8; i++) {
            fateThreads.add(new FateThread(i));
        }

        // Create fate particles that flow along the threads
        for (int i = 0; i < 150; i++) {
            fateParticles.add(new FateParticle());
        }

        // Create glowing orbs that travel from target to caster
        for (int i = 0; i < 12; i++) {
            siphonOrbs.add(new SiphonOrb(i));
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float progress = tick / maxDuration;
        intensity = Mth.clamp(1f - (float) Math.pow(progress, 0.5), 0f, 1f);
        intensity *= (0.9f + 0.1f * Mth.sin(tick * 0.4f));

        poseStack.pushPose();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Render target aura (victim losing fate)
        renderTargetAura(poseStack, bufferSource, progress);

        // Render fate threads
        for (FateThread thread : fateThreads) {
            thread.update(progress, tick);
            renderFateThread(consumer, matrix, thread, progress);
        }

        // Render fate particles flowing along threads
        for (FateParticle particle : fateParticles) {
            particle.update(progress, tick);
            if (particle.alpha > 0f) {
                renderBillboardQuad(consumer, matrix, 
                    particle.x, particle.y, particle.z,
                    particle.size, particle.r, particle.g, particle.b, particle.alpha);
            }
        }

        // Render siphon orbs
        for (SiphonOrb orb : siphonOrbs) {
            orb.update(progress, tick);
            if (orb.alpha > 0f) {
                renderSiphonOrb(consumer, matrix, orb);
            }
        }

        // Render caster absorption effect
        renderCasterAbsorption(poseStack, bufferSource, progress);

        poseStack.popPose();
    }

    private void renderTargetAura(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Shrinking aura around target as fate is drained
        float auraRadius = 2.5f * (1f - progress * 0.7f);
        float auraIntensity = intensity * (1f - progress * 0.5f);
        int segments = 32;

        for (int layer = 0; layer < 2; layer++) {
            float layerRadius = auraRadius + layer * 0.5f;
            float layerAlpha = auraIntensity * (0.4f - layer * 0.15f);

            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

                float wave = Mth.sin(angle1 * 2f + currentTick * 0.2f) * 0.2f;

                float x1 = Mth.cos(angle1) * layerRadius;
                float z1 = Mth.sin(angle1) * layerRadius;
                float x2 = Mth.cos(angle2) * layerRadius;
                float z2 = Mth.sin(angle2) * layerRadius;

                // Purple aura at target
                addVertex(consumer, matrix, 
                    (float) endX, (float) (endY + 1f), (float) endZ,
                    PURPLE_COLOR[0], PURPLE_COLOR[1], PURPLE_COLOR[2], layerAlpha * 0.5f);
                addVertex(consumer, matrix,
                    (float) (endX + x1), (float) (endY + 0.2f + wave), (float) (endZ + z1),
                    DARK_PURPLE_COLOR[0], DARK_PURPLE_COLOR[1], DARK_PURPLE_COLOR[2], layerAlpha);
                addVertex(consumer, matrix,
                    (float) (endX + x2), (float) (endY + 0.2f + wave), (float) (endZ + z2),
                    DARK_PURPLE_COLOR[0], DARK_PURPLE_COLOR[1], DARK_PURPLE_COLOR[2], layerAlpha);
            }
        }
    }

    private void renderFateThread(VertexConsumer consumer, Matrix4f matrix, FateThread thread, float progress) {
        if (thread.alpha <= 0f) return;

        int segments = thread.segments.size() - 1;
        for (int i = 0; i < segments; i++) {
            Vec3 pos1 = thread.segments.get(i);
            Vec3 pos2 = thread.segments.get(i + 1);

            Vec3 dir = pos2.subtract(pos1).normalize();
            Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(thread.width);

            // Fade colors along the thread
            float t = i / (float) segments;
            float[] color = interpolateColor(PURPLE_COLOR, GOLD_COLOR, t);

            float alpha = thread.alpha * (1f - t * 0.3f);

            addVertex(consumer, matrix,
                (float) (pos1.x - perp.x), (float) (pos1.y - perp.y), (float) (pos1.z - perp.z),
                color[0], color[1], color[2], alpha);
            addVertex(consumer, matrix,
                (float) (pos1.x + perp.x), (float) (pos1.y + perp.y), (float) (pos1.z + perp.z),
                color[0], color[1], color[2], alpha);
            addVertex(consumer, matrix,
                (float) (pos2.x + perp.x), (float) (pos2.y + perp.y), (float) (pos2.z + perp.z),
                color[0], color[1], color[2], alpha * 0.8f);
            addVertex(consumer, matrix,
                (float) (pos2.x - perp.x), (float) (pos2.y - perp.y), (float) (pos2.z - perp.z),
                color[0], color[1], color[2], alpha * 0.8f);
        }
    }

    private void renderSiphonOrb(VertexConsumer consumer, Matrix4f matrix, SiphonOrb orb) {
        // Render glowing orb with layered effect
        for (int layer = 0; layer < 2; layer++) {
            float size = orb.size * (1f + layer * 0.5f);
            float alpha = orb.alpha * (0.8f - layer * 0.3f);

            renderBillboardQuad(consumer, matrix,
                orb.x, orb.y, orb.z, size,
                orb.r, orb.g, orb.b, alpha);
        }
    }

    private void renderCasterAbsorption(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Growing golden aura around caster as they absorb fate
        float auraRadius = 1.5f * progress;
        float auraIntensity = intensity * progress;
        int segments = 32;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1 = Mth.cos(angle1) * auraRadius;
            float z1 = Mth.sin(angle1) * auraRadius;
            float x2 = Mth.cos(angle2) * auraRadius;
            float z2 = Mth.sin(angle2) * auraRadius;

            float pulse = Mth.sin(currentTick * 0.3f + angle1) * 0.3f;

            // Golden aura at caster
            addVertex(consumer, matrix,
                (float) startX, (float) (startY + 1f + pulse), (float) startZ,
                GOLD_COLOR[0], GOLD_COLOR[1], GOLD_COLOR[2], auraIntensity * 0.6f);
            addVertex(consumer, matrix,
                (float) (startX + x1), (float) (startY + 0.1f), (float) (startZ + z1),
                GOLD_COLOR[0], GOLD_COLOR[1], GOLD_COLOR[2], auraIntensity * 0.4f);
            addVertex(consumer, matrix,
                (float) (startX + x2), (float) (startY + 0.1f), (float) (startZ + z2),
                GOLD_COLOR[0], GOLD_COLOR[1], GOLD_COLOR[2], auraIntensity * 0.4f);
        }
    }

    private float[] interpolateColor(float[] color1, float[] color2, float t) {
        return new float[]{
            color1[0] + (color2[0] - color1[0]) * t,
            color1[1] + (color2[1] - color1[1]) * t,
            color1[2] + (color2[2] - color1[2]) * t
        };
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                          float r, float g, float b, float a) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a);
    }

    private void renderBillboardQuad(VertexConsumer consumer, Matrix4f matrix,
                                     float x, float y, float z, float size,
                                     float r, float g, float b, float a) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        Vec3 toCamera = new Vec3(
            cameraPos.x - x,
            cameraPos.y - y,
            cameraPos.z - z
        ).normalize();

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = toCamera.cross(up).normalize().scale(size);
        up = right.cross(toCamera).normalize().scale(size);

        addVertex(consumer, matrix,
            (float) (x - right.x - up.x), (float) (y - right.y - up.y), (float) (z - right.z - up.z),
            r, g, b, a);
        addVertex(consumer, matrix,
            (float) (x - right.x + up.x), (float) (y - right.y + up.y), (float) (z - right.z + up.z),
            r, g, b, a);
        addVertex(consumer, matrix,
            (float) (x + right.x + up.x), (float) (y + right.y + up.y), (float) (z + right.z + up.z),
            r, g, b, a);
        addVertex(consumer, matrix,
            (float) (x + right.x - up.x), (float) (y + right.y - up.y), (float) (z + right.z - up.z),
            r, g, b, a);
    }

    private class FateThread {
        List<Vec3> segments = new ArrayList<>();
        float width;
        float alpha;
        float spiralSpeed;
        float spiralRadius;

        FateThread(int index) {
            this.spiralSpeed = 0.3f + random.nextFloat() * 0.2f;
            this.spiralRadius = 0.3f + random.nextFloat() * 0.5f;
            this.width = 0.08f + random.nextFloat() * 0.05f;
            generateSegments(index);
        }

        void generateSegments(int index) {
            int segmentCount = 50;
            float angleOffset = (index / 8f) * Mth.TWO_PI;

            for (int i = 0; i < segmentCount; i++) {
                float t = i / (float) segmentCount;
                
                // Start from target (end) and go to caster (start)
                Vec3 basePos = new Vec3(
                    endX + (startX - endX) * t,
                    endY + (startY - endY) * t,
                    endZ + (startZ - endZ) * t
                );

                // Add spiral motion
                float spiralAngle = angleOffset + t * Mth.TWO_PI * 3f;
                Vec3 perpendicular1 = new Vec3(-direction.z, 0, direction.x).normalize();
                Vec3 perpendicular2 = direction.cross(perpendicular1).normalize();

                float spiralX = Mth.cos(spiralAngle) * spiralRadius * (1f - t * 0.5f);
                float spiralY = Mth.sin(spiralAngle) * spiralRadius * (1f - t * 0.5f);

                Vec3 spiralPos = basePos.add(
                    perpendicular1.scale(spiralX).add(perpendicular2.scale(spiralY))
                );

                segments.add(spiralPos);
            }
        }

        void update(float progress, float tick) {
            // Threads appear and fade
            if (progress < 0.15f) {
                this.alpha = progress / 0.15f;
            } else if (progress > 0.85f) {
                this.alpha = 1f - ((progress - 0.85f) / 0.15f);
            } else {
                this.alpha = 1f;
            }

            this.alpha *= intensity;
            this.alpha *= (0.85f + 0.15f * Mth.sin(tick * 0.3f + spiralSpeed * 10f));
        }
    }

    private class FateParticle {
        float x, y, z;
        float r, g, b;
        float size;
        float alpha;
        float pathProgress;
        float speed;
        int threadIndex;

        FateParticle() {
            respawn();
        }

        void respawn() {
            this.pathProgress = random.nextFloat() * 0.3f;
            this.speed = 0.008f + random.nextFloat() * 0.012f;
            this.threadIndex = random.nextInt(fateThreads.size());
            this.size = 0.06f + random.nextFloat() * 0.08f;

            // Mix of purple and gold
            if (random.nextFloat() < 0.6f) {
                this.r = PURPLE_COLOR[0];
                this.g = PURPLE_COLOR[1];
                this.b = PURPLE_COLOR[2];
            } else {
                this.r = GOLD_COLOR[0];
                this.g = GOLD_COLOR[1];
                this.b = GOLD_COLOR[2];
            }

            updatePosition();
        }

        void update(float progress, float tick) {
            this.pathProgress += speed;

            if (this.pathProgress >= 1f) {
                respawn();
            }

            updatePosition();

            // Fade in and out
            float fadeDist = 0.1f;
            if (pathProgress < fadeDist) {
                this.alpha = pathProgress / fadeDist;
            } else if (pathProgress > 1f - fadeDist) {
                this.alpha = (1f - pathProgress) / fadeDist;
            } else {
                this.alpha = 1f;
            }

            this.alpha *= intensity * 0.7f;
            this.alpha *= (0.8f + 0.2f * Mth.sin(tick * 0.5f));
        }

        void updatePosition() {
            if (threadIndex >= fateThreads.size()) return;
            
            FateThread thread = fateThreads.get(threadIndex);
            int segmentIndex = (int) (pathProgress * (thread.segments.size() - 1));
            segmentIndex = Mth.clamp(segmentIndex, 0, thread.segments.size() - 1);

            Vec3 pos = thread.segments.get(segmentIndex);
            this.x = (float) pos.x;
            this.y = (float) pos.y;
            this.z = (float) pos.z;
        }
    }

    private class SiphonOrb {
        float x, y, z;
        float r, g, b;
        float size;
        float alpha;
        float travelProgress;
        float speed;
        float delay;
        float bobOffset;

        SiphonOrb(int index) {
            this.delay = (index / 12f) * 0.6f;
            this.speed = 0.012f + random.nextFloat() * 0.008f;
            this.size = 0.15f + random.nextFloat() * 0.1f;
            this.bobOffset = random.nextFloat() * Mth.TWO_PI;

            // Orbs are mostly golden
            this.r = GOLD_COLOR[0];
            this.g = GOLD_COLOR[1];
            this.b = GOLD_COLOR[2];
        }

        void update(float progress, float tick) {
            float adjustedProgress = Mth.clamp((progress - delay) / (1f - delay), 0f, 1f);

            if (adjustedProgress <= 0f) {
                this.alpha = 0f;
                return;
            }

            this.travelProgress += speed;
            if (this.travelProgress > 1f) {
                this.travelProgress = 0f;
            }

            // Travel from target to caster
            float t = travelProgress;
            float bob = Mth.sin(tick * 0.2f + bobOffset) * 0.3f;

            this.x = (float) (endX + (startX - endX) * t);
            this.y = (float) (endY + (startY - endY) * t + bob);
            this.z = (float) (endZ + (startZ - endZ) * t);

            // Fade
            if (t < 0.15f) {
                this.alpha = t / 0.15f;
            } else if (t > 0.85f) {
                this.alpha = (1f - t) / 0.15f;
            } else {
                this.alpha = 1f;
            }

            this.alpha *= intensity * 0.9f;
        }
    }
}