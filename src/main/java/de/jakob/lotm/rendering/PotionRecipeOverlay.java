package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.potions.PotionRecipe;
import de.jakob.lotm.potions.PotionRecipeItem;
import de.jakob.lotm.util.BeyonderData;
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
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class PotionRecipeOverlay {
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "recipe_overlay"), (guiGraphics, deltaTracker) -> {
            render(guiGraphics);
        });
    }

    private static final int WIDTH = 180;
    private static final int HEIGHT = 120;

    private static boolean shouldRender = false;

    private static void render(GuiGraphics guiGraphics) {
        if(!shouldRender) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null) {
            return;
        }

        PotionRecipe recipe = getPotionRecipe(mc);

        if(recipe == null) {
            return;
        }

        int x = (mc.getWindow().getGuiScaledWidth() / 2) - (WIDTH / 2);
        int y = (mc.getWindow().getGuiScaledHeight() / 2) - (HEIGHT / 2);

        renderBackground(guiGraphics, x, y);
        renderTitle(guiGraphics, mc, x, y, recipe);
        drawIngredients(guiGraphics, mc, recipe, x, y);
    }

    public static void toggleRenderRecipe() {
        shouldRender = !shouldRender;
    }

    private static void renderTitle(GuiGraphics guiGraphics, Minecraft mc, int x, int y, PotionRecipe recipe) {
        String pathway = recipe.potion().getPathway();
        int color = BeyonderData.pathwayInfos.get(pathway).color();

        Component text = Component.literal(recipe.potion().getName(new ItemStack(recipe.potion())).getString()).withStyle(ChatFormatting.BOLD);

        int textY = y + 12;
        int textX = x + (WIDTH / 2) - (mc.font.width(text) / 2);

        guiGraphics.drawString(mc.font, text, textX, textY, color);
    }

    private static void renderBackground(GuiGraphics guiGraphics, int x, int y) {
        ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/recipe_background.png");
        guiGraphics.blit(textureLocation, x, y, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
    }

    private static void drawIngredients(GuiGraphics guiGraphics, Minecraft mc, PotionRecipe recipe, int x, int y) {
        int iconX1 = x + (WIDTH / 2) - 32;
        int iconX2 = x + (WIDTH / 2) + 16;
        int iconX3 = x + (WIDTH / 2) - (16/2);

        int iconY = 12 + y + mc.font.lineHeight * 2;
        int iconY2 = iconY + 48; // Moved main ingredient further down

        // Draw connecting lines before rendering items
        drawConnectingLines(guiGraphics, iconX1, iconX2, iconX3, iconY, iconY2);

        // Render items with scale
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(1.5f, 1.5f, 1.5f);

        guiGraphics.renderItem(recipe.supplementaryIngredient1(), (int) (iconX1 / 1.5f), (int) (iconY / 1.5f));
        guiGraphics.renderItem(recipe.supplementaryIngredient2(), (int) (iconX2 / 1.5f), (int) (iconY / 1.5f));
        guiGraphics.renderItem(recipe.mainIngredient(), (int) (iconX3 / 1.5f), (int) (iconY2 / 1.5f));

        guiGraphics.pose().popPose();

        // Draw outlines
        guiGraphics.renderOutline(iconX1 - 2, iconY - 2, 26, 26, 0x44000000);
        guiGraphics.renderOutline(iconX2 - 2, iconY - 2, 26, 26, 0x44000000);
        guiGraphics.renderOutline(iconX3 - 2, iconY2 - 2, 26, 26, 0x44000000);

        // Draw ingredient counts at bottom right of each icon
        drawItemCount(guiGraphics, mc, recipe.supplementaryIngredient1(), iconX1, iconY);
        drawItemCount(guiGraphics, mc, recipe.supplementaryIngredient2(), iconX2, iconY);
        drawItemCount(guiGraphics, mc, recipe.mainIngredient(), iconX3, iconY2);
    }

    private static void drawConnectingLines(GuiGraphics guiGraphics, int iconX1, int iconX2, int iconX3, int iconY, int iconY2) {
        int lineColor = 0xFF6B2C91; // Dark purple color

        // Calculate edge points of each icon (icons are 24x24)
        // Bottom edge of top-left icon
        int startX1 = iconX1 + 12;
        int startY1 = iconY + 24;

        // Bottom edge of top-right icon
        int startX2 = iconX2 + 12;
        int startY2 = iconY + 24;

        // Center of bottom icon
        int endX3 = iconX3 + 12;
        int endY3 = iconY2 + 12;

        // Draw line from left ingredient to main ingredient
        drawLine(guiGraphics, startX1, startY1, endX3, endY3, lineColor);

        // Draw line from right ingredient to main ingredient
        drawLine(guiGraphics, startX2, startY2, endX3, endY3, lineColor);
    }

    private static void drawLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        // Simple line drawing using fill
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);

        float xIncrement = (float)(x2 - x1) / steps;
        float yIncrement = (float)(y2 - y1) / steps;

        float x = x1;
        float y = y1;

        for(int i = 0; i <= steps; i++) {
            guiGraphics.fill((int)x, (int)y, (int)x + 2, (int)y + 2, color);
            x += xIncrement;
            y += yIncrement;
        }
    }

    private static void drawItemCount(GuiGraphics guiGraphics, Minecraft mc, ItemStack stack, int x, int y) {
        if(stack.isEmpty()) {
            return;
        }

        int count = stack.getCount();
        if(count <= 1) {
            return;
        }

        String countText = String.valueOf(count);

        // Position further down and to the right of the icon
        int textX = x + 26;
        int textY = y + 18;

        // Draw "x" before the number in gray
        guiGraphics.drawString(mc.font, "x", textX - mc.font.width("x") - 1, textY, 0xFFAAAAAA, true);

        // Draw count with shadow for better visibility
        guiGraphics.drawString(mc.font, countText, textX, textY, 0xFFAAAAAA, true);
    }

    @Nullable
    private static PotionRecipe getPotionRecipe(Minecraft mc) {
        if(mc.player == null)
            return null;

        ItemStack item = mc.player.getMainHandItem();
        if(!(item.getItem() instanceof PotionRecipeItem recipeItem))
            return null;

        return recipeItem.getRecipe();
    }
}