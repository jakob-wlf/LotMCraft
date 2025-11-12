package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.AddEffectPacket;
import net.minecraft.server.level.ServerLevel;

public class EffectManager {

    public static void playEffect(Effect effect, double x, double y, double z, ServerLevel level) {
        PacketHandler.sendToAllPlayersInSameLevel(new AddEffectPacket(effect.getIndex(), x, y, z), level);
    }

    public enum Effect {
        THUNDER_EXPLOSION(0),
        PURE_WHITE_LIGHT(1),
        CONQUERING(2),
        INFERNO(3),
        FLAME_VORTEX(4);

        private final int index;

        Effect(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}
