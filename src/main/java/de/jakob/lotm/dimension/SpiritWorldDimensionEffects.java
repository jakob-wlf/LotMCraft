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

            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            drawDome(new Matrix4f(modelViewMatrix), SpiritWorldSky.top(), SpiritWorldSky.horizon());

            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);

            drawLayer(modelViewMatrix, 100f, 4, 2,  0.0006f,  0.0003f,  0.0006f, Mth.HALF_PI, 0f, WHITE.lerp(tint, 0.5),
                    0.85f + 0.15f * Mth.sin(time * 0.030f), time);
            drawLayer(modelViewMatrix, 105f, 3, 2, -0.0004f,  0.0005f, -0.0004f, 0f, Mth.HALF_PI, WHITE.lerp(tint, 0.8),
                    0.65f + 0.20f * Mth.sin(time * 0.021f + 2.0f), time);
            drawLayer(modelViewMatrix, 110f, 6, 3,  0.0003f, -0.0002f,  0.0002f, Mth.HALF_PI, Mth.HALF_PI, new Vec3(0.75, 0.9, 1.0),
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
            BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            int stacks = 24, slices = 32;
            for (int i = 0; i < stacks; i++) {
                for (int j = 0; j < slices; j++) {
                    domeVert(b, mat, i,     j,     stacks, slices, top, horizon);
                    domeVert(b, mat, i + 1, j,     stacks, slices, top, horizon);
                    domeVert(b, mat, i + 1, j + 1, stacks, slices, top, horizon);
                    domeVert(b, mat, i,     j + 1, stacks, slices, top, horizon);
                }
            }
            BufferUploader.drawWithShader(b.buildOrThrow());
        }

        private void domeVert(BufferBuilder b, Matrix4f mat, int stack, int slice, int stacks, int slices,
                              Vec3 top, Vec3 horizon) {
            float phi = (float) Math.PI * stack / stacks;
            float theta = (float) (2 * Math.PI) * slice / slices;
            float dx = Mth.sin(phi) * Mth.cos(theta);
            float dy = Mth.cos(phi);
            float dz = Mth.sin(phi) * Mth.sin(theta);

            float f = (float) Math.pow(Math.abs(dy), 0.6);
            float r = (float) (horizon.x + (top.x - horizon.x) * f);
            float g = (float) (horizon.y + (top.y - horizon.y) * f);
            float bl = (float) (horizon.z + (top.z - horizon.z) * f);

            b.addVertex(mat, dx * 150f, dy * 150f, dz * 150f).setColor(r, g, bl, 1f);
        }

        private static final int STACKS = 24, SLICES = 48;

        private void drawLayer(Matrix4f base, float radius, int repU, int repV, float scrollU, float scrollV,
                               float spinSpeed, float tiltX, float tiltZ, Vec3 color, float intensity, float time) {
            RenderSystem.setShaderTexture(0, ETHER);

            Matrix4f rot = new Matrix4f().rotateY(time * spinSpeed).rotateX(tiltX).rotateZ(tiltZ);
            Matrix4f mat = new Matrix4f(base).mul(rot);
            float su = time * scrollU, sv = time * scrollV;

            BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            for (int i = 0; i < STACKS; i++) {
                for (int j = 0; j < SLICES; j++) {
                    sphereVert(b, mat, i,     j,     radius, repU, repV, su, sv, color, intensity);
                    sphereVert(b, mat, i + 1, j,     radius, repU, repV, su, sv, color, intensity);
                    sphereVert(b, mat, i + 1, j + 1, radius, repU, repV, su, sv, color, intensity);
                    sphereVert(b, mat, i,     j + 1, radius, repU, repV, su, sv, color, intensity);
                }
            }
            BufferUploader.drawWithShader(b.buildOrThrow());
        }

        private void sphereVert(BufferBuilder b, Matrix4f mat, int stack, int slice, float radius,
                                int repU, int repV, float su, float sv, Vec3 c, float intensity) {
            float phi   = (float) Math.PI * stack / STACKS;
            float theta = (float) (2 * Math.PI) * slice / SLICES;

            float dx = Mth.sin(phi) * Mth.cos(theta);
            float dy = Mth.cos(phi);
            float dz = Mth.sin(phi) * Mth.sin(theta);

            float u = (float) slice / SLICES * repU + su;
            float v = (float) stack / STACKS * repV + sv;

            float a = Math.abs(dy);
            float t = Mth.clamp((0.97f - a) / 0.17f, 0f, 1f);
            float k = intensity * t * t * (3f - 2f * t);

            b.addVertex(mat, dx * radius, dy * radius, dz * radius)
                    .setUv(u, v)
                    .setColor((float) c.x * k, (float) c.y * k, (float) c.z * k, 1f);
        }
    }
}