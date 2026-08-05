package de.jakob.lotm.gui.custom.AboveSeqAuthority;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder screen for the "Above the Sequence Authority" ability.
 * Displays a "Coming Soon" message with the ability texture as a backdrop icon.
 */
@OnlyIn(Dist.CLIENT)
public class AboveSeqAuthorityScreen extends Screen {

    private static final ResourceLocation ABILITY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/abilities/above_the_sequence_authority_ability.png");

    private static final int PANEL_W = 320;
    private static final int PANEL_H = 220;
    private static final int ICON_SIZE = 80;

    // Colors
    private static final int COL_BG       = 0xEE030010;
    private static final int COL_OUTLINE  = 0xFF4400CC;
    private static final int COL_TITLE    = 0xFFDDB8FF;
    private static final int COL_SUBTITLE = 0xFFAA88DD;
    private static final int COL_DESC     = 0xFF8866BB;

    public AboveSeqAuthorityScreen() {
        super(Component.translatable("lotmcraft.above_the_sequence_authority_ability"));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Skip the blur shader — just a plain dark overlay
        graphics.fill(0, 0, this.width, this.height, 0x88000000);
    }

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2;
        int cy = this.height / 2;
        int panelTop = cy - PANEL_H / 2;

        // Close button at the bottom of the panel
        this.addRenderableWidget(Button.builder(
                Component.literal("Close"),
                btn -> this.onClose()
        ).bounds(cx - 40, panelTop + PANEL_H - 32, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Dim background
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int cx = this.width / 2;
        int cy = this.height / 2;
        int px = cx - PANEL_W / 2;
        int py = cy - PANEL_H / 2;

        // Panel background
        graphics.fill(px, py, px + PANEL_W, py + PANEL_H, COL_BG);

        // Outline
        graphics.hLine(px, px + PANEL_W - 1, py,               COL_OUTLINE);
        graphics.hLine(px, px + PANEL_W - 1, py + PANEL_H - 1, COL_OUTLINE);
        graphics.vLine(px,               py, py + PANEL_H - 1,  COL_OUTLINE);
        graphics.vLine(px + PANEL_W - 1, py, py + PANEL_H - 1,  COL_OUTLINE);

        // Ability icon
        int iconX = cx - ICON_SIZE / 2;
        int iconY = py + 16;
        graphics.blit(ABILITY_TEXTURE, iconX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);

        // Title
        Component title = Component.translatable("lotmcraft.above_the_sequence_authority_ability");
        graphics.drawCenteredString(this.font, title, cx, iconY + ICON_SIZE + 12, COL_TITLE);

        // "Coming Soon" text
        graphics.drawCenteredString(this.font,
                Component.literal("✦  Coming Soon  ✦"),
                cx, iconY + ICON_SIZE + 28, COL_SUBTITLE);

        // Description
        Component desc = Component.translatable("lotmcraft.above_the_sequence_authority_ability.description");
        graphics.drawCenteredString(this.font, desc, cx, iconY + ICON_SIZE + 48, COL_DESC);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
