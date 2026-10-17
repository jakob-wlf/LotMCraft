package de.jakob.lotm.entity.client.ability_entities.twilight_giant.twilight_visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.ability_entities.twilight_giant.twilight_visual.TwilightDomeGeo.UV;
import de.jakob.lotm.entity.custom.ability_entities.twilight_giant.HurricaneOfLightEntity;
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
import org.joml.Vector3f;

import java.util.Random;

public class HurricaneOfLightRenderer extends EntityRenderer<HurricaneOfLightEntity> {
    private static final ResourceLocation FX_TEX =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/twilight_visual/hurricane_of_light.png");

    private static final UV SPECK = UV.px(0, 0, 32, 32, 256, 128);
    private static final UV STREAK = UV.px(32, 0, 96, 16, 256, 128);
    private static final UV SHARD1 = UV.px(0, 32, 32, 64, 256, 128);
    private static final UV SHARD2 = UV.px(32, 32, 64, 64, 256, 128);
    private static final UV SPARK = UV.px(64, 32, 96, 64, 256, 128);
    private static final UV FLASH = UV.px(64, 64, 128, 128, 256, 128);

    private static final int FB = LightTexture.FULL_BRIGHT;
    private static final float SPEED = 1.5F;

    private static final int N = 4200;
    private static final int SHARD1_END = 240;
    private static final int SHARD2_END = 480;
    private static final int STREAK_END = 1350;
    private static final int SPECK_END = 3400;
    private static final int SPARK_END = 4040;
    private static final float[] YS = new float[N];
    private static final float[] SX = new float[N];
    private static final float[] SZ = new float[N];
    private static final float[] H0 = new float[N];
    private static final float[] PHI = new float[N];
    private static final float[] OM = new float[N];
    private static final float[] RISE = new float[N];
    private static final float[] RF = new float[N];
    private static final float[] SZR = new float[N];
    private static final float[] AX = new float[N];
    private static final float[] AY = new float[N];
    private static final float[] AZ = new float[N];
    private static final float[] SPIN = new float[N];

    static {
        Random random = new Random(20261005L);
        for (int i = 0; i < N; i++) {
            YS[i] = 0.05F + random.nextFloat() * 3.35F;
            SX[i] = (random.nextFloat() - 0.5F) * 0.4F;
            SZ[i] = (random.nextFloat() - 0.5F) * 0.2F;
            H0[i] = random.nextFloat();
            PHI[i] = random.nextFloat() * Mth.TWO_PI;
            OM[i] = 0.8F + 0.45F * random.nextFloat();
            RISE[i] = 0.6F + 0.8F * random.nextFloat();
            RF[i] = 0.2F + 0.8F * (float) Math.pow(random.nextFloat(), 0.5);
            SZR[i] = random.nextFloat();
            float x = random.nextFloat() * 2 - 1;
            float y = random.nextFloat() * 2 - 1;
            float z = random.nextFloat() * 2 - 1;
            float len = Mth.sqrt(x * x + y * y + z * z);
            if (len < 1e-3F) {
                x = 0;
                y = 1;
                z = 0;
                len = 1;
            }
            AX[i] = x / len;
            AY[i] = y / len;
            AZ[i] = z / len;
            SPIN[i] = (0.2F + 0.4F * random.nextFloat()) * (random.nextBoolean() ? 1 : -1);
        }
    }

    public HurricaneOfLightRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(HurricaneOfLightEntity entity) {
        return FX_TEX;
    }

    @Override
    public boolean shouldRender(HurricaneOfLightEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(HurricaneOfLightEntity entity, float yaw, float partial, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        float age = entity.getAge(partial) * SPEED;
        float fade = entity.fade(partial);
        float scale = Math.max(0.01F, entity.radius() / HurricaneOfLightEntity.TOP_RADIUS);
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);

        VertexConsumer fx = buffers.getBuffer(RenderType.entityTranslucentEmissive(FX_TEX));
        PoseStack.Pose pose = poseStack.last();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        renderParticles(fx, pose, age, fade, entity.twilight(), camera.getLeftVector(), camera.getUpVector(), camera.getLookVector());
        poseStack.popPose();
    }

