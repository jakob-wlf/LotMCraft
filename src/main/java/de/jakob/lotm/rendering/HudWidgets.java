package de.jakob.lotm.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


public final class HudWidgets {

    private HudWidgets() {}

    

    public static void setTint(GuiGraphics g, int argb) {
        float a = ((argb >>> 24) & 0xFF) / 255f;
        float r = ((argb >> 16) & 0xFF) / 255f;
        float gr = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        g.setColor(r, gr, b, a);
    }

    public static void resetTint(GuiGraphics g) {
        g.setColor(1f, 1f, 1f, 1f);
    }

    
    public static void blitFull(GuiGraphics g, ResourceLocation tex, int x, int y, int w, int h) {
        int[] s = HudTheme.size(tex);
        RenderSystem.enableBlend();
        g.blit(tex, x, y, w, h, 0f, 0f, s[0], s[1], s[0], s[1]);
    }

    public static void blitTinted(GuiGraphics g, ResourceLocation tex, int x, int y, int w, int h, int argb) {
        setTint(g, argb);
        blitFull(g, tex, x, y, w, h);
        resetTint(g);
    }

    
    public static void blitRotated(GuiGraphics g, ResourceLocation tex, int x, int y, int w, int h,
                                   float degrees, int argb) {
        g.pose().pushPose();
        g.pose().translate(x + w / 2f, y + h / 2f, 0);
        g.pose().mulPose(Axis.ZP.rotationDegrees(degrees));
        g.pose().translate(-w / 2f, -h / 2f, 0);
        blitTinted(g, tex, 0, 0, w, h, argb);
        g.pose().popPose();
    }

    
    public static void drawIcon(GuiGraphics g, ResourceLocation tex, int x, int y, int size) {
        blitFull(g, tex, x, y, size, size);
    }

    

    
    public static void drawNineSlice(GuiGraphics g, ResourceLocation tex, int x, int y, int w, int h,
                                     int srcBorder, int drawBorder) {
        int[] s = HudTheme.size(tex);
        int tw = s[0], th = s[1];
        int b = Math.max(1, Math.min(drawBorder, Math.min(w, h) / 2));
        int cw = tw - 2 * srcBorder;
        int ch = th - 2 * srcBorder;
        RenderSystem.enableBlend();

        
        part(g, tex, x, y, b, b, 0, 0, srcBorder, srcBorder, tw, th);
        part(g, tex, x + w - b, y, b, b, tw - srcBorder, 0, srcBorder, srcBorder, tw, th);
        part(g, tex, x, y + h - b, b, b, 0, th - srcBorder, srcBorder, srcBorder, tw, th);
        part(g, tex, x + w - b, y + h - b, b, b, tw - srcBorder, th - srcBorder, srcBorder, srcBorder, tw, th);
        
        part(g, tex, x + b, y, w - 2 * b, b, srcBorder, 0, cw, srcBorder, tw, th);
        part(g, tex, x + b, y + h - b, w - 2 * b, b, srcBorder, th - srcBorder, cw, srcBorder, tw, th);
        part(g, tex, x, y + b, b, h - 2 * b, 0, srcBorder, srcBorder, ch, tw, th);
        part(g, tex, x + w - b, y + b, b, h - 2 * b, tw - srcBorder, srcBorder, srcBorder, ch, tw, th);
        
        part(g, tex, x + b, y + b, w - 2 * b, h - 2 * b, srcBorder, srcBorder, cw, ch, tw, th);
    }

    private static void part(GuiGraphics g, ResourceLocation tex, int dx, int dy, int dw, int dh,
                             int u, int v, int uw, int vh, int tw, int th) {
        if (dw <= 0 || dh <= 0 || uw <= 0 || vh <= 0) return;
        g.blit(tex, dx, dy, dw, dh, (float) u, (float) v, uw, vh, tw, th);
    }

    
    public static void drawFrame(GuiGraphics g, int x, int y, int w, int h) {
        int[] s = HudTheme.size(HudTheme.FRAME);
        int srcBorder = Math.min(s[0], s[1]) / 4;       
        int drawBorder = Math.max(3, srcBorder / 2);    
        drawNineSlice(g, HudTheme.FRAME, x, y, w, h, srcBorder, drawBorder);

        int ix = x + drawBorder, iy = y + drawBorder;
        int iw = w - 2 * drawBorder, ih = h - 2 * drawBorder;
        if (iw <= 0 || ih <= 0) return;

        int[] t = HudTheme.size(HudTheme.GLYPH_TILE);
        g.setColor(1f, 1f, 1f, 0.35f);
        RenderSystem.enableBlend();
        
        g.blit(HudTheme.GLYPH_TILE, ix, iy, iw, ih, 0f, 0f, iw, ih, t[0], t[1]);
        resetTint(g);
    }

    

    public static void drawGradientV(GuiGraphics g, int x, int y, int w, int h, int top, int bottom) {
        for (int i = 0; i < h; i++) {
            float r = h <= 1 ? 0f : (float) i / (h - 1);
            g.fill(x, y + i, x + w, y + i + 1, HudTheme.interpolate(top, bottom, r));
        }
    }

