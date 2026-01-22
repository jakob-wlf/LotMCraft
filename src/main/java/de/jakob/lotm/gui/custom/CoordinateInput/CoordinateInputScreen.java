package de.jakob.lotm.gui.custom.CoordinateInput;

import de.jakob.lotm.abilities.common.DivinationAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.SyncDreamDivinationCoordinatesPacket;
import de.jakob.lotm.network.packets.toServer.SyncTravelersDoorCoordinatesPacket;
import de.jakob.lotm.network.packets.toServer.TeleportPlayerToLocationPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class CoordinateInputScreen extends Screen {
    private EditBox xBox, yBox, zBox;

    private final LivingEntity entity;
    private final String use;
    
    public CoordinateInputScreen(LivingEntity entity, String use) {
        super(Component.literal("Enter Coordinates"));

        this.entity = entity;
        this.use = use;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // X coordinate input
        this.xBox = new EditBox(this.font, centerX - 60, centerY - 30, 120, 20, Component.literal("X - Coordinate"));
        this.xBox.setMaxLength(10);
        this.addRenderableWidget(this.xBox);

        // Y coordinate input
        this.yBox = new EditBox(this.font, centerX - 60, centerY - 5, 120, 20, Component.literal("Y - Coordinate"));
        this.yBox.setMaxLength(10);
        this.addRenderableWidget(this.yBox);

        // Z coordinate input
        this.zBox = new EditBox(this.font, centerX - 60, centerY + 20, 120, 20, Component.literal("Z - Coordinate"));
        this.zBox.setMaxLength(10);
        this.addRenderableWidget(this.zBox);

        // Confirm button
        Button confirmButton = Button.builder(Component.literal("Confirm"), this::onConfirm)
                .bounds(centerX - 50, centerY + 50, 100, 20)
                .build();
        this.addRenderableWidget(confirmButton);

    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Draw labels next to the input boxes
        graphics.drawString(this.font, "X:", centerX - 80, centerY - 25, 0xFFFFFF);
        graphics.drawString(this.font, "Y:", centerX - 80, centerY, 0xFFFFFF);
        graphics.drawString(this.font, "Z:", centerX - 80, centerY + 25, 0xFFFFFF);
    }
    
    private void onConfirm(Button button) {
        try {
            int x = Integer.parseInt(this.xBox.getValue());
            int y = Integer.parseInt(this.yBox.getValue());
            int z = Integer.parseInt(this.zBox.getValue());

            switch (use) {
                case "travelers_door" -> PacketHandler.sendToServer(new SyncTravelersDoorCoordinatesPacket(x, y, z, entity.getId()));
                case "dream_divination" -> {
                    if(!(entity instanceof Player player))
                        return;
                    PacketHandler.sendToServer(new SyncDreamDivinationCoordinatesPacket(x, y, z, entity.getId()));
                    DivinationAbility.performDreamDivination(player.level(), player, new BlockPos(x, y, z));
                }
                case "teleportation" -> PacketHandler.sendToServer(new TeleportPlayerToLocationPacket(x, y, z, entity.getId()));
            }

            this.onClose();
            
        } catch (NumberFormatException ignored) {
        }
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}