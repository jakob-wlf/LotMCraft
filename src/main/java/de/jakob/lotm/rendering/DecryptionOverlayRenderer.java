package de.jakob.lotm.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.error.handler.TheftHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.*;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class DecryptionOverlayRenderer {

    public static HashMap<UUID, LivingEntity> entitiesLookedAtByPlayerWithActiveDecryption = new HashMap<>();

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "decryption_overlay"), (guiGraphics, deltaTracker) -> {
            renderOverlay(guiGraphics);
        });
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) {
            removeShader(mc);
            return;
        }

//        if (entitiesLookedAtByPlayerWithActiveDecryption.containsKey(mc.player.getUUID())) {
//            applyShader(mc, "decryption");
//        }
    }

    private static void applyShader(Minecraft mc, String shaderName) {
        if (mc.gameRenderer.currentEffect() == null ||
                !mc.gameRenderer.currentEffect().getName().equals(shaderName)) {
            try {
                mc.gameRenderer.loadEffect(
                        ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "shaders/post/" + shaderName + ".json")
                );
            } catch (Exception ignored) {
            }
        }
    }

    private static void removeShader(Minecraft mc) {
        if (mc.gameRenderer.currentEffect() != null) {
            mc.gameRenderer.shutdownEffect();
        }
    }

    private static void renderOverlay(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        if (entitiesLookedAtByPlayerWithActiveDecryption.containsKey(mc.player.getUUID())) {
            renderDecryptionOverlay(guiGraphics, screenWidth, screenHeight);

            LivingEntity entity = entitiesLookedAtByPlayerWithActiveDecryption.get(mc.player.getUUID());
            if(entity != null) {
                int width =  (screenWidth / 3);
                int height = 35;

                int x = screenWidth / 2 - width / 2;
                int y = 15;

                renderOutLine(guiGraphics, x, y, width, height);

                //Entity name
                String name = entity.getName().getString();
                int nameX = x + (width / 2);
                int nameY = y + 5;
                guiGraphics.drawCenteredString(mc.font, name, nameX, nameY, 0xFFFFFFFF);

                //Health Bar
                int barWidth = (int) (width / 1.3);
                int barHeight = 14;

                int barX = x + ((width - barWidth) / 2);
                int barY = y + height - barHeight - 5;

                guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0x88000000);

                double fillPercentage = entity.getHealth() / entity.getMaxHealth();
                int filledBarWidth = (int) (barWidth * fillPercentage);

                if(filledBarWidth > 0)
                    drawHorizontalGradient(guiGraphics, barX, barY, filledBarWidth, barHeight, 0xFFFF0000, 0xFFe43fa3);

                //Health String
                guiGraphics.drawString(mc.font, entity.getHealth() + " ❤", barX + 3, barY + 1 + ((barHeight - mc.font.lineHeight) / 2), 0xFFFFFFFF);

                renderStealableItems(guiGraphics, entity, screenWidth);
            }
        }
    }

    private static void renderDecryptionOverlay(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        ResourceLocation backgroundTexture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/decryption_overlay.png");
        // Push the current pose
        guiGraphics.pose().pushPose();

        // Set up alpha blending
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Blit the texture with transparency
        guiGraphics.blit(backgroundTexture, 0, 0, screenWidth, screenHeight, 0, 0, 128, 96, 128, 96);

        // Reset blend settings and shader color
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // Reset to opaque
        RenderSystem.disableBlend();

        // Pop the pose to avoid affecting later rendering
        guiGraphics.pose().popPose();
    }

    private static void renderStealableItems(GuiGraphics guiGraphics, LivingEntity entity, int width) {
        List<Item> stealableItems = TheftHandler.getStealableItemsForEntity(entity);
        if(stealableItems.isEmpty()) {
            return;
        }

        int x = width - 24;
        int y = 15;

        for(Item item : stealableItems) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(1.5f, 1.5f, 1.5f);

            int scaledX = (int) (x / 1.5f);
            int scaledY = (int) (y / 1.5f);

            guiGraphics.renderItem(
                    new ItemStack(item),
                    scaledX, scaledY
            );

            guiGraphics.pose().popPose();

            y += 24;
        }

    }

    private static void renderOutLine(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0x77000000);
        guiGraphics.fill(x, y, x + width, y + 2, 0xFF5cff68);
        guiGraphics.fill(x, y + height - 2, x + width, y + height, 0xFF5cff68);
        guiGraphics.fill(x, y + 2, x + 2, y + height - 2, 0xFF5cff68);
        guiGraphics.fill(x + width - 2, y + 2, x + width, y + height - 2, 0xFF5cff68);
    }

    private static void drawHorizontalGradient(GuiGraphics guiGraphics, int x, int y, int width, int height,
                                             int startColor, int endColor) {
        for (int i = 0; i < width; i++) {
            float ratio = (float) i / width;
            int color = interpolateColor(startColor, endColor, ratio);
            guiGraphics.fill(x + i, y, x + i + 1, y + height, color);
        }
    }

    private static int interpolateColor(int color1, int color2, float ratio) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * ratio);
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}