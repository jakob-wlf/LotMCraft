package de.jakob.lotm.gui.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.OpenAbilitySelectionPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

public class AbilitySelectionScreen extends AbstractContainerScreen<AbilitySelectionMenu> {
    private ResourceLocation containerBackground;
    
    private Button leftButton;
    private Button rightButton;

    public AbilitySelectionScreen(AbilitySelectionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.containerBackground = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/ability_selection_gui.png");

        // Calculate screen size based on rows
        this.imageHeight = 130;
        this.inventoryLabelY = this.imageHeight - 48;
    }

    public void updateScreen(String pathway) {
        if(BeyonderData.tempImplementedPathwayGUIs.contains(pathway))
            this.containerBackground = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/ability_selection_gui_" + pathway + ".png");
        else
            this.containerBackground = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/ability_selection_gui.png");
    }
    
    @Override
    protected void init() {
        super.init();

        if(this.minecraft == null) return;

        updateScreen(menu.getPathway());

        Player player = this.minecraft.player;
        if(!ClientBeyonderCache.isBeyonder(player.getUUID()))
            return;

        int sequence = this.menu.getSequence();
        String pathway = this.menu.getPathway();
        
        // Left button
        if(sequence < 9) {
            this.leftButton = Button.builder(
                            Component.literal("<"),
                            button -> {
                                int newSequence = sequence + 1;
                                PacketHandler.sendToServer(new OpenAbilitySelectionPacket(newSequence, pathway));
                            })

                    .bounds(this.leftPos - 25, this.topPos + 20, 20, 20)
                    .build();
            this.addRenderableWidget(leftButton);
        }


        if(sequence > ClientBeyonderCache.getSequence(player.getUUID())) {
            // Right button
            this.rightButton = Button.builder(
                            Component.literal(">"),
                            button -> {
                                int newSequence = sequence - 1;
                                PacketHandler.sendToServer(new OpenAbilitySelectionPacket(newSequence, pathway));
                            })
                    .bounds(this.leftPos + this.imageWidth + 5, this.topPos + 20, 20, 20)
                    .build();
            this.addRenderableWidget(rightButton);
        }

        int increment = -22;


        int maxSequence = ClientBeyonderCache.getSequence(player.getUUID());
        for(int i = 9; i > maxSequence - 1; i--) {
            if(i == sequence)
                continue;

            int newSequence = i;
            Button b = Button.builder(
                            Component.literal(i + ""),
                            button -> {
                                PacketHandler.sendToServer(new OpenAbilitySelectionPacket(newSequence, pathway));
                            })
                    .bounds(this.leftPos + this.imageWidth + 30, this.topPos + increment + 20, 20, 20)
                    .build();
            this.addRenderableWidget(b);
            increment += 22;
        }
    }

    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updateScreen(menu.getPathway());

        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    public void updateMenuData(int sequence, String pathway) {
        Minecraft mc = Minecraft.getInstance();
        long windowHandle = mc.getWindow().getWindow();

        // Get current mouse position
        double[] xPos = new double[1];
        double[] yPos = new double[1];
        GLFW.glfwGetCursorPos(windowHandle, xPos, yPos);

        // Update the menu's data
        this.menu.updateData(sequence, pathway);
        // Recreate buttons with new data
        this.clearWidgets();
        this.init();

        GLFW.glfwSetCursorPos(windowHandle, xPos[0], yPos[0]);
    }
    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        // Bottom part (player inventory background)
        guiGraphics.blit(containerBackground, x, y, 0, 0, this.imageWidth, this.imageHeight);
        RenderSystem.disableBlend();
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title.getString(), this.titleLabelX, this.titleLabelY, 0xCCCCCC, true);
    }
}