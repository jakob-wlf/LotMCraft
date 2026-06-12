package de.jakob.lotm.gui.custom.Recipe;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.OpenRecipeMenuPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RecipeScreen extends AbstractContainerScreen<RecipeMenu> {
    private final ResourceLocation containerBackground;

    public RecipeScreen(RecipeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.containerBackground = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/recipe.png");

        this.imageHeight = 134;
        this.imageWidth = 216;
    }
    
    @Override
    protected void init() {
        super.init();

        String pathway  = this.menu.getPathway();
        int    sequence = this.menu.getSequence();

        // Only show nav arrows when opened from a BlasphemyCard
        if (!this.menu.isFromCard()) return;

        int bx = this.leftPos;
        int by = this.topPos + this.imageHeight - 20;

        // ◄ Previous (sequence+1, going toward seq 9 = lowest rank)
        if (sequence < 9) {
            addRenderableWidget(Button.builder(
                    Component.literal("◄"),
                    btn -> PacketHandler.sendToServer(new OpenRecipeMenuPacket(sequence + 1, pathway, true))
            ).pos(bx + 2, by).size(20, 16).build());
        }

        // ► Next (sequence-1, going toward seq 1 = highest rank)
        if (sequence > 1) {
            addRenderableWidget(Button.builder(
                    Component.literal("►"),
                    btn -> PacketHandler.sendToServer(new OpenRecipeMenuPacket(sequence - 1, pathway, true))
            ).pos(bx + this.imageWidth - 22, by).size(20, 16).build());
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(containerBackground, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title.getString(), this.titleLabelX, this.titleLabelY, 0x999999, true);
    }
}