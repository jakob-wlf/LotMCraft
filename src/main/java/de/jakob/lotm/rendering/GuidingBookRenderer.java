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
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        for (int i = 1; i <= 3; i++) {
            pages.add(ResourceLocation.fromNamespaceAndPath(modId, "textures/guiding_book/page_" + i + ".png"));
        }
        
        pagesLoaded = true;
    }
    
    // Alternative method if you want to manually register pages
    public static void registerPage(ResourceLocation pageTexture) {
        pages.add(pageTexture);
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
    
    @SubscribeEvent
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
        
        int pageWidth = Math.round(screenWidth / 1.5f);
        int pageHeight = Math.round(pageWidth / 1.5f);
        
        // Center the page
        int x = (screenWidth - pageWidth) / 2;
        int y = (screenHeight - pageHeight) / 2;
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        graphics.blit(pageTexture, x, y, 0, 0, pageWidth, pageHeight, pageWidth, pageHeight);
        
        RenderSystem.disableBlend();
    }
}