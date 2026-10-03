package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Hidden commands triggered by clicking the accept/decline buttons in the team invite chat message.
 */
public class TeamInviteResponseCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("lotm_accept_team")
                .then(Commands.argument("uuid", StringArgumentType.string())
                        .executes(context -> {
                            try {
                                UUID leaderUUID = UUID.fromString(StringArgumentType.getString(context, "uuid"));
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                TeamCommand.acceptInvite(player, leaderUUID);
                                return 1;
                            } catch (IllegalArgumentException e) {
                                return 0;
                            }
                        })
                )
        );

        dispatcher.register(Commands.literal("lotm_decline_team")
                .then(Commands.argument("uuid", StringArgumentType.string())
                        .executes(context -> {
                            try {
                                UUID leaderUUID = UUID.fromString(StringArgumentType.getString(context, "uuid"));
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                TeamCommand.declineInvite(player, leaderUUID);
                                return 1;
                            } catch (IllegalArgumentException e) {
                                return 0;
                            }
                        })
                )
        );
    }
}
