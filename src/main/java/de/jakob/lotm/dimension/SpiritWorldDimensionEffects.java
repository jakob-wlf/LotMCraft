package de.jakob.lotm.dimension;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class SpiritWorldDimensionEffects {

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_world"),
                new SpiritWorldEffects()
        );
    }

    public static class SpiritWorldEffects extends DimensionSpecialEffects {

        private static final ResourceLocation ETHER = ResourceLocation.fromNamespaceAndPath(
                LOTMCraft.MOD_ID, "textures/environment/spirit_ether.png");

        private static final Vec3 WHITE = new Vec3(1, 1, 1);
        private static final float RADIUS = 220f;

        public SpiritWorldEffects() {
            super(Float.NaN, true, SkyType.NONE, false, false);
        }

        @Override
        public @NotNull Vec3 getBrightnessDependentFogColor(Vec3 biomeFog, float brightness) {
            return SpiritWorldSky.horizon();
        }

        @Override
        public boolean isFoggyAt(int x, int z) {
            return false;
        }

        @Override
        public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix,
                                 Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
            setupFog.run();
            float time = ticks + partialTick;

            BlockPos pos = camera.getBlockPosition();
            SpiritWorldSky.update(pos.getX(), pos.getZ());
            Vec3 tint = SpiritWorldSky.tint();

            RenderSystem.disableCull();      // <- the bug: quads were being back-face culled
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            drawDome(new Matrix4f(modelViewMatrix), SpiritWorldSky.top(), SpiritWorldSky.horizon());

            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);

            //         height, texScale, scrollU,  scrollV,  rotSpeed, color,                 intensity
            drawLayer(modelViewMatrix, 40f, 150f,  0.0025f,  0.0012f,  0.0006f, WHITE.lerp(tint, 0.5),
                    0.85f + 0.15f * Mth.sin(time * 0.030f), time);
            drawLayer(modelViewMatrix, 70f, 220f, -0.0018f,  0.0022f, -0.0004f, WHITE.lerp(tint, 0.8),
                    0.65f + 0.20f * Mth.sin(time * 0.021f + 2.0f), time);
            drawLayer(modelViewMatrix, 100f, 320f, 0.0010f, -0.0008f,  0.0002f, new Vec3(0.75, 0.9, 1.0),
                    0.50f + 0.15f * Mth.sin(time * 0.015f + 4.0f), time);

            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            return true;
        }

        @Override
        public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick,
                                         net.minecraft.client.renderer.LightTexture lightTexture,
                                         double camX, double camY, double camZ) {
            return true;
        }

        @Override
        public boolean tickRain(ClientLevel level, int ticks, Camera camera) {
            return true;
        }

        private void drawDome(Matrix4f mat, Vec3 top, Vec3 horizon) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            b.addVertex(mat, 0, 120, 0).setColor((float) top.x, (float) top.y, (float) top.z, 1f);
            int seg = 24;
            for (int i = 0; i <= seg; i++) {
                float a = (float) (i * Math.PI * 2 / seg);
                b.addVertex(mat, Mth.cos(a) * 150, -20, Mth.sin(a) * 150)
                        .setColor((float) horizon.x, (float) horizon.y, (float) horizon.z, 1f);
            }
            BufferUploader.drawWithShader(b.buildOrThrow());
        }

        private void drawLayer(Matrix4f base, float height, float texScale, float scrollU, float scrollV,
                               float rotSpeed, Vec3 color, float intensity, float time) {
            RenderSystem.setShaderTexture(0, ETHER);
            Matrix4f mat = new Matrix4f(base).rotateY(time * rotSpeed);
            float su = time * scrollU, sv = time * scrollV;

            int n = 20;
            float step = RADIUS * 2 / n;
            BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    float x0 = -RADIUS + i * step, x1 = x0 + step;
                    float z0 = -RADIUS + j * step, z1 = z0 + step;
                    vert(b, mat, x0, z0, height, texScale, su, sv, color, intensity);
                    vert(b, mat, x0, z1, height, texScale, su, sv, color, intensity);
                    vert(b, mat, x1, z1, height, texScale, su, sv, color, intensity);
                    vert(b, mat, x1, z0, height, texScale, su, sv, color, intensity);
                }
            }
            BufferUploader.drawWithShader(b.buildOrThrow());
        }

        private void vert(BufferBuilder b, Matrix4f m, float x, float z, float y, float scale,
                          float su, float sv, Vec3 c, float intensity) {
            float dist = Mth.clamp(Mth.sqrt(x * x + z * z) / RADIUS, 0f, 1f);
            float k = intensity * (1f - dist * dist); // stays bright longer, fades near the horizon
            b.addVertex(m, x, y, z)
                    .setUv(x / scale + su, z / scale + sv)
                    .setColor((float) c.x * k, (float) c.y * k, (float) c.z * k, 1f);
        }
    }
}