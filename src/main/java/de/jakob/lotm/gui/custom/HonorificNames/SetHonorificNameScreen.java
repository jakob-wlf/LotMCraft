package de.jakob.lotm.gui.custom.HonorificNames;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.SetHonorificNameC2SPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.HonorificName;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.LinkedList;
import java.util.List;

public class SetHonorificNameScreen extends Screen {

    private static final int COL_TITLE   = 0xFFDDDDDD;
    private static final int COL_LABEL   = 0xFFAAAAAA;
    private static final int COL_ERROR   = 0xFFFF6666;
    private static final int COL_HINT    = 0xFF888888;
    private static final int COL_DIVIDER = 0xFF555555;
    private static final int COL_PANEL   = 0xC0151515;

    private final String pathway;
    private final int sequence;
    private final int lineCount;

    private final List<EditBox> lineBoxes = new LinkedList<>();
    private String errorMessage = "";

    public SetHonorificNameScreen(String pathway, int sequence) {
        super(Component.literal("Set Honorific Name"));
        this.pathway = pathway;
        this.sequence = sequence;
        this.lineCount = requiredLineCount(sequence);
    }

    private static int requiredLineCount(int sequence) {
        if (sequence == 3) return 5;
        if (sequence == 2) return 4;
        return 3;
    }

    @Override
    protected void init() {
        super.init();
        lineBoxes.clear();

        int centerX = this.width / 2;
        int startY = this.height / 2 - (lineCount * 30) / 2 + 20;

        for (int i = 0; i < lineCount; i++) {
            EditBox box = new EditBox(this.font,
                    centerX - 130, startY + i * 30,
                    260, 20,
                    Component.literal("Line " + (i + 1)));
            box.setMaxLength(HonorificName.MAX_LENGTH - 1);
            box.setHint(Component.literal("Line " + (i + 1)));
            this.addRenderableWidget(box);
            lineBoxes.add(box);
        }

        int confirmY = startY + lineCount * 30 + 8;
        this.addRenderableWidget(
                Button.builder(Component.literal("Confirm"), btn -> onConfirm())
                        .bounds(centerX - 58, confirmY, 112, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Cancel"), btn -> onClose())
                        .bounds(centerX - 58, confirmY + 26, 112, 20)
                        .build()
        );
    }

    private void onConfirm() {
        LinkedList<String> lines = new LinkedList<>();
        for (EditBox box : lineBoxes) {
            String value = box.getValue().trim();
            if (value.isEmpty()) {
                errorMessage = "All lines must be filled in.";
                return;
            }
            lines.add(value);
        }

        if (sequence >= 4) {
            errorMessage = "You must be sequence 3 or higher to utilize honorific name.";
            return;
        }

        if (lines.stream().distinct().count() != lines.size()) {
            errorMessage = "Each line must be different.";
            return;
        }

        if (!HonorificName.validate(pathway, lines)) {
            errorMessage = "Each line must contain at least one required word (see hints above).";
            return;
        }

        PacketHandler.sendToServer(new SetHonorificNameC2SPacket(lines));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.render(gfx, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int startY  = this.height / 2 - (lineCount * 30) / 2 + 20;

        int panelTop    = startY - 58;
        int panelBottom = startY + lineCount * 30 + 60 + (errorMessage.isEmpty() ? 0 : 14);
        int panelLeft   = centerX - 148;
        int panelRight  = centerX + 148;

        gfx.fill(panelLeft, panelTop, panelRight, panelBottom, COL_PANEL);
        gfx.hLine(panelLeft,      panelRight - 1, panelTop,        COL_DIVIDER);
        gfx.hLine(panelLeft,      panelRight - 1, panelBottom - 1, COL_DIVIDER);
        gfx.vLine(panelLeft,      panelTop,        panelBottom,     COL_DIVIDER);
        gfx.vLine(panelRight - 1, panelTop,        panelBottom,     COL_DIVIDER);

        gfx.drawCenteredString(font, "Set Honorific Name", centerX, panelTop + 8, COL_TITLE);

        int divY = panelTop + 20;
        gfx.hLine(centerX - 80, centerX + 80, divY, COL_DIVIDER);

        String pathInfo = BeyonderData.pathwayInfos.get(pathway).getSequenceName(sequence);
        gfx.drawCenteredString(font, pathInfo, centerX, divY + 5, COL_LABEL);

        List<String> requiredWords = HonorificName.getMustHaveWords(pathway);
        if (!requiredWords.isEmpty()) {
            int maxWidth = Math.min(this.width - 40, 400);
            gfx.drawCenteredString(font, "Required words (at least one per line):", centerX, divY + 17, COL_HINT);
            String wordsStr = String.join(", ", requiredWords);
            if (font.width(wordsStr) > maxWidth) {
                int truncIndex = Math.max(0, Math.min(
                        wordsStr.length() * maxWidth / font.width(wordsStr) - 3,
                        wordsStr.length() - 3));
                wordsStr = wordsStr.substring(0, truncIndex) + "...";
            }
            gfx.drawCenteredString(font, wordsStr, centerX, divY + 27, COL_HINT);
        }

        if (!errorMessage.isEmpty()) {
            gfx.drawCenteredString(font, errorMessage, centerX, startY + lineCount * 30 + 56, COL_ERROR);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}