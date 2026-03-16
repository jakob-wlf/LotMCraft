package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.data.ClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class SelectedAbilityRenderer {

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "selected_ability_overlay"),
                (guiGraphics, deltaTracker) -> renderOverlay(guiGraphics)
        );
    }

    private static final int HOTBAR_WIDTH  = 182;
    private static final int PAD_X         = 8;
    private static final int PAD_Y         = 4;  // reduced from 6
    private static final int GAP_HOTBAR    = 12;
    private static final int SEPARATOR_GAP = 2;  // reduced from 3

    private static final int COLOR_LABEL   = 0xFFAAAAAA;
    private static final int COLOR_BG      = 0xCC0A0A0F;

    private static void renderOverlay(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;

        var pathway = ClientBeyonderCache.getPathway(mc.player.getUUID());
        if (!BeyonderData.pathwayInfos.containsKey(pathway)) return;
        if (!ClientBeyonderCache.isBeyonder(mc.player.getUUID())) return;

        int pathwayColor = BeyonderData.pathwayInfos.get(pathway).color();

        int selectedIndex = ClientData.getSelectedAbility();
        var abilities = ClientData.getAbilityWheelAbilities();
        if (selectedIndex < 0 || selectedIndex >= abilities.size()) return;

        Ability selectedAbility = LOTMCraft.abilityHandler.getById(abilities.get(selectedIndex));
        if (!(selectedAbility instanceof SelectableAbility ability)) return;

        Component labelText   = Component.translatable("lotm.selected").append(":").withColor(COLOR_LABEL);
        Component abilityText = Component.translatable(ability.getSelectedAbility(mc.player)).withColor(pathwayColor);

        int textWidth  = Math.max(mc.font.width(labelText), mc.font.width(abilityText));
        int lineHeight = mc.font.lineHeight;

        int boxW = PAD_X + textWidth + PAD_X;
        int boxH = PAD_Y + lineHeight + SEPARATOR_GAP + 1 + SEPARATOR_GAP + lineHeight + PAD_Y;

        int screenW    = mc.getWindow().getGuiScaledWidth();
        int screenH    = mc.getWindow().getGuiScaledHeight();
        int boxX       = (screenW - HOTBAR_WIDTH) / 2 + HOTBAR_WIDTH + GAP_HOTBAR;
        int boxY       = screenH - boxH;

        // Background
        guiGraphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, COLOR_BG);

        // Uniform colored border all around
        guiGraphics.renderOutline(boxX, boxY, boxW, boxH, pathwayColor);

        // Separator line between label and ability name
        int textX    = boxX + PAD_X;
        int labelY   = boxY + PAD_Y;
        int sepY     = labelY + lineHeight + SEPARATOR_GAP;
        int abilityY = sepY + 1 + SEPARATOR_GAP;

        int sepColor = (pathwayColor & 0x00FFFFFF) | 0x55000000;
        guiGraphics.fill(textX, sepY, boxX + boxW - PAD_X, sepY + 1, sepColor);

        guiGraphics.drawString(mc.font, labelText,   textX, labelY,   COLOR_LABEL,   false);
        guiGraphics.drawString(mc.font, abilityText, textX, abilityY, pathwayColor,  false);
    }
}