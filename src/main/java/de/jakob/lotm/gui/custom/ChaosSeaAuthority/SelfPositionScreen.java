package de.jakob.lotm.gui.custom.ChaosSeaAuthority;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.EnvisionSelfTeleportPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Sub-screen for the "Self > Position" Envisioning entry.
 * Lets the player enter X/Y/Z coordinates and a dimension (with alias support)
 * then teleports via {@link EnvisionSelfTeleportPacket}.
 */
@OnlyIn(Dist.CLIENT)
public class SelfPositionScreen extends Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int PANEL_W = 240;
    private static final int PANEL_H = 178;

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final int C_BG      = 0xFF1A1E2A;
    private static final int C_OUTLINE = 0xFFAA8833;
    private static final int C_TITLE   = 0xFFFFCC55;
    private static final int C_LABEL   = 0xFFCCDDFF;
    private static final int C_HINT    = 0xFF99AACC;

    // ── Input fields ──────────────────────────────────────────────────────────
    private EditBox xBox;
    private EditBox yBox;
    private EditBox zBox;
    private EditBox dimBox;

    public SelfPositionScreen() {
        super(Component.literal("Envisioning – Position"));
    }

    // ── Init ──────────────────────────────────────────────────────────────────
    @Override
    protected void init() {
        super.init();
        int lx = (width  - PANEL_W) / 2;
        int ty = (height - PANEL_H) / 2;

        int fieldW  = 60;
        int fieldH  = 12;
        int labelX  = lx + 14;
        int fieldX  = lx + PANEL_W - 14 - fieldW;
        int startY  = ty + 44;
        int rowStep = 22;

        // X
        xBox = new EditBox(font, fieldX, startY, fieldW, fieldH, Component.literal("X"));
        xBox.setMaxLength(16);
        xBox.setFilter(s -> s.isEmpty() || s.equals("-") || isParsableDouble(s));
        xBox.setHint(Component.literal("0.0"));
        addRenderableWidget(xBox);

        // Y
        yBox = new EditBox(font, fieldX, startY + rowStep, fieldW, fieldH, Component.literal("Y"));
        yBox.setMaxLength(16);
        yBox.setFilter(s -> s.isEmpty() || s.equals("-") || isParsableDouble(s));
        yBox.setHint(Component.literal("64.0"));
        addRenderableWidget(yBox);

        // Z
        zBox = new EditBox(font, fieldX, startY + rowStep * 2, fieldW, fieldH, Component.literal("Z"));
        zBox.setMaxLength(16);
        zBox.setFilter(s -> s.isEmpty() || s.equals("-") || isParsableDouble(s));
        zBox.setHint(Component.literal("0.0"));
        addRenderableWidget(zBox);

        // Dimension
        int dimFieldW = 120;
        int dimFieldX = lx + PANEL_W - 14 - dimFieldW;
        dimBox = new EditBox(font, dimFieldX, startY + rowStep * 3, dimFieldW, fieldH, Component.literal("Dimension"));
        dimBox.setMaxLength(64);
        dimBox.setHint(Component.literal("overworld"));
        addRenderableWidget(dimBox);

        // Teleport button
        int btnW = 80;
        int btnY = ty + PANEL_H - 22;
        addRenderableWidget(Button.builder(Component.literal("Teleport"), btn -> onTeleport())
                .bounds(lx + (PANEL_W - btnW) / 2, btnY, btnW, 14)
                .build());

        // Back button (top-left corner of panel)
        addRenderableWidget(Button.builder(Component.literal("< Back"), btn -> onClose())
                .bounds(lx + 6, ty + 6, 40, 10)
                .build());
    }

    // ── Rendering ─────────────────────────────────────────────────────────────
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Dim world without blur shader
        g.fill(0, 0, width, height, 0x55000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        int lx = (width  - PANEL_W) / 2;
        int ty = (height - PANEL_H) / 2;

        // Panel background + outline
        g.fill(lx, ty, lx + PANEL_W, ty + PANEL_H, C_BG);
        drawOutline(g, lx, ty, PANEL_W, PANEL_H, C_OUTLINE);

        // Title
        g.drawCenteredString(font, "Self Position", lx + PANEL_W / 2, ty + 16, C_TITLE);

        // Divider under title
        g.fill(lx + 10, ty + 26, lx + PANEL_W - 10, ty + 27, C_OUTLINE);

        // Row labels
        int labelX = lx + 14;
        int startY = ty + 44;
        int rowStep = 22;
        int labelY  = startY + 2; // vertically centres 8-px font in 12-px box

        g.drawString(font, "X",          labelX, labelY,              C_LABEL, false);
        g.drawString(font, "Y",          labelX, labelY + rowStep,    C_LABEL, false);
        g.drawString(font, "Z",          labelX, labelY + rowStep * 2, C_LABEL, false);
        g.drawString(font, "Dimension",  labelX, labelY + rowStep * 3, C_LABEL, false);

        // Hint text under dimension box – two short lines kept within panel width
        int dimHintY = startY + rowStep * 3 + 14;
        g.drawString(font, "e.g. overworld, nether, end",          labelX, dimHintY,      C_HINT, false);
        g.drawString(font, "chaos_sea, river, dream, spirit, space", labelX, dimHintY + 9, C_HINT, false);

        // Render widgets on top
        super.render(g, mouseX, mouseY, partialTick);
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void onTeleport() {
        double x = parseOrZero(xBox.getValue());
        double y = parseOrZero(yBox.getValue());
        double z = parseOrZero(zBox.getValue());
        String dim = dimBox.getValue().trim();
        if (dim.isEmpty()) dim = "overworld";

        PacketHandler.sendToServer(new EnvisionSelfTeleportPacket(x, y, z, dim));
        onClose();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(new EnvisioningScreen());
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static boolean isParsableDouble(String s) {
        try { Double.parseDouble(s); return true; }
        catch (NumberFormatException e) { return false; }
    }

    private static double parseOrZero(String s) {
        if (s == null || s.isBlank()) return 0.0;
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private static void drawOutline(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x,         y,         x + w,     y + 1,     color); // top
        g.fill(x,         y + h - 1, x + w,     y + h,     color); // bottom
        g.fill(x,         y,         x + 1,     y + h,     color); // left
        g.fill(x + w - 1, y,         x + w,     y + h,     color); // right
    }
}
