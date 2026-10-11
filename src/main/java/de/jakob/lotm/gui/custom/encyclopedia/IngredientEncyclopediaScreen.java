package de.jakob.lotm.gui.custom.encyclopedia;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.item.PotionIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.*;

public class IngredientEncyclopediaScreen extends Screen implements MenuAccess<IngredientEncyclopediaMenu> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String HINT_PREFIX = "ingredient.lotm.hint.";
    private static final String UI = "ingredient.lotm.encyclopedia.";
    private static final String MOD_ID = LOTMCraft.MOD_ID;

    private static final boolean EASIEST_FIRST = true;

    private static final ResourceLocation COVER_TEX = tex("cover");
    private static final ResourceLocation PAGE_LEFT_TEX = tex("page_left");
    private static final ResourceLocation PAGE_RIGHT_TEX = tex("page_right");
    private static final ResourceLocation RIBBON_TEX = tex("ribbon");
    private static final ResourceLocation DIVIDER_TEX = tex("divider");
    private static final ResourceLocation EMBLEM_TEX = tex("emblem");
    private static final ResourceLocation FRAME_TEX = tex("frame");
    private static final ResourceLocation SLOT_TEX = tex("slot");
    private static final ResourceLocation TAB_GLOSS_TEX = tex("tab_gloss");

    private static final int BOOK_W = 344, BOOK_H = 212, COVER = 8, GUTTER = 4;
    private static final int PAGE_W = (BOOK_W - 2 * COVER - GUTTER) / 2;
    private static final int PAGE_H = BOOK_H - 2 * COVER;
    private static final int MARGIN = 12, TOP = 20, BOTTOM = 18;
    private static final int CONTENT_W = PAGE_W - 2 * MARGIN;
    private static final int CONTENT_H = PAGE_H - TOP - BOTTOM;
    private static final int HEADER_H = 40;
    private static final float HINT_SCALE = 0.75f;
    private static final int HINT_LH = 7;
    private static final float META_SCALE = 0.7f;
    private static final int META_LH = 8;
    private static final int ICON_COL = 24;
    private static final int DIVIDER_W = 120, DIVIDER_H = 9;
    private static final int EMBLEM_SIZE = 36;
    private static final int RIBBON_W = 9, RIBBON_H = 51;

    private static final int INK = 0xFF3B2A14, INK_SOFT = 0xFF74603E, INK_TEXT = 0xFF4A3A22;
    private static final int LEATHER_DARK = 0xFF2A180B;
    private static final int GOLD_DARK = 0xFF8C6D12;

    private static final int[] SEQ_COLORS = {
            0xFF1B1B26, 0xFF2D3E8E, 0xFF5E2D8E, 0xFF8E2D5A, 0xFFB03A2E,
            0xFFC4622D, 0xFFD08A2E, 0xFFB09A2E, 0xFF8FA13A, 0xFF6E8B3D };

    private interface Block { int height(); }

    private record Header(int sequence, int count) implements Block {
        public int height() { return HEADER_H; }
    }

    private record Entry(PotionIngredient ingredient, ItemStack stack, List<FormattedCharSequence> name,
                         String sequenceLabel, List<FormattedCharSequence> hint, boolean hintMissing,
                         int sequence, int headHeight, int height) implements Block {}

    private enum Kind { TITLE, TOC, CONTENT, FILLER }

    private static final class Page {
        final Kind kind;
        final int sequence;
        final List<Block> blocks = new ArrayList<>();
        int used;
        Page(Kind kind, int sequence) { this.kind = kind; this.sequence = sequence; }
        void add(Block b) { blocks.add(b); used += b.height(); }
    }

    private record Hover(int x, int y, int w, int h, ItemStack stack) {}
    private record Click(int x, int y, int w, int h, Runnable action) {}

    private final List<Page> pages = new ArrayList<>();
    private final List<Integer> chapterOrder = new ArrayList<>();
    private final Map<Integer, Integer> chapterStart = new HashMap<>();
    private final Map<Integer, Integer> chapterCount = new HashMap<>();
    private final Map<Item, Integer> itemPage = new HashMap<>();
    private final List<Hover> hovers = new ArrayList<>();
    private final List<Click> clicks = new ArrayList<>();
    private final List<String> missingKeys = new ArrayList<>();

    private final IngredientEncyclopediaMenu menu;
    @Nullable private final Item focus;
    private int spread = 0;
    private int totalIngredients = 0;

    public IngredientEncyclopediaScreen(IngredientEncyclopediaMenu menu, Inventory inventory, Component title) {
        super(title);
        this.menu = menu;
        ResourceLocation id = menu.getFocus();
        this.focus = id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
    }

    private static ResourceLocation tex(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/encyclopedia/" + name + ".png");
    }

    @Override
    public IngredientEncyclopediaMenu getMenu() {
        return menu;
    }

    @Override
    public void onClose() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.closeContainer();
        }
        super.onClose();
    }

    @Override
    protected void init() {
        super.init();
        if (pages.isEmpty()) {
            buildPages();
            if (focus != null && itemPage.containsKey(focus)) spread = itemPage.get(focus) / 2;
        }
    }

    private void buildPages() {
        List<PotionIngredient> all = new ArrayList<>(ModIngredients.getAll());
        totalIngredients = all.size();

        Comparator<PotionIngredient> cmp = Comparator.comparingInt(PotionIngredient::getSequence);
        if (EASIEST_FIRST) cmp = cmp.reversed();
        cmp = cmp.thenComparing(i -> i.getDescription().getString(), String.CASE_INSENSITIVE_ORDER);
        all.sort(cmp);

        Map<Integer, List<PotionIngredient>> bySequence = new LinkedHashMap<>();
        for (PotionIngredient i : all) bySequence.computeIfAbsent(i.getSequence(), k -> new ArrayList<>()).add(i);

        pages.add(new Page(Kind.TITLE, -1));
        pages.add(new Page(Kind.TOC, -1));

        for (Map.Entry<Integer, List<PotionIngredient>> chapter : bySequence.entrySet()) {
            int seq = chapter.getKey();
            if (pages.size() % 2 == 1) pages.add(new Page(Kind.FILLER, -1));

            Page cur = new Page(Kind.CONTENT, seq);
            pages.add(cur);
            chapterOrder.add(seq);
            chapterStart.put(seq, pages.size() - 1);
            chapterCount.put(seq, chapter.getValue().size());
            cur.add(new Header(seq, chapter.getValue().size()));

            for (PotionIngredient ing : chapter.getValue()) {
                Entry e = makeEntry(ing);
                if (cur.used + e.height() > CONTENT_H && !cur.blocks.isEmpty()) {
                    cur = new Page(Kind.CONTENT, seq);
                    pages.add(cur);
                }
                cur.add(e);
                itemPage.put(ing, pages.size() - 1);
            }
        }

        if (!missingKeys.isEmpty()) {
            LOGGER.warn("Ingredient encyclopedia: {} ingredient(s) have no hint translation. Add these keys to your lang file:\n{}",
                    missingKeys.size(), String.join("\n", missingKeys));
        }
    }

    private Entry makeEntry(PotionIngredient ing) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(ing);
        String key = HINT_PREFIX + id.getPath();
        boolean missing = !I18n.exists(key);
        Component hintComponent;
        if (missing) {
            missingKeys.add(key);
            hintComponent = Component.translatable(HINT_PREFIX + "unknown").withStyle(ChatFormatting.ITALIC);
        } else {
            hintComponent = Component.translatable(key);
        }

        int textW = CONTENT_W - ICON_COL;
        MutableComponent nameComponent = ing.getDescription().copy().withStyle(ChatFormatting.BOLD);
        List<FormattedCharSequence> name = font.split(nameComponent, textW);

        String sequenceLabel = Component.translatable(UI + "seq_short", ing.getSequence()).getString();

        int headH = Math.max(18, name.size() * 9 + 2 + META_LH);
        List<FormattedCharSequence> hint = font.split(hintComponent, (int) (CONTENT_W / HINT_SCALE));
        int height = headH + 4 + hint.size() * HINT_LH + 9;

        return new Entry(ing, new ItemStack(ing), name, sequenceLabel, hint, missing, ing.getSequence(), headH, height);
    }

    private static int seqColor(int seq) { return SEQ_COLORS[Mth.clamp(seq, 0, 9)]; }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        super.render(g, mx, my, partialTick);
        hovers.clear();
        clicks.clear();

        int bx = (width - BOOK_W) / 2, by = (height - BOOK_H) / 2;
        int lx = bx + COVER, rx = lx + PAGE_W + GUTTER, py = by + COVER;

        drawBook(g, bx, by);
        drawPage(g, pageAt(spread * 2), spread * 2, lx, py, true, mx, my);
        drawPage(g, pageAt(spread * 2 + 1), spread * 2 + 1, rx, py, false, mx, my);
        drawRibbon(g, rx, by);
        drawTabs(g, bx, by, mx, my);
        drawArrows(g, lx, rx, py, mx, my);

        for (Hover h : hovers) {
            if (inside(h.x, h.y, h.w, h.h, mx, my)) { g.renderTooltip(font, h.stack, mx, my); break; }
        }
    }

    @Nullable
    private Page pageAt(int i) { return i >= 0 && i < pages.size() ? pages.get(i) : null; }

    private void drawBook(GuiGraphics g, int bx, int by) {
        g.fill(bx + 3, by + 4, bx + BOOK_W + 3, by + BOOK_H + 4, 0x66000000);
        blit(g, COVER_TEX, bx, by, BOOK_W, BOOK_H);
    }

    private void drawRibbon(GuiGraphics g, int rx, int by) {
        blit(g, RIBBON_TEX, rx + PAGE_W - 28, by - 3, RIBBON_W, RIBBON_H);
    }

    private void drawPage(GuiGraphics g, @Nullable Page page, int index, int x, int y, boolean left, int mx, int my) {
        blit(g, left ? PAGE_LEFT_TEX : PAGE_RIGHT_TEX, x, y, PAGE_W, PAGE_H);
        if (page == null) return;

        int cx = x + MARGIN, cy = y + TOP;
        switch (page.kind) {
            case TITLE -> drawTitlePage(g, x, y);
            case TOC -> { drawRunning(g, page, x, y, left); drawToc(g, cx, cy, mx, my); drawFooter(g, index, x, y); }
            case FILLER -> drawDivider(g, x + PAGE_W / 2, y + PAGE_H / 2);
            case CONTENT -> {
                drawRunning(g, page, x, y, left);
                int yy = cy;
                for (Block b : page.blocks) {
                    if (b instanceof Header h) drawHeader(g, h, cx, yy);
                    else if (b instanceof Entry e) drawEntry(g, e, cx, yy);
                    yy += b.height();
                }
                drawFooter(g, index, x, y);
            }
        }
    }

    private void drawRunning(GuiGraphics g, Page page, int x, int y, boolean left) {
        Component text = left || page.kind != Kind.CONTENT
                ? Component.translatable(UI + "title")
                : Component.translatable(UI + "chapter", page.sequence);
        drawCenteredScaled(g, text.getVisualOrderText(), x + PAGE_W / 2f, y + 7, 0.7f, INK_SOFT);
        g.hLine(x + MARGIN, x + PAGE_W - MARGIN - 1, y + 16, 0x55805030);
    }

    private void drawFooter(GuiGraphics g, int index, int x, int y) {
        String n = "\u2014 " + (index + 1) + " \u2014";
        drawCenteredScaled(g, lit(n), x + PAGE_W / 2f, y + PAGE_H - 12, 0.8f, INK_SOFT);
    }

    private void drawTitlePage(GuiGraphics g, int x, int y) {
        int cx = x + PAGE_W / 2;
        blit(g, FRAME_TEX, x, y, PAGE_W, PAGE_H);

        float titleScale = 1.6f;
        List<FormattedCharSequence> title = font.split(
                Component.translatable(UI + "title").withStyle(ChatFormatting.BOLD), (int) ((PAGE_W - 44) / titleScale));
        int ty = y + 36;
        for (FormattedCharSequence l : title) { drawCenteredScaled(g, l, cx, ty, titleScale, INK); ty += 15; }

        int dy = ty + 6;
        drawDivider(g, cx, dy);

        List<FormattedCharSequence> sub = font.split(
                Component.translatable(UI + "subtitle").withStyle(ChatFormatting.ITALIC), (int) ((PAGE_W - 40) / 0.85f));
        int sy = dy + 10;
        for (FormattedCharSequence l : sub) { drawCenteredScaled(g, l, cx, sy, 0.85f, INK_SOFT); sy += 8; }

        int ey = y + PAGE_H - 56;
        blit(g, EMBLEM_TEX, cx - EMBLEM_SIZE / 2, ey - EMBLEM_SIZE / 2, EMBLEM_SIZE, EMBLEM_SIZE);

        drawCenteredScaled(g, Component.translatable(UI + "entries", totalIngredients).getVisualOrderText(),
                cx, y + PAGE_H - 26, 0.8f, INK_SOFT);
    }

    private void drawToc(GuiGraphics g, int x, int y, int mx, int my) {
        drawCenteredScaled(g, Component.translatable(UI + "contents").withStyle(ChatFormatting.BOLD).getVisualOrderText(),
                x + CONTENT_W / 2f, y - 2, 1.3f, INK);
        drawDivider(g, x + CONTENT_W / 2, y + 14);

        int ry = y + 26;
        for (int seq : chapterOrder) {
            int start = chapterStart.get(seq);
            boolean hov = inside(x - 2, ry - 3, CONTENT_W + 4, 14, mx, my);
            if (hov) g.fill(x - 2, ry - 3, x + CONTENT_W + 2, ry + 11, 0x22A06020);

            String label = Component.translatable(UI + "chapter", seq).getString();
            int labelEnd = x + 2 + font.width(label);
            g.drawString(font, label, x + 2, ry, hov ? GOLD_DARK : INK, false);
            String count = "(" + chapterCount.get(seq) + ")";
            drawScaled(g, lit(count), labelEnd + 4, ry + 1.5f, 0.7f, INK_SOFT);
            labelEnd += 4 + (int) (font.width(count) * 0.7f);

            String num = String.valueOf(start + 1);
            int numX = x + CONTENT_W - font.width(num);
            g.drawString(font, num, numX, ry, INK, false);
            for (int dx = labelEnd + 5; dx < numX - 4; dx += 3) g.fill(dx, ry + 8, dx + 1, ry + 9, 0x66805030);

            clicks.add(new Click(x - 2, ry - 3, CONTENT_W + 4, 14, () -> goToPage(start)));
            ry += 15;
        }
    }

    private void drawHeader(GuiGraphics g, Header h, int x, int y) {
        int col = seqColor(h.sequence);
        String num = String.valueOf(h.sequence);
        drawScaled(g, lit(num), x + 1, y - 1, 3f, 0x44000000);
        drawScaled(g, lit(num), x, y - 2, 3f, col);
        drawScaled(g, Component.translatable(UI + "chapter", h.sequence).withStyle(ChatFormatting.BOLD).getVisualOrderText(),
                x + 26, y + 3, 1.2f, INK);
        drawScaled(g, Component.translatable(UI + "chapter_count", h.count).getVisualOrderText(),
                x + 26, y + 17, 0.75f, INK_SOFT);
        drawDivider(g, x + CONTENT_W / 2, y + 32);
    }

    private void drawEntry(GuiGraphics g, Entry e, int x, int y) {
        blit(g, SLOT_TEX, x, y, 18, 18);
        g.fill(x + 1, y + 15, x + 17, y + 17, seqColor(e.sequence));
        g.renderItem(e.stack, x + 1, y + 1);
        hovers.add(new Hover(x, y, 18, 18, e.stack));

        int tx = x + ICON_COL;
        for (int i = 0; i < e.name.size(); i++) g.drawString(font, e.name.get(i), tx, y + i * 9, INK, false);

        int color = seqColor(e.sequence);
        int cy = y + e.name.size() * 9 + 1;
        g.fill(tx, cy + 1, tx + 4, cy + 5, 0xFF1E1208);
        g.fill(tx + 1, cy + 2, tx + 3, cy + 4, color);
        drawScaled(g, lit(e.sequenceLabel), tx + 6, cy + 0.5f, META_SCALE, color);

        int hy = y + e.headHeight + 4;
        for (int i = 0; i < e.hint.size(); i++)
            drawScaled(g, e.hint.get(i), x, hy + i * HINT_LH, HINT_SCALE, e.hintMissing ? INK_SOFT : INK_TEXT);

        g.hLine(x + 12, x + CONTENT_W - 13, y + e.height - 5, 0x33805030);
    }

    private void drawTabs(GuiGraphics g, int bx, int by, int mx, int my) {
        int active = activeChapter();
        int ty = by + 14;
        for (int seq : chapterOrder) {
            boolean act = seq == active;
            int w = act ? 20 : 15;
            int tx = bx + BOOK_W - 1;
            boolean hov = inside(tx, ty, w + 4, 16, mx, my);
            if (hov && !act) w = 18;
            g.fill(tx, ty, tx + w, ty + 16, 0xFF1E1208);
            g.fill(tx, ty + 1, tx + w - 1, ty + 15, seqColor(seq));
            blitRegion(g, TAB_GLOSS_TEX, tx, ty, w - 1, 16, 20, 16);
            String s = String.valueOf(seq);
            g.drawString(font, s, tx + (w - font.width(s)) / 2, ty + 4, 0xFFFFFFFF, true);
            final int start = chapterStart.get(seq);
            clicks.add(new Click(tx, ty, w + 4, 16, () -> goToPage(start)));
            ty += 19;
        }
    }

    private void drawArrows(GuiGraphics g, int lx, int rx, int py, int mx, int my) {
        int ay = py + PAGE_H - 16;
        if (spread > 0) {
            int ax = lx + 10;
            boolean hov = inside(ax - 3, ay - 3, 16, 15, mx, my);
            drawArrow(g, ax, ay, false, hov);
            clicks.add(new Click(ax - 3, ay - 3, 16, 15, () -> flip(-1)));
        }
        if (spread < maxSpread()) {
            int ax = rx + PAGE_W - 20;
            boolean hov = inside(ax - 3, ay - 3, 16, 15, mx, my);
            drawArrow(g, ax, ay, true, hov);
            clicks.add(new Click(ax - 3, ay - 3, 16, 15, () -> flip(1)));
        }
    }

    private static void drawArrow(GuiGraphics g, int x, int y, boolean right, boolean hover) {
        int c = hover ? 0xFFB5841A : INK_SOFT;
        for (int i = 0; i < 5; i++) {
            int px = right ? x + 2 * i : x + 8 - 2 * i;
            g.fill(px, y + i, px + 2, y + 9 - i, c);
        }
    }

    private int activeChapter() {
        Page l = pageAt(spread * 2), r = pageAt(spread * 2 + 1);
        if (l != null && l.kind == Kind.CONTENT) return l.sequence;
        if (r != null && r.kind == Kind.CONTENT) return r.sequence;
        return -1;
    }

    private static FormattedCharSequence lit(String s) { return Component.literal(s).getVisualOrderText(); }

    private static void blit(GuiGraphics g, ResourceLocation tex, int x, int y, int w, int h) {
        blitRegion(g, tex, x, y, w, h, w, h);
    }

    private static void blitRegion(GuiGraphics g, ResourceLocation tex, int x, int y, int w, int h, int texW, int texH) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.blit(tex, x, y, 0f, 0f, w, h, texW, texH);
    }

    private static void drawDivider(GuiGraphics g, int centerX, int centerY) {
        blit(g, DIVIDER_TEX, centerX - DIVIDER_W / 2, centerY - DIVIDER_H / 2, DIVIDER_W, DIVIDER_H);
    }

    private void drawScaled(GuiGraphics g, FormattedCharSequence t, float x, float y, float s, int color) {
        PoseStack ps = g.pose();
        ps.pushPose();
        ps.translate(x, y, 0f);
        ps.scale(s, s, 1f);
        g.drawString(font, t, 0, 0, color, false);
        ps.popPose();
    }

    private void drawCenteredScaled(GuiGraphics g, FormattedCharSequence t, float cx, float y, float s, int color) {
        drawScaled(g, t, cx - font.width(t) * s / 2f, y, s, color);
    }

    private static boolean inside(int x, int y, int w, int h, double mx, double my) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private int maxSpread() { return Math.max(0, (pages.size() - 1) / 2); }

    private void goToPage(int pageIndex) { setSpread(pageIndex / 2); }

    private void flip(int delta) { setSpread(spread + delta); }

    private void setSpread(int s) {
        s = Mth.clamp(s, 0, maxSpread());
        if (s == spread) return;
        spread = s;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0f));
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            for (Click c : clicks) {
                if (inside(c.x, c.y, c.w, c.h, mx, my)) { c.action.run(); return true; }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (scrollY < 0) flip(1);
        else if (scrollY > 0) flip(-1);
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        switch (key) {
            case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_PAGE_DOWN -> { flip(1); return true; }
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_PAGE_UP -> { flip(-1); return true; }
            case GLFW.GLFW_KEY_HOME -> { setSpread(0); return true; }
            case GLFW.GLFW_KEY_END -> { setSpread(maxSpread()); return true; }
            default -> { return super.keyPressed(key, scancode, mods); }
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}