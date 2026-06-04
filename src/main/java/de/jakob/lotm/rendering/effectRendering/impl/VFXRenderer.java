package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import de.jakob.lotm.network.packets.toClient.AddDirectionalEffectPacket;
import de.jakob.lotm.network.packets.toClient.AddMovableEffectPacket;
import de.jakob.lotm.rendering.effectRendering.*;
import de.jakob.lotm.util.data.EntityLocation;
import de.jakob.lotm.util.data.Location;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

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

            // Sky pillar — always visible from any distance (placed at camera +
            // direction * clamped distance so it is never frustum-culled).
            SefirahSkyBeamEffect skyBeam = getActiveSkyBeamEffect();
            if (skyBeam != null) {
                renderSkyBeamPillar(poseStack, camPos, skyBeam);
            }

            poseStack.popPose();
        }
    }

    private static final Map<UUID, ActiveMovableEffect> activeMovableEffects = new HashMap<>();

    /**
     * Entity IDs for ALL entity-tracked movable effects.
     * Refreshed every client tick: if the entity is currently loaded, the
     * effect's location is updated to a fresh {@link EntityLocation} so
     * tracking resumes after the entity re-enters render distance.
     */
    private static final Map<UUID, Integer> trackedEntityIds = new HashMap<>();


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

        // Refresh entity-tracking for all tracked effects every tick.
        // If the entity is loaded on this client, update the location reference
        // so the effect follows it live.  If it has unloaded, we simply leave
        // the last known location intact until it comes back.
        if (!trackedEntityIds.isEmpty() && mc.level != null) {
            for (Map.Entry<UUID, Integer> entry : trackedEntityIds.entrySet()) {
                net.minecraft.world.entity.Entity raw = mc.level.getEntity(entry.getValue());
                if (raw instanceof LivingEntity le) {
                    ActiveMovableEffect effect = activeMovableEffects.get(entry.getKey());
                    if (effect != null) {
                        effect.setLocation(new EntityLocation(le));
                    }
                }
            }
        }
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

        // If entity lookup failed (entity not yet tracked on this client),
        // queue a deferred retry so the location is upgraded once it loads.
        if (entityId != AddMovableEffectPacket.NO_ENTITY) {
            trackedEntityIds.put(effectId, entityId);
        }
    }

    public static void updateMovableEffectPosition(UUID effectId, double x, double y, double z) {
        ActiveMovableEffect effect = activeMovableEffects.get(effectId);
        if (effect != null) {
            // Replace with a plain Location so the update always takes effect.
            // EntityLocation.setPosition() is a no-op (getPosition() always
            // returns entity.position() and ignores the stored field).
            // trackedEntityIds will re-upgrade to EntityLocation next tick if
            // the entity is currently loaded.
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                effect.setLocation(new Location(new Vec3(x, y, z), mc.level));
            }
        }
    }

    public static void removeMovableEffect(UUID effectId) {
        activeMovableEffects.remove(effectId);
        trackedEntityIds.remove(effectId);
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

    /** Clear all client-side VFX state when the player disconnects. */
    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clearActiveEffects();
    }

    public static void clearActiveEffects() {
        activeEffects.clear();
        activeDirectionalEffects.clear();
        activeMovableEffects.clear();
        trackedEntityIds.clear();
    }

    // -------------------------------------------------------------------------
    // Sky-beam atmospheric tint — visible to all players in the level
    // regardless of their distance from the ritual player.
    // Works the same way as ApotheosisRenderer: fires every frame per-player
    // via ViewportEvent, which is purely client-side and distance-independent.
    // -------------------------------------------------------------------------

    private static SefirahSkyBeamEffect getActiveSkyBeamEffect() {
        for (ActiveMovableEffect e : activeMovableEffects.values()) {
            if (e instanceof SefirahSkyBeamEffect beam) return beam;
        }
        return null;
    }

    /** Blend strength of the ritual color tint applied to the atmosphere. */
    private static final float BEAM_FOG_BLEND = 0.30f;

    /**
     * Maximum world-distance at which the sky pillar is "pinned" in front of
     * the camera. Any player farther than this will still see the pillar at
     * this fixed virtual distance, keeping it inside the GPU view frustum.
     */
    private static final float SKY_PILLAR_MAX_DIST = 600f;
    /** Half-angle (radians) of the pillar's angular width. ~3.4 degrees. */
    private static final float SKY_PILLAR_HALF_ANGLE = 0.06f;

    /**
     * Renders a tall glow pillar in the direction of {@code beam} from the
     * camera.  The geometry is placed at most {@link #SKY_PILLAR_MAX_DIST}
     * blocks away, so it is never frustum-culled even at 20 000+ block ranges.
     * Additive blending means it does not occlude terrain — it just adds light.
     * <p>The pillar fades in between 30-150 blocks so it does not overlay the
     * actual cylinder geometry when the player is standing at the ritual.
     *
     * @param poseStack already has the {@code translate(-camPos)} applied
     */
    private static void renderSkyBeamPillar(PoseStack poseStack, Vec3 camPos, SefirahSkyBeamEffect beam) {
        double dx = beam.getX() - camPos.x;
        double dz = beam.getZ() - camPos.z;
        double dist2D = Math.sqrt(dx * dx + dz * dz);
        if (dist2D < 30.0) return;

        float nx = (float)(dx / dist2D);
        float nz = (float)(dz / dist2D);

        float renderDist = (float) Math.min(dist2D, SKY_PILLAR_MAX_DIST);
        float halfWidth  = renderDist * SKY_PILLAR_HALF_ANGLE;

        // Fade in so the pillar doesn't clash with local geometry.
        float alpha = (float) Math.min(1.0, Math.max(0.0, (dist2D - 30.0) / 120.0));

        float bx = (float)(camPos.x + nx * renderDist);
        float by = (float) beam.getY();
        float bz = (float)(camPos.z + nz * renderDist);

        // Perpendicular direction in XZ for billboard width.
        float perpX = -nz;
        float perpZ =  nx;

        float[] color = beam.computeColor();
        float cr = color[0], cg = color[1], cb = color[2];
        float height = 400f;

        Matrix4f m = poseStack.last().pose();

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
        RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // Outer wide glow layer — full pathway color.
        buf.addVertex(m, bx - perpX * halfWidth * 2, by,          bz - perpZ * halfWidth * 2).setColor(cr, cg, cb, 0f);
        buf.addVertex(m, bx + perpX * halfWidth * 2, by,          bz + perpZ * halfWidth * 2).setColor(cr, cg, cb, 0f);
        buf.addVertex(m, bx + perpX * halfWidth,     by + height, bz + perpZ * halfWidth    ).setColor(cr, cg, cb, 0.40f * alpha);
        buf.addVertex(m, bx - perpX * halfWidth,     by + height, bz - perpZ * halfWidth    ).setColor(cr, cg, cb, 0.40f * alpha);

        // Bright white core streak.
        buf.addVertex(m, bx - perpX * halfWidth * 0.35f, by,          bz - perpZ * halfWidth * 0.35f).setColor(1f, 1f, 1f, 0f);
        buf.addVertex(m, bx + perpX * halfWidth * 0.35f, by,          bz + perpZ * halfWidth * 0.35f).setColor(1f, 1f, 1f, 0f);
        buf.addVertex(m, bx + perpX * halfWidth * 0.25f, by + height, bz + perpZ * halfWidth * 0.25f).setColor(1f, 1f, 1f, 0.85f * alpha);
        buf.addVertex(m, bx - perpX * halfWidth * 0.25f, by + height, bz - perpZ * halfWidth * 0.25f).setColor(1f, 1f, 1f, 0.85f * alpha);

        BufferUploader.drawWithShader(buf.buildOrThrow());

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        SefirahSkyBeamEffect beam = getActiveSkyBeamEffect();
        if (beam == null) return;

        float[] color = beam.computeColor();
        // Lerp fog toward the beam color so distant players see the sky tint.
        event.setRed((float)  (event.getRed()   + (color[0] - event.getRed())   * BEAM_FOG_BLEND));
        event.setGreen((float)(event.getGreen() + (color[1] - event.getGreen()) * BEAM_FOG_BLEND));
        event.setBlue((float) (event.getBlue()  + (color[2] - event.getBlue())  * BEAM_FOG_BLEND));
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