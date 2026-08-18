package de.jakob.lotm.gui.custom.ritualistic_table;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public class RitualScreen extends AbstractContainerScreen<RitualMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/ritual/ritual_gui.png");

    private EditBox field1;
    private EditBox field2;
    private EditBox field3;
    private EditBox field4;

    public RitualScreen(RitualMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageWidth = 176;
        this.imageHeight = 260;
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2 + 12;

        field1 = new EditBox(this.font, x + 20, y + 66, 136, 16, Component.literal("Field 1"));
        field1.setMaxLength(32);
        field1.setHint(Component.literal("..."));
        this.addRenderableWidget(field1);

        field2 = new EditBox(this.font, x + 20, y + 83, 136, 16, Component.literal("Field 2"));
        field2.setMaxLength(32);
        field2.setHint(Component.literal("..."));
        this.addRenderableWidget(field2);

        field3 = new EditBox(this.font, x + 20, y + 100, 136, 16, Component.literal("Field 3"));
        field3.setMaxLength(32);
        field3.setHint(Component.literal("..."));
        this.addRenderableWidget(field3);

        this.addRenderableWidget(Button.builder(Component.literal("Start Ritual"), button -> {
                    // no functionality yet
                })
                .bounds(x + 38, y + 120, 100, 20)
                .build());
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Always let Escape close the screen, regardless of what's focused
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        if (field1.isFocused() && field1.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (field2.isFocused() && field2.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (field3.isFocused() && field3.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (field4.isFocused() && field4.keyPressed(keyCode, scanCode, modifiers)) return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}