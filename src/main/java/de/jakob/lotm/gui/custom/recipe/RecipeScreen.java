package de.jakob.lotm.gui.custom.recipe;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class RecipeScreen extends AbstractContainerScreen<RecipeMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/recipes/recipe.png");
    private static final ResourceLocation GLOW =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/recipes/recipe_glow.png");

    private static final int PANEL_W = 192;
    private static final int PANEL_H = 144;

    private static final int CX = 96;
    private static final int BUS_Y = 67;
    private static final int LINE_START_Y = 58;
    private static final int DROP_END_Y = 89;
    private static final int RING_CX = 96, RING_CY = 100, RING_R = 24, RING_NODES = 22;

    private static final int TITLE_COLOR = 0xF0DCA0;
    private static final int CAPTION_COLOR = 0xA89BC8;
    private static final int GOLD = 0xFFE29A;
    private static final int LAVENDER = 0xB894FF;
    private static final int PULSE = 0xEBD8FF;

    private static final int STAR_COUNT = 28;
    private final int[] starX = new int[STAR_COUNT];
    private final int[] starY = new int[STAR_COUNT];
    private final float[] starPhase = new float[STAR_COUNT];
    private final float[] starSpeed = new float[STAR_COUNT];

    private int tickCount;
    private final float[] glow = new float[RecipeMenu.SLOT_COUNT];
    private final float[] glowO = new float[RecipeMenu.SLOT_COUNT];

    public RecipeScreen(RecipeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = PANEL_W;
        this.imageHeight = PANEL_H;
        generateStars();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        tickCount++;
        for (int i = 0; i < glow.length; i++) {
            glowO[i] = glow[i];
            glow[i] += ((hasItem(i) ? 1f : 0f) - glow[i]) * 0.2f;
        }
    }

    private boolean hasItem(int slot) {
        return slot < menu.slots.size() && menu.slots.get(slot).hasItem();
    }

    private void generateStars() {
        int n = 0;
        for (int attempt = 0; n < STAR_COUNT && attempt < 4000; attempt++) {
            int h = hash(attempt, 7);
            int x = 8 + (h % (PANEL_W - 16));
            int y = 8 + ((h >> 11) % (PANEL_H - 16));
            if (blocked(x, y)) continue;
            starX[n] = x;
            starY[n] = y;
            starPhase[n] = (hash(attempt, 8) & 1023) / 1023f * Mth.TWO_PI;
            starSpeed[n] = 0.04f + (hash(attempt, 9) & 255) / 255f * 0.10f;
            n++;
        }
    }

    private static boolean blocked(int x, int y) {
        for (int[] p : RecipeMenu.SLOT_POS) {
            if (x >= p[0] - 5 && x <= p[0] + 21 && y >= p[1] - 5 && y <= p[1] + 21) return true;
        }
        if (x >= 36 && x <= 156 && y >= 3 && y <= 26) return true;
        float dx = x - RING_CX, dy = y - RING_CY;
        return dx * dx + dy * dy < 30 * 30;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        int tw = this.font.width(this.title);
        g.drawString(this.font, this.title, (PANEL_W - tw) / 2, 9, TITLE_COLOR, true);

        drawCentered(g, "Supplementary Ingredients", 28);
        drawCentered(g, "Main Ingredient", 129);
    }

    private void drawCentered(GuiGraphics g, String text, int y) {
        g.drawString(this.font, text, (PANEL_W - this.font.width(text)) / 2, y, CAPTION_COLOR, false);
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        float anim = tickCount + pt;
        float appear = Mth.clamp(anim / 10f, 0f, 1f);

        draw(g, BACKGROUND, x, y, 0, 0, PANEL_W, PANEL_H, PANEL_W, PANEL_H, 1f, 1f, 1f, 1f, false);

        renderStars(g, x, y, anim, appear);
        renderFeedLines(g, x, y, pt, anim, appear);
        renderRing(g, x, y, pt, anim, appear);
        renderSlots(g, x, y, pt, anim, appear);

        g.setColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderStars(GuiGraphics g, int x, int y, float anim, float appear) {
        for (int i = 0; i < STAR_COUNT; i++) {
            float s = 0.5f + 0.5f * Mth.sin(anim * starSpeed[i] + starPhase[i]);
            float a = s * s * appear;
            if (a < 0.05f) continue;
            int px = x + starX[i], py = y + starY[i];
            g.fill(px, py, px + 1, py + 1, argb(a, 0xE6DCFF));
            if (s > 0.85f) {                                   // brief cross-shaped glint at the peak
                int ga = argb((s - 0.85f) / 0.15f * 0.5f * appear, 0xE6DCFF);
                g.fill(px - 1, py, px, py + 1, ga);
                g.fill(px + 1, py, px + 2, py + 1, ga);
                g.fill(px, py - 1, px + 1, py, ga);
                g.fill(px, py + 1, px + 1, py + 2, ga);
            }
        }
    }

    private int slotCenterX(int supplementary) {
        return RecipeMenu.SLOT_POS[supplementary][0] + 8;
    }

    private int pathLength(int supp) {
        return (BUS_Y - LINE_START_Y) + Math.abs(slotCenterX(supp) - CX) + (DROP_END_Y - BUS_Y);
    }

    private void renderFeedLines(GuiGraphics g, int x, int y, float pt, float anim, float appear) {
        for (int i = 0; i < RecipeMenu.SUPPLEMENTARY_COUNT; i++) {
            float gl = glowOf(i, pt);
            if (gl < 0.02f) continue;

            int cx = slotCenterX(i);
            float a = gl * appear * (0.38f + 0.14f * Mth.sin(anim * 0.12f + i * 1.3f));
            int core = argb(a, LAVENDER);
            int edge = argb(a * 0.35f, LAVENDER);

            g.fill(x + cx, y + LINE_START_Y, x + cx + 1, y + BUS_Y + 1, core);
            int x0 = Math.min(cx, CX), x1 = Math.max(cx, CX);
            g.fill(x + x0, y + BUS_Y, x + x1 + 1, y + BUS_Y + 1, core);
            g.fill(x + x0, y + BUS_Y + 1, x + x1 + 1, y + BUS_Y + 2, edge);
            g.fill(x + CX - 1, y + BUS_Y, x + CX + 1, y + DROP_END_Y + 1, argb(a * 0.6f, LAVENDER));

            drawPulses(g, x, y, i, anim, gl * appear);
        }
    }

    private void drawPulses(GuiGraphics g, int x, int y, int supp, float anim, float strength) {
        int len = pathLength(supp);
        int period = len + 26;
        for (int k = 0; k < 2; k++) {
            float s = (anim * 1.15f + k * period * 0.5f + supp * 9f) % period;
            if (s > len) continue;
            for (int j = 0; j < 5; j++) {
                float sj = s - j * 2.6f;
                if (sj < 0f) continue;
                float fade = Math.min(1f, sj / 5f) * Math.min(1f, (len - sj) / 8f + 0.25f);
                float a = strength * fade * (1f - j / 5f);
                if (a <= 0.02f) continue;
                int[] p = pathPoint(supp, sj);
                int size = j == 0 ? 2 : 1;
                g.fill(x + p[0], y + p[1], x + p[0] + size, y + p[1] + size, argb(a, PULSE));
            }
        }
    }

    private int[] pathPoint(int supp, float s) {
        int cx = slotCenterX(supp);
        int down1 = BUS_Y - LINE_START_Y;
        int across = Math.abs(cx - CX);
        if (s < down1) return new int[]{cx, LINE_START_Y + (int) s};
        s -= down1;
        if (s < across) return new int[]{cx + (int) (cx < CX ? s : -s), BUS_Y};
        s -= across;
        return new int[]{CX - 1, BUS_Y + (int) s};
    }

    private void renderRing(GuiGraphics g, int x, int y, float pt, float anim, float appear) {
        float energy = 0f;
        for (int i = 0; i < glow.length; i++) energy += glowOf(i, pt);
        energy = energy / glow.length * appear;
        if (energy < 0.02f) return;

        float theta = anim * 0.045f;

        for (int s = 0; s < 2; s++) {
            float base = theta + s * Mth.PI;
            int rgb = s == 0 ? GOLD : LAVENDER;
            for (int t = 0; t < 8; t++) {
                double a = base - t * 0.09;
                int px = x + (int) Math.round(RING_CX + RING_R * Math.cos(a)) - (t == 0 ? 1 : 0);
                int py = y + (int) Math.round(RING_CY + RING_R * Math.sin(a)) - (t == 0 ? 1 : 0);
                int size = t == 0 ? 2 : 1;
                g.fill(px, py, px + size, py + size, argb(energy * (1f - t / 8f), rgb));
            }
        }

        for (int k = 0; k < RING_NODES; k++) {
            double a = -Math.PI / 2 + k * Math.PI * 2 / RING_NODES;
            double dGold = angularDistance(a, theta);
            double dViolet = angularDistance(a, theta + Math.PI);
            double d = Math.min(dGold, dViolet);
            if (d > 0.35) continue;
            float f = (float) (1.0 - d / 0.35);
            int nx = (int) Math.floor(RING_CX + RING_R * Math.cos(a) - 1 + 0.5);
            int ny = (int) Math.floor(RING_CY + RING_R * Math.sin(a) - 1 + 0.5);
            g.fill(x + nx, y + ny, x + nx + 2, y + ny + 2, argb(f * energy, dGold < dViolet ? 0xFFF0C0 : 0xE0CCFF));
        }
    }

    private static double angularDistance(double a, double b) {
        double d = Math.abs(a - b) % (Math.PI * 2);
        return d > Math.PI ? Math.PI * 2 - d : d;
    }

    private void renderSlots(GuiGraphics g, int x, int y, float pt, float anim, float appear) {
        for (int i = 0; i < RecipeMenu.SLOT_COUNT; i++) {
            int sx = x + RecipeMenu.SLOT_POS[i][0];
            int sy = y + RecipeMenu.SLOT_POS[i][1];
            float gl = glowOf(i, pt);

            if (gl > 0.02f) {
                boolean main = i == RecipeMenu.MAIN_SLOT;
                float breathe = 0.5f + 0.5f * Mth.sin(anim * (main ? 0.10f : 0.07f) + i * 1.1f);
                float a = gl * appear * (main ? 0.45f + 0.35f * breathe : 0.28f + 0.14f * breathe);
                float r = main ? 1.0f : 0.80f, gr = main ? 0.85f : 0.68f, b = main ? 0.60f : 1.0f;

                g.pose().pushPose();
                g.pose().translate(sx + 8, sy + 8, 0);
                g.pose().scale(1.2f, 1.2f, 1f);                  // frames are 20x20, the glow texture is made for 18x18
                draw(g, GLOW, -12, -12, 0, 0, 24, 24, 24, 24, r, gr, b, a, true);
                g.pose().popPose();
            } else if (i != RecipeMenu.MAIN_SLOT) {
                g.fill(sx + 5, sy + 8, sx + 11, sy + 9, argb(0.30f * appear, 0xA690D0));
            }
        }
    }

    private float glowOf(int slot, float pt) {
        return Mth.lerp(pt, glowO[slot], glow[slot]);
    }

    private static int argb(float alpha, int rgb) {
        int a = Mth.clamp((int) (alpha * 255f), 0, 255);
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    private static int hash(int a, int b) {
        int h = a * 0x9E3779B1 + b * 0x85EBCA6B;
        h ^= h >>> 15;
        h *= 0x2C1B3C6D;
        h ^= h >>> 12;
        h *= 0x297A2D39;
        h ^= h >>> 15;
        return h & 0x7FFFFFFF;
    }

    private void draw(GuiGraphics g, ResourceLocation texture, int x, int y, int u, int v, int w, int h,
                      int texW, int texH, float r, float gr, float b, float a, boolean additive) {
        RenderSystem.enableBlend();
        if (additive) {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        } else {
            RenderSystem.defaultBlendFunc();
        }
        g.setColor(r, gr, b, a);
        g.blit(texture, x, y, u, v, w, h, texW, texH);
        g.setColor(1f, 1f, 1f, 1f);
        if (additive) RenderSystem.defaultBlendFunc();
    }
}