package de.jakob.lotm.entity.client.ability_entities.twilight_giant.twilight_visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ability_entities.twilight_giant.TwilightDomeEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.UUID;

public class TwilightDomeRenderer extends EntityRenderer<TwilightDomeEntity> {
    private static final ResourceLocation DOME_TEX = tex("dome");
    private static final ResourceLocation SUN_TEX = tex("sun");
    private static final ResourceLocation CLOUD_TEX = tex("cloud");
    private static final int SEGMENTS = 96;
    private static final int RINGS = 32;
    private static final int CLOUDS = 8;

    public TwilightDomeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    private static ResourceLocation tex(String name) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/twilight_visual/" + name + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(TwilightDomeEntity entity) {
        return DOME_TEX;
    }

    @Override
    public boolean shouldRender(TwilightDomeEntity entity, Frustum frustum, double x, double y, double z) {
        return visible(entity);
    }

    private static boolean visible(TwilightDomeEntity dome) {
        UUID subject = dome.subject();
        if (subject == null) return true;
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.getUUID().equals(subject);
    }

    @Override
    public void render(TwilightDomeEntity dome, float yaw, float partial, PoseStack ps, MultiBufferSource buffers, int light) {
        if (!visible(dome)) return;
        float age = dome.getAge(partial);
        float fadeIn = Mth.clamp(age / 10.0F, 0.0F, 1.0F);
        float p = dome.sunsetProgress(partial);
        float dim = 1.0F - 0.75F * p;
        float radius = dome.radius();

        ps.pushPose();
        PoseStack.Pose pose = ps.last();
        drawDome(buffers.getBuffer(RenderType.entityTranslucentEmissive(DOME_TEX)), pose, fadeIn, dim, radius);
        drawSun(buffers.getBuffer(RenderType.entityTranslucentEmissive(SUN_TEX)), pose, dome, fadeIn, p, radius);
        drawClouds(buffers.getBuffer(RenderType.entityTranslucentEmissive(CLOUD_TEX)), pose, age, fadeIn, p, radius);
        ps.popPose();
    }

    private static void drawDome(VertexConsumer c, PoseStack.Pose pose, float fadeIn, float dim, float radius) {
        int color = argb(fadeIn, dim, dim, dim);
        float half = (float) (Math.PI / 2.0);
        for (int i = 0; i < RINGS; i++) {
            float lat0 = half * i / RINGS;
            float lat1 = half * (i + 1) / RINGS;
            float v0 = 1.0F - (float) i / RINGS;
            float v1 = 1.0F - (float) (i + 1) / RINGS;
            for (int j = 0; j < SEGMENTS; j++) {
                float lon0 = (float) (2.0 * Math.PI * j / SEGMENTS);
                float lon1 = (float) (2.0 * Math.PI * (j + 1) / SEGMENTS);
                float u0 = (float) j / SEGMENTS;
                float u1 = (float) (j + 1) / SEGMENTS;
                vertex(c, pose, radius * Mth.cos(lat0) * Mth.cos(lon0), radius * Mth.sin(lat0), radius * Mth.cos(lat0) * Mth.sin(lon0), u0, v0, color);
                vertex(c, pose, radius * Mth.cos(lat0) * Mth.cos(lon1), radius * Mth.sin(lat0), radius * Mth.cos(lat0) * Mth.sin(lon1), u1, v0, color);
                vertex(c, pose, radius * Mth.cos(lat1) * Mth.cos(lon1), radius * Mth.sin(lat1), radius * Mth.cos(lat1) * Mth.sin(lon1), u1, v1, color);
                vertex(c, pose, radius * Mth.cos(lat1) * Mth.cos(lon0), radius * Mth.sin(lat1), radius * Mth.cos(lat1) * Mth.sin(lon0), u0, v1, color);
            }
        }
    }

    private static void drawSun(VertexConsumer c, PoseStack.Pose pose, TwilightDomeEntity dome, float fadeIn, float p, float radius) {
        float scale = radius / 24.0F;
        float yawRad = dome.getYRot() * Mth.DEG_TO_RAD;
        float lon = (float) Math.atan2(Mth.cos(yawRad), -Mth.sin(yawRad)) + 0.3F;
        float elev = Mth.lerp(p, TwilightDomeEntity.SUN_HIGH, TwilightDomeEntity.SUN_LOW) * Mth.DEG_TO_RAD;
        float dist = radius * 0.95F;
        float cosE = Mth.cos(elev);
        float sinE = Mth.sin(elev);
        float cx = dist * cosE * Mth.cos(lon);
        float cy = dist * sinE;
        float cz = dist * cosE * Mth.sin(lon);
        float size = 4.5F * scale;
        float rx = -Mth.sin(lon) * size;
        float rz = Mth.cos(lon) * size;
        float ux = -sinE * Mth.cos(lon) * size;
        float uy = cosE * size;
        float uz = -sinE * Mth.sin(lon) * size;
        int color = argb(fadeIn, 1.0F, Mth.lerp(p, 0.95F, 0.45F), Mth.lerp(p, 0.75F, 0.15F));
        quad(c, pose, cx, cy, cz, rx, 0, rz, ux, uy, uz, color);
    }

    private static void drawClouds(VertexConsumer c, PoseStack.Pose pose, float age, float fadeIn, float p, float radius) {
        float scale = radius / 24.0F;
        float shade = 1.0F - 0.7F * p;
        for (int k = 0; k < CLOUDS; k++) {
            float seed = k * 1.7F;
            float theta = seed * 1.3F + age * (0.006F + 0.0025F * (k % 3));
            float radial = radius * (0.22F + 0.10F * (k % 4));
            float y = Mth.sqrt(radius * radius - radial * radial) * 0.92F + Mth.sin(age * 0.04F + seed) * 0.5F * scale;
            float cx = radial * Mth.cos(theta);
            float cz = radial * Mth.sin(theta);
            float hl = (4.5F + 1.5F * (k % 3)) * scale;
            float hw = (2.2F + 0.8F * (k % 2)) * scale;
            float ax = -Mth.sin(theta) * hl;
            float az = Mth.cos(theta) * hl;
            float bx = Mth.cos(theta) * hw;
            float bz = Mth.sin(theta) * hw;
            float alpha = fadeIn * (0.65F + 0.2F * Mth.sin(age * 0.07F + seed));
            int color = argb(alpha, shade, 0.78F * shade, 0.68F * shade);
            quad(c, pose, cx, y, cz, ax, 0, az, bx, 0, bz, color);
        }
    }

    private static void quad(VertexConsumer c, PoseStack.Pose pose,
                             float cx, float cy, float cz,
                             float ax, float ay, float az,
                             float bx, float by, float bz, int color) {
        vertex(c, pose, cx - ax + bx, cy - ay + by, cz - az + bz, 0, 0, color);
        vertex(c, pose, cx - ax - bx, cy - ay - by, cz - az - bz, 0, 1, color);
        vertex(c, pose, cx + ax - bx, cy + ay - by, cz + az - bz, 1, 1, color);
        vertex(c, pose, cx + ax + bx, cy + ay + by, cz + az + bz, 1, 0, color);
    }

    private static void vertex(VertexConsumer c, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color) {
        c.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private static int argb(float a, float r, float g, float b) {
        return (to255(a) << 24) | (to255(r) << 16) | (to255(g) << 8) | to255(b);
    }

    private static int to255(float f) {
        return Mth.clamp((int) (f * 255.0F), 0, 255);
    }
}
