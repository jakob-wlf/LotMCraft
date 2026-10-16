package de.jakob.lotm.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.data.ClientLuckPerceptionCache;
import de.jakob.lotm.util.data.ClientRecognitionCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public final class RecognitionOverlayRenderer {

    private RecognitionOverlayRenderer() {}

    private static final boolean SEE_THROUGH = false;
    private static final float FADE_START = 28f;
    private static final float FADE_END = 44f;
    private static final float SCALE = 0.03f;
    private static final float BASE_HEIGHT = 0.75f;
    private static final float LUCK_LABEL_OFFSET = 0.8f;
    private static final int MAX_NAME_WIDTH = 160;
    private static final int FULL_LIGHT = 0xF000F0;

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        if (!ClientRecognitionCache.isActive() || !ClientRecognitionCache.hasTarget()) return;

        LivingEntity entity = event.getEntity();
        if (entity.getId() != ClientRecognitionCache.getTargetId()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || entity == mc.player) return;

        float dist = mc.player.distanceTo(entity);
        float alpha = 1f - HudTheme.clamp01((dist - FADE_START) / (FADE_END - FADE_START));
        if (alpha < 0.1f) return;

        Font font = mc.font;
        float time = HudTheme.time(mc, mc.getTimer());
        float bob = (float) Math.sin(time * 0.12f + entity.getId() * 1.7f) * 0.8f;

        String path = ClientRecognitionCache.getPathway();
        String pathFormatted = path.isEmpty() ? "Unknown" : path.substring(0, 1).toUpperCase() + path.substring(1);
        int pathColor = HudTheme.withAlpha(HudTheme.pathwayColor(path.toLowerCase()), alpha);
        int brass = HudTheme.withAlpha(HudTheme.BRASS, alpha);
        int parchment = HudTheme.withAlpha(HudTheme.PARCHMENT, alpha);
        int parchmentDim = HudTheme.withAlpha(HudTheme.PARCHMENT_DIM, alpha);

        String name = font.plainSubstrByWidth(ClientRecognitionCache.getName(), MAX_NAME_WIDTH);
        String sep = "  |  ";
        String seq = "Seq " + ClientRecognitionCache.getSequence();

        float line2W = font.width(pathFormatted) + font.width(sep) + font.width(seq);
        float lineGap = font.lineHeight + 2;
        float totalH = font.lineHeight + lineGap;

        float lift = BASE_HEIGHT + (ClientLuckPerceptionCache.getLuck(entity.getId()) != null ? LUCK_LABEL_OFFSET : 0f);

        PoseStack pose = event.getPoseStack();
        MultiBufferSource buffers = event.getMultiBufferSource();
        Font.DisplayMode mode = SEE_THROUGH ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL;

        pose.pushPose();
        pose.translate(0, entity.getBbHeight() + lift, 0);
        pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        pose.scale(SCALE, -SCALE, SCALE);

        Matrix4f m = pose.last().pose();
        float y0 = -totalH / 2f + bob;

        font.drawInBatch(name, -font.width(name) / 2f, y0, parchment, true, m, buffers, mode, 0, FULL_LIGHT);

        float x = -line2W / 2f;
        float y1 = y0 + lineGap;
        font.drawInBatch(pathFormatted, x, y1, pathColor, true, m, buffers, mode, 0, FULL_LIGHT);
        x += font.width(pathFormatted);
        font.drawInBatch(sep, x, y1, brass, true, m, buffers, mode, 0, FULL_LIGHT);
        x += font.width(sep);
        font.drawInBatch(seq, x, y1, parchmentDim, true, m, buffers, mode, 0, FULL_LIGHT);

        pose.popPose();
    }
}