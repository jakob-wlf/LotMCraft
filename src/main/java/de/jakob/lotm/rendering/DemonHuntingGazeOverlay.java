package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class DemonHuntingGazeOverlay {

    private static final long PULSE_MILLIS = 450L;
    private static final int EDGE_WIDTH = 48;
    private static final int RED = 0xB0000A;
    private static final float MAX_ALPHA = 0.75f;

    private static final Map<Integer, Gaze> gazes = new HashMap<>();

    public static void show(int entityId, int pulses) {
        Gaze current = gazes.get(entityId);
        if (current != null && System.currentTimeMillis() < current.end()) return;
        long now = System.currentTimeMillis();
        gazes.put(entityId, new Gaze(now, now + pulses * PULSE_MILLIS));
    }

    public static void clearCache() {
        gazes.clear();
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelow(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "demon_hunting_gaze_overlay"),
                (guiGraphics, deltaTracker) -> render(guiGraphics));
    }

    private static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || gazes.isEmpty()) return;

        long now = System.currentTimeMillis();
        float left = 0, right = 0, top = 0, bottom = 0;
        var iterator = gazes.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            Gaze gaze = entry.getValue();
            Entity entity = mc.level.getEntity(entry.getKey());
            if (now >= gaze.end() || entity == null) {
                iterator.remove();
                continue;
            }
            float phase = (now - gaze.start()) % PULSE_MILLIS / (float) PULSE_MILLIS;
            float pulse = MAX_ALPHA * Mth.sin(phase * Mth.PI);

            Vec3 offset = entity.position().subtract(mc.player.position());
            double relative = Math.toRadians(Mth.wrapDegrees(Math.toDegrees(Math.atan2(offset.z, offset.x)) - 90.0 - mc.player.getYRot()));
            float sin = (float) Math.sin(relative);
            float cos = (float) Math.cos(relative);
            right = Math.max(right, pulse * Math.max(0, sin));
            left = Math.max(left, pulse * Math.max(0, -sin));
            top = Math.max(top, pulse * Math.max(0, cos));
            bottom = Math.max(bottom, pulse * Math.max(0, -cos));
        }

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        for (int i = 0; i < EDGE_WIDTH; i++) {
            float fade = 1f - i / (float) EDGE_WIDTH;
            if (left > 0) guiGraphics.fill(i, 0, i + 1, height, color(left * fade));
            if (right > 0) guiGraphics.fill(width - i - 1, 0, width - i, height, color(right * fade));
            if (top > 0) guiGraphics.fill(0, i, width, i + 1, color(top * fade));
            if (bottom > 0) guiGraphics.fill(0, height - i - 1, width, height - i, color(bottom * fade));
        }
    }

    private static int color(float alpha) {
        return ((int) (Mth.clamp(alpha, 0f, 1f) * 255) << 24) | RED;
    }

    private record Gaze(long start, long end) {
    }
}
