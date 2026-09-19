package de.jakob.lotm.gui.custom.ChaosSeaAuthority;

import de.jakob.lotm.attachments.EnvisionedCharacteristicsData;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncEnvisionCharacteristicsPacket;
import de.jakob.lotm.network.packets.toServer.RequestEnvisionCharacteristicsPacket;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class SelfCharacteristicsScreen extends Screen {

    private static final int C_BG            = 0xFF1A1E2A;
    private static final int C_OUTLINE       = 0xFFAA8833;
    private static final int C_TITLE         = 0xFFFFCC55;
    private static final int C_HINT          = 0xFF99AACC;
    private static final int C_SLOT_EMPTY    = 0xFF141824;
    private static final int C_SLOT_ACTIVE   = 0xFF1A3028;
    private static final int C_SLOT_BORDER   = 0xFF334466;
    private static final int C_SLOT_ACTIVE_B = 0xFF44AA88;
    private static final int C_SEQ_BG        = 0xFF1E2232;
    private static final int C_SEQ_HOVER     = 0xFF2E3850;
    private static final int C_SEQ_ENV_BG    = 0xFF1A3020;
    private static final int C_SEQ_BORDER    = 0xFF303850;
    private static final int C_SEQ_ENV_B     = 0xFF44AA66;
    private static final int C_TEXT          = 0xFFCCDDFF;
    private static final int C_TEXT_ENV      = 0xFF88FFAA;
    private static final int C_TEXT_LOCKED   = 0xFF445566;
    private static final int C_TIMER         = 0xFF88FFAA;
    private static final int C_DIVIDER       = 0xFF3A4A66;

    private static final int PAD         = 10;
    private static final int HEADER_H    = 40;
    private static final int SLOT_H      = 24;
    private static final int SLOT_GAP    = 3;
    private static final int SLOTS_AREA  = EnvisionedCharacteristicsData.MAX_SLOTS * (SLOT_H + SLOT_GAP) + 4;
    private static final int DIV_H       = 14;
    private static final int FOOTER_PAD  = 6;
    private static final int COL_HEAD_H  = 16;
    private static final int SEQ_ROW_H   = 11;
    private static final int SEQ_ROW_GAP = 2;
    private static final int COL_GAP     = 8;
    private static final int PREF_ELIG_H = 160;

    private int panelW, panelH, colW, numCols, eligH;

    private final List<SyncEnvisionCharacteristicsPacket.SlotInfo> localSlots = new ArrayList<>();
    private final LinkedHashMap<String, List<Integer>> columns = new LinkedHashMap<>();
    private int scrollPx = 0;

    public SelfCharacteristicsScreen() {
        super(Component.literal("Envision Characteristics"));
    }

    @Override
    protected void init() {
        super.init();
        PacketHandler.sendToServer(new RequestEnvisionCharacteristicsPacket("SYNC", "", -1, -1));
        buildColumns();
        scrollPx = 0;
        layout();
        addRenderableWidget(Button.builder(Component.literal("< Back"), b -> onClose())
                .bounds(lx() + 4, ty() + 4, 40, 12).build());
    }

    private void buildColumns() {
        columns.clear();
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;
        UUID self = mc.player.getUUID();
        Set<String> paths = new LinkedHashSet<>();
        String cur = ClientBeyonderCache.getPathway(self);
        if (cur != null && !cur.isEmpty() && !cur.equals("none")) paths.add(cur);
        for (String h : ClientBeyonderCache.getPathwayHistory(self))
            if (h != null && !h.isEmpty() && !h.equals("none")) paths.add(h);
        int minSeq = ClientBeyonderCache.getHighestSequence(self);
        if (minSeq < 0) minSeq = 9; // fallback if not yet loaded
        for (String p : paths) {
            List<Integer> seqs = new ArrayList<>();
            for (int s = minSeq; s <= 9; s++) seqs.add(s);
            columns.put(p, seqs);
        }
    }

    private void layout() {
        numCols = Math.max(1, Math.min(columns.size(), 4));
        int maxW = Math.min(width - 24, 480);
        panelW   = Math.min(PAD * 2 + numCols * 110 + (numCols - 1) * COL_GAP, maxW);
        colW     = Math.max(70, (panelW - PAD * 2 - (numCols - 1) * COL_GAP) / numCols);
        int fixedH = HEADER_H + SLOTS_AREA + DIV_H + FOOTER_PAD;
        eligH  = Math.max(80, Math.min(PREF_ELIG_H, height - 20 - fixedH));
        panelH = fixedH + eligH;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x55000000);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        var cache = SyncEnvisionCharacteristicsPacket.CLIENT_CACHE;
        if (!cache.equals(localSlots)) { localSlots.clear(); localSlots.addAll(cache); }
        if (columns.isEmpty()) buildColumns();
        renderBackground(g, mx, my, pt);
        int lx = lx(), ty = ty();

        g.fill(lx, ty, lx + panelW, ty + panelH, C_BG);
        drawOutline(g, lx, ty, panelW, panelH, C_OUTLINE);

        g.drawCenteredString(font, "Envision Characteristics", lx + panelW / 2, ty + 15, C_TITLE);
        int active = activeCount();
        g.drawCenteredString(font, active + " / " + EnvisionedCharacteristicsData.MAX_SLOTS + " slots active  (30 min each)",
                lx + panelW / 2, ty + 26, C_HINT);
        g.fill(lx + PAD, ty + HEADER_H - 2, lx + panelW - PAD, ty + HEADER_H - 1, C_DIVIDER);

        int slotX = lx + PAD, slotW = panelW - PAD * 2;
        for (int i = 0; i < EnvisionedCharacteristicsData.MAX_SLOTS; i++) {
            int sy = ty + HEADER_H + i * (SLOT_H + SLOT_GAP);
            var info = i < localSlots.size() ? localSlots.get(i) : SyncEnvisionCharacteristicsPacket.SlotInfo.emptySlot();
            renderSlot(g, slotX, sy, slotW, i, info, mx, my);
        }

        int divY = ty + HEADER_H + SLOTS_AREA;
        g.fill(lx + PAD, divY, lx + panelW - PAD, divY + 1, C_DIVIDER);
        g.drawString(font, "§7Eligible characteristics:", lx + PAD, divY + 4, C_HINT, false);

        int colsY = divY + DIV_H;
        int colsX = lx + PAD;
        int colsW = numCols * colW + (numCols - 1) * COL_GAP;
        g.enableScissor(colsX, colsY, colsX + colsW, colsY + eligH);
        renderColumns(g, colsX, colsY, mx, my, active);
        g.disableScissor();

        int ch = contentH();
        if (ch > eligH) {
            int barX   = colsX + colsW + 3;
            int thumbH = Math.max(14, eligH * eligH / ch);
            int thumbY = colsY + (int) ((long) scrollPx * (eligH - thumbH) / Math.max(1, ch - eligH));
            g.fill(barX, colsY, barX + 3, colsY + eligH, 0xFF1E2238);
            g.fill(barX, thumbY, barX + 3, thumbY + thumbH, 0xFF6677AA);
        }
        super.render(g, mx, my, pt);
    }

    private void renderSlot(GuiGraphics g, int sx, int sy, int sw, int idx,
                             SyncEnvisionCharacteristicsPacket.SlotInfo info, int mx, int my) {
        boolean active = !info.empty();
        boolean hover  = mx >= sx && mx < sx + sw && my >= sy && my < sy + SLOT_H;
        g.fill(sx, sy, sx + sw, sy + SLOT_H, active ? (hover ? brighten(C_SLOT_ACTIVE) : C_SLOT_ACTIVE) : C_SLOT_EMPTY);
        drawOutline(g, sx, sy, sw, SLOT_H, active ? C_SLOT_ACTIVE_B : C_SLOT_BORDER);
        g.drawString(font, "§eSlot " + (idx + 1), sx + 4, sy + 2, 0xFFCCDDFF, false);
        if (!active) {
            g.drawString(font, "§8— empty —", sx + 4, sy + 12, 0xFF445566, false);
        } else {
            g.drawString(font, "§b" + prettify(info.pathway()) + " Seq " + info.sequence(), sx + 44, sy + 2, C_TEXT_ENV, false);
            g.drawString(font, "§a" + formatTime(info.remainingMs() / 1000), sx + 44, sy + 12, C_TIMER, false);
            if (hover) g.drawString(font, "§c[release]", sx + sw - 50, sy + 8, 0xFFFF6666, false);
        }
    }

    private void renderColumns(GuiGraphics g, int startX, int startY, int mx, int my, int activeSlots) {
        if (columns.isEmpty()) {
            g.drawCenteredString(font, "§7No pathway history.", startX + numCols * (colW + COL_GAP) / 2, startY + eligH / 2, C_HINT);
            return;
        }
        int idx = 0;
        for (var entry : columns.entrySet()) {
            if (idx >= numCols) break;
            String path = entry.getKey();
            int cx = startX + idx * (colW + COL_GAP);
            int cy = startY - scrollPx;
            int pc = pathwayColor(path);
            g.fill(cx, cy, cx + colW, cy + COL_HEAD_H, (0x55 << 24) | (pc & 0x00FFFFFF));
            drawOutline(g, cx, cy, colW, COL_HEAD_H, pc);
            g.drawCenteredString(font, prettify(path), cx + colW / 2, cy + (COL_HEAD_H - 7) / 2, pc);
            int ry = cy + COL_HEAD_H + 2;
            for (int seq : entry.getValue()) {
                boolean env  = localSlots.stream().anyMatch(s -> !s.empty() && s.pathway().equals(path) && s.sequence() == seq);
                boolean full = !env && activeSlots >= EnvisionedCharacteristicsData.MAX_SLOTS;
                boolean hover = !full && mx >= cx && mx < cx + colW && my >= ry && my < ry + SEQ_ROW_H
                        && my >= startY && my < startY + eligH;
                int bg  = env ? C_SEQ_ENV_BG : (hover ? C_SEQ_HOVER : C_SEQ_BG);
                int bdr = env ? C_SEQ_ENV_B  : C_SEQ_BORDER;
                int tc  = full ? C_TEXT_LOCKED : (env ? C_TEXT_ENV : C_TEXT);
                g.fill(cx, ry, cx + colW, ry + SEQ_ROW_H, bg);
                drawOutline(g, cx, ry, colW, SEQ_ROW_H, bdr);
                g.drawString(font, (env ? "§a\u2713 " : "") + "Seq " + seq, cx + 4, ry + (SEQ_ROW_H - 7) / 2, tc, false);
                ry += SEQ_ROW_H + SEQ_ROW_GAP;
            }
            idx++;
        }
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        int colsY = ty() + HEADER_H + SLOTS_AREA + DIV_H;
        if (my >= colsY && my < colsY + eligH) {
            scrollPx = (int) Math.max(0, Math.min(Math.max(0, contentH() - eligH), scrollPx - sy * 15));
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != 0) return super.mouseClicked(mx, my, btn);
        int lx = lx(), ty = ty();

        int slotX = lx + PAD, slotW = panelW - PAD * 2;
        for (int i = 0; i < EnvisionedCharacteristicsData.MAX_SLOTS; i++) {
            int sy = ty + HEADER_H + i * (SLOT_H + SLOT_GAP);
            if (mx >= slotX && mx < slotX + slotW && my >= sy && my < sy + SLOT_H) {
                var info = i < localSlots.size() ? localSlots.get(i) : SyncEnvisionCharacteristicsPacket.SlotInfo.emptySlot();
                if (!info.empty()) {
                    PacketHandler.sendToServer(new RequestEnvisionCharacteristicsPacket("RELEASE", "", -1, i));
                    return true;
                }
            }
        }

        int colsY  = ty + HEADER_H + SLOTS_AREA + DIV_H;
        int startX = lx + PAD;
        if (my >= colsY && my < colsY + eligH && activeCount() < EnvisionedCharacteristicsData.MAX_SLOTS) {
            int cidx = 0;
            for (var entry : columns.entrySet()) {
                if (cidx >= numCols) break;
                int cx = startX + cidx * (colW + COL_GAP);
                if (mx >= cx && mx < cx + colW) {
                    double ry = colsY - scrollPx + COL_HEAD_H + 2;
                    for (int seq : entry.getValue()) {
                        if (my >= ry && my < ry + SEQ_ROW_H) {
                            boolean onCooldown = SyncEnvisionCharacteristicsPacket.CLIENT_COOLDOWNS.stream()
                                    .anyMatch(c -> c.pathway().equals(entry.getKey()) && c.sequence() == seq && c.remainingMs() > 0);
                            if (!onCooldown) {
                                PacketHandler.sendToServer(new RequestEnvisionCharacteristicsPacket("ENVISION", entry.getKey(), seq, -1));
                            }
                            return true;
                        }
                        ry += SEQ_ROW_H + SEQ_ROW_GAP;
                    }
                }
                cidx++;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public void onClose() { minecraft.setScreen(new EnvisioningScreen()); }

    @Override
    public boolean isPauseScreen() { return false; }

    private int lx() { return (width  - panelW) / 2; }
    private int ty() { return (height - panelH) / 2; }
    private int activeCount() { return (int) localSlots.stream().filter(s -> !s.empty()).count(); }
    private int contentH() {
        int maxRows = columns.values().stream().mapToInt(List::size).max().orElse(0);
        return COL_HEAD_H + 2 + maxRows * (SEQ_ROW_H + SEQ_ROW_GAP) + 4;
    }

    private static int pathwayColor(String p) {
        return switch (p) {
            case "fool"             -> 0xFF44CCFF;
            case "wheel_of_fortune" -> 0xFFFFAA33;
            case "darkness"         -> 0xFF9966FF;
            case "door"             -> 0xFF55DDAA;
            case "demoness"         -> 0xFFFF5599;
            case "red_priest"       -> 0xFFFF4444;
            case "mother"           -> 0xFFFFBBDD;
            case "abyss"            -> 0xFF4477BB;
            case "visionary"        -> 0xFFAA88FF;
            case "sun"              -> 0xFFFFDD44;
            case "tyrant"           -> 0xFFFF8833;
            case "death"            -> 0xFF88AACC;
            case "justiciar"        -> 0xFFDDCCAA;
            case "error"            -> 0xFFFF3333;
            case "twilight_giant"   -> 0xFF44FFCC;
            case "black_emperor"    -> 0xFF9955CC;
            default                 -> 0xFFAABBCC;
        };
    }

    private static String prettify(String s) {
        if (s == null || s.isEmpty()) return "Unknown";
        var sb = new StringBuilder();
        for (String w : s.split("_"))
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(' ');
        return sb.toString().trim();
    }

    private static String formatTime(long secs) {
        long h = secs / 3600, m = (secs % 3600) / 60, s = secs % 60;
        return h > 0 ? h + "h " + m + "m" : m + "m " + s + "s";
    }

    private static int brighten(int c) {
        return (c & 0xFF000000)
                | (Math.min(255, ((c >> 16) & 0xFF) + 20) << 16)
                | (Math.min(255, ((c >>  8) & 0xFF) + 20) <<  8)
                |  Math.min(255,  (c        & 0xFF) + 20);
    }

    private static void drawOutline(GuiGraphics g, int x, int y, int w, int h, int col) {
        g.fill(x,         y,         x + w,     y + 1,     col);
        g.fill(x,         y + h - 1, x + w,     y + h,     col);
        g.fill(x,         y,         x + 1,     y + h,     col);
        g.fill(x + w - 1, y,         x + w,     y + h,     col);
    }
}
