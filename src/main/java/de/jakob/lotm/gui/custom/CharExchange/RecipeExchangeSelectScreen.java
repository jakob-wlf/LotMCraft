package de.jakob.lotm.gui.custom.CharExchange;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.RequestRecipeExchangePacket;
import de.jakob.lotm.network.packets.toServer.RequestRecipePathExchangePacket;
import de.jakob.lotm.beyonders.potions.PotionRecipeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Step 1 of the Recipe Exchange UI.
 *
 * Scans the player's inventory for recipe items and lets them pick one to sacrifice.
 */
@OnlyIn(Dist.CLIENT)
public class RecipeExchangeSelectScreen extends Screen {

    private static final int PANEL_W   = 400;
    private static final int PANEL_H   = 300;
    private static final int LIST_X_PAD = 20;
    private static final int ROW_H      = 22;

    private static final int COL_BG      = 0xF0080014;
    private static final int COL_OUTLINE = 0xFF4400AA;
    private static final int COL_TITLE   = 0xFFCC88FF;
    private static final int COL_FLAVOUR = 0xFFAA88CC;
    private static final int COL_DIVIDER = 0xFF330088;

    public enum Mode { EXCHANGE, PATH_EXCHANGE }

    private final List<RecipeEntry> entries = new ArrayList<>();
    private final Mode mode;

    private record RecipeEntry(int slot, ItemStack stack, String displayName) {}

    public RecipeExchangeSelectScreen(Mode mode) {
        super(Component.literal(mode == Mode.PATH_EXCHANGE ? "Recipe Path Exchange" : "Recipe Exchange"));
        this.mode = mode;
    }

    @Override
    protected void init() {
        super.init();

        entries.clear();
        var inventory = Minecraft.getInstance().player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof PotionRecipeItem) {
                entries.add(new RecipeEntry(i, stack.copy(), stack.getHoverName().getString()));
            }
        }

        int cx      = (width  - PANEL_W) / 2;
        int cy      = (height - PANEL_H) / 2;
        int listTop = cy + 80;

        for (int idx = 0; idx < entries.size(); idx++) {
            RecipeEntry entry = entries.get(idx);
            int rowY = listTop + idx * ROW_H;
            final int finalSlot = entry.slot();
            addRenderableWidget(Button.builder(
                    Component.literal(entry.displayName()).withStyle(ChatFormatting.AQUA),
                    btn -> onRecipeSelected(finalSlot))
                    .bounds(cx + LIST_X_PAD, rowY, PANEL_W - LIST_X_PAD * 2, ROW_H - 2)
                    .build());
        }

        addRenderableWidget(Button.builder(
                Component.literal("Cancel").withStyle(ChatFormatting.GRAY),
                btn -> onClose())
                .bounds(cx + PANEL_W / 2 - 40, cy + PANEL_H - 28, 80, 20)
                .build());
    }

    private void onRecipeSelected(int slot) {
        var inv = Minecraft.getInstance().player.getInventory();
        inv.setItem(slot, ItemStack.EMPTY);

        if (mode == Mode.PATH_EXCHANGE) {
            PacketHandler.sendToServer(new RequestRecipePathExchangePacket(slot));
        } else {
            PacketHandler.sendToServer(new RequestRecipeExchangePacket(slot));
        }
        onClose();
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xBB000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        int cx = (width  - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;

        g.fill(cx, cy, cx + PANEL_W, cy + PANEL_H, COL_BG);
        g.renderOutline(cx, cy, PANEL_W, PANEL_H, COL_OUTLINE);

        String titleStr = mode == Mode.PATH_EXCHANGE ? "✦ Recipe Path Exchange ✦" : "✦ Recipe Exchange ✦";
        g.drawCenteredString(font,
                Component.literal(titleStr).withStyle(ChatFormatting.BOLD),
                cx + PANEL_W / 2, cy + 10, COL_TITLE);

        String flavour = mode == Mode.PATH_EXCHANGE
                ? "Sacrifice a recipe. Receive the same sequence from a different path."
                : "Sacrifice a recipe. Receive one sequence higher from a different path.";
        g.drawCenteredString(font,
                Component.literal(flavour).withStyle(ChatFormatting.ITALIC),
                cx + PANEL_W / 2, cy + 26, COL_FLAVOUR);

        g.fill(cx + 10, cy + 38, cx + PANEL_W - 10, cy + 39, COL_DIVIDER);

        if (entries.isEmpty()) {
            g.drawCenteredString(font,
                    Component.literal("No recipes in your inventory.").withStyle(ChatFormatting.RED),
                    cx + PANEL_W / 2, cy + 55, 0xFFFF4444);
        } else {
            g.drawString(font,
                    Component.literal("Select a recipe to sacrifice:").withStyle(ChatFormatting.GRAY),
                    cx + LIST_X_PAD, cy + 55, 0xFFAAAAAA, false);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
