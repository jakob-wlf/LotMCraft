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


public class BeamsOfLightEffect extends ActiveEffect  {

    private static int LOOP_PERIOD = 120;


    
    private static final int BRIGHT_COUNT = 40;
    
    private static final int DARK_COUNT   = 20;

    private final RandomSource rng = RandomSource.create();


    
    private final float[] bDirX    = new float[BRIGHT_COUNT];
    private final float[] bDirY    = new float[BRIGHT_COUNT];
    private final float[] bDirZ    = new float[BRIGHT_COUNT];
    private final float[] bLength  = new float[BRIGHT_COUNT];
    
    private final float[] bHalfW   = new float[BRIGHT_COUNT];
    
    private final float[] bPhase   = new float[BRIGHT_COUNT];
    
    private final int[]   bColor   = new int[BRIGHT_COUNT];


    private final float[] dDirX    = new float[DARK_COUNT];
    private final float[] dDirY    = new float[DARK_COUNT];
    private final float[] dDirZ    = new float[DARK_COUNT];
    private final float[] dLength  = new float[DARK_COUNT];
    private final float[] dHalfW   = new float[DARK_COUNT];
    private final float[] dPhase   = new float[DARK_COUNT];



    public BeamsOfLightEffect(Location location, int duration, boolean infinite) {

        super(location, duration, infinite);
        LOOP_PERIOD = duration;
        bakeBeams();
    }





    private void bakeBeams() {
        for (int i = 0; i < BRIGHT_COUNT; i++) {
            float[] dir = uniformSphereDir();
            bDirX[i]   = dir[0];
            bDirY[i]   = dir[1];
            bDirZ[i]   = dir[2];
            bLength[i] = 5f + rng.nextFloat() * 8f;
            bHalfW[i]  = 0.04f + rng.nextFloat() * 0.10f;
            bPhase[i]  = rng.nextFloat() * Mth.TWO_PI;
            bColor[i]  = rng.nextFloat() < 0.55f ? 0 : 1;
        }

        for (int i = 0; i < DARK_COUNT; i++) {
            float[] dir = uniformSphereDir();
            dDirX[i]   = dir[0];
            dDirY[i]   = dir[1];
            dDirZ[i]   = dir[2];
            dLength[i] = 4f + rng.nextFloat() * 6f;
            dHalfW[i]  = 0.06f + rng.nextFloat() * 0.10f;
            dPhase[i]  = rng.nextFloat() * Mth.TWO_PI;
        }
    }

    
    private float[] uniformSphereDir() {
        float theta = rng.nextFloat() * Mth.TWO_PI;
        float phi   = (float) Math.acos(1f - 2f * rng.nextFloat());
        float sinPhi = Mth.sin(phi);
        return new float[]{
                sinPhi * Mth.cos(theta),
                Mth.cos(phi),
                sinPhi * Mth.sin(theta)
        };
    }





