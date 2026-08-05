package de.jakob.lotm.gui.custom.ChaosSeaAuthority;

import de.jakob.lotm.attachments.EnvisionedCharacteristicsData;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncEnvisionCharacteristicsPacket;
import de.jakob.lotm.network.packets.toServer.RequestTargetEnvisionCharacteristicsPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.PathwayInfos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;

/**
 * Envisioning > Target > Characteristics
 *
 * Chaos Sea Authority owner can envision characteristics from non-neighbouring pathways
 * (at Seq 4–9) onto a target player who is at least 2 sequences weaker.
 */
@OnlyIn(Dist.CLIENT)
public class TargetCharacteristicsScreen extends Screen {

    // ── Colours ───────────────────────────────────────────────────────────────
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
    private static final int C_SEL_BG        = 0xFF22263A;
    private static final int C_SEL_BD        = 0xFF5060A0;
    private static final int C_DROP_BG       = 0xFF14172A;
    private static final int C_DROP_BD       = 0xFF6070B0;
    private static final int C_DROP_HOVER    = 0x33FFFFFF;

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int PAD         = 10;
    private static final int HEADER_H    = 56;  // back btn + title + subtitle + target selector
    private static final int SLOT_H      = 24;
    private static final int SLOT_GAP    = 3;
    private static final int SLOTS_AREA  = EnvisionedCharacteristicsData.TARGET_MAX_SLOTS * (SLOT_H + SLOT_GAP) + 4;
    private static final int DIV_H       = 14;
    private static final int COL_HEAD_H  = 16;
    private static final int SEQ_ROW_H   = 11;
    private static final int SEQ_ROW_GAP = 2;
    private static final int COL_GAP     = 8;
    private static final int PREF_ELIG_H = 140;
    private static final int SEL_H       = 12;
    private static final int DROP_ROW_H  = 11;
    private static final int DROP_MAX    = 6;

    // ── Dynamic layout ────────────────────────────────────────────────────────
    private int panelW, panelH, colW, numCols, eligH;

    // ── State ─────────────────────────────────────────────────────────────────
    private final List<SyncEnvisionCharacteristicsPacket.SlotInfo> localSlots = new ArrayList<>();
    private final List<SyncEnvisionCharacteristicsPacket.CooldownInfo> localCooldowns = new ArrayList<>();
    private final LinkedHashMap<String, List<Integer>> columns = new LinkedHashMap<>();

    private final List<String> players = new ArrayList<>();
    private String selectedPlayer = null;
    private boolean dropOpen = false;
    private int dropScroll = 0;
    private int scrollPx = 0;

    // cached target info from last sync
    private String targetPathway = "";
    private int    targetSequence = -1;

    public TargetCharacteristicsScreen() {
        super(Component.literal("Target Characteristics"));
    }

    @Override
    protected void init() {
        super.init();
        clearTargetCache();
        refreshPlayers();
        layout();
        addRenderableWidget(Button.builder(Component.literal("< Back"), b -> onClose())
                .bounds(lx() + 4, ty() + 4, 40, 12).build());
    }

    private void clearTargetCache() {
        SyncEnvisionCharacteristicsPacket.TARGET_CACHE     = List.of();
        SyncEnvisionCharacteristicsPacket.TARGET_COOLDOWNS = List.of();
        SyncEnvisionCharacteristicsPacket.TARGET_PATHWAY   = "";
        SyncEnvisionCharacteristicsPacket.TARGET_SEQUENCE  = -1;
    }

