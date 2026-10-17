package de.jakob.lotm.gui.custom.spirit_housing;

import de.jakob.lotm.beyonders.abilities.darkness.SpiritCommandingAbility;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.HashMap;
import java.util.Map;

public class SpiritManageScreen extends net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<SpiritManageMenu> {
    private final Map<String, LivingEntity> shown = new HashMap<>();

    public SpiritManageScreen(SpiritManageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 150;
        this.inventoryLabelY = 10000;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF0101018);
        graphics.renderOutline(leftPos, topPos, imageWidth, imageHeight, 0xFF6a4a78);
        graphics.drawString(font, Component.translatable("ability.lotmcraft.spirit_commanding.head"), leftPos + 8, topPos + 22, 0xFFC8B4D4, false);
        graphics.drawString(font, Component.translatable("ability.lotmcraft.spirit_commanding.torso"), leftPos + 8, topPos + 42, 0xFFC8B4D4, false);
        graphics.drawString(font, Component.translatable("ability.lotmcraft.spirit_commanding.arms"), leftPos + 8, topPos + 62, 0xFFC8B4D4, false);
        graphics.drawString(font, Component.translatable("ability.lotmcraft.spirit_commanding.legs"), leftPos + 8, topPos + 82, 0xFFC8B4D4, false);
        graphics.drawString(font, Component.translatable("ability.lotmcraft.spirit_commanding.bin"), leftPos + 8, topPos + 102, 0xFF886666, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        for (Slot slot : menu.slots) {
            String type = SpiritCommandingAbility.typeOf(slot.getItem());
            LivingEntity creature = creature(type);
            if (creature == null) continue;
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, x, y, x + 16, y + 16, 10, 0.1f, x + 8, y + 8, creature);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private LivingEntity creature(String type) {
        if (type.isEmpty() || minecraft == null || minecraft.level == null) return null;
        LivingEntity cached = shown.get(type);
        if (cached != null) return cached;
        var entityType = EntityType.byString(type);
        if (entityType.isEmpty() || !(entityType.get().create(minecraft.level) instanceof LivingEntity living)) return null;
        shown.put(type, living);
        return living;
    }
}
