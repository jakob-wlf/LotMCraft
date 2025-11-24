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

    private static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        PotionRecipe recipe = getPotionRecipe(mc);

        if(recipe == null)
            return;

        int x = (mc.getWindow().getGuiScaledWidth() / 2) - (WIDTH / 2);
        int y = (mc.getWindow().getGuiScaledHeight() / 2) - (HEIGHT / 2);

        renderBackground(guiGraphics, x, y);
        renderTitle(guiGraphics, mc, x, y, recipe);
        drawIngredients(guiGraphics, mc, recipe, x, y);

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
        int iconY2 = iconY + 39;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(1.5f, 1.5f, 1.5f);

        guiGraphics.renderItem(recipe.supplementaryIngredient1(), (int) (iconX1 / 1.5f), (int) (iconY / 1.5f));
        guiGraphics.renderItem(recipe.supplementaryIngredient2(), (int) (iconX2 / 1.5f), (int) (iconY / 1.5f));
        guiGraphics.renderItem(recipe.mainIngredient(), (int) (iconX3 / 1.5f), (int) (iconY2 / 1.5f));

        guiGraphics.pose().popPose();

        guiGraphics.renderOutline(iconX1 - 2, iconY - 2, 26, 26, 0x44000000);
        guiGraphics.renderOutline(iconX2 - 2, iconY - 2, 26, 26, 0x44000000);
        guiGraphics.renderOutline(iconX3 - 2, iconY2 - 2, 26, 26, 0x44000000);
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
