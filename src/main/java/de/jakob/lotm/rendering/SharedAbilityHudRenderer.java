package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.util.data.ClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.List;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class SharedAbilityHudRenderer {

    private static final int HOTBAR_WIDTH  = 182;
    private static final int FRAME_SIZE    = 22;   // matches AbilityIconRenderer WIDTH/HEIGHT
    private static final int ICON_SIZE     = 18;   // matches AbilityIconRenderer ICON_WIDTH/HEIGHT
    private static final int PAD_X         = 8;
    private static final int PAD_Y         = 4;
    private static final int SEPARATOR_GAP = 2;
    private static final int COLOR_LABEL   = 0xFFAAAAAA;
    private static final int COLOR_BG      = 0xCC0A0A0F;
    private static final int COLOR_ACCENT  = 0xFF4444AA;

    private static final ResourceLocation backgroundTexture =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/ability_frame.png");
    private static final ResourceLocation foregroundTexture =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/ability_frame_foreground.png");

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "shared_ability_hud"),
                (guiGraphics, deltaTracker) -> renderOverlay(guiGraphics)
        );
    }

    private static void renderOverlay(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;

        List<String> wheel = ClientData.getSharedWheelAbilities();
        if (wheel.isEmpty()) return;

        int idx = ClientData.getSelectedSharedAbility();
        if (idx < 0 || idx >= wheel.size()) return;

        Ability selected = LOTMCraft.abilityHandler.getById(wheel.get(idx));
        if (selected == null) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        // --- Icon frame ---
        // Regular ability icon (AbilityIconRenderer) is at:
        //   x = hotbarStartX - FRAME_SIZE - 5   (shifted left 30 more if offhand item present)
        //   y = screenH - hotbarHeight/2 - FRAME_SIZE/2  = screenH - 11 - 11 = screenH - 22
        int hotbarStartX = (screenW - HOTBAR_WIDTH) / 2;
        boolean hasOffhand = !mc.player.getItemInHand(InteractionHand.OFF_HAND).isEmpty();
        int regularIconX = hasOffhand
                ? hotbarStartX - FRAME_SIZE - 5 - 30
                : hotbarStartX - FRAME_SIZE - 5;
        int iconY = screenH - 11 - (FRAME_SIZE / 2);

        // Place shared icon directly to the left of the regular icon, with 3px gap
        int sharedIconX = regularIconX - FRAME_SIZE - 3;

        guiGraphics.blit(backgroundTexture, sharedIconX, iconY, 0, 0, FRAME_SIZE, FRAME_SIZE, FRAME_SIZE, FRAME_SIZE);
        if (selected.getTextureLocation() != null) {
            guiGraphics.blit(selected.getTextureLocation(),
                    sharedIconX + 2, iconY + 2,
                    0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        }
        guiGraphics.blit(foregroundTexture, sharedIconX, iconY, 0, 0, FRAME_SIZE, FRAME_SIZE, FRAME_SIZE, FRAME_SIZE);

        // --- Sub-ability text box (only for SelectableAbility) ---
        if (!(selected instanceof SelectableAbility ability)) return;

        Component labelText   = Component.translatable("lotm.selected").append(":").withColor(COLOR_LABEL);
        Component abilityText = Component.translatable(ability.getSelectedAbility(mc.player)).withColor(COLOR_ACCENT);

        int textWidth = Math.max(mc.font.width(labelText), mc.font.width(abilityText));
        int lineHeight = mc.font.lineHeight;

        int boxW = PAD_X + textWidth + PAD_X;
        int boxH = PAD_Y + lineHeight + SEPARATOR_GAP + 1 + SEPARATOR_GAP + lineHeight + PAD_Y;

        // SelectedAbilityRenderer is at boxX = hotbarEndX + 12, boxY = screenH - boxH (bottom-aligned)
        // Place shared text box immediately to the right of it, same Y alignment
        int existingBoxW;
        {
            int selIdx = ClientData.getSelectedAbility();
            var abilities = ClientData.getAbilityWheelAbilities();
            if (selIdx >= 0 && selIdx < abilities.size()) {
                Ability selAbility = LOTMCraft.abilityHandler.getById(abilities.get(selIdx));
                if (selAbility instanceof SelectableAbility selectable) {
                    Component selLabel = Component.translatable("lotm.selected").append(":").withColor(COLOR_LABEL);
                    Component selName  = Component.translatable(selectable.getSelectedAbility(mc.player)).withColor(0xFFFFFFFF);
                    existingBoxW = PAD_X + Math.max(mc.font.width(selLabel), mc.font.width(selName)) + PAD_X;
                } else {
                    existingBoxW = 0;
                }
            } else {
                existingBoxW = 0;
            }
        }

        int hotbarEndX = (screenW - HOTBAR_WIDTH) / 2 + HOTBAR_WIDTH;
        int gap = 12;
        // If SelectedAbilityRenderer is visible (existingBoxW > 0), place after it; otherwise use same start position
        int boxX = existingBoxW > 0
                ? hotbarEndX + gap + existingBoxW + gap
                : hotbarEndX + gap;
        int boxY = screenH - boxH;

        guiGraphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, COLOR_BG);
        guiGraphics.renderOutline(boxX, boxY, boxW, boxH, COLOR_ACCENT);

        int textX  = boxX + PAD_X;
        int labelY = boxY + PAD_Y;
        int sepY   = labelY + lineHeight + SEPARATOR_GAP;
        int nameY  = sepY + 1 + SEPARATOR_GAP;

        int sepColor = (COLOR_ACCENT & 0x00FFFFFF) | 0x55000000;
        guiGraphics.fill(textX, sepY, boxX + boxW - PAD_X, sepY + 1, sepColor);

        guiGraphics.drawString(mc.font, labelText,   textX, labelY, COLOR_LABEL,  false);
        guiGraphics.drawString(mc.font, abilityText, textX, nameY,  COLOR_ACCENT, false);
    }
}