    private void renderParticles(VertexConsumer consumer, PoseStack.Pose pose, float age, float fade, boolean twilight,
                                 Vector3f left, Vector3f up, Vector3f look) {
        float time = Math.max(0.0F, age);
        float[] edge1 = new float[3];
        float[] edge2 = new float[3];
        float baseRadius = HurricaneOfLightEntity.BASE_RADIUS;
        float span = HurricaneOfLightEntity.TOP_RADIUS - baseRadius;

        float core = Mth.clamp(time / 12.0F, 0, 1) * (1.0F - fade);
        if (core > 0) {
            for (int j = 0; j < 9; j++) {
                float height = (j + 0.5F) / 9.0F;
                float x = Mth.sin(time * 0.045F + height * 2.5F) * 0.8F * height;
                float z = Mth.cos(time * 0.037F + height * 2.0F) * 0.8F * height;
                float pulse = 0.8F + 0.2F * Mth.sin(age * 0.15F + j * 1.7F);
                bill(consumer, pose, SPECK, x, height * HurricaneOfLightEntity.HEIGHT, z, 2.6F + height * 9.0F,
                        dye(0.24F * core * pulse, 0.95F, 0.96F, 0.98F, twilight), left, up);
            }
        }

        float skirt = Mth.clamp(time / 14.0F, 0, 1) * (1.0F - fade);
        if (skirt > 0) {
            for (int j = 0; j < 28; j++) {
                float turn = j / 28.0F;
                float angle = turn * Mth.TWO_PI + time * 0.09F + (j % 3) * 0.7F;
                float radius = baseRadius * (0.55F + 0.55F * ((j * 7) % 10) / 10.0F) + 1.5F;
                float y = 0.3F + 0.9F * ((j * 5) % 7) / 7.0F + 0.35F * Mth.sin(time * 0.12F + j);
                bill(consumer, pose, SPECK, Mth.cos(angle) * radius, y, Mth.sin(angle) * radius,
                        1.6F + 1.2F * ((j * 3) % 5) / 5.0F, dye(0.20F * skirt, 0.80F, 0.78F, 0.74F, twilight), left, up);
            }
        }

        for (int i = 0; i < N; i++) {
            float delay = (1.0F - Mth.clamp(YS[i] / 3.4F, 0, 1)) * 8.0F;
            float shown = Mth.clamp((time - delay) / 12.0F, 0, 1);
            if (shown <= 0) continue;
            float height = H0[i] + 0.010F * RISE[i] * time;
            height -= Mth.floor(height);
            float radius = (baseRadius + span * (float) Math.pow(height, 1.15F)) * RF[i] * (1.0F + 0.5F * fade);
            float theta = PHI[i] + OM[i] * (0.42F - 0.22F * height) * time;
            float cos = Mth.cos(theta);
            float sin = Mth.sin(theta);
            float swayX = Mth.sin(time * 0.045F + height * 2.5F) * 0.8F * height;
            float swayZ = Mth.cos(time * 0.037F + height * 2.0F) * 0.8F * height;
            float vortexX = radius * cos + swayX;
            float vortexY = height * HurricaneOfLightEntity.HEIGHT * (1.0F + 0.1F * fade);
            float vortexZ = radius * sin + swayZ;

            float ease = shown * shown * (3.0F - 2.0F * shown);
            float burst = Mth.sin(Mth.PI * shown) * 1.4F;
            float x = Mth.lerp(ease, SX[i], vortexX) + AX[i] * burst;
            float y = Mth.lerp(ease, YS[i], vortexY) + AY[i] * burst;
            float z = Mth.lerp(ease, SZ[i], vortexZ) + AZ[i] * burst;

            float edge = Math.min(height / 0.08F, 1.0F) * Math.min((1.0F - height) / 0.12F, 1.0F);
            float alpha = Mth.clamp(shown * 3.0F, 0, 1) * (1.0F - ease * (1.0F - edge)) * (1.0F - fade);
            if (alpha <= 0.01F) continue;
            float sizeScale = 0.8F + 1.3F * height;

            if (i < SHARD2_END) {
                float angle = SPIN[i] * (age + i);
                rot(AX[i], AY[i], AZ[i], angle, 1, 0, 0, edge1);
                rot(AX[i], AY[i], AZ[i], angle, 0, 1, 0, edge2);
                float size = (0.11F + 0.15F * SZR[i]) * (0.8F + 0.4F * height);
                UV uv = i < SHARD1_END ? SHARD1 : SHARD2;
                TwilightDomeGeo.quad(consumer, pose, FB, dye(alpha, 0.84F, 0.86F, 0.89F, twilight), uv,
                        x - edge1[0] * size + edge2[0] * size, y - edge1[1] * size + edge2[1] * size, z - edge1[2] * size + edge2[2] * size,
                        x - edge1[0] * size - edge2[0] * size, y - edge1[1] * size - edge2[1] * size, z - edge1[2] * size - edge2[2] * size,
                        x + edge1[0] * size - edge2[0] * size, y + edge1[1] * size - edge2[1] * size, z + edge1[2] * size - edge2[2] * size,
                        x + edge1[0] * size + edge2[0] * size, y + edge1[1] * size + edge2[1] * size, z + edge1[2] * size + edge2[2] * size,
                        0, 1, 0);
            } else if (i < STREAK_END) {
                float tx = -sin * 0.95F;
                float ty = 0.3F;
                float tz = cos * 0.95F;
                float length = Mth.sqrt(tx * tx + ty * ty + tz * tz);
                streak(consumer, pose, STREAK, x, y, z, tx / length, ty / length, tz / length,
                        (1.0F + 1.7F * SZR[i]) * sizeScale, (0.05F + 0.08F * SZR[i]) * sizeScale, look,
                        dye(alpha * 0.6F, 0.88F, 0.93F, 1.0F, twilight));
            } else if (i < SPECK_END) {
                boolean warm = (i % 10) == 0;
                bill(consumer, pose, SPECK, x, y, z, (0.16F + 0.45F * SZR[i] * SZR[i]) * sizeScale,
                        dye(alpha * 0.65F, 1.0F, warm ? 0.93F : 0.97F, warm ? 0.88F : 1.0F, twilight), left, up);
            } else if (i < SPARK_END) {
                bill(consumer, pose, SPARK, x, y, z, 0.10F + 0.10F * SZR[i], dye(alpha * 0.95F, 1, 1, 1, twilight), left, up);
            } else {
                float twinkle = Math.max(0.0F, Mth.sin(age * 0.3F * OM[i] + PHI[i] * 3.0F));
                twinkle = twinkle * twinkle * twinkle * twinkle * twinkle * twinkle;
                if (twinkle > 0.02F) {
                    bill(consumer, pose, FLASH, x, y, z, (0.5F + 0.9F * SZR[i]) * sizeScale * 0.8F,
                            dye(alpha * twinkle, 1.0F, 0.94F, 0.82F, twilight), left, up);
                }
            }
        }
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

    private static void streak(VertexConsumer consumer, PoseStack.Pose pose, UV uv, float x, float y, float z,
                               float tx, float ty, float tz, float length, float width, Vector3f look, int color) {
        float wx = ty * look.z - tz * look.y;
        float wy = tz * look.x - tx * look.z;
        float wz = tx * look.y - ty * look.x;
        float magnitude = Mth.sqrt(wx * wx + wy * wy + wz * wz);
        if (magnitude < 1e-4F) return;
        wx = wx / magnitude * width;
        wy = wy / magnitude * width;
        wz = wz / magnitude * width;
        float lx = tx * length;
        float ly = ty * length;
        float lz = tz * length;
        TwilightDomeGeo.quad(consumer, pose, FB, color, uv,
                x - lx + wx, y - ly + wy, z - lz + wz,
                x - lx - wx, y - ly - wy, z - lz - wz,
                x + lx - wx, y + ly - wy, z + lz - wz,
                x + lx + wx, y + ly + wy, z + lz + wz,
                0, 1, 0);
    }

    private static void rot(float kx, float ky, float kz, float angle, float vx, float vy, float vz, float[] out) {
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        float dot = kx * vx + ky * vy + kz * vz;
        float cx = ky * vz - kz * vy;
        float cy = kz * vx - kx * vz;
        float cz = kx * vy - ky * vx;
        out[0] = vx * cos + cx * sin + kx * dot * (1 - cos);
        out[1] = vy * cos + cy * sin + ky * dot * (1 - cos);
        out[2] = vz * cos + cz * sin + kz * dot * (1 - cos);
    }

    private static int dye(float alpha, float red, float green, float blue, boolean twilight) {
        if (twilight) return argb(alpha, red, green * 0.35F, blue * 0.10F);
        return argb(alpha, red, green * 0.97F, blue * 0.90F);
    }

    private static int argb(float alpha, float red, float green, float blue) {
        return (to255(alpha) << 24) | (to255(red) << 16) | (to255(green) << 8) | to255(blue);
    }

    private static int to255(float value) {
        return Mth.clamp((int) (value * 255.0F), 0, 255);
    }
}