    private void refreshPlayers() {
        players.clear();
        var mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            mc.getConnection().getOnlinePlayers().stream()
                    .map(p -> p.getProfile().getName())
                    .filter(n -> mc.player == null || !n.equals(mc.player.getName().getString()))
                    .sorted()
                    .forEach(players::add);
        }
    }

    private void layout() {
        numCols = Math.max(1, Math.min(columns.size(), 4));
        int colsContentW = numCols * 100 + (numCols - 1) * COL_GAP;
        panelW = Math.min(Math.max(240, PAD * 2 + colsContentW), Math.min(width - 20, 480));
        colW   = Math.max(70, (panelW - PAD * 2 - (numCols - 1) * COL_GAP) / numCols);
        int fixedH = HEADER_H + SLOTS_AREA + DIV_H;
        eligH  = Math.max(60, Math.min(PREF_ELIG_H, height - 20 - fixedH));
        panelH = fixedH + eligH + PAD;
    }

    private void buildColumns() {
        columns.clear();
        if (targetPathway == null || targetPathway.isEmpty() || targetPathway.equals("none")) return;
        PathwayInfos info = BeyonderData.pathwayInfos.get(targetPathway);
        Set<String> neighbours = new HashSet<>();
        neighbours.add(targetPathway);
        if (info != null) neighbours.addAll(Arrays.asList(info.neighboringPathways()));
        for (String p : BeyonderData.implementedPathways) {
            if (neighbours.contains(p)) continue;
            List<Integer> seqs = new ArrayList<>();
            // Only sequences 4–9 (the weaker half)
            for (int s = 4; s <= 9; s++) seqs.add(s);
            columns.put(p, seqs);
        }
        layout();
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x55000000);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        // Pull latest cache
        var tCache = SyncEnvisionCharacteristicsPacket.TARGET_CACHE;
        var tCd    = SyncEnvisionCharacteristicsPacket.TARGET_COOLDOWNS;
        String newPath = SyncEnvisionCharacteristicsPacket.TARGET_PATHWAY;
        int    newSeq  = SyncEnvisionCharacteristicsPacket.TARGET_SEQUENCE;
        if (!tCache.equals(localSlots)) { localSlots.clear(); localSlots.addAll(tCache); }
        if (!tCd.equals(localCooldowns)) { localCooldowns.clear(); localCooldowns.addAll(tCd); }
        if (!newPath.equals(targetPathway) || newSeq != targetSequence) {
            targetPathway  = newPath;
            targetSequence = newSeq;
            buildColumns();
        }

        renderBackground(g, mx, my, pt);
        int lx = lx(), ty = ty();

        // Panel
        g.fill(lx, ty, lx + panelW, ty + panelH, C_BG);
        drawOutline(g, lx, ty, panelW, panelH, C_OUTLINE);

        // Title
        g.drawCenteredString(font, "Target Characteristics", lx + panelW / 2, ty + 15, C_TITLE);
        g.drawCenteredString(font, "Non-neighbouring path · Seq 4–9 · Target 2+ weaker",
                lx + panelW / 2, ty + 25, C_HINT);
        g.fill(lx + PAD, ty + 36, lx + panelW - PAD, ty + 37, C_DIVIDER);

        // Target selector
        renderSelector(g, lx, ty, mx, my);
        g.fill(lx + PAD, ty + HEADER_H - 2, lx + panelW - PAD, ty + HEADER_H - 1, C_DIVIDER);

        // Envision slots (target's current slots)
        int active = activeCount();
        g.drawCenteredString(font, active + " / " + EnvisionedCharacteristicsData.TARGET_MAX_SLOTS + " slot",
                lx + PAD + 60, ty + HEADER_H + 2, C_HINT);
        int slotX = lx + PAD, slotW = panelW - PAD * 2;
        for (int i = 0; i < EnvisionedCharacteristicsData.TARGET_MAX_SLOTS; i++) {
            int sy = ty + HEADER_H + 8 + i * (SLOT_H + SLOT_GAP);
            var info = i < localSlots.size() ? localSlots.get(i) : SyncEnvisionCharacteristicsPacket.SlotInfo.emptySlot();
            renderSlot(g, slotX, sy, slotW, i, info, mx, my);
        }

        // Divider + eligible label
        int divY = ty + HEADER_H + 8 + SLOTS_AREA;
        g.fill(lx + PAD, divY, lx + panelW - PAD, divY + 1, C_DIVIDER);
        g.drawString(font, selectedPlayer == null
                ? "§7Select a target to see eligible characteristics"
                : "§7Non-neighbouring characteristics (Seq 4–9):",
                lx + PAD, divY + 4, C_HINT, false);

        // Columns
        int colsY = divY + DIV_H;
        int colsX = lx + PAD;
        int colsW = numCols * colW + (numCols - 1) * COL_GAP;
        if (!columns.isEmpty()) {
            g.enableScissor(colsX, colsY, colsX + colsW, colsY + eligH);
            renderColumns(g, colsX, colsY, mx, my, active);
            g.disableScissor();
            int ch = contentH();
            if (ch > eligH) {
                int barX   = colsX + colsW + 3;
                int thumbH = Math.max(12, eligH * eligH / ch);
                int thumbY = colsY + (int) ((long) scrollPx * (eligH - thumbH) / Math.max(1, ch - eligH));
                g.fill(barX, colsY, barX + 3, colsY + eligH, 0xFF1E2238);
                g.fill(barX, thumbY, barX + 3, thumbY + thumbH, 0xFF6677AA);
            }
        }

        // Target info badge
        if (targetPathway != null && !targetPathway.isEmpty() && !targetPathway.equals("none")) {
            String badge = prettify(targetPathway) + " Seq " + targetSequence;
            g.drawString(font, "§7Target: §b" + badge, lx + PAD, ty + 40, 0xFFAABBCC, false);
        }

        super.render(g, mx, my, pt);

        // Dropdown last (floats above everything)
        if (dropOpen) renderDropdown(g, lx, ty, mx, my);
    }

    private void renderSelector(GuiGraphics g, int lx, int ty, int mx, int my) {
        int selX = lx + PAD + 55;
        int selY = ty + 38;
        int selW = panelW - PAD * 2 - 55;
        boolean hov  = mx >= selX && mx < selX + selW && my >= selY && my < selY + SEL_H;
        int     bd   = dropOpen ? C_OUTLINE : (hov ? 0xFF8090C0 : C_SEL_BD);
        g.drawString(font, "§7Target:", lx + PAD, selY + 2, C_HINT, false);
        g.fill(selX, selY, selX + selW, selY + SEL_H, C_SEL_BG);
        drawOutline(g, selX, selY, selW, SEL_H, bd);
        String disp = selectedPlayer != null ? selectedPlayer : "— select player —";
        g.drawString(font, disp, selX + 3, selY + 2, 0xFFEEEEFF, false);
        g.drawString(font, dropOpen ? "▲" : "▼", selX + selW - 10, selY + 2, C_HINT, false);
    }

    private void renderDropdown(GuiGraphics g, int lx, int ty, int mx, int my) {
        int selX = lx + PAD + 55;
        int selY = ty + 38 + SEL_H + 1;
        int selW = panelW - PAD * 2 - 55;
        int vis  = Math.min(DROP_MAX, players.size());
        if (vis == 0) {
            g.fill(selX, selY, selX + selW, selY + DROP_ROW_H + 2, C_DROP_BG);
            drawOutline(g, selX, selY, selW, DROP_ROW_H + 2, C_DROP_BD);
            g.drawString(font, "§8No players online", selX + 4, selY + 2, 0xFF666688, false);
            return;
        }
        int dropH = vis * (DROP_ROW_H + 1);
        g.fill(selX, selY, selX + selW, selY + dropH, C_DROP_BG);
        drawOutline(g, selX, selY, selW, dropH, C_DROP_BD);
        for (int j = 0; j < vis; j++) {
            int idx  = dropScroll + j;
            if (idx >= players.size()) break;
            String name = players.get(idx);
            int ry   = selY + j * (DROP_ROW_H + 1);
            boolean h = mx >= selX && mx < selX + selW && my >= ry && my < ry + DROP_ROW_H;
            if (h) g.fill(selX, ry, selX + selW, ry + DROP_ROW_H, C_DROP_HOVER);
            g.drawString(font, name, selX + 4, ry + 2, 0xFFEEEEFF, false);
        }
    }

    private void renderSlot(GuiGraphics g, int sx, int sy, int sw, int idx,
                             SyncEnvisionCharacteristicsPacket.SlotInfo info, int mx, int my) {
        boolean active = !info.empty();
        boolean hover  = mx >= sx && mx < sx + sw && my >= sy && my < sy + SLOT_H && selectedPlayer != null;
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
                long cdMs = localCooldowns.stream()
                        .filter(c -> c.pathway().equals(path) && c.sequence() == seq)
                        .mapToLong(SyncEnvisionCharacteristicsPacket.CooldownInfo::remainingMs)
                        .findFirst().orElse(0L);
                boolean onCd   = !env && cdMs > 0;
                boolean full   = !env && !onCd && activeSlots >= EnvisionedCharacteristicsData.TARGET_MAX_SLOTS;
                boolean locked = onCd || full || selectedPlayer == null;
                boolean hover  = !locked && mx >= cx && mx < cx + colW && my >= ry && my < ry + SEQ_ROW_H
                        && my >= startY && my < startY + eligH;
                int bg  = env ? C_SEQ_ENV_BG : (onCd ? 0xFF1A1820 : (hover ? C_SEQ_HOVER : C_SEQ_BG));
                int bdr = env ? C_SEQ_ENV_B  : (onCd ? 0xFF554455 : C_SEQ_BORDER);
                int tc  = locked ? C_TEXT_LOCKED : (env ? C_TEXT_ENV : C_TEXT);
                g.fill(cx, ry, cx + colW, ry + SEQ_ROW_H, bg);
                drawOutline(g, cx, ry, colW, SEQ_ROW_H, bdr);
                String label = env  ? "\u00a7a\u2713 Seq " + seq
                             : onCd ? "\u00a78Seq " + seq + " \u00a77(" + formatTime(cdMs / 1000) + ")"
                             : "Seq " + seq;
                g.drawString(font, label, cx + 4, ry + (SEQ_ROW_H - 7) / 2, tc, false);
                ry += SEQ_ROW_H + SEQ_ROW_GAP;
            }
            idx++;
        }
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        // Scroll dropdown
        if (dropOpen) {
            dropScroll = (int) Math.max(0, Math.min(Math.max(0, players.size() - DROP_MAX), dropScroll - sy));
            return true;
        }
        // Scroll columns
        int colsY = ty() + HEADER_H + 8 + SLOTS_AREA + DIV_H;
        if (my >= colsY && my < colsY + eligH) {
            scrollPx = (int) Math.max(0, Math.min(Math.max(0, contentH() - eligH), scrollPx - sy * 14));
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int lx = lx(), ty = ty();
        int selX = lx + PAD + 55;
        int selY = ty + 38;
        int selW = panelW - PAD * 2 - 55;

        // Toggle dropdown
        if (mx >= selX && mx < selX + selW && my >= selY && my < selY + SEL_H) {
            if (!dropOpen) refreshPlayers();
            dropOpen = !dropOpen;
            return true;
        }

        // Pick from dropdown
        if (dropOpen) {
            int dropY = selY + SEL_H + 1;
            int vis   = Math.min(DROP_MAX, players.size());
            for (int j = 0; j < vis; j++) {
                int rowY = dropY + j * (DROP_ROW_H + 1);
                if (mx >= selX && mx < selX + selW && my >= rowY && my < rowY + DROP_ROW_H) {
                    int idx = dropScroll + j;
                    if (idx < players.size()) {
                        selectedPlayer = players.get(idx);
                        dropOpen = false;
                        scrollPx = 0;
                        columns.clear();
                        localSlots.clear();
                        localCooldowns.clear();
                        targetPathway = "";
                        targetSequence = -1;
                        PacketHandler.sendToServer(new RequestTargetEnvisionCharacteristicsPacket(
                                "SYNC", "", -1, -1, selectedPlayer));
                    }
                    return true;
                }
            }
            dropOpen = false;
            return true;
        }

        if (btn != 0) return super.mouseClicked(mx, my, btn);

        // Slot release click
        int slotX2 = lx + PAD, slotW = panelW - PAD * 2;
        for (int i = 0; i < EnvisionedCharacteristicsData.TARGET_MAX_SLOTS; i++) {
            int sy = ty + HEADER_H + 8 + i * (SLOT_H + SLOT_GAP);
            if (mx >= slotX2 && mx < slotX2 + slotW && my >= sy && my < sy + SLOT_H && selectedPlayer != null) {
                var info = i < localSlots.size() ? localSlots.get(i) : SyncEnvisionCharacteristicsPacket.SlotInfo.emptySlot();
                if (!info.empty()) {
                    PacketHandler.sendToServer(new RequestTargetEnvisionCharacteristicsPacket(
                            "RELEASE", "", -1, i, selectedPlayer));
                    return true;
                }
            }
        }

        // Column envision click
        int colsY  = ty + HEADER_H + 8 + SLOTS_AREA + DIV_H;
        int startX = lx + PAD;
        if (my >= colsY && my < colsY + eligH && selectedPlayer != null && activeCount() < EnvisionedCharacteristicsData.TARGET_MAX_SLOTS) {
            int cidx = 0;
            for (var entry : columns.entrySet()) {
                if (cidx >= numCols) break;
                int cx = startX + cidx * (colW + COL_GAP);
                if (mx >= cx && mx < cx + colW) {
                    double ry = colsY - scrollPx + COL_HEAD_H + 2;
                    for (int seq : entry.getValue()) {
                        if (my >= ry && my < ry + SEQ_ROW_H) {
                            boolean onCooldown = localCooldowns.stream()
                                    .anyMatch(c -> c.pathway().equals(entry.getKey()) && c.sequence() == seq && c.remainingMs() > 0);
                            if (!onCooldown) {
                                PacketHandler.sendToServer(new RequestTargetEnvisionCharacteristicsPacket(
                                        "ENVISION", entry.getKey(), seq, -1, selectedPlayer));
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

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onClose() { minecraft.setScreen(new EnvisioningScreen()); }

    @Override
    public boolean isPauseScreen() { return false; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int lx() { return (width  - panelW) / 2; }
    private int ty() { return (height - panelH) / 2; }
    private int activeCount() { return (int) localSlots.stream().filter(s -> !s.empty()).count(); }
    private int contentH() { return COL_HEAD_H + 2 + 6 * (SEQ_ROW_H + SEQ_ROW_GAP) + 4; } // 6 rows (seq 4–9)

    private static int pathwayColor(String p) {
        PathwayInfos info = BeyonderData.pathwayInfos.get(p);
        return info != null ? (0xFF000000 | (info.color() & 0x00FFFFFF)) : 0xFFAABBCC;
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
