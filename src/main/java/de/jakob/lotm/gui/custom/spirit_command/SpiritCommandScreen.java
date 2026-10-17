package de.jakob.lotm.gui.custom.spirit_command;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.ability_wheel.BaseAbilityWheelScreen;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.UseSpiritPowerPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class SpiritCommandScreen extends BaseAbilityWheelScreen<SpiritCommandMenu> {
    private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/abilities/spirit_commanding_ability.png");
    public SpiritCommandScreen(SpiritCommandMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected List<String> getAbilities() {
        return menu.powers();
    }

    @Override
    protected int getMaxAbilities() {
        return 6;
    }

    @Override
    protected int getLineColor() {
        return 0x4D6a4a78;
    }

    @Override
    protected int getGlowColor() {
        return 0x6a4a78;
    }

    @Override
    protected void renderSlot(GuiGraphics graphics, SlotPosition pos, String abilityId, boolean hovered, int index) {
        int size = hovered ? SLOT_HOVER_SIZE : SLOT_SIZE;
        int x = pos.x() - size / 2;
        int y = pos.y() - size / 2;
        graphics.fill(x, y, x + size, y + size, 0xCC120814);
        int border = hovered ? 0xFFc8b4d4 : 0xFF6a4a78;
        graphics.fill(x, y, x + size, y + 1, border);
        graphics.fill(x, y + size - 1, x + size, y + size, border);
        graphics.fill(x, y, x + 1, y + size, border);
        graphics.fill(x + size - 1, y, x + size, y + size, border);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ICON);
        int iconPadding = 3;
        int iconSize = size - iconPadding * 2;
        graphics.blit(ICON, x + iconPadding, y + iconPadding, 0, 0, iconSize, iconSize, iconSize, iconSize);
        if (!hovered) return;
        int split = abilityId.lastIndexOf('|');
        String type = split < 0 ? abilityId : abilityId.substring(0, split);
        String slot = split < 0 ? "0" : abilityId.substring(split + 1);
        String path = ResourceLocation.parse(type).getPath();
        Component name = Component.translatable("ability.lotmcraft.spirit_power." + path + "." + slot);
        int textWidth = font.width(name);
        graphics.drawString(font, name, pos.x() - textWidth / 2, pos.y() - size / 2 - 12, 0xFFFFFF, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredSlot != -1) {
            PacketHandler.sendToServer(new UseSpiritPowerPacket(hoveredSlot));
            onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
