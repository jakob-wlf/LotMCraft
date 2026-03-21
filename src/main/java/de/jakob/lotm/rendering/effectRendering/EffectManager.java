package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.AddEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class EffectManager {

    // -------------------------------------------------------------------------
    // Without entity (no time scaling) — original API, unchanged
    // -------------------------------------------------------------------------

    public static void playEffect(Effect effect, double x, double y, double z, ServerLevel level) {
        PacketHandler.sendToAllPlayersInSameLevel(
                new AddEffectPacket(effect.getIndex(), x, y, z), level);
    }

    public static void playEffect(Effect effect, double x, double y, double z, ServerPlayer player) {
        PacketHandler.sendToPlayer(player,
                new AddEffectPacket(effect.getIndex(), x, y, z));
    }

    // -------------------------------------------------------------------------
    // With entity — the client will look the entity up by ID and apply the
    // local time multiplier at its position every tick.
    //
    // NOTE: AddEffectPacket needs an extra optional int field for entityId.
    //       Pass -1 (or Entity.INVALID_ID) when no entity is involved so
    //       existing packet handling stays backward-compatible.
    // -------------------------------------------------------------------------

    /**
     * Play an effect for all players in the level, tied to an entity so the
     * client automatically slows/speeds the effect inside time-change areas.
     *
     * @param entity The entity whose position drives the time multiplier.
     *               Its numeric entity-ID is sent over the network so the
     *               client can look it up from the client-side entity list.
     */
    public static void playEffect(Effect effect, double x, double y, double z,
                                  ServerLevel level, LivingEntity entity) {
        PacketHandler.sendToAllPlayersInSameLevel(
                new AddEffectPacket(effect.getIndex(), x, y, z, entity.getId()), level);
    }

    /**
     * Play an effect for a single player, tied to an entity.
     */
    public static void playEffect(Effect effect, double x, double y, double z,
                                  ServerPlayer player, LivingEntity entity) {
        PacketHandler.sendToPlayer(player,
                new AddEffectPacket(effect.getIndex(), x, y, z, entity.getId()));
    }

    // -------------------------------------------------------------------------
    // Effect registry
    // -------------------------------------------------------------------------

    public enum Effect {
        THUNDER_EXPLOSION(0),
        PURE_WHITE_LIGHT(1),
        CONQUERING(2),
        INFERNO(3),
        FLAME_VORTEX(4),
        EXPLOSION(5),
        COLLAPSE(6),
        APOCALYPSE(7),
        SPACE_FRAGMENTATION(8),
        WAYPOINT(9),
        SPACE_DISTORTION(10),
        HOLY_LIGHT_SMALL(11),
        LIGHT_OF_HOLINESS(12),
        SEFIRAH_CASTLE_PARTICLES(13),
        SEFIRAH_CASTLE(14),
        GIFTING_PARTICLES(15),
        ABILITY_THEFT(16),
        CONCEPTUAL_THEFT(17),
        DECEPTION(18),
        LOOPHOLE(19),
        MISFORTUNE_FIELD(20),
        MISFORTUNE_CURSE(21),
        BLESSING(22),
        NIGHT_DOMAIN(23),
        MIRACLE(24),
        SPIRITUAL_BAPTISM(25),
        CONCEALMENT(26);

        private final int index;

        Effect(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}