    public static void drawGradientH(GuiGraphics g, int x, int y, int w, int h, int left, int right) {
        for (int i = 0; i < w; i++) {
            float r = w <= 1 ? 0f : (float) i / (w - 1);
            g.fill(x + i, y, x + i + 1, y + h, HudTheme.interpolate(left, right, r));
        }
    }

    

    
    public static void drawTube(GuiGraphics g, int x, int y, int w, int h, float fill, int color,
                                float time, boolean flicker) {
        int ix = x + HudTheme.TUBE_INSET_X;
        int iy = y + HudTheme.TUBE_INSET_Y;
        int iw = w - 2 * HudTheme.TUBE_INSET_X;
        int ih = h - 2 * HudTheme.TUBE_INSET_Y;
        fill = HudTheme.clamp01(fill);

        g.fill(ix, iy, ix + iw, iy + ih, 0xCC0B0A0D);
        
        blitFull(g, HudTheme.TUBE_FRAME, x, y, w, h);

        int fw = Math.round(iw * fill);
        float a = 1f;
        if (flicker) {
            a = 0.65f + 0.35f * (float) Math.abs(Math.sin(time * 0.9f) * Math.sin(time * 2.3f + 1f));
        }
        color |= 0xFF000000;

        if (fw > 0) {
            int top = HudTheme.scaleAlpha(HudTheme.lighten(color, 0.30f), a);
            int bottom = HudTheme.scaleAlpha(HudTheme.darken(color, 0.40f), a);
            drawGradientV(g, ix, iy, fw, ih, top, bottom);

            
            g.fill(ix, iy, ix + fw, iy + 1, HudTheme.withAlpha(0xFFFFFFFF, 0.25f * a));
            
            g.fill(ix + fw - 1, iy, ix + fw, iy + ih, HudTheme.withAlpha(HudTheme.lighten(color, 0.75f), a));

            
            if (fw > 4) {
                for (int i = 0; i < 5; i++) {
                    float px = HudTheme.frac(time * 0.004f * (0.6f + 0.12f * i) + i * 0.21f);
                    int mx = ix + (int) (px * (fw - 2));
                    float wob = 0.5f + 0.5f * (float) Math.sin(time * 0.08f + i * 37.7f);
                    int my = iy + 1 + (int) (wob * Math.max(0, ih - 3));
                    float fade = (float) Math.sin(px * Math.PI);
                    g.fill(mx, my, mx + 1, my + 1, HudTheme.withAlpha(0xFFFFFFFF, 0.55f * fade * a));
                }
            }
        }

        
        for (int t = 1; t <= 3; t++) {
            int tx = ix + iw * t / 4;
            g.fill(tx, iy, tx + 1, iy + 2, 0x55FFFFFF);
            g.fill(tx, iy + ih - 2, tx + 1, iy + ih, 0x55FFFFFF);
        }

        blitFull(g, HudTheme.TUBE_GLASS, x, y, w, h);
    }

    

    
    public static void drawSocket(GuiGraphics g, int x, int y, int size, ResourceLocation icon,
                                  int ringColor, float time) {
        blitFull(g, HudTheme.SOCKET, x, y, size, size);
        int iconSize = Math.round(size * 0.62f);
        int off = (size - iconSize) / 2;
        drawIcon(g, icon, x + off, y + off, iconSize);
        blitRotated(g, HudTheme.SOCKET_RING, x, y, size, size, time * 1.5f, ringColor | 0xFF000000);
    }

    

    
    public static void drawDial(GuiGraphics g, int x, int y, int size, float value, float time, float alpha) {
        if (alpha <= 0.01f) return;
        int tint = HudTheme.withAlpha(0xFFFFFFFF, alpha);

        blitTinted(g, HudTheme.DIAL, x, y, size, size, tint);

        float angle = HudTheme.lerp(HudTheme.NEEDLE_ANGLE_EMPTY, HudTheme.NEEDLE_ANGLE_FULL, HudTheme.clamp01(value));
        if (value < 0.3f) {
            float intensity = (0.3f - value) / 0.3f;
            angle += (float) (Math.sin(time * 7f) + 0.5 * Math.sin(time * 13f)) * 3f * intensity;
        }
        blitRotated(g, HudTheme.NEEDLE, x, y, size, size, angle, tint);

        blitTinted(g, HudTheme.DIAL_GLASS, x, y, size, size, tint);
    }

    

    
    public static int drawKeycap(GuiGraphics g, Font font, int cx, int y, Component key) {
        int textW = font.width(key);
        int w = Math.max(16, textW + 10);
        int h = 16;
        int x = cx - w / 2;

        int[] s = HudTheme.size(HudTheme.KEYCAP);
        int srcBorder = Math.max(1, Math.min(s[0], s[1]) / 3);
        drawNineSlice(g, HudTheme.KEYCAP, x, y, w, h, srcBorder, Math.min(4, h / 2));

        g.drawString(font, key, cx - textW / 2, y + (h - font.lineHeight) / 2 - 1, HudTheme.KEYCAP_TEXT, true);
        return h;
    }
}