package de.jakob.lotm.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.AbilityItemHandler;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.red_priest.CullAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.HashSet;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class CullOverlay {

    public static HashSet<UUID> playersWithCullActivated = new HashSet<>();

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "cull_overlay"), (guiGraphics, deltaTracker) -> {
            renderOverlay(guiGraphics);
        });
    }

    private static void renderOverlay(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        if (playersWithCullActivated.contains(mc.player.getUUID())) {
            ResourceLocation backgroundTexture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/cull_overlay.png");
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
    }

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null || !((ToggleAbility) LOTMCraft.abilityHandler.getById("cull_ability")).isActiveForEntity(localPlayer)) {
            return;
        }

        if (event.getEntity() != localPlayer) {
            RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f);
        }
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null || !((ToggleAbility) LOTMCraft.abilityHandler.getById("cull_ability")).isActiveForEntity(localPlayer)) {
            return;
        }

        if (event.getEntity() != localPlayer) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}