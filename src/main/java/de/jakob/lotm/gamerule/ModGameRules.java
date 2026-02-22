package de.jakob.lotm.gamerule;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncGriefingGamerulePacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;

public class ModGameRules {
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_GRIEFING;
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_BEYONDER_SPAWNING;
    public static GameRules.Key<GameRules.IntegerValue> DIGESTION_RATE;
    public static GameRules.Key<GameRules.BooleanValue> REDUCE_REGEN_IN_BEYONDER_FIGHT;
    public static GameRules.Key<GameRules.BooleanValue> SPAWN_WITH_STARTING_CHARACTERISTIC;
    public static GameRules.Key<GameRules.BooleanValue> REGRESS_SEQUENCE_ON_DEATH;

    public static GameRules.Key<GameRules.IntegerValue> SEQ_0_AMOUNT;
    public static GameRules.Key<GameRules.IntegerValue> SEQ_1_AMOUNT;
    public static GameRules.Key<GameRules.IntegerValue> SEQ_2_AMOUNT;

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
                GameRules.IntegerValue.create(15)
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

        SEQ_0_AMOUNT = GameRules.register(
                "amountOfSeq0",
                GameRules.Category.MISC,
                GameRules.IntegerValue.create(1,
                        (server, value) -> {
                            int v = value.get();

                            if (v < 0) {
                                value.set(0, server);
                            }
                            else if (v > 5) {
                                value.set(5, server);
                            }
                        })
        );

        SEQ_1_AMOUNT = GameRules.register(
                "amountOfSeq1",
                GameRules.Category.MISC,
                GameRules.IntegerValue.create(3,
                        (server, value) -> {
                            int v = value.get();

                            if (v < 0) {
                                value.set(0, server);
                            }
                            else if (v > 10) {
                                value.set(10, server);
                            }
                        })
        );

        SEQ_2_AMOUNT = GameRules.register(
                "amountOfSeq2",
                GameRules.Category.MISC,
                GameRules.IntegerValue.create(9,
                        (server, value) -> {
                            int v = value.get();

                            if (v < 0) {
                                value.set(0, server);
                            }
                            else if (v > 20) {
                                value.set(20, server);
                            }
                        })
        );
    }

}