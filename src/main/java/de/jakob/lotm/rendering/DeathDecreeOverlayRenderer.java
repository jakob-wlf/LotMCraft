package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Darkens the screen progressively as Death Decree stacks accumulate on the local
 * player. Amplifier is stacks - 1 (see BeyonderDataTickHandler), so amplifier 0 is the
 * first stack. Each stack already reads as clearly darker than the last (see
 * ALPHA_PER_STACK) rather than scaling smoothly, so the jump in severity is obvious.
 *
 * This class is Dist.CLIENT-only (see @EventBusSubscriber below) and must never be
 * referenced from common-side code — that keeps it unloaded on dedicated servers,
 * where Minecraft/GuiGraphics do not exist and would crash the JVM if touched.
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class DeathDecreeOverlayRenderer {

    private static final int[] ALPHA_PER_STACK = {90, 190, 245};

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "death_decree_overlay"), (guiGraphics, deltaTracker) -> {
            renderOverlay(guiGraphics);
        });
    }

    private static void renderOverlay(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        MobEffectInstance mark = mc.player.getEffect(ModEffects.DEATH_DECREE_MARK);
        if (mark == null) return;

        int stacks = mark.getAmplifier() + 1;
        int alpha = ALPHA_PER_STACK[Math.min(stacks, ALPHA_PER_STACK.length) - 1];

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        guiGraphics.fill(0, 0, screenWidth, screenHeight, (alpha << 24));
    }
}
