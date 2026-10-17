package de.jakob.lotm.gui.custom.honorific_names;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.HonorificNamesRespondPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.HonorificName;
import de.jakob.lotm.util.playerMap.PendingPrayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Random;

public class HonorificNamesScreen extends AbstractContainerScreen<HonorificNamesMenu> {

    private static final String MOD_ID = LOTMCraft.MOD_ID;

    private static ResourceLocation tex(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/honorific_names/" + name);
    }

    private static final ResourceLocation TEX_BG      = tex("background.png");   // 280x240
    private static final ResourceLocation TEX_GLOW    = tex("border_glow.png");  // 280x240
    private static final ResourceLocation TEX_CARD    = tex("prayer_card.png");  // 244x42
    private static final ResourceLocation TEX_BUTTON  = tex("button.png");       // 64x32
    private static final ResourceLocation TEX_RING    = tex("rune_ring.png");    // 96x96
    private static final ResourceLocation TEX_SPARKLE = tex("sparkle.png");      // 4 frames of 8x8

    private static final int COL_LABEL = 0xFFA898E8;
    private static final int COL_TEXT  = 0xFFE4DCF5;
    private static final int COL_GOLD  = 0xFFFFD27A;

    private static final int W = 280;
    private static final int H = 240;
    private static final int PADDING = 10;
    private static final int TITLE_H = 24;
    private static final int OWN_NAME_H = 100;
    private static final int DIVIDER_H = 6;
    private static final int PRAYER_ENTRY_H = 44;
    private static final int CARD_W = 244;
    private static final int CARD_H = 42;
    private static final int SCROLL_W = 12;

    private static final int PARTICLE_COUNT = 10;
    private final float[] pX = new float[PARTICLE_COUNT];
    private final float[] pY = new float[PARTICLE_COUNT];
    private final float[] pSpeed = new float[PARTICLE_COUNT];
    private final float[] pPhase = new float[PARTICLE_COUNT];
    private final float[] pFreq = new float[PARTICLE_COUNT];

    private int animTicks = 0;
    private float time = 0f; // ticks + partialTick

    private int prayerScrollOffset = 0;

