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
    public static GameRules.Key<GameRules.BooleanValue> DISABLE_FLIGHT_IN_COMBAT;
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_ARTIFACTS;
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_ARTIFACTS_WITH_NO_NEGATIVES;
    public static GameRules.Key<GameRules.IntegerValue> CHARSTACK_REQUIRED_FOR_APOTHEOSIS;
    public static GameRules.Key<GameRules.IntegerValue> UNIQUENESS_SPAWN_LIKELIHOOD;
    public static GameRules.Key<GameRules.BooleanValue> SEQUENCE_DIMENSION_LOCK;

    public static GameRules.Key<GameRules.IntegerValue> MAX_ALLY_COUNT;

    public static GameRules.Key<GameRules.IntegerValue> SEQ_0_AMOUNT;
    public static GameRules.Key<GameRules.IntegerValue> SEQ_1_AMOUNT;
    public static GameRules.Key<GameRules.IntegerValue> SEQ_2_AMOUNT;
    public static GameRules.Key<GameRules.IntegerValue> SEQ_3_AMOUNT;
    public static GameRules.Key<GameRules.IntegerValue> SEQ_4_AMOUNT;
    public static GameRules.Key<GameRules.IntegerValue> SEQ_5_AMOUNT;
    public static GameRules.Key<GameRules.IntegerValue> SEQ_6_AMOUNT;
    public static GameRules.Key<GameRules.IntegerValue> SEQ_7_AMOUNT;
    public static GameRules.Key<GameRules.IntegerValue> SEQ_8_AMOUNT;

    public static void register() {
        ALLOW_GRIEFING = GameRules.register(
            "allowAbilityGriefing",
            GameRules.Category.MISC,
            GameRules.BooleanValue.create(true)
        );

        DISABLE_FLIGHT_IN_COMBAT = GameRules.register(
                "disableFlightInCombat",
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

        SEQUENCE_DIMENSION_LOCK = GameRules.register(
                "sequenceDimensionLock",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(false)
        );

        CHARSTACK_REQUIRED_FOR_APOTHEOSIS = GameRules.register(
                "charStackRequiredForApotheosis",
                GameRules.Category.MISC,
                GameRules.IntegerValue.create(2)
        );

        UNIQUENESS_SPAWN_LIKELIHOOD = GameRules.register(
                "uniquenessSpawnLikelihood",
                GameRules.Category.MISC,
                GameRules.IntegerValue.create(1,
                        (server, value) -> {
                            int v = value.get();

                            if (v <= 0) {
                                value.set(1, server);
                            }
                            else if (v > 100) {
                                value.set(99, server);
                            }
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

        ALLOW_ARTIFACTS = GameRules.register(
                "allowArtifacts",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(true)
        );

        ALLOW_ARTIFACTS_WITH_NO_NEGATIVES = GameRules.register(
                "allowArtifactsWithNoNegatives",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(false)
        );

        MAX_ALLY_COUNT = GameRules.register(
                "maxAllyCount",
                GameRules.Category.MISC,
                GameRules.IntegerValue.create(100,
                        (server, value) -> {
                            if (value.get() < 0) {
                                value.set(0, server);
                            }
                        })
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

        SEQ_3_AMOUNT = GameRules.register(
                "amountOfSeq3",
                GameRules.Category.MISC,
                GameRules.IntegerValue.create(40,
                        (server, value) -> {
                            int v = value.get();

                            if (v < 0) {
                                value.set(0, server);
                            }
                            else if (v > 80) {
                                value.set(80, server);
                            }
                        })
        );

        SEQ_4_AMOUNT = GameRules.register(
                "amountOfSeq4",
                GameRules.Category.MISC,
                GameRules.IntegerValue.create(80,
                        (server, value) -> {
                            int v = value.get();

                            if (v < 0) {
                                value.set(0, server);
                            }
                            else if (v > 120) {
                                value.set(120, server);
                            }
                        })
        );

        SEQ_5_AMOUNT = GameRules.register(
                "amountOfSeq5",
                GameRules.Category.MISC,
                GameRules.IntegerValue.create(120,
                        (server, value) -> {
                            int v = value.get();

                            if (v < 0) {
                                value.set(0, server);
                            }
                            else if (v > 180) {
                                value.set(180, server);
                            }
                        })
        );

        SEQ_6_AMOUNT = GameRules.register(
                "amountOfSeq6",
                GameRules.Category.MISC,
                GameRules.IntegerValue.create(180,
                        (server, value) -> {
                            int v = value.get();

                            if (v < 0) {
                                value.set(0, server);
                            }
                            else if (v > 240) {
                                value.set(240, server);
                            }
                        })
        );

        SEQ_7_AMOUNT = GameRules.register(
                "amountOfSeq7",
                GameRules.Category.MISC,
                GameRules.IntegerValue.create(240,
                        (server, value) -> {
                            int v = value.get();

                            if (v < 0) {
                                value.set(0, server);
                            }
                            else if (v > 300) {
                                value.set(300, server);
                            }
                        })
        );

        SEQ_8_AMOUNT = GameRules.register(
                "amountOfSeq8",
                GameRules.Category.MISC,
                GameRules.IntegerValue.create(300,
                        (server, value) -> {
                            int v = value.get();

                            if (v < 0) {
                                value.set(0, server);
                            }
                            else if (v > 400) {
                                value.set(400, server);
                            }
                        })
        );

    }

}