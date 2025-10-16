package de.jakob.lotm.overlay;

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
public class MarionetteOverlayRenderer {

    public static HashMap<UUID, LivingEntity> currentMarionette = new HashMap<>();

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "marionette_overlay"), (guiGraphics, deltaTracker) -> {
            renderOverlay(guiGraphics);
        });
    }

    private static void renderOverlay(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();

        if (currentMarionette.containsKey(mc.player.getUUID())) {
            LivingEntity entity = currentMarionette.get(mc.player.getUUID());
            if(entity != null) {
                int width =  (screenWidth / 3);
                int height = 45;

                int barWidth = (int) (width / 1.3);
                int barHeight = 14;

                int x = screenWidth - barWidth - 40;
                int y = 15;

                renderOutLine(guiGraphics, x, y, width, height);

                //Marionette Label
                String label = Component.translatable("lotm.marionette").getString() + ":";
                int labelY = y + 5;
                int labelX = x + (width / 2);
                guiGraphics.drawCenteredString(mc.font, label, labelX, labelY, 0xFFFFFFFF);

                //Entity name
                String name = entity.getName().getString();
                int nameY = y + 5 + mc.font.lineHeight;
                int nameX = x + (width / 2);
                guiGraphics.drawCenteredString(mc.font, name, nameX, nameY, 0xFFFFFFFF);

                //Health Bar
                int barY = y + height - barHeight - 5;
                int barX = x + (width / 2) - (barWidth / 2);

                guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0x88000000);

                double fillPercentage = entity.getHealth() / entity.getMaxHealth();
                int filledBarWidth = (int) (barWidth * fillPercentage);

                if(filledBarWidth > 0)
                    drawHorizontalGradient(guiGraphics, barX, barY, filledBarWidth, barHeight, 0xFFFF0000, 0xFFe43fa3);

                //Health String
                guiGraphics.drawCenteredString(mc.font, entity.getHealth() + " ❤", barX + (barWidth / 2), barY + 1 + ((barHeight - mc.font.lineHeight) / 2), 0xFFFFFFFF);

                //displayStats(guiGraphics, entity, screenWidth);
            }
        }
    }

    private static void renderOutLine(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0x77000000);
        guiGraphics.fill(x, y, x + width, y + 2, 0xFFa742f5);
        guiGraphics.fill(x, y + height - 2, x + width, y + height, 0xFFa742f5);
        guiGraphics.fill(x, y + 2, x + 2, y + height - 2, 0xFFa742f5);
        guiGraphics.fill(x + width - 2, y + 2, x + width, y + height - 2, 0xFFa742f5);
    }

    private static final Attribute[] attributesThatShouldGetDisplayed = new Attribute[]{
            Attributes.MOVEMENT_SPEED.value(), Attributes.ATTACK_DAMAGE.value(), Attributes.JUMP_STRENGTH.value(), Attributes.ARMOR.value()
    };

    private static void displayStats(GuiGraphics guiGraphics, LivingEntity entity, int screenWidth) {
        // Collect all attributes the entity supports by probing the registry
        Collection<AttributeInstance> attributes = BuiltInRegistries.ATTRIBUTE.holders()
                .map(entity::getAttribute)
                .filter(Objects::nonNull)
                .filter(a -> {
                    for(Attribute at : attributesThatShouldGetDisplayed) {
                        if(at == a.getAttribute().value())
                            return true;
                    }
                    return false;
                })
                .toList();

        int startingX = screenWidth - 10 - attributes.stream()
                .map(a -> Minecraft.getInstance().font.width(
                        getAttributeName(a.getAttribute()) + ": " + formatValue(a.getValue())))
                .max(Comparator.naturalOrder())
                .orElse(0);

        if (startingX == 0)
            return;

        Font font = Minecraft.getInstance().font;
        int y = 15 + (font.lineHeight / 2);



        int outlineX = startingX - 10;
        int outlineY = 15;
        int outlineWidth = screenWidth - outlineX;
        int parameterCount = attributes.size();
        if(ClientBeyonderCache.isBeyonder(entity.getUUID()))
            parameterCount += 3;

        guiGraphics.fill(startingX - 10, 15,
                screenWidth,
                15 + (parameterCount * 2 * font.lineHeight),
                0x77000000);

        int outlineHeight = 15 + (parameterCount * 2 * font.lineHeight) - outlineY;

        renderOutLine(guiGraphics, outlineX, outlineY, outlineWidth, outlineHeight);

        for (AttributeInstance attribute : attributes) {
            guiGraphics.drawString(font,
                    getAttributeName(attribute.getAttribute()) + ": " + formatValue(attribute.getValue()),
                    startingX, y, 0xFFFFFFFF);
            y += font.lineHeight * 2;
        }

        if(ClientBeyonderCache.isBeyonder(entity.getUUID())) {
            String pathway = ClientBeyonderCache.getPathway(entity.getUUID());
            String pathwayName = BeyonderData.pathwayInfos.get(pathway).getName();
            int sequence = ClientBeyonderCache.getSequence(entity.getUUID());
            float spirituality = ClientBeyonderCache.getSpirituality(entity.getUUID());

            guiGraphics.drawString(font,
                    Component.translatable("lotm.pathway").getString() + ": " + pathwayName,
                    startingX, y, 0xFFFFFFFF);
            y += font.lineHeight * 2;

            guiGraphics.drawString(font,
                    Component.translatable("lotm.sequence").getString() + ": " + sequence,
                    startingX, y, 0xFFFFFFFF);
            y += font.lineHeight * 2;

            guiGraphics.drawString(font,
                    Component.translatable("lotm.spirituality").getString() + ": " + formatValue(spirituality),
                    startingX, y, 0xFFFFFFFF);
        }
    }

    private static double formatValue(double value) {
        return Math.round(value * 100000d) / 100000d;
    }

    private static void drawHorizontalGradient(GuiGraphics guiGraphics, int x, int y, int width, int height,
                                             int startColor, int endColor) {
        for (int i = 0; i < width; i++) {
            float ratio = (float) i / width;
            int color = interpolateColor(startColor, endColor, ratio);
            guiGraphics.fill(x + i, y, x + i + 1, y + height, color);
        }
    }

    private static String getAttributeName(Holder<Attribute> attribute) {
        if (attribute == null) {
            return "Unknown Attribute";
        }

        // Registered name looks like: "minecraft:generic.movement_speed"
        String regName = attribute.getRegisteredName();

        // Extract the path (after the colon), e.g. "generic.movement_speed"
        String path = regName.contains(":") ? regName.substring(regName.indexOf(":") + 1) : regName;

        // Build translation key: "attribute.name.generic.movement_speed"
        String translationKey = "attribute.name." + path;

        // Return the localized string
        return Component.translatable(translationKey).getString();
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