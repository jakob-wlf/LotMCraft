package de.jakob.lotm.gui.custom.HonorificNames;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.SetHonorificNamePacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.beyonderMap.HonorificName;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * Standalone screen that lets the player set their honorific name without using JSON.
 * Shows one EditBox per required line, with hint text listing the pathway's required words.
 */
public class SetHonorificNameScreen extends Screen {

    private static final int COL_TITLE   = 0xFFFFFFFF;
    private static final int COL_LABEL   = 0xFF9090D0;
    private static final int COL_ERROR   = 0xFFFF5555;
    private static final int COL_HINT    = 0xFF7777AA;

    private final String pathway;
    private final int sequence;

    /** Number of edit boxes to show (3, 4, or 5 depending on sequence). */
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
                        .bounds(centerX - 55, confirmY, 110, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Cancel"), btn -> onClose())
                        .bounds(centerX - 55, confirmY + 30, 110, 20)
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

        // Client-side validation
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

        PacketHandler.sendToServer(new SetHonorificNamePacket(lines));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx, mouseX, mouseY, partialTick);
        super.render(gfx, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int startY = this.height / 2 - (lineCount * 30) / 2;

        // Title
        String title = "Set Honorific Name";
        gfx.drawCenteredString(font, title, centerX, startY - 40, COL_TITLE);

        // Pathway / sequence info
        String pathInfo = BeyonderData.pathwayInfos.get(pathway).getSequenceName(sequence);
        gfx.drawCenteredString(font, pathInfo, centerX, startY - 28, COL_LABEL);

        // Required words hint
        List<String> requiredWords = HonorificName.getMustHaveWords(pathway);
        if (!requiredWords.isEmpty()) {
            // Wrap text if too long
            int maxWidth = Math.min(this.width - 40, 400);
            gfx.drawCenteredString(font, "Required words (at least one per line):", centerX, startY - 16, COL_HINT);
            // Simple display - truncate if too wide
            String wordsStr = String.join(", ", requiredWords);
            if (font.width(wordsStr) > maxWidth) {
                int truncIndex = Math.max(0, Math.min(
                        wordsStr.length() * maxWidth / font.width(wordsStr) - 3,
                        wordsStr.length() - 3));
                wordsStr = wordsStr.substring(0, truncIndex) + "...";
            }
            gfx.drawCenteredString(font, wordsStr, centerX, startY - 3, 0xFF8888CC);
        }

        // Error message
        if (!errorMessage.isEmpty()) {
            gfx.drawCenteredString(font, errorMessage, centerX,
                    startY + lineCount * 30 + 64, COL_ERROR);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
