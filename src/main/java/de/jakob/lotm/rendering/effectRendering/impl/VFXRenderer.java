package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.toClient.AddDirectionalEffectPacket;
import de.jakob.lotm.network.packets.toClient.AddMovableEffectPacket;
import de.jakob.lotm.rendering.effectRendering.*;
import de.jakob.lotm.util.data.Location;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.*;

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

            Iterator<Map.Entry<UUID, ActiveMovableEffect>> movableIterator = activeMovableEffects.entrySet().iterator();
            while (movableIterator.hasNext()) {
                Map.Entry<UUID, ActiveMovableEffect> entry = movableIterator.next();
                ActiveMovableEffect effect = entry.getValue();
                effect.update(poseStack, event.getPartialTick().getGameTimeDeltaPartialTick(false));

                if (effect.isFinished()) {
                    movableIterator.remove();
                }
            }


            poseStack.popPose();
        }
    }

    private static final Map<UUID, ActiveMovableEffect> activeMovableEffects = new HashMap<>();


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
        activeMovableEffects.values().forEach(ActiveMovableEffect::tick);
    }

    public static void addActiveMovableEffect(UUID effectId, int effectIndex,
                                              double x, double y, double z,
                                              int duration, boolean infinite) {
        // No entity — fall back to unscaled effect
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Location location = new Location(new Vec3(x, y, z), mc.level);
        ActiveMovableEffect effect = MovableEffectFactory.createEffect(effectIndex, location, duration, infinite);
        activeMovableEffects.put(effectId, effect);
    }

    public static void addActiveMovableEffect(UUID effectId, int effectIndex,
                                              double x, double y, double z,
                                              int duration, boolean infinite,
                                              int entityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            // Level not ready — fall back to unscaled effect
            addActiveMovableEffect(effectId, effectIndex, x, y, z, duration, infinite);
            return;
        }

        LivingEntity entity = null;
        if (entityId != AddMovableEffectPacket.NO_ENTITY) {
            net.minecraft.world.entity.Entity raw = mc.level.getEntity(entityId);
            if (raw instanceof LivingEntity le) {
                entity = le;
            }
        }

        Location location = new Location(new Vec3(x, y, z), mc.level);
        // entity may be null here — MovableEffectFactory handles that gracefully
        ActiveMovableEffect effect = MovableEffectFactory.createEffect(effectIndex, location, duration, infinite, entity);
        activeMovableEffects.put(effectId, effect);
    }

    public static void updateMovableEffectPosition(UUID effectId, double x, double y, double z) {
        ActiveMovableEffect effect = activeMovableEffects.get(effectId);
        if (effect != null) {
            effect.setPosition(x, y, z);
        }
    }

    public static void removeMovableEffect(UUID effectId) {
        activeMovableEffects.remove(effectId);
    }

    public static void cancelEffectsNear(double x, double y, double z, double radius) {
        for (ActiveEffect effect : activeEffects) {
            double dx = effect.getX() - x;
            double dy = effect.getY() - y;
            double dz = effect.getZ() - z;
            if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                effect.cancel();
            }
        }
        for (ActiveMovableEffect effect : activeMovableEffects.values()) {
            double dx = effect.getX() - x;
            double dy = effect.getY() - y;
            double dz = effect.getZ() - z;
            if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                effect.cancel();
            }
        }
        for (ActiveDirectionalEffect effect : activeDirectionalEffects) {
            double midX = (effect.getStartX() + effect.getEndX()) / 2.0;
            double midY = (effect.getStartY() + effect.getEndY()) / 2.0;
            double midZ = (effect.getStartZ() + effect.getEndZ()) / 2.0;
            double dx = midX - x;
            double dy = midY - y;
            double dz = midZ - z;
            if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                effect.cancel();
            }
        }
    }

    // Update clearActiveEffects method to include movable effects
    public static void clearActiveEffects() {
        activeEffects.clear();
        activeDirectionalEffects.clear();
        activeMovableEffects.clear(); // ADD THIS
    }

    public static void addActiveEffect(int effectIndex, double x, double y, double z) {
        activeEffects.add(EffectFactory.createEffect(effectIndex, x, y, z));
    }

    /**
     * Add an effect whose playback speed is continuously driven by the local
     * time multiplier at {@code entityId}'s position.
     * <p>
     * Called by the packet handler when the server sends an {@code AddEffectPacket}
     * that includes an entity ID. The entity is looked up from the client-side
     * level; if it cannot be found (e.g. not yet loaded) the effect falls back
     * to normal speed via {@link EffectFactory#createEffect(int, double, double, double)}.
     *
     * @param effectIndex Index into the effect registry
     * @param x           Spawn X coordinate
     * @param y           Spawn Y coordinate
     * @param z           Spawn Z coordinate
     * @param entityId    Numeric entity ID from {@link net.minecraft.world.entity.Entity#getId()}.
     *                    Pass -1 to get the same behaviour as the no-entity overload.
     */
    public static void addActiveEffect(int effectIndex,
                                       double x, double y, double z,
                                       int entityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            // Level not ready — fall back to unscaled effect
            activeEffects.add(EffectFactory.createEffect(effectIndex, x, y, z));
            return;
        }

        LivingEntity entity = null;
        if (entityId != -1) {
            net.minecraft.world.entity.Entity raw = mc.level.getEntity(entityId);
            if (raw instanceof LivingEntity le) {
                entity = le;
            }
        }

        // entity may be null here — EffectFactory handles that gracefully
        activeEffects.add(EffectFactory.createEffect(effectIndex, x, y, z, entity));
    }

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

    public static void addActiveDirectionalEffect(int effectIndex,
                                                  double startX, double startY, double startZ,
                                                  double endX, double endY, double endZ,
                                                  int duration, int entityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            // Level not ready — fall back to unscaled effect
            addActiveDirectionalEffect(effectIndex, startX, startY, startZ, endX, endY, endZ, duration);
            return;
        }

        LivingEntity entity = null;
        if (entityId != AddDirectionalEffectPacket.NO_ENTITY) {
            net.minecraft.world.entity.Entity raw = mc.level.getEntity(entityId);
            if (raw instanceof LivingEntity le) {
                entity = le;
            }
        }

        // entity may be null here — DirectionalEffectFactory handles that gracefully
        activeDirectionalEffects.add(
                DirectionalEffectFactory.createEffect(effectIndex,
                        startX, startY, startZ,
                        endX, endY, endZ,
                        duration, entity)
        );
    }
}