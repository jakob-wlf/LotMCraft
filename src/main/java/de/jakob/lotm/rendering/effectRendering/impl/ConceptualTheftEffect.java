package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class ConceptualTheftEffect extends ActiveEffect {

    private final RandomSource random = RandomSource.create();
    private final List<ConceptFragment> fragments = new ArrayList<>();
    private final List<SiphonLine> siphonLines = new ArrayList<>();

    // Subtle purple theme - mysterious and conceptual
    private static final float[] MAIN_COLOR = {0.45f, 0.2f, 0.65f};      // Deep purple
    private static final float[] DRAIN_COLOR = {0.25f, 0.1f, 0.4f};      // Darker purple
    private static final float[] ESSENCE_COLOR = {0.7f, 0.5f, 0.9f};     // Light purple glow

    public ConceptualTheftEffect(double x, double y, double z) {
        super(x, y, z, 30);

        // Create concept fragments that represent stolen abilities
        for (int i = 0; i < 50; i++) {
            fragments.add(new ConceptFragment());
        }

        // Create siphon lines that pull essence inward
        for (int i = 0; i < 16; i++) {
            siphonLines.add(new SiphonLine(i));
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        float progress = getProgress();
        float intensity = (float) Math.max(0f, 1f - Math.pow(progress, 0.4));

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // Render the central void - where concepts are being absorbed
        renderCentralVoid(poseStack, progress, intensity);

        // Render siphon lines pulling inward
        renderSiphonLines(poseStack, progress, intensity);

        // Render fragmenting concepts being stolen
        renderConceptFragments(poseStack, progress, intensity);

        // Render the absorption ring
        renderAbsorptionRing(poseStack, progress, intensity);

        poseStack.popPose();
    }

    private void renderCentralVoid(PoseStack poseStack, float progress, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        float radius = 0.8f + intensity * 0.25f;
        int segments = 16;

        // Dark central sphere representing the void
        Tesselator tesselator = Tesselator.getInstance();

        for (int lat = 0; lat < segments; lat++) {
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            float theta1 = (float) (lat * Math.PI / segments);
            float theta2 = (float) ((lat + 1) * Math.PI / segments);

            for (int lon = 0; lon <= segments; lon++) {
                float phi = (float) (lon * 2 * Math.PI / segments);

                Vec3 v1 = spherePoint(radius, theta1, phi);
                Vec3 v2 = spherePoint(radius, theta2, phi);

                float alpha = intensity * 0.8f;

                buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z)
                        .setColor(DRAIN_COLOR[0], DRAIN_COLOR[1], DRAIN_COLOR[2], alpha);
                buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z)
                        .setColor(DRAIN_COLOR[0], DRAIN_COLOR[1], DRAIN_COLOR[2], alpha);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderSiphonLines(PoseStack poseStack, float progress, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (SiphonLine line : siphonLines) {
            line.update(progress, intensity);

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            int segments = 12;
            for (int i = 0; i <= segments; i++) {
                float t = i / (float) segments;
                Vec3 pos = line.getPosition(t);
                float width = 0.02f * (1f - t) * line.alpha;
                float alpha = line.alpha * (1f - t * 0.5f);

                Vec3 perpendicular = new Vec3(
                        -line.direction.z,
                        0,
                        line.direction.x
                ).normalize().scale(width);

                buffer.addVertex(matrix,
                                (float)(pos.x + perpendicular.x),
                                (float)(pos.y + perpendicular.y),
                                (float)(pos.z + perpendicular.z))
                        .setColor(ESSENCE_COLOR[0], ESSENCE_COLOR[1], ESSENCE_COLOR[2], alpha);

                buffer.addVertex(matrix,
                                (float)(pos.x - perpendicular.x),
                                (float)(pos.y - perpendicular.y),
                                (float)(pos.z - perpendicular.z))
                        .setColor(ESSENCE_COLOR[0], ESSENCE_COLOR[1], ESSENCE_COLOR[2], alpha);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderConceptFragments(PoseStack poseStack, float progress, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (ConceptFragment fragment : fragments) {
            fragment.update(progress, intensity);

            if (fragment.alpha <= 0.01f) continue;

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            // Render as small diamond shapes
            float size = fragment.size;
            Vec3 pos = fragment.pos;

            buffer.addVertex(matrix, (float)(pos.x), (float)(pos.y + size), (float)(pos.z))
                    .setColor(MAIN_COLOR[0], MAIN_COLOR[1], MAIN_COLOR[2], fragment.alpha);
            buffer.addVertex(matrix, (float)(pos.x + size), (float)(pos.y), (float)(pos.z))
                    .setColor(MAIN_COLOR[0], MAIN_COLOR[1], MAIN_COLOR[2], fragment.alpha);
            buffer.addVertex(matrix, (float)(pos.x), (float)(pos.y - size), (float)(pos.z))
                    .setColor(MAIN_COLOR[0], MAIN_COLOR[1], MAIN_COLOR[2], fragment.alpha);
            buffer.addVertex(matrix, (float)(pos.x - size), (float)(pos.y), (float)(pos.z))
                    .setColor(MAIN_COLOR[0], MAIN_COLOR[1], MAIN_COLOR[2], fragment.alpha);

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderAbsorptionRing(PoseStack poseStack, float progress, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        float innerRadius = 0.5f;
        float outerRadius = 1.6f + (float)Math.sin(progress * Math.PI * 3) * 0.25f;
        int segments = 32;
        float ringAlpha = intensity * 0.3f;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= segments; i++) {
            float angle = (float) (i * 2 * Math.PI / segments - progress * Math.PI * 2);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float fade = (float)Math.abs(Math.sin(i * Math.PI / segments));
            float alpha = ringAlpha * fade;

            buffer.addVertex(matrix, cos * innerRadius, 0, sin * innerRadius)
                    .setColor(ESSENCE_COLOR[0], ESSENCE_COLOR[1], ESSENCE_COLOR[2], alpha * 0.5f);
            buffer.addVertex(matrix, cos * outerRadius, 0, sin * outerRadius)
                    .setColor(ESSENCE_COLOR[0], ESSENCE_COLOR[1], ESSENCE_COLOR[2], 0f);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private Vec3 spherePoint(float radius, float theta, float phi) {
        float x = (float) (radius * Math.sin(theta) * Math.cos(phi));
        float y = (float) (radius * Math.cos(theta));
        float z = (float) (radius * Math.sin(theta) * Math.sin(phi));
        return new Vec3(x, y, z);
    }

    private class ConceptFragment {
        Vec3 pos;
        Vec3 velocity;
        Vec3 targetOffset;
        float alpha;
        float size;
        float lifetime;
        float maxLifetime;
        float dissolveFactor;

        ConceptFragment() {
            reset();
        }

        void reset() {
            float theta = random.nextFloat() * (float) Math.PI;
            float phi = random.nextFloat() * (float) Math.PI * 2;
            float dist = 2.0f + random.nextFloat() * 1.2f;

            pos = new Vec3(
                    Math.sin(theta) * Math.cos(phi) * dist,
                    (random.nextFloat() - 0.5) * 2.0,
                    Math.sin(theta) * Math.sin(phi) * dist
            );

            targetOffset = new Vec3(
                    (random.nextDouble() - 0.5) * 0.3,
                    (random.nextDouble() - 0.5) * 0.3,
                    (random.nextDouble() - 0.5) * 0.3
            );

            velocity = pos.normalize().scale(-0.03);
            size = 0.05f + random.nextFloat() * 0.07f;
            maxLifetime = 60f + random.nextFloat() * 120f;
            lifetime = random.nextFloat() * 30f;
            alpha = 0f;
            dissolveFactor = random.nextFloat();
        }

        void update(float expansion, float intensity) {
            lifetime++;

            float progress = lifetime / maxLifetime;

            if (progress > 1f) {
                reset();
                return;
            }

            // Pull toward center with spiral motion
            Vec3 toCenter = targetOffset.subtract(pos).normalize();
            velocity = velocity.add(toCenter.scale(0.04));

            // Add slight spiral
            float spiralAngle = lifetime * 0.05f;
            Vec3 spiral = new Vec3(
                    Math.cos(spiralAngle) * 0.01,
                    0,
                    Math.sin(spiralAngle) * 0.01
            );

            pos = pos.add(velocity.add(spiral));

            // Fade in, sustain, then dissolve
            float fadeIn = Mth.clamp(progress * 3f, 0f, 1f);
            float fadeOut = 1f - Mth.clamp((progress - 0.7f) * 3.3f, 0f, 1f);
            float flicker = (float)(Math.sin(lifetime * 0.2 + dissolveFactor * Math.PI * 2) * 0.3 + 0.7);

            alpha = intensity * fadeIn * fadeOut * flicker;

            // Shrink as it gets absorbed
            size = (0.05f + random.nextFloat() * 0.07f) * (1f - progress * 0.5f);
        }
    }

    private class SiphonLine {
        Vec3 startPos;
        Vec3 direction;
        float alpha;
        float phase;
        float speed;

        SiphonLine(int index) {
            float angle = (float) (index * 2 * Math.PI / 16);
            float dist = 2.6f;
            startPos = new Vec3(
                    Math.cos(angle) * dist,
                    (random.nextFloat() - 0.5) * 0.8,
                    Math.sin(angle) * dist
            );
            direction = startPos.normalize().scale(-1);
            phase = random.nextFloat() * (float)Math.PI * 2;
            speed = 0.8f + random.nextFloat() * 0.4f;
        }

        void update(float progress, float intensity) {
            float wave = (float)Math.sin(progress * Math.PI * 4 * speed + phase);
            alpha = intensity * (wave * 0.3f + 0.5f);
        }

        Vec3 getPosition(float t) {
            // Curved path from outer to center
            Vec3 straightPath = startPos.add(direction.scale(startPos.length() * t));

            // Add curve
            float curvature = (float)Math.sin(t * Math.PI) * 0.3f;
            Vec3 perpendicular = new Vec3(-direction.z, 0, direction.x).scale(curvature);

            return straightPath.add(perpendicular);
        }
    }
}