package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.jakob.lotm.beyonders.advancementrituals.AdvancementRitualManager;
import de.jakob.lotm.beyonders.advancementrituals.RitualRequirementData;
import de.jakob.lotm.beyonders.advancementrituals.ServerPopulationData;
import de.jakob.lotm.beyonders.advancementrituals.visionary.seq0.VisSeq0Ritual;
import de.jakob.lotm.beyonders.advancementrituals.visionary.seq1.VisSeq1Ritual;
import de.jakob.lotm.beyonders.advancementrituals.visionary.seq2.VisSeq2Ritual;
import de.jakob.lotm.beyonders.advancementrituals.visionary.seq3.VisSeq3Ritual;
import de.jakob.lotm.beyonders.advancementrituals.visionary.seq4.VisSeq4Ritual;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class RitualsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ritual")
                .then(Commands.argument("pathway", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                BeyonderData.pathways, builder))
                        .then(Commands.argument("sequence", IntegerArgumentType.integer(0, 9))
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        new String[]{"0", "1", "2", "3", "4", "5"}, builder))
                                .then(Commands.literal("check")
                                        .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            if (!(source.getEntity() instanceof ServerPlayer player)) {
                                                source.sendFailure(Component.literal("Only a player can check their own ritual progress."));
                                                return 0;
                                            }

                                            String pathway = StringArgumentType.getString(context, "pathway");
                                            int sequence = IntegerArgumentType.getInteger(context, "sequence");

                                            return checkRitual(source, player, pathway, sequence);
                                        })
                                )
                                .then(Commands.literal("edit")
                                        .requires(source -> source.hasPermission(2))
                                        .then(Commands.literal("RequiredAmount")
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(context -> editRequiredAmount(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "pathway"),
                                                                IntegerArgumentType.getInteger(context, "sequence"),
                                                                IntegerArgumentType.getInteger(context, "amount"))))))
                        )
                )
        );
    }

    private static int editRequiredAmount(CommandSourceStack source, String pathway, int sequence, int amount) {
        if (!BeyonderData.pathways.contains(pathway)) {
            source.sendFailure(Component.literal("Unknown pathway: " + pathway));
            return 0;
        }

        RitualRequirementData.get(source.getServer()).setRequiredAmount(pathway, sequence, amount);
        source.sendSuccess(() -> Component.literal("Set the " + pathway + " sequence " + sequence
                + " ritual required amount to " + amount + "."), true);
        return 1;
    }

    private static int checkRitual(CommandSourceStack source, ServerPlayer player, String pathway, int sequence) {
        source.sendSuccess(() -> Component.literal(
                "Ritual active: " + AdvancementRitualManager.isRitualActive(player)), false);
        source.sendSuccess(() -> Component.literal(
                "Completion pending: " + AdvancementRitualManager.hasValidCompletion(player, pathway, sequence)
                        + " (" + (AdvancementRitualManager.getRemainingCompletionTicks(player) / 20) + "s left)"), false);

        if (pathway.equals("visionary") && sequence == 1) {
            int required = VisSeq1Ritual.getRequiredCount(player);
            int progress = VisSeq1Ritual.getCurrentWeightedProgress(player);
            int highestPlayerCount = ServerPopulationData.get(player.server).getHighestPlayerCount();

            source.sendSuccess(() -> Component.literal(
                    "Story write targets: " + progress + " / " + required
                            + " (highest player count ever: " + highestPlayerCount + ")"), false);
        }

        if (pathway.equals("visionary") && sequence == 2) {
            int required = VisSeq2Ritual.getRequiredCount(player);
            int progress = VisSeq2Ritual.getCurrentWeightedProgress(player);
            source.sendSuccess(() -> Component.literal(
                    "Players manipulated: " + progress + " / " + required), false);
        }

        if (pathway.equals("visionary") && sequence == 3) {
            int required = VisSeq3Ritual.getRequiredCount(player);
            int progress = VisSeq3Ritual.getCurrentWeightedProgress(player);
            source.sendSuccess(() -> Component.literal(
                    "Sleepers tonight: " + progress + " / " + required), false);
        }

        if (pathway.equals("visionary") && sequence == 4) {
            int required = VisSeq4Ritual.getRequiredCount(player);
            int progress = VisSeq4Ritual.getCurrentWeightedProgress(player);
            source.sendSuccess(() -> Component.literal(
                    "Gathered nearby: " + progress + " / " + required), false);
        }

        if (pathway.equals("visionary") && sequence == 0) {
            int required = VisSeq0Ritual.getRequiredCount(player);
            int progress = VisSeq0Ritual.getCurrentWeightedProgress(player);
            source.sendSuccess(() -> Component.literal(
                    "Fulfilled predictions: " + progress + " / " + required), false);
        }

        return 1;
    }
}
