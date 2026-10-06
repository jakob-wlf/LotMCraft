package de.jakob.lotm.entity.client.ability_entities.twilight_giant.twilight_visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.ability_entities.twilight_giant.twilight_visual.TwilightDomeGeo.UV;
import de.jakob.lotm.entity.custom.ability_entities.twilight_giant.TwilightSlashEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Random;

public class TwilightSlashRenderer extends EntityRenderer<TwilightSlashEntity> {
    private static final ResourceLocation FX_TEX =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/twilight_visual/slash_fx.png");

    private static final UV ARC = UV.px(0, 0, 32, 64, 256, 128);
    private static final UV STAR = UV.px(64, 0, 128, 64, 256, 128);
    private static final UV GLOW = UV.px(128, 0, 192, 64, 256, 128);
    private static final UV NEB = UV.px(0, 64, 128, 128, 256, 128);
    private static final int FB = LightTexture.FULL_BRIGHT;

    private static final float ARC_HALF_WIDTH = 2.0F;
    private static final float ARC_TILT = 0.7F;
    private static final int ARC_SEGS = 22;
    private static final int TRAIL_SEGS = 40;
    private static final float TRAIL_HALF_WIDTH = 8.4F;
    private static final int TRAIL_SHEETS = 7;
    private static final float TRAIL_DARK = 1.0F;
    private static final int N_STARS = 1500;
    private static final float[] ST_Q = new float[N_STARS];
    private static final float[] ST_OFF = new float[N_STARS];
    private static final float[] ST_PHI = new float[N_STARS];
    private static final float[] ST_H = new float[N_STARS];
    private static final float[] ST_SZ = new float[N_STARS];
    private static final float[] ST_PH = new float[N_STARS];
    private static final float[] ST_SPD = new float[N_STARS];

    static {
        Random random = new Random(20261005L);
        for (int i = 0; i < N_STARS; i++) {
            ST_Q[i] = random.nextFloat();
            ST_OFF[i] = (random.nextFloat() * 2 - 1) * TRAIL_HALF_WIDTH;
            ST_PHI[i] = random.nextFloat() * Mth.PI;
            ST_H[i] = (random.nextFloat() - 0.5F) * 0.6F;
            ST_SZ[i] = 0.07F + 0.5F * (float) Math.pow(random.nextFloat(), 4.0);
            ST_PH[i] = random.nextFloat() * Mth.TWO_PI;
            ST_SPD[i] = 0.18F + 0.45F * random.nextFloat();
        }
    }

    public TwilightSlashRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(TwilightSlashEntity entity) {
        return FX_TEX;
    }

