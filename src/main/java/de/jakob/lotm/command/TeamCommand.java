package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import de.jakob.lotm.util.helper.CohortUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeamCommand {

    // Pending invites: leaderUUID -> (targetUUID -> expiry time ms)
    private static final Map<UUID, Map<UUID, Long>> pendingInvites = new ConcurrentHashMap<>();
    private static final long INVITE_TIMEOUT = 30_000L;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("rteam")
                .requires(source -> source.getPlayer() != null)
                // /team add <player> — leader invites a player
                .then(Commands.literal("add")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer leader = context.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                    return CohortUtil.addMember(leader, target);
                                })
                        )
                )
                // /team remove <player> — leader removes a member, or member removes themselves
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer sender = context.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                    return CohortUtil.removeMember(sender, target);
                                })
                        )
                )
                // /team leave — member leaves their team
                .then(Commands.literal("leave")
                        .executes(context -> {
                            ServerPlayer sender = context.getSource().getPlayerOrException();
                            return CohortUtil.leave(sender);
                        })
                )
                // /team disband — leader disbands the whole team
                .then(Commands.literal("disband")
                        .executes(context -> {
                            ServerPlayer sender = context.getSource().getPlayerOrException();
                            return CohortUtil.disband(sender);
                        })
                )
                // /rteam admin — OP-only subcommands for managing team state
                .then(Commands.literal("admin")
                        .requires(source -> source.hasPermission(2))
                        // /rteam admin reset <player> — clears all team data for a player
                        .then(Commands.literal("reset")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            return CohortUtil.adminReset(target, context.getSource().getPlayer());
                                        })
                                )
                        )
                        // /rteam admin info <player> — shows team status of a player
                        .then(Commands.literal("info")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            return CohortUtil.adminInfo(target, context.getSource().getPlayer());
                                        })
                                )
                        )
                )
        );
    }

}
