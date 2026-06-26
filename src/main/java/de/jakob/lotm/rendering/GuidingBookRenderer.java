package de.jakob.lotm.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.item.custom.GuidingBookItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class GuidingBookRenderer {

    private static List<ResourceLocation> pages = new ArrayList<>();
    private static int currentPage = -1; // -1 means closed
    private static ItemStack lastHeldItem = ItemStack.EMPTY;
    private static boolean pagesLoaded = false;

    // Load all pages from the textures/guiding_book folder
    public static void loadPages(String modId) {
        if (pagesLoaded) return;

        pages.clear();

        for (int i = 1; i <= 4; i++) {
            pages.add(ResourceLocation.fromNamespaceAndPath(modId, "textures/guiding_book/page_" + i + ".png"));
        }

        pagesLoaded = true;
    }
    public static void nextPage() {
        if (pages.isEmpty()) return;

        currentPage++;
        if (currentPage >= pages.size()) {
            currentPage = -1; // Close book after last page
        }
    }

    public static void closePage() {
        currentPage = -1;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack heldItem = mc.player.getMainHandItem();

        // Check if player switched items while book was open
        if (currentPage >= 0) {
            if (heldItem.isEmpty() || !(heldItem.getItem() instanceof GuidingBookItem)) {
                closePage();
                lastHeldItem = ItemStack.EMPTY;
                return;
            }

            // Check if the specific itemstack changed (different slot)
            if (!ItemStack.isSameItemSameComponents(heldItem, lastHeldItem)) {
                closePage();
                lastHeldItem = ItemStack.EMPTY;
                return;
            }
        }

        // Update last held item
        if (heldItem.getItem() instanceof GuidingBookItem) {
            lastHeldItem = heldItem.copy();
        }

        // Render current page if open
        if (currentPage >= 0 && currentPage < pages.size()) {
            renderPage(event.getGuiGraphics(), pages.get(currentPage));
        }
    }

    private static void renderPage(GuiGraphics graphics, ResourceLocation pageTexture) {
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int padding = 1;

        int pageWidth = (int) (screenWidth / 1.75f);
        int pageHeight = (int) (pageWidth / 1.5f);

        int contentWidth = pageWidth - (padding * 2);
        int contentHeight = pageHeight - (padding * 2);

        int x = (screenWidth - pageWidth) / 2;
        int y = (screenHeight - pageHeight) / 2;

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 1000);

        graphics.fill(0, 0, screenWidth, screenHeight, 0x60000000);
        graphics.fill(x + 2, y + 2, x + pageWidth + 2, y + pageHeight + 2, 0x80000000);

        int borderColor = 0xFF8B7355;
        graphics.fill(x, y, x + pageWidth, y + 1, borderColor);
        graphics.fill(x, y + pageHeight - 1, x + pageWidth, y + pageHeight, borderColor);
        graphics.fill(x, y, x + 1, y + pageHeight, borderColor);
        graphics.fill(x + pageWidth - 1, y, x + pageWidth, y + pageHeight, borderColor);

        int midX = x + pageWidth / 2;
        graphics.fill(midX, y + 4, midX + 1, y + pageHeight - 4, 0xAA8B7355);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        graphics.blit(
                pageTexture,
                x + padding,
                y + padding,
                0,
                0,
                contentWidth,
                contentHeight,
                contentWidth,
                contentHeight
        );

        RenderSystem.disableBlend();
        graphics.pose().popPose();
    }
}