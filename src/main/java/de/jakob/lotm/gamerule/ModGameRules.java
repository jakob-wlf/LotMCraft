package de.jakob.lotm.gamerule;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncGriefingGamerulePacket;
import net.minecraft.world.level.GameRules;

public class ModGameRules {
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_GRIEFING;
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_BEYONDER_SPAWNING;
    public static GameRules.Key<GameRules.BooleanValue> REDUCE_REGEN_IN_BEYONDER_FIGHT;

    public static void register() {
        ALLOW_GRIEFING = GameRules.register(
            "allowAbilityGriefing",
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

        REDUCE_REGEN_IN_BEYONDER_FIGHT = GameRules.register(
                "reduceRegenInBeyonderFight",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(true)
        );
    }

}