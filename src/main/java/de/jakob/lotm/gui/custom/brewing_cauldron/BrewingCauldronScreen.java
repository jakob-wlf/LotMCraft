package de.jakob.lotm.gui.custom.brewing_cauldron;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Locale;

public class BrewingCauldronScreen extends AbstractContainerScreen<BrewingCauldronMenu> {

    private static final ResourceLocation BACKGROUND = tex("brewing_cauldron_gui");
    private static final ResourceLocation LIQUID = tex("liquid_strip");
    private static final ResourceLocation STREAM = tex("stream");
    private static final ResourceLocation DROPLET = tex("droplet");
    private static final ResourceLocation GLOW = tex("slot_glow");
    private static final ResourceLocation GHOST = tex("recipe_ghost");
    private static final ResourceLocation PARTICLES = tex("particles");

    private static ResourceLocation tex(String name) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID,
                "textures/gui/brewing_cauldron/" + name + ".png");
    }

    private static final int BG_TEX_SIZE = 256;

    private static final int SLOT_FIRST = 36;
    private static final int[][] SLOT_POS = {{48, 37}, {112, 37}, {80, 72}, {80, 122}, {152, 122}, {14, 37}, {146, 37}};
    private static final int S_OUTPUT = 3;
    private static final int S_RECIPE = 4;

    private static final int LIQ_X = 32, LIQ_Y = 64, LIQ_W = 112, LIQ_H = 24, LIQ_FRAMES = 8;
    private static final int LIQ_CX = 88, LIQ_CY = 76;

    private static final int STREAM_X = 85, STREAM_Y = 89, STREAM_W = 6, STREAM_H = 31, STREAM_FRAMES = 4;

    private static final int[][] DIAG_LINES = {{65, 54, 79, 70, 1, 0, 5}, {110, 54, 96, 70, -1, 1, 6}};
    private static final int[][] H_LINES = {
            {65, 87, 44, 0}, {110, 88, 44, 1},
            {31, 46, 44, 5}, {144, 129, 44, 6}
    };

    private static final int THREAD_Y = 130, THREAD_X_CAULDRON = 121, THREAD_X_SLOT = 149;

    private static final int TITLE_COLOR = 0xE2D6F2;

    private int tickCount;

    private float brew, brewO;
    private float liquidPhase, liquidPhaseO;
    private float progress, progressO;
    private final float[] glow = new float[SLOT_POS.length];
    private final float[] glowO = new float[SLOT_POS.length];
    private boolean wasCrafting;
    private int flashWait;
    private int flashTicks;
    private static final int FLASH_LENGTH = 16;

    public BrewingCauldronScreen(BrewingCauldronMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 240;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        tickCount++;

        boolean crafting = menu.isCrafting();

        brewO = brew;
        brew += ((crafting ? 1f : 0f) - brew) * 0.12f;
        if (!crafting && brew < 0.01f) brew = 0f;

        liquidPhaseO = liquidPhase;
        liquidPhase += Mth.lerp(brew, 0.10f, 0.55f);
        if (liquidPhase > 4096f) {
            liquidPhase -= 4096f;
            liquidPhaseO -= 4096f;
        }

        progressO = progress;
        int max = menu.getMaxProgress();
        if (crafting && max > 0) {
            float target = Mth.clamp(menu.getProgress() / (float) max, 0f, 1f);
            if (target < progress - 0.2f) progressO = target;
            progress = target;
        } else if (brew < 0.03f) {
            progress = 0f;
            progressO = 0f;
        }

        for (int i = 0; i < glow.length; i++) {
            glowO[i] = glow[i];
            glow[i] += ((hasItem(i) ? 1f : 0f) - glow[i]) * 0.25f;
        }

        if (wasCrafting && !crafting) flashWait = 6;
        wasCrafting = crafting;
        if (flashWait > 0) {
            flashWait--;
            if (hasItem(S_OUTPUT)) {
                flashTicks = FLASH_LENGTH;
                flashWait = 0;
            }
        }
        if (flashTicks > 0) flashTicks--;
    }

    private boolean hasItem(int logicalSlot) {
        int idx = SLOT_FIRST + logicalSlot;
        return idx < menu.slots.size() && menu.slots.get(idx).hasItem();
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        int w = this.font.width(this.title);
        g.drawString(this.font, this.title, (this.imageWidth - w) / 2, 6, TITLE_COLOR, true);
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        float anim = tickCount + pt;
        float brewV = Mth.lerp(pt, brewO, brew);

        renderLiquid(g, x, y, pt, anim, brewV);
        draw(g, BACKGROUND, x, y, 0, 0, imageWidth, imageHeight, BG_TEX_SIZE, BG_TEX_SIZE, 1f, 1f, 1f, 1f, false);

        renderRecipeGhost(g, x, y, anim);
        renderLeyLines(g, x, y, pt, anim, brewV);
        renderRecipeThread(g, x, y, pt, anim, brewV);
        renderSlotGlows(g, x, y, pt, anim, brewV);
        renderStream(g, x, y, pt, anim, brewV);
        renderBubbles(g, x, y, anim, brewV);
        renderSteam(g, x, y, anim, brewV);
        renderFlash(g, x, y, pt);

        g.setColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderLiquid(GuiGraphics g, int x, int y, float pt, float anim, float brewV) {
        float phase = Mth.lerp(pt, liquidPhaseO, liquidPhase);
        int frameA = Math.floorMod((int) Math.floor(phase), LIQ_FRAMES);
        int frameB = (frameA + 1) % LIQ_FRAMES;
        float frac = phase - (float) Math.floor(phase);

        float dim = Mth.lerp(brewV, 0.62f, 1.0f);
        float r = dim, gr = dim * 0.96f, b = dim;
        int px = x + LIQ_X, py = y + LIQ_Y;
        int texH = LIQ_H * LIQ_FRAMES;

        draw(g, LIQUID, px, py, 0, frameA * LIQ_H, LIQ_W, LIQ_H, LIQ_W, texH, r, gr, b, 1f, false);
        if (frac > 0.02f) {
            draw(g, LIQUID, px, py, 0, frameB * LIQ_H, LIQ_W, LIQ_H, LIQ_W, texH, r, gr, b, frac, false);
        }

        if (brewV > 0.02f) {
            float a = 0.55f * brewV * (0.85f + 0.15f * Mth.sin(anim * 0.25f));
            draw(g, LIQUID, px, py, 0, frameA * LIQ_H, LIQ_W, LIQ_H, LIQ_W, texH, 0.75f, 0.35f, 1.0f, a, true);
        }
    }

    private void renderRecipeGhost(GuiGraphics g, int x, int y, float anim) {
        if (hasItem(S_RECIPE)) return;
        float a = 0.38f + 0.10f * Mth.sin(anim * 0.10f);
        draw(g, GHOST, x + SLOT_POS[S_RECIPE][0], y + SLOT_POS[S_RECIPE][1], 0, 0, 16, 16, 16, 16,
                1f, 1f, 1f, a, false);
    }

    private void renderLeyLines(GuiGraphics g, int x, int y, float pt, float anim, float brewV) {
        final int lineRgb = 0xB478F0;
        final int pulseRgb = 0xEBD2FF;

        for (int[] l : DIAG_LINES) {
            float gl = Math.max(glowOf(l[5], pt), glowOf(l[6], pt));
            float a = lineAlpha(gl, brewV, anim, l[5]);
            drawDiag(g, x, y, l[0], l[1], l[2], l[3], l[4], argb(a, lineRgb), argb(a * 0.40f, lineRgb));
            if (brewV > 0.05f && gl > 0.5f) {
                drawPulses(g, x, y, l[0], l[1], l[2], l[3], anim, brewV * gl, l[5], pulseRgb, true);
            }
        }
        for (int[] l : H_LINES) {
            float gl = glowOf(l[3], pt);
            float a = lineAlpha(gl, brewV, anim, l[3]);
            int x0 = Math.min(l[0], l[1]), x1 = Math.max(l[0], l[1]);
            g.fill(x + x0, y + l[2], x + x1 + 1, y + l[2] + 1, argb(a, lineRgb));
            g.fill(x + x0, y + l[2] + 1, x + x1 + 1, y + l[2] + 2, argb(a * 0.40f, lineRgb));
            if (brewV > 0.05f && gl > 0.5f) {
                drawPulses(g, x, y, l[0], l[2], l[1], l[2], anim, brewV * gl, l[3] + 2, pulseRgb, false);
            }
        }
    }

    private float lineAlpha(float slotGlow, float brewV, float anim, int seed) {
        float idle = 0.10f + 0.25f * slotGlow;
        float active = (0.60f + 0.25f * Mth.sin(anim * 0.35f + seed * 1.7f)) * (0.45f + 0.55f * slotGlow);
        return Mth.clamp(Mth.lerp(brewV, idle, active), 0f, 1f);
    }

    private void drawDiag(GuiGraphics g, int x, int y, int x0, int y0, int x1, int y1, int side, int core, int edge) {
        int dy = y1 - y0;
        for (int yy = y0; yy <= y1; yy++) {
            int xx = x0 + Math.round((yy - y0) * (float) (x1 - x0) / dy);
            g.fill(x + xx, y + yy, x + xx + 1, y + yy + 1, core);
            g.fill(x + xx + side, y + yy, x + xx + side + 1, y + yy + 1, edge);
        }
    }

    private void drawPulses(GuiGraphics g, int x, int y, int x0, int y0, int x1, int y1,
                            float anim, float strength, int seed, int rgb, boolean fadeAtEnd) {
        for (int k = 0; k < 2; k++) {
            float t = frac(anim * 0.034f + k * 0.5f + seed * 0.19f);
            for (int j = 0; j < 5; j++) {
                float tj = t - j * 0.035f;
                if (tj < 0f) continue;
                float fade = Math.min(1f, tj * 7f) * (fadeAtEnd ? 1f : Math.min(1f, (1f - tj) * 5f));
                float a = strength * fade * (1f - j / 5f);
                if (a <= 0.02f) continue;
                int px = x + Math.round(Mth.lerp(tj, x0, x1));
                int py = y + Math.round(Mth.lerp(tj, y0, y1));
                int size = j == 0 ? 2 : 1;
                g.fill(px, py, px + size, py + size, argb(a, rgb));
            }
        }
    }

    private void renderRecipeThread(GuiGraphics g, int x, int y, float pt, float anim, float brewV) {
        float gl = glowOf(S_RECIPE, pt);
        if (gl < 0.02f) return;
        final int rgb = 0xEBBE6E;
        float calm = 0.30f + 0.10f * Mth.sin(anim * 0.08f);
        float a = gl * Mth.lerp(brewV, calm, 0.75f);

        g.fill(x + THREAD_X_CAULDRON, y + THREAD_Y, x + THREAD_X_SLOT + 1, y + THREAD_Y + 1, argb(a, rgb));
        g.fill(x + THREAD_X_CAULDRON, y + THREAD_Y + 1, x + THREAD_X_SLOT + 1, y + THREAD_Y + 2, argb(a * 0.4f, rgb));
        g.fill(x + 120, y + 129, x + 122, y + 131, argb(a, 0xFFE2A0));

        if (brewV > 0.05f) {
            drawPulses(g, x, y, THREAD_X_SLOT, THREAD_Y, THREAD_X_CAULDRON, THREAD_Y, anim, brewV * gl, 5, 0xFFE9B8, false);
        }
    }

    private void renderSlotGlows(GuiGraphics g, int x, int y, float pt, float anim, float brewV) {
        for (int i = 0; i < glow.length; i++) {
            float gl = glowOf(i, pt);
            if (gl < 0.01f) continue;

            float a, r, gr, b;
            if (isIngredientSlot(i)) {
                a = gl * (0.32f + 0.32f * brewV * (0.5f + 0.5f * Mth.sin(anim * 0.30f + i * 2f)));
                r = 0.80f; gr = 0.65f; b = 1.0f;
            } else if (i == S_OUTPUT) {
                float pulse = 0.5f + 0.5f * Mth.sin(anim * 0.18f);
                a = gl * (0.40f + 0.45f * pulse);
                r = 0.88f; gr = 0.72f; b = 1.0f;
            } else {
                a = gl * (0.28f + 0.22f * brewV);
                r = 1.0f; gr = 0.85f; b = 0.50f;
            }

            int sx = x + SLOT_POS[i][0];
            int sy = y + SLOT_POS[i][1];
            if (i == S_OUTPUT) {
                drawGlowScaled(g, sx + 8, sy + 8, 1.2f, r, gr, b, a);   // output frame is 20x20, so enlarge the halo
            } else {
                draw(g, GLOW, sx - 4, sy - 4, 0, 0, 24, 24, 24, 24, r, gr, b, a, true);
            }
        }
    }

    private void drawGlowScaled(GuiGraphics g, int cx, int cy, float scale, float r, float gr, float b, float a) {
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().scale(scale, scale, 1f);
        draw(g, GLOW, -12, -12, 0, 0, 24, 24, 24, 24, r, gr, b, a, true);
        g.pose().popPose();
    }

    private void renderStream(GuiGraphics g, int x, int y, float pt, float anim, float brewV) {
        float p = Mth.lerp(pt, progressO, progress);
        if (p <= 0.001f || brewV < 0.02f) return;

        float h = Mth.clamp(p, 0f, 1f) * STREAM_H;
        int full = (int) h;
        float frac = h - full;
        int frame = ((int) (anim * 0.5f)) % STREAM_FRAMES;
        float a = Math.min(1f, brewV * 1.6f);
        int u = frame * STREAM_W;
        int texW = STREAM_W * STREAM_FRAMES;

        if (full > 0) {
            draw(g, STREAM, x + STREAM_X, y + STREAM_Y, u, 0, STREAM_W, full, texW, STREAM_H, 1f, 1f, 1f, a, false);
        }
        if (full < STREAM_H && frac > 0.05f) {      // sub-pixel leading edge
            draw(g, STREAM, x + STREAM_X, y + STREAM_Y + full, u, full, STREAM_W, 1, texW, STREAM_H,
                    1f, 1f, 1f, a * frac, false);
        }

        int dropY = Math.max(STREAM_Y - 1, STREAM_Y + (int) h - 3);
        float da = a * (0.8f + 0.2f * Mth.sin(anim * 0.6f));
        draw(g, DROPLET, x + STREAM_X, y + dropY, 0, 0, 6, 6, 6, 6, 1f, 1f, 1f, da, true);
    }

    private static final int BUBBLE_COUNT = 10;

    private void renderBubbles(GuiGraphics g, int x, int y, float anim, float brewV) {
        if (brewV < 0.05f) return;
        for (int i = 0; i < BUBBLE_COUNT; i++) {
            int period = 34 + (hash(i, -1) & 31);
            float shifted = anim + (hash(i, -2) & 63);
            int cycle = (int) (shifted / period);
            float p = (shifted % period) / period;
            int h = hash(i, cycle);

            float u = ((h & 0xFF) / 255f) * 2f - 1f;
            int size = (h >> 8) & 3;
            float bx = LIQ_CX + u * 44f;
            float k = u * 44f / 50f;
            float ey = 9f * Mth.sqrt(Math.max(0f, 1f - k * k));
            float by = Mth.lerp(p, LIQ_CY + ey * 0.8f, LIQ_CY - ey * 0.8f);

            if (bx + 2 > 79 && bx - 2 < 97 && by + 2 > 71 && by - 2 < 89) continue;

            float a = brewV * Math.min(1f, p * 6f);
            int cell = size;
            if (p > 0.9f) {
                cell = 3;
                a *= 1f - (p - 0.9f) / 0.1f;
            }
            if (a <= 0.02f) continue;
            draw(g, PARTICLES, x + (int) bx - 2, y + (int) by - 2, cell * 4, 0, 4, 4, 16, 8, 1f, 1f, 1f, a, false);
        }
    }

    private static final int STEAM_COUNT = 5;

    private void renderSteam(GuiGraphics g, int x, int y, float anim, float brewV) {
        if (brewV < 0.05f) return;
        for (int i = 0; i < STEAM_COUNT; i++) {
            int period = 52 + (hash(i, -3) & 31);
            float shifted = anim + (hash(i, -4) & 127);
            int cycle = (int) (shifted / period);
            float p = (shifted % period) / period;
            int h = hash(i, cycle + 100);

            float u = ((h & 0xFF) / 255f) * 2f - 1f;
            int cell = (h >> 8) & 3;
            float sx = LIQ_CX + u * 24f + Mth.sin(anim * 0.10f + i * 1.9f) * 3f;
            float sy = Mth.lerp(p, 62f, 40f);
            float a = brewV * Mth.sin(p * Mth.PI) * 0.60f;
            if (a <= 0.02f) continue;

            g.pose().pushPose();
            g.pose().translate(x + sx - 4f, y + sy - 4f, 0f);
            g.pose().scale(2f, 2f, 1f);
            draw(g, PARTICLES, 0, 0, cell * 4, 4, 4, 4, 16, 8, 1f, 1f, 1f, a, false);
            g.pose().popPose();
        }
    }

    private void renderFlash(GuiGraphics g, int x, int y, float pt) {
        if (flashTicks <= 0) return;
        float t = 1f - Mth.clamp((flashTicks - pt) / FLASH_LENGTH, 0f, 1f);   // 0 -> 1
        float fade = (1f - t) * (1f - t);
        int cx = x + SLOT_POS[S_OUTPUT][0] + 8;
        int cy = y + SLOT_POS[S_OUTPUT][1] + 8;
        drawGlowScaled(g, cx, cy, 1.2f + t * 2.4f, 0.95f, 0.85f, 1f, 0.95f * fade);
        drawGlowScaled(g, cx, cy, 1.0f + t * 1.1f, 1f, 1f, 1f, 0.85f * fade);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
        renderBrewTooltip(g, mouseX, mouseY);
    }

    private void renderBrewTooltip(GuiGraphics g, int mouseX, int mouseY) {
        if (this.hoveredSlot != null || !menu.isCrafting()) return;
        int max = menu.getMaxProgress();
        if (max <= 0) return;
        if (!isHovering(31, 60, 115, 76, mouseX, mouseY)) return;

        int progressTicks = Mth.clamp(menu.getProgress(), 0, max);
        int pct = progressTicks * 100 / max;
        float secondsLeft = (max - progressTicks) / 20f;

        List<Component> lines = List.of(
                Component.literal("Brewing: " + pct + "%").withStyle(ChatFormatting.LIGHT_PURPLE),
                Component.literal(String.format(Locale.ROOT, "%.1fs remaining", secondsLeft)).withStyle(ChatFormatting.GRAY)
        );
        g.renderComponentTooltip(this.font, lines, mouseX, mouseY);
    }

    private static boolean isIngredientSlot(int logicalSlot) {
        return logicalSlot != S_OUTPUT && logicalSlot != S_RECIPE;
    }

    private float glowOf(int slot, float pt) {
        return Mth.lerp(pt, glowO[slot], glow[slot]);
    }

    private static float frac(float v) {
        return v - (float) Math.floor(v);
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