package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import de.jakob.lotm.util.data.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ArtifactExplosionEffect extends ActiveEffect {


    private static final int   FIRE_COUNT         = 60;
    
    private static final int   INNER_FIRE_COUNT   = 32;
    private static final int   SMOKE_COUNT        = 16;
    private static final int   DEBRIS_COUNT       = 21;
    private static final float GRAVITY            = 0.016f;

    private final RandomSource rng = RandomSource.create();


    private final float[] fVx     = new float[FIRE_COUNT];
    private final float[] fVy     = new float[FIRE_COUNT];
    private final float[] fVz     = new float[FIRE_COUNT];
    private final float[] fSize   = new float[FIRE_COUNT];
    private final float[] fMaxAge = new float[FIRE_COUNT];
    private final float[] fGreen  = new float[FIRE_COUNT];


    private final float[] iVx     = new float[INNER_FIRE_COUNT];
    private final float[] iVy     = new float[INNER_FIRE_COUNT];
    private final float[] iVz     = new float[INNER_FIRE_COUNT];
    private final float[] iSize   = new float[INNER_FIRE_COUNT];
    private final float[] iMaxAge = new float[INNER_FIRE_COUNT];


    private final float[] sPx    = new float[SMOKE_COUNT];
    private final float[] sPy    = new float[SMOKE_COUNT];
    private final float[] sPz    = new float[SMOKE_COUNT];
    
    private final float[] sVx    = new float[SMOKE_COUNT];
    private final float[] sVy    = new float[SMOKE_COUNT];
    private final float[] sVz    = new float[SMOKE_COUNT];
    
    private final float[] sDrag  = new float[SMOKE_COUNT];
    private final float[] sSize  = new float[SMOKE_COUNT];
    private final float[] sDelay = new float[SMOKE_COUNT];
    private final float[] sLife  = new float[SMOKE_COUNT];
    private final float[] sGray  = new float[SMOKE_COUNT];


    private final float[]   dVx     = new float[DEBRIS_COUNT];
    private final float[]   dVy     = new float[DEBRIS_COUNT];
    private final float[]   dVz     = new float[DEBRIS_COUNT];
    private final float[]   dSize   = new float[DEBRIS_COUNT];
    private final float[]   dMaxAge = new float[DEBRIS_COUNT];
    private final boolean[] dHot    = new boolean[DEBRIS_COUNT];



    public ArtifactExplosionEffect(Location location, int duration, boolean infinite) {
        super(location, duration, infinite);
        bakeParticles();
    }

    private void bakeParticles() {


        for (int i = 0; i < FIRE_COUNT; i++) {
            float theta = rng.nextFloat() * Mth.TWO_PI;
            float phi   = rng.nextFloat() * Mth.PI;
            float sphi  = Mth.sin(phi);

            float speed = 0.10f + rng.nextFloat() * 0.19f;
            fVx[i]     = sphi * Mth.cos(theta) * speed;

            fVy[i]     = Math.abs(Mth.cos(phi)) * speed * 0.6f + 0.08f;
            fVz[i]     = sphi * Mth.sin(theta) * speed;
            fSize[i]   = 0.28f + rng.nextFloat() * 0.52f;

            fMaxAge[i] = 8f + rng.nextFloat() * 12f;
            fGreen[i]  = 0.30f + rng.nextFloat() * 0.50f;
        }


        for (int i = 0; i < INNER_FIRE_COUNT; i++) {
            float theta = rng.nextFloat() * Mth.TWO_PI;
            float phi   = rng.nextFloat() * Mth.PI;
            float sphi  = Mth.sin(phi);
            float speed = 0.225f + rng.nextFloat() * 0.275f;
            iVx[i]     = sphi * Mth.cos(theta) * speed;
            iVy[i]     = Math.abs(Mth.cos(phi)) * speed * 0.5f + 0.12f;
            iVz[i]     = sphi * Mth.sin(theta) * speed;
            iSize[i]   = 0.10f + rng.nextFloat() * 0.18f;
            iMaxAge[i] = 5f + rng.nextFloat() * 7f;
        }








        for (int i = 0; i < SMOKE_COUNT; i++) {
            float a    = rng.nextFloat() * Mth.TWO_PI;
            float dist = 0.25f + rng.nextFloat() * 0.9f;
            sPx[i]    = Mth.cos(a) * dist;
            sPy[i]    = 0.1f + rng.nextFloat() * 0.6f;
            sPz[i]    = Mth.sin(a) * dist;


            float lateral = 0.02f + rng.nextFloat() * 0.035f;
            sVx[i]    = Mth.cos(a) * lateral;
            sVy[i]    = 0.06f + rng.nextFloat() * 0.07f;
            sVz[i]    = Mth.sin(a) * lateral;

            sDrag[i]  = 0.030f + rng.nextFloat() * 0.020f;

            sSize[i]  = 0.7f  + rng.nextFloat();

            sDelay[i] = 5f + rng.nextFloat() * 23f;
            sLife[i]  = 28f + rng.nextFloat() * 22f;
            sGray[i]  = 0.10f + rng.nextFloat() * 0.22f;
        }


        for (int i = 0; i < DEBRIS_COUNT; i++) {
            float theta = rng.nextFloat() * Mth.TWO_PI;
            float phi   = (0.05f + rng.nextFloat() * 0.85f) * Mth.PI;
            float sphi  = Mth.sin(phi);
            float speed = 0.6f + rng.nextFloat() * 0.16f;
            dVx[i]     = sphi * Mth.cos(theta) * speed;
            dVy[i]     = Mth.cos(phi) * speed + 0.14f;
            dVz[i]     = sphi * Mth.sin(theta) * speed;
            dSize[i]   = 0.05f + rng.nextFloat() * 0.11f;
            dMaxAge[i] = 16f   + rng.nextFloat() * 34f;
            dHot[i]    = rng.nextFloat() < 0.50f;
        }
    }





    @Override
    protected void render(PoseStack poseStack, float partialTick) {
        if (Minecraft.getInstance().level == null) return;

        float age = currentTick + partialTick;

        Quaternionf camRot = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        Vector3f right = new Vector3f(1f, 0f, 0f).rotate(camRot);
        Vector3f up    = new Vector3f(0f, 1f, 0f).rotate(camRot);

        poseStack.pushPose();
        poseStack.translate(location.getX(), location.getY(), location.getZ());
        Matrix4f m = poseStack.last().pose();

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();


        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        renderFlash(m, age, right, up);
        renderInnerFire(m, age, right, up);
        renderFire(m, age, right, up);
        renderShockwave(m, age);


        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        renderSmoke(m, age, right, up);
        renderDebris(m, age, right, up);

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }





    
    private void renderFlash(Matrix4f m, float age, Vector3f right, Vector3f up) {
        final float MAX = 8f;
        if (age >= MAX) return;

        float t     = age / MAX;
        float alpha = (1f - t) * (1f - t) * 0.98f;
        float size  = 0.75f + t * 6.5f;
        singleQuad(m, 0f, 0f, 0f, size, right, up, 0.8f, 0.0f, 1.0f, alpha);
    }

    
    private void renderShockwave(Matrix4f m, float age) {
        final float MAX = 27f;
        if (age >= MAX) return;

        float t     = age / MAX;
        float inner = t * 10f;
        float outer = inner + 1.75f;
        float alpha = (1f - t) * (1f - t) * 0.70f;
        int   segs  = 40;

        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= segs; i++) {
            float a = i * Mth.TWO_PI / segs;
            float c = Mth.cos(a), s = Mth.sin(a);
            buf.addVertex(m, c * inner, 0.05f, s * inner).setColor(0.9f, 0.0f, 1.0f, alpha);
            buf.addVertex(m, c * outer, 0.05f, s * outer).setColor(0.4f, 0.0f, 0.5f, 0f);
        }

        BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    
    private void renderFire(Matrix4f m, float age, Vector3f right, Vector3f up) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int drawn = 0;

        for (int i = 0; i < FIRE_COUNT; i++) {
            if (age > fMaxAge[i]) continue;
            float t     = age / fMaxAge[i];
            float alpha = (float) Math.sin(t * Math.PI) * 0.95f;
            if (alpha < 0.005f) continue;

            float px = fVx[i] * age;
            float py = fVy[i] * age;
            float pz = fVz[i] * age;

            float size  = fSize[i]  * (1f + t * 0.5f);

            quad(buf, m, px, py, pz, size, right, up, 0.8f, 0f, 1.0f, alpha);
            drawn++;
        }

        if (drawn > 0) BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    
    private void renderInnerFire(Matrix4f m, float age, Vector3f right, Vector3f up) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int drawn = 0;

        for (int i = 0; i < INNER_FIRE_COUNT; i++) {
            if (age > iMaxAge[i]) continue;
            float t     = age / iMaxAge[i];
            float alpha = (float) Math.sin(t * Math.PI) * 1.0f;
            if (alpha < 0.005f) continue;

            float px = iVx[i] * age;
            float py = iVy[i] * age;
            float pz = iVz[i] * age;


            float g = 0.6f + (1f - t) * 0.4f;
            float b = (1f - t) * 0.5f;

            quad(buf, m, px, py, pz, iSize[i], right, up, 1f, g, b, alpha);
            drawn++;
        }

        if (drawn > 0) BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    
    private void renderSmoke(Matrix4f m, float age, Vector3f right, Vector3f up) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int drawn = 0;

        for (int i = 0; i < SMOKE_COUNT; i++) {
            float localAge = age - sDelay[i];
            if (localAge <= 0f || localAge > sLife[i]) continue;
            float t = localAge / sLife[i];


            float attenuation = 1f - sDrag[i] * localAge * 0.5f;
            if (attenuation < 0.05f) attenuation = 0.05f;

            float px = sPx[i] + sVx[i] * localAge * attenuation;
            float py = sPy[i] + sVy[i] * localAge * attenuation;
            float pz = sPz[i] + sVz[i] * localAge * attenuation;


            float alpha = Mth.clamp(localAge * 0.20f, 0f, 1f) * (1f - t) * 0.55f;
            if (alpha < 0.005f) continue;


            float size = sSize[i] * 0.5f * (1f + t * 2.0f);
            float g    = sGray[i];

            float r = g * 0.55f;
            float g2 = g * 0.1f;
            float b = g * 0.85f;

            quad(buf, m, px, py, pz, size, right, up, r, g2, b, alpha);
            drawn++;
        }

        if (drawn > 0) BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    
    private void renderDebris(Matrix4f m, float age, Vector3f right, Vector3f up) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int drawn = 0;

        for (int i = 0; i < DEBRIS_COUNT; i++) {
            if (age > dMaxAge[i]) continue;
            float t = age / dMaxAge[i];

            float px = dVx[i] * age;
            float py = dVy[i] * age - 0.5f * GRAVITY * age * age;
            float pz = dVz[i] * age;
            if (py < -0.5f) continue;

            float alpha = (float) Math.sin(t * Math.PI) * 0.88f;
            if (alpha < 0.005f) continue;

            float r, g, b;
            if (dHot[i] && t < 0.55f) {
                float heat = 1f - (t / 0.55f);
                r = 1f;
                g = 0.25f + heat * 0.45f;
                b = 0f;
            } else {
                r = 0.22f; g = 0.20f; b = 0.18f;
            }

            quad(buf, m, px, py, pz, dSize[i], right, up, r, g, b, alpha);
            drawn++;
        }

        if (drawn > 0) BufferUploader.drawWithShader(buf.buildOrThrow());
    }





    private static void quad(
            BufferBuilder buf, Matrix4f m,
            float cx, float cy, float cz, float size,
            Vector3f right, Vector3f up,
            float r, float g, float b, float a) {

        float rx = right.x * size, ry = right.y * size, rz = right.z * size;
        float ux = up.x    * size, uy = up.y    * size, uz = up.z    * size;

        buf.addVertex(m, cx - rx - ux, cy - ry - uy, cz - rz - uz).setColor(r, g, b, a);
        buf.addVertex(m, cx + rx - ux, cy + ry - uy, cz + rz - uz).setColor(r, g, b, a);
        buf.addVertex(m, cx + rx + ux, cy + ry + uy, cz + rz + uz).setColor(r, g, b, a);
        buf.addVertex(m, cx - rx + ux, cy - ry + uy, cz - rz + uz).setColor(r, g, b, a);
    }

    private static void singleQuad(
            Matrix4f m,
            float cx, float cy, float cz, float size,
            Vector3f right, Vector3f up,
            float r, float g, float b, float a) {

        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        quad(buf, m, cx, cy, cz, size, right, up, r, g, b, a);
        BufferUploader.drawWithShader(buf.buildOrThrow());
    }
}