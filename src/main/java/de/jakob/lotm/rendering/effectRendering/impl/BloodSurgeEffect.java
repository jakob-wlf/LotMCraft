package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import de.jakob.lotm.util.data.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class BloodSurgeEffect extends ActiveEffect {

    private static final int   SEGMENTS    = 24;
    private static final float MAX_HEIGHT  = 28f;
    private static final float BASE_RADIUS = 1.6f;

    private static final float[] BRIGHT_R = {0.90f, 0.00f, 0.04f};
    private static final float[] MID_R    = {0.55f, 0.00f, 0.03f};
    private static final float[] DARK_R   = {0.20f, 0.00f, 0.01f};

    private final RandomSource random = RandomSource.create();
    private final List<BloodDroplet> droplets = new ArrayList<>();
    private final List<RuneRing>    runeRings = new ArrayList<>();

    public BloodSurgeEffect(Location location, int duration, boolean infinite) {
        super(location, duration, infinite);

        for (int i = 0; i < 220; i++) droplets.add(new BloodDroplet());
        for (int i = 0; i < 5;   i++) runeRings.add(new RuneRing(i));
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        if (Minecraft.getInstance().level == null) return;

        float progress  = getProgress();
        float intensity = progress < 0.12f
                ? progress / 0.12f
                : (progress > 0.75f ? 1f - (progress - 0.75f) / 0.25f : 1f);

        poseStack.pushPose();
        poseStack.translate(getX(), getY(), getZ());

        MultiBufferSource.BufferSource buf = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vc = buf.getBuffer(RenderType.lightning());
        Matrix4f mat = poseStack.last().pose();

        renderGroundCorona(vc, mat, tick, intensity, progress);
        renderBloodPillar(vc, mat, tick, intensity, progress);
        renderSpiralStreams(vc, mat, tick, intensity);
        renderRuneRings(vc, mat, tick, intensity);
        renderDroplets(vc, mat, tick, intensity);
        renderMistHalo(vc, mat, tick, intensity);
        renderApex(vc, mat, tick, intensity, progress);

        poseStack.popPose();
    }

    private void renderGroundCorona(VertexConsumer vc, Matrix4f mat,
                                    float tick, float intensity, float progress) {
        int numRings = 7;
        for (int r = 0; r < numRings; r++) {
            float delay  = r * 0.06f;
            float t      = Mth.clamp((progress - delay) / (1f - delay), 0f, 1f);
            float radius = (2f + r * 3.2f) * t;
            float alpha  = intensity * (1f - t * 0.7f) * (1f - (float) r / numRings * 0.5f);
            float y0     = 0.03f;

            float flicker = 1f + 0.12f * Mth.sin(tick * 0.25f + r * 1.3f);
            alpha *= flicker;

            float innerR = radius * 0.7f;
            float outerR = radius;
            for (int i = 0; i < SEGMENTS; i++) {
                float a1 = (float) (i     * Math.PI * 2 / SEGMENTS);
                float a2 = (float) ((i+1) * Math.PI * 2 / SEGMENTS);

                float ix1 = Mth.cos(a1)*innerR, iz1 = Mth.sin(a1)*innerR;
                float ox1 = Mth.cos(a1)*outerR, oz1 = Mth.sin(a1)*outerR;
                float ix2 = Mth.cos(a2)*innerR, iz2 = Mth.sin(a2)*innerR;
                float ox2 = Mth.cos(a2)*outerR, oz2 = Mth.sin(a2)*outerR;

                addV(vc, mat, ix1, y0, iz1, BRIGHT_R[0], BRIGHT_R[1], BRIGHT_R[2], alpha * 0.9f);
                addV(vc, mat, ix2, y0, iz2, BRIGHT_R[0], BRIGHT_R[1], BRIGHT_R[2], alpha * 0.9f);
                addV(vc, mat, ox2, y0, oz2, DARK_R[0],  DARK_R[1],  DARK_R[2],  alpha * 0.2f);
                addV(vc, mat, ox1, y0, oz1, DARK_R[0],  DARK_R[1],  DARK_R[2],  alpha * 0.2f);
            }
        }
    }

    private void renderBloodPillar(VertexConsumer vc, Matrix4f mat,
                                   float tick, float intensity, float progress) {
        int rings = 22;
        float height = MAX_HEIGHT * Mth.clamp(progress / 0.25f, 0f, 1f);
        float rotation = tick * 0.03f;

        for (int h = 0; h < rings; h++) {
            float t1 = (float) h       / rings;
            float t2 = (float) (h + 1) / rings;
            float y1 = t1 * height;
            float y2 = t2 * height;

            float bulge = 1f + 0.4f * Mth.sin(t1 * Mth.PI * 2.5f + tick * 0.07f);
            float r1 = BASE_RADIUS * (1f - t1 * 0.55f) * bulge;
            float r2 = BASE_RADIUS * (1f - t2 * 0.55f)
                       * (1f + 0.4f * Mth.sin(t2 * Mth.PI * 2.5f + tick * 0.07f));

            float cr = Mth.lerp(t1, BRIGHT_R[0], DARK_R[0]);
            float cg = Mth.lerp(t1, BRIGHT_R[1], DARK_R[1]);
            float cb = Mth.lerp(t1, BRIGHT_R[2], DARK_R[2]);
            float alpha = intensity * (1f - t1 * 0.6f) * 0.88f;

            for (int i = 0; i < SEGMENTS; i++) {
                float ang1 = (float) (i     * Math.PI * 2 / SEGMENTS) + rotation;
                float ang2 = (float) ((i+1) * Math.PI * 2 / SEGMENTS) + rotation;
                float noise = Mth.sin(ang1 * 4f + tick * 0.15f + h * 0.4f) * 0.12f;

                float x1a = Mth.cos(ang1) * (r1 + noise), z1a = Mth.sin(ang1) * (r1 + noise);
                float x2a = Mth.cos(ang2) * (r2 + noise), z2a = Mth.sin(ang2) * (r2 + noise);
                float x1b = Mth.cos(ang1) * r2,           z1b = Mth.sin(ang1) * r2;
                float x2b = Mth.cos(ang2) * r2,           z2b = Mth.sin(ang2) * r2;

                addV(vc, mat, x1a, y1, z1a, cr, cg, cb, alpha);
                addV(vc, mat, x2a, y1, z2a, cr, cg, cb, alpha);
                addV(vc, mat, x2b, y2, z2b, cr * 0.5f, cg, cb, alpha * 0.5f);
                addV(vc, mat, x1b, y2, z1b, cr * 0.5f, cg, cb, alpha * 0.5f);
            }
        }
    }

    private void renderSpiralStreams(VertexConsumer vc, Matrix4f mat,
                                     float tick, float intensity) {
        int numStreams = 6;
        int pointsPerStream = 40;
        float height = MAX_HEIGHT;

        for (int s = 0; s < numStreams; s++) {
            float phaseOffset = (float) s / numStreams * Mth.TWO_PI;
            float streamSpeed = 0.08f + s * 0.012f;
            float streamRadius = BASE_RADIUS * 1.4f + s * 0.35f;

            for (int p = 0; p < pointsPerStream - 1; p++) {
                float t1 = (float) p       / pointsPerStream;
                float t2 = (float) (p + 1) / pointsPerStream;

                float angle1 = phaseOffset + t1 * Mth.TWO_PI * 2.5f + tick * streamSpeed;
                float angle2 = phaseOffset + t2 * Mth.TWO_PI * 2.5f + tick * streamSpeed;

                float y1 = t1 * height;
                float y2 = t2 * height;
                float rx1 = Mth.cos(angle1) * streamRadius, rz1 = Mth.sin(angle1) * streamRadius;
                float rx2 = Mth.cos(angle2) * streamRadius, rz2 = Mth.sin(angle2) * streamRadius;

                float alpha = intensity * (1f - t1) * 0.55f;
                float width = 0.18f * (1f - t1 * 0.5f);

                float dx = rx2 - rx1, dz = rz2 - rz1;
                float len = Mth.sqrt(dx*dx + dz*dz);
                if (len < 1e-4f) continue;
                float nx = -dz / len * width, nz = dx / len * width;

                addV(vc, mat, rx1-nx, y1, rz1-nz, BRIGHT_R[0], BRIGHT_R[1], BRIGHT_R[2], alpha);
                addV(vc, mat, rx1+nx, y1, rz1+nz, BRIGHT_R[0], BRIGHT_R[1], BRIGHT_R[2], alpha);
                addV(vc, mat, rx2+nx, y2, rz2+nz, MID_R[0],    MID_R[1],    MID_R[2],    alpha * 0.4f);
                addV(vc, mat, rx2-nx, y2, rz2-nz, MID_R[0],    MID_R[1],    MID_R[2],    alpha * 0.4f);
            }
        }
    }

    private void renderRuneRings(VertexConsumer vc, Matrix4f mat,
                                  float tick, float intensity) {
        for (RuneRing ring : runeRings) {
            ring.render(vc, mat, tick, intensity);
        }
    }

    private class RuneRing {
        final float height;
        final float radius;
        final float rotSpeed;
        final float phaseOffset;

        RuneRing(int index) {
            height     = 2.5f + index * 4.8f;
            radius     = BASE_RADIUS * (2.8f - index * 0.3f);
            rotSpeed   = 0.04f + index * 0.008f;
            phaseOffset = index * Mth.TWO_PI / 5f;
        }

        void render(VertexConsumer vc, Matrix4f mat, float tick, float intensity) {
            int segs   = 32;
            int dashes = 12;
            float rotation = tick * rotSpeed + phaseOffset;
            float outerR = radius;
            float innerR = radius * 0.80f;
            float alpha  = intensity * 0.75f * (1f + 0.2f * Mth.sin(tick * 0.18f + phaseOffset));
            float y0     = height;

            for (int d = 0; d < dashes; d++) {
                float dashStart = (float) d / dashes + rotation / (Mth.TWO_PI);
                float dashEnd   = dashStart + 0.04f;

                float a1 = dashStart * Mth.TWO_PI;
                float a2 = dashEnd   * Mth.TWO_PI;

                float ix1 = Mth.cos(a1)*innerR, iz1 = Mth.sin(a1)*innerR;
                float ox1 = Mth.cos(a1)*outerR, oz1 = Mth.sin(a1)*outerR;
                float ix2 = Mth.cos(a2)*innerR, iz2 = Mth.sin(a2)*innerR;
                float ox2 = Mth.cos(a2)*outerR, oz2 = Mth.sin(a2)*outerR;

                addV(vc, mat, ix1, y0, iz1, BRIGHT_R[0], BRIGHT_R[1], BRIGHT_R[2], alpha);
                addV(vc, mat, ix2, y0, iz2, BRIGHT_R[0], BRIGHT_R[1], BRIGHT_R[2], alpha);
                addV(vc, mat, ox2, y0, oz2, MID_R[0],    MID_R[1],    MID_R[2],    alpha * 0.5f);
                addV(vc, mat, ox1, y0, oz1, MID_R[0],    MID_R[1],    MID_R[2],    alpha * 0.5f);
            }
        }
    }

    private void renderDroplets(VertexConsumer vc, Matrix4f mat,
                                 float tick, float intensity) {
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        for (BloodDroplet d : droplets) {
            d.update(tick);
            if (d.alpha <= 0.01f) continue;

            float ex = d.ex, ey = d.ey, ez = d.ez;
            Vec3 toCamera = new Vec3(
                    cam.x - (getX() + ex),
                    cam.y - (getY() + ey),
                    cam.z - (getZ() + ez)).normalize();

            Vec3 up    = new Vec3(0, 1, 0);
            Vec3 right = toCamera.cross(up).normalize().scale(d.size);
            Vec3 upv   = right.cross(toCamera).normalize().scale(d.size);

            float a = d.alpha * intensity;
            addV(vc, mat, (float)(ex-right.x-upv.x),(float)(ey-right.y-upv.y),(float)(ez-right.z-upv.z), BRIGHT_R[0],BRIGHT_R[1],BRIGHT_R[2], a);
            addV(vc, mat, (float)(ex-right.x+upv.x),(float)(ey-right.y+upv.y),(float)(ez-right.z+upv.z), BRIGHT_R[0],BRIGHT_R[1],BRIGHT_R[2], a);
            addV(vc, mat, (float)(ex+right.x+upv.x),(float)(ey+right.y+upv.y),(float)(ez+right.z+upv.z), DARK_R[0],  DARK_R[1],  DARK_R[2],   a*0.3f);
            addV(vc, mat, (float)(ex+right.x-upv.x),(float)(ey+right.y-upv.y),(float)(ez+right.z-upv.z), DARK_R[0],  DARK_R[1],  DARK_R[2],   a*0.3f);
        }
    }

    private class BloodDroplet {
        float ex, ey, ez;
        float alpha, size, speed, angle, dist, phaseOff, vy;

        BloodDroplet() { reset(true); }

        void reset(boolean randomH) {
            angle    = random.nextFloat() * Mth.TWO_PI;
            dist     = BASE_RADIUS * 0.6f + random.nextFloat() * BASE_RADIUS * 3f;
            ey       = randomH ? random.nextFloat() * MAX_HEIGHT : 0f;
            size     = 0.04f + random.nextFloat() * 0.10f;
            speed    = 0.04f + random.nextFloat() * 0.06f;
            vy       = speed * (0.8f + random.nextFloat() * 0.6f);
            phaseOff = random.nextFloat() * Mth.TWO_PI;
            alpha    = 0f;
        }

        void update(float tick) {
            ey    += vy;
            angle += 0.015f + (dist / (BASE_RADIUS * 4)) * 0.01f;
            ex = Mth.cos(angle) * (dist + Mth.sin(tick * 0.1f + phaseOff) * 0.3f);
            ez = Mth.sin(angle) * (dist + Mth.sin(tick * 0.1f + phaseOff) * 0.3f);
            float life = ey / MAX_HEIGHT;
            alpha = 1f - life;
            if (ey >= MAX_HEIGHT) reset(false);
        }
    }


    private void renderMistHalo(VertexConsumer vc, Matrix4f mat,
                                 float tick, float intensity) {
        int layers = 4;
        for (int l = 0; l < layers; l++) {
            float t       = (float) l / layers;
            float radius  = BASE_RADIUS * (3f + t * 6f);
            float yOffset = 0.05f + l * 0.15f;
            float pulse   = 1f + 0.08f * Mth.sin(tick * 0.13f + l * 0.7f);
            float alpha   = intensity * (0.25f - t * 0.20f) * pulse;

            for (int i = 0; i < SEGMENTS; i++) {
                float a1 = (float) (i     * Math.PI * 2 / SEGMENTS);
                float a2 = (float) ((i+1) * Math.PI * 2 / SEGMENTS);

                float ix1 = Mth.cos(a1)*radius*0.4f, iz1 = Mth.sin(a1)*radius*0.4f;
                float ox1 = Mth.cos(a1)*radius,      oz1 = Mth.sin(a1)*radius;
                float ix2 = Mth.cos(a2)*radius*0.4f, iz2 = Mth.sin(a2)*radius*0.4f;
                float ox2 = Mth.cos(a2)*radius,      oz2 = Mth.sin(a2)*radius;

                addV(vc, mat, ix1, yOffset, iz1, MID_R[0],  MID_R[1],  MID_R[2],  alpha);
                addV(vc, mat, ix2, yOffset, iz2, MID_R[0],  MID_R[1],  MID_R[2],  alpha);
                addV(vc, mat, ox2, yOffset, oz2, DARK_R[0], DARK_R[1], DARK_R[2], 0f);
                addV(vc, mat, ox1, yOffset, oz1, DARK_R[0], DARK_R[1], DARK_R[2], 0f);
            }
        }
    }

    private void renderApex(VertexConsumer vc, Matrix4f mat,
                             float tick, float intensity, float progress) {
        // Apex blooms after 30% of the effect
        float apexT = Mth.clamp((progress - 0.30f) / 0.25f, 0f, 1f);
        if (apexT <= 0f) return;

        float apexR = BASE_RADIUS * 1.1f * apexT * (1f + 0.2f * Mth.sin(tick * 0.22f));
        float apexY = MAX_HEIGHT * Mth.clamp(progress / 0.25f, 0f, 1f);
        float alpha = intensity * apexT * 0.85f;

        for (int i = 0; i < SEGMENTS; i++) {
            float a1 = (float) (i     * Math.PI * 2 / SEGMENTS);
            float a2 = (float) ((i+1) * Math.PI * 2 / SEGMENTS);

            float x1 = Mth.cos(a1)*apexR, z1 = Mth.sin(a1)*apexR;
            float x2 = Mth.cos(a2)*apexR, z2 = Mth.sin(a2)*apexR;

            addV(vc, mat, 0,  apexY,      0,  BRIGHT_R[0], BRIGHT_R[1], BRIGHT_R[2], alpha);
            addV(vc, mat, x1, apexY,      z1, MID_R[0],    MID_R[1],    MID_R[2],    alpha * 0.5f);
            addV(vc, mat, x2, apexY,      z2, MID_R[0],    MID_R[1],    MID_R[2],    alpha * 0.5f);
            addV(vc, mat, 0,  apexY+2.5f, 0,  DARK_R[0],   DARK_R[1],   DARK_R[2],   0f);
        }
    }


    private void addV(VertexConsumer vc, Matrix4f mat,
                      float px, float py, float pz,
                      float r,  float g,  float b,  float a) {
        vc.addVertex(mat, px, py, pz).setColor(r, g, b, a);
    }
}