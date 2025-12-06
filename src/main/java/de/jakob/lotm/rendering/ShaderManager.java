package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ActiveShaderComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class ShaderManager {
    
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            
            if (player == null) {
                removeShader(mc);
                return;
            }
            
            // Check in priority order - shattered glass takes precedence
            if (shouldApplyShatteredGlass(player)) {
                applyShader(mc, "shattered_glass");
            } else if (shouldApplySanityShader(player)) {
                applyShader(mc, "sanity_loss");
            } else if (shouldApplyAbyssalDistortion(player)) {
                applyShader(mc, "abyssal_distortion");
            } else if (shouldApplyHolyEffect(player)) {
                applyShader(mc, "holy_effect");
            } else if (shouldApplyDrought(player)) {
                applyShader(mc, "drought_effect");
            } else if (shouldApplyBlizzard(player)) {
                applyShader(mc, "blizzard_effect");
            } else if (shouldApplyHistoricalVoid(player)) {
                applyShader(mc, "fog_of_history");
            } else if (shouldApplySunKingdomShader(player)) {
                applyShader(mc, "holy_effect");
            }else {
                removeShader(mc);
            }
        }
    }

    private static boolean shouldApplySunKingdomShader(Player player) {
        return player.getData(ModAttachments.SHADER_COMPONENT.get()).isShaderActive() &&
                player.getData(ModAttachments.SHADER_COMPONENT.get()).getShaderIndex()
                        == ActiveShaderComponent.SHADERTYPE.SUN_KINGDOM.getIndex();
    }

    private static boolean shouldApplySanityShader(Player player) {
        return player.getData(ModAttachments.SANITY_COMPONENT.get()).getSanity() < .5f;
    }


    private static boolean shouldApplyShatteredGlass(Player player) {
        return player.getData(ModAttachments.MIRROR_WORLD_COMPONENT.get()).isInMirrorWorld();
    }

    private static boolean shouldApplyDrought(Player player) {
        return player.getData(ModAttachments.SHADER_COMPONENT.get()).isShaderActive() &&
                player.getData(ModAttachments.SHADER_COMPONENT.get()).getShaderIndex()
                        == ActiveShaderComponent.SHADERTYPE.DROUGHT.getIndex();
    }

    private static boolean shouldApplyBlizzard(Player player) {
        return player.getData(ModAttachments.SHADER_COMPONENT.get()).isShaderActive() &&
                player.getData(ModAttachments.SHADER_COMPONENT.get()).getShaderIndex()
                        == ActiveShaderComponent.SHADERTYPE.BLIZZARD.getIndex();
    }

    private static boolean shouldApplyAbyssalDistortion(Player player) {
        return player.getData(ModAttachments.TRANSFORMATION_COMPONENT.get()).isTransformed() &&
               player.getData(ModAttachments.TRANSFORMATION_COMPONENT.get()).getTransformationIndex() 
                   == TransformationComponent.TransformationType.DESIRE_AVATAR.getIndex();
    }

    private static boolean shouldApplyHistoricalVoid(Player player) {
        return player.getData(ModAttachments.TRANSFORMATION_COMPONENT.get()).isTransformed() &&
                player.getData(ModAttachments.TRANSFORMATION_COMPONENT.get()).getTransformationIndex()
                        == TransformationComponent.TransformationType.FOG_OF_HISTORY.getIndex();
    }

    private static boolean shouldApplyHolyEffect(Player player) {
        return player.getData(ModAttachments.TRANSFORMATION_COMPONENT.get()).isTransformed() &&
                player.getData(ModAttachments.TRANSFORMATION_COMPONENT.get()).getTransformationIndex()
                        == TransformationComponent.TransformationType.SOLAR_ENVOY.getIndex();
    }
    
    private static void applyShader(Minecraft mc, String shaderName) {
        if (mc.gameRenderer.currentEffect() == null ||
            !mc.gameRenderer.currentEffect().getName().equals(shaderName)) {
            try {
                mc.gameRenderer.loadEffect(
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "shaders/post/" + shaderName + ".json")
                );
            } catch (Exception e) {
                LOTMCraft.LOGGER.error("Failed to load shader: " + shaderName, e);
            }
        }
    }
    
    private static void removeShader(Minecraft mc) {
        if (mc.gameRenderer.currentEffect() != null) {
            mc.gameRenderer.shutdownEffect();
        }
    }
}