    @Override
    protected void render(PoseStack poseStack, float tick) {
        if (Minecraft.getInstance().level == null) return;


        float age = tick % LOOP_PERIOD;



        float precession = age / LOOP_PERIOD * Mth.TWO_PI;
        float cosP = Mth.cos(precession);
        float sinP = Mth.sin(precession);


        Quaternionf camRot = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        Vector3f camRight  = new Vector3f(1f, 0f, 0f).rotate(camRot);
        Vector3f camUp     = new Vector3f(0f, 1f, 0f).rotate(camRot);

        poseStack.pushPose();
        poseStack.translate(location.getPosition().x, location.getPosition().y, location.getPosition().z);
        Matrix4f m = poseStack.last().pose();

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();


        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        renderDarkBeams(m, age, cosP, sinP, camRight, camUp);


        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE);
        renderBrightBeams(m, age, cosP, sinP, camRight, camUp);
        renderCore(m, age, camRight, camUp);

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }





    private void renderBrightBeams(Matrix4f m, float age,
                                   float cosP, float sinP,
                                   Vector3f camRight, Vector3f camUp) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int drawn = 0;

        for (int i = 0; i < BRIGHT_COUNT; i++) {

            float dx =  cosP * bDirX[i] + sinP * bDirZ[i];
            float dy =  bDirY[i];
            float dz = -sinP * bDirX[i] + cosP * bDirZ[i];


            float alpha = 0.30f + 0.70f * (0.5f + 0.5f * Mth.sin(
                    age / LOOP_PERIOD * Mth.TWO_PI + bPhase[i]));
            if (alpha < 0.01f) continue;

            float r, g, b;
            if (bColor[i] == 0) { r = 1f;    g = 1f;    b = 1f;    }
            else                 { r = 0.65f; g = 0.05f; b = 0.95f; }

            beamQuad(buf, m, dx, dy, dz, bLength[i], bHalfW[i], camRight, camUp,
                    r, g, b, alpha);
            drawn++;
        }

        if (drawn > 0) BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private void renderDarkBeams(Matrix4f m, float age,
                                 float cosP, float sinP,
                                 Vector3f camRight, Vector3f camUp) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int drawn = 0;

        for (int i = 0; i < DARK_COUNT; i++) {
            float dx =  cosP * dDirX[i] + sinP * dDirZ[i];
            float dy =  dDirY[i];
            float dz = -sinP * dDirX[i] + cosP * dDirZ[i];

            float alpha = 0.20f + 0.50f * (0.5f + 0.5f * Mth.sin(
                    age / LOOP_PERIOD * Mth.TWO_PI + dPhase[i]));


            beamQuad(buf, m, dx, dy, dz, dLength[i], dHalfW[i], camRight, camUp,
                    0.02f, 0f, 0.05f, alpha);
            drawn++;
        }

        if (drawn > 0) BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    
    private void renderCore(Matrix4f m, float age, Vector3f camRight, Vector3f camUp) {
        float t = age / LOOP_PERIOD * Mth.TWO_PI;

        float innerPulse = 0.80f + 0.20f * Mth.sin(t * 3f);
        singleQuad(m, 0f, 0f, 0f, 0.30f * innerPulse, camRight, camUp,
                1f, 1f, 1f, 0.95f);

        float midPulse = 0.70f + 0.30f * Mth.sin(t * 1.5f);
        singleQuad(m, 0f, 0f, 0f, 0.80f * midPulse, camRight, camUp,
                0.80f, 0.50f, 1f, 0.55f);

        float outerPulse = 0.60f + 0.40f * Mth.sin(t + 1.1f);
        singleQuad(m, 0f, 0f, 0f, 1.60f * outerPulse, camRight, camUp,
                0.40f, 0f, 0.70f, 0.30f);
    }





    
    private static void beamQuad(
            BufferBuilder buf, Matrix4f m,
            float dx, float dy, float dz,
            float length, float halfW,
            Vector3f camRight, Vector3f camUp,
            float r, float g, float b, float a) {


        float dLen = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dLen < 0.0001f) return;
        dx /= dLen; dy /= dLen; dz /= dLen;



        float dotR = dx * camRight.x + dy * camRight.y + dz * camRight.z;
        float px   = camRight.x - dotR * dx;
        float py   = camRight.y - dotR * dy;
        float pz   = camRight.z - dotR * dz;

        float pLen = (float) Math.sqrt(px * px + py * py + pz * pz);
        if (pLen < 0.01f) {

            float dotU = dx * camUp.x + dy * camUp.y + dz * camUp.z;
            px = camUp.x - dotU * dx;
            py = camUp.y - dotU * dy;
            pz = camUp.z - dotU * dz;
            pLen = (float) Math.sqrt(px * px + py * py + pz * pz);
            if (pLen < 0.001f) return;
        }
        float invP = halfW / pLen;
        px *= invP; py *= invP; pz *= invP;




        float nearOff = 0.05f;
        float nearX = dx * nearOff, nearY = dy * nearOff, nearZ = dz * nearOff;


        float tipX = dx * length, tipY = dy * length, tipZ = dz * length;
        float tipW = 0.03f;
        float tpx = px * tipW, tpy = py * tipW, tpz = pz * tipW;


        buf.addVertex(m, nearX - px, nearY - py, nearZ - pz).setColor(r, g, b, a);
        buf.addVertex(m, nearX + px, nearY + py, nearZ + pz).setColor(r, g, b, a);
        buf.addVertex(m, tipX  + tpx, tipY + tpy, tipZ + tpz).setColor(r, g, b, 0f);
        buf.addVertex(m, tipX  - tpx, tipY - tpy, tipZ - tpz).setColor(r, g, b, 0f);
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