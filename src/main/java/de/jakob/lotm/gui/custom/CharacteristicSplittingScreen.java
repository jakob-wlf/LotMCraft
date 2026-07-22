package de.jakob.lotm.gui.custom;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.CharacteristicSplittingPacket;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.playerMap.Characteristic;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CharacteristicSplittingScreen extends Screen {
    private final int panelW = 200;
    private final int panelH = 180;
    private final int slotH = 25;
    private final int pad = 10;
    private int scrollPx = 0;

    public CharacteristicSplittingScreen() {
        super(Component.translatable("ability.lotmcraft.angel_authority.characteristic_splitting"));
    }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x55000000);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);
        int lx = (int) ((width - panelW) / 2.0f);
        int ty = (int) ((height - panelH) / 2.0f);

        g.fill(lx - 2, ty - 2, lx + panelW + 2, ty + panelH + 2, 0xFFAAAAAA);
        g.fill(lx, ty, lx + panelW, ty + panelH, 0xDD000000);

        g.drawString(font, title, lx + pad, ty + pad, 0xFFFFFFFF, false);

        ArrayList<Characteristic> chars = ClientBeyonderCache.getCharList(Minecraft.getInstance().player.getUUID());
        if (chars == null || chars.isEmpty()) {
            g.drawString(font, Component.literal("No characteristics"), lx + pad, ty + pad + 20, 0xFFAAAAAA, false);
        } else {
            int startY = ty + pad + 20;
            g.enableScissor(lx, startY, lx + panelW, ty + panelH - pad);
            int ry = startY - scrollPx;
            for (Characteristic c : chars) {
                if (c.stack() <= 0) continue;
                renderSlot(g, lx + pad, ry, panelW - pad * 2, c, mx, my);
                ry += slotH + 2;
            }
            g.disableScissor();
        }
        super.render(g, mx, my, pt);

        // Tooltip pass
        if (chars != null) {
            int startY = ty + pad + 20;
            int ry = startY - scrollPx;
            for (Characteristic c : chars) {
                if (c.stack() <= 0) continue;
                if (mx >= lx + pad && mx < lx + panelW - pad && my >= ry && my < ry + slotH && my >= startY && my < ty + panelH - pad) {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(Component.literal(prettify(c.pathway())).withStyle(ChatFormatting.GOLD));
                    tooltip.add(Component.literal("Sequence " + c.sequence()).withStyle(ChatFormatting.AQUA));
                    tooltip.add(Component.literal("Count: " + c.stack()).withStyle(ChatFormatting.GRAY));
                    tooltip.add(Component.empty());
                    tooltip.add(Component.literal("Click to split").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
                    g.renderTooltip(font, tooltip, Optional.empty(), mx, my);
                }
                ry += slotH + 2;
            }
        }
    }

    private void renderSlot(GuiGraphics g, int sx, int sy, int sw, Characteristic c, int mx, int my) {
        boolean hover = mx >= sx && mx < sx + sw && my >= sy && my < sy + slotH;
        g.fill(sx, sy, sx + sw, sy + slotH, hover ? 0x66FFFFFF : 0x33FFFFFF);
        g.renderOutline(sx, sy, sw, slotH, hover ? 0xFFFFFFFF : 0xFFAAAAAA);

        String text = prettify(c.pathway()) + " Seq " + c.sequence() + " (" + c.stack() + ")";
        g.drawString(font, text, sx + 5, sy + (slotH - 8) / 2, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != 0) return super.mouseClicked(mx, my, btn);
        int lx = (int) ((width - panelW) / 2.0f);
        int ty = (int) ((height - panelH) / 2.0f);
        int startY = ty + pad + 20;

        ArrayList<Characteristic> chars = ClientBeyonderCache.getCharList(Minecraft.getInstance().player.getUUID());
        if (chars != null) {
            int ry = startY - scrollPx;
            for (Characteristic c : chars) {
                if (c.stack() <= 0) continue;
                if (mx >= lx + pad && mx < lx + panelW - pad && my >= ry && my < ry + slotH) {
                    PacketHandler.sendToServer(new CharacteristicSplittingPacket(c.pathway(), c.sequence()));
                    return true;
                }
                ry += slotH + 2;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scrollPx = Math.max(0, scrollPx - (int) (sy * 15));
        return true;
    }

    private String prettify(String s) {
        if (s == null || s.isEmpty()) return "Unknown";
        StringBuilder sb = new StringBuilder();
        for (String w : s.split("_"))
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(' ');
        return sb.toString().trim();
    }

    private void drawOutline(GuiGraphics g, int x, int y, int w, int h, int col) {
        g.renderOutline(x, y, w, h, col);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
