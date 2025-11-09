package de.jakob.lotm.rendering.effectRendering;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.HashSet;
import java.util.Iterator;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class VFXRenderer {
    private static final HashSet<ActiveEffect> activeEffects = new HashSet<>();

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            PoseStack poseStack = event.getPoseStack();
            Camera camera = event.getCamera();
            Vec3 camPos = camera.getPosition();

            poseStack.pushPose();
            poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

            // Render all active effects
            Iterator<ActiveEffect> iterator = activeEffects.iterator();
            while (iterator.hasNext()) {
                ActiveEffect effect = iterator.next();
                effect.update(poseStack, event.getPartialTick().getGameTimeDeltaPartialTick(false));

                // Remove finished effects
                if (effect.isFinished()) {
                    iterator.remove();
                }
            }

            poseStack.popPose();
        }
    }

    public static void addActiveEffect(int effectIndex, double x, double y, double z) {
        activeEffects.add(EffectFactory.createEffect(effectIndex, x, y, z));
    }

    public static void clearActiveEffects() {
        activeEffects.clear();
    }
}