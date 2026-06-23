package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.RequestActiveStatusOfAbilityC2SPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.data.ClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class AbilityIconRenderer {

    public static boolean isDeactivated = false;

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "ability_icon_overlay"), (guiGraphics, deltaTracker) -> {
            renderText(guiGraphics);
        });
    }

    private final static int hotbarWidth = 182;
    private final static int hotbarheight = 22;
    private final static int WIDTH = 22;
    private final static int HEIGHT = 22;

    private final static int ICON_WIDTH = 18;
    private final static int ICON_HEIGHT = 18;

    private final static ResourceLocation backgroundTexture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/ability_frame.png");
    private final static ResourceLocation foregroundTexture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/ability_frame_foreground.png");

    private static void renderText(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;

        if(!BeyonderData.pathwayInfos.containsKey(ClientBeyonderCache.getPathway(mc.player.getUUID())))
            return;

        if(!ClientBeyonderCache.isBeyonder(mc.player.getUUID())){
            return;
        }

        int selectedAbilityIndex = ClientData.getSelectedAbility();
        if(selectedAbilityIndex < 0 || selectedAbilityIndex >= ClientData.getAbilityWheelAbilities().size()) {
            return;
        }

        String selectedAbilityId = ClientData.getAbilityWheelAbilities().get(selectedAbilityIndex);
        Ability selectedAbility = LOTMCraft.abilityHandler.getById(selectedAbilityId.split(":")[0]);
        if(selectedAbility == null) {
            return;
        }

        PacketHandler.sendToServer(new RequestActiveStatusOfAbilityC2SPacket(selectedAbilityId.split(":")[0]));

        int screenWidth = mc.getWindow().getGuiScaledWidth();

        int hotbarStartX = (screenWidth - hotbarWidth) / 2;

        ItemStack offhandItem = mc.player.getItemInHand(InteractionHand.OFF_HAND);
        int x = offhandItem.isEmpty() ? hotbarStartX - WIDTH - 5 : hotbarStartX - WIDTH - 5 - 30;
        int y = mc.getWindow().getGuiScaledHeight() - (hotbarheight / 2) - (HEIGHT / 2);

        guiGraphics.blit(backgroundTexture, x, y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);

        int abilityIconX = x + 2;
        int abilityIconY = y + 2;

        guiGraphics.blit(selectedAbility.getTextureLocation(), abilityIconX, abilityIconY, 0, 0, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);

        int subIndex = getSubIndex(selectedAbilityId);
        if(subIndex != -1) {
            String badge = String.valueOf(subIndex);
            int bx = x + ICON_WIDTH - net.minecraft.client.Minecraft.getInstance().font.width(badge);
            int by = y + ICON_HEIGHT - net.minecraft.client.Minecraft.getInstance().font.lineHeight / 2;
            guiGraphics.fill(bx, by, bx + net.minecraft.client.Minecraft.getInstance().font.width(badge), by + net.minecraft.client.Minecraft.getInstance().font.lineHeight, 0xAA000000);
            guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, badge, bx, by, 0xFFFFFF, false);
        }

        if(isDeactivated) {
            guiGraphics.fill(abilityIconX, abilityIconY, abilityIconX + ICON_WIDTH, abilityIconY + ICON_HEIGHT, 0x55000000);
        }

        guiGraphics.blit(foregroundTexture, x, y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
    }

    private static int getSubIndex(String s) {
        String[] parts = s.split(":");
        if (parts.length < 2) return -1;
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
