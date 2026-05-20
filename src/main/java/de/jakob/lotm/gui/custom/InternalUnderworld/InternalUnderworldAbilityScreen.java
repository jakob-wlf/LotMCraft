package de.jakob.lotm.gui.custom.InternalUnderworld;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.HashMap;
import java.util.Map;

public class InternalUnderworldAbilityScreen extends AbstractContainerScreen<ChestMenu> {
    private static final int ICON_SIZE = 16;
    private static final int ICON_BG_COLOR = 0xFF0F1E33;
    private static final int PANEL_BG_COLOR = 0xFF0A1B36;
    private static final int PANEL_INNER_COLOR = 0xFF08162C;
    private static final int PANEL_BORDER_COLOR = 0xFF335A8C;
    private static final int SLOT_BG_COLOR = 0xFF102540;
    private static final int SLOT_BORDER_COLOR = 0xFF1A385E;
    private static final int ICON_BORDER_COLOR = 0xFFFFFFFF;

    // Cache ability display names to ids for items without explicit tags.
    private static final Map<String, String> ABILITY_NAME_TO_ID = new HashMap<>();
    private static boolean abilityNameCacheBuilt = false;

    private final int containerRows;
    private final int chestSlotCount;

    public InternalUnderworldAbilityScreen(ChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.containerRows = menu.getRowCount();
        this.chestSlotCount = this.containerRows * 9;
        this.imageWidth = 176;
        this.imageHeight = this.containerRows * 18 + 28;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (this.hoveredSlot != null && !isChestSlot(this.hoveredSlot)) {
            this.hoveredSlot = null;
        }
        renderAbilityIcons(guiGraphics);
        RenderSystem.disableDepthTest();
        renderTooltip(guiGraphics, mouseX, mouseY);
        RenderSystem.enableDepthTest();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, PANEL_BG_COLOR);
        guiGraphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, PANEL_INNER_COLOR);
        guiGraphics.renderOutline(x, y, this.imageWidth, this.imageHeight, PANEL_BORDER_COLOR);

        for (Slot slot : this.menu.slots) {
            if (!isChestSlot(slot)) {
                continue;
            }
            int sx = x + slot.x;
            int sy = y + slot.y;
            guiGraphics.fill(sx, sy, sx + 18, sy + 18, SLOT_BG_COLOR);
            guiGraphics.renderOutline(sx, sy, 18, 18, SLOT_BORDER_COLOR);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String titleText = this.title.getString();
        int maxWidth = this.imageWidth - 12;
        String displayText = this.font.plainSubstrByWidth(titleText, maxWidth);
        if (!displayText.equals(titleText)) {
            int dotsWidth = this.font.width("...");
            displayText = this.font.plainSubstrByWidth(titleText, Math.max(0, maxWidth - dotsWidth)) + "...";
        }
        int titleX = Math.max(4, (this.imageWidth - this.font.width(displayText)) / 2);
        guiGraphics.drawString(this.font, displayText, titleX, this.titleLabelY, 0xBFD7FF, false);
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (!isChestSlot(slot)) {
            return;
        }

        ItemStack stack = slot.getItem();
        if (!stack.isEmpty()) {
            String abilityId = getAbilityId(stack);
            if (abilityId == null || abilityId.isEmpty()) {
                abilityId = resolveAbilityIdFromName(stack);
            }
            // Skip default item rendering when we will draw a custom ability icon.
            if (abilityId != null && !abilityId.isEmpty()) {
                return;
            }
        }

        super.renderSlot(guiGraphics, slot);
    }

    private void renderAbilityIcons(GuiGraphics guiGraphics) {
        // Draw ability icons over the soul ability list slots.
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0f, 0.0f, 500.0f);

        for (Slot slot : this.menu.slots) {
            if (!isChestSlot(slot)) {
                continue;
            }
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                continue;
            }

            String abilityId = getAbilityId(stack);
            if (abilityId == null || abilityId.isEmpty()) {
                abilityId = resolveAbilityIdFromName(stack);
            }
            if (abilityId == null || abilityId.isEmpty()) {
                continue;
            }

            int x = this.leftPos + slot.x;
            int y = this.topPos + slot.y;

            guiGraphics.fill(x, y, x + 18, y + 18, SLOT_BG_COLOR);
            guiGraphics.renderOutline(x, y, 18, 18, SLOT_BORDER_COLOR);
            guiGraphics.fill(x, y, x + 18, y + 18, 0xFF0A1B36);
            drawAbilityIcon(guiGraphics, abilityId, x + 1, y + 1);
            guiGraphics.renderOutline(x + 1, y + 1, 16, 16, ICON_BORDER_COLOR);
        }

        guiGraphics.pose().popPose();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private void drawAbilityIcon(GuiGraphics guiGraphics, String abilityId, int x, int y) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/abilities/" + abilityId + ".png");
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, texture);
        guiGraphics.blit(texture, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }

    private static String getAbilityId(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        CompoundTag tag = data.copyTag();
        if (!tag.contains("AbilityId")) {
            return null;
        }
        return tag.getString("AbilityId");
    }

    private static String resolveAbilityIdFromName(ItemStack stack) {
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name == null) return null;
        String displayName = name.getString();
        if (displayName == null || displayName.isEmpty()) return null;

        if (!abilityNameCacheBuilt) {
            buildAbilityNameCache();
        }

        return ABILITY_NAME_TO_ID.get(displayName);
    }

    private static void buildAbilityNameCache() {
        // Build a lookup of pretty display names to ability ids.
        ABILITY_NAME_TO_ID.clear();
        for (Ability ability : LOTMCraft.abilityHandler.getAbilities()) {
            String display = prettyAbilityName(ability.getId());
            if (display != null && !display.isEmpty()) {
                ABILITY_NAME_TO_ID.put(display, ability.getId());
            }
        }
        abilityNameCacheBuilt = true;
    }

    private static String prettyAbilityName(String id) {
        if (id == null || id.isEmpty()) return "Ability";
        String cleaned = id.replace("_ability", "").replace('_', ' ');
        String[] parts = cleaned.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) sb.append(part.substring(1));
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    private boolean isChestSlot(Slot slot) {
        return slot.index < chestSlotCount;
    }
}
