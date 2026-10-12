package de.jakob.lotm.gui.custom.pathway_selection;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.pathway_selection.PathwaySelectionMenu;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.PathwayInfos;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathwaySelectionScreen extends AbstractContainerScreen<PathwaySelectionMenu> {
    private static ResourceLocation sprite(String name) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "introspect/" + name);
    }

    private static final ResourceLocation SPR_FRAME = sprite("frame");
    private static final ResourceLocation SPR_NAMEPLATE = sprite("nameplate");
    private static final ResourceLocation SPR_DIVIDER = sprite("divider");
    private static final ResourceLocation SPR_ICON_FRAME = sprite("icon_frame");
    private static final ResourceLocation SPR_ICON_SHADOW = sprite("icon_frame_shadow");
    private static final ResourceLocation SPR_ICON_GLOW = sprite("icon_frame_glow_mask");
    private static final ResourceLocation SPR_COG_LARGE = sprite("cog_large");
    private static final ResourceLocation SPR_COG_SMALL = sprite("cog_small");
    private static final ResourceLocation SPR_TAB_IDLE = sprite("tab_idle");
    private static final ResourceLocation SPR_TAB_HOVER = sprite("tab_hover");
    private static final ResourceLocation SPR_TAB_ACTIVE = sprite("tab_active");
    private static final ResourceLocation SPR_TAB_LOCKED = sprite("tab_locked");
    private static final ResourceLocation SPR_TAB_GLOW_THIN = sprite("tab_glow_thin_mask");
    private static final ResourceLocation SPR_RAIL = sprite("scroll_rail");
    private static final ResourceLocation SPR_KNOB = sprite("scroll_knob");
    private static final ResourceLocation SPR_PANEL_INSET = sprite("panel_inset");

    private static final int TEXT_HEADING = 0xFFF4D58A;
    private static final int TEXT_NORMAL = 0xFFE8CF94;
    private static final int TEXT_BODY = 0xFFD8D0BC;
    private static final int TEXT_DIM = 0xFF8C8672;
    private static final int FALLBACK_ACCENT = 0xD9A94F;

    private static final int PANEL_W = 360;
    private static final int PANEL_H = 236;

    private static final int COLS = 4;
    private static final int VISIBLE_ROWS = 3;
    private static final int CELL = 42;
    private static final int GAP = 4;
    private static final int GRID_X = 156;
    private static final int GRID_Y = 46;
    private static final int GRID_H = VISIBLE_ROWS * CELL + (VISIBLE_ROWS - 1) * GAP;

    private static final int RAIL_X = 340;
    private static final int LEFT_X = 16;
    private static final int LEFT_W = 130;

    private static final int CONFIRM_W = 150;
    private static final int CONFIRM_H = 20;
    private static final int CONFIRM_X = (PANEL_W - CONFIRM_W) / 2;
    private static final int CONFIRM_Y = 200;

    private static final int START_SEQUENCE = 9;
    private static final long CONFIRM_TIMEOUT_MS = 5000L;

    private static final Map<String, ResourceLocation> ICON_CACHE = new HashMap<>();

    private final List<String> pathways = new ArrayList<>();
    private int selected = -1;
    private int scroll = 0;

    private boolean confirming = false;
    private long confirmStartedAt = 0L;
    private boolean sent = false;
    private long sentAt = 0L;

    private float scale = 1f;
    private int originX = 0;
    private int originY = 0;

    public PathwaySelectionScreen(PathwaySelectionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = PANEL_W;
        this.imageHeight = PANEL_H;

        this.pathways.addAll(menu.getOfferedPathways());
    }

    private void updateLayout() {
        scale = Math.min(1f, Math.min((this.width - 8f) / PANEL_W, (this.height - 8f) / PANEL_H));
        scale = Math.max(scale, 0.3f);
        originX = Math.round((this.width - PANEL_W * scale) / 2f);
        originY = Math.round((this.height - PANEL_H * scale) / 2f);
    }

    private int toLocalX(double screenX) {
        return (int) Math.floor((screenX - originX) / scale);
    }

    private int toLocalY(double screenY) {
        return (int) Math.floor((screenY - originY) / scale);
    }

    private int totalRows() {
        return (int) Math.ceil(pathways.size() / (double) COLS);
    }

    private int maxScroll() {
        return Math.max(0, totalRows() - VISIBLE_ROWS);
    }

    private static boolean inside(int px, int py, int x, int y, int w, int h) {
        return px >= x && px < x + w && py >= y && py < y + h;
    }

    private int cellAt(int lx, int ly) {
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = (row + scroll) * COLS + col;
                if (idx >= pathways.size()) return -1;
                int x = GRID_X + col * (CELL + GAP);
                int y = GRID_Y + row * (CELL + GAP);
                if (inside(lx, ly, x, y, CELL, CELL)) return idx;
            }
        }
        return -1;
    }

    private void ensureSelectedVisible() {
        if (selected < 0) return;
        int row = selected / COLS;
        if (row < scroll) scroll = row;
        else if (row >= scroll + VISIBLE_ROWS) scroll = row - VISIBLE_ROWS + 1;
        scroll = Mth.clamp(scroll, 0, maxScroll());
    }

    private void select(int index) {
        if (index == selected) return;
        selected = index;
        confirming = false;
        ensureSelectedVisible();
    }

    private static void setTint(int rgb, float alpha) {
        RenderSystem.setShaderColor(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, alpha);
    }

    private static void resetTint() {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private void drawRotated(GuiGraphics g, ResourceLocation spr, float cx, float cy, int size, float degrees) {
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(cx, cy, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(degrees));
        g.blitSprite(spr, -size / 2, -size / 2, size, size);
        pose.popPose();
    }

    private void heading(GuiGraphics g, String text, int x, int y) {
        g.drawString(this.font, Component.literal(text).withStyle(ChatFormatting.BOLD), x, y, TEXT_HEADING, true);
    }

    private void drawCentered(GuiGraphics g, String text, int centerX, int y, int color, boolean shadow) {
        g.drawString(this.font, text, centerX - this.font.width(text) / 2, y, color, shadow);
    }

    private String fit(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) return text;
        return this.font.plainSubstrByWidth(text, maxWidth - this.font.width("…")) + "…";
    }

    private static ResourceLocation iconTexture(String pathway) {
        return ICON_CACHE.computeIfAbsent(pathway, id ->
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/icons/" + id + "_icon.png"));
    }

    private static int accentOf(String pathwayId) {
        PathwayInfos info = BeyonderData.pathwayInfos.get(pathwayId);
        return info != null ? (info.color() & 0xFFFFFF) : FALLBACK_ACCENT;
    }

    private static float animTicks() {
        return (Util.getMillis() % 1_000_000L) / 50f;
    }

    private static void playClick(float pitch) {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        updateLayout();
        if (confirming && Util.getMillis() - confirmStartedAt > CONFIRM_TIMEOUT_MS) confirming = false;
        if (sent && Util.getMillis() - sentAt > 3000L) sent = false;

        int lx = toLocalX(mouseX);
        int ly = toLocalY(mouseY);
        float t = animTicks();

        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(originX, originY, 0);
        pose.scale(scale, scale, 1f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        resetTint();

        renderCogs(g, t);
        g.blitSprite(SPR_FRAME, 0, 0, PANEL_W, PANEL_H);

        renderTitle(g);
        renderGrid(g, lx, ly, t);
        renderDetails(g, lx, ly, t);
        renderConfirmButton(g, lx, ly);

        resetTint();
        pose.popPose();
    }

    private void renderCogs(GuiGraphics g, float t) {
        float large = t * 0.5f;

        drawRotated(g, SPR_COG_LARGE, 10, -2, 48, -large);
        drawRotated(g, SPR_COG_SMALL, 42, -10, 24, large * 1.25f + 15f);

        drawRotated(g, SPR_COG_LARGE, PANEL_W - 10, -2, 48, large);
        drawRotated(g, SPR_COG_SMALL, PANEL_W - 42, -10, 24, -large * 1.25f + 15f);
        resetTint();
    }

    private void renderTitle(GuiGraphics g) {
        int plateW = 190;
        g.blitSprite(SPR_NAMEPLATE, (PANEL_W - plateW) / 2, 14, plateW, 20);
        Component title = Component.literal("Choose Your Path").withStyle(ChatFormatting.BOLD);
        g.drawString(this.font, title, (PANEL_W - this.font.width(title)) / 2, 20, TEXT_HEADING, true);

        g.blitSprite(SPR_DIVIDER, 14, 37, PANEL_W - 28, 5);
        g.blitSprite(SPR_DIVIDER, 14, 194, PANEL_W - 28, 5);

        g.fill(150, GRID_Y, 151, 190, 0x66D9A94F);
    }

    private void renderGrid(GuiGraphics g, int lx, int ly, float t) {
        int hovered = cellAt(lx, ly);

        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = (row + scroll) * COLS + col;
                if (idx >= pathways.size()) break;
                int x = GRID_X + col * (CELL + GAP);
                int y = GRID_Y + row * (CELL + GAP);
                renderCell(g, idx, x, y, idx == hovered, idx == selected, t);
            }
        }

        int rail = Math.max(1, maxScroll());
        g.blitSprite(SPR_RAIL, RAIL_X, GRID_Y, 8, GRID_H);
        int knobY = GRID_Y + Math.round((GRID_H - 14) * (scroll / (float) rail));
        if (maxScroll() > 0) {
            g.blitSprite(SPR_KNOB, RAIL_X, knobY, 8, 14);
        }

        String adjacent = adjacentLine();
        if (!adjacent.isEmpty()) {
            g.drawString(this.font, fit(adjacent, 188), GRID_X, 182, TEXT_DIM, false);
        }
    }

    private void renderCell(GuiGraphics g, int idx, int x, int y, boolean hovered, boolean isSelected, float t) {
        String id = pathways.get(idx);
        int rgb = accentOf(id);
        int inner = CELL - 12;

        float b = (hovered || isSelected) ? 1f : 0.6f;
        RenderSystem.setShaderColor(b, b, b, 1f);
        g.blit(iconTexture(id), x + 6, y + 6, inner, inner, 0, 0, 62, 62, 62, 62);
        resetTint();

        g.blitSprite(SPR_ICON_SHADOW, x, y, CELL, CELL);
        g.blitSprite(SPR_ICON_FRAME, x, y, CELL, CELL);

        if (isSelected) {
            setTint(rgb, 0.55f + 0.2f * Mth.sin(t * 0.12f));
            g.blitSprite(SPR_ICON_GLOW, x, y, CELL, CELL);
            resetTint();
        } else if (hovered) {
            setTint(rgb, 0.5f);
            g.blitSprite(SPR_ICON_GLOW, x, y, CELL, CELL);
            resetTint();
        }
    }

    private String adjacentLine() {
        int idx = focusIndex(-1, -1);
        if (idx < 0) return "";
        PathwayInfos info = BeyonderData.pathwayInfos.get(pathways.get(idx));
        if (info == null) return "";
        List<String> names = new ArrayList<>();
        for (String n : info.neighboringPathways()) {
            if (n != null && !n.isEmpty() && BeyonderData.pathwayInfos.containsKey(n)) {
                names.add(Component.translatable("lotm.pathway." + n).getString());
            }
        }
        if (names.isEmpty()) return "";
        return "Adjacent paths: " + String.join(", ", names);
    }

    private int focusIndex(int lx, int ly) {
        int hovered = (lx >= 0 && ly >= 0) ? cellAt(lx, ly) : -1;
        if (hovered >= 0) return hovered;
        return selected;
    }

    private void renderDetails(GuiGraphics g, int lx, int ly, float t) {
        int idx = focusIndex(lx, ly);

        int frameSize = 74;
        int fx = LEFT_X + (LEFT_W - frameSize) / 2;
        int fy = 46;

        if (idx < 0) {
            g.blitSprite(SPR_ICON_SHADOW, fx, fy, frameSize, frameSize);
            g.blitSprite(SPR_ICON_FRAME, fx, fy, frameSize, frameSize);
            drawCentered(g, "?", fx + frameSize / 2, fy + frameSize / 2 - 4, TEXT_DIM, true);

            g.blitSprite(SPR_NAMEPLATE, LEFT_X, 124, LEFT_W, 18);
            drawCentered(g, "Select a pathway", LEFT_X + LEFT_W / 2, 129, TEXT_DIM, false);
            return;
        }

        String id = pathways.get(idx);
        PathwayInfos info = BeyonderData.pathwayInfos.get(id);
        int rgb = accentOf(id);
        int argb = 0xFF000000 | rgb;

        g.blit(iconTexture(id), fx + 6, fy + 6, 62, 62, 0, 0, 62, 62, 62, 62);
        g.blitSprite(SPR_ICON_SHADOW, fx, fy, frameSize, frameSize);
        g.blitSprite(SPR_ICON_FRAME, fx, fy, frameSize, frameSize);
        setTint(rgb, 0.55f + 0.25f * Mth.sin(t * 0.12f));
        g.blitSprite(SPR_ICON_GLOW, fx, fy, frameSize, frameSize);
        resetTint();

        String name = info != null ? info.getName() : id;
        g.blitSprite(SPR_NAMEPLATE, LEFT_X, 124, LEFT_W, 18);
        drawCentered(g, fit(name, LEFT_W - 22), LEFT_X + LEFT_W / 2, 129, argb, true);

        if (info == null) return;

        heading(g, "Begins as", LEFT_X + 4, 148);
        g.drawString(this.font, fit(info.getSequenceName(START_SEQUENCE), LEFT_W - 6), LEFT_X + 4, 158, argb, true);

        heading(g, "Ascends to", LEFT_X + 4, 170);
        g.drawString(this.font, fit(info.getSequenceName(0), LEFT_W - 6), LEFT_X + 4, 180, argb, true);
    }

    private boolean confirmEnabled() {
        return selected >= 0 && !sent;
    }

    private void renderConfirmButton(GuiGraphics g, int lx, int ly) {
        boolean enabled = confirmEnabled();
        boolean hovered = enabled && inside(lx, ly, CONFIRM_X, CONFIRM_Y, CONFIRM_W, CONFIRM_H);

        ResourceLocation spr;
        if (!enabled) spr = SPR_TAB_LOCKED;
        else if (confirming) spr = SPR_TAB_ACTIVE;
        else if (hovered) spr = SPR_TAB_HOVER;
        else spr = SPR_TAB_IDLE;

        g.blitSprite(spr, CONFIRM_X, CONFIRM_Y, CONFIRM_W, CONFIRM_H);

        String label;
        int color;
        if (!enabled) {
            label = "Select a pathway";
            color = 0xFF6E6C78;
        } else if (confirming) {
            PathwayInfos info = BeyonderData.pathwayInfos.get(pathways.get(selected));
            String name = info != null ? info.getName() : pathways.get(selected);
            int rgb = accentOf(pathways.get(selected));
            label = "Confirm: " + name + "?";
            color = 0xFF000000 | rgb;

            float pulse = 0.6f + 0.2f * Mth.sin(animTicks() * 0.15f);
            setTint(rgb, pulse);
            g.blitSprite(SPR_TAB_GLOW_THIN, CONFIRM_X, CONFIRM_Y, CONFIRM_W, CONFIRM_H);
            resetTint();
        } else {
            label = "Walk this Path";
            color = hovered ? 0xFFFFFFFF : TEXT_NORMAL;
        }

        drawCentered(g, fit(label, CONFIRM_W - 12), CONFIRM_X + CONFIRM_W / 2, CONFIRM_Y + 6, color, true);

        g.drawString(this.font, "Esc to cancel", 16, CONFIRM_Y + 6, TEXT_DIM, false);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) {
    }

    private void onConfirmPressed() {
        if (!confirmEnabled()) return;

        if (!confirming) {
            confirming = true;
            confirmStartedAt = Util.getMillis();
            playClick(1.0f);
            return;
        }

        sent = true;
        sentAt = Util.getMillis();
        playClick(1.3f);

        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, selected);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        updateLayout();
        if (button == 0) {
            int lx = toLocalX(mouseX);
            int ly = toLocalY(mouseY);

            int cell = cellAt(lx, ly);
            if (cell >= 0) {
                if (cell != selected) playClick(0.9f);
                select(cell);
                return true;
            }

            if (inside(lx, ly, CONFIRM_X, CONFIRM_Y, CONFIRM_W, CONFIRM_H)) {
                onConfirmPressed();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll() > 0 && scrollY != 0) {
            scroll = Mth.clamp(scroll - (int) Math.signum(scrollY), 0, maxScroll());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!pathways.isEmpty()) {
            int delta = 0;
            switch (keyCode) {
                case GLFW.GLFW_KEY_RIGHT -> delta = 1;
                case GLFW.GLFW_KEY_LEFT -> delta = -1;
                case GLFW.GLFW_KEY_DOWN -> delta = COLS;
                case GLFW.GLFW_KEY_UP -> delta = -COLS;
                case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                    onConfirmPressed();
                    return true;
                }
                default -> {
                }
            }
            if (delta != 0) {
                int next = selected < 0 ? 0 : Mth.clamp(selected + delta, 0, pathways.size() - 1);
                if (next != selected) playClick(0.9f);
                select(next);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}