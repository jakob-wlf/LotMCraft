package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.AddMovableEffectPacket;
import de.jakob.lotm.network.packets.toClient.RemoveMovableEffectPacket;
import de.jakob.lotm.network.packets.toClient.UpdateMovableEffectPositionPacket;
import de.jakob.lotm.util.data.Location;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class MovableEffectManager {

    // -------------------------------------------------------------------------
    // Without entity (no time scaling) — original API, unchanged
    // -------------------------------------------------------------------------

    public static UUID playEffect(MovableEffect effect, Location location, int durationTicks,
                                  boolean infinite, ServerLevel level) {
        UUID effectId = UUID.randomUUID();
        Vec3 pos = location.getPosition();
        PacketHandler.sendToAllPlayersInSameLevel(
                new AddMovableEffectPacket(effectId, effect.getIndex(),
                        pos.x, pos.y, pos.z, durationTicks, infinite),
                level
        );
        return effectId;
    }

    public static UUID playEffect(MovableEffect effect, Location location, int durationTicks,
                                  boolean infinite, ServerPlayer player) {
        UUID effectId = UUID.randomUUID();
        Vec3 pos = location.getPosition();
        PacketHandler.sendToPlayer(player,
                new AddMovableEffectPacket(effectId, effect.getIndex(),
                        pos.x, pos.y, pos.z, durationTicks, infinite)
        );
        return effectId;
    }

    // -------------------------------------------------------------------------
    // With entity — client looks the entity up by ID and applies the local
    // time multiplier at its position every tick.
    // -------------------------------------------------------------------------

    /**
     * Play a movable effect for all players in the level, tied to an entity so
     * the client automatically slows/speeds the effect inside time-change areas.
     */
    public static UUID playEffect(MovableEffect effect, Location location, int durationTicks,
                                  boolean infinite, ServerLevel level, LivingEntity entity) {
        UUID effectId = UUID.randomUUID();
        Vec3 pos = location.getPosition();
        PacketHandler.sendToAllPlayersInSameLevel(
                new AddMovableEffectPacket(effectId, effect.getIndex(),
                        pos.x, pos.y, pos.z, durationTicks, infinite, entity.getId()),
                level
        );
        return effectId;
    }

    /**
     * Play a movable effect for a single player, tied to an entity.
     */
    public static UUID playEffect(MovableEffect effect, Location location, int durationTicks,
                                  boolean infinite, ServerPlayer player, LivingEntity entity) {
        UUID effectId = UUID.randomUUID();
        Vec3 pos = location.getPosition();
        PacketHandler.sendToPlayer(player,
                new AddMovableEffectPacket(effectId, effect.getIndex(),
                        pos.x, pos.y, pos.z, durationTicks, infinite, entity.getId())
        );
        return effectId;
    }

    // -------------------------------------------------------------------------
    // Position updates and removal — unchanged
    // -------------------------------------------------------------------------

    public static void updateEffectPosition(UUID effectId, Location newLocation, ServerLevel level) {
        Vec3 pos = newLocation.getPosition();
        PacketHandler.sendToAllPlayersInSameLevel(
                new UpdateMovableEffectPositionPacket(effectId, pos.x, pos.y, pos.z), level);
    }

    public static void updateEffectPosition(UUID effectId, Location newLocation, ServerPlayer player) {
        Vec3 pos = newLocation.getPosition();
        PacketHandler.sendToPlayer(player,
                new UpdateMovableEffectPositionPacket(effectId, pos.x, pos.y, pos.z));
    }

    public static void removeEffect(UUID effectId, ServerLevel level) {
        PacketHandler.sendToAllPlayersInSameLevel(new RemoveMovableEffectPacket(effectId), level);
    }

    public static void removeEffect(UUID effectId, ServerPlayer player) {
        PacketHandler.sendToPlayer(player, new RemoveMovableEffectPacket(effectId));
    }

    // -------------------------------------------------------------------------
    // Effect registry
    // -------------------------------------------------------------------------

    public enum MovableEffect {
        HORROR_AURA(0),
        LIFE_AURA(1);

        private final int index;

        MovableEffect(int index) { this.index = index; }
        public int getIndex() { return index; }
    }
}