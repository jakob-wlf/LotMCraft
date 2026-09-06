package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class DangerArrowsOverlay {

    public enum Direction {
        UP("textures/gui/danger_arrow_up.png"),
        UP_RIGHT("textures/gui/danger_arrow_up_right.png"),
        UP_LEFT("textures/gui/danger_arrow_up_left.png"),
        DOWN("textures/gui/danger_arrow_down.png"),
        DOWN_RIGHT("textures/gui/danger_arrow_down_right.png"),
        DOWN_LEFT("textures/gui/danger_arrow_down_left.png"),
        RIGHT("textures/gui/danger_arrow_right.png"),
        LEFT("textures/gui/danger_arrow_left.png"),
        NONE(null);

        private final ResourceLocation texture;

        Direction(String path) {
            this.texture = (path != null)
                    ? ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, path)
                    : null;
        }

        public ResourceLocation getTexture() {
            return texture;
        }
    }

    private static Direction currentDirection = Direction.NONE;
    private static long expireTime = 0;

    private static final int ARROW_WIDTH = 32;
    private static final int ARROW_HEIGHT = 32;

    public static void show(String directionInput, int durationTicks) {
        currentDirection = switch (directionInput) {
            case "North" -> Direction.UP;
            case "North-East" -> Direction.UP_RIGHT;
            case "North-West" -> Direction.UP_LEFT;
            case "South" -> Direction.DOWN;
            case "South-East" -> Direction.DOWN_RIGHT;
            case "South-West" -> Direction.DOWN_LEFT;
            case "East" -> Direction.RIGHT;
            case "West" -> Direction.LEFT;
            default -> Direction.NONE;
        };

        if (durationTicks > 0) {
            expireTime = System.currentTimeMillis() + (durationTicks * 50L);
        } else {
            expireTime = -1; // infinite
        }
    }

    public static void show(String direction) {
        show(direction, -1);
    }

    public static void hide() {
        currentDirection = Direction.NONE;
        expireTime = 0;
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "danger_arrows_overlay"),
                (guiGraphics, deltaTracker) -> renderOverlay(guiGraphics)
        );
    }

    public static void renderOverlay(GuiGraphics guiGraphics) {
        if (currentDirection == Direction.NONE) return;


        // check if the auto hide timer has expired
        if (expireTime != -1 && System.currentTimeMillis() > expireTime) {
            currentDirection = Direction.NONE;
            return;
        }

        ResourceLocation texture = currentDirection.getTexture();
        if (texture == null) {
            return;
        }

        int fourtX = (guiGraphics.guiWidth() - ARROW_WIDTH) / 4;
        int fourthY = (guiGraphics.guiHeight() - ARROW_HEIGHT) / 4;

        int x = fourtX * 2;
        int y = fourthY * 2;

        switch (currentDirection) {
            case UP -> { x = fourtX * 2; y = fourthY * 1; }
            case DOWN -> { x = fourtX * 2; y = fourthY * 3; }
            case LEFT -> { x = fourtX * 1; y = fourthY * 2; }
            case RIGHT -> { x = fourtX * 3; y = fourthY * 2; }

            case UP_LEFT -> { x = fourtX * 1; y = fourthY * 1; }
            case UP_RIGHT -> { x = fourtX * 3; y = fourthY * 1; }
            case DOWN_LEFT -> { x = fourtX * 1; y = fourthY * 3; }
            case DOWN_RIGHT -> { x = fourtX * 3; y = fourthY * 3; }
            case NONE -> {}
        }

        guiGraphics.blit(
                texture,
                x,
                y,
                0,
                0,
                ARROW_WIDTH,
                ARROW_HEIGHT,
                ARROW_WIDTH,
                ARROW_HEIGHT
        );

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.5F);
    }
}