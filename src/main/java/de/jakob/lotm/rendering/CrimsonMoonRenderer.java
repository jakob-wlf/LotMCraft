package de.jakob.lotm.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class CrimsonMoonRenderer {
    
    // Use your own custom moon texture here
    private static final ResourceLocation CRIMSON_MOON_TEXTURE = 
        ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/environment/crimson_moon.png");

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // Try AFTER_SKY or AFTER_CELESTIAL_BODIES
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        Camera camera = event.getCamera();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        // Debug: Print to console
        System.out.println("Rendering crimson moon! Time: " + level.getTimeOfDay(partialTick));

        renderCrimsonMoon(event.getPoseStack(), level, partialTick);
    }

    private static void renderCrimsonMoon(PoseStack poseStack, ClientLevel level, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, CRIMSON_MOON_TEXTURE);

        // Apply crimson color tint - make it brighter for testing
        RenderSystem.setShaderColor(1.5F, 0.3F, 0.3F, 1.0F);

        poseStack.pushPose();

        // Calculate moon rotation
        float celestialAngle = level.getTimeOfDay(partialTick);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(celestialAngle * 360.0F));

        // Position moon opposite to sun
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180.0F));

        Matrix4f matrix = poseStack.last().pose();

        // Render moon quad - make it bigger for testing
        float moonSize = 30.0F;
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        bufferBuilder.addVertex(matrix, -moonSize, -100.0F, -moonSize).setUv(0.0F, 0.0F);
        bufferBuilder.addVertex(matrix, moonSize, -100.0F, -moonSize).setUv(1.0F, 0.0F);
        bufferBuilder.addVertex(matrix, moonSize, -100.0F, moonSize).setUv(1.0F, 1.0F);
        bufferBuilder.addVertex(matrix, -moonSize, -100.0F, moonSize).setUv(0.0F, 1.0F);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        poseStack.popPose();

        // Reset color
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}