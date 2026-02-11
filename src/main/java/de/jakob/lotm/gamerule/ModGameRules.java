package de.jakob.lotm.gamerule;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncGriefingGamerulePacket;
import net.minecraft.world.level.GameRules;

public class ModGameRules {
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_GRIEFING;
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_BEYONDER_SPAWNING;
    public static GameRules.Key<GameRules.IntegerValue> DIGESTION_RATE;
    public static GameRules.Key<GameRules.BooleanValue> REDUCE_REGEN_IN_BEYONDER_FIGHT;
    public static GameRules.Key<GameRules.BooleanValue> SPAWN_WITH_STARTING_CHARACTERISTIC;
    public static GameRules.Key<GameRules.BooleanValue> REGRESS_SEQUENCE_ON_DEATH;

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

        DIGESTION_RATE = GameRules.register(
                "digestion_rate",
                GameRules.Category.MISC,
                GameRules.IntegerValue.create(50)
        );

        REDUCE_REGEN_IN_BEYONDER_FIGHT = GameRules.register(
                "reduceRegenInBeyonderFight",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(true)
        );

        SPAWN_WITH_STARTING_CHARACTERISTIC = GameRules.register(
                "spawnWithCharacteristic",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(true)
        );

        REGRESS_SEQUENCE_ON_DEATH = GameRules.register(
                "regressSequenceOnDeath",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(true)
        );
    }

}