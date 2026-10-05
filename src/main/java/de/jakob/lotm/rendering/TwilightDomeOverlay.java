package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ability_entities.twilight_giant.TwilightDomeEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.List;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public final class TwilightDomeOverlay {
    private TwilightDomeOverlay() {
    }

    private record Hit(TwilightDomeEntity dome, float edge) {
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "twilight_dome_tint"),
                TwilightDomeOverlay::renderTint);
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "twilight_dome_blackout"),
                TwilightDomeOverlay::renderBlackout);
    }

    private static Hit find(Minecraft mc) {
        if (mc.player == null || mc.level == null) return null;
        Vec3 eye = mc.player.getEyePosition();
        List<TwilightDomeEntity> domes = mc.level.getEntitiesOfClass(TwilightDomeEntity.class, mc.player.getBoundingBox().inflate(80.0));
        Hit best = null;
        for (TwilightDomeEntity dome : domes) {
            if (dome.subject() != null) {
                if (!mc.player.getUUID().equals(dome.subject())) continue;
                return new Hit(dome, 1.0F);
            }
            if (eye.y < dome.getY() - 2.0) continue;
            double dist = Math.sqrt(dome.position().distanceToSqr(eye));
            float edge = Mth.clamp((float) ((dome.radius() - dist) / 2.0), 0.0F, 1.0F);
            if (edge > 0.0F && (best == null || edge > best.edge())) best = new Hit(dome, edge);
        }
        return best;
    }

    public static void renderTint(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        Hit hit = find(mc);
        if (hit == null) return;
        float partial = dt.getGameTimeDeltaPartialTick(false);
        float fadeIn = Mth.clamp(hit.dome().getAge(partial) / 10.0F, 0.0F, 1.0F);
        float strength = hit.edge() * fadeIn;
        float p = hit.dome().sunsetProgress(partial);
        int r;
        int gr;
        int b;
        float alpha;
        if (hit.dome().subject() != null) {
            float horizon = TwilightDomeEntity.SUN_HORIZON;
            if (p <= horizon) {
                float along = horizon <= 0.0F ? 0.0F : p / horizon;
                r = 255;
                gr = 80;
                b = 25;
                alpha = along * 0.5F * strength;
            } else {
                float set = (p - horizon) / (1.0F - horizon);
                r = (int) Mth.lerp(set, 255.0F, 0.0F);
                gr = (int) Mth.lerp(set, 80.0F, 0.0F);
                b = (int) Mth.lerp(set, 25.0F, 0.0F);
                alpha = Mth.lerp(set, 0.5F, 0.92F) * strength;
            }
        } else {
            r = (int) Mth.lerp(p, 255.0F, 0.0F);
            gr = (int) Mth.lerp(p, 80.0F, 0.0F);
            b = (int) Mth.lerp(p, 25.0F, 0.0F);
            alpha = Mth.lerp(p, 0.38F, 0.92F) * strength;
        }
        if (alpha <= 0.0F) return;
        g.fill(0, 0, g.guiWidth(), g.guiHeight(), argb(alpha, r, gr, b));
    }

    public static void renderBlackout(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        Hit hit = find(mc);
        if (hit == null || hit.dome().subject() == null) return;
        float alpha = hit.dome().black() * hit.edge();
        if (alpha <= 0.0F) return;
        g.fill(0, 0, g.guiWidth(), g.guiHeight(), argb(alpha, 0, 0, 0));
    }

    private static int argb(float a, int r, int g, int b) {
        int ai = Mth.clamp((int) (a * 255.0F), 0, 255);
        return (ai << 24) | (Mth.clamp(r, 0, 255) << 16) | (Mth.clamp(g, 0, 255) << 8) | Mth.clamp(b, 0, 255);
    }
}