    @Override
    public boolean shouldRender(TwilightSlashEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(TwilightSlashEntity entity, float yaw, float partial, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        float age = entity.getAge(partial);
        double ex = Mth.lerp(partial, entity.xo, entity.getX());
        double ey = Mth.lerp(partial, entity.yo, entity.getY());
        double ez = Mth.lerp(partial, entity.zo, entity.getZ());
        Vec3 start = entity.pathStart(partial);
        Vec3 end = entity.pathEnd();
        float sx = (float) (start.x - ex);
        float sy = (float) (start.y - ey);
        float sz = (float) (start.z - ez);
        float tx = (float) (end.x - ex);
        float tz = (float) (end.z - ez);
        float dx = tx - sx;
        float dz = tz - sz;
        float length = Mth.sqrt(dx * dx + dz * dz);
        if (length < 1.0E-4F) return;
        float fx = dx / length;
        float fz = dz / length;
        float sdx = -fz;
        float sdz = fx;
        float progress = TwilightSlashEntity.ease(age / TwilightSlashEntity.FLIGHT_END);

        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucentEmissive(FX_TEX));
        PoseStack.Pose pose = poseStack.last();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3f left = camera.getLeftVector();
        Vector3f up = camera.getUpVector();

        float trailFade = 1.0F - Mth.clamp((age - TwilightSlashEntity.FLIGHT_END) / 18.0F, 0, 1);
        if (trailFade > 0 && progress > 0.002F) {
            for (int sheet = 0; sheet < TRAIL_SHEETS; sheet++) {
                float phi = sheet * Mth.PI / TRAIL_SHEETS;
                float cos = Mth.cos(phi);
                float sin = Mth.sin(phi);
                ribbon(consumer, pose, sx, sy, sz, dx, dz, progress, sdx * cos, sin, sdz * cos, TRAIL_HALF_WIDTH, trailFade);
            }
            for (int i = 0; i < N_STARS; i++) {
                float seen = Mth.clamp((progress - ST_Q[i]) / 0.035F, 0, 1);
                if (seen <= 0) continue;
                float twinkle = 0.5F + 0.5F * Mth.sin(age * ST_SPD[i] + ST_PH[i]);
                twinkle = twinkle * twinkle;
                float alpha = seen * trailFade * (0.3F + 0.7F * twinkle);
                if (alpha < 0.02F) continue;
                float cos = Mth.cos(ST_PHI[i]);
                float sin = Mth.sin(ST_PHI[i]);
                float x = sx + dx * ST_Q[i] + sdx * cos * ST_OFF[i];
                float y = sy + sin * ST_OFF[i] + ST_H[i];
                float z = sz + dz * ST_Q[i] + sdz * cos * ST_OFF[i];
                int kind = i % 7;
                float red = kind == 4 ? 1.0F : kind == 6 ? 1.0F : 0.82F;
                float green = kind == 4 ? 0.9F : kind == 6 ? 0.7F : 0.9F;
                float blue = kind == 4 ? 0.65F : kind == 6 ? 0.9F : 1.0F;
                bill(consumer, pose, STAR, x, y, z, ST_SZ[i] * (0.6F + 0.7F * twinkle), argb(alpha, red, green, blue), left, up);
            }
        }

        float arcAlpha = (age < TwilightSlashEntity.FLIGHT_END ? 1.0F : 1.0F - Mth.clamp((age - TwilightSlashEntity.FLIGHT_END) / 3.0F, 0, 1))
                * Mth.clamp(age / 2.5F, 0, 1);
        float px = sx + dx * progress;
        float pz = sz + dz * progress;
        if (arcAlpha > 0) {
            float wobble = Mth.sin(age * 0.5F) * 0.06F;
            for (int j = 5; j >= 1; j--) {
                float back = j * 0.9F;
                crescent(consumer, pose, px - fx * back, sy, pz - fz * back, fx, fz, sdx, sdz, ARC_TILT + wobble,
                        ARC_HALF_WIDTH * (1.0F - j * 0.03F), 1.4F, 0.9F, arcAlpha * 0.26F * (1.0F - j / 6.0F),
                        1.0F, 0.55F, 0.3F);
            }
            bill(consumer, pose, GLOW, px, sy, pz, 3.4F, argb(0.5F * arcAlpha, 1.0F, 0.45F, 0.15F), left, up);
            crescent(consumer, pose, px, sy, pz, fx, fz, sdx, sdz, ARC_TILT + wobble, ARC_HALF_WIDTH * 1.22F, 1.7F, 1.5F,
                    arcAlpha * 0.38F, 1.0F, 0.3F, 0.12F);
            crescent(consumer, pose, px, sy, pz, fx, fz, sdx, sdz, ARC_TILT + wobble, ARC_HALF_WIDTH, 1.4F, 0.9F, arcAlpha,
                    1.0F, 1.0F, 1.0F);
            crescent(consumer, pose, px, sy + 0.04F, pz, fx, fz, sdx, sdz, ARC_TILT + wobble, ARC_HALF_WIDTH * 0.8F, 1.28F, 0.3F,
                    arcAlpha * 0.9F, 1.0F, 0.95F, 0.75F);
        }
        float flash = (age - (TwilightSlashEntity.FLIGHT_END - 3.0F)) / 11.0F;
        if (flash > 0 && flash < 1) {
            bill(consumer, pose, GLOW, tx, sy, tz, 2.5F + flash * 5.0F, argb(1 - flash, 1.0F, 0.6F, 0.25F), left, up);
            bill(consumer, pose, STAR, tx, sy, tz, 3.0F + flash * 3.0F, argb(1 - flash, 1.0F, 0.9F, 0.7F), left, up);
        }
    }

    private static void crescent(VertexConsumer consumer, PoseStack.Pose pose, float cx, float cy, float cz,
                                 float fx, float fz, float sdx, float sdz, float tilt,
                                 float halfWidth, float bulge, float thick, float alpha,
                                 float red, float green, float blue) {
        float bx = fx * Mth.cos(tilt);
        float by = Mth.sin(tilt);
        float bz = fz * Mth.cos(tilt);
        float[] ox = new float[ARC_SEGS + 1];
        float[] oy = new float[ARC_SEGS + 1];
        float[] oz = new float[ARC_SEGS + 1];
        float[] ix = new float[ARC_SEGS + 1];
        float[] iy = new float[ARC_SEGS + 1];
        float[] iz = new float[ARC_SEGS + 1];
        float[] edgeAlpha = new float[ARC_SEGS + 1];
        for (int i = 0; i <= ARC_SEGS; i++) {
            float t = -1.0F + 2.0F * i / ARC_SEGS;
            float along = t * halfWidth;
            float curve = Math.max(0.0F, 1.0F - t * t);
            float outer = bulge * curve;
            float inner = outer - thick * (float) Math.pow(curve, 0.6F);
            ox[i] = cx + sdx * along + bx * outer;
            oy[i] = cy + by * outer;
            oz[i] = cz + sdz * along + bz * outer;
            ix[i] = cx + sdx * along + bx * inner;
            iy[i] = cy + by * inner;
            iz[i] = cz + sdz * along + bz * inner;
            edgeAlpha[i] = (float) Math.pow(curve, 0.35F) * alpha;
        }
        for (int i = 0; i < ARC_SEGS; i++) {
            int start = argb(edgeAlpha[i], red, green, blue);
            int next = argb(edgeAlpha[i + 1], red, green, blue);
            TwilightDomeGeo.v(consumer, pose, ox[i], oy[i], oz[i], ARC.u0(), ARC.v0(), start, FB, 0, 1, 0);
            TwilightDomeGeo.v(consumer, pose, ix[i], iy[i], iz[i], ARC.u0(), ARC.v1(), start, FB, 0, 1, 0);
            TwilightDomeGeo.v(consumer, pose, ix[i + 1], iy[i + 1], iz[i + 1], ARC.u1(), ARC.v1(), next, FB, 0, 1, 0);
            TwilightDomeGeo.v(consumer, pose, ox[i + 1], oy[i + 1], oz[i + 1], ARC.u1(), ARC.v0(), next, FB, 0, 1, 0);
        }
    }

