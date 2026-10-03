package de.jakob.lotm.entity.client.projectiles.spirit_ball;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.projectiles.SpiritBallEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Random;
import org.joml.Vector3f;

import java.awt.*;

public class SpiritBallRenderer extends EntityRenderer<SpiritBallEntity> {

    public SpiritBallRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SpiritBallEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        // --- Stable per-entity color from UUID ---
        Random random = new Random(entity.getUUID().getMostSignificantBits());
        float hue        = random.nextFloat();
        // Lighter: lower saturation, much higher brightness
        float saturation = 0.5f + random.nextFloat() * 0.3f;
        float brightness = 0.92f + random.nextFloat() * 0.08f;

        int rgb   = Color.HSBtoRGB(hue, saturation, brightness);
        float red   = ((rgb >> 16) & 0xFF) / 255f;
        float green = ((rgb >> 8)  & 0xFF) / 255f;
        float blue  = ( rgb        & 0xFF) / 255f;

        // --- Pulse: stronger sine wave on brightness (±20%) for a more vivid glow ---
        float time  = entity.tickCount + partialTicks;
        float pulse = 1.0f + 0.20f * (float) Math.sin(time * 0.18f * Math.PI * 2.0);

        float pr = Math.min(1f, red   * pulse);
        float pg = Math.min(1f, green * pulse);
        float pb = Math.min(1f, blue  * pulse);

        ResourceLocation texture = getTextureLocation(entity);

        // --- Inner solid octahedron-faceted shape ---
        poseStack.pushPose();
        poseStack.scale(0.125f, 0.125f, 0.125f);
        Matrix4f matrix = poseStack.last().pose();

        VertexConsumer solidVc = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        renderSphere(matrix, solidVc, pr, pg, pb, 255);

        poseStack.popPose();

        // --- Outer glow shell: slightly larger, semi-transparent ---
        poseStack.pushPose();
        float glowScale = 0.125f * 1.35f;
        poseStack.scale(glowScale, glowScale, glowScale);
        Matrix4f glowMatrix = poseStack.last().pose();

        // Alpha pulses opposite phase for a "breathing" glow
        int glowAlpha = (int) (90 + 50 * Math.sin(time * 0.18f * Math.PI * 2.0 + Math.PI));
        VertexConsumer transVc = buffer.getBuffer(RenderType.entityTranslucentCull(texture));
        renderSphere(glowMatrix, transVc, Math.min(1f, pr * 1.15f), Math.min(1f, pg * 1.15f), Math.min(1f, pb * 1.15f), glowAlpha);

        poseStack.popPose();
    }

    @Override
    protected int getBlockLightLevel(SpiritBallEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull SpiritBallEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID,
                "textures/entity/spirit_ball/spirit_ball.png");
    }

    // ---------------------------------------------------------------

    private void renderSphere(Matrix4f matrix, VertexConsumer buffer,
                              float red, float green, float blue, int alpha) {
        int rings    = 4;
        int segments = 4;

        for (int i = 0; i < rings; i++) {
            float theta1 = (float) (Math.PI * i       / rings);
            float theta2 = (float) (Math.PI * (i + 1) / rings);

            for (int j = 0; j < segments; j++) {
                float phi1 = (float) (2 * Math.PI * j       / segments);
                float phi2 = (float) (2 * Math.PI * (j + 1) / segments);

                // positions (also serve as normals for a unit sphere)
                Vector3f v1 = spherical(theta1, phi1);
                Vector3f v2 = spherical(theta2, phi1);
                Vector3f v3 = spherical(theta2, phi2);
                Vector3f v4 = spherical(theta1, phi2);

                // UVs: map spherical coords to [0,1]² so the texture is sampled
                float u1 = (float) j       / segments;
                float u2 = (float) (j + 1) / segments;
                float v_top = (float) i       / rings;
                float v_bot = (float) (i + 1) / rings;

                putQuad(buffer, matrix,
                        v1, v2, v3, v4,
                        u1, u2, v_top, v_bot,
                        red, green, blue, alpha);
            }
        }
    }

    private Vector3f spherical(float theta, float phi) {
        float x = (float) (Math.sin(theta) * Math.cos(phi));
        float y = (float)  Math.cos(theta);
        float z = (float) (Math.sin(theta) * Math.sin(phi));
        return new Vector3f(x, y, z);
    }

    private void putQuad(VertexConsumer buffer, Matrix4f matrix,
                         Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4,
                         float u1, float u2, float vTop, float vBot,
                         float red, float green, float blue, int alpha) {

        int r = Math.min(255, (int)(red   * 255));
        int g = Math.min(255, (int)(green * 255));
        int b = Math.min(255, (int)(blue  * 255));

        // Each vertex gets its own spherical normal for per-face shading
        putVertex(buffer, matrix, v1, u1, vTop, r, g, b, alpha, v1);
        putVertex(buffer, matrix, v2, u1, vBot, r, g, b, alpha, v2);
        putVertex(buffer, matrix, v3, u2, vBot, r, g, b, alpha, v3);
        putVertex(buffer, matrix, v4, u2, vTop, r, g, b, alpha, v4);
    }

    private void putVertex(VertexConsumer buffer, Matrix4f matrix,
                           Vector3f pos, float u, float v,
                           int r, int g, int b, int alpha,
                           Vector3f normal) {
        buffer.addVertex(matrix, pos.x, pos.y, pos.z)
                .setColor(r, g, b, alpha)
                .setUv(u, v)
                .setLight(240)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(normal.x, normal.y, normal.z);
    }
}