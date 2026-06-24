package de.jakob.lotm.gui.custom.CopiedAbilityWheel;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.gui.custom.AbilityWheel.BaseAbilityWheelScreen;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.network.packets.toServer.UseCopiedAbilityPacket;
import de.jakob.lotm.util.data.ClientData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class CopiedAbilityWheelScreen extends BaseAbilityWheelScreen<CopiedAbilityWheelMenu> {

    private static final int MAX_ABILITIES = 24;

    public CopiedAbilityWheelScreen(CopiedAbilityWheelMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected List<String> getAbilities() {
        return ClientData.getCopiedAbilityIds();
    }

    @Override
    protected int getMaxAbilities() {
        return MAX_ABILITIES;
    }

    @Override
    protected int getLineColor() {
        return 0x4Da8c4e3;
    }

    @Override
    protected int getGlowColor() {
        return 0xa8c4e3;
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, SlotPosition pos, String abilityId, boolean isHovered, int index) {
        int size = isHovered ? SLOT_HOVER_SIZE : SLOT_SIZE;
        int x = pos.x - size / 2;
        int y = pos.y - size / 2;

        guiGraphics.fill(x, y, x + size, y + size, 0xCC000000);

        int borderColor = isHovered ? 0xFFa8c4e3 : 0xFF8999ab;
        int borderWidth = 1;

        if (isHovered) {
            guiGraphics.fill(x - 1, y - 1, x + size + 1, y + size + 1, 0x40a8c4e3);
        }

        guiGraphics.fill(x, y, x + size, y + borderWidth, borderColor);
        guiGraphics.fill(x, y + size - borderWidth, x + size, y + size, borderColor);
        guiGraphics.fill(x, y, x + borderWidth, y + size, borderColor);
        guiGraphics.fill(x + size - borderWidth, y, x + size, y + size, borderColor);

        try {
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/abilities/" + abilityId + ".png");
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture);

            int iconPadding = 3;
            int iconSize = size - iconPadding * 2;
            guiGraphics.blit(texture,
                    x + iconPadding,
                    y + iconPadding,
                    0, 0,
                    iconSize,
                    iconSize,
                    iconSize,
                    iconSize);
        } catch (Exception e) {
            int padding = size / 5;
            guiGraphics.fill(x + padding, y + padding, x + size - padding, y + size - padding, 0xFF146B8B);
        }

        // Render remaining uses indicator
        if (index < ClientData.getCopiedAbilityRemainingUses().size()) {
            int uses = ClientData.getCopiedAbilityRemainingUses().get(index);
            if (uses > 0) {
                String usesText = String.valueOf(uses);
                guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, usesText,
                        x + size - 8, y + size - 10, 0xFFFFFF, true);
            } else if (uses == -1) {
                guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, "∞",
                        x + size - 8, y + size - 10, 0x90EE90, true);
            }
        }

        // Show ability name tooltip on hover
        if (isHovered) {
            Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
            if (ability != null) {
                Component name = ability.getNameFormatted(ClientHandler.getPlayer());
                int textWidth = net.minecraft.client.Minecraft.getInstance().font.width(name);
                guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, name,
                        pos.x - textWidth / 2, pos.y - size / 2 - 12, 0xFFFFFF, true);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredSlot != -1) {
            PacketHandler.sendToServer(new UseCopiedAbilityPacket(hoveredSlot));
            this.onClose();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
