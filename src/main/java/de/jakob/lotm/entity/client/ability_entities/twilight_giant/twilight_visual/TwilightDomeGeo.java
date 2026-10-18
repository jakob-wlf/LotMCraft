package de.jakob.lotm.entity.client.ability_entities.twilight_giant.twilight_visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;

final class TwilightDomeGeo {
    static final int WHITE = 0xFFFFFFFF;

    private TwilightDomeGeo() {
    }

    record UV(float u0, float v0, float u1, float v1) {
        static UV px(int x0, int y0, int x1, int y1, int w, int h) {
            return new UV((float) x0 / w, (float) y0 / h, (float) x1 / w, (float) y1 / h);
        }
    }

    static int lift(int light) {
        return LightTexture.pack(Math.max(LightTexture.block(light), 9), LightTexture.sky(light));
    }

    static void quad(VertexConsumer c, PoseStack.Pose p, int light, int color, UV uv,
                     float ax, float ay, float az,
                     float bx, float by, float bz,
                     float cx, float cy, float cz,
                     float dx, float dy, float dz,
                     float nx, float ny, float nz) {
        v(c, p, ax, ay, az, uv.u0(), uv.v0(), color, light, nx, ny, nz);
        v(c, p, bx, by, bz, uv.u0(), uv.v1(), color, light, nx, ny, nz);
        v(c, p, cx, cy, cz, uv.u1(), uv.v1(), color, light, nx, ny, nz);
        v(c, p, dx, dy, dz, uv.u1(), uv.v0(), color, light, nx, ny, nz);
    }

    static void v(VertexConsumer c, PoseStack.Pose p, float x, float y, float z, float u, float v,
                  int color, int light, float nx, float ny, float nz) {
        c.addVertex(p, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(p, nx, ny, nz);
    }

    static void box(VertexConsumer c, PoseStack.Pose p, int light, UV side, UV top,
                    float x0, float y0, float z0, float x1, float y1, float z1) {
        quad(c, p, light, WHITE, side, x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0, 1, 0, 0);
        quad(c, p, light, WHITE, side, x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1, -1, 0, 0);
        quad(c, p, light, WHITE, side, x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1, 0, 0, 1);
        quad(c, p, light, WHITE, side, x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0, 0, 0, -1);
        quad(c, p, light, WHITE, top, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, 0, 1, 0);
    }
}
