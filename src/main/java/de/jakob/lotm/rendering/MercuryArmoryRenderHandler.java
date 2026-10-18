package de.jakob.lotm.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.twilight_giant.LightConcealmentAbility;
import de.jakob.lotm.beyonders.abilities.twilight_giant.MercuryArmoryAbility;
import de.jakob.lotm.beyonders.abilities.twilight_giant.MercuryLiquefactionAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class MercuryArmoryRenderHandler {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final float PUDDLE_RADIUS = 1.2f;
    private static final float PUDDLE_HEIGHT = 0.4f;
    private static final int SEGMENTS = 30;
    private static final int RINGS = 10;

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (MercuryArmoryAbility.isHidden(player) || LightConcealmentAbility.isHidden(player)) {
            event.setCanceled(true);
            return;
        }
        if (!MercuryLiquefactionAbility.isLiquefied(player)) return;
        event.setCanceled(true);
        renderMercury(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), player, event.getPartialTick());
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && (MercuryArmoryAbility.isHidden(player) || LightConcealmentAbility.isHidden(player) || MercuryLiquefactionAbility.isLiquefied(player))) event.setCanceled(true);
    }

    private static void renderMercury(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Player player, float partialTick) {
        float time = player.tickCount + partialTick;
        poseStack.pushPose();
        poseStack.translate(0, PUDDLE_HEIGHT * 0.5f, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 2f));

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        for (int ring = 0; ring < RINGS; ring++) {
            float theta1 = ring * Mth.PI / RINGS;
            float theta2 = (ring + 1) * Mth.PI / RINGS;
            for (int seg = 0; seg < SEGMENTS; seg++) {
                float phi1 = seg * Mth.TWO_PI / SEGMENTS;
                float phi2 = (seg + 1) * Mth.TWO_PI / SEGMENTS;
                vertex(consumer, matrix, theta1, phi1, time, packedLight);
                vertex(consumer, matrix, theta1, phi2, time, packedLight);
                vertex(consumer, matrix, theta2, phi2, time, packedLight);
                vertex(consumer, matrix, theta2, phi1, time, packedLight);
            }
        }
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float theta, float phi, float time, int packedLight) {
        float ripple = 1f + Mth.sin(phi * 3f + time * 0.25f) * 0.08f + Mth.sin(phi * 5f - time * 0.4f) * 0.04f;
        float radius = PUDDLE_RADIUS * ripple;
        float x = radius * Mth.sin(theta) * Mth.cos(phi);
        float y = PUDDLE_HEIGHT * Mth.cos(theta) * (1f + Mth.sin(time * 0.15f) * 0.1f);
        float z = radius * Mth.sin(theta) * Mth.sin(phi);
        float shine = 0.75f + 0.25f * Mth.sin(phi * 2f + theta * 3f + time * 0.2f);
        consumer.addVertex(matrix, x, y, z)
                .setColor(0.78f * shine, 0.8f * shine, 0.86f * shine, 0.92f)
                .setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(x / radius, y / PUDDLE_HEIGHT, z / radius);
    }
}
