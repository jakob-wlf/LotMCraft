package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.AddDirectionalEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class DirectionalEffectManager {

    // -------------------------------------------------------------------------
    // Without entity (no time scaling) — original API, unchanged
    // -------------------------------------------------------------------------

    public static void playEffect(DirectionalEffect effect,
                                  double startX, double startY, double startZ,
                                  double endX, double endY, double endZ,
                                  int durationTicks, ServerLevel level) {
        PacketHandler.sendToAllPlayersInSameLevel(
                new AddDirectionalEffectPacket(effect.getIndex(),
                        startX, startY, startZ,
                        endX, endY, endZ,
                        durationTicks),
                level
        );
    }

    public static void playEffect(DirectionalEffect effect,
                                  double startX, double startY, double startZ,
                                  double endX, double endY, double endZ,
                                  int durationTicks, ServerPlayer player) {
        PacketHandler.sendToPlayer(player,
                new AddDirectionalEffectPacket(effect.getIndex(),
                        startX, startY, startZ,
                        endX, endY, endZ,
                        durationTicks)
        );
    }

    // -------------------------------------------------------------------------
    // With entity — client looks the entity up by ID and applies the local
    // time multiplier at its position every tick.
    // -------------------------------------------------------------------------

    /**
     * Play a directional effect for all players in the level, tied to an entity
     * so the client automatically slows/speeds the effect inside time-change areas.
     */
    public static void playEffect(DirectionalEffect effect,
                                  double startX, double startY, double startZ,
                                  double endX, double endY, double endZ,
                                  int durationTicks, ServerLevel level,
                                  LivingEntity entity) {
        PacketHandler.sendToAllPlayersInSameLevel(
                new AddDirectionalEffectPacket(effect.getIndex(),
                        startX, startY, startZ,
                        endX, endY, endZ,
                        durationTicks, entity.getId()),
                level
        );
    }

    /**
     * Play a directional effect for a single player, tied to an entity.
     */
    public static void playEffect(DirectionalEffect effect,
                                  double startX, double startY, double startZ,
                                  double endX, double endY, double endZ,
                                  int durationTicks, ServerPlayer player,
                                  LivingEntity entity) {
        PacketHandler.sendToPlayer(player,
                new AddDirectionalEffectPacket(effect.getIndex(),
                        startX, startY, startZ,
                        endX, endY, endZ,
                        durationTicks, entity.getId())
        );
    }

    // -------------------------------------------------------------------------
    // Effect registry
    // -------------------------------------------------------------------------

    public enum DirectionalEffect {
        FATE_SIPHONING(0);

        private final int index;

        DirectionalEffect(int index) { this.index = index; }
        public int getIndex() { return index; }
    }
}