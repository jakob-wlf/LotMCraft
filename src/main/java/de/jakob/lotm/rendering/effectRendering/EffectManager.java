package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.AddEffectPacket;
import de.jakob.lotm.network.packets.toClient.CancelEffectByPositionPacket;
import de.jakob.lotm.network.packets.toClient.CancelEffectPacket;
import de.jakob.lotm.network.packets.toClient.UpdateEffectPositionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class EffectManager {

    public static UUID playEffect(int effectId, double x, double y, double z, ServerLevel level) {
        return playEffect(effectId, x, y, z, level, null, false, null);
    }

    public static UUID playEffect(int effectId, double x, double y, double z, ServerPlayer player) {
        return playEffect(effectId, x, y, z, player, null, false, null);
    }

    public static UUID playEffect(int effectId, double x, double y, double z, ServerLevel level, EffectParams params) {
        return playEffect(effectId, x, y, z, level, null, false, params);
    }

    public static UUID playEffect(int effectId, double x, double y, double z, ServerPlayer player, EffectParams params) {
        return playEffect(effectId, x, y, z, player, null, false, params);
    }

    public static UUID playEffect(int effectId, double x, double y, double z, ServerLevel level, LivingEntity timeReferenceEntity) {
        return playEffect(effectId, x, y, z, level, timeReferenceEntity, false, null);
    }

    public static UUID playEffect(int effectId, double x, double y, double z, ServerPlayer player, LivingEntity timeReferenceEntity) {
        return playEffect(effectId, x, y, z, player, timeReferenceEntity, false, null);
    }

    public static UUID playEffect(int effectId, double x, double y, double z, ServerLevel level, LivingEntity timeReferenceEntity, EffectParams params) {
        return playEffect(effectId, x, y, z, level, timeReferenceEntity, false, params);
    }

    public static UUID playEffect(int effectId, double x, double y, double z, ServerPlayer player, LivingEntity timeReferenceEntity, EffectParams params) {
        return playEffect(effectId, x, y, z, player, timeReferenceEntity, false, params);
    }

    public static UUID playMovableEffect(int effectId, ServerLevel level, LivingEntity followEntity) {
        Vec3 pos = followEntity.position();
        return playEffect(effectId, pos.x, pos.y, pos.z, level, followEntity, true, null);
    }

    public static UUID playMovableEffect(int effectId, ServerPlayer player, LivingEntity followEntity) {
        Vec3 pos = followEntity.position();
        return playEffect(effectId, pos.x, pos.y, pos.z, player, followEntity, true, null);
    }

    public static UUID playMovableEffect(int effectId, ServerLevel level, LivingEntity followEntity, EffectParams params) {
        Vec3 pos = followEntity.position();
        return playEffect(effectId, pos.x, pos.y, pos.z, level, followEntity, true, params);
    }

    public static UUID playMovableEffect(int effectId, ServerPlayer player, LivingEntity followEntity, EffectParams params) {
        Vec3 pos = followEntity.position();
        return playEffect(effectId, pos.x, pos.y, pos.z, player, followEntity, true, params);
    }

    public static UUID playEffect(int effectId, double x, double y, double z, ServerLevel level,
                                  LivingEntity entity, boolean followEntity, EffectParams params) {
        UUID id = UUID.randomUUID();
        PacketHandler.sendToAllPlayersInSameLevel(toPacket(id, effectId, x, y, z, entity, followEntity, params), level);
        return id;
    }

    public static UUID playEffect(int effectId, double x, double y, double z, ServerPlayer player,
                                  LivingEntity entity, boolean followEntity, EffectParams params) {
        UUID id = UUID.randomUUID();
        PacketHandler.sendToPlayer(player, toPacket(id, effectId, x, y, z, entity, followEntity, params));
        return id;
    }

    /**
     * Same as playMovableEffect(effectId, player, followEntity, params), but lets the
     * caller supply the effect UUID so multiple per-player sends (one per viewer) can
     * share one id and later be updated/cancelled together with a single loop.
     */
    public static void playMovableEffectWithId(UUID id, int effectId, ServerPlayer player,
                                                LivingEntity followEntity, EffectParams params) {
        Vec3 pos = followEntity.position();
        PacketHandler.sendToPlayer(player, toPacket(id, effectId, pos.x, pos.y, pos.z, followEntity, true, params));
    }

    public static UUID playDirectionalEffect(int effectId,
                                             double startX, double startY, double startZ,
                                             double endX, double endY, double endZ,
                                             Integer duration, ServerLevel level) {
        return playDirectionalEffect(effectId, startX, startY, startZ, endX, endY, endZ, duration, level, null);
    }

    public static UUID playDirectionalEffect(int effectId,
                                             double startX, double startY, double startZ,
                                             double endX, double endY, double endZ,
                                             Integer duration, ServerPlayer player) {
        return playDirectionalEffect(effectId, startX, startY, startZ, endX, endY, endZ, duration, player, null);
    }

    public static UUID playDirectionalEffect(int effectId,
                                             double startX, double startY, double startZ,
                                             double endX, double endY, double endZ,
                                             Integer duration, ServerLevel level, LivingEntity entity) {
        EffectParams params = EffectParams.direction(duration, startX, startY, startZ, endX, endY, endZ);
        return playEffect(effectId, startX, startY, startZ, level, entity, false, params);
    }

    public static UUID playDirectionalEffect(int effectId,
                                             double startX, double startY, double startZ,
                                             double endX, double endY, double endZ,
                                             Integer duration, ServerPlayer player, LivingEntity entity) {
        EffectParams params = EffectParams.direction(duration, startX, startY, startZ, endX, endY, endZ);
        return playEffect(effectId, startX, startY, startZ, player, entity, false, params);
    }

    public static void updateEffectPosition(UUID effectId, double x, double y, double z, ServerLevel level) {
        PacketHandler.sendToAllPlayersInSameLevel(new UpdateEffectPositionPacket(effectId, x, y, z), level);
    }

    public static void updateEffectPosition(UUID effectId, double x, double y, double z, ServerPlayer player) {
        PacketHandler.sendToPlayer(player, new UpdateEffectPositionPacket(effectId, x, y, z));
    }

    public static void cancelEffect(UUID effectId, ServerLevel level) {
        PacketHandler.sendToAllPlayersInSameLevel(new CancelEffectPacket(effectId), level);
    }

    public static void cancelEffect(UUID effectId, ServerPlayer player) {
        PacketHandler.sendToPlayer(player, new CancelEffectPacket(effectId));
    }

    public static void cancelEffectsNear(double x, double y, double z, double radius, ServerLevel level) {
        PacketHandler.sendToAllPlayersInSameLevel(
                new CancelEffectByPositionPacket(x, y, z, radius), level);
    }

    private static AddEffectPacket toPacket(UUID id, int effectId, double x, double y, double z,
                                            LivingEntity entity, boolean followEntity, EffectParams params) {
        int entityId = entity == null ? AddEffectPacket.NO_ENTITY : entity.getId();
        Integer duration = params == null ? null : params.duration();
        Boolean infinite = params == null ? null : params.infinite();
        float[] arr = (params == null || params.params() == null) ? EffectParams.defaultParamsArray() : params.params();
        return new AddEffectPacket(id, effectId, x, y, z, entityId, followEntity,
                duration == null ? AddEffectPacket.NO_DURATION_OVERRIDE : duration,
                infinite != null && infinite,
                infinite != null,
                arr);
    }
}