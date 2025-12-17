package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.effectRendering.ActiveDirectionalEffect;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import de.jakob.lotm.rendering.effectRendering.DirectionalEffectFactory;
import de.jakob.lotm.rendering.effectRendering.EffectFactory;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.HashSet;
import java.util.Iterator;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class VFXRenderer {
    private static final HashSet<ActiveEffect> activeEffects = new HashSet<>();
    private static final HashSet<ActiveDirectionalEffect> activeDirectionalEffects = new HashSet<>(); // ADD THIS

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
                effect.update(poseStack, event.getPartialTick().getGameTimeDeltaPartialTick(true));

                if (effect.isFinished()) {
                    iterator.remove();
                }
            }

            // Render all active directional effects - ADD THIS
            Iterator<ActiveDirectionalEffect> dirIterator = activeDirectionalEffects.iterator();
            while (dirIterator.hasNext()) {
                ActiveDirectionalEffect effect = dirIterator.next();
                effect.update(poseStack, event.getPartialTick().getGameTimeDeltaPartialTick(true));

                if (effect.isFinished()) {
                    dirIterator.remove();
                }
            }

            poseStack.popPose();
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        // Only pause effects in singleplayer when paused
        // On servers, effects should always progress
        if (mc.hasSingleplayerServer() && mc.isPaused()) {
            return;
        }

        activeEffects.forEach(ActiveEffect::tick);
        activeDirectionalEffects.forEach(ActiveDirectionalEffect::tick);
    }

    public static void addActiveEffect(int effectIndex, double x, double y, double z) {
        activeEffects.add(EffectFactory.createEffect(effectIndex, x, y, z));
    }

    // ADD THIS METHOD
    public static void addActiveDirectionalEffect(int effectIndex,
                                                  double startX, double startY, double startZ,
                                                  double endX, double endY, double endZ,
                                                  int duration) {
        activeDirectionalEffects.add(
                DirectionalEffectFactory.createEffect(effectIndex,
                        startX, startY, startZ,
                        endX, endY, endZ,
                        duration)
        );
    }

    public static void clearActiveEffects() {
        activeEffects.clear();
        activeDirectionalEffects.clear(); // ADD THIS
    }
}