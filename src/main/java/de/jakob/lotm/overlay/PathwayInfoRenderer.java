package de.jakob.lotm.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

//TODO: translate Sequence and Pathway Names
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class PathwayInfoRenderer {
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "pathway_infos"), (guiGraphics, deltaTracker) -> {
            renderAbilities(guiGraphics);
        });
    }

    @SubscribeEvent
    public static void onPostRenderOverlay(RenderGuiLayerEvent.Post event) {
        ResourceLocation name = event.getName();
        if (!name.equals(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "pathway_infos")))
            return;

        renderAbilitiesForeground(event.getGuiGraphics());
    }

    private static final int WIDTH = 304 / 2;
    private static final int HEIGHT = 456 / 2;

    private static final int BG_WIDTH = WIDTH;
    private static final int BG_HEIGHT = HEIGHT;
    private static final int ICON_SRC_SIZE = 1000;
    private static final float ICON_SCALE_DIVISOR = 3;
    private static final int ICON_OFFSET_DIVISOR = 16;
    private static final int FONT_LINE_OFFSET = 1;
    private static final int TEXT_LEFT_PADDING = 3;

    private static int textX = 0;

    private static void renderAbilities(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (!shouldRender(mc)) return;

        int x = calcCenterX(mc);
        int y = calcCenterY(mc);
        String pathway = ClientBeyonderCache.getPathway(mc.player.getUUID());
        int sequence = ClientBeyonderCache.getSequence(mc.player.getUUID());
        int color = BeyonderData.pathwayInfos.get(pathway).color();

        // Background
        renderBackground(guiGraphics, x, y);

        // Icon
        int iconSize = (int) (BG_WIDTH / ICON_SCALE_DIVISOR);
        int iconX = x + BG_WIDTH - (BG_WIDTH / ICON_OFFSET_DIVISOR) - iconSize;
        int iconY = y + (BG_WIDTH / ICON_OFFSET_DIVISOR);
        renderIcon(guiGraphics, pathway, iconX, iconY, iconSize);

        // Text
        renderPathwayText(guiGraphics, mc, pathway, iconX, iconY, iconSize, color);
        renderSequenceText(guiGraphics, mc, pathway, sequence, iconY, iconSize, color);

        //passive abilities
        renderPassiveAbilityIcons(guiGraphics, mc, iconY, iconSize, x);

        // Foreground
        guiGraphics.flush(); // Ensure text is rendered first
    }

    private static void renderAbilitiesForeground(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (!shouldRender(mc)) return;

        int x = calcCenterX(mc);
        int y = calcCenterY(mc);

        renderForeground(guiGraphics, x, y);
    }

    private static boolean shouldRender(Minecraft mc) {
        return mc.player != null && mc.level != null &&
                LOTMCraft.pathwayInfosKey != null && LOTMCraft.pathwayInfosKey.isDown() && ClientBeyonderCache.isBeyonder(mc.player.getUUID());
    }

    private static int calcCenterX(Minecraft mc) {
        return mc.getWindow().getGuiScaledWidth() / 2 - BG_WIDTH / 2;
    }

    private static int calcCenterY(Minecraft mc) {
        return (mc.getWindow().getGuiScaledHeight() - BG_HEIGHT) / 2;
    }

    private static void renderSequenceText(GuiGraphics g, Minecraft mc, String pathway, int sequence, int iconY, int iconSize, int color) {
        if(BeyonderData.pathwayInfos.get(pathway).sequenceNames().length <= sequence)
            return;

        Component sequenceString = Component.translatable("lotm.sequence").append(" " + sequence).append(":").withStyle(ChatFormatting.BOLD);
        Component sequenceName = Component.literal(BeyonderData.pathwayInfos.get(pathway).sequenceNames()[sequence]);

        boolean shouldSplit = mc.font.width(sequenceName) > 80;
        g.drawString(mc.font, sequenceString, textX, iconY + (int) Math.round(iconSize * 1.15), 0xFFf5edff);

        if(!shouldSplit) {
            g.drawString(mc.font, sequenceName, textX, iconY + (int) Math.round(iconSize * 1.15) + mc.font.lineHeight + FONT_LINE_OFFSET, color);
        }
        else {
            Component[] split = splitIntoTwo(sequenceName);
            if(split.length < 2)
                return;
            g.drawString(mc.font, split[0], textX, iconY + (int) Math.round(iconSize * 1.15) + mc.font.lineHeight + FONT_LINE_OFFSET, color);
            g.drawString(mc.font, split[1], textX, iconY + (int) Math.round(iconSize * 1.15) + (mc.font.lineHeight * 2) + (FONT_LINE_OFFSET * 2), color);
        }
    }

    private static void renderBackground(GuiGraphics g, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ResourceLocation bgTexture = ResourceLocation.fromNamespaceAndPath(
                LOTMCraft.MOD_ID, "textures/gui/pathway_info_overlay_background.png"
        );
        g.blit(bgTexture, x, y, BG_WIDTH, BG_HEIGHT, 0, 0, 64, 96, 64, 96);
    }

    private static void renderIcon(GuiGraphics g, String pathway, int x, int y, int size) {
        ResourceLocation iconTexture = ResourceLocation.fromNamespaceAndPath(
                LOTMCraft.MOD_ID, "textures/gui/icons/" + pathway + "_icon.png"
        );
        g.blit(iconTexture, x, y, size, size, 0, 0, ICON_SRC_SIZE, ICON_SRC_SIZE, ICON_SRC_SIZE, ICON_SRC_SIZE);
    }

    private static void renderPathwayText(GuiGraphics g, Minecraft mc, String pathway,
                                          int iconX, int iconY, int iconSize, int color) {
        Component pathwayTitle = Component.literal(BeyonderData.pathwayInfos.get(pathway).name() + "-")
                .withStyle(ChatFormatting.BOLD);
        Component pathwayTitle2 = Component.translatable("lotm.pathway").withStyle(ChatFormatting.BOLD);

        int fontWidth = Math.max(mc.font.width(pathwayTitle), mc.font.width(pathwayTitle2));
        int pathwayTitleY = iconY + iconSize / 2 - mc.font.lineHeight;
        boolean hasSecondLine = fontWidth > 65;

        if (!hasSecondLine) {
            if(fontWidth > 55)
                textX = iconX - fontWidth - TEXT_LEFT_PADDING;
            else
                textX = iconX - fontWidth - 3 * TEXT_LEFT_PADDING;
            g.drawString(mc.font, pathwayTitle, textX, pathwayTitleY, color);
            g.drawString(mc.font, pathwayTitle2, textX, iconY + iconSize / 2 + FONT_LINE_OFFSET, color);
        } else {
            Component[] splitParts = splitIntoTwo(pathwayTitle);
            if (splitParts.length < 2) return;

            fontWidth = Math.max(Math.max(mc.font.width(splitParts[0]), mc.font.width(splitParts[1])),
                    mc.font.width(pathwayTitle2));
            textX = iconX - fontWidth - TEXT_LEFT_PADDING;
            g.drawString(mc.font, splitParts[0], textX, pathwayTitleY - FONT_LINE_OFFSET - mc.font.lineHeight + 3, color);
            g.drawString(mc.font, splitParts[1], textX, pathwayTitleY + 3, color);
            g.drawString(mc.font, pathwayTitle2, textX,
                    pathwayTitleY + FONT_LINE_OFFSET + mc.font.lineHeight + 3, color);
        }
    }

    private static void renderPassiveAbilityIcons(GuiGraphics guiGraphics, Minecraft mc, int iconY, int iconSize, int screenStartX) {
        if(mc.player == null)
            return;

        int x = textX;
        int y = iconY + (int) Math.round(iconSize * 1.85f);

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

                    x += 24 + 4;
                    if(x >= screenStartX + WIDTH - 24 - 4) {
                        x = textX;
                        y += 24 + 4;
                    }
                }
            }
        }
    }

    private static void renderForeground(GuiGraphics g, int x, int y) {
        ResourceLocation fgTexture = ResourceLocation.fromNamespaceAndPath(
                LOTMCraft.MOD_ID, "textures/gui/pathway_info_overlay.png"
        );
        g.blit(fgTexture, x, y, BG_WIDTH, BG_HEIGHT, 0, 0, 64, 96, 64, 96);
        RenderSystem.disableBlend();
    }

    private static Component[] splitIntoTwo(Component component) {
        String s = component.getString();
        String[] split = s.split(" ");
        if(split.length == 0)
            return new Component[0];
        if(split.length == 1) {
            return new Component[]{Component.literal(s.substring(0, s.length() / 2)).withStyle(component.getStyle()), Component.literal(s.substring(s.length() / 2)).withStyle(component.getStyle())};
        } else if (split.length == 2) {
            return new Component[]{Component.literal(split[0]).withStyle(component.getStyle()), Component.literal(split[1]).withStyle(component.getStyle())};
        }
        else {
            StringBuilder firstPart = new StringBuilder();
            for(int i = 0; i < split.length - 1; i++) {
                firstPart.append(split[i]).append(" ");
            }
            return new Component[]{Component.literal(firstPart.toString()).withStyle(component.getStyle()), Component.literal(split[split.length - 1]).withStyle(component.getStyle())};
        }

    }
}
