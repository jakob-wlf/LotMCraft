package de.jakob.lotm.gui.custom.introspect;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AllyComponent;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.ControllingUtils;
import de.jakob.lotm.beyonders.acting.ActingHelper;
import de.jakob.lotm.beyonders.acting.ActingTask;
import de.jakob.lotm.beyonders.acting.ActingTaskRegistry;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.network.packets.toServer.*;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.data.*;
import de.jakob.lotm.util.helper.AbilityId;
import de.jakob.lotm.util.helper.ClientTeamData;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IntrospectScreen extends AbstractContainerScreen<IntrospectMenu> {

    // =====================================================================================
    //  Sprites
    // =====================================================================================
    private static ResourceLocation sprite(String name) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "introspect/" + name);
    }

    private static final ResourceLocation SPR_FRAME = sprite("frame");
    private static final ResourceLocation SPR_DRAWER = sprite("drawer");
    private static final ResourceLocation SPR_NAMEPLATE = sprite("nameplate");
    private static final ResourceLocation SPR_COUNTER = sprite("counter_plate");
    private static final ResourceLocation SPR_DIVIDER = sprite("divider");
    private static final ResourceLocation SPR_ICON_FRAME = sprite("icon_frame");
    private static final ResourceLocation SPR_ICON_SHADOW = sprite("icon_frame_shadow");
    private static final ResourceLocation SPR_ICON_GLOW = sprite("icon_frame_glow_mask");
    private static final ResourceLocation SPR_COG_LARGE = sprite("cog_large");
    private static final ResourceLocation SPR_COG_SMALL = sprite("cog_small");

    private static final ResourceLocation SPR_TUBE_FRAME = sprite("tube_frame");
    private static final ResourceLocation SPR_TUBE_GLASS = sprite("tube_glass");
    private static final ResourceLocation SPR_DIAL = sprite("dial");
    private static final ResourceLocation SPR_NEEDLE = sprite("needle");
    private static final ResourceLocation SPR_DIAL_GLASS = sprite("dial_glass");

    private static final ResourceLocation SPR_TAB_IDLE = sprite("tab_idle");
    private static final ResourceLocation SPR_TAB_HOVER = sprite("tab_hover");
    private static final ResourceLocation SPR_TAB_ACTIVE = sprite("tab_active");
    private static final ResourceLocation SPR_TAB_LOCKED = sprite("tab_locked");
    private static final ResourceLocation SPR_TAB_GLOW = sprite("tab_glow_mask");

    private static final ResourceLocation SPR_MINI_IDLE = sprite("mini_btn_idle");
    private static final ResourceLocation SPR_MINI_HOVER = sprite("mini_btn_hover");
    private static final ResourceLocation SPR_MINI_PRESSED = sprite("mini_btn_pressed");
    private static final ResourceLocation SPR_MINI_DISABLED = sprite("mini_btn_disabled");

    private static final ResourceLocation SPR_SLOT_IDLE = sprite("slot_idle");
    private static final ResourceLocation SPR_SLOT_HOVER = sprite("slot_hover");
    private static final ResourceLocation SPR_SLOT_DROP = sprite("slot_drop");
    private static final ResourceLocation SPR_SLOT_COPIED = sprite("slot_copied");
    private static final ResourceLocation SPR_SLOT_EMPTY = sprite("slot_empty");
    private static final ResourceLocation SPR_SLOT_RING = sprite("slot_ring_mask");

    private static final ResourceLocation SPR_LEVER_ON = sprite("lever_on");
    private static final ResourceLocation SPR_LEVER_OFF = sprite("lever_off");

    private static final ResourceLocation ICON_ABILITIES = sprite("icon_abilities");
    private static final ResourceLocation ICON_QUESTS = sprite("icon_quests");
    private static final ResourceLocation ICON_ACTING = sprite("icon_acting");
    private static final ResourceLocation ICON_MISSED = sprite("icon_missed");
    private static final ResourceLocation ICON_HONORIFIC = sprite("icon_honorific");
    private static final ResourceLocation ICON_ALLIES = sprite("icon_allies");
    private static final ResourceLocation ICON_APOTHEOSIS = sprite("icon_apotheosis");

    // =====================================================================================
    //  Palette (text)
    // =====================================================================================
    private static final int TEXT_HEADING = 0xFFF4D58A;   // pale brass
    private static final int TEXT_NORMAL = 0xFFE8CF94;
    private static final int TEXT_BODY = 0xFFD8D0BC;
    private static final int TEXT_DIM = 0xFF8C8672;
    private static final int TEXT_GOLD = 0xFFFFD470;
    private static final int FALLBACK_ACCENT = 0xD9A94F;

    private static final boolean SHOW_COGS = true;

    // =====================================================================================
    //  Main panel layout (relative to leftPos / topPos)
    // =====================================================================================
    private static final int PAD_X = 14;
    private static final int CONTENT_W = 164;

    private static final int ICON_FRAME_X = 114;
    private static final int ICON_FRAME_Y = 13;
    private static final int ICON_FRAME_SIZE = 64;

    private static final int SEQ_LABEL_Y = 15;
    private static final int SEQ_COUNTER_Y = 26;
    private static final int NAMEPLATE_Y = 47;
    private static final int NAMEPLATE_W = 94;
    private static final int NAMEPLATE_H = 30;

    private static final int DIV1_Y = 81;
    private static final int DIGEST_LABEL_Y = 87;
    private static final int TUBE_Y = 97;
    private static final int DIV2_Y = 119;
    private static final int DIAL_Y = 124;
    private static final int DIV3_Y = 161;
    private static final int PASSIVE_LABEL_Y = 167;
    private static final int PASSIVE_SLOT_Y = 178;
    private static final int BOTTOM_Y = 200;

    private static final int TUBE_W = 128;
    private static final int TUBE_H = 16;
    private static final int TUBE_LIQUID_X = 12;
    private static final int TUBE_LIQUID_Y = 4;
    private static final int TUBE_LIQUID_W = 104;
    private static final int TUBE_LIQUID_H = 8;

    // Side tabs
    private static final int TAB_W = 78;
    private static final int TAB_H = 20;

    // =====================================================================================
    //  State
    // =====================================================================================
    private final Inventory playerInventory;

    private boolean showAbilities = false;
    private boolean showAllAbilities = false;
    private boolean showSubAbilities = false;

    private boolean showQuests = false;

    private boolean showActing = false;

    private boolean showMissedActing = false;
    private boolean showAllies = false;

    private enum SidePanel {ABILITIES, QUESTS, ACTING, MISSED, ALLIES}

    private enum Tab {
        ABILITY_WHEEL,
        ABILITY_BAR,
        SHARED_ABILITIES,
        RECORDED_ABILITIES
    }

    private Tab currentTab = Tab.ABILITY_WHEEL;

    private EditBox allyNameInput;

    // Ally widgets are (re)built only when the data changes – not every frame.
    private final List<net.minecraft.client.gui.components.AbstractWidget> allyWidgets = new ArrayList<>();
    private String allyWidgetSignature = null;

    private PassiveAbility hoveredPassive = null;
    private float displaySanity = -1f;

    private final List<PassiveAbility> passiveAbilities = new ArrayList<>();
    private final List<Ability> availableAbilities = new ArrayList<>();
    private final List<SubAbilityEntry> subAbilityEntries = new ArrayList<>();
    private final List<Ability> abilityWheelSlots = new ArrayList<>();
    private final List<Integer> abilityWheelSubIndexes = new ArrayList<>();
    private final List<Ability> abilityBarSlots = new ArrayList<>();
    private final List<Boolean> abilityWheelIsCopied = new ArrayList<>();
    private final List<Boolean> abilityBarIsCopied = new ArrayList<>();
    private final List<Integer> abilityBarSubIndexes = new ArrayList<>();
    private final List<String> sharedWheelSlots = new ArrayList<>();
    private int draggedFromSharedWheelIndex = -1;
    private int draggedFromSharedPoolIndex = -1;
    private Ability draggedAbility = null;
    private int draggedSubIndex = -1;
    private int draggedFromWheelIndex = -1;
    private int draggedFromBarIndex = -1;
    private boolean draggedFromAvailable = false;
    // For copied-ability dragging: index in the copied list
    private int draggedFromCopiedIndex = -1;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    private static final int ABILITIES_PANEL_WIDTH = 120;
    private static final int ABILITIES_PANEL_HEIGHT = 115;
    private static final int ABILITY_WHEEL_HEIGHT = 100;
    // Ability bar: 2 rows x 5 columns (the old single row overlapped its own icons)
    private static final int BAR_COLS = 5;
    private static final int BAR_CELL_W = 22;
    private static final int BAR_CELL_H = 28;
    private static final int ABILITY_BAR_HEIGHT = 75;
    private static final int ABILITY_ICON_SIZE = 16;
    private static final int ABILITY_WHEEL_MAX = 24;
    private static final int ABILITY_BAR_MAX = 10;
    private static final int SHARED_POOL_HEIGHT = 50;
    private static final int SHARED_WHEEL_HEIGHT = 60;

    // Height of the copied-abilities list panel (replaces the normal ABILITIES_PANEL when active)
    private static final int COPIED_PANEL_HEIGHT = 115;

    private static final int QUESTS_PANEL_WIDTH = 140;
    private static final int COMPLETED_QUESTS_HEIGHT = 80;
    private static final int ACTIVE_QUEST_HEIGHT = 140;
    private static final int QUEST_ITEM_SIZE = 16;

    private static final int ACTING_PANEL_WIDTH = 140;
    private static final int ACTING_PANEL_HEIGHT = 120;

    private static final int ALLY_PANEL_WIDTH = 140;
    private static final int ALLY_PANEL_HEIGHT = 200;
    private static final int ALLY_ROW_H = 12;
    private static final int ALLY_MAX_ROWS = 8;
    private static final int ALLY_MAX_REQUESTS = 5;

    private static final String[] KEYBIND_LABELS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};

    private int abilitiesScrollOffset = 0;
    private int maxAbilitiesScroll = 0;
    private int completedQuestsScrollOffset = 0;
    private int maxCompletedQuestsScroll = 0;
    private int sharedPoolScrollOffset = 0;
    private int maxSharedPoolScroll = 0;
    private int copiedScrollOffset = 0;
    private int maxCopiedScroll = 0;

    // Filled while rendering the active-quest drawer so tooltips use the exact same positions
    private int questRewardX = 0;
    private int questRewardY = 0;

    private record SubAbilityEntry(Ability parent, int subIndex) {}

    // =====================================================================================
    //  Custom widgets
    // =====================================================================================

    /** Brass index-tab style button (works at any size thanks to the 9-slice sprites). */
    private static class BrassButton extends Button {
        private final ResourceLocation icon;
        private boolean selected = false;
        private int glowRgb = FALLBACK_ACCENT;

        BrassButton(int x, int y, int w, int h, Component label, ResourceLocation icon, OnPress onPress) {
            super(x, y, w, h, label, onPress, DEFAULT_NARRATION);
            this.icon = icon;
        }

        BrassButton selected(boolean selected) {
            this.selected = selected;
            return this;
        }

        BrassButton glow(int rgb) {
            this.glowRgb = rgb;
            return this;
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            Font font = Minecraft.getInstance().font;
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();

            ResourceLocation spr;
            if (!this.active) spr = SPR_TAB_LOCKED;
            else if (selected) spr = SPR_TAB_ACTIVE;
            else if (isHoveredOrFocused()) spr = SPR_TAB_HOVER;
            else spr = SPR_TAB_IDLE;

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            g.blitSprite(spr, x, y, w, h);

            if (selected && this.active) {
                float pulse = 0.65f + 0.2f * Mth.sin((Util.getMillis() % 100000L) / 160f);
                setTint(glowRgb, pulse);
                g.blitSprite(SPR_TAB_GLOW, x, y, w, h);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }

            int color;
            if (!this.active) color = 0xFF6E6C78;
            else if (selected) color = 0xFF000000 | glowRgb;
            else if (isHoveredOrFocused()) color = 0xFFFFFFFF;
            else color = TEXT_NORMAL;

            int textX;
            if (icon != null) {
                g.blitSprite(icon, x + 6, y + (h - 9) / 2, 9, 9);
                textX = x + 19;
            } else {
                textX = x + (w - font.width(getMessage())) / 2;
            }
            int textY = y + (h - 8) / 2;

            g.enableScissor(x + 3, y + 1, x + w - 3, y + h - 1);
            g.drawString(font, getMessage(), Math.max(textX, x + 3), textY, color, true);
            g.disableScissor();
        }
    }

    /** Small square keycap button (✓ / ✗). */
    private static class MiniButton extends Button {
        MiniButton(int x, int y, int size, Component label, OnPress onPress) {
            super(x, y, size, size, label, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            Font font = Minecraft.getInstance().font;
            ResourceLocation spr;
            if (!this.active) spr = SPR_MINI_DISABLED;
            else if (isHoveredOrFocused()) spr = SPR_MINI_HOVER;
            else spr = SPR_MINI_IDLE;
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            g.blitSprite(spr, getX(), getY(), getWidth(), getHeight());
            int tw = font.width(getMessage());
            g.drawString(font, getMessage(), getX() + (getWidth() - tw) / 2 + 1, getY() + (getHeight() - 8) / 2 + 1, 0xFFFFFFFF, true);
        }
    }

    /** "All abilities" toggle drawn as a little lever. */
    private static class LeverButton extends Button {
        private final boolean on;

        LeverButton(int x, int y, boolean on, OnPress onPress) {
            super(x, y, 60, 16, Component.literal("All"), onPress, DEFAULT_NARRATION);
            this.on = on;
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            Font font = Minecraft.getInstance().font;
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            g.blitSprite(on ? SPR_LEVER_ON : SPR_LEVER_OFF, getX() + getWidth() - 24, getY(), 24, 16);
            int color = on ? 0xFF8FD694 : (isHoveredOrFocused() ? 0xFFFFFFFF : TEXT_DIM);
            g.drawString(font, "All", getX() + 4, getY() + 4, color, true);
        }
    }

    // =====================================================================================

    public IntrospectScreen(IntrospectMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.playerInventory = playerInventory;
        this.imageHeight = 231;
        this.imageWidth = 192;
    }

    private boolean hasRecordAbility() {
        return !AbilityWheelClientData.getCopiedAbilityIds().isEmpty();
    }

    // -----------------------------------------------------------------------
    //  Small drawing helpers
    // -----------------------------------------------------------------------

    private static void setTint(int rgb, float alpha) {
        RenderSystem.setShaderColor(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, alpha);
    }

    private int accentRgb() {
        var info = BeyonderData.pathwayInfos.get(menu.getPathway());
        return info != null ? (info.color() & 0xFFFFFF) : FALLBACK_ACCENT;
    }

    private float animTicks(float partialTick) {
        return (Util.getMillis() % 1_000_000L) / 50f;
    }

    private void heading(GuiGraphics g, Component text, int x, int y) {
        g.drawString(this.font, text.copy().withStyle(ChatFormatting.BOLD), x, y, TEXT_HEADING, true);
    }

    private void drawDrawer(GuiGraphics g, int x, int y, int w, int h) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        g.blitSprite(SPR_DRAWER, x, y, w, h);
    }

    /** Thin brass rule under a drawer title. */
    private void drawTitleRule(GuiGraphics g, int x, int y, int w) {
        g.fill(x + 4, y + 14, x + w - 4, y + 15, 0x66D9A94F);
    }

    private void drawSlot(GuiGraphics g, int x, int y, boolean hover, boolean dropTarget, boolean copied) {
        ResourceLocation s;
        if (dropTarget) s = SPR_SLOT_DROP;
        else if (copied) s = SPR_SLOT_COPIED;
        else if (hover) s = SPR_SLOT_HOVER;
        else s = SPR_SLOT_IDLE;
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        g.blitSprite(s, x, y, 18, 18);
    }

    private void drawCounter(GuiGraphics g, int x, int y, int w, int h, String text, int color) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        g.blitSprite(SPR_COUNTER, x, y, w, h);
        int tw = this.font.width(text);
        g.drawString(this.font, text, x + (w - tw) / 2, y + (h - 8) / 2, color, false);
    }

    private void drawRotated(GuiGraphics g, ResourceLocation spr, float cx, float cy, int size, float degrees) {
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(cx, cy, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(degrees));
        g.blitSprite(spr, -size / 2, -size / 2, size, size);
        pose.popPose();
    }

    private void renderCogs(GuiGraphics g, int x, int y, float t) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        // Large cog peeks out of the top-right corner; small one meshes with it (8:10 tooth ratio)
        float large = t * 0.5f;
        drawRotated(g, SPR_COG_LARGE, x + 170, y - 2, 48, large);
        drawRotated(g, SPR_COG_SMALL, x + 138, y - 10, 24, -large * 1.25f + 15f);
    }

    /** Tube gauge: frame -> liquid -> glass. progress 0..1. */
    private void renderTube(GuiGraphics g, int x, int y, float progress, int topColor, int bottomColor, float t) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        g.blitSprite(SPR_TUBE_FRAME, x, y, TUBE_W, TUBE_H);

        int lx = x + TUBE_LIQUID_X;
        int ly = y + TUBE_LIQUID_Y;
        int fill = Mth.clamp((int) (TUBE_LIQUID_W * progress), 0, TUBE_LIQUID_W);
        if (progress > 0.001f && fill < 1) fill = 1;

        if (fill > 0) {
            g.fillGradient(lx, ly, lx + fill, ly + TUBE_LIQUID_H, topColor, bottomColor);
            g.fill(lx, ly, lx + fill, ly + 1, 0x55FFFFFF);          // surface shine

            int sheenW = 6;
            int sx = lx + (int) ((t * 0.7f) % (TUBE_LIQUID_W + sheenW)) - sheenW;
            int x0 = Math.max(sx, lx);
            int x1 = Math.min(sx + sheenW, lx + fill);
            if (x1 > x0) g.fill(x0, ly + 1, x1, ly + TUBE_LIQUID_H - 1, 0x26FFFFFF);

            g.fill(lx + fill - 1, ly, lx + fill, ly + TUBE_LIQUID_H, 0x40FFFFFF);   // meniscus
        }
        g.blitSprite(SPR_TUBE_GLASS, x, y, TUBE_W, TUBE_H);
    }

    /** Pressure dial for sanity. value 0..1, needle sweeps -120..+120 degrees. */
    private void renderDial(GuiGraphics g, int x, int y, float value, float t) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        g.blitSprite(SPR_DIAL, x, y, 32, 32);

        float angle = -120f + 240f * Mth.clamp(value, 0f, 1f);
        if (value < 0.25f) {
            angle += Mth.sin(t * 1.7f) * (1.5f + (0.25f - value) * 8f);   // nervous needle
        }
        drawRotated(g, SPR_NEEDLE, x + 16f, y + 16f, 32, angle);
        g.blitSprite(SPR_DIAL_GLASS, x, y, 32, 32);
    }

    // -----------------------------------------------------------------------

    private void initializeAbilities() {
        availableAbilities.clear();
        subAbilityEntries.clear();
        passiveAbilities.clear();
        abilitiesScrollOffset = 0;

        if (showAllAbilities) {
            availableAbilities.addAll(LOTMCraft.abilityHandler.getAllAbilitiesUpToSequenceOrdered(menu.getSequence()));
        } else {
            availableAbilities.addAll(LOTMCraft.abilityHandler.getAllAbilitiesForEntity(minecraft.player));
        }

        List<Ability> unique = availableAbilities.stream().distinct().toList();
        availableAbilities.clear();
        availableAbilities.addAll(unique);
        availableAbilities.removeIf(Ability::getShouldBeHidden);

        // Sub-abilities toggle only applies on normal tabs (not copied tabs)
        if (showSubAbilities && !isCopiedTab(currentTab)) {
            List<Ability> expanded = new ArrayList<>();
            for (Ability ability : availableAbilities) {
                expanded.add(ability);
                subAbilityEntries.add(null);
                if (ability instanceof SelectableAbility sa) {
                    String[] names = sa.getAbilityNamesCopy();
                    if (names.length > 1) {
                        for (int si = 0; si < names.length; si++) {
                            expanded.add(ability);
                            subAbilityEntries.add(new SubAbilityEntry(ability, si));
                        }
                    }
                }
            }
            availableAbilities.clear();
            availableAbilities.addAll(expanded);
        } else {
            for (int i = 0; i < availableAbilities.size(); i++) {
                subAbilityEntries.add(null);
            }
        }

        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int rows = (int) Math.ceil((double) availableAbilities.size() / iconsPerRow);
        int visibleRows = (ABILITIES_PANEL_HEIGHT - 20) / (ABILITY_ICON_SIZE + 2);
        maxAbilitiesScroll = Math.max(0, rows - visibleRows);

        // Also recompute copied scroll
        updateCopiedScroll();

        passiveAbilities.addAll(LOTMCraft.passiveAbilityHandler.getPassiveAbilitiesForEntity(minecraft.player));
    }

    private boolean isCopiedTab(Tab tab) {
        return tab == Tab.RECORDED_ABILITIES;
    }

    private void updateCopiedScroll() {
        List<String> ids = AbilityWheelClientData.getCopiedAbilityIds();
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int rows = (int) Math.ceil((double) ids.size() / iconsPerRow);
        int visibleRows = (COPIED_PANEL_HEIGHT - 20) / (ABILITY_ICON_SIZE + 2);
        maxCopiedScroll = Math.max(0, rows - visibleRows);
        copiedScrollOffset = Math.min(copiedScrollOffset, maxCopiedScroll);
    }

    private void updateCompletedQuestsScroll() {
        int questsCount = ClientQuestData.getCompletedQuests().size();
        int lineHeight = this.font.lineHeight + 2;
        int visibleLines = (COMPLETED_QUESTS_HEIGHT - 20) / lineHeight;
        maxCompletedQuestsScroll = Math.max(0, questsCount - visibleLines);
    }

    public void setAbilityWheelSlots(ArrayList<String> abilityIds) {
        this.abilityWheelSlots.clear();
        this.abilityWheelSubIndexes.clear();
        this.abilityWheelIsCopied.clear();
        for (String id : abilityIds) {
            AbilityId parsed = AbilityId.parse(id);
            Ability ability = LOTMCraft.abilityHandler.getById(parsed.baseId());
            if (ability != null) {
                this.abilityWheelSlots.add(ability);
                this.abilityWheelSubIndexes.add(parsed.subIndex());
                this.abilityWheelIsCopied.add(parsed.copied());
            }
        }
    }

    public void setAbilityBarSlots(ArrayList<String> abilityIds) {
        this.abilityBarSlots.clear();
        this.abilityBarSubIndexes.clear();
        this.abilityBarIsCopied.clear();
        for (String id : abilityIds) {
            AbilityId parsed = AbilityId.parse(id);
            Ability ability = LOTMCraft.abilityHandler.getById(parsed.baseId());
            if (ability != null) {
                this.abilityBarSlots.add(ability);
                this.abilityBarSubIndexes.add(parsed.subIndex());
                this.abilityBarIsCopied.add(parsed.copied());
            }
        }
    }

    private ArrayList<String> wheelSlotsToIdList() {
        ArrayList<String> ids = new ArrayList<>();
        for (int i = 0; i < abilityWheelSlots.size(); i++) {
            ids.add(AbilityId.of(abilityWheelSlots.get(i), abilityWheelSubIndexes.get(i), abilityWheelIsCopied.get(i)).toString());
        }
        return ids;
    }

    private ArrayList<String> barSlotsToIdList() {
        ArrayList<String> ids = new ArrayList<>();
        for (int i = 0; i < abilityBarSlots.size(); i++) {
            ids.add(AbilityId.of(abilityBarSlots.get(i), abilityBarSubIndexes.get(i), abilityBarIsCopied.get(i)).toString());
        }
        return ids;
    }

    @Override
    protected void init() {
        super.init();

        if (this.minecraft == null) return;

        this.killCount = ClientSacrificeCache.getKillCount();

        PacketHandler.sendToServer(new RequestAbilityBarPacket());
        PacketHandler.sendToServer(new RequestQuestDataPacket());
        PacketHandler.sendToServer(new RequestSharedAbilitiesPacket());

        sharedWheelSlots.clear();
        sharedWheelSlots.addAll(AbilityWheelClientData.getSharedWheelAbilities());

        KEYBIND_LABELS[0] = LOTMCraft.useAbilityBarAbility1.getKey().getDisplayName().getString();
        KEYBIND_LABELS[1] = LOTMCraft.useAbilityBarAbility2.getKey().getDisplayName().getString();
        KEYBIND_LABELS[2] = LOTMCraft.useAbilityBarAbility3.getKey().getDisplayName().getString();
        KEYBIND_LABELS[3] = LOTMCraft.useAbilityBarAbility4.getKey().getDisplayName().getString();
        KEYBIND_LABELS[4] = LOTMCraft.useAbilityBarAbility5.getKey().getDisplayName().getString();
        KEYBIND_LABELS[5] = LOTMCraft.useAbilityBarAbility6.getKey().getDisplayName().getString();

        int panelX = this.leftPos + this.imageWidth + 5;
        int inputWidth = ALLY_PANEL_WIDTH - 10;
        int inputHeight = 16;
        int inputX = panelX + 5;
        int inputY = this.topPos + ALLY_PANEL_HEIGHT + 5;

        allyNameInput = new EditBox(this.font, inputX, inputY, inputWidth, inputHeight, Component.literal("Ally name"));
        allyNameInput.setMaxLength(32);
        allyNameInput.setHint(Component.literal("Enter player name"));
        this.addWidget(allyNameInput);

        updateCompletedQuestsScroll();
        updateButtonPositions();
    }

    private String abbreviateKeybind(String keybind) {
        if (keybind.startsWith("Button ")) return "B" + keybind.substring(7);
        if (keybind.equalsIgnoreCase("Not Bound") || keybind.equalsIgnoreCase("Unbound")) return "-";
        if (keybind.equalsIgnoreCase("Middle Mouse Button")) return "MMB";
        if (keybind.equalsIgnoreCase("Left Mouse Button")) return "LMB";
        if (keybind.equalsIgnoreCase("Right Mouse Button")) return "RMB";
        if (keybind.length() > 5) return keybind.substring(0, 4) + "…";
        return keybind;
    }

    /** Opens one side drawer (or closes it if it was already open). */
    private void togglePanel(SidePanel panel) {
        boolean wasOpen = switch (panel) {
            case ABILITIES -> showAbilities;
            case QUESTS -> showQuests;
            case ACTING -> showActing;
            case MISSED -> showMissedActing;
            case ALLIES -> showAllies;
        };

        showAbilities = false;
        showQuests = false;
        showActing = false;
        showMissedActing = false;
        showAllies = false;
        allyNameInput.setFocused(false);

        if (!wasOpen) {
            switch (panel) {
                case ABILITIES -> showAbilities = true;
                case QUESTS -> showQuests = true;
                case ACTING -> showActing = true;
                case MISSED -> showMissedActing = true;
                case ALLIES -> showAllies = true;
            }
        }
        updateButtonPositions();
    }

    private void addSideTab(String label, ResourceLocation icon, int yOffset, boolean selected, Button.OnPress press) {
        int growth = selected ? 4 : 0;
        int x = this.leftPos - TAB_W + 2 - growth;
        BrassButton b = new BrassButton(x, this.topPos + yOffset, TAB_W + growth, TAB_H,
                Component.literal(label), icon, press)
                .selected(selected)
                .glow(accentRgb());
        this.addRenderableWidget(b);
    }

    private void updateButtonPositions() {
        this.clearWidgets();
        allyWidgets.clear();
        allyWidgetSignature = null;

        int baseLeftPos = this.leftPos;

        this.addRenderableWidget(allyNameInput);
        allyNameInput.setVisible(showAllies);
        allyNameInput.setEditable(showAllies);

        addSideTab("Abilities", ICON_ABILITIES, 10, showAbilities, b -> togglePanel(SidePanel.ABILITIES));
        addSideTab("Quests", ICON_QUESTS, 35, showQuests, b -> togglePanel(SidePanel.QUESTS));
        addSideTab("Acting", ICON_ACTING, 60, showActing, b -> togglePanel(SidePanel.ACTING));
        addSideTab("Missed", ICON_MISSED, 85, showMissedActing, b -> togglePanel(SidePanel.MISSED));

        if (menu.getSequence() < 4) {
            addSideTab("Honorific", ICON_HONORIFIC, 110, false, b -> openHonorificNamesMenu());
        }

        if (ClientUniquenessCache.hasUniqueness() && menu.getSequence() == 1) {
            boolean canApotheosize = false;
            if (this.minecraft != null && this.minecraft.player != null) {
                int charStack = ClientBeyonderCache.getCharStack(this.minecraft.player.getUUID());
                canApotheosize = ClientUniquenessCache.getKillCount() >= RequestUniquenessApotheosisPacket.KILLS_REQUIRED_FOR_APOTHEOSIS && charStack >= 2;
            }
            final boolean finalCanApotheosize = canApotheosize;

            BrassButton apotheosisButton = new BrassButton(baseLeftPos - TAB_W + 2, this.topPos + 135, TAB_W, TAB_H,
                    Component.literal("Apotheosis").withStyle(finalCanApotheosize ? ChatFormatting.GOLD : ChatFormatting.GRAY),
                    ICON_APOTHEOSIS,
                    button -> {
                        if (finalCanApotheosize) PacketHandler.sendToServer(new RequestUniquenessApotheosisPacket());
                    })
                    .glow(0xFFAA00);
            apotheosisButton.active = finalCanApotheosize;
            this.addRenderableWidget(apotheosisButton);
        }

        if (isCreativeOp()) {
            LeverButton lever = new LeverButton(baseLeftPos - TAB_W + 2, this.topPos + 162, showAllAbilities,
                    button -> {
                        showAllAbilities = !showAllAbilities;
                        initializeAbilities();
                        updateButtonPositions();
                    });
            this.addRenderableWidget(lever);
        }

        addSideTab("Allies", ICON_ALLIES, 185, showAllies, b -> togglePanel(SidePanel.ALLIES));

        if (showAbilities) {
            addAbilityButtons(baseLeftPos);
        }

        if (showQuests) {
            addQuestButtons(baseLeftPos);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (allyNameInput.isFocused() && allyNameInput.isVisible()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                allyNameInput.setFocused(false);
                return true;
            }
            return allyNameInput.keyPressed(keyCode, scanCode, modifiers)
                    || allyNameInput.canConsumeInput();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static String tabLabel(String full, String abbreviated, int width) {
        return width >= 38 ? full : abbreviated;
    }

    private void addAbilityButtons(int baseLeftPos) {
        int panelX = baseLeftPos + this.imageWidth + 5;
        int tabButtonY = this.topPos;

        boolean showSharedTab = ClientTeamData.hasTeam();
        boolean showCopiedTab = hasRecordAbility();

        // Count how many tabs we have
        int tabCount = 2; // Wheel + Bar always present
        if (showSharedTab) tabCount++;
        if (showCopiedTab) tabCount++;

        // Sub-abilities button only shown on non-copied tabs
        boolean showSubToggle = !isCopiedTab(currentTab);
        int subButtonWidth = showSubToggle ? 34 : 0;
        int remainingWidth = ABILITIES_PANEL_WIDTH - subButtonWidth;
        int tabButtonWidth = remainingWidth / tabCount;

        int tabX = panelX;

        this.addRenderableWidget(new BrassButton(tabX, tabButtonY, tabButtonWidth, 15,
                Component.literal(tabLabel("Wheel", "Whl", tabButtonWidth)), null,
                button -> {
                    currentTab = Tab.ABILITY_WHEEL;
                    copiedScrollOffset = 0;
                    updateButtonPositions();
                })
                .selected(currentTab == Tab.ABILITY_WHEEL).glow(accentRgb()));
        tabX += tabButtonWidth;

        this.addRenderableWidget(new BrassButton(tabX, tabButtonY, tabButtonWidth, 15,
                Component.literal("Bar"), null,
                button -> {
                    currentTab = Tab.ABILITY_BAR;
                    copiedScrollOffset = 0;
                    updateButtonPositions();
                })
                .selected(currentTab == Tab.ABILITY_BAR).glow(accentRgb()));
        tabX += tabButtonWidth;

        if (showSharedTab) {
            this.addRenderableWidget(new BrassButton(tabX, tabButtonY, tabButtonWidth, 15,
                    Component.literal(tabLabel("Shared", "Shr", tabButtonWidth)), null,
                    button -> {
                        currentTab = Tab.SHARED_ABILITIES;
                        sharedPoolScrollOffset = 0;
                        copiedScrollOffset = 0;
                        updateButtonPositions();
                    })
                    .selected(currentTab == Tab.SHARED_ABILITIES).glow(accentRgb()));
            tabX += tabButtonWidth;
        } else if (currentTab == Tab.SHARED_ABILITIES) {
            currentTab = Tab.ABILITY_WHEEL;
        }

        if (showCopiedTab) {
            this.addRenderableWidget(new BrassButton(tabX, tabButtonY, tabButtonWidth, 15,
                    Component.literal("Cop").withStyle(ChatFormatting.LIGHT_PURPLE), null,
                    button -> {
                        currentTab = Tab.RECORDED_ABILITIES;
                        copiedScrollOffset = 0;
                        updateCopiedScroll();
                        updateButtonPositions();
                    })
                    .selected(currentTab == Tab.RECORDED_ABILITIES).glow(0xB07CE8));
            tabX += tabButtonWidth;
        } else if (currentTab == Tab.RECORDED_ABILITIES) {
            currentTab = Tab.ABILITY_WHEEL;
        }

        // Sub-abilities toggle — only for non-copied tabs
        if (showSubToggle) {
            this.addRenderableWidget(new BrassButton(panelX + ABILITIES_PANEL_WIDTH - subButtonWidth, tabButtonY, subButtonWidth, 15,
                    Component.literal("Sub").withStyle(showSubAbilities ? ChatFormatting.AQUA : ChatFormatting.GRAY), null,
                    button -> {
                        showSubAbilities = !showSubAbilities;
                        initializeAbilities();
                        updateButtonPositions();
                    })
                    .selected(showSubAbilities).glow(0x55FFFF));
        }

        int clearButtonX = baseLeftPos + this.imageWidth + 5;
        int clearButtonY;

        if (currentTab == Tab.ABILITY_WHEEL) {
            clearButtonY = this.topPos + 15 + ABILITIES_PANEL_HEIGHT + 5 + ABILITY_WHEEL_HEIGHT + 5;
            this.addRenderableWidget(new BrassButton(clearButtonX, clearButtonY, ABILITIES_PANEL_WIDTH, 20,
                    Component.literal("Clear Wheel").withStyle(ChatFormatting.RED), null,
                    button -> {
                        abilityWheelSlots.clear();
                        abilityWheelSubIndexes.clear();
                        abilityWheelIsCopied.clear();
                        PacketHandler.sendToServer(new SyncAbilityWheelAbilitiesPacket(new ArrayList<>()));
                    }));
        } else if (currentTab == Tab.ABILITY_BAR) {
            clearButtonY = this.topPos + 15 + ABILITIES_PANEL_HEIGHT + 5 + ABILITY_BAR_HEIGHT + 5;
            this.addRenderableWidget(new BrassButton(clearButtonX, clearButtonY, ABILITIES_PANEL_WIDTH, 20,
                    Component.literal("Clear Bar").withStyle(ChatFormatting.RED), null,
                    button -> {
                        abilityBarSlots.clear();
                        abilityBarSubIndexes.clear();
                        abilityBarIsCopied.clear();
                        PacketHandler.sendToServer(new SyncAbilityBarAbilitiesPacket(new ArrayList<>()));
                    }));
        }
        // Copied tabs: clear button for the wheel drop zone
        else if (isCopiedTab(currentTab)) {
            clearButtonY = this.topPos + 15 + COPIED_PANEL_HEIGHT + 5 + ABILITY_WHEEL_HEIGHT + 5;
            this.addRenderableWidget(new BrassButton(clearButtonX, clearButtonY, ABILITIES_PANEL_WIDTH / 2 - 1, 20,
                    Component.literal("Clear Wheel").withStyle(ChatFormatting.RED), null,
                    button -> {
                        abilityWheelSlots.clear();
                        abilityWheelSubIndexes.clear();
                        abilityWheelIsCopied.clear();
                        PacketHandler.sendToServer(new SyncAbilityWheelAbilitiesPacket(new ArrayList<>()));
                    }));
        }
    }

    private void addQuestButtons(int baseLeftPos) {
        int panelX = baseLeftPos + this.imageWidth + 5;

        if (ClientQuestData.hasActiveQuest()) {
            int discardButtonY = this.topPos + COMPLETED_QUESTS_HEIGHT + 5 + ACTIVE_QUEST_HEIGHT + 5;

            this.addRenderableWidget(new BrassButton(panelX, discardButtonY, QUESTS_PANEL_WIDTH, 20,
                    Component.literal("Discard Quest").withStyle(ChatFormatting.RED), null,
                    button -> {
                        PacketHandler.sendToServer(new DiscardQuestPacket());
                        PacketHandler.sendToServer(new RequestQuestDataPacket());
                    }));
        }
    }

    private void openHonorificNamesMenu() {
        if (menu.getSequence() >= 4) return;
        PacketHandler.sendToServer(new OpenHonorificNamesMenuPacket());
    }

    private boolean isCreativeOp() {
        return this.minecraft != null && this.minecraft.player != null
                && this.minecraft.player.isCreative()
                && this.minecraft.player.hasPermissions(2);
    }

    private int killCount = 0;

    public void updateKillCount(int killCount) {
        this.killCount = killCount;
    }

    public void updateMenuData(int sequence, String pathway, float digestionProgress, float sanity) {
        this.menu.updateData(sequence, pathway, digestionProgress, sanity);
        initializeAbilities();
    }

    // =====================================================================================
    //  Render
    // =====================================================================================

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Ally widgets must be rebuilt *before* the widget pass, never during it
        if (showAllies) {
            syncAllyWidgets();
        }

        this.hoveredPassive = null;
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        // renderBg (below) draws the frame AND all drawers, so widgets render on top of them
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (draggedAbility != null) {
            renderAbilityIcon(guiGraphics, draggedAbility, mouseX - dragOffsetX, mouseY - dragOffsetY, draggedSubIndex);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);

        if (showAbilities && draggedAbility == null) {
            renderAbilityTooltips(guiGraphics, mouseX, mouseY);
        }

        if (showQuests) {
            renderQuestItemTooltips(guiGraphics, mouseX, mouseY);
        }

        renderPassiveTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int x = this.leftPos;
        int y = this.topPos;
        float t = animTicks(partialTick);

        if (SHOW_COGS) renderCogs(guiGraphics, x, y, t);

        guiGraphics.blitSprite(SPR_FRAME, x, y, this.imageWidth, this.imageHeight);

        renderHeader(guiGraphics, x, y, t);
        renderDigestion(guiGraphics, x, y, t);
        renderSanity(guiGraphics, x, y, t);
        renderPassiveSection(guiGraphics, x, y, mouseX, mouseY);
        renderBottomRow(guiGraphics, x, y);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // Side drawers are drawn here (not in render()) so that their buttons draw on top
        if (showQuests) renderQuestPanel(guiGraphics);
        if (showAbilities) renderAbilitiesPanel(guiGraphics, mouseX, mouseY);
        if (showActing) renderActingPanel(guiGraphics);
        if (showAllies) renderAlliesPanel(guiGraphics);
        if (showMissedActing) renderMissedActingPanel(guiGraphics);

        RenderSystem.disableBlend();
    }

    // -------------------------------------------------------------- main panel sections

    private void renderHeader(GuiGraphics g, int x, int y, float t) {
        int accent = accentRgb();

        // --- pathway icon in the brass frame ---
        int fx = x + ICON_FRAME_X;
        int fy = y + ICON_FRAME_Y;
        int s = ICON_FRAME_SIZE;
        int inner = s - 12;

        ResourceLocation iconTexture = ResourceLocation.fromNamespaceAndPath(
                LOTMCraft.MOD_ID, "textures/gui/icons/" + menu.getPathway() + "_icon.png");
        g.blit(iconTexture, fx + 6, fy + 6, inner, inner, 0, 0, 62, 62, 62, 62);
        g.blitSprite(SPR_ICON_SHADOW, fx, fy, s, s);
        g.blitSprite(SPR_ICON_FRAME, fx, fy, s, s);

        float pulse = 0.55f + 0.25f * Mth.sin(t * 0.12f);
        setTint(accent, pulse);
        g.blitSprite(SPR_ICON_GLOW, fx, fy, s, s);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // --- sequence counter ---
        heading(g, Component.translatable("lotm.sequence"), x + 16, y + SEQ_LABEL_Y);
        drawCounter(g, x + 16, y + SEQ_COUNTER_Y, 30, 16, String.valueOf(menu.getSequence()), TEXT_HEADING);

        Player player = playerInventory.player;
        int charStackCount = 0;
        if (player.level().isClientSide) {
            charStackCount = ClientBeyonderCache.getCharStack(player.getUUID());
        }
        if (charStackCount > 0) {
            g.drawString(this.font, Component.literal("+" + charStackCount).withStyle(ChatFormatting.BOLD),
                    x + 50, y + SEQ_COUNTER_Y + 4, TEXT_GOLD, true);
        }

        // --- sequence name on a nameplate (wraps to two lines) ---
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        g.blitSprite(SPR_NAMEPLATE, x + 16, y + NAMEPLATE_Y, NAMEPLATE_W, NAMEPLATE_H);

        String name = BeyonderData.getSequenceName(menu.getPathway(), menu.getSequence());
        List<String> nameLines = wrapText(name, NAMEPLATE_W - 20);
        if (nameLines.size() > 2) {
            nameLines = new ArrayList<>(nameLines.subList(0, 2));
            nameLines.set(1, nameLines.get(1) + "…");
        }
        int totalH = nameLines.size() * this.font.lineHeight;
        int ty = y + NAMEPLATE_Y + (NAMEPLATE_H - totalH) / 2 + 1;
        for (String line : nameLines) {
            int lw = this.font.width(line);
            g.drawString(this.font, line, x + 16 + (NAMEPLATE_W - lw) / 2, ty, 0xFF000000 | accent, true);
            ty += this.font.lineHeight;
        }
    }

    private void renderDigestion(GuiGraphics g, int x, int y, float t) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        g.blitSprite(SPR_DIVIDER, x + PAD_X, y + DIV1_Y, CONTENT_W, 5);

        heading(g, Component.translatable("lotm.digestion"), x + 16, y + DIGEST_LABEL_Y);

        float progress = menu.getDigestionProgress();
        renderTube(g, x + PAD_X, y + TUBE_Y, progress, 0xFFE8795F, 0xFFA8422D, t);

        drawCounter(g, x + PAD_X + TUBE_W + 4, y + TUBE_Y + 1, 30, 14,
                (int) (Mth.clamp(progress, 0f, 1f) * 100) + "%", TEXT_NORMAL);
    }

    private void renderSanity(GuiGraphics g, int x, int y, float t) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        g.blitSprite(SPR_DIVIDER, x + PAD_X, y + DIV2_Y, CONTENT_W, 5);

        float sanity = Mth.clamp(menu.getSanity(), 0f, 1f);
        if (displaySanity < 0f) displaySanity = sanity;
        displaySanity += (sanity - displaySanity) * 0.12f;        // needle eases towards the value

        renderDial(g, x + 16, y + DIAL_Y, displaySanity, t);

        int tx = x + 56;
        heading(g, Component.translatable("lotm.sanity"), tx, y + DIAL_Y + 1);

        drawCounter(g, tx, y + DIAL_Y + 12, 30, 14, (int) (sanity * 100) + "%", TEXT_NORMAL);

        String state;
        int stateColor;
        if (sanity > 0.66f) {
            state = "Stable";
            stateColor = 0xFF8FD694;
        } else if (sanity > 0.33f) {
            state = "Strained";
            stateColor = 0xFFE8BB68;
        } else {
            state = "Critical";
            stateColor = 0xFFE36C54;
        }
        g.drawString(this.font, state, tx + 36, y + DIAL_Y + 15, stateColor, true);

        if (minecraft != null && minecraft.player != null) {
            float capReduction = minecraft.player.getPersistentData()
                    .getFloat(de.jakob.lotm.beyonders.acting.ActingCapHelper.CAP_REDUCTION_KEY);
            if (capReduction > 0.001f) {
                int capPct = Math.round((1f - capReduction) * 100);
                g.drawString(this.font, Component.literal("Acting cap: " + capPct + "%").withStyle(ChatFormatting.GOLD),
                        tx, y + DIAL_Y + 26, 0xFFFFAA00, true);
            }
        }
    }

    private void renderPassiveSection(GuiGraphics g, int x, int y, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        g.blitSprite(SPR_DIVIDER, x + PAD_X, y + DIV3_Y, CONTENT_W, 5);

        heading(g, Component.translatable("lotm.passive_abilities"), x + 16, y + PASSIVE_LABEL_Y);

        int slotsY = y + PASSIVE_SLOT_Y;
        int slotsX = x + PAD_X;
        int accent = accentRgb();

        for (int i = 0; i < 9; i++) {
            int sx = slotsX + i * 18;
            boolean filled = i < passiveAbilities.size();
            boolean hovered = filled && mouseX >= sx && mouseX < sx + 18 && mouseY >= slotsY && mouseY < slotsY + 18;

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            g.blitSprite(!filled ? SPR_SLOT_EMPTY : (hovered ? SPR_SLOT_HOVER : SPR_SLOT_IDLE), sx, slotsY, 18, 18);

            if (filled) {
                PassiveAbility passive = passiveAbilities.get(i);
                renderPassiveAbilityIcon(g, passive, sx + 1, slotsY + 1);
                if (hovered) {
                    setTint(accent, 0.9f);
                    g.blitSprite(SPR_SLOT_RING, sx, slotsY, 18, 18);
                    RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                    hoveredPassive = passive;
                }
            }
        }
    }

    private void renderBottomRow(GuiGraphics g, int x, int y) {
        // Red priest kill counter (right aligned)
        if (menu.getPathway().equals("red_priest") && menu.getSequence() <= 3) {
            Component text = Component.literal("Kills: " + killCount).withStyle(ChatFormatting.BOLD);
            int w = this.font.width(text);
            g.drawString(this.font, text, x + PAD_X + CONTENT_W - w, y + BOTTOM_Y + 5, TEXT_BODY, true);
        }

        // Uniqueness
        if (!ClientUniquenessCache.hasUniqueness()) return;
        String pathway = ClientUniquenessCache.getPathway();
        if (pathway.isEmpty()) return;

        ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(
                LOTMCraft.MOD_ID, "textures/item/" + pathway + "_uniqueness.png");

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        g.blitSprite(SPR_SLOT_HOVER, x + PAD_X, y + BOTTOM_Y, 18, 18);
        g.blit(textureLocation, x + PAD_X + 1, y + BOTTOM_Y + 1, 0, 0, 16, 16, 16, 16);

        int kills = ClientUniquenessCache.getKillCount();
        Component killText = Component.literal(kills + "/" + RequestUniquenessApotheosisPacket.KILLS_REQUIRED_FOR_APOTHEOSIS + " kills")
                .withStyle(ChatFormatting.GOLD);
        g.drawString(this.font, killText, x + PAD_X + 22, y + BOTTOM_Y + 5, 0xFFFFAA00, true);
    }

    private void renderPassiveTooltip(GuiGraphics g, int mouseX, int mouseY) {
        if (hoveredPassive == null) return;

        MutableComponent name = hoveredPassive.getName();
        MutableComponent description = hoveredPassive.getDescription();
        int color = BeyonderData.pathwayInfos.containsKey(menu.getPathway()) ? BeyonderData.pathwayInfos.get(menu.getPathway()).color() : 0xFFFFFF;

        List<Component> tooltipLines = new ArrayList<>();
        tooltipLines.add(name.withStyle(ChatFormatting.BOLD).withColor(color));
        if (description != null && !description.getString().startsWith("ability.")) {
            String descText = description.getString();
            List<String> wrappedLines = wrapText(descText, 100);
            for (String line : wrappedLines) {
                tooltipLines.add(Component.literal(line).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        g.renderTooltip(this.font, tooltipLines, java.util.Optional.empty(), mouseX, mouseY);
    }

    // -------------------------------------------------------------- Acting drawer

    private void renderActingPanel(GuiGraphics guiGraphics) {
        int baseLeftPos = this.leftPos;
        int panelX = baseLeftPos + this.imageWidth + 5;
        int panelY = this.topPos;

        drawDrawer(guiGraphics, panelX, panelY, ACTING_PANEL_WIDTH, ACTING_PANEL_HEIGHT);
        heading(guiGraphics, Component.literal("Acting"), panelX + 5, panelY + 5);
        drawTitleRule(guiGraphics, panelX, panelY, ACTING_PANEL_WIDTH);

        int listY = panelY + 15;
        int listHeight = COMPLETED_QUESTS_HEIGHT - 20;

        List<ActingTask> actingRequirements = new ArrayList<>(ActingTaskRegistry.getTasksFor(menu.getPathway(), menu.getSequence()));
        int skipLineAmount = 0;
        int lineHeight = this.font.lineHeight + 2;

        int startIndex = 0;
        int endIndex = Math.min(actingRequirements.size(), startIndex + (listHeight / lineHeight));

        for (int i = startIndex; i < endIndex; i++) {
            String questId = actingRequirements.get(i).getId();
            MutableComponent actingName = getActingTaskName(questId);
            if (!ActingHelper.isTriggerUnlocked(menu.getPathway(), menu.getSequence(), minecraft.player, questId)) {
                actingName = actingName.withStyle(ChatFormatting.OBFUSCATED);
            }

            if (actingName.getString().length() > 24) {
                actingName = Component.literal(actingName.getString().substring(0, 21).strip() + "…");
            }
            int textY = listY + (i - startIndex) * lineHeight + 5 + skipLineAmount * lineHeight;
            guiGraphics.drawString(this.font, "- ", panelX + 5, textY, accentRgb() | 0xFF000000, false);
            guiGraphics.drawString(this.font, actingName, panelX + 15, textY, TEXT_BODY, false);

            if (!Component.translatable("lotm.acting." + questId + ".description").getString().equals("lotm.acting." + questId + ".description")) {
                Component description = Component.translatable("lotm.acting." + questId + ".description");
                List<String> wrappedDesc = wrapText(description.getString(), ACTING_PANEL_WIDTH - 20);
                for (String line : wrappedDesc) {
                    textY += this.font.lineHeight;
                    guiGraphics.drawString(this.font, line, panelX + 15, textY, TEXT_DIM, false);
                }
                skipLineAmount += wrappedDesc.size();
            }
        }

        if (actingRequirements.isEmpty()) {
            Component noReqs = Component.literal("No acting requirements yet").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            int textWidth = this.font.width(noReqs);
            guiGraphics.drawString(this.font, noReqs, panelX + (ACTING_PANEL_WIDTH - textWidth) / 2,
                    panelY + ACTING_PANEL_HEIGHT / 2 - this.font.lineHeight / 2, TEXT_DIM, false);
        }
    }

    // -------------------------------------------------------------- Allies drawer

    private List<AllyComponent.AllyInfo> playerAllies(boolean requests) {
        Player player = ClientHandler.getPlayer();
        var component = player.getData(ModAttachments.ALLY_COMPONENT);
        var source = requests ? component.requests() : component.allies();
        return source.stream().filter(ally -> ally.isPlayer()).collect(Collectors.toList());
    }

    private int allyRowY(int panelY, int index) {
        return panelY + 17 + index * ALLY_ROW_H;
    }

    private int allyRequestsLabelY(int panelY) {
        return panelY + 17 + ALLY_MAX_ROWS * ALLY_ROW_H + 6;
    }

    private int allyRequestRowY(int panelY, int index) {
        return allyRequestsLabelY(panelY) + 13 + index * ALLY_ROW_H;
    }

    private String allySignature(List<AllyComponent.AllyInfo> allies, List<AllyComponent.AllyInfo> requests) {
        StringBuilder sb = new StringBuilder();
        for (AllyComponent.AllyInfo a : allies) sb.append(a.playerName()).append(',');
        sb.append('|');
        for (AllyComponent.AllyInfo a : requests) sb.append(a.playerName()).append(',');
        return sb.toString();
    }

    /** (Re)creates the ✓ / ✗ / "Request Ally" widgets only when the ally data actually changed. */
    private void syncAllyWidgets() {
        if (this.minecraft == null || this.minecraft.player == null) return;

        List<AllyComponent.AllyInfo> allies = playerAllies(false);
        List<AllyComponent.AllyInfo> requests = playerAllies(true);
        String signature = allySignature(allies, requests);
        if (signature.equals(allyWidgetSignature)) return;
        allyWidgetSignature = signature;

        for (var w : allyWidgets) this.removeWidget(w);
        allyWidgets.clear();

        int panelX = this.leftPos + this.imageWidth + 5;
        int panelY = this.topPos;
        int btn = ALLY_ROW_H - 1;

        for (int i = 0; i < Math.min(allies.size(), ALLY_MAX_ROWS); i++) {
            AllyComponent.AllyInfo ally = allies.get(i);
            int plateW = this.font.width(ally.playerName()) + this.font.width("- ") + 8;
            MiniButton remove = new MiniButton(panelX + 4 + plateW + 3, allyRowY(panelY, i), btn,
                    Component.literal("✗").withStyle(ChatFormatting.RED),
                    button -> {
                        PacketHandler.sendToServer(new RemoveAllyPacket(ally.uuid(), ally.playerName()));
                        allyWidgetSignature = null;
                    });
            allyWidgets.add(remove);
            this.addRenderableWidget(remove);
        }

        for (int i = 0; i < Math.min(requests.size(), ALLY_MAX_REQUESTS); i++) {
            AllyComponent.AllyInfo ally = requests.get(i);
            int plateW = this.font.width(ally.playerName()) + this.font.width("- ") + 8;
            int by = allyRequestRowY(panelY, i);
            int bx = panelX + 4 + plateW + 3;

            MiniButton accept = new MiniButton(bx, by, btn,
                    Component.literal("✓").withStyle(ChatFormatting.GREEN),
                    button -> {
                        PacketHandler.sendToServer(new HandleAllyRequestPacket(ally.uuid(), ally.playerName(), true));
                        ClientHandler.closeGUI();
                    });
            MiniButton deny = new MiniButton(bx + btn + 2, by, btn,
                    Component.literal("✗").withStyle(ChatFormatting.RED),
                    button -> {
                        PacketHandler.sendToServer(new HandleAllyRequestPacket(ally.uuid(), ally.playerName(), false));
                        ClientHandler.closeGUI();
                    });
            allyWidgets.add(accept);
            allyWidgets.add(deny);
            this.addRenderableWidget(accept);
            this.addRenderableWidget(deny);
        }

        int inputWidth = ALLY_PANEL_WIDTH - 10;
        int inputX = panelX + 5;
        int inputY = panelY + ALLY_PANEL_HEIGHT + 5;
        int buttonY = inputY + 16 + 5;

        BrassButton requestAllyButton = new BrassButton(inputX, buttonY, inputWidth, 20,
                Component.literal("Request Ally"), null,
                button -> {
                    String targetName = allyNameInput.getValue();
                    if (!targetName.isBlank()) {
                        PacketHandler.sendToServer(new SendAllyRequestPacket(targetName));
                    }
                    allyNameInput.setValue("");
                })
                .glow(accentRgb());
        allyWidgets.add(requestAllyButton);
        this.addRenderableWidget(requestAllyButton);
    }

    private void renderAlliesPanel(GuiGraphics guiGraphics) {
        int baseLeftPos = this.leftPos;
        int panelX = baseLeftPos + this.imageWidth + 5;
        int panelY = this.topPos;

        drawDrawer(guiGraphics, panelX, panelY, ALLY_PANEL_WIDTH, ALLY_PANEL_HEIGHT);
        heading(guiGraphics, Component.literal("Allies"), panelX + 5, panelY + 5);
        drawTitleRule(guiGraphics, panelX, panelY, ALLY_PANEL_WIDTH);

        List<AllyComponent.AllyInfo> allies = playerAllies(false);
        for (int i = 0; i < Math.min(allies.size(), ALLY_MAX_ROWS); i++) {
            drawAllyRow(guiGraphics, panelX, allyRowY(panelY, i), allies.get(i).playerName());
        }
        if (allies.isEmpty()) {
            guiGraphics.drawString(this.font, Component.literal("No allies yet").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                    panelX + 8, allyRowY(panelY, 0) + 2, TEXT_DIM, false);
        }

        int requestsY = allyRequestsLabelY(panelY);
        guiGraphics.fill(panelX + 4, requestsY - 4, panelX + ALLY_PANEL_WIDTH - 4, requestsY - 3, 0x66D9A94F);
        heading(guiGraphics, Component.literal("Ally Requests"), panelX + 5, requestsY);

        List<AllyComponent.AllyInfo> requests = playerAllies(true);
        for (int i = 0; i < Math.min(requests.size(), ALLY_MAX_REQUESTS); i++) {
            drawAllyRow(guiGraphics, panelX, allyRequestRowY(panelY, i), requests.get(i).playerName());
        }
    }

    private void drawAllyRow(GuiGraphics g, int panelX, int rowY, String name) {
        int plateW = this.font.width(name) + this.font.width("- ") + 8;
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        g.blitSprite(SPR_COUNTER, panelX + 4, rowY, plateW, ALLY_ROW_H - 1);
        g.drawString(this.font, "- ", panelX + 8, rowY + 2, 0xFFFFFFFF, false);
        g.drawString(this.font, Component.literal(name).withStyle(ChatFormatting.GOLD),
                panelX + 8 + this.font.width("- "), rowY + 2, 0xFFCCCCCC, false);
    }

    // -------------------------------------------------------------- Missed acting drawer

    private void renderMissedActingPanel(GuiGraphics guiGraphics) {
        int baseLeftPos = this.leftPos;
        int panelX = baseLeftPos + this.imageWidth + 5;
        int panelY = this.topPos;
        int panelHeight = ACTING_PANEL_HEIGHT + 40;

        drawDrawer(guiGraphics, panelX, panelY, ACTING_PANEL_WIDTH, panelHeight);
        heading(guiGraphics, Component.literal("Missed Acting"), panelX + 5, panelY + 5);
        drawTitleRule(guiGraphics, panelX, panelY, ACTING_PANEL_WIDTH);

        net.minecraft.nbt.CompoundTag missed = new net.minecraft.nbt.CompoundTag();
        if (minecraft.player != null) {
            missed = minecraft.player.getPersistentData()
                    .getCompound(de.jakob.lotm.beyonders.acting.ActingCapHelper.MISSED_ACTING_KEY);
        }

        int listY = panelY + 17;
        int lineHeight = this.font.lineHeight + 2;
        int maxY = panelY + panelHeight - 4;

        if (missed.isEmpty()) {
            Component none = Component.literal("No missed acting").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            int textWidth = this.font.width(none);
            guiGraphics.drawString(this.font, none,
                    panelX + (ACTING_PANEL_WIDTH - textWidth) / 2,
                    panelY + panelHeight / 2 - this.font.lineHeight / 2, TEXT_DIM, false);
            return;
        }

        List<String> groupKeys = new ArrayList<>(missed.getAllKeys());
        groupKeys.sort((a, b) -> {
            int sa = safeParseInt(a.contains("/") ? a.split("/", 2)[1] : "0");
            int sb = safeParseInt(b.contains("/") ? b.split("/", 2)[1] : "0");
            return Integer.compare(sb, sa);
        });

        int currentY = listY;
        for (String groupKey : groupKeys) {
            if (currentY + lineHeight > maxY) break;
            net.minecraft.nbt.CompoundTag group = missed.getCompound(groupKey);
            net.minecraft.nbt.CompoundTag tasks = group.getCompound("tasks");
            if (tasks.isEmpty()) continue;

            String seqStr = groupKey.contains("/") ? groupKey.split("/", 2)[1] : groupKey;

            for (String taskId : tasks.getAllKeys()) {
                if (currentY + lineHeight > maxY) break;

                MutableComponent taskName = getActingTaskName(taskId).withStyle(ChatFormatting.OBFUSCATED);
                guiGraphics.drawString(this.font,
                        Component.literal("- ").withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal("[Seq " + seqStr + "] ")
                                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                                .append(taskName),
                        panelX + 5, currentY, 0xFFCCCCCC, false);
                currentY += lineHeight;
            }
        }
    }

    private static int safeParseInt(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
    }

    private static final Map<String, String> SUFFIXES = Map.of(
            "_while_full_health", "lotm.acting.condition.while_full_health",
            "_while_low_health", "lotm.acting.condition.while_low_health",
            "_while_hurt", "lotm.acting.condition.while_hurt",
            "_at_night", "lotm.acting.condition.at_night"
    );

    private MutableComponent getActingTaskName(String taskId) {
        String suffixKey = null;
        for (String suffix : SUFFIXES.keySet()) {
            if (taskId.endsWith(suffix)) {
                suffixKey = SUFFIXES.get(suffix);
                taskId = taskId.substring(0, taskId.length() - suffix.length());
                break;
            }
        }

        if (taskId.startsWith("use_") && taskId.contains("_ability")) {
            String abilityId = taskId.substring(4);
            String abilitySuffixKey = null;
            for (String suffix : SUFFIXES.keySet()) {
                if (abilityId.endsWith(suffix)) {
                    abilitySuffixKey = SUFFIXES.get(suffix);
                    abilityId = abilityId.substring(0, abilityId.length() - suffix.length());
                    break;
                }
            }

            return Component.translatable("lotm.acting.ability_use").append(Component.translatable("lotmcraft." + abilityId))
                    .append(abilitySuffixKey != null ? Component.translatable(abilitySuffixKey) : Component.empty())
                    .append(suffixKey != null ? Component.translatable(suffixKey) : Component.empty());
        }

        return Component.translatable("lotm.acting." + taskId).append(suffixKey != null ? Component.translatable(suffixKey) : Component.empty());
    }

    // -------------------------------------------------------------- Quest drawers

    private void renderQuestPanel(GuiGraphics guiGraphics) {
        int baseLeftPos = this.leftPos;
        int panelX = baseLeftPos + this.imageWidth + 5;
        int panelY = this.topPos;

        renderCompletedQuestsSection(guiGraphics, panelX, panelY);

        int activeQuestY = panelY + COMPLETED_QUESTS_HEIGHT + 5;
        renderActiveQuestSection(guiGraphics, panelX, activeQuestY);
    }

    private void renderCompletedQuestsSection(GuiGraphics guiGraphics, int panelX, int panelY) {
        drawDrawer(guiGraphics, panelX, panelY, QUESTS_PANEL_WIDTH, COMPLETED_QUESTS_HEIGHT);
        heading(guiGraphics, Component.literal("Completed Quests"), panelX + 5, panelY + 5);
        drawTitleRule(guiGraphics, panelX, panelY, QUESTS_PANEL_WIDTH);

        int listY = panelY + 15;
        int listHeight = COMPLETED_QUESTS_HEIGHT - 20;

        List<String> completedQuests = new ArrayList<>(ClientQuestData.getCompletedQuests());
        int lineHeight = this.font.lineHeight + 2;

        int startIndex = completedQuestsScrollOffset;
        int endIndex = Math.min(completedQuests.size(), startIndex + (listHeight / lineHeight));

        for (int i = startIndex; i < endIndex; i++) {
            String questId = completedQuests.get(i);
            Component questName = Component.translatable("lotm.quest.impl." + questId);
            if (questName.getString().length() > 24) {
                questName = Component.literal(questName.getString().substring(0, 21).strip() + "…");
            }
            int textY = listY + (i - startIndex) * lineHeight + 2;
            guiGraphics.drawString(this.font, "✓ ", panelX + 5, textY, 0xFF4CAF50, false);
            guiGraphics.drawString(this.font, questName, panelX + 15, textY, TEXT_BODY, false);
        }

        if (maxCompletedQuestsScroll > 0) {
            Component scrollHint = Component.literal("(Scroll)").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            int hintWidth = this.font.width(scrollHint);
            guiGraphics.drawString(this.font, scrollHint, panelX + QUESTS_PANEL_WIDTH - hintWidth - 5,
                    panelY + COMPLETED_QUESTS_HEIGHT - 12, TEXT_DIM, false);
        }
    }

    private void renderActiveQuestSection(GuiGraphics guiGraphics, int panelX, int panelY) {
        drawDrawer(guiGraphics, panelX, panelY, QUESTS_PANEL_WIDTH, ACTIVE_QUEST_HEIGHT);
        heading(guiGraphics, Component.literal("Active Quest"), panelX + 5, panelY + 5);
        drawTitleRule(guiGraphics, panelX, panelY, QUESTS_PANEL_WIDTH);

        if (!ClientQuestData.hasActiveQuest()) {
            Component noQuest = Component.literal("No active quest").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            int textWidth = this.font.width(noQuest);
            guiGraphics.drawString(this.font, noQuest, panelX + (QUESTS_PANEL_WIDTH - textWidth) / 2,
                    panelY + 40, TEXT_DIM, false);
            return;
        }

        int accent = accentRgb();
        int contentY = panelY + 19;

        Component questName = Component.literal(ClientQuestData.getActiveQuestName())
                .withStyle(ChatFormatting.BOLD)
                .withColor(accent);

        if (questName.getString().length() > 24) {
            questName = Component.literal(questName.getString().substring(0, 21).strip() + "…")
                    .withStyle(ChatFormatting.BOLD)
                    .withColor(accent);
        }

        guiGraphics.drawString(this.font, questName, panelX + 5, contentY, 0xFFFFFFFF, false);
        contentY += this.font.lineHeight + 3;

        String description = ClientQuestData.getActiveQuestDescription();
        List<String> wrappedDesc = wrapText(description, QUESTS_PANEL_WIDTH - 10);
        for (String line : wrappedDesc) {
            guiGraphics.drawString(this.font, line, panelX + 5, contentY, TEXT_BODY, false);
            contentY += this.font.lineHeight;
        }
        contentY += 3;

        // Progress as a tube gauge
        float progress = ClientQuestData.getActiveQuestProgress();
        int tubeX = panelX + (QUESTS_PANEL_WIDTH - TUBE_W) / 2;
        renderTube(guiGraphics, tubeX, contentY, progress, 0xFF4DB0FF, 0xFF1976D2, animTicks(0f));

        Component progressText = Component.literal((int) (progress * 100) + "%");
        int progressTextWidth = this.font.width(progressText);
        guiGraphics.drawString(this.font, progressText,
                tubeX + TUBE_LIQUID_X + (TUBE_LIQUID_W - progressTextWidth) / 2, contentY + 4, 0xFFFFFFFF, true);

        contentY += TUBE_H + 5;

        Component rewardsLabel = Component.literal("Rewards:").withStyle(ChatFormatting.BOLD);
        guiGraphics.drawString(this.font, rewardsLabel, panelX + 5, contentY, TEXT_HEADING, false);
        contentY += this.font.lineHeight + 3;

        List<ItemStack> rewards = ClientQuestData.getActiveQuestRewards();
        int rewardX = panelX + 5;
        int rewardY = contentY;
        questRewardX = rewardX;
        questRewardY = rewardY;

        for (int i = 0; i < rewards.size() && i < 8; i++) {
            ItemStack reward = rewards.get(i);
            int x = rewardX + (i % 4) * (QUEST_ITEM_SIZE + 6);
            int y = rewardY + (i / 4) * (QUEST_ITEM_SIZE + 6);
            drawSlot(guiGraphics, x - 1, y - 1, false, false, false);
            guiGraphics.renderItem(reward, x, y);
            guiGraphics.renderItemDecorations(this.font, reward, x, y);
        }

        contentY += (((Math.max(rewards.size(), 1) - 1) / 4) + 1) * (QUEST_ITEM_SIZE + 6) + 2;

        int digestion = ClientQuestData.getActiveQuestDigestionReward();
        if (digestion > 0) {
            Component digestionText = Component.literal("+" + digestion + " Digestion").withStyle(ChatFormatting.GOLD);
            guiGraphics.drawString(this.font, digestionText, panelX + 5, contentY, 0xFFFFAA00, false);
        }
    }

    private void renderQuestItemTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!ClientQuestData.hasActiveQuest()) return;

        List<ItemStack> rewards = ClientQuestData.getActiveQuestRewards();

        for (int i = 0; i < rewards.size() && i < 8; i++) {
            ItemStack reward = rewards.get(i);
            int x = questRewardX + (i % 4) * (QUEST_ITEM_SIZE + 6);
            int y = questRewardY + (i / 4) * (QUEST_ITEM_SIZE + 6);

            if (mouseX >= x && mouseX < x + QUEST_ITEM_SIZE && mouseY >= y && mouseY < y + QUEST_ITEM_SIZE) {
                guiGraphics.renderTooltip(this.font, reward, mouseX, mouseY);
                return;
            }
        }
    }

    // -------------------------------------------------------------- Ability tooltips

    private void renderAbilityTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int baseLeftPos = this.leftPos;
        int panelX = baseLeftPos + this.imageWidth + 5;
        int panelY = this.topPos + 15;
        int slotY = panelY + ABILITIES_PANEL_HEIGHT + 5;

        Ability hoveredAbility = null;
        int hoveredSubIndex = -1;

        // On copied tabs, the upper panel shows the copied list, not the normal abilities list
        if (!isCopiedTab(currentTab)) {
            int availIdx = getAbilityIndexAt(mouseX, mouseY, panelX, panelY);
            if (availIdx >= 0) {
                hoveredAbility = availableAbilities.get(availIdx);
                hoveredSubIndex = (subAbilityEntries.size() > availIdx && subAbilityEntries.get(availIdx) != null)
                        ? subAbilityEntries.get(availIdx).subIndex() : -1;
            }
        } else {
            // Hover over the copied-abilities list
            int copiedIdx = getCopiedAbilityIndexAt(mouseX, mouseY, panelX, panelY);
            if (copiedIdx >= 0) {
                List<String> ids = AbilityWheelClientData.getCopiedAbilityIds();
                if (copiedIdx < ids.size()) {
                    AbilityId parsed = AbilityId.parse(ids.get(copiedIdx));
                    hoveredAbility = LOTMCraft.abilityHandler.getById(parsed.baseId());
                    hoveredSubIndex = parsed.subIndex();
                }
            }
        }

        if (hoveredAbility == null) {
            if (currentTab == Tab.ABILITY_WHEEL) {
                int wheelSlot = getAbilityWheelSlot(mouseX, mouseY, panelX, slotY);
                if (wheelSlot >= 0 && wheelSlot < abilityWheelSlots.size()) {
                    hoveredAbility = abilityWheelSlots.get(wheelSlot);
                    hoveredSubIndex = abilityWheelSubIndexes.get(wheelSlot);
                }
            } else if (currentTab == Tab.SHARED_ABILITIES && ClientTeamData.hasTeam()) {
                int iconsPerRow2 = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
                List<String> allPooledTooltip = getAllPooledAbilities();
                for (int s = 0; s < allPooledTooltip.size(); s++) {
                    int sRow = s / iconsPerRow2 - sharedPoolScrollOffset;
                    int sx = panelX + 5 + (s % iconsPerRow2) * (ABILITY_ICON_SIZE + 2);
                    int sy = slotY + 14 + sRow * (ABILITY_ICON_SIZE + 2);
                    if (sy < slotY + 14 || sy + ABILITY_ICON_SIZE > slotY + SHARED_POOL_HEIGHT) continue;
                    if (mouseX >= sx && mouseX <= sx + ABILITY_ICON_SIZE && mouseY >= sy && mouseY <= sy + ABILITY_ICON_SIZE) {
                        hoveredAbility = LOTMCraft.abilityHandler.getById(allPooledTooltip.get(s));
                        break;
                    }
                }
                if (hoveredAbility == null) {
                    int wheelY2 = slotY + SHARED_POOL_HEIGHT + 5;
                    int iconsPerRowW2 = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
                    for (int i = 0; i < sharedWheelSlots.size(); i++) {
                        int wx = panelX + 5 + (i % iconsPerRowW2) * (ABILITY_ICON_SIZE + 2);
                        int wy = wheelY2 + 14 + (i / iconsPerRowW2) * (ABILITY_ICON_SIZE + 2);
                        if (mouseX >= wx && mouseX <= wx + ABILITY_ICON_SIZE && mouseY >= wy && mouseY <= wy + ABILITY_ICON_SIZE) {
                            hoveredAbility = LOTMCraft.abilityHandler.getById(sharedWheelSlots.get(i));
                            break;
                        }
                    }
                }
            } else if (currentTab == Tab.ABILITY_BAR) {
                int barSlot = getAbilityBarSlot(mouseX, mouseY, panelX, slotY);
                if (barSlot >= 0 && barSlot < abilityBarSlots.size()) {
                    hoveredAbility = abilityBarSlots.get(barSlot);
                    hoveredSubIndex = abilityBarSubIndexes.get(barSlot);
                }
            } else if (isCopiedTab(currentTab)) {
                // Hover over wheel drop zone (reused for copied tabs)
                int wheelSlot = getAbilityWheelSlot(mouseX, mouseY, panelX, slotY);
                if (wheelSlot >= 0 && wheelSlot < abilityWheelSlots.size()) {
                    hoveredAbility = abilityWheelSlots.get(wheelSlot);
                    hoveredSubIndex = abilityWheelSubIndexes.get(wheelSlot);
                }
                int barSlot = getAbilityBarSlot(mouseX, mouseY, panelX, slotY + ABILITY_WHEEL_HEIGHT + 5);
                if (barSlot >= 0 && barSlot < abilityBarSlots.size()) {
                    hoveredAbility = abilityBarSlots.get(barSlot);
                    hoveredSubIndex = abilityBarSubIndexes.get(barSlot);
                }
            }
        }

        if (hoveredAbility != null) {
            List<Component> tooltipLines = new ArrayList<>();

            if (hoveredSubIndex >= 0 && hoveredAbility instanceof SelectableAbility sa) {
                String[] names = sa.getAbilityNamesCopy();
                if (hoveredSubIndex < names.length) {
                    int color = showAllAbilities ? 0xFFFFFF : BeyonderData.pathwayInfos.get(menu.getPathway()).color();
                    tooltipLines.add(Component.translatable(names[hoveredSubIndex]).withStyle(ChatFormatting.BOLD).withColor(color));
                    tooltipLines.add(Component.literal("(" + hoveredAbility.getNameFormatted(ClientHandler.getPlayer()).getString() + ")").withStyle(ChatFormatting.DARK_GRAY));
                } else {
                    tooltipLines.add(hoveredAbility.getNameFormatted(ClientHandler.getPlayer()));
                }
            } else if (showAllAbilities) {
                tooltipLines.add(hoveredAbility.getNameFormatted(ClientHandler.getPlayer()));
            } else {
                int color = BeyonderData.pathwayInfos.get(menu.getPathway()).color();
                tooltipLines.add(hoveredAbility.getName().withStyle(ChatFormatting.BOLD).withColor(color));
            }

            Component description = hoveredAbility.getDescription();
            if (description != null) {
                String descText = description.getString();
                List<String> wrappedLines = wrapText(descText, 100);
                for (String line : wrappedLines) {
                    tooltipLines.add(Component.literal(line).withStyle(ChatFormatting.DARK_GRAY));
                }
                tooltipLines.add(Component.literal(""));
            }

            int cooldown = hoveredAbility.getCooldown();
            if (cooldown > 0) {
                tooltipLines.add(Component.literal("Cooldown: ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(cooldown / 20 + "s").withStyle(ChatFormatting.BLUE)));
            }

            float spiritualityCost = hoveredAbility.spiritualityCost();
            if (spiritualityCost > 0) {
                tooltipLines.add(Component.literal("Spirituality Cost: ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(spiritualityCost + "").withStyle(ChatFormatting.DARK_PURPLE)));
            }

            guiGraphics.renderTooltip(this.font, tooltipLines, java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            int lineWidth = this.font.width(testLine);

            if (lineWidth > maxWidth && !currentLine.isEmpty()) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (!currentLine.isEmpty()) currentLine.append(" ");
                currentLine.append(word);
            }
        }

        if (!currentLine.isEmpty()) lines.add(currentLine.toString());
        return lines;
    }

    // -------------------------------------------------------------- Abilities drawer

    private void renderAbilitiesPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int baseLeftPos = this.leftPos;
        int panelX = baseLeftPos + this.imageWidth + 5;
        int panelY = this.topPos + 15;

        if (isCopiedTab(currentTab)) {
            renderCopiedAbilitiesPanel(guiGraphics, panelX, panelY, mouseX, mouseY);
            return;
        }

        drawDrawer(guiGraphics, panelX, panelY, ABILITIES_PANEL_WIDTH, ABILITIES_PANEL_HEIGHT);
        heading(guiGraphics, Component.literal("Abilities"), panelX + 5, panelY + 5);

        int hoverIdx = draggedAbility == null ? getAbilityIndexAt(mouseX, mouseY, panelX, panelY) : -1;
        renderAvailableAbilities(guiGraphics, panelX, panelY + 15, hoverIdx);

        int slotY = panelY + ABILITIES_PANEL_HEIGHT + 5;

        if (currentTab == Tab.ABILITY_WHEEL) {
            renderAbilityWheelSection(guiGraphics, panelX, slotY, mouseX, mouseY);
        } else if (currentTab == Tab.ABILITY_BAR) {
            renderAbilityBarSection(guiGraphics, panelX, slotY, mouseX, mouseY);
        } else if (currentTab == Tab.SHARED_ABILITIES) {
            renderSharedAbilitiesTab(guiGraphics, panelX, slotY);
        }
    }


    private void renderCopiedAbilitiesPanel(GuiGraphics guiGraphics, int panelX, int panelY, int mouseX, int mouseY) {
        boolean isRecorded = currentTab == Tab.RECORDED_ABILITIES;
        List<String> ids = AbilityWheelClientData.getCopiedAbilityIds();
        List<Integer> remainingUses = AbilityWheelClientData.getCopiedAbilityRemainingUses();

        String tabLabel = "Copied";

        // Upper panel – copied ability pool
        drawDrawer(guiGraphics, panelX, panelY, ABILITIES_PANEL_WIDTH, COPIED_PANEL_HEIGHT);
        guiGraphics.drawString(this.font,
                Component.literal(tabLabel).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.LIGHT_PURPLE),
                panelX + 5, panelY + 5, 0xFFFFFFFF, true);

        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int clipTop = panelY + 14;
        int clipBottom = panelY + COPIED_PANEL_HEIGHT - 2;

        for (int listIdx = 0; listIdx < ids.size(); listIdx++) {
            int row = listIdx / iconsPerRow - copiedScrollOffset;
            int col = listIdx % iconsPerRow;
            int ix = panelX + 5 + col * (ABILITY_ICON_SIZE + 2);
            int iy = panelY + 14 + row * (ABILITY_ICON_SIZE + 2);
            if (iy < clipTop || iy + ABILITY_ICON_SIZE > clipBottom) continue;
            if (draggedFromCopiedIndex == listIdx) continue; // being dragged

            AbilityId parsed = AbilityId.parse(ids.get(listIdx));
            Ability ability = LOTMCraft.abilityHandler.getById(parsed.baseId());
            if (ability != null) {
                boolean hover = draggedAbility == null
                        && mouseX >= ix && mouseX < ix + ABILITY_ICON_SIZE && mouseY >= iy && mouseY < iy + ABILITY_ICON_SIZE;
                drawSlot(guiGraphics, ix - 1, iy - 1, hover, false, true);
                int uses = listIdx < remainingUses.size() ? remainingUses.get(listIdx) : -1;
                renderCopiedAbilityIcon(guiGraphics, ability, ix, iy, isRecorded, uses);
            }
        }

        if (ids.isEmpty()) {
            Component empty = Component.literal("No " + tabLabel.toLowerCase() + " abilities")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            int w = this.font.width(empty);
            guiGraphics.drawString(this.font, empty,
                    panelX + (ABILITIES_PANEL_WIDTH - w) / 2,
                    panelY + COPIED_PANEL_HEIGHT / 2 - this.font.lineHeight / 2,
                    TEXT_DIM, false);
        }

        if (maxCopiedScroll > 0) {
            Component scrollHint = Component.literal("(Scroll)").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            int hintWidth = this.font.width(scrollHint);
            guiGraphics.drawString(this.font, scrollHint,
                    panelX + ABILITIES_PANEL_WIDTH - hintWidth - 5,
                    panelY + COPIED_PANEL_HEIGHT - 12, TEXT_DIM, false);
        }

        int slotY = panelY + COPIED_PANEL_HEIGHT + 5;
        renderAbilityWheelSection(guiGraphics, panelX, slotY, mouseX, mouseY);
    }

    /**
     * Renders a single copied-ability icon.
     * Recorded abilities show remaining uses (bottom-left badge).
     */
    private void renderCopiedAbilityIcon(GuiGraphics guiGraphics, Ability ability, int x, int y,
                                         boolean isRecorded, int remainingUses) {
        // Draw the icon
        if (ability.getTextureLocation() != null) {
            guiGraphics.blit(ability.getTextureLocation(), x, y, 0, 0,
                    ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE);
        } else {
            guiGraphics.fill(x, y, x + ABILITY_ICON_SIZE, y + ABILITY_ICON_SIZE, 0xFFFFFFFF);
        }

        // Recorded: show remaining-uses badge (bottom-left)
        if (isRecorded && remainingUses >= 0) {
            String badge = String.valueOf(remainingUses);
            int bx = x + 1;
            int by = y + ABILITY_ICON_SIZE - font.lineHeight + 1;
            guiGraphics.fill(bx - 1, by - 1, bx + font.width(badge) + 1, y + ABILITY_ICON_SIZE, 0x99000000);
            guiGraphics.drawString(font, badge, bx, by, 0xFFFFFF, false);
        }
    }

    // -----------------------------------------------------------------------

    private void renderAvailableAbilities(GuiGraphics guiGraphics, int panelX, int panelY, int hoverIdx) {
        int startX = panelX + 5;
        int startY = panelY - abilitiesScrollOffset * (ABILITY_ICON_SIZE + 2);

        List<Ability> displayedAbilities = currentTab == Tab.SHARED_ABILITIES
                ? buildDisplayedSharedAbilities()
                : availableAbilities;

        List<SubAbilityEntry> displayedEntries = currentTab == Tab.SHARED_ABILITIES
                ? buildDisplayedSharedEntries()
                : subAbilityEntries;

        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);

        for (int i = 0; i < displayedAbilities.size(); i++) {
            int row = i / iconsPerRow;
            int col = i % iconsPerRow;

            int x = startX + col * (ABILITY_ICON_SIZE + 2);
            int y = startY + row * (ABILITY_ICON_SIZE + 2);

            if (y >= panelY && y + ABILITY_ICON_SIZE <= panelY + ABILITIES_PANEL_HEIGHT - 15) {
                Ability ability = displayedAbilities.get(i);
                int si = (displayedEntries.size() > i && displayedEntries.get(i) != null)
                        ? displayedEntries.get(i).subIndex() : -1;
                drawSlot(guiGraphics, x - 1, y - 1, i == hoverIdx, false, false);
                renderAbilityIcon(guiGraphics, ability, x, y, si);
            }
        }
    }

    private List<Ability> buildDisplayedSharedAbilities() {
        List<Ability> result = new ArrayList<>();
        for (int i = 0; i < availableAbilities.size(); i++) {
            Ability a = availableAbilities.get(i);
            SubAbilityEntry e = subAbilityEntries.size() > i ? subAbilityEntries.get(i) : null;
            if (e != null || a.canBeShared) result.add(a);
        }
        return result;
    }

    private List<SubAbilityEntry> buildDisplayedSharedEntries() {
        List<SubAbilityEntry> result = new ArrayList<>();
        for (int i = 0; i < availableAbilities.size(); i++) {
            Ability a = availableAbilities.get(i);
            SubAbilityEntry e = subAbilityEntries.size() > i ? subAbilityEntries.get(i) : null;
            if (e != null || a.canBeShared) result.add(e);
        }
        return result;
    }

    private void renderAbilityWheelSection(GuiGraphics guiGraphics, int panelX, int wheelY, int mouseX, int mouseY) {
        drawDrawer(guiGraphics, panelX, wheelY, ABILITIES_PANEL_WIDTH, ABILITY_WHEEL_HEIGHT);
        heading(guiGraphics, Component.literal("Ability Wheel"), panelX + 5, wheelY + 5);

        renderAbilityWheel(guiGraphics, panelX, wheelY + 15, mouseX, mouseY);
    }

    private void renderAbilityBarSection(GuiGraphics guiGraphics, int panelX, int barY, int mouseX, int mouseY) {
        drawDrawer(guiGraphics, panelX, barY, ABILITIES_PANEL_WIDTH, ABILITY_BAR_HEIGHT);
        heading(guiGraphics, Component.literal("Ability Bar"), panelX + 5, barY + 5);

        renderAbilityBar(guiGraphics, panelX, barY, mouseX, mouseY);
    }

    private void renderAbilityWheel(GuiGraphics guiGraphics, int panelX, int panelY, int mouseX, int mouseY) {
        int startX = panelX + 5;
        int startY = panelY;
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);

        for (int i = 0; i < ABILITY_WHEEL_MAX; i++) {
            int row = i / iconsPerRow;
            int col = i % iconsPerRow;
            int x = startX + col * (ABILITY_ICON_SIZE + 2);
            int y = startY + row * (ABILITY_ICON_SIZE + 2);

            boolean over = mouseX >= x && mouseX < x + ABILITY_ICON_SIZE && mouseY >= y && mouseY < y + ABILITY_ICON_SIZE;
            boolean copied = i < abilityWheelSlots.size() && i < abilityWheelIsCopied.size() && abilityWheelIsCopied.get(i);
            drawSlot(guiGraphics, x - 1, y - 1, over && draggedAbility == null, over && draggedAbility != null, copied);

            if (i < abilityWheelSlots.size() && draggedFromWheelIndex != i) {
                int si = abilityWheelSubIndexes.get(i);
                renderAbilityIcon(guiGraphics, abilityWheelSlots.get(i), x, y, si);
            }
        }
    }

    // Bar layout helpers: 2 rows x 5 columns
    private int barIconX(int slot, int panelX) {
        return panelX + 5 + (slot % BAR_COLS) * BAR_CELL_W + (BAR_CELL_W - ABILITY_ICON_SIZE) / 2;
    }

    private int barIconY(int slot, int barY) {
        return barY + 15 + (slot / BAR_COLS) * BAR_CELL_H;
    }

    private void renderAbilityBar(GuiGraphics guiGraphics, int panelX, int barY, int mouseX, int mouseY) {
        for (int i = 0; i < ABILITY_BAR_MAX; i++) {
            int x = barIconX(i, panelX);
            int y = barIconY(i, barY);

            boolean over = mouseX >= x && mouseX < x + ABILITY_ICON_SIZE && mouseY >= y && mouseY < y + ABILITY_ICON_SIZE;
            boolean copied = i < abilityBarSlots.size() && i < abilityBarIsCopied.size() && abilityBarIsCopied.get(i);
            drawSlot(guiGraphics, x - 1, y - 1, over && draggedAbility == null, over && draggedAbility != null, copied);

            if (i < abilityBarSlots.size() && draggedFromBarIndex != i) {
                int si = abilityBarSubIndexes.get(i);
                renderAbilityIcon(guiGraphics, abilityBarSlots.get(i), x, y, si);
            }

            String keybind = abbreviateKeybind(KEYBIND_LABELS[i]);
            Component keybindText = Component.literal(keybind).withStyle(ChatFormatting.GRAY);
            int textWidth = this.font.width(keybindText);
            int textX = x + (ABILITY_ICON_SIZE - textWidth) / 2;
            int textY = y + ABILITY_ICON_SIZE + 3;
            guiGraphics.drawString(this.font, keybindText, textX, textY, 0xFFAAAAAA, false);
        }
    }

    private void renderSharedAbilitiesTab(GuiGraphics guiGraphics, int panelX, int sectionY) {
        String myUUID = this.minecraft.player.getStringUUID();
        List<String> myContributions = new ArrayList<>(ClientTeamData.getContributionsFor(myUUID));
        int maxSlots = ClientTeamData.getSlotsPerMember();
        List<String> allPooled = getAllPooledAbilities();

        drawDrawer(guiGraphics, panelX, sectionY, ABILITIES_PANEL_WIDTH, SHARED_POOL_HEIGHT);
        guiGraphics.drawString(this.font, Component.literal("Sharing").withStyle(ChatFormatting.BOLD), panelX + 5, sectionY + 3, TEXT_HEADING, true);

        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);

        int slotsToShow = allPooled.size();
        int myFilledCount = myContributions.size();
        int emptyOwnSlots = Math.max(0, maxSlots - myFilledCount);
        int totalSlots = slotsToShow + emptyOwnSlots;
        int totalRows = (int) Math.ceil((double) totalSlots / iconsPerRow);
        int visibleRows = (SHARED_POOL_HEIGHT - 14) / (ABILITY_ICON_SIZE + 2);
        maxSharedPoolScroll = Math.max(0, totalRows - visibleRows);

        for (int i = 0; i < slotsToShow; i++) {
            String abilityId = allPooled.get(i);
            int row = i / iconsPerRow - sharedPoolScrollOffset;
            int col = i % iconsPerRow;
            int ix = panelX + 5 + col * (ABILITY_ICON_SIZE + 2);
            int iy = sectionY + 14 + row * (ABILITY_ICON_SIZE + 2);
            if (iy < sectionY + 14 || iy + ABILITY_ICON_SIZE > sectionY + SHARED_POOL_HEIGHT) continue;

            // Own contributions use the plain slot, other members' abilities get the brass-rimmed one
            boolean isMine = myContributions.contains(abilityId);
            drawSlot(guiGraphics, ix - 1, iy - 1, !isMine, false, false);

            Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
            if (ability != null && draggedFromSharedPoolIndex != i) {
                renderAbilityIcon(guiGraphics, ability, ix, iy, -1);
            }
        }

        for (int e = 0; e < emptyOwnSlots; e++) {
            int i = slotsToShow + e;
            int row = i / iconsPerRow - sharedPoolScrollOffset;
            int col = i % iconsPerRow;
            int ix = panelX + 5 + col * (ABILITY_ICON_SIZE + 2);
            int iy = sectionY + 14 + row * (ABILITY_ICON_SIZE + 2);
            if (iy < sectionY + 14 || iy + ABILITY_ICON_SIZE > sectionY + SHARED_POOL_HEIGHT) continue;
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            guiGraphics.blitSprite(SPR_SLOT_EMPTY, ix - 1, iy - 1, 18, 18);
        }

        int wheelY = sectionY + SHARED_POOL_HEIGHT + 5;
        drawDrawer(guiGraphics, panelX, wheelY, ABILITIES_PANEL_WIDTH, SHARED_WHEEL_HEIGHT);
        guiGraphics.drawString(this.font, Component.literal("Shared Wheel").withStyle(ChatFormatting.BOLD), panelX + 5, wheelY + 3, TEXT_HEADING, true);

        int startX = panelX + 5;
        int startY = wheelY + 14;
        int iconsPerRowW = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int maxWheelSlots = iconsPerRowW * 2;

        if (allPooled.isEmpty()) {
            Component empty = Component.literal("No shared abilities").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            int w = this.font.width(empty);
            guiGraphics.drawString(this.font, empty, panelX + (ABILITIES_PANEL_WIDTH - w) / 2, wheelY + 28, TEXT_DIM, false);
        } else {
            for (int i = 0; i < maxWheelSlots; i++) {
                int wx = startX + (i % iconsPerRowW) * (ABILITY_ICON_SIZE + 2);
                int wy = startY + (i / iconsPerRowW) * (ABILITY_ICON_SIZE + 2);
                drawSlot(guiGraphics, wx - 1, wy - 1, false, false, false);
                if (i < sharedWheelSlots.size() && draggedFromSharedWheelIndex != i) {
                    Ability ability = LOTMCraft.abilityHandler.getById(sharedWheelSlots.get(i));
                    if (ability != null) {
                        renderAbilityIcon(guiGraphics, ability, wx, wy, -1);
                    }
                }
            }
        }
    }

    private List<String> getAllPooledAbilities() {
        List<String> result = new ArrayList<>();
        String leaderUUID = ClientTeamData.getLeaderUUID();
        if (!leaderUUID.isEmpty()) {
            for (String id : ClientTeamData.getContributionsFor(leaderUUID)) {
                if (!result.contains(id)) result.add(id);
            }
        }
        for (String memberUUID : ClientTeamData.getMemberUUIDs()) {
            for (String id : ClientTeamData.getContributionsFor(memberUUID)) {
                if (!result.contains(id)) result.add(id);
            }
        }
        String myUUID = this.minecraft.player.getStringUUID();
        for (String id : ClientTeamData.getContributionsFor(myUUID)) {
            if (!result.contains(id)) result.add(id);
        }
        return result;
    }


    private void renderAbilityIcon(GuiGraphics guiGraphics, Ability ability, int x, int y, int subIndex) {
        if (ability.getTextureLocation() != null) {
            guiGraphics.blit(ability.getTextureLocation(), x, y, 0, 0, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE);
        } else {
            guiGraphics.fill(x, y, x + ABILITY_ICON_SIZE, y + ABILITY_ICON_SIZE, 0xFFFFFFFF);
            guiGraphics.renderOutline(x, y, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, 0xFF000000);
        }
        if (subIndex >= 0) {
            String badge = String.valueOf(subIndex);
            int bx = x + ABILITY_ICON_SIZE - font.width(badge) - 1;
            int by = y + ABILITY_ICON_SIZE - font.lineHeight + 1;
            guiGraphics.fill(bx - 1, by - 1, x + ABILITY_ICON_SIZE, y + ABILITY_ICON_SIZE, 0xCC0F0E13);
            guiGraphics.drawString(font, badge, bx, by, 0xF4D58A, false);
        }
    }

    /** Draws only the passive icon; hover + tooltip are handled by the passive section. */
    private void renderPassiveAbilityIcon(GuiGraphics guiGraphics, PassiveAbility ability, int x, int y) {
        if (ability.getTexture() != null) {
            guiGraphics.blit(ability.getTexture(), x, y, 0, 0, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE);
        } else {
            guiGraphics.fill(x, y, x + ABILITY_ICON_SIZE, y + ABILITY_ICON_SIZE, 0xFFFFFFFF);
            guiGraphics.renderOutline(x, y, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, 0xFF000000);
        }
    }

    private int getCopiedAbilityIndexAt(int mouseX, int mouseY, int panelX, int panelY) {
        List<String> ids = AbilityWheelClientData.getCopiedAbilityIds();
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int clipTop = panelY + 14;
        int clipBottom = panelY + COPIED_PANEL_HEIGHT - 2;

        for (int listIdx = 0; listIdx < ids.size(); listIdx++) {
            int row = listIdx / iconsPerRow - copiedScrollOffset;
            int col = listIdx % iconsPerRow;
            int ix = panelX + 5 + col * (ABILITY_ICON_SIZE + 2);
            int iy = panelY + 14 + row * (ABILITY_ICON_SIZE + 2);
            if (iy < clipTop || iy + ABILITY_ICON_SIZE > clipBottom) continue;
            if (mouseX >= ix && mouseX <= ix + ABILITY_ICON_SIZE
                    && mouseY >= iy && mouseY <= iy + ABILITY_ICON_SIZE) {
                return listIdx;
            }
        }
        return -1;
    }

    private boolean handleSharedTabClick(int mouseX, int mouseY, int panelX, int sectionY) {
        if (!ClientTeamData.hasTeam()) return false;

        String myUUID = this.minecraft.player.getStringUUID();
        List<String> myContributions = new ArrayList<>(ClientTeamData.getContributionsFor(myUUID));
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);

        List<String> allPooled = getAllPooledAbilities();
        for (int i = 0; i < allPooled.size(); i++) {
            int row = i / iconsPerRow - sharedPoolScrollOffset;
            int ix = panelX + 5 + (i % iconsPerRow) * (ABILITY_ICON_SIZE + 2);
            int iy = sectionY + 14 + row * (ABILITY_ICON_SIZE + 2);
            if (iy < sectionY + 14 || iy + ABILITY_ICON_SIZE > sectionY + SHARED_POOL_HEIGHT) continue;
            if (mouseX >= ix && mouseX <= ix + ABILITY_ICON_SIZE && mouseY >= iy && mouseY <= iy + ABILITY_ICON_SIZE) {
                Ability ability = LOTMCraft.abilityHandler.getById(allPooled.get(i));
                if (ability == null) return false;
                draggedAbility = ability;
                draggedSubIndex = -1;
                draggedFromSharedPoolIndex = myContributions.contains(allPooled.get(i)) ? i : -1;
                draggedFromSharedWheelIndex = -1;
                draggedFromWheelIndex = -1;
                draggedFromBarIndex = -1;
                draggedFromAvailable = false;
                draggedFromCopiedIndex = -1;
                dragOffsetX = mouseX - ix;
                dragOffsetY = mouseY - iy;
                return true;
            }
        }

        int wheelY = sectionY + SHARED_POOL_HEIGHT + 5;
        int startX = panelX + 5;
        int startY = wheelY + 14;
        int iconsPerRowW = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int maxWheelSlots = iconsPerRowW * 2;

        for (int i = 0; i < maxWheelSlots; i++) {
            int wx = startX + (i % iconsPerRowW) * (ABILITY_ICON_SIZE + 2);
            int wy = startY + (i / iconsPerRowW) * (ABILITY_ICON_SIZE + 2);
            if (mouseX >= wx && mouseX <= wx + ABILITY_ICON_SIZE && mouseY >= wy && mouseY <= wy + ABILITY_ICON_SIZE) {
                if (i < sharedWheelSlots.size()) {
                    draggedAbility = LOTMCraft.abilityHandler.getById(sharedWheelSlots.get(i));
                    draggedSubIndex = -1;
                    draggedFromSharedWheelIndex = i;
                    draggedFromSharedPoolIndex = -1;
                    draggedFromWheelIndex = -1;
                    draggedFromBarIndex = -1;
                    draggedFromAvailable = false;
                    draggedFromCopiedIndex = -1;
                    dragOffsetX = mouseX - wx;
                    dragOffsetY = mouseY - wy;
                    return true;
                }
                return false;
            }
        }

        return false;
    }

    // =====================================================================================
    //  Input
    // =====================================================================================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && showAbilities) {
            int baseLeftPos = this.leftPos;
            int panelX = baseLeftPos + this.imageWidth + 5;
            int panelY = this.topPos + 15;
            int slotY = panelY + ABILITIES_PANEL_HEIGHT + 5;

            if (isCopiedTab(currentTab)) {
                int copiedListIdx = getCopiedAbilityIndexAt((int) mouseX, (int) mouseY, panelX, panelY);
                if (copiedListIdx >= 0) {
                    List<String> ids = AbilityWheelClientData.getCopiedAbilityIds();
                    if (copiedListIdx < ids.size()) {
                        AbilityId parsed = AbilityId.parse(ids.get(copiedListIdx));
                        Ability ability = LOTMCraft.abilityHandler.getById(parsed.baseId());
                        if (ability != null) {
                            draggedAbility = ability;
                            draggedSubIndex = parsed.subIndex();
                            draggedFromCopiedIndex = copiedListIdx;
                            draggedFromWheelIndex = -1;
                            draggedFromBarIndex = -1;
                            draggedFromAvailable = false;
                            draggedFromSharedPoolIndex = -1;
                            draggedFromSharedWheelIndex = -1;
                            // Compute icon position for offset
                            int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
                            int row = copiedListIdx / iconsPerRow - copiedScrollOffset;
                            int col = copiedListIdx % iconsPerRow;
                            int ix = panelX + 5 + col * (ABILITY_ICON_SIZE + 2);
                            int iy = panelY + 14 + row * (ABILITY_ICON_SIZE + 2);
                            dragOffsetX = (int) mouseX - ix;
                            dragOffsetY = (int) mouseY - iy;
                            return true;
                        }
                    }
                }

                int copiedWheelSlotY = panelY + COPIED_PANEL_HEIGHT + 5;
                int wheelSlot = getAbilityWheelSlot((int) mouseX, (int) mouseY, panelX, copiedWheelSlotY);
                if (wheelSlot >= 0 && wheelSlot < abilityWheelSlots.size()) {
                    draggedAbility = abilityWheelSlots.get(wheelSlot);
                    draggedSubIndex = abilityWheelSubIndexes.get(wheelSlot);
                    draggedFromWheelIndex = wheelSlot;
                    draggedFromBarIndex = -1;
                    draggedFromAvailable = false;
                    draggedFromCopiedIndex = -1;
                    dragOffsetX = (int) mouseX - getWheelSlotX(wheelSlot, panelX, copiedWheelSlotY);
                    dragOffsetY = (int) mouseY - getWheelSlotY(wheelSlot, panelX, copiedWheelSlotY);
                    return true;
                }

                int copiedBarSlotY = panelY + COPIED_PANEL_HEIGHT + 5 + ABILITY_WHEEL_HEIGHT + 5;
                int barSlot = getAbilityBarSlot((int) mouseX, (int) mouseY, panelX, copiedBarSlotY);
                if (barSlot >= 0 && barSlot < abilityBarSlots.size()) {
                    draggedAbility = abilityBarSlots.get(barSlot);
                    draggedSubIndex = abilityBarSubIndexes.get(barSlot);
                    draggedFromBarIndex = barSlot;
                    draggedFromWheelIndex = -1;
                    draggedFromAvailable = false;
                    draggedFromCopiedIndex = -1;
                    dragOffsetX = (int) mouseX - getBarSlotX(barSlot, panelX, copiedBarSlotY);
                    dragOffsetY = (int) mouseY - getBarSlotY(barSlot, panelX, copiedBarSlotY);
                    return true;
                }

                return super.mouseClicked(mouseX, mouseY, button);
            }

            // ---- Normal tabs ----
            if (currentTab == Tab.SHARED_ABILITIES) {
                if (handleSharedTabClick((int) mouseX, (int) mouseY, panelX, slotY)) return true;
            }

            if (currentTab == Tab.ABILITY_WHEEL) {
                int wheelSlot = getAbilityWheelSlot((int) mouseX, (int) mouseY, panelX, slotY);
                if (wheelSlot >= 0 && wheelSlot < abilityWheelSlots.size()) {
                    draggedAbility = abilityWheelSlots.get(wheelSlot);
                    draggedSubIndex = abilityWheelSubIndexes.get(wheelSlot);
                    draggedFromWheelIndex = wheelSlot;
                    draggedFromBarIndex = -1;
                    draggedFromAvailable = false;
                    draggedFromCopiedIndex = -1;
                    dragOffsetX = (int) mouseX - getWheelSlotX(wheelSlot, panelX, slotY);
                    dragOffsetY = (int) mouseY - getWheelSlotY(wheelSlot, panelX, slotY);
                    return true;
                }
            } else if (currentTab == Tab.ABILITY_BAR) {
                int barSlot = getAbilityBarSlot((int) mouseX, (int) mouseY, panelX, slotY);
                if (barSlot >= 0 && barSlot < abilityBarSlots.size()) {
                    draggedAbility = abilityBarSlots.get(barSlot);
                    draggedSubIndex = abilityBarSubIndexes.get(barSlot);
                    draggedFromBarIndex = barSlot;
                    draggedFromWheelIndex = -1;
                    draggedFromAvailable = false;
                    draggedFromCopiedIndex = -1;
                    dragOffsetX = (int) mouseX - getBarSlotX(barSlot, panelX, slotY);
                    dragOffsetY = (int) mouseY - getBarSlotY(barSlot, panelX, slotY);
                    return true;
                }
            }

            int availIdx = getAbilityIndexAt((int) mouseX, (int) mouseY, panelX, panelY);
            if (availIdx >= 0) {
                List<Ability> clickableAbilities = currentTab == Tab.SHARED_ABILITIES
                        ? buildDisplayedSharedAbilities() : availableAbilities;
                List<SubAbilityEntry> clickableEntries = currentTab == Tab.SHARED_ABILITIES
                        ? buildDisplayedSharedEntries() : subAbilityEntries;

                draggedAbility = clickableAbilities.get(availIdx);
                draggedSubIndex = (clickableEntries.size() > availIdx && clickableEntries.get(availIdx) != null)
                        ? clickableEntries.get(availIdx).subIndex() : -1;
                draggedFromWheelIndex = -1;
                draggedFromBarIndex = -1;
                draggedFromAvailable = true;
                draggedFromCopiedIndex = -1;
                dragOffsetX = (int) mouseX - getAbilityXByIndex(availIdx, panelX);
                dragOffsetY = (int) mouseY - getAbilityYByIndex(availIdx, panelY);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggedAbility != null) {
            int baseLeftPos = this.leftPos;
            int panelX = baseLeftPos + this.imageWidth + 5;
            int panelY = this.topPos + 15;
            int slotY = panelY + ABILITIES_PANEL_HEIGHT + 5;

            if (isCopiedTab(currentTab) || draggedFromCopiedIndex >= 0) {
                int copiedWheelSlotY = panelY + COPIED_PANEL_HEIGHT + 5;
                int copiedBarSlotY = copiedWheelSlotY + ABILITY_WHEEL_HEIGHT + 5;

                if (draggedFromCopiedIndex >= 0) {
                    if (isInAbilityWheelArea((int) mouseX, (int) mouseY, panelX, copiedWheelSlotY)) {
                        int targetSlot = getAbilityWheelSlot((int) mouseX, (int) mouseY, panelX, copiedWheelSlotY);
                        if (targetSlot >= 0 && targetSlot < ABILITY_WHEEL_MAX) {
                            if (targetSlot < abilityWheelSlots.size()) {
                                abilityWheelSlots.set(targetSlot, draggedAbility);
                                abilityWheelSubIndexes.set(targetSlot, draggedSubIndex);
                                abilityWheelIsCopied.set(targetSlot, true);
                            } else {
                                while (abilityWheelSlots.size() < targetSlot) {
                                    abilityWheelSlots.add(draggedAbility);
                                    abilityWheelSubIndexes.add(-1);
                                    abilityWheelIsCopied.add(false);
                                }
                                abilityWheelSlots.add(draggedAbility);
                                abilityWheelSubIndexes.add(draggedSubIndex);
                                abilityWheelIsCopied.add(true);
                            }
                            PacketHandler.sendToServer(new SyncAbilityWheelAbilitiesPacket(wheelSlotsToIdList()));
                        }
                    } else if (isInAbilityBarArea((int) mouseX, (int) mouseY, panelX, copiedBarSlotY)) {
                        int targetSlot = getAbilityBarSlot((int) mouseX, (int) mouseY, panelX, copiedBarSlotY);
                        if (targetSlot >= 0 && targetSlot < ABILITY_BAR_MAX) {
                            if (targetSlot < abilityBarSlots.size()) {
                                abilityBarSlots.set(targetSlot, draggedAbility);
                                abilityBarSubIndexes.set(targetSlot, draggedSubIndex);
                                abilityBarIsCopied.set(targetSlot, true);
                            } else {
                                abilityBarSlots.add(draggedAbility);
                                abilityBarSubIndexes.add(draggedSubIndex);
                                abilityBarIsCopied.add(true);
                            }
                            PacketHandler.sendToServer(new SyncAbilityBarAbilitiesPacket(barSlotsToIdList()));
                        }
                    }
                } else if (draggedFromWheelIndex >= 0) {
                    if (isInAbilityWheelArea((int) mouseX, (int) mouseY, panelX, copiedWheelSlotY)) {
                        int targetSlot = getAbilityWheelSlot((int) mouseX, (int) mouseY, panelX, copiedWheelSlotY);
                        if (targetSlot >= 0 && targetSlot < ABILITY_WHEEL_MAX && targetSlot != draggedFromWheelIndex) {
                            if (targetSlot < abilityWheelSlots.size()) {
                                Ability temp = abilityWheelSlots.get(targetSlot);
                                int tempSi = abilityWheelSubIndexes.get(targetSlot);
                                boolean tempCopied = abilityWheelIsCopied.get(targetSlot);
                                boolean draggedCopied = abilityWheelIsCopied.get(draggedFromWheelIndex);
                                abilityWheelSlots.set(targetSlot, draggedAbility);
                                abilityWheelSubIndexes.set(targetSlot, draggedSubIndex);
                                abilityWheelIsCopied.set(targetSlot, draggedCopied);
                                abilityWheelSlots.set(draggedFromWheelIndex, temp);
                                abilityWheelSubIndexes.set(draggedFromWheelIndex, tempSi);
                                abilityWheelIsCopied.set(draggedFromWheelIndex, tempCopied);
                            } else {
                                boolean wasCopied = abilityWheelIsCopied.get(draggedFromWheelIndex);
                                abilityWheelSlots.remove(draggedFromWheelIndex);
                                abilityWheelSubIndexes.remove(draggedFromWheelIndex);
                                abilityWheelIsCopied.remove(draggedFromWheelIndex);
                                abilityWheelSlots.add(draggedAbility);
                                abilityWheelSubIndexes.add(draggedSubIndex);
                                abilityWheelIsCopied.add(wasCopied);
                            }
                        } else if (targetSlot < 0) {
                            abilityWheelSlots.remove(draggedFromWheelIndex);
                            abilityWheelSubIndexes.remove(draggedFromWheelIndex);
                            abilityWheelIsCopied.remove(draggedFromWheelIndex);
                        }
                    } else {
                        abilityWheelSlots.remove(draggedFromWheelIndex);
                        abilityWheelSubIndexes.remove(draggedFromWheelIndex);
                        abilityWheelIsCopied.remove(draggedFromWheelIndex);
                    }
                    PacketHandler.sendToServer(new SyncAbilityWheelAbilitiesPacket(wheelSlotsToIdList()));
                } else if (draggedFromBarIndex >= 0) {
                    if (isInAbilityBarArea((int) mouseX, (int) mouseY, panelX, copiedBarSlotY)) {
                        int targetSlot = getAbilityBarSlot((int) mouseX, (int) mouseY, panelX, copiedBarSlotY);
                        if (targetSlot >= 0 && targetSlot < ABILITY_BAR_MAX && targetSlot != draggedFromBarIndex) {
                            if (targetSlot < abilityBarSlots.size()) {
                                Ability temp = abilityBarSlots.get(targetSlot);
                                int tempSi = abilityBarSubIndexes.get(targetSlot);
                                boolean tempCopied = abilityBarIsCopied.get(targetSlot);
                                boolean draggedCopied = abilityBarIsCopied.get(draggedFromBarIndex);
                                abilityBarSlots.set(targetSlot, draggedAbility);
                                abilityBarSubIndexes.set(targetSlot, draggedSubIndex);
                                abilityBarIsCopied.set(targetSlot, draggedCopied);
                                abilityBarSlots.set(draggedFromBarIndex, temp);
                                abilityBarSubIndexes.set(draggedFromBarIndex, tempSi);
                                abilityBarIsCopied.set(draggedFromBarIndex, tempCopied);
                            } else {
                                boolean wasCopied = abilityBarIsCopied.get(draggedFromBarIndex);
                                abilityBarSlots.remove(draggedFromBarIndex);
                                abilityBarSubIndexes.remove(draggedFromBarIndex);
                                abilityBarIsCopied.remove(draggedFromBarIndex);
                                abilityBarSlots.add(draggedAbility);
                                abilityBarSubIndexes.add(draggedSubIndex);
                                abilityBarIsCopied.add(wasCopied);
                            }
                        } else if (targetSlot < 0) {
                            abilityBarSlots.remove(draggedFromBarIndex);
                            abilityBarSubIndexes.remove(draggedFromBarIndex);
                            abilityBarIsCopied.remove(draggedFromBarIndex);
                        }
                    } else {
                        abilityBarSlots.remove(draggedFromBarIndex);
                        abilityBarSubIndexes.remove(draggedFromBarIndex);
                        abilityBarIsCopied.remove(draggedFromBarIndex);
                    }
                    PacketHandler.sendToServer(new SyncAbilityBarAbilitiesPacket(barSlotsToIdList()));
                }

                clearDragState();
                return true;
            }

            if (currentTab == Tab.ABILITY_WHEEL) {
                if (isInAbilityWheelArea((int) mouseX, (int) mouseY, panelX, slotY)) {
                    int targetSlot = getAbilityWheelSlot((int) mouseX, (int) mouseY, panelX, slotY);
                    if (targetSlot >= 0 && targetSlot < ABILITY_WHEEL_MAX) {
                        if (draggedFromAvailable) {
                            if (targetSlot < abilityWheelSlots.size()) {
                                abilityWheelSlots.set(targetSlot, draggedAbility);
                                abilityWheelSubIndexes.set(targetSlot, draggedSubIndex);
                                abilityWheelIsCopied.set(targetSlot, false);
                            } else {
                                while (abilityWheelSlots.size() < targetSlot) {
                                    abilityWheelSlots.add(draggedAbility);
                                    abilityWheelSubIndexes.add(-1);
                                    abilityWheelIsCopied.add(false);
                                }
                                abilityWheelSlots.add(draggedAbility);
                                abilityWheelSubIndexes.add(draggedSubIndex);
                                abilityWheelIsCopied.add(false);
                            }
                        } else if (draggedFromWheelIndex >= 0) {
                            if (targetSlot < abilityWheelSlots.size() && targetSlot != draggedFromWheelIndex) {
                                Ability temp = abilityWheelSlots.get(targetSlot);
                                int tempSi = abilityWheelSubIndexes.get(targetSlot);
                                boolean tempCopied = abilityWheelIsCopied.get(targetSlot);
                                boolean draggedCopied = abilityWheelIsCopied.get(draggedFromWheelIndex);
                                abilityWheelSlots.set(targetSlot, draggedAbility);
                                abilityWheelSubIndexes.set(targetSlot, draggedSubIndex);
                                abilityWheelIsCopied.set(targetSlot, draggedCopied);
                                abilityWheelSlots.set(draggedFromWheelIndex, temp);
                                abilityWheelSubIndexes.set(draggedFromWheelIndex, tempSi);
                                abilityWheelIsCopied.set(draggedFromWheelIndex, tempCopied);
                            } else if (targetSlot >= abilityWheelSlots.size()) {
                                boolean wasCopied = abilityWheelIsCopied.get(draggedFromWheelIndex);
                                abilityWheelSlots.remove(draggedFromWheelIndex);
                                abilityWheelSubIndexes.remove(draggedFromWheelIndex);
                                abilityWheelIsCopied.remove(draggedFromWheelIndex);
                                abilityWheelSlots.add(draggedAbility);
                                abilityWheelSubIndexes.add(draggedSubIndex);
                                abilityWheelIsCopied.add(wasCopied);
                            }
                        }
                    }
                } else {
                    if (!draggedFromAvailable && draggedFromWheelIndex >= 0) {
                        abilityWheelSlots.remove(draggedFromWheelIndex);
                        abilityWheelSubIndexes.remove(draggedFromWheelIndex);
                        abilityWheelIsCopied.remove(draggedFromWheelIndex);
                    }
                }
                PacketHandler.sendToServer(new SyncAbilityWheelAbilitiesPacket(wheelSlotsToIdList()));

            } else if (currentTab == Tab.ABILITY_BAR) {
                if (isInAbilityBarArea((int) mouseX, (int) mouseY, panelX, slotY)) {
                    int targetSlot = getAbilityBarSlot((int) mouseX, (int) mouseY, panelX, slotY);
                    if (targetSlot >= 0 && targetSlot < ABILITY_BAR_MAX) {
                        if (draggedFromAvailable) {
                            if (targetSlot < abilityBarSlots.size()) {
                                abilityBarSlots.set(targetSlot, draggedAbility);
                                abilityBarSubIndexes.set(targetSlot, draggedSubIndex);
                                abilityBarIsCopied.set(targetSlot, false);
                            } else {
                                abilityBarSlots.add(draggedAbility);
                                abilityBarSubIndexes.add(draggedSubIndex);
                                abilityBarIsCopied.add(false);
                            }
                        } else if (draggedFromBarIndex >= 0) {
                            if (targetSlot < abilityBarSlots.size() && targetSlot != draggedFromBarIndex) {
                                Ability temp = abilityBarSlots.get(targetSlot);
                                int tempSi = abilityBarSubIndexes.get(targetSlot);
                                boolean tempCopied = abilityBarIsCopied.get(targetSlot);
                                boolean draggedCopied = abilityBarIsCopied.get(draggedFromBarIndex);
                                abilityBarSlots.set(targetSlot, draggedAbility);
                                abilityBarSubIndexes.set(targetSlot, draggedSubIndex);
                                abilityBarIsCopied.set(targetSlot, draggedCopied);
                                abilityBarSlots.set(draggedFromBarIndex, temp);
                                abilityBarSubIndexes.set(draggedFromBarIndex, tempSi);
                                abilityBarIsCopied.set(draggedFromBarIndex, tempCopied);
                            } else if (targetSlot >= abilityBarSlots.size()) {
                                boolean wasCopied = abilityBarIsCopied.get(draggedFromBarIndex);
                                abilityBarSlots.remove(draggedFromBarIndex);
                                abilityBarSubIndexes.remove(draggedFromBarIndex);
                                abilityBarIsCopied.remove(draggedFromBarIndex);
                                abilityBarSlots.add(draggedAbility);
                                abilityBarSubIndexes.add(draggedSubIndex);
                                abilityBarIsCopied.add(wasCopied);
                            }
                        }
                    }
                } else {
                    if (!draggedFromAvailable && draggedFromBarIndex >= 0) {
                        abilityBarSlots.remove(draggedFromBarIndex);
                        abilityBarSubIndexes.remove(draggedFromBarIndex);
                        abilityBarIsCopied.remove(draggedFromBarIndex);
                    }
                }
                PacketHandler.sendToServer(new SyncAbilityBarAbilitiesPacket(barSlotsToIdList()));

            } else if (currentTab == Tab.SHARED_ABILITIES && draggedAbility != null) {
                int sectionY = panelY + ABILITIES_PANEL_HEIGHT + 5;
                String myUUID = this.minecraft.player.getStringUUID();
                List<String> myContributions = new ArrayList<>(ClientTeamData.getContributionsFor(myUUID));
                String draggedId = draggedAbility.getId();

                boolean inPoolArea = (int) mouseX >= panelX && (int) mouseX <= panelX + ABILITIES_PANEL_WIDTH
                        && (int) mouseY >= sectionY && (int) mouseY <= sectionY + SHARED_POOL_HEIGHT;

                int wheelY = sectionY + SHARED_POOL_HEIGHT + 5;
                int startX = panelX + 5;
                int startY = wheelY + 14;
                int iconsPerRowW = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
                int maxWheelSlots = iconsPerRowW * 2;

                int targetWheelSlot = -1;
                for (int i = 0; i < maxWheelSlots; i++) {
                    int wx = startX + (i % iconsPerRowW) * (ABILITY_ICON_SIZE + 2);
                    int wy = startY + (i / iconsPerRowW) * (ABILITY_ICON_SIZE + 2);
                    if ((int) mouseX >= wx && (int) mouseX <= wx + ABILITY_ICON_SIZE
                            && (int) mouseY >= wy && (int) mouseY <= wy + ABILITY_ICON_SIZE) {
                        targetWheelSlot = i;
                        break;
                    }
                }

                if (draggedFromAvailable) {
                    if (inPoolArea && !myContributions.contains(draggedId)) {
                        myContributions.add(draggedId);
                        PacketHandler.sendToServer(new SyncSharedAbilitiesPacket(new ArrayList<>(myContributions)));
                    }
                } else if (draggedFromSharedWheelIndex >= 0) {
                    if (targetWheelSlot >= 0 && targetWheelSlot != draggedFromSharedWheelIndex) {
                        sharedWheelSlots.remove(draggedFromSharedWheelIndex);
                        sharedWheelSlots.add(Math.min(targetWheelSlot, sharedWheelSlots.size()), draggedId);
                    } else if (targetWheelSlot < 0 && !inPoolArea) {
                        sharedWheelSlots.remove(draggedFromSharedWheelIndex);
                    }
                } else if (draggedFromSharedPoolIndex >= 0) {
                    if (targetWheelSlot >= 0) {
                        List<String> allPooled = getAllPooledAbilities();
                        if (allPooled.contains(draggedId) && !sharedWheelSlots.contains(draggedId)) {
                            if (targetWheelSlot < sharedWheelSlots.size()) {
                                sharedWheelSlots.add(targetWheelSlot, draggedId);
                            } else {
                                sharedWheelSlots.add(draggedId);
                            }
                        }
                    } else if (!inPoolArea) {
                        myContributions.remove(draggedId);
                        sharedWheelSlots.remove(draggedId);
                        AbilityWheelClientData.setSharedWheelAbilities(sharedWheelSlots);
                        PacketHandler.sendToServer(new SyncSharedAbilitiesPacket(new ArrayList<>(myContributions)));
                    }
                } else {
                    if (targetWheelSlot >= 0) {
                        List<String> allPooled = getAllPooledAbilities();
                        if (allPooled.contains(draggedId) && !sharedWheelSlots.contains(draggedId)) {
                            int mySeq = BeyonderData.getSequence(this.minecraft.player);
                            int reqSeq = draggedAbility.lowestSequenceUsable();
                            if (reqSeq >= 0 && mySeq > reqSeq) {
                                this.minecraft.player.displayClientMessage(
                                        Component.literal("Your sequence is too low to use this ability.").withStyle(ChatFormatting.RED), true);
                            } else {
                                if (targetWheelSlot < sharedWheelSlots.size()) {
                                    sharedWheelSlots.add(targetWheelSlot, draggedId);
                                } else {
                                    sharedWheelSlots.add(draggedId);
                                }
                            }
                        }
                    }
                }
            }

            AbilityWheelClientData.setSharedWheelAbilities(sharedWheelSlots);
            clearDragState();
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void clearDragState() {
        draggedAbility = null;
        draggedSubIndex = -1;
        draggedFromWheelIndex = -1;
        draggedFromBarIndex = -1;
        draggedFromSharedWheelIndex = -1;
        draggedFromSharedPoolIndex = -1;
        draggedFromAvailable = false;
        draggedFromCopiedIndex = -1;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (showAbilities) {
            int baseLeftPos = this.leftPos;
            int panelX = baseLeftPos + this.imageWidth + 5;
            int panelY = this.topPos + 15;

            if (isCopiedTab(currentTab)) {
                // Scroll the copied-ability pool
                if (mouseX >= panelX && mouseX <= panelX + ABILITIES_PANEL_WIDTH &&
                        mouseY >= panelY && mouseY <= panelY + COPIED_PANEL_HEIGHT) {
                    copiedScrollOffset = Math.max(0, Math.min(maxCopiedScroll, copiedScrollOffset - (int) scrollY));
                    return true;
                }
            } else {
                if (mouseX >= panelX && mouseX <= panelX + ABILITIES_PANEL_WIDTH &&
                        mouseY >= panelY && mouseY <= panelY + ABILITIES_PANEL_HEIGHT - 15) {
                    abilitiesScrollOffset = Math.max(0, Math.min(maxAbilitiesScroll, abilitiesScrollOffset - (int) scrollY));
                    return true;
                }

                if (currentTab == Tab.SHARED_ABILITIES) {
                    int slotY = this.topPos + 15 + ABILITIES_PANEL_HEIGHT + 5;
                    if (mouseX >= panelX && mouseX <= panelX + ABILITIES_PANEL_WIDTH &&
                            mouseY >= slotY && mouseY <= slotY + SHARED_POOL_HEIGHT) {
                        sharedPoolScrollOffset = Math.max(0, Math.min(maxSharedPoolScroll, sharedPoolScrollOffset - (int) scrollY));
                        return true;
                    }
                }
            }
        }

        if (showQuests) {
            int baseLeftPos = this.leftPos;
            int panelX = baseLeftPos + this.imageWidth + 5;
            int panelY = this.topPos;

            if (mouseX >= panelX && mouseX <= panelX + QUESTS_PANEL_WIDTH &&
                    mouseY >= panelY && mouseY <= panelY + COMPLETED_QUESTS_HEIGHT) {
                completedQuestsScrollOffset = Math.max(0, Math.min(maxCompletedQuestsScroll, completedQuestsScrollOffset - (int) scrollY));
                updateCompletedQuestsScroll();
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    // =====================================================================================
    //  Hit-test helpers
    // =====================================================================================

    private int getAbilityIndexAt(int mouseX, int mouseY, int panelX, int panelY) {
        int adjustedMouseY = mouseY - 15;
        int startX = panelX + 5;
        int startY = panelY - (abilitiesScrollOffset * (ABILITY_ICON_SIZE + 2));
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);

        int clipBottom = panelY + ABILITIES_PANEL_HEIGHT - 15;

        List<Ability> displayedAbilities = currentTab == Tab.SHARED_ABILITIES
                ? buildDisplayedSharedAbilities() : availableAbilities;

        for (int i = 0; i < displayedAbilities.size(); i++) {
            int row = i / iconsPerRow;
            int col = i % iconsPerRow;
            int x = startX + col * (ABILITY_ICON_SIZE + 2);
            int y = startY + row * (ABILITY_ICON_SIZE + 2);

            if (y < panelY || y + ABILITY_ICON_SIZE > clipBottom) continue;

            if (mouseX >= x && mouseX <= x + ABILITY_ICON_SIZE &&
                    adjustedMouseY >= y && adjustedMouseY <= y + ABILITY_ICON_SIZE) {
                return i;
            }
        }

        return -1;
    }

    private int getAbilityXByIndex(int index, int panelX) {
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int col = index % iconsPerRow;
        return panelX + 5 + col * (ABILITY_ICON_SIZE + 2);
    }

    private int getAbilityYByIndex(int index, int panelY) {
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int row = index / iconsPerRow;
        return panelY - abilitiesScrollOffset * (ABILITY_ICON_SIZE + 2) + row * (ABILITY_ICON_SIZE + 2) + 15;
    }

    private boolean isInAbilityWheelArea(int mouseX, int mouseY, int panelX, int wheelY) {
        return mouseX >= panelX && mouseX <= panelX + ABILITIES_PANEL_WIDTH &&
                mouseY >= wheelY + 15 && mouseY <= wheelY + ABILITY_WHEEL_HEIGHT;
    }

    private boolean isInAbilityBarArea(int mouseX, int mouseY, int panelX, int barY) {
        return mouseX >= panelX && mouseX <= panelX + ABILITIES_PANEL_WIDTH &&
                mouseY >= barY + 15 && mouseY <= barY + ABILITY_BAR_HEIGHT;
    }

    private int getAbilityWheelSlot(int mouseX, int mouseY, int panelX, int wheelY) {
        int startX = panelX + 5;
        int startY = wheelY + 15;
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);

        for (int i = 0; i < ABILITY_WHEEL_MAX; i++) {
            int row = i / iconsPerRow;
            int col = i % iconsPerRow;
            int x = startX + col * (ABILITY_ICON_SIZE + 2);
            int y = startY + row * (ABILITY_ICON_SIZE + 2);

            if (mouseX >= x && mouseX <= x + ABILITY_ICON_SIZE &&
                    mouseY >= y && mouseY <= y + ABILITY_ICON_SIZE) {
                return i;
            }
        }
        return -1;
    }

    private int getAbilityBarSlot(int mouseX, int mouseY, int panelX, int barY) {
        for (int i = 0; i < ABILITY_BAR_MAX; i++) {
            int x = barIconX(i, panelX);
            int y = barIconY(i, barY);

            if (mouseX >= x && mouseX <= x + ABILITY_ICON_SIZE &&
                    mouseY >= y && mouseY <= y + ABILITY_ICON_SIZE) {
                return i;
            }
        }
        return -1;
    }

    private int getWheelSlotX(int slot, int panelX, int wheelY) {
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int col = slot % iconsPerRow;
        return panelX + 5 + col * (ABILITY_ICON_SIZE + 2);
    }

    private int getWheelSlotY(int slot, int panelX, int wheelY) {
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int row = slot / iconsPerRow;
        return wheelY + 15 + row * (ABILITY_ICON_SIZE + 2);
    }

    private int getBarSlotX(int slot, int panelX, int barY) {
        return barIconX(slot, panelX);
    }

    private int getBarSlotY(int slot, int panelX, int barY) {
        return barIconY(slot, barY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title.getString(), this.titleLabelX, this.titleLabelY, 0xCCCCCC, true);
    }
}