package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public final class KeyOfLightStatuePromptRenderer {
    private KeyOfLightStatuePromptRenderer() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.options.hideGui) return;
        if (!"wheel_of_fortune".equalsIgnoreCase(
            ClientBeyonderCache.getPathway(minecraft.player.getUUID()))) return;
        if (!(minecraft.hitResult instanceof BlockHitResult hit)) return;
        if (!minecraft.level.getBlockState(hit.getBlockPos()).is(ModBlocks.KEY_OF_LIGHT_STATUE)) return;

        GuiGraphics graphics = event.getGuiGraphics();
        String prompt = "Attempt to align yourself";
        int x = graphics.guiWidth() / 2 - minecraft.font.width(prompt) / 2;
        int y = graphics.guiHeight() / 2 + 14;
        graphics.drawString(minecraft.font, prompt, x, y, 0xFFE6D7FF, true);
    }
}