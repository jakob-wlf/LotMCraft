package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.jakob.lotm.sefirah.GreatOldOneManager;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.stream.Collectors;

public class GreatOldOneCommand {

    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    context.getSource().getServer().getPlayerList().getPlayers()
                            .stream().map(p -> p.getGameProfile().getName()).collect(Collectors.toList()),
                    builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("greatoldone")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("lord-of-mysteries")
                        // /greatoldone lord-of-mysteries  — applies to self
                        .executes(context -> transform(context.getSource(), "lord-of-mysteries", null))
                        // /greatoldone lord-of-mysteries <player>
                        .then(Commands.argument("player", StringArgumentType.string())
                                .suggests(PLAYER_SUGGESTIONS)
                                .executes(context -> transform(
                                        context.getSource(),
                                        "lord-of-mysteries",
                                        StringArgumentType.getString(context, "player")))))
                .then(Commands.literal("eternal-darkness")
                        // /greatoldone eternal-darkness  — applies to self
                        .executes(context -> transform(context.getSource(), "eternal-darkness", null))
                        // /greatoldone eternal-darkness <player>
                        .then(Commands.argument("player", StringArgumentType.string())
                                .suggests(PLAYER_SUGGESTIONS)
                                .executes(context -> transform(
                                        context.getSource(),
                                        "eternal-darkness",
                                        StringArgumentType.getString(context, "player")))))
        );
    }

    private static int transform(CommandSourceStack source, String gooType, String targetName) {
        ServerPlayer player;
        if (targetName != null) {
            player = source.getServer().getPlayerList().getPlayerByName(targetName);
            if (player == null) {
                source.sendFailure(Component.literal("Player \"" + targetName + "\" not found or not online."));
                return 0;
            }
        } else {
            if (!(source.getEntity() instanceof ServerPlayer self)) {
                source.sendFailure(Component.literal("Specify a player name when running from console."));
                return 0;
            }
            player = self;
        }

        if (GreatOldOneManager.isGreatOldOne(player)) {
            source.sendFailure(Component.literal(player.getGameProfile().getName() + " is already a Great Old One."));
            return 0;
        }

        if (!BeyonderData.isBeyonder(player)) {
            source.sendFailure(Component.literal(player.getGameProfile().getName() + " must be a beyonder."));
            return 0;
        }

        GreatOldOneManager.transformAs(player, gooType);
        String label = gooType.equalsIgnoreCase("eternal-darkness") ? "Eternal Darkness" : "Lord of Mysteries";
        source.sendSuccess(() -> Component.literal(
                "§d" + player.getGameProfile().getName() + " has been transformed into " + label + "."), true);
        return 1;
    }
}

