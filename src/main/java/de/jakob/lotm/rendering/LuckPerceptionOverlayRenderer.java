package de.jakob.lotm.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.data.ClientLuckPerceptionCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public final class LuckPerceptionOverlayRenderer {

    private LuckPerceptionOverlayRenderer() {}

    private static final boolean SEE_THROUGH = false;
    private static final float FADE_START = 16f;
    private static final float FADE_END = 26f;
    private static final float ICON = 12f;
    private static final int FULL_LIGHT = 0xF000F0;

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        Double luck = ClientLuckPerceptionCache.getLuck(entity.getId());
        if (luck == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        float dist = mc.player.distanceTo(entity);
        float alpha = 1f - HudTheme.clamp01((dist - FADE_START) / (FADE_END - FADE_START));
        if (alpha < 0.1f) return;

        float time = HudTheme.time(mc, mc.getTimer());
        float bob = (float) Math.sin(time * 0.15f + entity.getId() * 1.7f) * 1.5f;

        PoseStack pose = event.getPoseStack();
        MultiBufferSource buffers = event.getMultiBufferSource();
        Font font = mc.font;

        pose.pushPose();
        pose.translate(0, entity.getBbHeight() + 0.85f, 0);
        pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        pose.scale(0.05f, -0.05f, 0.05f);

        String text = HudTheme.formatLuck(luck);
        int textW = font.width(text);
        float total = ICON + 2 + textW;
        float x0 = -total / 2f;
        float y0 = -ICON / 2f + bob;

        RenderType type = SEE_THROUGH
                ? RenderType.textSeeThrough(HudTheme.LUCK_CLOVER)
                : RenderType.text(HudTheme.LUCK_CLOVER);
        VertexConsumer vc = buffers.getBuffer(type);
        Matrix4f m = pose.last().pose();
        int a = (int) (alpha * 255f);
        vc.addVertex(m, x0, y0, 0).setColor(255, 255, 255, a).setUv(0f, 0f).setLight(FULL_LIGHT);
        vc.addVertex(m, x0, y0 + ICON, 0).setColor(255, 255, 255, a).setUv(0f, 1f).setLight(FULL_LIGHT);
        vc.addVertex(m, x0 + ICON, y0 + ICON, 0).setColor(255, 255, 255, a).setUv(1f, 1f).setLight(FULL_LIGHT);
        vc.addVertex(m, x0 + ICON, y0, 0).setColor(255, 255, 255, a).setUv(1f, 0f).setLight(FULL_LIGHT);

        int color = HudTheme.withAlpha(HudTheme.luckColor(luck), alpha);
        font.drawInBatch(text, x0 + ICON + 2, y0 + (ICON - font.lineHeight) / 2f + 1, color, true,
                pose.last().pose(), buffers,
                SEE_THROUGH ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL,
                0, FULL_LIGHT);

        pose.popPose();
    }
}