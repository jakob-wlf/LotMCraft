package de.jakob.lotm.gui.custom.BlasphemySlate;

import de.jakob.lotm.attachments.BlasphemySlateData;
import de.jakob.lotm.item.custom.BlasphemySlateHalfItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.OpenRecipeMenuPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.PathwayInfos;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class SlateHalfPathwayScreen extends Screen {

    private static final int PANEL_W = 180;
    private static final int BTN_H   = 18;
    private static final int BTN_GAP = 4;
    private static final int PADDING = 8;

    private final BlasphemySlateHalfItem.HalfType halfType;
    private int leftPos, topPos;

    public SlateHalfPathwayScreen(BlasphemySlateHalfItem.HalfType halfType) {
        super(Component.empty());
        this.halfType = halfType;
    }

    @Override
    protected void init() {
        Set<String> pathways = halfType == BlasphemySlateHalfItem.HalfType.LEFT
                ? BlasphemySlateData.LEFT_HALF_PATHWAYS
                : BlasphemySlateData.RIGHT_HALF_PATHWAYS;

        List<String> ordered = new ArrayList<>(pathways);
        ordered.sort(String::compareTo);

        int panelH = PADDING + 16 + PADDING + ordered.size() * (BTN_H + BTN_GAP) + PADDING;
        leftPos = (width  - PANEL_W) / 2;
        topPos  = (height - panelH)  / 2;

        int btnY = topPos + PADDING + 16 + PADDING;
        for (String pathway : ordered) {
            PathwayInfos info = BeyonderData.pathwayInfos.get(pathway);
            String displayName = info != null ? info.getName() : capitalize(pathway);
            int color = info != null ? info.color() : 0xFFFFFFFF;

            int finalBtnY = btnY;
            String finalPathway = pathway;
            addRenderableWidget(Button.builder(
                    Component.literal(displayName).withColor(color),
                    b -> {
                        PacketHandler.sendToServer(new OpenRecipeMenuPacket(9, finalPathway, true));
                        onClose();
                    })
                    .bounds(leftPos + PADDING, finalBtnY, PANEL_W - PADDING * 2, BTN_H)
                    .build());

            btnY += BTN_H + BTN_GAP;
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Set<String> pathways = halfType == BlasphemySlateHalfItem.HalfType.LEFT
                ? BlasphemySlateData.LEFT_HALF_PATHWAYS
                : BlasphemySlateData.RIGHT_HALF_PATHWAYS;
        int panelH = PADDING + 16 + PADDING + pathways.size() * (BTN_H + BTN_GAP) + PADDING;

        g.fill(leftPos, topPos, leftPos + PANEL_W, topPos + panelH, 0xDD000000);
        g.renderOutline(leftPos, topPos, PANEL_W, panelH, 0xFF6A2090);

        String titleText = (halfType == BlasphemySlateHalfItem.HalfType.LEFT ? "Left" : "Right") + " Half — Pathways";
        Component title = Component.literal(titleText).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
        g.drawString(font, title,
                leftPos + PANEL_W / 2 - font.width(title) / 2,
                topPos + PADDING, 0xFFCC88FF, true);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).replace("_", " ");
    }
}