    private static void ribbon(VertexConsumer consumer, PoseStack.Pose pose, float sx, float sy, float sz,
                               float dx, float dz, float progress, float wx, float wy, float wz,
                               float halfWidth, float alpha) {
        float midV = (NEB.v0() + NEB.v1()) * 0.5F;
        for (int segment = 0; segment < TRAIL_SEGS; segment++) {
            float q0 = progress * segment / TRAIL_SEGS;
            float q1 = progress * (segment + 1) / TRAIL_SEGS;
            float a0 = trailAlpha(q0, progress) * alpha;
            float a1 = trailAlpha(q1, progress) * alpha;
            float x0 = sx + dx * q0;
            float z0 = sz + dz * q0;
            float x1 = sx + dx * q1;
            float z1 = sz + dz * q1;
            boolean flip = (segment & 1) == 1;
            float u0 = flip ? NEB.u1() : NEB.u0();
            float u1 = flip ? NEB.u0() : NEB.u1();
            int edge = argb(0, TRAIL_DARK, TRAIL_DARK, TRAIL_DARK);
            int mid0 = argb(a0, TRAIL_DARK, TRAIL_DARK, TRAIL_DARK * 1.2F);
            int mid1 = argb(a1, TRAIL_DARK, TRAIL_DARK, TRAIL_DARK * 1.2F);
            TwilightDomeGeo.v(consumer, pose, x0 - wx * halfWidth, sy - wy * halfWidth, z0 - wz * halfWidth, u0, NEB.v0(), edge, FB, 0, 1, 0);
            TwilightDomeGeo.v(consumer, pose, x0, sy, z0, u0, midV, mid0, FB, 0, 1, 0);
            TwilightDomeGeo.v(consumer, pose, x1, sy, z1, u1, midV, mid1, FB, 0, 1, 0);
            TwilightDomeGeo.v(consumer, pose, x1 - wx * halfWidth, sy - wy * halfWidth, z1 - wz * halfWidth, u1, NEB.v0(), edge, FB, 0, 1, 0);
            TwilightDomeGeo.v(consumer, pose, x0, sy, z0, u0, midV, mid0, FB, 0, 1, 0);
            TwilightDomeGeo.v(consumer, pose, x0 + wx * halfWidth, sy + wy * halfWidth, z0 + wz * halfWidth, u0, NEB.v1(), edge, FB, 0, 1, 0);
            TwilightDomeGeo.v(consumer, pose, x1 + wx * halfWidth, sy + wy * halfWidth, z1 + wz * halfWidth, u1, NEB.v1(), edge, FB, 0, 1, 0);
            TwilightDomeGeo.v(consumer, pose, x1, sy, z1, u1, midV, mid1, FB, 0, 1, 0);
        }
    }

    private static float trailAlpha(float q, float progress) {
        float fromHead = progress > 0 ? (progress - q) / progress : 0.0F;
        return (1.0F - 0.25F * fromHead) * Mth.clamp(q * 25.0F, 0, 1);
    }

    private static void bill(VertexConsumer consumer, PoseStack.Pose pose, UV uv, float x, float y, float z,
                             float size, int color, Vector3f left, Vector3f up) {
        float lx = left.x * size;
        float ly = left.y * size;
        float lz = left.z * size;
        float ux = up.x * size;
        float uy = up.y * size;
        float uz = up.z * size;
        TwilightDomeGeo.quad(consumer, pose, FB, color, uv,
                x + lx + ux, y + ly + uy, z + lz + uz,
                x + lx - ux, y + ly - uy, z + lz - uz,
                x - lx - ux, y - ly - uy, z - lz - uz,
                x - lx + ux, y - ly + uy, z - lz + uz,
                0, 1, 0);
    }

    private static int argb(float alpha, float red, float green, float blue) {
        return (to255(alpha) << 24) | (to255(red) << 16) | (to255(green) << 8) | to255(blue);
    }

    private static int to255(float value) {
        return Mth.clamp((int) (value * 255.0F), 0, 255);
    }
}
