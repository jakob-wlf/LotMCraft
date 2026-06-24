package de.jakob.lotm.gui.custom.AbilityWheel;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.events.KeyInputHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.network.packets.toServer.UpdateSelectedAbilityPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.ClientData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class AbilityWheelScreen extends BaseAbilityWheelScreen<AbilityWheelMenu> {

    private static final int MAX_ABILITIES = 27;

    public AbilityWheelScreen(AbilityWheelMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected List<String> getAbilities() {
        if (ClientData.sharedAbilityMode) {
            return ClientData.getSharedWheelAbilities();
        }
        return ClientData.getAbilityWheelAbilities();
    }

    @Override
    protected int getMaxAbilities() {
        return MAX_ABILITIES;
    }

    @Override
    protected int getLineColor() {
        return 0x4Dc4a8e3;
    }

    @Override
    protected int getGlowColor() {
        return 0xc4a8e3;
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, SlotPosition pos, String abilityId, boolean isHovered, int index) {
        int selectedIndex = ClientData.getSelectedAbility();
        boolean isSelected = index == selectedIndex;

        int size = isHovered ? SLOT_HOVER_SIZE : SLOT_SIZE;
        int x = pos.x - size / 2;
        int y = pos.y - size / 2;

        guiGraphics.fill(x, y, x + size, y + size, 0xCC000000);

        int borderColor = isSelected ? 0xFFc4a8e3 : (isHovered ? 0xFFc4a8e3 : 0xFF9989ab);
        int borderWidth = isSelected ? 2 : 1;

        if (isHovered || isSelected) {
            int glowColor = isSelected ? 0x60c4a8e3 : 0x409989ab;
            guiGraphics.fill(x - 1, y - 1, x + size + 1, y + size + 1, glowColor);
        }

        guiGraphics.fill(x, y, x + size, y + borderWidth, borderColor);
        guiGraphics.fill(x, y + size - borderWidth, x + size, y + size, borderColor);
        guiGraphics.fill(x, y, x + borderWidth, y + size, borderColor);
        guiGraphics.fill(x + size - borderWidth, y, x + size, y + size, borderColor);

        // Parse sub-ability index from abilityId (e.g. "someid:2")
        int colonIdx = abilityId.lastIndexOf(':');
        int subIndex = -1;
        String baseId = abilityId;
        if (colonIdx >= 0) {
            try {
                subIndex = Integer.parseInt(abilityId.substring(colonIdx + 1));
                baseId = abilityId.substring(0, colonIdx);
            } catch (NumberFormatException ignored) {}
        }

        try {
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/abilities/" + baseId + ".png");
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
            guiGraphics.fill(x + padding, y + padding, x + size - padding, y + size - padding, 0xFF8B6914);
        }

        // Draw sub-index badge in the bottom-right corner of the slot
        if (subIndex >= 0) {
            String badge = String.valueOf(subIndex);
            int bx = pos.x + size / 2 - net.minecraft.client.Minecraft.getInstance().font.width(badge) - 2;
            int by = pos.y + size / 2 - net.minecraft.client.Minecraft.getInstance().font.lineHeight + 1;
            guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, badge, bx, by, 0xFFFFFF, false);
        }

        if (isHovered) {
            Ability ability = LOTMCraft.abilityHandler.getById(baseId);
            if (ability != null) {
                Component name;
                if (subIndex >= 0 && ability instanceof SelectableAbility sa) {
                    String[] names = sa.getAbilityNamesCopy();
                    if (subIndex < names.length) {
                        String pathway = BeyonderData.getPathway(ClientHandler.getPlayer());
                        int color = BeyonderData.pathwayInfos.containsKey(pathway) ? BeyonderData.pathwayInfos.get(pathway).color() : 0xFFFFFF;
                        name = Component.translatable(names[subIndex]).withStyle(ChatFormatting.BOLD).withColor(color);
                    } else {
                        name = ability.getNameFormatted(ClientHandler.getPlayer());
                    }
                } else {
                    name = ability.getNameFormatted(ClientHandler.getPlayer());
                }
                int textWidth = net.minecraft.client.Minecraft.getInstance().font.width(name);
                guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, name,
                        pos.x - textWidth / 2, pos.y - size / 2 - 12, 0xFFFFFF, true);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredSlot != -1) {
            if (ClientData.sharedAbilityMode) {
                ClientData.setSelectedSharedAbility(hoveredSlot);
            } else {
                PacketHandler.sendToServer(new UpdateSelectedAbilityPacket(hoveredSlot));
                ClientData.setAbilityWheelData(
                        new java.util.ArrayList<>(getAbilities()),
                        hoveredSlot
                );
            }

            KeyInputHandler.holdAbilityWheelCooldownTicks = 12;
            this.onClose();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        if (KeyInputHandler.wasWheelOpenedWithHold && hoveredSlot != -1) {
            PacketHandler.sendToServer(new UpdateSelectedAbilityPacket(hoveredSlot));
            if (ClientData.sharedAbilityMode) {
                ClientData.setSelectedSharedAbility(hoveredSlot);
            } else {
                ClientData.setAbilityWheelData(
                        new java.util.ArrayList<>(getAbilities()),
                        hoveredSlot
                );
            }
        }
        ClientData.sharedAbilityMode = false;
        super.onClose();
    }
}