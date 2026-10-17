package de.jakob.lotm.entity.client.ability_entities.twilight_giant.twilight_visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ability_entities.twilight_giant.ProtectionBarrierEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ProtectionBarrierRenderer extends EntityRenderer<ProtectionBarrierEntity> {
    private static final ResourceLocation SHELL = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/protection_barrier_shell.png");
    private static final ResourceLocation RUNES = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/protection_barrier_runes.png");
    private static final ResourceLocation OUTLINE = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/protection_barrier_runes_outline.png");
    private static final int LAT = 20;
    private static final int LON = 64;
    private static final float[][] X = new float[LAT + 1][LON + 1];
    private static final float[][] Y = new float[LAT + 1][LON + 1];
    private static final float[][] Z = new float[LAT + 1][LON + 1];

    static {
        for (int i = 0; i <= LAT; i++) {
            double th = i / (double) LAT * Math.PI / 2;
            for (int j = 0; j <= LON; j++) {
                double ph = -j / (double) LON * Math.PI * 2;
                X[i][j] = (float) (Math.sin(th) * Math.cos(ph));
                Y[i][j] = (float) Math.cos(th);
                Z[i][j] = (float) (Math.sin(th) * Math.sin(ph));
            }
        }
    }

    public ProtectionBarrierRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(ProtectionBarrierEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(ProtectionBarrierEntity entity) {
        return SHELL;
    }

    @Override
    public void render(ProtectionBarrierEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        if (entity.isRemoved() || entity.closeAt() >= 0) return;
        float age = entity.tickCount + partialTick;
        float grow = Mth.clamp(age / ProtectionBarrierEntity.GROW_TICKS, 0f, 1f);
        float ease = 1f - (1f - grow) * (1f - grow) * (1f - grow);
        int closeAt = entity.closeAt();
        float out = 1f;
        if (closeAt >= 0) {
            float since = age - closeAt;
            out = Mth.clamp(1f - since / ProtectionBarrierEntity.FADE_TICKS, 0f, 1f);
        }
        float radius = entity.radius();
        float scale = Math.max(ease * out, 0.0001f) * radius;
        float runeFade = Mth.clamp((grow - 0.5f) / 0.5f, 0f, 1f) * out;

        poseStack.pushPose();
        poseStack.translate(0f, radius * (1f - ease), 0f);
        poseStack.scale(scale, scale, scale);
        PoseStack.Pose pose = poseStack.last();

        dome(buffers.getBuffer(RenderType.entityTranslucentEmissive(SHELL)), pose, 0, age, ease, runeFade);
        glowDome(buffers.getBuffer(RenderType.eyes(SHELL)), pose, age, ease, out);
        if (runeFade > 0.01f) {
            dome(buffers.getBuffer(RenderType.entityTranslucentEmissive(OUTLINE)), pose, 2, age, ease, runeFade);
            dome(buffers.getBuffer(RenderType.eyes(RUNES)), pose, 1, age, ease, runeFade);
        }
        poseStack.popPose();
    }

    private static void dome(VertexConsumer consumer, PoseStack.Pose pose, int mode, float age, float ease, float fade) {
        for (int i = 0; i < LAT; i++) {
            for (int j = 0; j < LON; j++) {
                vert(consumer, pose, i, j, 1, mode, age, ease, fade);
                vert(consumer, pose, i + 1, j, 1, mode, age, ease, fade);
                vert(consumer, pose, i + 1, j + 1, 1, mode, age, ease, fade);
                vert(consumer, pose, i, j + 1, 1, mode, age, ease, fade);
                vert(consumer, pose, i, j + 1, -1, mode, age, ease, fade);
                vert(consumer, pose, i + 1, j + 1, -1, mode, age, ease, fade);
                vert(consumer, pose, i + 1, j, -1, mode, age, ease, fade);
                vert(consumer, pose, i, j, -1, mode, age, ease, fade);
            }
        }
    }

    private static void glowDome(VertexConsumer consumer, PoseStack.Pose pose, float age, float ease, float out) {
        for (int i = 0; i < LAT; i++) {
            for (int j = 0; j < LON; j++) {
                glowVert(consumer, pose, i, j, 1, age, ease, out);
                glowVert(consumer, pose, i + 1, j, 1, age, ease, out);
                glowVert(consumer, pose, i + 1, j + 1, 1, age, ease, out);
                glowVert(consumer, pose, i, j + 1, 1, age, ease, out);
                glowVert(consumer, pose, i, j + 1, -1, age, ease, out);
                glowVert(consumer, pose, i + 1, j + 1, -1, age, ease, out);
                glowVert(consumer, pose, i + 1, j, -1, age, ease, out);
                glowVert(consumer, pose, i, j, -1, age, ease, out);
            }
        }
    }

    private static void glowVert(VertexConsumer consumer, PoseStack.Pose pose, int i, int j, int side, float age, float ease, float out) {
        float pulse = 0.55f + 0.12f * Mth.sin(age * 0.15f + j * 0.45f + i * 0.35f);
        int color = (int) (255 * Mth.clamp(pulse * ease * out, 0f, 1f));
        consumer.addVertex(pose, X[i][j] * 1.003f, Y[i][j] * 1.003f, Z[i][j] * 1.003f)
                .setColor(color, color, color, 255)
                .setUv(j * 4f / LON, i * 3f / LAT)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, X[i][j] * side, Y[i][j] * side, Z[i][j] * side);
    }

    private static void vert(VertexConsumer consumer, PoseStack.Pose pose, int i, int j, int side, int mode, float age, float ease, float fade) {
        float lift = mode == 1 ? 1.006f : mode == 2 ? 1.0045f : 1f;
        float x = X[i][j] * lift;
        float y = Y[i][j] * lift;
        float z = Z[i][j] * lift;
        float u;
        float v;
        int r;
        int g;
        int b;
        int a;
        if (mode == 2) {
            u = j / (float) LON;
            v = i * 1.5f / LAT - age * 0.012f;
            r = 255;
            g = 255;
            b = 255;
            a = (int) (255 * fade);
        } else if (mode == 1) {
            u = j / (float) LON;
            v = i * 1.5f / LAT - age * 0.012f;
            float wave = 0.55f + 0.45f * Mth.sin(age * 0.28f - i * 0.55f);
            float spark = 0.18f * Mth.sin(age * 0.55f + j * 1.7f + i * 0.9f);
            float tone = Mth.clamp((0.75f + 0.25f * wave + spark) * fade, 0f, 1f);
            float hue = 0.5f + 0.5f * Mth.sin(age * 0.12f - i * 0.4f);
            r = (int) (255 * tone);
            g = (int) (255 * tone * (0.42f + 0.33f * hue));
            b = (int) (255 * tone * 0.12f);
            a = 255;
        } else {
            u = j * 4f / LON;
            v = i * 3f / LAT;
            float shimmer = 1.08f + 0.07f * Mth.sin(age * 0.15f + j * 0.45f + i * 0.35f);
            r = (int) (255 * Mth.clamp(Mth.lerp(ease, 1.0f, 0.97f) * shimmer, 0f, 1f));
            g = (int) (255 * Mth.clamp(Mth.lerp(ease, 1.0f, 0.98f) * shimmer, 0f, 1f));
            b = (int) (255 * Mth.clamp(Mth.lerp(ease, 1.0f, 1.00f) * shimmer, 0f, 1f));
            a = (int) (255 * Mth.lerp(ease, 0.9f, 0.62f));
        }
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, X[i][j] * side, Y[i][j] * side, Z[i][j] * side);
    }
}
