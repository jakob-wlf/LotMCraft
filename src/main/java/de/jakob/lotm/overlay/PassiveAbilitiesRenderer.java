package de.jakob.lotm.overlay;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class PassiveAbilitiesRenderer {
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "passive_abilities_overlay"), (guiGraphics, deltaTracker) -> {
            renderAbilities(guiGraphics);
        });
    }

    private static final int SPACING = 4;

    private static void renderAbilities(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int x = 5;
        int y = 5;

        if (LOTMCraft.showPassiveAbilitiesKey != null && LOTMCraft.showPassiveAbilitiesKey.isDown()) {
            for (var itemHolder : PassiveAbilityHandler.ITEMS.getEntries()) {
                if (itemHolder.get() instanceof PassiveAbilityItem abilityItem) {
                    if (abilityItem.shouldApplyTo(mc.player)) {
                        guiGraphics.pose().pushPose();
                        guiGraphics.pose().scale(1.5f, 1.5f, 1.5f);

                        int scaledX = (int) (x / 1.5f);
                        int scaledY = (int) (y / 1.5f);

                        guiGraphics.renderItem(
                                new ItemStack(itemHolder.get()),
                                scaledX, scaledY
                        );

                        guiGraphics.pose().popPose();

                        x += 24 + SPACING;
                    }
                }
            }
        }
    }
}
