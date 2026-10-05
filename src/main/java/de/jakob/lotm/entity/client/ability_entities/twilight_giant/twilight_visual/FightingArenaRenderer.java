package de.jakob.lotm.entity.client.ability_entities.twilight_giant.twilight_visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.ability_entities.twilight_giant.twilight_visual.TwilightDomeGeo.UV;
import de.jakob.lotm.entity.custom.ability_entities.twilight_giant.FightingArenaEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FightingArenaRenderer extends EntityRenderer<FightingArenaEntity> {
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/twilight_visual/arena.png");
    private static final UV SIDE = UV.px(0, 0, 128, 128, 256, 128);
    private static final UV TOP = UV.px(128, 0, 192, 64, 256, 128);
    private static final UV DOOR = UV.px(192, 0, 256, 64, 256, 128);
    private static final UV BANNER = UV.px(128, 64, 192, 128, 256, 128);
    private static final UV FLAME = UV.px(192, 64, 256, 128, 256, 128);
    private static final int N = 48;
    private static final int SEAT_SHADE = 0xFFD2C8C8;
    private static final float R_PODIUM = 6.0F;
    private static final float H_PODIUM = 1.2F;
    private static final float R_TIER1 = 7.4F;
    private static final float H_TIER2 = 2.1F;
    private static final float R_TIER2 = 8.8F;
    private static final float H_WALL = 3.4F;
    private static final float R_OUT = FightingArenaEntity.NATIVE_RADIUS;

    public FightingArenaRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(FightingArenaEntity entity) {
        return TEX;
    }

    @Override
    public boolean shouldRender(FightingArenaEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(FightingArenaEntity arena, float yaw, float partial, PoseStack ps, MultiBufferSource buffers, int packedLight) {
        float age = arena.getAge(partial);
        int light = TwilightDomeGeo.lift(packedLight);
        VertexConsumer c = buffers.getBuffer(RenderType.entityCutoutNoCull(TEX));
        float fit = arena.radius() / FightingArenaEntity.NATIVE_RADIUS;

        ps.pushPose();
        ps.scale(fit, fit * arena.rise(partial), fit);
        PoseStack.Pose pose = ps.last();

        wall(c, pose, light, R_PODIUM, 0.0F, H_PODIUM, false);
        ring(c, pose, light, R_PODIUM, R_TIER1, H_PODIUM, true);
        wall(c, pose, light, R_TIER1, H_PODIUM, H_TIER2, false);
        ring(c, pose, light, R_TIER1, R_TIER2, H_TIER2, true);
        wall(c, pose, light, R_TIER2, H_TIER2, H_WALL, false);
        ring(c, pose, light, R_TIER2, R_OUT, H_WALL, false);
        wall(c, pose, light, R_OUT, 0.0F, H_WALL, true);

        for (int k = 0; k < 8; k++) {
            float deg = 22.5F + 45.0F * k;
            localAt(ps, 9.4F, deg);
            PoseStack.Pose lp = ps.last();
            TwilightDomeGeo.box(c, lp, light, SIDE, TOP, -0.45F, H_WALL, -0.45F, 0.45F, 5.0F, 0.45F);
            TwilightDomeGeo.box(c, lp, light, SIDE, TOP, -0.6F, 5.0F, -0.6F, 0.6F, 5.3F, 0.6F);
            ps.popPose();
        }

        for (int k = 0; k < 4; k++) {
            localAt(ps, 0.0F, 90.0F * k);
            PoseStack.Pose lp = ps.last();
            float x = R_OUT + 0.03F;
            TwilightDomeGeo.quad(c, lp, light, TwilightDomeGeo.WHITE, DOOR, x, 2.8F, 1.5F, x, 0.0F, 1.5F, x, 0.0F, -1.5F, x, 2.8F, -1.5F, 1, 0, 0);
            ps.popPose();
        }

        for (int k = 0; k < 8; k++) {
            localAt(ps, 0.0F, 45.0F * k);
            PoseStack.Pose lp = ps.last();
            float x = R_TIER2 - 0.03F;
            TwilightDomeGeo.quad(c, lp, light, TwilightDomeGeo.WHITE, BANNER, x, 3.3F, 0.45F, x, 2.25F, 0.45F, x, 2.25F, -0.45F, x, 3.3F, -0.45F, -1, 0, 0);
            ps.popPose();
        }

        int fire = LightTexture.FULL_BRIGHT;
        for (int k = 0; k < 4; k++) {
            localAt(ps, 5.2F, 45.0F + 90.0F * k);
            PoseStack.Pose lp = ps.last();
            TwilightDomeGeo.box(c, lp, light, SIDE, TOP, -0.3F, 0.0F, -0.3F, 0.3F, 0.9F, 0.3F);
            TwilightDomeGeo.box(c, lp, light, SIDE, TOP, -0.45F, 0.9F, -0.45F, 0.45F, 1.1F, 0.45F);
            float h = 0.95F + 0.12F * Mth.sin(age * 0.7F + k * 1.9F);
            float w = 0.42F + 0.05F * Mth.sin(age * 0.9F + k);
            float y0 = 1.1F;
            float y1 = y0 + h;
            TwilightDomeGeo.quad(c, lp, fire, TwilightDomeGeo.WHITE, FLAME, -w, y1, 0, -w, y0, 0, w, y0, 0, w, y1, 0, 0, 0, 1);
            TwilightDomeGeo.quad(c, lp, fire, TwilightDomeGeo.WHITE, FLAME, 0, y1, -w, 0, y0, -w, 0, y0, w, 0, y1, w, 1, 0, 0);
            ps.popPose();
        }

        ps.popPose();
    }

    private static void localAt(PoseStack ps, float r, float deg) {
        float a = deg * Mth.DEG_TO_RAD;
        ps.pushPose();
        ps.translate(r * Mth.cos(a), 0.0F, r * Mth.sin(a));
        ps.mulPose(Axis.YP.rotationDegrees(-deg));
    }

    private static void wall(VertexConsumer c, PoseStack.Pose p, int light, float r, float y0, float y1, boolean outward) {
        for (int i = 0; i < N; i++) {
            float a0 = (float) (2.0 * Math.PI * i / N);
            float a1 = (float) (2.0 * Math.PI * (i + 1) / N);
            float mid = (a0 + a1) * 0.5F;
            float s = outward ? 1.0F : -1.0F;
            TwilightDomeGeo.quad(c, p, light, TwilightDomeGeo.WHITE, SIDE,
                    r * Mth.cos(a0), y1, r * Mth.sin(a0),
                    r * Mth.cos(a0), y0, r * Mth.sin(a0),
                    r * Mth.cos(a1), y0, r * Mth.sin(a1),
                    r * Mth.cos(a1), y1, r * Mth.sin(a1),
                    s * Mth.cos(mid), 0.0F, s * Mth.sin(mid));
        }
    }

    private static void ring(VertexConsumer c, PoseStack.Pose p, int light, float rIn, float rOut, float y, boolean seats) {
        for (int i = 0; i < N; i++) {
            float a0 = (float) (2.0 * Math.PI * i / N);
            float a1 = (float) (2.0 * Math.PI * (i + 1) / N);
            int color = seats && ((i / 2) % 2 == 1) ? SEAT_SHADE : TwilightDomeGeo.WHITE;
            TwilightDomeGeo.quad(c, p, light, color, TOP,
                    rIn * Mth.cos(a0), y, rIn * Mth.sin(a0),
                    rOut * Mth.cos(a0), y, rOut * Mth.sin(a0),
                    rOut * Mth.cos(a1), y, rOut * Mth.sin(a1),
                    rIn * Mth.cos(a1), y, rIn * Mth.sin(a1),
                    0, 1, 0);
        }
    }
}
