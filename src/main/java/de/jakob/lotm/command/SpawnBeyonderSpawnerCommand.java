package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderSpawnerEntity;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class SpawnBeyonderSpawnerCommand {

    private static final SuggestionProvider<CommandSourceStack> PATHWAY_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    // "random" + all implemented pathways
                    java.util.stream.Stream.concat(
                            java.util.stream.Stream.of("random"),
                            BeyonderData.implementedPathways.stream()
                    ).toList(),
                    builder
            );

    /**
     * Command tree:
     *
     * /place_beyonder_spawner
     *   <triggerRadius>              – double, how close a player must be
     *   <minSequence>                – int 0-9, player must be at this seq or lower to trigger
     *   <pathway>                    – string (or "random")
     *   <sequenceMin>                – int 0-9, minimum sequence of the spawned Beyonder
     *   [sequenceMax]                – int 0-9 (optional, >= sequenceMin → random range)
     *   [hasQuest]                   – bool (optional, default false)
     *   [hasTrades]                  – bool (optional, default false)
     *   [spawnAnimation]             – bool (optional, default true)
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("place_beyonder_spawner")
            .requires(source -> source.hasPermission(2))

            .then(Commands.argument("triggerRadius", DoubleArgumentType.doubleArg(1.0, 128.0))
            .then(Commands.argument("minSequence", IntegerArgumentType.integer(0, 10))
            .then(Commands.argument("pathway", StringArgumentType.string())
                .suggests(PATHWAY_SUGGESTIONS)

            .then(Commands.argument("sequenceMin", IntegerArgumentType.integer(0, 9))
                .executes(ctx -> execute(ctx.getSource(),
                        DoubleArgumentType.getDouble(ctx, "triggerRadius"),
                        IntegerArgumentType.getInteger(ctx, "minSequence"),
                        StringArgumentType.getString(ctx, "pathway"),
                        IntegerArgumentType.getInteger(ctx, "sequenceMin"),
                        -1,
                        false, false, false))

                .then(Commands.argument("hasQuest", BoolArgumentType.bool())
                    .executes(ctx -> execute(ctx.getSource(),
                            DoubleArgumentType.getDouble(ctx, "triggerRadius"),
                            IntegerArgumentType.getInteger(ctx, "minSequence"),
                            StringArgumentType.getString(ctx, "pathway"),
                            IntegerArgumentType.getInteger(ctx, "sequenceMin"),
                            -1,
                            BoolArgumentType.getBool(ctx, "hasQuest"),
                            false, false))

                    .then(Commands.argument("hasTrades", BoolArgumentType.bool())
                        .executes(ctx -> execute(ctx.getSource(),
                                DoubleArgumentType.getDouble(ctx, "triggerRadius"),
                                IntegerArgumentType.getInteger(ctx, "minSequence"),
                                StringArgumentType.getString(ctx, "pathway"),
                                IntegerArgumentType.getInteger(ctx, "sequenceMin"),
                                -1,
                                BoolArgumentType.getBool(ctx, "hasQuest"),
                                BoolArgumentType.getBool(ctx, "hasTrades"),
                                false))

                        .then(Commands.argument("spawnAnimation", BoolArgumentType.bool())
                            .executes(ctx -> execute(ctx.getSource(),
                                    DoubleArgumentType.getDouble(ctx, "triggerRadius"),
                                    IntegerArgumentType.getInteger(ctx, "minSequence"),
                                    StringArgumentType.getString(ctx, "pathway"),
                                    IntegerArgumentType.getInteger(ctx, "sequenceMin"),
                                    -1,
                                    BoolArgumentType.getBool(ctx, "hasQuest"),
                                    BoolArgumentType.getBool(ctx, "hasTrades"),
                                    BoolArgumentType.getBool(ctx, "spawnAnimation")))
                        )
                    )
                )

            .then(Commands.argument("sequenceMax", IntegerArgumentType.integer(0, 9))
                .executes(ctx -> execute(ctx.getSource(),
                        DoubleArgumentType.getDouble(ctx, "triggerRadius"),
                        IntegerArgumentType.getInteger(ctx, "minSequence"),
                        StringArgumentType.getString(ctx, "pathway"),
                        IntegerArgumentType.getInteger(ctx, "sequenceMin"),
                        IntegerArgumentType.getInteger(ctx, "sequenceMax"),
                        false, false, false))

                .then(Commands.argument("hasQuest", BoolArgumentType.bool())
                    .executes(ctx -> execute(ctx.getSource(),
                            DoubleArgumentType.getDouble(ctx, "triggerRadius"),
                            IntegerArgumentType.getInteger(ctx, "minSequence"),
                            StringArgumentType.getString(ctx, "pathway"),
                            IntegerArgumentType.getInteger(ctx, "sequenceMin"),
                            IntegerArgumentType.getInteger(ctx, "sequenceMax"),
                            BoolArgumentType.getBool(ctx, "hasQuest"),
                            false, false))

                    .then(Commands.argument("hasTrades", BoolArgumentType.bool())
                        .executes(ctx -> execute(ctx.getSource(),
                                DoubleArgumentType.getDouble(ctx, "triggerRadius"),
                                IntegerArgumentType.getInteger(ctx, "minSequence"),
                                StringArgumentType.getString(ctx, "pathway"),
                                IntegerArgumentType.getInteger(ctx, "sequenceMin"),
                                IntegerArgumentType.getInteger(ctx, "sequenceMax"),
                                BoolArgumentType.getBool(ctx, "hasQuest"),
                                BoolArgumentType.getBool(ctx, "hasTrades"),
                                false))

                        .then(Commands.argument("spawnAnimation", BoolArgumentType.bool())
                            .executes(ctx -> execute(ctx.getSource(),
                                    DoubleArgumentType.getDouble(ctx, "triggerRadius"),
                                    IntegerArgumentType.getInteger(ctx, "minSequence"),
                                    StringArgumentType.getString(ctx, "pathway"),
                                    IntegerArgumentType.getInteger(ctx, "sequenceMin"),
                                    IntegerArgumentType.getInteger(ctx, "sequenceMax"),
                                    BoolArgumentType.getBool(ctx, "hasQuest"),
                                    BoolArgumentType.getBool(ctx, "hasTrades"),
                                    BoolArgumentType.getBool(ctx, "spawnAnimation")))
                        )
                    )
                )
            )
        )))));
    }

    private static int execute(
            CommandSourceStack source,
            double triggerRadius,
            int minSequence,
            String pathway,
            int sequenceMin,
            int sequenceMax,        // -1 means fixed (no range)
            boolean hasQuest,
            boolean hasTrades,
            boolean spawnAnimation
    ) {
        try {
            if (hasQuest && hasTrades) {
                source.sendFailure(Component.literal("Cannot have both hasQuest and hasTrades set to true!"));
                return 0;
            }

            if (sequenceMax >= 0 && sequenceMax < sequenceMin) {
                source.sendFailure(Component.literal(
                        "sequenceMax (" + sequenceMax + ") must be >= sequenceMin (" + sequenceMin + ")!"));
                return 0;
            }

            String resolvedPathway = pathway;
            if (pathway.equalsIgnoreCase("random")) {
                resolvedPathway = ""; // entity handles random selection
            } else if (!BeyonderData.pathways.contains(pathway)) {
                source.sendFailure(Component.literal("Unknown pathway: " + pathway
                        + ". Use \"random\" for a random pathway."));
                return 0;
            }

            ServerLevel serverLevel = source.getLevel();

            // Determine spawn position: the executor's position (or position of the source)
            double x = source.getPosition().x;
            double y = source.getPosition().y;
            double z = source.getPosition().z;

            // Build and place the spawner entity
            BeyonderSpawnerEntity spawner = new BeyonderSpawnerEntity(
                    ModEntities.BEYONDER_SPAWNER.get(), serverLevel);
            spawner.setTriggerRadius(triggerRadius);
            spawner.setMinSequence(minSequence);
            spawner.setPathway(resolvedPathway);
            spawner.setSequenceMin(sequenceMin);
            spawner.setSequenceMax(sequenceMax);
            spawner.setHasQuest(hasQuest);
            spawner.setHasTrades(hasTrades);
            spawner.setSpawnAnimation(spawnAnimation);
            spawner.setPos(x, y, z);

            serverLevel.addFreshEntity(spawner);

            // Build a nice feedback message
            String seqDisplay = sequenceMax >= 0
                    ? "sequence " + sequenceMin + "–" + sequenceMax
                    : "sequence " + sequenceMin;
            String pathwayDisplay = resolvedPathway.isBlank() ? "random" : resolvedPathway;

            source.sendSuccess(() -> Component.literal(
                    "Placed BeyonderSpawner at (" + (int)x + ", " + (int)y + ", " + (int)z + "):\n"
                    + "  Radius: " + triggerRadius + " | Required player seq ≤ " + minSequence + "\n"
                    + "  Pathway: " + pathwayDisplay + " | " + seqDisplay + "\n"
                    + "  Quest=" + hasQuest + " Trades=" + hasTrades
                    + " Animation=" + spawnAnimation
            ), false);

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to place BeyonderSpawner: " + e.getMessage()));
            return 0;
        }
    }
}