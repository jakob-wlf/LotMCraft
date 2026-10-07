package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.data.ClientSacrificeCache;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;


@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class StatusPanelRenderer {

    
    public static final HashSet<String> activeToggleAbilities = new HashSet<>();

    private static float smoothSpirit = -1f;
    private static float smoothSanity = -1f;

    
    private static final int ORIGIN_X = 6;
    private static final int ORIGIN_Y = 6;
    private static final int MEDALLION_SIZE = 28;
    private static final int TUBE_W = 110;
    private static final int TUBE_H = 12;
    private static final int DIAL_SIZE = 28;
    private static final int SOCKET_SIZE = 22;
    private static final int SOCKET_STEP = 24;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.isDeadOrDying()) {
            ClientSacrificeCache.resetSacrificeDuration();
            return;
        }
        ClientSacrificeCache.tickDown();
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "status_panel"),
                StatusPanelRenderer::render);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;

        UUID id = mc.player.getUUID();
        if (!ClientBeyonderCache.isBeyonder(id)) {
            smoothSpirit = -1f;
            smoothSanity = -1f;
            return;
        }

        String pathway = ClientBeyonderCache.getPathway(id);
        int sequence = ClientBeyonderCache.getSequence(id);
        int color = HudTheme.pathwayColor(pathway);
        float time = HudTheme.time(mc, dt);
        float smooth = HudTheme.smoothFactor(dt.getRealtimeDeltaTicks(), 0.8f);

        
        float max = BeyonderData.getMaxSpirituality(pathway, sequence, mc.player);
        float spiritTarget = max > 0 ? HudTheme.clamp01(ClientBeyonderCache.getSpirituality(id) / max) : 0f;
        smoothSpirit = smoothSpirit < 0 ? spiritTarget : smoothSpirit + (spiritTarget - smoothSpirit) * smooth;
        if (Math.abs(smoothSpirit - spiritTarget) < 0.002f) smoothSpirit = spiritTarget;

        float sanityTarget = HudTheme.clamp01(mc.player.getData(ModAttachments.SANITY_COMPONENT.get()).getSanity());
        smoothSanity = smoothSanity < 0 ? sanityTarget : smoothSanity + (sanityTarget - smoothSanity) * smooth;
        if (Math.abs(smoothSanity - sanityTarget) < 0.002f) smoothSanity = sanityTarget;

        int tubeX = ORIGIN_X + MEDALLION_SIZE - 6;
        int tubeY = ORIGIN_Y + (MEDALLION_SIZE - TUBE_H) / 2;

        
        HudWidgets.drawTube(g, tubeX, tubeY, TUBE_W, TUBE_H, smoothSpirit, color, time, smoothSpirit < 0.2f);

        
        renderMedallion(g, mc, ORIGIN_X, ORIGIN_Y, sequence, time);

        
        if (sanityTarget <= 0.99f || smoothSanity <= 0.99f) {
            float alpha = HudTheme.smoothstep((1 - smoothSanity) / 0.05f);
            int dialX = tubeX + TUBE_W - 8;
            int dialY = ORIGIN_Y + (MEDALLION_SIZE - DIAL_SIZE) / 2;
            HudWidgets.drawDial(g, dialX, dialY, DIAL_SIZE, smoothSanity, time, alpha);
        }

        
        renderFuse(g, tubeX + 4, tubeY + TUBE_H + 2, TUBE_W - 8, time);

        
        renderToggles(g, ORIGIN_X, ORIGIN_Y + MEDALLION_SIZE + 3, color, time);
    }

    private static void renderMedallion(GuiGraphics g, Minecraft mc, int x, int y, int sequence, float time) {
        
        HudWidgets.blitRotated(g, HudTheme.MEDALLION_COG, x, y, MEDALLION_SIZE, MEDALLION_SIZE,
                time * 0.8f, 0xFFFFFFFF);
        HudWidgets.blitFull(g, HudTheme.MEDALLION, x, y, MEDALLION_SIZE, MEDALLION_SIZE);

        String text = String.valueOf(sequence);
        int tw = mc.font.width(text);
        int tx = x + (MEDALLION_SIZE - tw) / 2;
        int ty = y + (MEDALLION_SIZE - mc.font.lineHeight) / 2 + 1;
        g.drawString(mc.font, text, tx, ty, HudTheme.PARCHMENT, true);
    }

    private static void renderFuse(GuiGraphics g, int x, int y, int width, float time) {
        int total = ClientSacrificeCache.getTotalTicks();
        int remaining = ClientSacrificeCache.getRemainingTicks();
        if (total <= 0 || remaining <= 0) return;

        float frac = HudTheme.clamp01((float) remaining / total);
        int burn = Math.max(1, Math.round(width * frac));

        
        g.fill(x, y, x + burn, y + 2, 0xFF8A6A3A);
        g.fill(x, y, x + burn, y + 1, 0xFFB8893B);

        
        int sx = x + burn;
        float flick = 0.6f + 0.4f * (float) Math.abs(Math.sin(time * 1.7f) * Math.sin(time * 0.6f + 2f));
        g.fill(sx - 3, y - 1, sx + 2, y + 3, HudTheme.withAlpha(HudTheme.EMBER, 0.35f * flick));
        g.fill(sx - 1, y - 1, sx + 1, y + 3, HudTheme.withAlpha(HudTheme.EMBER_BRIGHT, flick));
        g.fill(sx - 1, y, sx, y + 2, 0xFFFFF2C0);

        
        for (int i = 0; i < 3; i++) {
            float p = HudTheme.frac(time * 0.05f + i * 0.33f);
            int px = sx + (int) (Math.sin(i * 12.9f + time * 0.2f) * 3f);
            int py = y - 1 - (int) (p * 6f);
            g.fill(px, py, px + 1, py + 1, HudTheme.withAlpha(HudTheme.EMBER_BRIGHT, 1f - p));
        }
    }

    private static void renderToggles(GuiGraphics g, int startX, int y, int ringColor, float time) {
        if (activeToggleAbilities.isEmpty()) return;

        
        List<String> ids = new ArrayList<>(activeToggleAbilities);
        ids.sort(String::compareTo);

        int x = startX;
        for (String abilityId : ids) {
            Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
            if (ability == null) continue;

            HudWidgets.drawSocket(g, x, y, SOCKET_SIZE, ability.getTextureLocation(), ringColor, time);
            x += SOCKET_STEP;
        }
    }

    public static void clearCache() {
        activeToggleAbilities.clear();
        smoothSpirit = -1f;
        smoothSanity = -1f;
    }
}