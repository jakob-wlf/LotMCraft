package de.jakob.lotm.gui.custom;

import de.jakob.lotm.abilities.common.DivinationAbility;
import de.jakob.lotm.abilities.fool.ShapeShiftingAbility;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class UsernameInputScreen extends Screen {
    private EditBox xBox;

    private final LivingEntity entity;

    public UsernameInputScreen(LivingEntity entity) {
        super(Component.literal("Enter username you want to change to"));

        this.entity = entity;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.xBox = new EditBox(this.font, centerX - 60, centerY - 30, 120, 20, Component.literal("X - Coordinate"));
        this.xBox.setMaxLength(1000);
        this.addRenderableWidget(this.xBox);

        // Confirm button
        Button confirmButton = Button.builder(Component.literal("Confirm"), this::onConfirm)
                .bounds(centerX - 50, centerY + 50, 100, 20)
                .build();
        this.addRenderableWidget(confirmButton);

        if(ShapeShiftingAbility.attemptingToChangeSkin.containsKey(entity.getUUID())) {
            this.onClose();
        }
    }
    
    private void onConfirm(Button button) {
        try {
            String username = this.xBox.getValue();

            ShapeShiftingAbility.changeSkin(username, entity);

            this.onClose();
            
        } catch (NumberFormatException ignored) {
        }
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}