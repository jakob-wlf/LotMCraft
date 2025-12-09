package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.AddDirectionalEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class DirectionalEffectManager {

    public static void playEffect(DirectionalEffect effect, 
                                 double startX, double startY, double startZ,
                                 double endX, double endY, double endZ,
                                 int durationTicks,
                                 ServerLevel level) {
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
                                 int durationTicks,
                                 ServerPlayer player) {
        PacketHandler.sendToPlayer(player, 
            new AddDirectionalEffectPacket(effect.getIndex(),
                startX, startY, startZ,
                endX, endY, endZ,
                durationTicks)
        );
    }

    public enum DirectionalEffect {
        FATE_SIPHONING(0);

        private final int index;

        DirectionalEffect(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}