    public HonorificNamesScreen(HonorificNamesMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = W;
        this.imageHeight = H;

        Random r = new Random(1337);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            pX[i] = 8 + r.nextFloat() * (W - 16);
            pY[i] = r.nextFloat() * (H - 12);
            pSpeed[i] = 0.12f + r.nextFloat() * 0.25f;
            pPhase[i] = r.nextFloat() * 6.28f;
            pFreq[i] = 0.6f + r.nextFloat() * 1.2f;
        }
    }

    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }

    private int listHeight() {
        return H - TITLE_H - OWN_NAME_H - DIVIDER_H - PADDING;
    }

    private int visibleCount() {
        return Math.max(1, listHeight() / PRAYER_ENTRY_H);
    }

    private int listTop() {
        return topPos + TITLE_H + OWN_NAME_H + DIVIDER_H + 11;
    }

    private static int lerpColor(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return 0xFF000000
                | ((int) (ar + (br - ar) * t) << 16)
                | ((int) (ag + (bg - ag) * t) << 8)
                | (int) (ab + (bb - ab) * t);
    }


    private void rebuildButtons() {
        this.clearWidgets();

        if (menu.getSequence() < 4) {
            this.addRenderableWidget(new MysticButton(
                    leftPos + W - PADDING - 80, topPos + TITLE_H + OWN_NAME_H - 20, 80, 16,
                    Component.literal("Set Name"),
                    btn -> Minecraft.getInstance().setScreen(
                            new SetHonorificNameScreen(menu.getPathway(), menu.getSequence()))));
        }

        List<PendingPrayer> prayers = menu.getPendingPrayers();
        int listTop = listTop();
        int visibleCount = visibleCount();

        for (int i = 0; i < visibleCount; i++) {
            int idx = i + prayerScrollOffset;
            if (idx >= prayers.size()) break;

            int entryY = listTop + i * PRAYER_ENTRY_H;
            int btnIdx = idx;

            this.addRenderableWidget(new MysticButton(
                    leftPos + PADDING + 8, entryY + 22, 108, 16,
                    Component.literal("Send Message"),
                    btn -> respondToPrayer(btnIdx, false)));

            this.addRenderableWidget(new MysticButton(
                    leftPos + PADDING + 8 + 112, entryY + 22, 108, 16,
                    Component.literal("Teleport"),
                    btn -> respondToPrayer(btnIdx, true)));
        }

        if (prayers.size() > visibleCount) {
            int scrollX = leftPos + PADDING + CARD_W + 4;
            int areaTop = listTop - 2;
            int areaH = visibleCount * PRAYER_ENTRY_H - 4;

            this.addRenderableWidget(new MysticButton(scrollX, areaTop, SCROLL_W, 12,
                    Component.literal("\u25B2"),
                    btn -> {
                        if (prayerScrollOffset > 0) {
                            prayerScrollOffset--;
                            rebuildButtons();
                        }
                    }));

            this.addRenderableWidget(new MysticButton(scrollX, areaTop + areaH - 12, SCROLL_W, 12,
                    Component.literal("\u25BC"),
                    btn -> {
                        int maxScroll = Math.max(0, prayers.size() - visibleCount);
                        if (prayerScrollOffset < maxScroll) {
                            prayerScrollOffset++;
                            rebuildButtons();
                        }
                    }));
        }
    }

    private void respondToPrayer(int index, boolean teleport) {
        List<PendingPrayer> prayers = menu.getPendingPrayers();
        if (index >= 0 && index < prayers.size()) {
            PendingPrayer prayer = prayers.get(index);
            PacketHandler.sendToServer(new HonorificNamesRespondPacket(prayer.senderUUID(), teleport));
            prayers.remove(index);
            prayerScrollOffset = Math.min(prayerScrollOffset, Math.max(0, prayers.size() - visibleCount()));
            rebuildButtons();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.animTicks++;
        this.time = animTicks + partialTick;
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        gfx.blit(TEX_BG, x, y, 0, 0, W, H, W, H);

        float cx = x + W / 2f;
        float cy = y + TITLE_H + 4 + (OWN_NAME_H - 2) / 2f;
        float pulse = (float) (Math.sin(time * 0.05f) * 0.5 + 0.5);
        drawRing(gfx, cx, cy, 96, time * 0.45f, 0.14f + 0.08f * pulse);
        drawRing(gfx, cx, cy, 60, -time * 0.8f, 0.10f + 0.08f * (1f - pulse));

        drawParticles(gfx, x, y);

        float glow = 0.20f + 0.40f * (float) (Math.sin(time * 0.06f) * 0.5 + 0.5);
        gfx.setColor(1f, 1f, 1f, glow);
        gfx.blit(TEX_GLOW, x, y, 0, 0, W, H, W, H);
        gfx.setColor(1f, 1f, 1f, 1f);

        renderPrayerCards(gfx, x, mouseX, mouseY);
        renderScrollbar(gfx);

        RenderSystem.disableBlend();
    }

    private void drawRing(GuiGraphics gfx, float cx, float cy, int size, float angleDeg, float alpha) {
        PoseStack pose = gfx.pose();
        pose.pushPose();
        pose.translate(cx, cy, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(angleDeg));
        gfx.setColor(1f, 1f, 1f, alpha);
        gfx.blit(TEX_RING, -size / 2, -size / 2, size, size, 0, 0, 96, 96, 96, 96);
        gfx.setColor(1f, 1f, 1f, 1f);
        pose.popPose();
    }

    private void drawParticles(GuiGraphics gfx, int x, int y) {
        final float range = H - 12;
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float py = pY[i] - time * pSpeed[i];
            py = ((py % range) + range) % range;
            float px = pX[i] + (float) Math.sin(time * 0.04f * pFreq[i] + pPhase[i]) * 5f;

            float edge = Math.min(1f, Math.min(py, range - py) / 20f);
            float tw = (float) (Math.sin(time * 0.12f * pFreq[i] + pPhase[i]) * 0.5 + 0.5);
            int frame = tw < 0.2f ? 3 : tw < 0.5f ? 0 : tw < 0.82f ? 1 : 2;
            float alpha = (0.2f + 0.8f * tw) * edge;

            if (i % 4 == 0) gfx.setColor(1f, 0.85f, 0.5f, alpha);
            else gfx.setColor(0.75f, 0.6f, 1f, alpha);

            gfx.blit(TEX_SPARKLE, x + (int) px, y + 6 + (int) py, 8, 8, frame * 8, 0, 8, 8, 32, 8);
        }
        gfx.setColor(1f, 1f, 1f, 1f);
    }

    private void renderPrayerCards(GuiGraphics gfx, int x, int mouseX, int mouseY) {
        List<PendingPrayer> prayers = menu.getPendingPrayers();
        int listTop = listTop();
        int visibleCount = visibleCount();

        for (int i = 0; i < visibleCount; i++) {
            int idx = i + prayerScrollOffset;
            if (idx >= prayers.size()) break;

            int cardX = x + PADDING;
            int cardY = listTop + i * PRAYER_ENTRY_H - 2;

            gfx.blit(TEX_CARD, cardX, cardY, 0, 0, CARD_W, CARD_H, CARD_W, CARD_H);

            boolean hovered = mouseX >= cardX && mouseX < cardX + CARD_W
                    && mouseY >= cardY && mouseY < cardY + CARD_H;
            if (hovered) {
                float p = (float) (Math.sin(time * 0.2f) * 0.5 + 0.5);
                int a = (int) (0x1E + 0x14 * p);
                gfx.fill(cardX + 4, cardY + 1, cardX + CARD_W - 1, cardY + CARD_H - 1, (a << 24) | 0xB080FF);
            }
        }
    }

    private void renderScrollbar(GuiGraphics gfx) {
        List<PendingPrayer> prayers = menu.getPendingPrayers();
        int visibleCount = visibleCount();
        if (prayers.size() <= visibleCount) return;

        int scrollX = leftPos + PADDING + CARD_W + 4;
        int areaTop = listTop() - 2;
        int areaH = visibleCount * PRAYER_ENTRY_H - 4;

        int trackTop = areaTop + 12 + 2;
        int trackH = areaH - 24 - 4;
        int trackX = scrollX + SCROLL_W / 2 - 1;

        gfx.fill(trackX, trackTop, trackX + 2, trackTop + trackH, 0xFF2A1F4A);

        int maxScroll = Math.max(1, prayers.size() - visibleCount);
        int thumbH = Math.max(8, trackH * visibleCount / prayers.size());
        int thumbY = trackTop + (trackH - thumbH) * prayerScrollOffset / maxScroll;
        gfx.fill(trackX - 1, thumbY, trackX + 3, thumbY + thumbH, 0xFFB8903F);
        gfx.fill(trackX, thumbY + 1, trackX + 2, thumbY + thumbH - 1, 0xFFFFD27A);
    }


    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        renderTitle(gfx);
        renderOwnNameSection(gfx);
        renderPendingPrayersSection(gfx);
    }

    private void renderTitle(GuiGraphics gfx) {
        String titleStr = "Honorific Name";
        int cx = (W - font.width(titleStr)) / 2;
        for (int i = 0; i < titleStr.length(); i++) {
            String ch = String.valueOf(titleStr.charAt(i));
            float wave = (float) (Math.sin(time * 0.12f - i * 0.5f) * 0.5 + 0.5);
            wave = wave * wave * wave;
            int col = lerpColor(0xFFC9B2FF, 0xFFFFE9A8, wave);
            gfx.drawString(font, ch, cx, 8, col, true);
            cx += font.width(ch);
        }
    }

    private void renderOwnNameSection(GuiGraphics gfx) {
        int sectionY = TITLE_H + 6;
        gfx.drawString(font, "Your Honorific Name:", PADDING, sectionY, COL_LABEL, false);

        HonorificName name = menu.getOwnName();
        if (name.isEmpty()) {
            String hint = "Not set \u2014 click \"Set Name\" to set it";
            gfx.drawString(font, hint, PADDING + 2, sectionY + 14, 0xFF7C7CA8, false);
        } else {
            int lineY = sectionY + 14;
            List<String> lines = name.lines();
            for (int i = 0; i < lines.size(); i++) {
                String lineLabel = "  Line " + (i + 1) + ":  ";
                gfx.drawString(font, lineLabel, PADDING + 2, lineY, 0xFF8070B8, false);
                gfx.drawString(font, lines.get(i), PADDING + 2 + font.width(lineLabel), lineY, COL_TEXT, true);
                lineY += 11;
            }
        }

        String badge = BeyonderData.pathwayInfos.containsKey(menu.getPathway())
                ? BeyonderData.pathwayInfos.get(menu.getPathway()).getSequenceName(menu.getSequence())
                : "";
        int badgeX = W - PADDING - font.width(badge) - 2;
        gfx.drawString(font, badge, badgeX, TITLE_H + 6, COL_GOLD, false);
    }

    private void renderPendingPrayersSection(GuiGraphics gfx) {
        int sectionLabelY = TITLE_H + OWN_NAME_H + DIVIDER_H;
        gfx.drawString(font, "Pending Prayers:", PADDING, sectionLabelY, COL_LABEL, false);

        List<PendingPrayer> prayers = menu.getPendingPrayers();

        if (prayers.isEmpty()) {
            String msg = "No prayers...";
            float breathe = (float) (Math.sin(time * 0.06f) * 0.5 + 0.5);
            int col = lerpColor(0xFF4A4A72, 0xFF8A82C0, breathe);
            gfx.drawString(font, msg, (W - font.width(msg)) / 2, sectionLabelY + 40, col, false);
            return;
        }

        int listTop = listTop() - topPos;
        int visibleCount = visibleCount();

        for (int i = 0; i < visibleCount; i++) {
            int idx = i + prayerScrollOffset;
            if (idx >= prayers.size()) break;

            PendingPrayer prayer = prayers.get(idx);
            int entryRelY = listTop + i * PRAYER_ENTRY_H;

            String nameLine = prayer.senderName()
                    + "  [" + prayer.senderPathway() + " / Seq " + prayer.senderSequence() + "]";
            nameLine = font.plainSubstrByWidth(nameLine, CARD_W - 16);
            gfx.drawString(font, nameLine, PADDING + 8, entryRelY + 2, COL_GOLD, true);

            String coordLine = String.format("@ %.0f, %.0f, %.0f", prayer.x(), prayer.y(), prayer.z());
            gfx.drawString(font, coordLine, PADDING + 8, entryRelY + 12, 0xFF9090BC, false);
        }

        if (prayers.size() > visibleCount) {
            String indicator = (prayerScrollOffset + 1) + " / " + prayers.size();
            gfx.drawString(font, indicator,
                    W - PADDING - font.width(indicator) - 2,
                    sectionLabelY, 0xFF8070B8, false);
        }
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<PendingPrayer> prayers = menu.getPendingPrayers();
        int maxScroll = Math.max(0, prayers.size() - visibleCount());

        if (scrollY < 0 && prayerScrollOffset < maxScroll) {
            prayerScrollOffset++;
            rebuildButtons();
            return true;
        } else if (scrollY > 0 && prayerScrollOffset > 0) {
            prayerScrollOffset--;
            rebuildButtons();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }


    private static class MysticButton extends Button {

        MysticButton(int x, int y, int w, int h, Component msg, OnPress onPress) {
            super(x, y, w, h, msg, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            boolean hov = this.isHoveredOrFocused();
            int v = hov ? 16 : 0;
            int w = this.getWidth();
            int h = this.getHeight();
            int edge = Math.min(6, w / 2);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            gfx.setColor(1f, 1f, 1f, this.active ? 1f : 0.5f);

            gfx.blit(TEX_BUTTON, getX(), getY(), edge, h, 0, v, edge, 16, 64, 32);
            if (w - 2 * edge > 0) {
                gfx.blit(TEX_BUTTON, getX() + edge, getY(), w - 2 * edge, h, 6, v, 52, 16, 64, 32);
            }
            gfx.blit(TEX_BUTTON, getX() + w - edge, getY(), edge, h, 64 - edge, v, edge, 16, 64, 32);
            gfx.setColor(1f, 1f, 1f, 1f);

            int col = hov ? 0xFFFFE9A8 : 0xFFD8C8F0;
            var font = Minecraft.getInstance().font;
            gfx.drawCenteredString(font, this.getMessage(),
                    getX() + w / 2, getY() + (h - 8) / 2, col);
        }
    }
}
