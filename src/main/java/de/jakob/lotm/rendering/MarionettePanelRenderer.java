package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class MarionettePanelRenderer {

    public static HashMap<UUID, MarionetteInfos> currentMarionette = new HashMap<>();
    private static final Map<UUID, MarionetteInfos> cachedMarionette = new HashMap<>();
    private static final Map<UUID, Long> nullSinceTick = new HashMap<>();
    private static final Map<UUID, Double> smoothedHealth = new HashMap<>();

    private static final long GRACE_TICKS = 10;

    private static final int CARD_W = 140;
    private static final int CARD_H = 50;
    private static final int CARD_Y = 14;
    private static final int CARD_MARGIN_RIGHT = 16;
    private static final int PAD = 10;
    private static final int TUBE_H = 12;
    private static final int WORM_SIZE = 12;
    private static final float LABEL_SCALE = 0.7f;

    private static final ResourceLocation WORM_OF_SPIRIT = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/misc/worm_of_spirit.png");

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "marionette_overlay"),
                MarionettePanelRenderer::render);
    }

    @SubscribeEvent
    public static void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        currentMarionette.remove(uuid);
        cachedMarionette.remove(uuid);
        nullSinceTick.remove(uuid);
        smoothedHealth.remove(uuid);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        UUID uuid = mc.player.getUUID();
        long now = mc.level.getGameTime();

        
        MarionetteInfos infos = currentMarionette.get(uuid);
        if (infos != null) {
            cachedMarionette.put(uuid, infos);
            nullSinceTick.remove(uuid);
        } else if (cachedMarionette.containsKey(uuid)) {
            long since = nullSinceTick.computeIfAbsent(uuid, k -> now);
            if (now - since < GRACE_TICKS) infos = cachedMarionette.get(uuid);
        }

        if (infos == null) {
            smoothedHealth.remove(uuid);
            return;
        }

        float time = HudTheme.time(mc, dt);
        int screenW = mc.getWindow().getGuiScaledWidth();

        int bob = Math.round((float) Math.sin(time * 0.06f));
        int x = screenW - CARD_W - CARD_MARGIN_RIGHT;
        int y = CARD_Y + bob;

        
        renderStrings(g, x, y, time);

        
        HudWidgets.drawFrame(g, x, y, CARD_W, CARD_H);
        float accent = 0.12f + 0.12f * HudTheme.pulse(time, 0.1f);
        g.renderOutline(x + 2, y + 2, CARD_W - 4, CARD_H - 4, HudTheme.withAlpha(HudTheme.MARIONETTE_ACCENT, accent));

        
        Component label = Component.translatable("lotm.marionette");
        int labelW = (int) (mc.font.width(label) * LABEL_SCALE);
        g.pose().pushPose();
        g.pose().translate(x + (CARD_W - labelW) / 2f, y + 8, 0);
        g.pose().scale(LABEL_SCALE, LABEL_SCALE, 1f);
        g.drawString(mc.font, label, 0, 0, HudTheme.PARCHMENT_DIM, false);
        g.pose().popPose();

        
        int maxNameW = CARD_W - 2 * PAD - (infos.hasWorm() ? WORM_SIZE + 4 : 0);
        String name = mc.font.plainSubstrByWidth(infos.name(), maxNameW);
        int nameW = mc.font.width(name);
        int totalW = nameW + (infos.hasWorm() ? WORM_SIZE + 4 : 0);
        int nameX = x + (CARD_W - totalW) / 2;
        int nameY = y + 8 + (int) Math.ceil(mc.font.lineHeight * LABEL_SCALE) + 2;
        g.drawString(mc.font, name, nameX, nameY, HudTheme.PARCHMENT, true);
        if (infos.hasWorm()) {
            HudWidgets.drawIcon(g, WORM_OF_SPIRIT, nameX + nameW + 4, nameY + (mc.font.lineHeight - WORM_SIZE) / 2, WORM_SIZE);
        }

        
        double current = smoothedHealth.getOrDefault(uuid, infos.health());
        double target = infos.health();
        double next = current + (target - current) * HudTheme.smoothFactor(dt.getRealtimeDeltaTicks(), 0.82f);
        if (Math.abs(next - target) < 0.05) next = target;
        smoothedHealth.put(uuid, next);

        float fill = infos.maxHealth() > 0 ? HudTheme.clamp01((float) (next / infos.maxHealth())) : 0f;
        int tubeW = CARD_W - 2 * PAD;
        int tubeX = x + PAD;
        int tubeY = y + CARD_H - TUBE_H - 8;
        HudWidgets.drawTube(g, tubeX, tubeY, tubeW, TUBE_H, fill, HudTheme.healthColor(fill), time, fill < 0.25f);

        String healthText = Math.round(infos.health()) + " \u2764";
        int htW = mc.font.width(healthText);
        g.drawString(mc.font, healthText, tubeX + (tubeW - htW) / 2, tubeY + (TUBE_H - mc.font.lineHeight) / 2 + 1,
                0xFFFFFFFF, true);

        
        Component key = LOTMCraft.nextMarionetteKey.getTranslatedKeyMessage();
        HudWidgets.drawKeycap(g, mc.font, x + CARD_W / 2, y + CARD_H - 4, key);
    }

    private static void renderStrings(GuiGraphics g, int cardX, int cardY, float time) {
        int endY = cardY + 6;
        int color = HudTheme.withAlpha(HudTheme.PARCHMENT, 0.55f);
        int[] anchors = new int[]{cardX + 12, cardX + CARD_W - 12};

        for (int sx : anchors) {
            for (int row = 0; row < endY; row++) {
                float k = row / (float) Math.max(1, endY);
                int off = Math.round((float) Math.sin(time * 0.05f + sx * 0.3f) * 1.5f * (float) Math.sin(k * Math.PI));
                g.fill(sx + off, row, sx + off + 1, row + 1, color);
            }
        }
    }

    public record MarionetteInfos(String name, double health, double maxHealth, boolean hasWorm) {
    }

    public static void clearCache() {
        currentMarionette.clear();
        cachedMarionette.clear();
        nullSinceTick.clear();
        smoothedHealth.clear();
    }
}