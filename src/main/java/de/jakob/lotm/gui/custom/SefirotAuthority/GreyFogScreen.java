package de.jakob.lotm.gui.custom.SefirotAuthority;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.GreyFogActionPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Sub-screen for the Sefirah Castle "Call upon the Grey Fog" ability.
 * Shows three area-effect powers the castle owner can activate.
 */
@OnlyIn(Dist.CLIENT)
public class GreyFogScreen extends Screen {

    private static final int PANEL_W = 220;
    private static final int PANEL_H = 130;

    private final Screen parent;

    public GreyFogScreen(Screen parent) {
        super(Component.literal("The Grey Fog"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int cx = (this.width  - PANEL_W) / 2;
        int cy = (this.height - PANEL_H) / 2;

        int btnW = PANEL_W - 20;
        int btnX = cx + 10;

        addRenderableWidget(Button.builder(
                Component.literal("Seal Surroundings [250 blocks]").withStyle(ChatFormatting.DARK_AQUA),
                b -> {
                    PacketHandler.sendToServer(new GreyFogActionPacket(GreyFogActionPacket.SEAL));
                    onClose();
                }
        ).bounds(btnX, cy + 30, btnW, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("Weave the Fool Card [hold paper]").withStyle(ChatFormatting.AQUA),
                b -> {
                    PacketHandler.sendToServer(new GreyFogActionPacket(GreyFogActionPacket.ANTI_DIVINATION));
                    onClose();
                }
        ).bounds(btnX, cy + 55, btnW, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("Reverse Fortune [250 blocks]").withStyle(ChatFormatting.LIGHT_PURPLE),
                b -> {
                    PacketHandler.sendToServer(new GreyFogActionPacket(GreyFogActionPacket.FOOL_EFFECTS));
                    onClose();
                }
        ).bounds(btnX, cy + 80, btnW, 20).build());
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Plain dark overlay — no vanilla blur effect
        g.fill(0, 0, this.width, this.height, 0x88000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);

        int cx = (this.width  - PANEL_W) / 2;
        int cy = (this.height - PANEL_H) / 2;

        // Panel background
        g.fill(cx, cy, cx + PANEL_W, cy + PANEL_H, 0xDD000000);
        g.renderOutline(cx, cy, PANEL_W, PANEL_H, 0xFF8A6A2E);

        // Title
        Component title = Component.literal("The Grey Fog").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD);
        g.drawString(this.font, title,
                cx + PANEL_W / 2 - font.width(title) / 2, cy + 10, 0xFFCCCCCC, true);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
