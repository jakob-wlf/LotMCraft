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
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class SpaceTearEffect extends ActiveEffect {

    private final RandomSource random = RandomSource.create();
    private final List<VoidCrack> cracks = new ArrayList<>();
    private final List<RiftTendril> tendrils = new ArrayList<>();
    private final List<VoidShard> shards = new ArrayList<>();
    private final List<SpaceParticle> particles = new ArrayList<>();
    private final List<ShockwavePulse> pulses = new ArrayList<>();
    private final List<EnergyNode> nodes = new ArrayList<>();
    private final ObsidianRing[] rings = new ObsidianRing[3];

    private static final float CYCLE_DURATION = 60f;
    private static final ResourceLocation OBSIDIAN_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/block/obsidian.png");

    public SpaceTearEffect(Location location, int maxDuration, boolean infinite) {
        super(location, maxDuration, infinite);

        for (int i = 0; i < 14; i++) cracks.add(new VoidCrack(i));
        for (int i = 0; i < 10; i++) tendrils.add(new RiftTendril(i));
        for (int i = 0; i < 35; i++) shards.add(new VoidShard());
        for (int i = 0; i < 70; i++) particles.add(new SpaceParticle());
        for (int i = 0; i < 4; i++) pulses.add(new ShockwavePulse(i));
        for (int i = 0; i < 6; i++) nodes.add(new EnergyNode(i));

        rings[0] = new ObsidianRing(3.0f, 4.4f,  0.045f,  0.031f,  0.017f);
        rings[1] = new ObsidianRing(3.5f, 5.0f, -0.032f,  0.024f, -0.011f);
        rings[2] = new ObsidianRing(2.5f, 3.8f,  0.058f, -0.019f,  0.027f);
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        poseStack.pushPose();
        poseStack.translate(getX(), getY(), getZ());

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();

        float loopProgress = (tick % CYCLE_DURATION) / CYCLE_DURATION;

        renderCoreRift(consumer, matrix, tick, loopProgress);

        for (EnergyNode node : nodes) {
            node.update(tick);
            renderBillboardQuad(consumer, matrix, node.x, node.y, node.z,
                    node.size, node.r, 0f, node.b, node.alpha);
        }

        for (ShockwavePulse pulse : pulses) {
            pulse.update(tick, loopProgress);
            if (pulse.alpha > 0.01f) renderShockwave(consumer, matrix, pulse);
        }

        for (VoidCrack crack : cracks) {
            crack.update(tick, loopProgress);
            renderVoidCrack(consumer, matrix, crack, tick);
        }

        for (RiftTendril tendril : tendrils) {
            tendril.update(tick, loopProgress);
            renderRiftTendril(consumer, matrix, tendril, tick);
        }

        for (VoidShard shard : shards) {
            shard.update(tick, loopProgress);
            if (shard.alpha > 0.01f) renderVoidShard(consumer, matrix, shard);
        }

        for (SpaceParticle particle : particles) {
            particle.update(tick, loopProgress);
            if (particle.alpha > 0.01f) {
                renderBillboardQuad(consumer, matrix, particle.x, particle.y, particle.z,
                        particle.size, particle.r, particle.g, particle.b, particle.alpha);
            }
        }

        if(tick % 20 == 0) {
            cracks.clear();
            for (int i = 0; i < 14; i++) cracks.add(new VoidCrack(i));
        }

        bufferSource.endBatch(RenderType.lightning());

        VertexConsumer texConsumer = bufferSource.getBuffer(RenderType.entitySolid(OBSIDIAN_TEXTURE));
        for (ObsidianRing ring : rings) {
            ring.update(tick);
            renderObsidianRing(texConsumer, matrix, normalMatrix, ring);
        }

        poseStack.popPose();
    }

    private void renderCoreRift(VertexConsumer consumer, Matrix4f matrix, float tick, float loopProgress) {
        int segments = 20;
        int ringCount = 10;

        float pulsePhase = Mth.sin(loopProgress * Mth.TWO_PI * 3f);
        float coreRadius = 0.7f + 0.35f * pulsePhase;
        float rotation = tick * 0.09f;

        for (int r = 0; r < ringCount; r++) {
            float theta1 = (r / (float) ringCount) * Mth.PI;
            float theta2 = ((r + 1) / (float) ringCount) * Mth.PI;

            for (int i = 0; i < segments; i++) {
                float phi1 = (i / (float) segments) * Mth.TWO_PI + rotation;
                float phi2 = ((i + 1) / (float) segments) * Mth.TWO_PI + rotation;

                float x1 = Mth.sin(theta1) * Mth.cos(phi1) * coreRadius;
                float y1 = Mth.cos(theta1) * coreRadius;
                float z1 = Mth.sin(theta1) * Mth.sin(phi1) * coreRadius;
                float x2 = Mth.sin(theta1) * Mth.cos(phi2) * coreRadius;
                float y2 = Mth.cos(theta1) * coreRadius;
                float z2 = Mth.sin(theta1) * Mth.sin(phi2) * coreRadius;
                float x3 = Mth.sin(theta2) * Mth.cos(phi2) * coreRadius;
                float y3 = Mth.cos(theta2) * coreRadius;
                float z3 = Mth.sin(theta2) * Mth.sin(phi2) * coreRadius;
                float x4 = Mth.sin(theta2) * Mth.cos(phi1) * coreRadius;
                float y4 = Mth.cos(theta2) * coreRadius;
                float z4 = Mth.sin(theta2) * Mth.sin(phi1) * coreRadius;

                float equatorFactor = 1f - Math.abs((r / (float) ringCount) - 0.5f) * 2f;
                float distort = 0.8f + 0.2f * Mth.sin(tick * 0.22f + r * 0.7f);
                float cr = (0.25f + 0.15f * equatorFactor) * distort;
                float cb = (0.55f + 0.3f * equatorFactor) * distort;
                float alpha = (0.55f + 0.2f * equatorFactor + 0.1f * pulsePhase) * distort;

                addVertex(consumer, matrix, x1, y1, z1, cr, 0f, cb, alpha);
                addVertex(consumer, matrix, x2, y2, z2, cr, 0f, cb, alpha);
                addVertex(consumer, matrix, x3, y3, z3, cr, 0f, cb, alpha);
                addVertex(consumer, matrix, x4, y4, z4, cr, 0f, cb, alpha);
            }
        }

        for (int ribbon = 0; ribbon < 3; ribbon++) {
            float ribbonOffset = ribbon * (Mth.TWO_PI / 3f) + tick * 0.14f;
            int rSegments = 32;
            for (int i = 0; i < rSegments; i++) {
                float phi1 = (i / (float) rSegments) * Mth.TWO_PI + ribbonOffset;
                float phi2 = ((i + 1) / (float) rSegments) * Mth.TWO_PI + ribbonOffset;
                float waveY1 = Mth.sin(phi1 * 3f + tick * 0.18f) * 0.45f;
                float waveY2 = Mth.sin(phi2 * 3f + tick * 0.18f) * 0.45f;
                float r2 = coreRadius * 1.25f;
                float pulse2 = 0.7f + 0.3f * Mth.sin(tick * 0.2f + ribbon);

                addVertex(consumer, matrix, Mth.cos(phi1) * r2, waveY1 - 0.05f, Mth.sin(phi1) * r2, 0.5f * pulse2, 0f, 0.9f * pulse2, 0.55f);
                addVertex(consumer, matrix, Mth.cos(phi2) * r2, waveY2 - 0.05f, Mth.sin(phi2) * r2, 0.5f * pulse2, 0f, 0.9f * pulse2, 0.55f);
                addVertex(consumer, matrix, Mth.cos(phi2) * r2, waveY2 + 0.05f, Mth.sin(phi2) * r2, 0.3f * pulse2, 0f, 0.7f * pulse2, 0.3f);
                addVertex(consumer, matrix, Mth.cos(phi1) * r2, waveY1 + 0.05f, Mth.sin(phi1) * r2, 0.3f * pulse2, 0f, 0.7f * pulse2, 0.3f);
            }
        }

        for (int layer = 1; layer <= 3; layer++) {
            float coronaRadius = coreRadius * (1f + layer * 0.35f);
            float coronaAlpha = 0.3f / layer * (0.8f + 0.2f * Mth.sin(tick * 0.15f + layer));
            for (int i = 0; i < segments * 2; i++) {
                float phi1 = (i / (float) (segments * 2)) * Mth.TWO_PI + rotation * 0.5f;
                float phi2 = ((i + 1) / (float) (segments * 2)) * Mth.TWO_PI + rotation * 0.5f;
                float inner = coronaRadius * 0.88f;
                addVertex(consumer, matrix, Mth.cos(phi1) * inner, 0f, Mth.sin(phi1) * inner, 0.4f, 0f, 0.8f, coronaAlpha);
                addVertex(consumer, matrix, Mth.cos(phi2) * inner, 0f, Mth.sin(phi2) * inner, 0.4f, 0f, 0.8f, coronaAlpha);
                addVertex(consumer, matrix, Mth.cos(phi2) * coronaRadius, 0f, Mth.sin(phi2) * coronaRadius, 0.15f, 0f, 0.4f, 0f);
                addVertex(consumer, matrix, Mth.cos(phi1) * coronaRadius, 0f, Mth.sin(phi1) * coronaRadius, 0.15f, 0f, 0.4f, 0f);
            }
        }
    }

    private void renderShockwave(VertexConsumer consumer, Matrix4f matrix, ShockwavePulse pulse) {
        int segments = 36;
        float innerR = pulse.radius * 0.92f;
        float outerR = pulse.radius;

        for (int i = 0; i < segments; i++) {
            float phi1 = (i / (float) segments) * Mth.TWO_PI + pulse.tiltOffset;
            float phi2 = ((i + 1) / (float) segments) * Mth.TWO_PI + pulse.tiltOffset;

            float[] p1i = tiltPoint(Mth.cos(phi1) * innerR, pulse.height, Mth.sin(phi1) * innerR, pulse.tiltX, pulse.tiltZ);
            float[] p2i = tiltPoint(Mth.cos(phi2) * innerR, pulse.height, Mth.sin(phi2) * innerR, pulse.tiltX, pulse.tiltZ);
            float[] p1o = tiltPoint(Mth.cos(phi1) * outerR, pulse.height, Mth.sin(phi1) * outerR, pulse.tiltX, pulse.tiltZ);
            float[] p2o = tiltPoint(Mth.cos(phi2) * outerR, pulse.height, Mth.sin(phi2) * outerR, pulse.tiltX, pulse.tiltZ);

            addVertex(consumer, matrix, p1i[0], p1i[1], p1i[2], 0.4f, 0f, 0.85f, pulse.alpha);
            addVertex(consumer, matrix, p2i[0], p2i[1], p2i[2], 0.4f, 0f, 0.85f, pulse.alpha);
            addVertex(consumer, matrix, p2o[0], p2o[1], p2o[2], 0.2f, 0f, 0.5f, 0f);
            addVertex(consumer, matrix, p1o[0], p1o[1], p1o[2], 0.2f, 0f, 0.5f, 0f);
        }
    }

    private void renderVoidCrack(VertexConsumer consumer, Matrix4f matrix, VoidCrack crack, float tick) {
        if (crack.alpha <= 0.01f) return;

        float sparkPos = (tick * 0.32f + crack.phaseOffset * 7f) % crack.points.length;

        for (int seg = 0; seg < crack.points.length - 1; seg++) {
            float[] p1 = crack.points[seg];
            float[] p2 = crack.points[seg + 1];

            float t = seg / (float) crack.points.length;
            float width = crack.width * (1f - t * 0.45f);

            float sparkDist = Math.abs(seg - sparkPos);
            float sparkIntensity = Math.max(0f, 1f - sparkDist / 2.5f);

            Vec3 dir = new Vec3(p2[0] - p1[0], p2[1] - p1[1], p2[2] - p1[2]).normalize();
            Vec3 perp = getPerp(dir).scale(width);
            Vec3 perpOuter = perp.scale(3.5f);

            float edgePulse = 0.7f + 0.3f * Mth.sin(tick * 0.28f + t * 5f + crack.phaseOffset * Mth.TWO_PI);
            float widthPulse = 1f + 0.65f * Mth.sin(tick * 0.38f + t * 9f + crack.phaseOffset * 5f);
            Vec3 perpPulsed = perp.scale(widthPulse);
            Vec3 perpOuterPulsed = perpOuter.scale(widthPulse);

            float r = (0.35f + 0.45f * sparkIntensity) * edgePulse;
            float b = (0.7f + 0.25f * sparkIntensity) * edgePulse;
            float edgeAlpha = crack.alpha * 0.7f;

            addVertex(consumer, matrix, p1f(p1[0], perpOuterPulsed, 'x', -1), p1f(p1[1], perpOuterPulsed, 'y', -1), p1f(p1[2], perpOuterPulsed, 'z', -1), r, 0f, b, 0f);
            addVertex(consumer, matrix, p1f(p1[0], perpPulsed, 'x', -1), p1f(p1[1], perpPulsed, 'y', -1), p1f(p1[2], perpPulsed, 'z', -1), r, 0f, b, edgeAlpha);
            addVertex(consumer, matrix, p1f(p2[0], perpPulsed, 'x', -1), p1f(p2[1], perpPulsed, 'y', -1), p1f(p2[2], perpPulsed, 'z', -1), r, 0f, b, edgeAlpha);
            addVertex(consumer, matrix, p1f(p2[0], perpOuterPulsed, 'x', -1), p1f(p2[1], perpOuterPulsed, 'y', -1), p1f(p2[2], perpOuterPulsed, 'z', -1), r, 0f, b, 0f);

            addVertex(consumer, matrix, p1f(p1[0], perpPulsed, 'x', 1), p1f(p1[1], perpPulsed, 'y', 1), p1f(p1[2], perpPulsed, 'z', 1), r, 0f, b, edgeAlpha);
            addVertex(consumer, matrix, p1f(p1[0], perpOuterPulsed, 'x', 1), p1f(p1[1], perpOuterPulsed, 'y', 1), p1f(p1[2], perpOuterPulsed, 'z', 1), r, 0f, b, 0f);
            addVertex(consumer, matrix, p1f(p2[0], perpOuterPulsed, 'x', 1), p1f(p2[1], perpOuterPulsed, 'y', 1), p1f(p2[2], perpOuterPulsed, 'z', 1), r, 0f, b, 0f);
            addVertex(consumer, matrix, p1f(p2[0], perpPulsed, 'x', 1), p1f(p2[1], perpPulsed, 'y', 1), p1f(p2[2], perpPulsed, 'z', 1), r, 0f, b, edgeAlpha);

            float cr = 0.6f + 0.4f * sparkIntensity;
            float cb = 0.9f + 0.1f * sparkIntensity;
            float coreAlpha = crack.alpha * (0.9f + 0.1f * sparkIntensity);
            Vec3 core = perpPulsed.scale(0.35f);

            addVertex(consumer, matrix, p1f(p1[0], perpPulsed, 'x', -1), p1f(p1[1], perpPulsed, 'y', -1), p1f(p1[2], perpPulsed, 'z', -1), cr, 0f, cb, edgeAlpha);
            addVertex(consumer, matrix, p1f(p1[0], core, 'x', -1), p1f(p1[1], core, 'y', -1), p1f(p1[2], core, 'z', -1), cr, 0f, cb, coreAlpha);
            addVertex(consumer, matrix, p1f(p2[0], core, 'x', -1), p1f(p2[1], core, 'y', -1), p1f(p2[2], core, 'z', -1), cr, 0f, cb, coreAlpha);
            addVertex(consumer, matrix, p1f(p2[0], perpPulsed, 'x', -1), p1f(p2[1], perpPulsed, 'y', -1), p1f(p2[2], perpPulsed, 'z', -1), cr, 0f, cb, edgeAlpha);

            addVertex(consumer, matrix, p1f(p1[0], core, 'x', 1), p1f(p1[1], core, 'y', 1), p1f(p1[2], core, 'z', 1), cr, 0f, cb, coreAlpha);
            addVertex(consumer, matrix, p1f(p1[0], perpPulsed, 'x', 1), p1f(p1[1], perpPulsed, 'y', 1), p1f(p1[2], perpPulsed, 'z', 1), cr, 0f, cb, edgeAlpha);
            addVertex(consumer, matrix, p1f(p2[0], perpPulsed, 'x', 1), p1f(p2[1], perpPulsed, 'y', 1), p1f(p2[2], perpPulsed, 'z', 1), cr, 0f, cb, edgeAlpha);
            addVertex(consumer, matrix, p1f(p2[0], core, 'x', 1), p1f(p2[1], core, 'y', 1), p1f(p2[2], core, 'z', 1), cr, 0f, cb, coreAlpha);
        }
    }

    private void renderRiftTendril(VertexConsumer consumer, Matrix4f matrix, RiftTendril tendril, float tick) {
        if (tendril.alpha <= 0.01f) return;

        int segments = 20;
        for (int i = 0; i < segments - 1; i++) {
            float t1 = i / (float) segments;
            float t2 = (i + 1) / (float) segments;

            Vec3 pos1 = tendril.getPosition(t1, tick);
            Vec3 pos2 = tendril.getPosition(t2, tick);

            float width = tendril.width * (1f - t1 * 0.75f);
            Vec3 dir = pos2.subtract(pos1).normalize();
            Vec3 perp = getPerp(dir).scale(width);
            Vec3 perpOuter = perp.scale(2.8f);

            float r = 0.25f + 0.2f * t1;
            float b = 0.6f - 0.2f * t1;
            float alpha = tendril.alpha * (1f - t1 * 0.65f);

            addVertex(consumer, matrix, (float)(pos1.x - perpOuter.x), (float)(pos1.y - perpOuter.y), (float)(pos1.z - perpOuter.z), r, 0f, b, 0f);
            addVertex(consumer, matrix, (float)(pos1.x - perp.x), (float)(pos1.y - perp.y), (float)(pos1.z - perp.z), r, 0f, b, alpha);
            addVertex(consumer, matrix, (float)(pos2.x - perp.x), (float)(pos2.y - perp.y), (float)(pos2.z - perp.z), r, 0f, b, alpha * 0.6f);
            addVertex(consumer, matrix, (float)(pos2.x - perpOuter.x), (float)(pos2.y - perpOuter.y), (float)(pos2.z - perpOuter.z), r, 0f, b, 0f);

            addVertex(consumer, matrix, (float)(pos1.x + perp.x), (float)(pos1.y + perp.y), (float)(pos1.z + perp.z), r, 0f, b, alpha);
            addVertex(consumer, matrix, (float)(pos1.x + perpOuter.x), (float)(pos1.y + perpOuter.y), (float)(pos1.z + perpOuter.z), r, 0f, b, 0f);
            addVertex(consumer, matrix, (float)(pos2.x + perpOuter.x), (float)(pos2.y + perpOuter.y), (float)(pos2.z + perpOuter.z), r, 0f, b, 0f);
            addVertex(consumer, matrix, (float)(pos2.x + perp.x), (float)(pos2.y + perp.y), (float)(pos2.z + perp.z), r, 0f, b, alpha * 0.6f);
        }
    }

    private void renderVoidShard(VertexConsumer consumer, Matrix4f matrix, VoidShard shard) {
        float cosR  = Mth.cos(shard.rotation);
        float sinR  = Mth.sin(shard.rotation);
        float cosTX = Mth.cos(shard.tiltX);
        float sinTX = Mth.sin(shard.tiltX);
        float cosTZ = Mth.cos(shard.tiltZ);
        float sinTZ = Mth.sin(shard.tiltZ);

        float hx = shard.sizeX * 0.5f;
        float hy = shard.sizeY * 0.5f;

        float[][] local = {
                {-hx * 0.4f, -hy}, {-hx, hy * 0.3f}, {0f, hy}, {hx, hy * 0.2f}, {hx * 0.3f, -hy}
        };

        float[][] world = new float[5][3];
        for (int i = 0; i < 5; i++) {
            float lx = local[i][0] * cosR - local[i][1] * sinR;
            float ly = local[i][0] * sinR + local[i][1] * cosR;
            float ty = ly * cosTX;
            float tz = ly * sinTX;
            float fx = lx * cosTZ - tz * sinTZ;
            float fz = lx * sinTZ + tz * cosTZ;
            world[i][0] = shard.x + fx;
            world[i][1] = shard.y + ty;
            world[i][2] = shard.z + fz;
        }

        float glint = 0.85f + 0.15f * Mth.sin(currentTick * 0.22f + shard.phaseOffset * Mth.TWO_PI);
        float cr = 0.3f * glint;
        float cb = 0.6f * glint;

        addVertex(consumer, matrix, world[0][0], world[0][1], world[0][2], 0.05f, 0f, 0.12f, shard.alpha);
        addVertex(consumer, matrix, world[1][0], world[1][1], world[1][2], cr, 0f, cb, shard.alpha);
        addVertex(consumer, matrix, world[2][0], world[2][1], world[2][2], cr * 1.3f, 0f, cb, shard.alpha * 0.9f);
        addVertex(consumer, matrix, world[3][0], world[3][1], world[3][2], cr, 0f, cb, shard.alpha);

        addVertex(consumer, matrix, world[0][0], world[0][1], world[0][2], 0.05f, 0f, 0.12f, shard.alpha);
        addVertex(consumer, matrix, world[3][0], world[3][1], world[3][2], cr, 0f, cb, shard.alpha);
        addVertex(consumer, matrix, world[4][0], world[4][1], world[4][2], 0.05f, 0f, 0.12f, shard.alpha);
        addVertex(consumer, matrix, world[2][0], world[2][1], world[2][2], cr * 1.3f, 0f, cb, shard.alpha * 0.6f);
    }

    private void renderObsidianRing(VertexConsumer consumer, Matrix4f matrix, Matrix3f normalMatrix, ObsidianRing ring) {
        int segments = 48;
        float uvTiles = 8f;

        float nx = -Mth.cos(ring.tiltX) * Mth.sin(ring.tiltZ);
        float ny =  Mth.cos(ring.tiltX) * Mth.cos(ring.tiltZ);
        float nz =  Mth.sin(ring.tiltX);

        for (int i = 0; i < segments; i++) {
            float phi1 = (i       / (float) segments) * Mth.TWO_PI + ring.spinAngle;
            float phi2 = ((i + 1) / (float) segments) * Mth.TWO_PI + ring.spinAngle;

            float u1 = (i       / (float) segments) * uvTiles;
            float u2 = ((i + 1) / (float) segments) * uvTiles;

            float[] innerA = tiltPoint(Mth.cos(phi1) * ring.innerRadius, 0, Mth.sin(phi1) * ring.innerRadius, ring.tiltX, ring.tiltZ);
            float[] outerA = tiltPoint(Mth.cos(phi1) * ring.outerRadius, 0, Mth.sin(phi1) * ring.outerRadius, ring.tiltX, ring.tiltZ);
            float[] innerB = tiltPoint(Mth.cos(phi2) * ring.innerRadius, 0, Mth.sin(phi2) * ring.innerRadius, ring.tiltX, ring.tiltZ);
            float[] outerB = tiltPoint(Mth.cos(phi2) * ring.outerRadius, 0, Mth.sin(phi2) * ring.outerRadius, ring.tiltX, ring.tiltZ);

            addTexVertex(consumer, matrix, normalMatrix, innerA[0], innerA[1], innerA[2], u1, 0f, nx, ny, nz);
            addTexVertex(consumer, matrix, normalMatrix, outerA[0], outerA[1], outerA[2], u1, 1f, nx, ny, nz);
            addTexVertex(consumer, matrix, normalMatrix, outerB[0], outerB[1], outerB[2], u2, 1f, nx, ny, nz);
            addTexVertex(consumer, matrix, normalMatrix, innerB[0], innerB[1], innerB[2], u2, 0f, nx, ny, nz);

            addTexVertex(consumer, matrix, normalMatrix, innerB[0], innerB[1], innerB[2], u2, 0f, -nx, -ny, -nz);
            addTexVertex(consumer, matrix, normalMatrix, outerB[0], outerB[1], outerB[2], u2, 1f, -nx, -ny, -nz);
            addTexVertex(consumer, matrix, normalMatrix, outerA[0], outerA[1], outerA[2], u1, 1f, -nx, -ny, -nz);
            addTexVertex(consumer, matrix, normalMatrix, innerA[0], innerA[1], innerA[2], u1, 0f, -nx, -ny, -nz);
        }
    }

    private float[] tiltPoint(float x, float y, float z, float tiltX, float tiltZ) {
        float ry = y * Mth.cos(tiltX) - z * Mth.sin(tiltX);
        float rz = y * Mth.sin(tiltX) + z * Mth.cos(tiltX);
        float fx = x * Mth.cos(tiltZ) - ry * Mth.sin(tiltZ);
        float fy = x * Mth.sin(tiltZ) + ry * Mth.cos(tiltZ);
        return new float[]{fx, fy, rz};
    }

    private float p1f(float base, Vec3 perp, char axis, int sign) {
        double v = switch (axis) {
            case 'x' -> perp.x;
            case 'y' -> perp.y;
            default  -> perp.z;
        };
        return base + (float) v * sign;
    }

    private Vec3 getPerp(Vec3 dir) {
        Vec3 up = Math.abs(dir.y) < 0.85f ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        return dir.cross(up).normalize();
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                           float r, float g, float b, float a) {
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
    }

    private void addTexVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normalMatrix,
                              float x, float y, float z, float u, float v,
                              float nx, float ny, float nz) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(nx, ny, nz);
    }

    private void renderBillboardQuad(VertexConsumer consumer, Matrix4f matrix,
                                     float x, float y, float z, float size,
                                     float r, float g, float b, float a) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        Vec3 toCamera = new Vec3(
                cameraPos.x - (getX() + x),
                cameraPos.y - (getY() + y),
                cameraPos.z - (getZ() + z)
        ).normalize();

        Vec3 up    = new Vec3(0, 1, 0);
        Vec3 right = toCamera.cross(up).normalize().scale(size);
        up         = right.cross(toCamera).normalize().scale(size);

        addVertex(consumer, matrix, (float)(x - right.x - up.x), (float)(y - right.y - up.y), (float)(z - right.z - up.z), r, g, b, a);
        addVertex(consumer, matrix, (float)(x - right.x + up.x), (float)(y - right.y + up.y), (float)(z - right.z + up.z), r, g, b, a);
        addVertex(consumer, matrix, (float)(x + right.x + up.x), (float)(y + right.y + up.y), (float)(z + right.z + up.z), r, g, b, a);
        addVertex(consumer, matrix, (float)(x + right.x - up.x), (float)(y + right.y - up.y), (float)(z + right.z - up.z), r, g, b, a);
    }

    private class VoidCrack {
        float[][] points;
        float width;
        float alpha;
        float phaseOffset;
        float currentAngleH;
        float currentAngleV;
        private final float angleHSpeed;
        private final float angleVSpeed;

        VoidCrack(int index) {
            this.phaseOffset   = index / 14f;
            this.width         = 0.07f + random.nextFloat() * 0.09f;
            this.currentAngleH = random.nextFloat() * Mth.TWO_PI;
            this.currentAngleV = (random.nextFloat() - 0.5f) * Mth.PI;
            this.angleHSpeed   = (random.nextFloat() - 0.5f) * 0.012f;
            this.angleVSpeed   = (random.nextFloat() - 0.5f) * 0.009f;
            int numPoints = 7 + random.nextInt(5);
            this.points = new float[numPoints][3];
            buildPoints();
        }

        private void buildPoints() {
            float dirX = Mth.cos(currentAngleV) * Mth.cos(currentAngleH);
            float dirY = Mth.sin(currentAngleV);
            float dirZ = Mth.cos(currentAngleV) * Mth.sin(currentAngleH);
            Vec3 dir   = new Vec3(dirX, dirY, dirZ);
            Vec3 perp1 = getPerp(dir);
            Vec3 perp2 = dir.cross(perp1).normalize();
            points[0][0] = 0; points[0][1] = 0; points[0][2] = 0;
            float stepLen = 8f / points.length;
            for (int i = 1; i < points.length; i++) {
                float jag1 = (random.nextFloat() - 0.5f) * 1.4f;
                float jag2 = (random.nextFloat() - 0.5f) * 1.4f;
                points[i][0] = points[i-1][0] + dirX * stepLen + (float)(perp1.x * jag1 + perp2.x * jag2);
                points[i][1] = points[i-1][1] + dirY * stepLen + (float)(perp1.y * jag1 + perp2.y * jag2);
                points[i][2] = points[i-1][2] + dirZ * stepLen + (float)(perp1.z * jag1 + perp2.z * jag2);
            }
        }

        void update(float tick, float loopProgress) {
            float adjustedProgress = (loopProgress + phaseOffset) % 1f;
            this.alpha = 0.9f * Mth.sin(adjustedProgress * Mth.PI);
            currentAngleH += angleHSpeed;
            currentAngleV  = Mth.clamp(currentAngleV + angleVSpeed, -Mth.PI * 0.48f, Mth.PI * 0.48f);
            if (adjustedProgress < 0.05f) buildPoints();
        }
    }

    private class RiftTendril {
        float baseAngleH;
        float baseAngleV;
        float length;
        float width;
        float alpha;
        float phaseOffset;
        float waveFreqA;
        float waveAmpA;
        float waveFreqB;
        float waveAmpB;

        RiftTendril(int index) {
            this.phaseOffset = index / 10f;
            this.baseAngleH  = (index / 10f) * Mth.TWO_PI + random.nextFloat() * 0.6f;
            this.baseAngleV  = (random.nextFloat() - 0.5f) * Mth.PI * 0.75f;
            this.width       = 0.18f + random.nextFloat() * 0.12f;
            this.length      = 4f + random.nextFloat() * 4f;
            this.waveFreqA   = 1.5f + random.nextFloat() * 2f;
            this.waveAmpA    = 0.5f + random.nextFloat() * 0.6f;
            this.waveFreqB   = 2.0f + random.nextFloat() * 1.5f;
            this.waveAmpB    = 0.35f + random.nextFloat() * 0.4f;
        }

        void update(float tick, float loopProgress) {
            float adjustedProgress = (loopProgress + phaseOffset) % 1f;
            this.alpha = 0.8f * Mth.sin(adjustedProgress * Mth.PI);
        }

        Vec3 getPosition(float t, float tick) {
            float dirX = Mth.cos(baseAngleV) * Mth.cos(baseAngleH);
            float dirY = Mth.sin(baseAngleV);
            float dirZ = Mth.cos(baseAngleV) * Mth.sin(baseAngleH);
            Vec3 dir   = new Vec3(dirX, dirY, dirZ);
            Vec3 right = getPerp(dir);
            Vec3 up2   = dir.cross(right).normalize();
            float wA   = Mth.sin(t * waveFreqA * Mth.TWO_PI + tick * 0.14f) * waveAmpA * t;
            float wB   = Mth.cos(t * waveFreqB * Mth.PI     + tick * 0.10f) * waveAmpB * t;
            float dist = t * length;
            return new Vec3(
                    dirX * dist + right.x * wA + up2.x * wB,
                    dirY * dist + right.y * wA + up2.y * wB,
                    dirZ * dist + right.z * wA + up2.z * wB
            );
        }
    }

    private class VoidShard {
        float x, y, z;
        float sizeX, sizeY;
        float rotation;
        float tiltX, tiltZ;
        float rotSpeed;
        float alpha;
        float phaseOffset;
        float orbitAngle;
        float orbitRadius;
        float orbitAltitude;
        float orbitSpeed;

        VoidShard() {
            this.phaseOffset   = random.nextFloat();
            this.sizeX         = 0.18f + random.nextFloat() * 0.28f;
            this.sizeY         = 0.25f + random.nextFloat() * 0.45f;
            this.rotSpeed      = (random.nextFloat() - 0.5f) * 0.09f;
            this.tiltX         = random.nextFloat() * Mth.PI;
            this.tiltZ         = random.nextFloat() * Mth.TWO_PI;
            this.orbitAngle    = random.nextFloat() * Mth.TWO_PI;
            this.orbitRadius   = 1.2f + random.nextFloat() * 6.5f;
            this.orbitAltitude = (random.nextFloat() - 0.5f) * 5f;
            this.orbitSpeed    = 0.012f + random.nextFloat() * 0.018f;
        }

        void update(float tick, float loopProgress) {
            float adjustedProgress = (loopProgress + phaseOffset) % 1f;
            this.alpha       = 0.75f * Mth.sin(adjustedProgress * Mth.PI);
            this.orbitAngle += orbitSpeed;
            this.rotation   += rotSpeed;
            this.tiltX      += rotSpeed * 0.7f;
            this.tiltZ      += rotSpeed * 0.4f;
            float drift = Mth.sin(tick * 0.035f + phaseOffset * Mth.TWO_PI) * 0.8f;
            this.x = Mth.cos(orbitAngle) * orbitRadius;
            this.y = orbitAltitude + drift;
            this.z = Mth.sin(orbitAngle) * orbitRadius;
        }
    }

    private class SpaceParticle {
        float x, y, z;
        float angle;
        float radius;
        float vertAngle;
        float size;
        float r, g, b;
        float alpha;
        float phaseOffset;
        float orbitSpeed;

        SpaceParticle() {
            this.phaseOffset = random.nextFloat();
            this.angle       = random.nextFloat() * Mth.TWO_PI;
            this.vertAngle   = (random.nextFloat() - 0.5f) * Mth.PI;
            this.radius      = 0.3f + random.nextFloat() * 7.5f;
            this.size        = 0.04f + random.nextFloat() * 0.07f;
            this.orbitSpeed  = 0.009f + random.nextFloat() * 0.022f;
            if (random.nextFloat() > 0.55f) {
                this.r = 0.45f; this.g = 0.0f; this.b = 0.8f;
            } else {
                this.r = 0.1f; this.g = 0.05f; this.b = 0.55f;
            }
        }

        void update(float tick, float loopProgress) {
            float adjustedProgress = (loopProgress + phaseOffset) % 1f;
            this.alpha  = 0.8f * Mth.sin(adjustedProgress * Mth.PI);
            this.angle += orbitSpeed;
            float cosV  = Mth.cos(vertAngle);
            this.x = cosV * Mth.cos(angle) * radius;
            this.y = Mth.sin(vertAngle) * radius;
            this.z = cosV * Mth.sin(angle) * radius;
        }
    }

    private class ShockwavePulse {
        float radius;
        float alpha;
        float phaseOffset;
        float tiltX;
        float tiltZ;
        float tiltOffset;
        float height;

        ShockwavePulse(int index) {
            this.phaseOffset = index / 4f;
            this.tiltX       = (random.nextFloat() - 0.5f) * Mth.PI * 0.7f;
            this.tiltZ       = random.nextFloat() * Mth.TWO_PI;
            this.tiltOffset  = random.nextFloat() * Mth.TWO_PI;
            this.height      = (random.nextFloat() - 0.5f) * 1.5f;
        }

        void update(float tick, float loopProgress) {
            float adjustedProgress = (loopProgress + phaseOffset) % 1f;
            this.radius = adjustedProgress * 9f;
            this.alpha  = 0.75f * Mth.sin(adjustedProgress * Mth.PI) * (1f - adjustedProgress * 0.5f);
        }
    }

    private class EnergyNode {
        float x, y, z;
        float angle;
        float orbitRadius;
        float orbitSpeed;
        float orbitPlaneAngle;
        float size;
        float r, b;
        float alpha;

        EnergyNode(int index) {
            this.orbitPlaneAngle = (index / 6f) * Mth.PI;
            this.angle           = (index / 6f) * Mth.TWO_PI;
            this.orbitRadius     = 1.1f + random.nextFloat() * 0.5f;
            this.orbitSpeed      = 0.08f + random.nextFloat() * 0.06f;
            this.size            = 0.1f + random.nextFloat() * 0.07f;
            this.r               = 0.4f + random.nextFloat() * 0.3f;
            this.b               = 0.7f + random.nextFloat() * 0.3f;
        }

        void update(float tick) {
            this.angle += orbitSpeed;
            float cosP  = Mth.cos(orbitPlaneAngle);
            float sinP  = Mth.sin(orbitPlaneAngle);
            float baseX = Mth.cos(angle) * orbitRadius;
            float baseY = Mth.sin(angle) * orbitRadius;
            this.x     = baseX;
            this.y     = baseY * cosP;
            this.z     = baseY * sinP;
            this.alpha = 0.7f + 0.3f * Mth.sin(tick * 0.28f + orbitPlaneAngle);
        }
    }

    private static class ObsidianRing {
        float innerRadius;
        float outerRadius;
        float spinSpeed;
        float spinAngle;
        float tiltX;
        float tiltZ;
        final float precessSpeedX;
        final float precessSpeedZ;

        ObsidianRing(float innerRadius, float outerRadius,
                     float spinSpeed,
                     float precessSpeedX, float precessSpeedZ) {
            this.innerRadius   = innerRadius;
            this.outerRadius   = outerRadius;
            this.spinSpeed     = spinSpeed;
            this.spinAngle     = 0f;
            this.tiltX         = (float)(Math.random() * Mth.TWO_PI);
            this.tiltZ         = (float)(Math.random() * Mth.TWO_PI);
            this.precessSpeedX = precessSpeedX;
            this.precessSpeedZ = precessSpeedZ;
        }

        void update(float tick) {
            spinAngle = tick * spinSpeed;
            tiltX    += precessSpeedX;
            tiltZ    += precessSpeedZ;
        }
    }
}