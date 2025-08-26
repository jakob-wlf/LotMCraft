package de.jakob.lotm.gui.custom;

import de.jakob.lotm.abilities.common.DivinationAbility;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class CoordinateInputScreen extends Screen {
    private EditBox xBox, yBox, zBox;

    private final LivingEntity entity;
    
    public CoordinateInputScreen(LivingEntity entity) {
        super(Component.literal("Enter Coordinates"));

        this.entity = entity;
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

        if(DivinationAbility.dreamDivinationUsers.containsKey(entity.getUUID())) {
            this.onClose();
        }
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

            if(!DivinationAbility.dreamDivinationUsers.containsKey(entity.getUUID())) {
                DivinationAbility.dreamDivinationUsers.put(entity.getUUID(), BlockPos.containing(x, y ,z));
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