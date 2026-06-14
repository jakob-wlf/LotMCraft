package de.jakob.lotm.gui.custom.Trades;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.ExecuteBeyonderTradePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BeyonderTradeScreen extends AbstractContainerScreen<BeyonderTradeMenu> {

    private static final int TRADE_ROW_HEIGHT = 26;
    private static final int TRADE_LIST_START_Y = 28;
    private static final int TRADE_LIST_START_X = 8;

    private static final int SLOT_SIZE = 18;
    private static final int ARROW_WIDTH = 22;

    private final List<BeyonderNPCEntity.TradeEntry> trades;

    public BeyonderTradeScreen(BeyonderTradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.trades = menu.getNpc() != null
                ? menu.getNpc().getCurrentTrades()
                : List.of();

        int tradeCount = Math.max(1, trades.size());
        this.imageHeight = TRADE_LIST_START_Y + tradeCount * TRADE_ROW_HEIGHT + 4 + 76 + 14;
        this.imageWidth = 196;

    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;

        this.inventoryLabelX = (this.imageWidth - this.font.width(this.playerInventoryTitle)) / 2;
        this.inventoryLabelY = this.imageHeight - 94;

        int invStartY = this.imageHeight - 85;
        repositionPlayerInventorySlots(invStartY);

        for (int i = 0; i < trades.size(); i++) {
            final int tradeIndex = i;
            int rowY = topPos + TRADE_LIST_START_Y + i * TRADE_ROW_HEIGHT;

            int buttonX = leftPos + TRADE_LIST_START_X + (SLOT_SIZE * 2) + ARROW_WIDTH + SLOT_SIZE + 8;
            int buttonY = rowY;

            this.addRenderableWidget(Button.builder(Component.translatable("lotm.accept").withColor(0xb4fa8e), btn -> onConfirmTrade(tradeIndex)).bounds(buttonX, buttonY, 45, 18).build());
        }
    }

    private void repositionPlayerInventorySlots(int invStartY) {
        int inputSlotCount = menu.getInputSlotsContainer().getContainerSize();

        for (int i = 0; i < menu.slots.size(); i++) {
            net.minecraft.world.inventory.Slot slot = menu.slots.get(i);
            if (i < inputSlotCount) {
                int tradeIndex = i / 2;
                boolean isSlotB = (i % 2) == 1;

                int rowY = TRADE_LIST_START_Y + tradeIndex * TRADE_ROW_HEIGHT;
                int slotX = TRADE_LIST_START_X + (isSlotB ? SLOT_SIZE : 0);

                setSlotPosition(slot, slotX + 1, rowY + 1);
            } else {
                int relativeIndex = i - inputSlotCount;
                if (relativeIndex < 27) {
                    int row = relativeIndex / 9;
                    int col = relativeIndex % 9;
                    setSlotPosition(slot, 8 + col * 18, invStartY + row * 18);
                } else {
                    int col = relativeIndex - 27;
                    setSlotPosition(slot, 8 + col * 18, invStartY + 58);
                }
            }
        }
    }

    private void setSlotPosition(net.minecraft.world.inventory.Slot slot, int x, int y) {
        try {
            var xField = net.minecraft.world.inventory.Slot.class.getDeclaredField("x");
            var yField = net.minecraft.world.inventory.Slot.class.getDeclaredField("y");
            xField.setAccessible(true);
            yField.setAccessible(true);
            xField.setInt(slot, x);
            yField.setInt(slot, y);
        } catch (Exception ignored) {}
    }

    private void onConfirmTrade(int tradeIndex) {
        PacketHandler.sendToServer(new ExecuteBeyonderTradePacket(menu.getNpcEntityId(), tradeIndex));
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = leftPos;
        int y = topPos;

        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xCC000000);
        guiGraphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF1A1A1A);

        for (int i = 0; i < trades.size(); i++) {
            BeyonderNPCEntity.TradeEntry trade = trades.get(i);
            int rowY = y + TRADE_LIST_START_Y + i * TRADE_ROW_HEIGHT;
            int rowX = x + TRADE_LIST_START_X;

            drawSlotBackground(guiGraphics, rowX, rowY);

            if (!trade.costB().isEmpty()) {
                drawSlotBackground(guiGraphics, rowX + SLOT_SIZE, rowY);
            }

            int arrowX = rowX + SLOT_SIZE * 2 + 2;
            guiGraphics.drawString(this.font, "->", arrowX, rowY + 5, 0x9E9E9E, false);

            int resultX = arrowX + ARROW_WIDTH;
            drawSlotBackground(guiGraphics, resultX, rowY);
        }
    }

    private void drawSlotBackground(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF333333);
        guiGraphics.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xFF1A1A1A);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        for (int i = 0; i < trades.size(); i++) {
            BeyonderNPCEntity.TradeEntry trade = trades.get(i);
            int rowY = topPos + TRADE_LIST_START_Y + i * TRADE_ROW_HEIGHT;
            int rowX = leftPos + TRADE_LIST_START_X;

            int arrowX = rowX + SLOT_SIZE * 2 + 2;
            int resultX = arrowX + ARROW_WIDTH;

            ItemStack result = trade.result();
            if (!result.isEmpty()) {
                guiGraphics.renderItem(result, resultX + 1, rowY + 1);
                guiGraphics.renderItemDecorations(this.font, result, resultX + 1, rowY + 1);
            }

            if (menu.getInputSlotsContainer().getItem(BeyonderTradeMenu.getInputSlotAIndex(i)).isEmpty()) {
                ItemStack costA = trade.costA();
                System.out.println("Cost A for trade " + i + ": " + costA);
                if (!costA.isEmpty()) {
                    guiGraphics.pose().pushPose();
                    RenderSystem.setShaderColor(1f, 1f, 1f, 0.35f);
                    guiGraphics.renderFakeItem(costA, rowX + 1, rowY + 1);
                    RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                    guiGraphics.pose().popPose();
                }
            }
            if (!trade.costB().isEmpty()
                    && menu.getInputSlotsContainer().getItem(BeyonderTradeMenu.getInputSlotBIndex(i)).isEmpty()) {
                ItemStack costB = trade.costB();
                guiGraphics.pose().pushPose();
                RenderSystem.setShaderColor(1f, 1f, 1f, 0.35f);
                guiGraphics.renderFakeItem(costB, rowX + SLOT_SIZE + 1, rowY + 1);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                guiGraphics.pose().popPose();
            }
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}