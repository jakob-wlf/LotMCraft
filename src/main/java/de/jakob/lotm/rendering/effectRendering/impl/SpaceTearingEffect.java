package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import de.jakob.lotm.util.data.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpaceTearingEffect extends ActiveEffect {

    private static final float CRACK_RADIUS    = 5.0f;
    private static final float STORM_RADIUS    = 76.0f;
    private static final int   MAX_DEPTH       = 6;

    private static final ResourceLocation OBSIDIAN_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/block/obsidian.png");

    private final List<CrackSegment>  allSegments  = new ArrayList<>();
    private final List<SpiralArm>     spiralArms   = new ArrayList<>();
    private final List<StormParticle> stormParticles = new ArrayList<>();

    public SpaceTearingEffect(Location location, int duration, boolean infinite) {
        super(location, duration, infinite);

        Random rng = new Random(Double.doubleToLongBits(getX() * 31 + getZ()));

        int rootCount = 10 + rng.nextInt(5);
        for (int i = 0; i < rootCount; i++) {
            float baseAngle   = (i / (float) rootCount) * Mth.TWO_PI;
            float angleJitter = (rng.nextFloat() - 0.5f) * (Mth.TWO_PI / rootCount) * 0.6f;
            float tiltY       = (rng.nextFloat() - 0.5f) * Mth.PI * 0.5f;
            growBranch(rng, 0f, 0f, 0f, baseAngle + angleJitter, tiltY,
                    CRACK_RADIUS, 0.22f, 0, 0.05f, 0.30f);
        }

        int armCount = 5 + rng.nextInt(3);
        for (int i = 0; i < armCount; i++) {
            float startAngle  = (i / (float) armCount) * Mth.TWO_PI + rng.nextFloat() * 0.4f;
            float tiltOffset  = (rng.nextFloat() - 0.5f) * 0.6f;
            float spinRate    = 2.8f + rng.nextFloat() * 2.2f;
            float birthT      = 0.05f + (i / (float) armCount) * 0.18f;
            float width       = 1.8f + rng.nextFloat() * 2.4f;
            boolean clockwise = rng.nextBoolean();
            spiralArms.add(new SpiralArm(startAngle, tiltOffset, spinRate, birthT, width, clockwise));
        }

        for (int i = 0; i < 1200; i++) {
            float angle  = rng.nextFloat() * Mth.TWO_PI;
            float tiltV  = (rng.nextFloat() - 0.5f) * Mth.PI * 0.35f;
            float speed  = 0.15f + rng.nextFloat() * 0.70f;
            float life   = 0.20f + rng.nextFloat() * 0.45f;
            float birthT = rng.nextFloat() * 0.55f;
            float size   = 0.06f + rng.nextFloat() * 0.22f;
            float spin   = (rng.nextFloat() - 0.5f) * 4.5f;
            int   type   = rng.nextInt(5);
            stormParticles.add(new StormParticle(angle, tiltV, speed, life, birthT, size, spin, type));
        }
    }

    private void growBranch(Random rng,
                            float ox, float oy, float oz,
                            float angle, float tiltY,
                            float remainingLength, float halfWidth,
                            int depth, float spawnT, float timeBudget) {
        if (remainingLength < 0.08f || depth > MAX_DEPTH) return;

        int   segCount = 2 + rng.nextInt(2);
        float segLen   = remainingLength / segCount;
        float tPerSeg  = timeBudget / segCount;

        float cx = ox, cy = oy, cz = oz;
        float curAngle = angle, curTiltY = tiltY, curT = spawnT;

        for (int s = 0; s < segCount; s++) {
            curAngle += (rng.nextFloat() - 0.5f) * 0.65f;
            curTiltY += (rng.nextFloat() - 0.5f) * 0.40f;
            curTiltY  = Mth.clamp(curTiltY, -Mth.HALF_PI * 1.3f, Mth.HALF_PI * 1.3f);

            float ex = cx + Mth.cos(curAngle) * Mth.cos(curTiltY) * segLen;
            float ey = cy + Mth.sin(curTiltY) * segLen;
            float ez = cz + Mth.sin(curAngle) * Mth.cos(curTiltY) * segLen;

            allSegments.add(new CrackSegment(cx, cy, cz, ex, ey, ez, halfWidth, curT, depth));
            cx = ex; cy = ey; cz = ez;
            curT += tPerSeg;
        }

        if (depth < MAX_DEPTH) {
            int branchCount;
            if      (depth == 0) branchCount = 3 + rng.nextInt(3);
            else if (depth == 1) branchCount = 2 + rng.nextInt(3);
            else if (depth <= 3) branchCount = 2 + rng.nextInt(2);
            else                 branchCount = 1 + rng.nextInt(2);

            for (int b = 0; b < branchCount; b++) {
                float splitAngle = curAngle + (rng.nextFloat() - 0.5f) * Mth.TWO_PI * 0.50f;
                float splitTilt  = curTiltY + (rng.nextFloat() - 0.5f) * Mth.PI  * 0.65f;
                float childLen   = remainingLength * (0.38f + rng.nextFloat() * 0.24f);
                float childWidth = halfWidth * (0.44f + rng.nextFloat() * 0.16f);
                float overlap    = tPerSeg * (0.03f + rng.nextFloat() * 0.10f);
                growBranch(rng, cx, cy, cz, splitAngle, splitTilt,
                        childLen, childWidth, depth + 1, curT - overlap, timeBudget * 0.60f);
            }
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float progress = Math.min(tick / (float) getMaxDuration(), 1.0f);
        float alpha    = progress < 0.72f ? 1.0f : 1.0f - ((progress - 0.72f) / 0.28f);

        poseStack.pushPose();
        poseStack.translate(getX(), getY(), getZ());

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        Matrix4f mat = poseStack.last().pose();
        Matrix3f nm  = poseStack.last().normal();

        VertexConsumer glowBuf = buffers.getBuffer(RenderType.lightning());
        for (SpiralArm arm : spiralArms) {
            renderSpiralArm(glowBuf, mat, arm, progress, alpha);
        }
        for (StormParticle p : stormParticles) {
            renderStormParticle(glowBuf, mat, p, progress, alpha);
        }
        for (CrackSegment seg : allSegments) {
            float sp = segProgress(progress, seg.spawnT);
            if (sp <= 0f) continue;
            renderSegmentGlow(glowBuf, mat, seg, sp, alpha);
        }
        buffers.endBatch(RenderType.lightning());

        VertexConsumer solidBuf = buffers.getBuffer(RenderType.entitySolid(OBSIDIAN_TEXTURE));
        for (CrackSegment seg : allSegments) {
            float sp = segProgress(progress, seg.spawnT);
            if (sp <= 0f) continue;
            renderSegment(solidBuf, mat, nm, seg, sp, alpha);
        }
        buffers.endBatch(RenderType.entitySolid(OBSIDIAN_TEXTURE));

        poseStack.popPose();
    }

    private void renderSpiralArm(VertexConsumer buf, Matrix4f mat,
                                 SpiralArm arm, float progress, float masterAlpha) {
        if (progress < arm.birthT) return;

        float localT   = Mth.clamp((progress - arm.birthT) / (1.0f - arm.birthT), 0f, 1f);
        float expansion = easeOut(localT);
        float fade      = localT < 0.12f ? localT / 0.12f
                : localT > 0.72f ? 1.0f - ((localT - 0.72f) / 0.28f)
                : 1.0f;

        int steps = 220;
        for (int i = 0; i < steps - 1; i++) {
            float t0 = i       / (float)(steps - 1);
            float t1 = (i + 1) / (float)(steps - 1);

            float r0 = easeOut(t0) * STORM_RADIUS * expansion;
            float r1 = easeOut(t1) * STORM_RADIUS * expansion;

            float spin0 = arm.startAngle + (arm.clockwise ? 1f : -1f) * arm.spinRate * t0 * Mth.TWO_PI;
            float spin1 = arm.startAngle + (arm.clockwise ? 1f : -1f) * arm.spinRate * t1 * Mth.TWO_PI;

            float tilt0 = arm.tiltOffset * Mth.sin(t0 * Mth.PI);
            float tilt1 = arm.tiltOffset * Mth.sin(t1 * Mth.PI);

            float x0 = Mth.cos(spin0) * r0;
            float y0 = tilt0 * r0 * 0.12f;
            float z0 = Mth.sin(spin0) * r0;

            float x1 = Mth.cos(spin1) * r1;
            float y1 = tilt1 * r1 * 0.12f;
            float z1 = Mth.sin(spin1) * r1;

            float radialFade = 1.0f - (float)Math.pow(t0, 1.6f);
            float hw = (arm.width * radialFade + 0.3f);

            float dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
            float len = Mth.sqrt(dx*dx + dy*dy + dz*dz);
            if (len < 1e-4f) continue;

            float perpX = -(dz / len);
            float perpZ =  (dx / len);

            float alphaMult = masterAlpha * fade * radialFade;
            int aOuter = (int)(alphaMult * 180);
            int aInner = (int)(alphaMult * 255);
            if (aOuter <= 0) continue;

            int depthR = (int)(120 * radialFade);

            buf.addVertex(mat, x0 + perpX * hw,       y0, z0 + perpZ * hw).setColor(depthR, 0, 255, aOuter).setUv(0f,0f).setLight(LightTexture.FULL_BRIGHT);
            buf.addVertex(mat, x0 - perpX * hw,       y0, z0 - perpZ * hw).setColor(depthR, 0, 255, aOuter).setUv(1f,0f).setLight(LightTexture.FULL_BRIGHT);
            buf.addVertex(mat, x1 - perpX * (hw*0.9f), y1, z1 - perpZ * (hw*0.9f)).setColor(depthR, 0, 255, aOuter).setUv(1f,1f).setLight(LightTexture.FULL_BRIGHT);
            buf.addVertex(mat, x1 + perpX * (hw*0.9f), y1, z1 + perpZ * (hw*0.9f)).setColor(depthR, 0, 255, aOuter).setUv(0f,1f).setLight(LightTexture.FULL_BRIGHT);

            float ihw = hw * 0.28f;
            buf.addVertex(mat, x0 + perpX * ihw,       y0, z0 + perpZ * ihw).setColor(255, 220, 255, aInner).setUv(0f,0f).setLight(LightTexture.FULL_BRIGHT);
            buf.addVertex(mat, x0 - perpX * ihw,       y0, z0 - perpZ * ihw).setColor(255, 220, 255, aInner).setUv(1f,0f).setLight(LightTexture.FULL_BRIGHT);
            buf.addVertex(mat, x1 - perpX * (ihw*0.9f),y1, z1 - perpZ * (ihw*0.9f)).setColor(255, 220, 255, aInner).setUv(1f,1f).setLight(LightTexture.FULL_BRIGHT);
            buf.addVertex(mat, x1 + perpX * (ihw*0.9f),y1, z1 + perpZ * (ihw*0.9f)).setColor(255, 220, 255, aInner).setUv(0f,1f).setLight(LightTexture.FULL_BRIGHT);
        }
    }

    private void renderStormParticle(VertexConsumer buf, Matrix4f mat,
                                     StormParticle p, float progress, float masterAlpha) {
        if (progress < p.birthT) return;

        float localT = Mth.clamp((progress - p.birthT) / p.life, 0f, 1f);
        float fade   = localT < 0.12f ? localT / 0.12f : 1.0f - ((localT - 0.12f) / 0.88f);
        float dist   = easeOut(localT) * p.speed * STORM_RADIUS;
        float spiralAngle = p.angle + p.spin * localT;

        float cx = Mth.cos(spiralAngle) * Mth.cos(p.tiltV) * dist;
        float cy = Mth.sin(p.tiltV) * dist * 0.18f;
        float cz = Mth.sin(spiralAngle) * Mth.cos(p.tiltV) * dist;

        float s = p.size * (1.0f - localT * 0.55f);
        int   a = (int)(masterAlpha * fade * 210);
        if (s <= 0f || a <= 0) return;

        int r, g, b;
        switch (p.type) {
            case 0  -> { r = 180; g = 0;   b = 255; }
            case 1  -> { r = 255; g = 255; b = 255; }
            case 2  -> { r = 60;  g = 0;   b = 200; }
            case 3  -> { r = 240; g = 80;  b = 255; }
            default -> { r = 160; g = 40;  b = 255; }
        }

        buf.addVertex(mat, cx-s, cy-s, cz  ).setColor(r,g,b,a).setUv(0f,0f).setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, cx+s, cy-s, cz  ).setColor(r,g,b,a).setUv(1f,0f).setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, cx+s, cy+s, cz  ).setColor(r,g,b,a).setUv(1f,1f).setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, cx-s, cy+s, cz  ).setColor(r,g,b,a).setUv(0f,1f).setLight(LightTexture.FULL_BRIGHT);

        buf.addVertex(mat, cx,   cy-s, cz-s).setColor(r,g,b,a).setUv(0f,0f).setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, cx,   cy-s, cz+s).setColor(r,g,b,a).setUv(1f,0f).setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, cx,   cy+s, cz+s).setColor(r,g,b,a).setUv(1f,1f).setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, cx,   cy+s, cz-s).setColor(r,g,b,a).setUv(0f,1f).setLight(LightTexture.FULL_BRIGHT);
    }

    private static float segProgress(float progress, float spawnT) {
        return easeOut(Mth.clamp((progress - spawnT) / 0.038f, 0f, 1f));
    }

    private void renderSegment(VertexConsumer buf, Matrix4f mat, Matrix3f nm,
                               CrackSegment seg, float sp, float masterAlpha) {
        float ex = seg.x0 + (seg.x1 - seg.x0) * sp;
        float ey = seg.y0 + (seg.y1 - seg.y0) * sp;
        float ez = seg.z0 + (seg.z1 - seg.z0) * sp;

        float dx = ex - seg.x0, dy = ey - seg.y0, dz = ez - seg.z0;
        float len = Mth.sqrt(dx*dx + dy*dy + dz*dz);
        if (len < 1e-4f) return;

        float fdx = dx/len, fdy = dy/len, fdz = dz/len;

        float perpX, perpY, perpZ;
        if (Math.abs(fdy) < 0.9f) { perpX = -fdz; perpY = 0f; perpZ = fdx; }
        else                       { perpX =  1f;  perpY = 0f; perpZ = 0f; }
        float pl = Mth.sqrt(perpX*perpX + perpY*perpY + perpZ*perpZ);
        perpX /= pl; perpY /= pl; perpZ /= pl;

        float hw0 = seg.halfWidth;
        float hw1 = seg.halfWidth * 0.38f;
        int   a   = (int)(masterAlpha * 255);

        float fnx = fdy*perpZ - fdz*perpY;
        float fny = fdz*perpX - fdx*perpZ;
        float fnz = fdx*perpY - fdy*perpX;

        float lx0=seg.x0+perpX*hw0, ly0=seg.y0+perpY*hw0, lz0=seg.z0+perpZ*hw0;
        float rx0=seg.x0-perpX*hw0, ry0=seg.y0-perpY*hw0, rz0=seg.z0-perpZ*hw0;
        float lx1=ex    +perpX*hw1, ly1=ey    +perpY*hw1, lz1=ez    +perpZ*hw1;
        float rx1=ex    -perpX*hw1, ry1=ey    -perpY*hw1, rz1=ez    -perpZ*hw1;

        addSolidVert(buf,mat,nm, lx0,ly0,lz0, 0f,0f, a,  fnx, fny, fnz);
        addSolidVert(buf,mat,nm, rx0,ry0,rz0, 0f,1f, a,  fnx, fny, fnz);
        addSolidVert(buf,mat,nm, rx1,ry1,rz1, 1f,1f, a,  fnx, fny, fnz);
        addSolidVert(buf,mat,nm, lx1,ly1,lz1, 1f,0f, a,  fnx, fny, fnz);
        addSolidVert(buf,mat,nm, lx1,ly1,lz1, 1f,0f, a, -fnx,-fny,-fnz);
        addSolidVert(buf,mat,nm, rx1,ry1,rz1, 1f,1f, a, -fnx,-fny,-fnz);
        addSolidVert(buf,mat,nm, rx0,ry0,rz0, 0f,1f, a, -fnx,-fny,-fnz);
        addSolidVert(buf,mat,nm, lx0,ly0,lz0, 0f,0f, a, -fnx,-fny,-fnz);
    }

    private void renderSegmentGlow(VertexConsumer buf, Matrix4f mat,
                                   CrackSegment seg, float sp, float masterAlpha) {
        float ex = seg.x0 + (seg.x1 - seg.x0) * sp;
        float ey = seg.y0 + (seg.y1 - seg.y0) * sp;
        float ez = seg.z0 + (seg.z1 - seg.z0) * sp;

        float dx = ex - seg.x0, dy = ey - seg.y0, dz = ez - seg.z0;
        float len = Mth.sqrt(dx*dx + dy*dy + dz*dz);
        if (len < 1e-4f) return;

        float depthFade = Math.max(0.12f, 1.0f - seg.depth * 0.13f);
        int   oR = (int)(140 * depthFade);
        int   a  = (int)(masterAlpha * depthFade * 200);

        float perpX = -(dz/len), perpZ = (dx/len);
        float glowHW  = seg.halfWidth * 0.60f + 0.018f;
        float innerHW = glowHW * 0.25f;

        buf.addVertex(mat, seg.x0+perpX*glowHW,  seg.y0, seg.z0+perpZ*glowHW).setColor(oR,0,255,a).setUv(0f,0f).setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, seg.x0-perpX*glowHW,  seg.y0, seg.z0-perpZ*glowHW).setColor(oR,0,255,a).setUv(1f,0f).setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, ex    -perpX*glowHW,  ey,     ez    -perpZ*glowHW).setColor(oR,0,255,0).setUv(1f,1f).setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, ex    +perpX*glowHW,  ey,     ez    +perpZ*glowHW).setColor(oR,0,255,0).setUv(0f,1f).setLight(LightTexture.FULL_BRIGHT);

        buf.addVertex(mat, seg.x0+perpX*innerHW, seg.y0, seg.z0+perpZ*innerHW).setColor(255,220,255,a).setUv(0f,0f).setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, seg.x0-perpX*innerHW, seg.y0, seg.z0-perpZ*innerHW).setColor(255,220,255,a).setUv(1f,0f).setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, ex    -perpX*innerHW, ey,     ez    -perpZ*innerHW).setColor(255,220,255,0).setUv(1f,1f).setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, ex    +perpX*innerHW, ey,     ez    +perpZ*innerHW).setColor(255,220,255,0).setUv(0f,1f).setLight(LightTexture.FULL_BRIGHT);
    }

    private void addSolidVert(VertexConsumer buf, Matrix4f mat, Matrix3f nm,
                              float x, float y, float z, float u, float v, int a,
                              float nx, float ny, float nz) {
        buf.addVertex(mat, x, y, z)
                .setColor(255, 255, 255, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(nx, ny, nz);
    }

    private static float easeOut(float t) {
        float f = 1f - t;
        return 1f - f * f * f;
    }

    private static class CrackSegment {
        final float x0, y0, z0, x1, y1, z1, halfWidth, spawnT;
        final int   depth;

        CrackSegment(float x0, float y0, float z0,
                     float x1, float y1, float z1,
                     float halfWidth, float spawnT, int depth) {
            this.x0=x0; this.y0=y0; this.z0=z0;
            this.x1=x1; this.y1=y1; this.z1=z1;
            this.halfWidth=halfWidth; this.spawnT=spawnT; this.depth=depth;
        }
    }

    private static class SpiralArm {
        final float startAngle, tiltOffset, spinRate, birthT, width;
        final boolean clockwise;

        SpiralArm(float startAngle, float tiltOffset, float spinRate,
                  float birthT, float width, boolean clockwise) {
            this.startAngle=startAngle; this.tiltOffset=tiltOffset;
            this.spinRate=spinRate; this.birthT=birthT;
            this.width=width; this.clockwise=clockwise;
        }
    }

    private static class StormParticle {
        final float angle, tiltV, speed, life, birthT, size, spin;
        final int   type;

        StormParticle(float angle, float tiltV, float speed,
                      float life, float birthT, float size, float spin, int type) {
            this.angle=angle; this.tiltV=tiltV; this.speed=speed;
            this.life=life; this.birthT=birthT; this.size=size;
            this.spin=spin; this.type=type;
        }
    }
}