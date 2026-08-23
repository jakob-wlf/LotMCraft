package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import de.jakob.lotm.util.data.EntityLocation;
import de.jakob.lotm.util.data.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;

/**
 * Dark, wide vertical rings hovering behind the back of an entity carrying Death
 * Decree stacks, like a halo. One ring is added per stack (see STACKS_PARAM), each
 * further out than the last, so the number of rings makes the stack count readable
 * at a glance. Follows its target's position AND body yaw every frame (see render),
 * via EffectManager.playMovableEffect + updateEffectPosition, like
 * LifeAuraEffect/HorrorAuraEffect.
 *
 * Uses standard alpha blending (not RenderType.lightning()'s additive blend) since
 * additive blending cannot produce a dark mark — it can only ever brighten the
 * framebuffer, same technique as NationOfTheDeadEffect.renderDomainSurface.
 */
public class DeathDecreeRingEffect extends ActiveEffect {

    public static final int STACKS_PARAM = 6;

    private static final float BASE_RADIUS = 0.75f;
    private static final float RADIUS_STEP = 0.3f;
    private static final float RING_WIDTH = 0.18f;
    private static final int SEGMENTS = 48;
    private static final int MAX_STACKS = 3;
    private static final float BACK_OFFSET = 0.5f;
    private static final float HEIGHT_OFFSET = 1.0f;

    public DeathDecreeRingEffect(Location location, int duration, boolean infinite) {
        super(location, duration, infinite);
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        if (Minecraft.getInstance().level == null) return;

        float rawStacks = params[STACKS_PARAM];
        int stacks = Mth.clamp(Math.round(rawStacks <= 0 ? 1 : rawStacks), 1, MAX_STACKS);

        float yawDeg = 0f;
        double offsetX = 0, offsetZ = 0;
        if (getLocation() instanceof EntityLocation entityLocation) {
            Entity entity = entityLocation.getEntity();
            yawDeg = entity.getYRot();
            float yawRad = yawDeg * Mth.DEG_TO_RAD;
            // Forward is (-sin(yaw), cos(yaw)) per Entity.calculateViewVector; negate for behind.
            offsetX = Mth.sin(yawRad) * BACK_OFFSET;
            offsetZ = -Mth.cos(yawRad) * BACK_OFFSET;
        }

        poseStack.pushPose();
        poseStack.translate(getX() + offsetX, getY() + HEIGHT_OFFSET, getZ() + offsetZ);
        // Face the ring's plane toward the entity: yaw to match body rotation, then tip
        // the disc from horizontal (ground-facing) to vertical (player-facing).
        poseStack.mulPose(Axis.YP.rotationDegrees(yawDeg));
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        Matrix4f m = poseStack.last().pose();

        RenderSystem.depthMask(false);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        for (int ring = 0; ring < stacks; ring++) {
            float radius = BASE_RADIUS + ring * RADIUS_STEP;
            float rotation = tick * 0.03f * (ring % 2 == 0 ? 1f : -1f);
            renderRing(m, radius, rotation);
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    private void renderRing(Matrix4f m, float radius, float rotation) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        float innerRadius = radius;
        float outerRadius = radius + RING_WIDTH;

        for (int i = 0; i <= SEGMENTS; i++) {
            float a = (i / (float) SEGMENTS) * Mth.TWO_PI + rotation;
            float c = Mth.cos(a), s = Mth.sin(a);
            buf.addVertex(m, c * innerRadius, 0f, s * innerRadius).setColor(0.05f, 0f, 0.03f, 0.85f);
            buf.addVertex(m, c * outerRadius, 0f, s * outerRadius).setColor(0.05f, 0f, 0.03f, 0.85f);
        }

        BufferUploader.drawWithShader(buf.buildOrThrow());
    }
}
