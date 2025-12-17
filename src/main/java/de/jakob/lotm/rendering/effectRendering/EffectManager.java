package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.AddEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class EffectManager {

    public static void playEffect(Effect effect, double x, double y, double z, ServerLevel level) {
        PacketHandler.sendToAllPlayersInSameLevel(new AddEffectPacket(effect.getIndex(), x, y, z), level);
    }

    public static void playEffect(Effect effect, double x, double y, double z, ServerPlayer player) {
        PacketHandler.sendToPlayer(player, new AddEffectPacket(effect.getIndex(), x, y, z));
    }

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
        NIGHT_DOMAIN(23);

        private final int index;

        Effect(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}
