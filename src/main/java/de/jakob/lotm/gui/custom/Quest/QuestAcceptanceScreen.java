package de.jakob.lotm.gui.custom.Quest;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.QuestAcceptanceResponsePacket;
import de.jakob.lotm.quest.Quest;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QuestAcceptanceScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/gui/quest_acceptance.png");
    
    private static final int WINDOW_WIDTH = 256;
    private static final int WINDOW_HEIGHT = 200;
    
    private final String questId;
    private final Component questName;
    private final Component questDescription;
    private final Component questLore;
    private final List<ItemStack> rewards;
    private final float digestionReward;
    private final int questSequence;
    
    private int leftPos;
    private int topPos;
    
    public QuestAcceptanceScreen(String questId, Component questName, Component questDescription, 
                                  List<ItemStack> rewards, float digestionReward, int questSequence) {
        super(Component.translatable("lotm.quest.acceptance.title"));
        this.questId = questId;
        this.questName = questName;
        this.questDescription = questDescription;
        this.questLore = Component.translatable("lotm.quest.impl." + questId + ".lore");
        this.rewards = rewards;
        this.digestionReward = digestionReward;
        this.questSequence = questSequence;
    }
    
    @Override
    protected void init() {
        super.init();
        
        this.leftPos = (this.width - WINDOW_WIDTH) / 2;
        this.topPos = (this.height - WINDOW_HEIGHT) / 2;
        
        // Accept button
        this.addRenderableWidget(Button.builder(
                Component.translatable("lotm.quest.acceptance.accept"),
                button -> acceptQuest())
                .bounds(this.leftPos + 30, this.topPos + WINDOW_HEIGHT - 30, 90, 20)
                .build());
        
        // Decline button
        this.addRenderableWidget(Button.builder(
                Component.translatable("lotm.quest.acceptance.decline"),
                button -> declineQuest())
                .bounds(this.leftPos + WINDOW_WIDTH - 120, this.topPos + WINDOW_HEIGHT - 30, 90, 20)
                .build());
    }
    
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        // Render window background
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        //guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        guiGraphics.fill(leftPos, topPos, leftPos + WINDOW_WIDTH, topPos + WINDOW_HEIGHT, 0xCC000000);
        guiGraphics.fill(leftPos + 2, topPos + 2, leftPos + WINDOW_WIDTH - 2, topPos + WINDOW_HEIGHT - 2, 0xFF1A1A1A);

        Iterator var5 = this.renderables.iterator();

        while(var5.hasNext()) {
            Renderable renderable = (Renderable)var5.next();
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        // Render quest title
        guiGraphics.drawCenteredString(this.font, this.questName, 
                this.leftPos + WINDOW_WIDTH / 2, this.topPos + 10, 0x4CAF50);
        
        // Render sequence requirement
        Component sequenceText = Component.translatable("lotm.quest.acceptance.sequence", questSequence > 9 ? "-" : questSequence)
                .withStyle(style -> style.withColor(0x9E9E9E));
        guiGraphics.drawCenteredString(this.font, sequenceText,
                this.leftPos + WINDOW_WIDTH / 2, this.topPos + 22, 0x9E9E9E);
        
        // Render lore section
        Component loreTitle = Component.translatable("lotm.quest.acceptance.lore")
                .withStyle(style -> style.withColor(0xFFB74D));
        guiGraphics.drawString(this.font, loreTitle, this.leftPos + 15, this.topPos + 38, 0xFFB74D);
        
        List<Component> loreLines = wrapText(questLore, WINDOW_WIDTH - 30);
        int yOffset = this.topPos + 50;
        for (Component line : loreLines) {
            guiGraphics.drawString(this.font, line, this.leftPos + 15, yOffset, 0xBDBDBD);
            yOffset += 10;
        }
        
        // Render description section
        yOffset += 5;
        Component descTitle = Component.translatable("lotm.quest.acceptance.description")
                .withStyle(style -> style.withColor(0x64B5F6));
        guiGraphics.drawString(this.font, descTitle, this.leftPos + 15, yOffset, 0x64B5F6);
        
        yOffset += 12;
        List<Component> descLines = wrapText(questDescription, WINDOW_WIDTH - 30);
        for (Component line : descLines) {
            guiGraphics.drawString(this.font, line, this.leftPos + 15, yOffset, 0xE0E0E0);
            yOffset += 10;
        }
        
        // Render rewards section
        yOffset += 8;
        Component rewardTitle = Component.translatable("lotm.quest.acceptance.rewards")
                .withStyle(style -> style.withColor(0xFFD54F));
        guiGraphics.drawString(this.font, rewardTitle, this.leftPos + 15, yOffset, 0xFFD54F);
        
        yOffset += 12;
        
        // Render item rewards
        int xOffset = this.leftPos + 20;
        for (ItemStack reward : rewards) {
            guiGraphics.renderItem(reward, xOffset, yOffset);
            guiGraphics.renderItemDecorations(this.font, reward, xOffset, yOffset);
            xOffset += 20;
            
            if (xOffset > this.leftPos + WINDOW_WIDTH - 40) {
                xOffset = this.leftPos + 20;
                yOffset += 20;
            }
        }
        
        // Render digestion reward if present
        if (digestionReward > 0) {
            if (xOffset > this.leftPos + 20) {
                yOffset += 20;
            } else {
                yOffset += 2;
            }
            
            Component digestionText = Component.translatable("lotm.quest.acceptance.digestion", 
                    String.format("%.1f%%", digestionReward * 100))
                    .withStyle(style -> style.withColor(0x9C27B0));
            guiGraphics.drawString(this.font, digestionText, this.leftPos + 20, yOffset, 0x9C27B0);
        }
    }
    
    private List<Component> wrapText(Component text, int maxWidth) {
        List<Component> lines = new ArrayList<>();
        String fullText = text.getString();
        StringBuilder currentLine = new StringBuilder();
        
        String[] words = fullText.split(" ");
        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            
            if (this.font.width(testLine) <= maxWidth) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(Component.literal(currentLine.toString()));
                    currentLine = new StringBuilder(word);
                } else {
                    lines.add(Component.literal(word));
                }
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(Component.literal(currentLine.toString()));
        }
        
        return lines;
    }
    
    private void acceptQuest() {
        PacketHandler.sendToServer(new QuestAcceptanceResponsePacket(questId, true));
        this.onClose();
    }
    
    private void declineQuest() {
        PacketHandler.sendToServer(new QuestAcceptanceResponsePacket(questId, false));
        this.onClose();
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}