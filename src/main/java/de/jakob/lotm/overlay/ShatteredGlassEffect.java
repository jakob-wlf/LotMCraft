package de.jakob.lotm.overlay;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ShatteredGlassEffect {
    
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            
            if (player != null && shouldApplyEffect(player)) {
                applyShader(mc);
            } else {
                removeShader(mc);
            }
        }
    }
    
    private static boolean shouldApplyEffect(Player player) {
        return player.getData(ModAttachments.MIRROR_WORLD_COMPONENT.get()).isInMirrorWorld();
    }
    
    private static void applyShader(Minecraft mc) {
        if (mc.gameRenderer.currentEffect() == null ||
            !mc.gameRenderer.currentEffect().getName().equals("shattered_glass")) {
            try {
                mc.gameRenderer.loadEffect(
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "shaders/post/shattered_glass.json")
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void removeShader(Minecraft mc) {
        if (mc.gameRenderer.currentEffect() != null) {
            mc.gameRenderer.shutdownEffect();
        }
    }
}