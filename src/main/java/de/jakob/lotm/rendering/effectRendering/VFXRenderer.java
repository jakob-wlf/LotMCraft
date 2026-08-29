package de.jakob.lotm.rendering.effectRendering;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class VFXRenderer {

    private static final Map<UUID, ActiveEffect> activeEffects = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        PoseStack poseStack = event.getPoseStack();
        var camPos = event.getCamera().getPosition();

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        Iterator<Map.Entry<UUID, ActiveEffect>> it = activeEffects.entrySet().iterator();
        while (it.hasNext()) {
            ActiveEffect effect = it.next().getValue();
            effect.update(poseStack, event.getPartialTick().getGameTimeDeltaPartialTick(true));
            if (effect.isFinished()) it.remove();
        }

        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hasSingleplayerServer() && mc.isPaused()) return;
        activeEffects.values().forEach(ActiveEffect::tick);
    }

    public static void addActiveEffect(UUID id, int effectIndex, double x, double y, double z,
                                       LivingEntity entity, boolean followEntity, EffectParams overrides) {
        ActiveEffect effect = EffectFactory.createEffect(effectIndex, id, x, y, z, entity, followEntity, overrides);
        activeEffects.put(effect.getId(), effect);
    }

    public static void updateEffectPosition(UUID id, double x, double y, double z) {
        ActiveEffect effect = activeEffects.get(id);
        if (effect != null) effect.setPosition(x, y, z);
    }

    public static void cancelEffect(UUID id) {
        ActiveEffect effect = activeEffects.get(id);
        if (effect != null) effect.cancel();
    }

    public static void cancelEffectsNear(double x, double y, double z, double radius) {
        double r2 = radius * radius;
        for (ActiveEffect effect : activeEffects.values()) {
            double dx = effect.getX() - x, dy = effect.getY() - y, dz = effect.getZ() - z;
            if (dx * dx + dy * dy + dz * dz <= r2) {
                effect.cancel();
            }
        }
    }

    public static void clearActiveEffects() {
        activeEffects.clear();
    }
}