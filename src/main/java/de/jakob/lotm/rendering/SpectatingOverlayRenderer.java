package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class SpectatingOverlayRenderer {

    public static HashMap<UUID, LivingEntity> entitiesLookedAtByPlayerWithActiveSpectating = new HashMap<>();

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spectating_overlay"), (guiGraphics, deltaTracker) -> {
            renderOverlay(guiGraphics);
        });
    }

    private static void renderOverlay(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();

        if (entitiesLookedAtByPlayerWithActiveSpectating.containsKey(mc.player.getUUID())) {
            LivingEntity entity = entitiesLookedAtByPlayerWithActiveSpectating.get(mc.player.getUUID());
            if (entity != null) {
                int width = screenWidth / 3;
                int height = 42;
                int x = screenWidth / 2 - width / 2;
                int y = 12;

                renderPanel(guiGraphics, x, y, width, height, 0xFFf7cd83);

                String name = entity.getName().getString();
                guiGraphics.drawString(mc.font, name, x + width / 2 - mc.font.width(name) / 2 + 1, y + 7 + 1, 0x55000000);
                guiGraphics.drawCenteredString(mc.font, name, x + width / 2, y + 7, 0xFFf7cd83);

                int barWidth = (int) (width / 1.3);
                int barHeight = 12;
                int barX = x + (width - barWidth) / 2;
                int barY = y + height - barHeight - 7;

                renderHealthBar(guiGraphics, mc.font, barX, barY, barWidth, barHeight,
                        entity.getHealth(), entity.getMaxHealth(),
                        0xFFFF0000, 0xFFe43fa3);

                displayStats(guiGraphics, entity, screenWidth);
            }
        }
    }

    private static void renderPanel(GuiGraphics guiGraphics, int x, int y, int width, int height, int accentColor) {
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, 0xCC0a0a12);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + 10, 0x220d0d1a);

        guiGraphics.fill(x, y + 2, x + 2, y + height - 2, accentColor);
        guiGraphics.fill(x + width - 2, y + 2, x + width, y + height - 2, accentColor);
        guiGraphics.fill(x + 2, y, x + width - 2, y + 2, accentColor);
        guiGraphics.fill(x + 2, y + height - 2, x + width - 2, y + height, accentColor);

        guiGraphics.fill(x, y, x + 2, y + 2, 0x00000000);
        guiGraphics.fill(x + width - 2, y, x + width, y + 2, 0x00000000);
        guiGraphics.fill(x, y + height - 2, x + 2, y + height, 0x00000000);
        guiGraphics.fill(x + width - 2, y + height - 2, x + width, y + height, 0x00000000);

        guiGraphics.fill(x + 2, y + 2, x + 4, y + 4, accentColor);
        guiGraphics.fill(x + width - 4, y + 2, x + width - 2, y + 4, accentColor);
        guiGraphics.fill(x + 2, y + height - 4, x + 4, y + height - 2, accentColor);
        guiGraphics.fill(x + width - 4, y + height - 4, x + width - 2, y + height - 2, accentColor);
    }

    private static void renderHealthBar(GuiGraphics guiGraphics, Font font, int barX, int barY, int barWidth, int barHeight,
                                        float health, float maxHealth, int colorStart, int colorEnd) {
        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xAA000000);

        double fillPct = health / maxHealth;
        int filledWidth = (int) (barWidth * fillPct);
        if (filledWidth > 0) {
            drawHorizontalGradient(guiGraphics, barX, barY, filledWidth, barHeight, colorStart, colorEnd);
            guiGraphics.fill(barX, barY, barX + filledWidth, barY + 2, 0x33FFFFFF);
        }

        guiGraphics.fill(barX, barY, barX + barWidth, barY + 1, 0x44FFFFFF);
        guiGraphics.fill(barX, barY + barHeight - 1, barX + barWidth, barY + barHeight, 0x44000000);

        String healthText = health + " ❤";
        guiGraphics.drawString(font, healthText, barX + 4 + 1, barY + 1 + (barHeight - font.lineHeight) / 2 + 1, 0x55000000);
        guiGraphics.drawString(font, healthText, barX + 4, barY + 1 + (barHeight - font.lineHeight) / 2, 0xFFFFFFFF);
    }

    private static final Attribute[] attributesThatShouldGetDisplayed = new Attribute[]{
            Attributes.MOVEMENT_SPEED.value(), Attributes.ATTACK_DAMAGE.value(), Attributes.JUMP_STRENGTH.value(), Attributes.ARMOR.value()
    };

    private static void displayStats(GuiGraphics guiGraphics, LivingEntity entity, int screenWidth) {
        Collection<AttributeInstance> attributes = BuiltInRegistries.ATTRIBUTE.holders()
                .map(entity::getAttribute)
                .filter(Objects::nonNull)
                .filter(a -> {
                    for (Attribute at : attributesThatShouldGetDisplayed) {
                        if (at == a.getAttribute().value()) return true;
                    }
                    return false;
                })
                .toList();

        int maxTextWidth = attributes.stream()
                .map(a -> Minecraft.getInstance().font.width(
                        getAttributeName(a.getAttribute()) + ": " + formatValue(a.getValue())))
                .max(Comparator.naturalOrder())
                .orElse(0);

        if (maxTextWidth == 0) return;

        Font font = Minecraft.getInstance().font;
        int padding = 12;
        int lineSpacing = font.lineHeight + 5;
        int parameterCount = attributes.size();
        if (ClientBeyonderCache.isBeyonder(entity.getUUID())) parameterCount += 3;

        int panelWidth = maxTextWidth + padding * 2;
        int panelHeight = parameterCount * lineSpacing + padding * 2 - 5;
        int panelX = screenWidth - panelWidth - 10;
        int panelY = 12;

        renderPanel(guiGraphics, panelX, panelY, panelWidth, panelHeight, 0xFFf7cd83);

        int textX = panelX + padding;
        int y = panelY + padding;

        for (AttributeInstance attribute : attributes) {
            String line = getAttributeName(attribute.getAttribute()) + ": " + formatValue(attribute.getValue());
            guiGraphics.drawString(font, line, textX + 1, y + 1, 0x55000000);
            guiGraphics.drawString(font, line, textX, y, 0xFFd4cfc8);
            y += lineSpacing;
        }

        if (ClientBeyonderCache.isBeyonder(entity.getUUID())) {
            String pathway = ClientBeyonderCache.getPathway(entity.getUUID());
            if (!BeyonderData.pathwayInfos.containsKey(pathway)) return;
            String pathwayName = BeyonderData.pathwayInfos.get(pathway).getName();
            int sequence = ClientBeyonderCache.getSequence(entity.getUUID());
            float spirituality = ClientBeyonderCache.getSpirituality(entity.getUUID());

            String[] beyonderLines = {
                    Component.translatable("lotm.pathway").getString() + ": " + pathwayName,
                    Component.translatable("lotm.sequence").getString() + ": " + sequence,
                    Component.translatable("lotm.spirituality").getString() + ": " + formatValue(spirituality)
            };

            for (String line : beyonderLines) {
                guiGraphics.drawString(font, line, textX + 1, y + 1, 0x55000000);
                guiGraphics.drawString(font, line, textX, y, 0xFFf7cd83);
                y += lineSpacing;
            }
        }
    }

    private static double formatValue(double value) {
        return Math.round(value * 100000d) / 100000d;
    }

    private static void drawHorizontalGradient(GuiGraphics guiGraphics, int x, int y, int width, int height,
                                               int startColor, int endColor) {
        for (int i = 0; i < width; i++) {
            float ratio = (float) i / width;
            guiGraphics.fill(x + i, y, x + i + 1, y + height, interpolateColor(startColor, endColor, ratio));
        }
    }

    private static String getAttributeName(Holder<Attribute> attribute) {
        if (attribute == null) return "Unknown Attribute";
        String regName = attribute.getRegisteredName();
        String path = regName.contains(":") ? regName.substring(regName.indexOf(":") + 1) : regName;
        return Component.translatable("attribute.name." + path).getString();
    }

    private static int interpolateColor(int color1, int color2, float ratio) {
        int a1 = (color1 >> 24) & 0xFF, r1 = (color1 >> 16) & 0xFF, g1 = (color1 >> 8) & 0xFF, b1 = color1 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF, r2 = (color2 >> 16) & 0xFF, g2 = (color2 >> 8) & 0xFF, b2 = color2 & 0xFF;
        return ((int)(a1 + (a2 - a1) * ratio) << 24) | ((int)(r1 + (r2 - r1) * ratio) << 16)
                | ((int)(g1 + (g2 - g1) * ratio) << 8) | (int)(b1 + (b2 - b1) * ratio);
    }

    public static void clearCache() {
        entitiesLookedAtByPlayerWithActiveSpectating.clear();
    }
}