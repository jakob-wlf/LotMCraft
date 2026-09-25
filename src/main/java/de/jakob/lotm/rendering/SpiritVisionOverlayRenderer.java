package de.jakob.lotm.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.ShapeShiftComponent;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.HashMap;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class SpiritVisionOverlayRenderer {

    public static final HashMap<UUID, LivingEntity> entitiesLookedAt = new HashMap<>();

    private static final int ACCENT       = 0xFFb57bee;
    private static final int ACCENT_LIGHT = 0xFFcfa0f5;
    private static final int BG_DARK      = 0xCC0a0612;

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_vision_overlay"), (guiGraphics, deltaTracker) -> {
            renderOverlay(guiGraphics);
        });
    }

    private static void renderOverlay(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        if (!entitiesLookedAt.containsKey(mc.player.getUUID())) return;

        ResourceLocation backgroundTexture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/spirit_vision_overlay.png");
        guiGraphics.pose().pushPose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(backgroundTexture, 0, 0, screenWidth, screenHeight, 0, 0, 44, 256, 44, 256);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        guiGraphics.pose().popPose();

        LivingEntity entity = entitiesLookedAt.get(mc.player.getUUID());
        if (entity == null) return;

        int width = screenWidth / 3;
        int height = 42;
        int x = screenWidth / 2 - width / 2;
        int y = 12;

        renderPanel(guiGraphics, x, y, width, height);

        String name = entity.getName().getString();
        float maxHealth = entity.getMaxHealth();
        float health = entity.getHealth();

        ShapeShiftComponent shapeShiftComponent = entity.getData(ModAttachments.SHAPE_SHIFT);
        if (!shapeShiftComponent.getShape().isEmpty()) {
            String entityType = shapeShiftComponent.getShape();
            if (entityType.startsWith("player:")){
                name = entityType.split(":")[1];
            } else {
                name = entityType.contains(":") ? entityType.split(":")[1] : entityType;
            }
            maxHealth = getEntityMaxHealth(entityType);
            health = getEntityMaxHealth(entityType);

            if ((ClientBeyonderCache.getPathway(mc.player.getUUID()).equals("fool") && ClientBeyonderCache.getSequence(mc.player.getUUID()) < ClientBeyonderCache.getSequence(entity.getUUID()))
                    || (ClientBeyonderCache.getPathway(mc.player.getUUID()).equals("visionary") && ClientBeyonderCache.getSequence(mc.player.getUUID()) < ClientBeyonderCache.getSequence(entity.getUUID()))
                    || AbilityUtil.isTargetSignificantlyStronger(ClientBeyonderCache.getSequence(entity.getUUID()), ClientBeyonderCache.getSequence(mc.player.getUUID()))) {

                name = name + " (Shape Shifting)";
                maxHealth = entity.getMaxHealth();
                health = entity.getHealth();
            }
        }

        if (!VisionaryHandler.shouldStayInvisible(ClientBeyonderCache.getSequence(mc.player.getUUID()), entity)){
            guiGraphics.drawString(mc.font, name, x + width / 2 - mc.font.width(name) / 2 + 1, y + 7 + 1, 0x55000000);
            guiGraphics.drawCenteredString(mc.font, name, x + width / 2, y + 7, ACCENT_LIGHT);

            int barWidth = (int) (width / 1.3);
            int barHeight = 12;
            int barX = x + (width - barWidth) / 2;
            int barY = y + height - barHeight - 7;

            renderHealthBar(guiGraphics, mc.font, barX, barY, barWidth, barHeight,
                    health, maxHealth);
        }
    }

    private static float getEntityMaxHealth(String shapeKey) {
        if (shapeKey == null || shapeKey.isEmpty()) return 20f;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return 20f;

        if (shapeKey.startsWith("player:")) {
            String[] parts = shapeKey.split(":");
            if (parts.length >= 3) {
                try {
                    UUID uuid = UUID.fromString(parts[2]);
                    Player targetPlayer = level.getPlayerByUUID(uuid);
                    if (targetPlayer != null) {
                        return targetPlayer.getMaxHealth();
                    }
                } catch (IllegalArgumentException e) {
                }
            }
            return 20f;
        } else if (shapeKey.startsWith("lotmcraft:beyonder_npc:")) {
            shapeKey = "lotmcraft:beyonder_npc";
        }

        EntityType<?> type = null;
        ResourceLocation entityID = ResourceLocation.tryParse(shapeKey);
        if (entityID != null) {
            type = BuiltInRegistries.ENTITY_TYPE.get(entityID);
        }
        if (type == null) return 20f;

        Entity entity = type.create(level);
        if (!(entity instanceof LivingEntity livingEntity)) return 20f;
        return livingEntity.getMaxHealth();
    }

    private static void renderPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, BG_DARK);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + 10, 0x220d0a1a);

        guiGraphics.fill(x,             y + 2,          x + 2,         y + height - 2, ACCENT);
        guiGraphics.fill(x + width - 2, y + 2,          x + width,     y + height - 2, ACCENT);
        guiGraphics.fill(x + 2,         y,              x + width - 2, y + 2,          ACCENT);
        guiGraphics.fill(x + 2,         y + height - 2, x + width - 2, y + height,     ACCENT);

        guiGraphics.fill(x,             y,              x + 2,         y + 2,          0x00000000);
        guiGraphics.fill(x + width - 2, y,              x + width,     y + 2,          0x00000000);
        guiGraphics.fill(x,             y + height - 2, x + 2,         y + height,     0x00000000);
        guiGraphics.fill(x + width - 2, y + height - 2, x + width,    y + height,     0x00000000);

        guiGraphics.fill(x + 2,         y + 2,          x + 4,         y + 4,          ACCENT);
        guiGraphics.fill(x + width - 4, y + 2,          x + width - 2, y + 4,          ACCENT);
        guiGraphics.fill(x + 2,         y + height - 4, x + 4,         y + height - 2, ACCENT);
        guiGraphics.fill(x + width - 4, y + height - 4, x + width - 2, y + height - 2, ACCENT);
    }

    private static void renderHealthBar(GuiGraphics guiGraphics, Font font, int barX, int barY, int barWidth, int barHeight,
                                        float health, float maxHealth) {
        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xAA000000);

        int filledWidth = (int) (barWidth * (health / maxHealth));
        if (filledWidth > 0) {
            drawHorizontalGradient(guiGraphics, barX, barY, filledWidth, barHeight, ACCENT, ACCENT_LIGHT);
            guiGraphics.fill(barX, barY, barX + filledWidth, barY + 2, 0x33FFFFFF);
        }

        guiGraphics.fill(barX, barY,                 barX + barWidth, barY + 1,        0x44FFFFFF);
        guiGraphics.fill(barX, barY + barHeight - 1, barX + barWidth, barY + barHeight, 0x44000000);

        String healthText = health + " ❤";
        guiGraphics.drawString(font, healthText, barX + 4 + 1, barY + 1 + (barHeight - font.lineHeight) / 2 + 1, 0x55000000);
        guiGraphics.drawString(font, healthText, barX + 4,     barY + 1 + (barHeight - font.lineHeight) / 2,     0xFFFFFFFF);
    }

    private static void drawHorizontalGradient(GuiGraphics guiGraphics, int x, int y, int width, int height,
                                               int startColor, int endColor) {
        for (int i = 0; i < width; i++) {
            float ratio = (float) i / width;
            guiGraphics.fill(x + i, y, x + i + 1, y + height, interpolateColor(startColor, endColor, ratio));
        }
    }

    private static int interpolateColor(int color1, int color2, float ratio) {
        int a1 = (color1 >> 24) & 0xFF, r1 = (color1 >> 16) & 0xFF, g1 = (color1 >> 8) & 0xFF, b1 = color1 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF, r2 = (color2 >> 16) & 0xFF, g2 = (color2 >> 8) & 0xFF, b2 = color2 & 0xFF;
        return ((int)(a1 + (a2 - a1) * ratio) << 24) | ((int)(r1 + (r2 - r1) * ratio) << 16)
                | ((int)(g1 + (g2 - g1) * ratio) << 8) | (int)(b1 + (b2 - b1) * ratio);
    }

    public static void clearCache() {
        entitiesLookedAt.clear();
    }
}