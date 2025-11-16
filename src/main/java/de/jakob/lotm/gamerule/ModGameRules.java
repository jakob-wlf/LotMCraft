package de.jakob.lotm.gamerule;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.SyncGriefingGamerulePacket;
import net.minecraft.world.level.GameRules;

public class ModGameRules {
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_GRIEFING;
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_BEYONDER_SPAWNING;

    public static void register() {
        ALLOW_GRIEFING = GameRules.register(
            "allowGriefing",
            GameRules.Category.MISC,
            GameRules.BooleanValue.create(true)
        );

        ALLOW_BEYONDER_SPAWNING = GameRules.register(
                "allowBeyonderSpawning",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(true, (server, value) -> {
                    PacketHandler.sendToAllPlayers(new SyncGriefingGamerulePacket(value.get()));
                })
        );
    }

}