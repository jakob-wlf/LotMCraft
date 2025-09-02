package de.jakob.lotm.overlay;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class SelectedAbilityRenderer {
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "selected_ability_overlay"), (guiGraphics, deltaTracker) -> {
            renderText(guiGraphics);
        });
    }

    private static void renderText(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();

        int x = (screenWidth / 2) + (screenWidth / 6) + 30;
        int y = mc.getWindow().getGuiScaledHeight() - 45;

        if(!BeyonderData.pathwayInfos.containsKey(ClientBeyonderCache.getPathway(mc.player.getUUID())))
            return;

        int grayColor = 0xFF808080;
        int color = BeyonderData.pathwayInfos.get(ClientBeyonderCache.getPathway(mc.player.getUUID())).color();

        if(ClientBeyonderCache.isBeyonder(mc.player.getUUID())){
            if (mc.player.getMainHandItem().getItem() instanceof SelectableAbilityItem abilityItem && abilityItem.canUse(mc.player, mc.player.getMainHandItem())) {
                Component message1 = Component.translatable("lotm.selected").append(":").withColor(grayColor);
                Component message2 = Component.translatable(abilityItem.getSelectedAbility(mc.player)).withColor(color);
                guiGraphics.drawString(mc.font, message1, x, y, grayColor);
                guiGraphics.drawString(mc.font, message2, x, y + mc.font.lineHeight + 2, color);
            }
        }
    }
}
