package de.jakob.lotm.gui.custom.Messages;


import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class MessagesScreen extends AbstractContainerScreen<MessagesMenu> {
    private final LivingEntity entity;

    public MessagesScreen(LivingEntity entity) {
        super(Component.literal("Messages List:"));

        this.entity = entity;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {

    }
}
