package de.jakob.lotm.gui.custom.Introspect;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class IntrospectScreen extends AbstractContainerScreen<IntrospectMenu> {
    private ResourceLocation containerBackground;

    public IntrospectScreen(IntrospectMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.containerBackground = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/introspect.png");

        this.imageHeight = 192;
        this.imageWidth = 192;
    }

    public void updateScreen(String pathway, int sequence) {
        this.containerBackground = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/introspect.png");
    }
    
    @Override
    protected void init() {
        super.init();

        if(this.minecraft == null) return;

        updateScreen(menu.getPathway(), menu.getSequence());
    }

    public void updateMenuData(int sequence, String pathway, float digestionProgress) {
        this.menu.updateData(sequence, pathway, digestionProgress);
    }

    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updateScreen(menu.getPathway(), menu.getSequence());

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

        guiGraphics.blit(containerBackground, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        renderPathwaySymbol(guiGraphics, x, y);
        //renderPathwayLabel(guiGraphics, x, y);
        renderSequenceNumber(guiGraphics, x, y);
        renderSequenceName(guiGraphics, x, y);
        renderDigestionLabel(guiGraphics, x, y);
        renderDigestionProgress(guiGraphics, x, y);
        renderPassiveAbilitiesText(guiGraphics, x, y);
        RenderSystem.disableBlend();
    }

    private void renderPassiveAbilitiesText(GuiGraphics guiGraphics, int x, int y) {
        Component passiveAbilitiesText = Component.translatable("lotm.passive_abilities").withStyle(ChatFormatting.BOLD);

        int color = 0xDDDDDD;

        int textY = 123;
        int textX = 7;

        guiGraphics.drawString(this.font, passiveAbilitiesText, x + textX, y + textY, color, true);
    }

    private void renderDigestionLabel(GuiGraphics guiGraphics, int x, int y) {
        Component digestionText = Component.translatable("lotm.digestion").withStyle(ChatFormatting.BOLD);

        int color = 0xDDDDDD;

        int textY = 76;
        int textX = 7;

        guiGraphics.drawString(this.font, digestionText, x + textX, y + textY, color, true);
    }

    private void renderDigestionProgress(GuiGraphics guiGraphics, int x, int y) {
        int barStartY = 93;
        int barEndY = 104;

        int barStartX = 3;
        int barEndX = (int) (115 * menu.getDigestionProgress()) + barStartX;

        int color = 0xFFe8bb68;
        int color2 = 0xFFF5ad2a;

        guiGraphics.fillGradient(barStartX + x, barStartY + y, barEndX + x, barEndY + y, color, color2);
    }

    private void renderPathwayLabel(GuiGraphics guiGraphics, int x, int y) {
        String pathway = menu.getPathway();

        Component pathwayTitle = Component.literal(BeyonderData.pathwayInfos.get(pathway).getName() + "-").append(Component.translatable("lotm.pathway")).withStyle(ChatFormatting.BOLD);

        int color = BeyonderData.pathwayInfos.get(pathway).color();

        int textX = 0;
        int textY = -12;

        guiGraphics.drawString(this.font, pathwayTitle, x + textX, y + textY, color, true);
    }

    private void renderSequenceNumber(GuiGraphics guiGraphics, int x, int y) {
        int color = 0xDDDDDD;

        Component sequenceText = Component.translatable("lotm.sequence").append(": ").append(Component.literal(menu.getSequence() + "")).withStyle(ChatFormatting.BOLD);

        int textX = 7;
        int textY = 7;

        guiGraphics.drawString(this.font, sequenceText, x + textX, y + textY, color, true);
    }

    private void renderSequenceName(GuiGraphics guiGraphics, int x, int y) {
        int color = BeyonderData.pathwayInfos.get(menu.getPathway()).color();

        Component sequenceNameText = Component.literal(BeyonderData.getSequenceName(menu.getPathway(), menu.getSequence()));

        int textX = 7;
        int textY = 28;

        guiGraphics.drawString(this.font, sequenceNameText, x + textX, y + textY, color, true);
    }


    private void renderPathwaySymbol(GuiGraphics guiGraphics, int x, int y) {
        ResourceLocation iconTexture = ResourceLocation.fromNamespaceAndPath(
                LOTMCraft.MOD_ID, "textures/gui/icons/" + menu.getPathway() + "_icon.png"
        );

        int iconX = 126;
        int iconY = 3;
        int iconWidth = 62;
        int iconHeight = 62;

        int screenX = x + iconX;
        int screenY = y + iconY;

        guiGraphics.blit(iconTexture, screenX, screenY, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight);
    }


    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title.getString(), this.titleLabelX, this.titleLabelY, 0xCCCCCC, true);
    }
}