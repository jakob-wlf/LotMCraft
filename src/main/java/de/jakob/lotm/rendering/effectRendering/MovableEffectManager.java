package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.AddMovableEffectPacket;
import de.jakob.lotm.network.packets.toClient.UpdateMovableEffectPositionPacket;
import de.jakob.lotm.network.packets.toClient.RemoveMovableEffectPacket;
import de.jakob.lotm.util.data.Location;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class MovableEffectManager {

    /**
     * Play a movable effect at a location
     * @param effect The effect type
     * @param location The initial location
     * @param durationTicks Duration in ticks (ignored if effect is infinite)
     * @param infinite Whether the effect should run indefinitely
     * @param level The server level
     * @return UUID of the effect for later updates/removal
     */
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

    /**
     * Play a movable effect for a specific player
     */
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

    /**
     * Update the position of a movable effect
     */
    public static void updateEffectPosition(UUID effectId, Location newLocation, ServerLevel level) {
        Vec3 pos = newLocation.getPosition();
        PacketHandler.sendToAllPlayersInSameLevel(
            new UpdateMovableEffectPositionPacket(effectId, pos.x, pos.y, pos.z),
            level
        );
    }

    /**
     * Update the position of a movable effect for a specific player
     */
    public static void updateEffectPosition(UUID effectId, Location newLocation, ServerPlayer player) {
        Vec3 pos = newLocation.getPosition();
        PacketHandler.sendToPlayer(player,
            new UpdateMovableEffectPositionPacket(effectId, pos.x, pos.y, pos.z)
        );
    }

    /**
     * Remove a movable effect
     */
    public static void removeEffect(UUID effectId, ServerLevel level) {
        PacketHandler.sendToAllPlayersInSameLevel(
            new RemoveMovableEffectPacket(effectId),
            level
        );
    }

    /**
     * Remove a movable effect for a specific player
     */
    public static void removeEffect(UUID effectId, ServerPlayer player) {
        PacketHandler.sendToPlayer(player,
            new RemoveMovableEffectPacket(effectId)
        );
    }

    public enum MovableEffect {
        HORROR_AURA(0);

        private final int index;

        MovableEffect(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}