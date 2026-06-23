package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ControllingDataComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TeamComponent;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.PendingTeamInviteS2CPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.TeamInviteManager;
import de.jakob.lotm.util.helper.TeamUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
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
                                    return executeAdd(context.getSource(), leader, target);
                                })
                        )
                )
                // /team remove <player> — leader removes a member, or member removes themselves
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer sender = context.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                    return executeRemove(context.getSource(), sender, target);
                                })
                        )
                )
                // /team leave — member leaves their team
                .then(Commands.literal("leave")
                        .executes(context -> {
                            ServerPlayer sender = context.getSource().getPlayerOrException();
                            return executeLeave(context.getSource(), sender);
                        })
                )
                // /team disband — leader disbands the whole team
                .then(Commands.literal("disband")
                        .executes(context -> {
                            ServerPlayer sender = context.getSource().getPlayerOrException();
                            return executeDisband(context.getSource(), sender);
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
                                            return executeAdminReset(context.getSource(), target);
                                        })
                                )
                        )
                        // /rteam admin info <player> — shows team status of a player
                        .then(Commands.literal("info")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            return executeAdminInfo(context.getSource(), target);
                                        })
                                )
                        )
                )
        );
    }

    private static boolean checkEligible(CommandSourceStack source, ServerPlayer player) {
        // If the player is currently controlling a marionette, check their original body's data
        ControllingDataComponent controlling = player.getData(ModAttachments.CONTROLLING_DATA);
        if (controlling.isControlling()) {
            net.minecraft.nbt.CompoundTag bodyTag = controlling.getBodyEntity();
            String pathway = bodyTag != null ? bodyTag.getCompound("NeoForgeData").getString("beyonder_pathway") : "";
            int sequence = bodyTag != null ? bodyTag.getCompound("NeoForgeData").getInt("beyonder_sequence") : LOTMCraft.NON_BEYONDER_SEQ;
            if (!pathway.equals("red_priest") || sequence > 3) {
                source.sendFailure(Component.literal("Only Red Priest Beyonders at sequence 3 or higher can use this command."));
                return false;
            }
            return true;
        }
        if (!TeamUtils.isEligibleLeader(player)) {
            source.sendFailure(Component.literal("Only Red Priest Beyonders at sequence 3 or higher can use this command."));
            return false;
        }
        return true;
    }

    private static int executeAdd(CommandSourceStack source, ServerPlayer leader, ServerPlayer target) {
        if (!checkEligible(source, leader)) return 0;
        if (leader.equals(target)) {
            source.sendFailure(Component.literal("You cannot invite yourself."));
            return 0;
        }

        TeamComponent leaderTeam = leader.getData(ModAttachments.TEAM_COMPONENT.get());
        int maxSize = TeamUtils.getMaxTeamSize(BeyonderData.getSequence(leader));

        if (leaderTeam.memberCount() >= maxSize) {
            source.sendFailure(Component.literal("Your team is full (" + maxSize + " members max at your sequence)."));
            return 0;
        }
        TeamComponent targetTeam = target.getData(ModAttachments.TEAM_COMPONENT.get());

        if (leaderTeam.hasMember(target.getStringUUID())) {
            if (!targetTeam.isInTeam()) {
                // Stale entry — member left while leader was offline, clean it up
                leader.setData(ModAttachments.TEAM_COMPONENT.get(), leaderTeam.removeMember(target.getStringUUID()));
                leaderTeam = leader.getData(ModAttachments.TEAM_COMPONENT.get());
            } else {
                source.sendFailure(Component.literal(target.getName().getString() + " is already in your team."));
                return 0;
            }
        }

        if (targetTeam.isInTeam()) {
            // Check if the membership is stale (leader no longer claims this player)
            ServerPlayer claimedLeader = source.getServer().getPlayerList()
                    .getPlayer(UUID.fromString(targetTeam.leaderUUID()));
            boolean stale = claimedLeader == null ||
                    !claimedLeader.getData(ModAttachments.TEAM_COMPONENT.get()).hasMember(target.getStringUUID());
            if (stale) {
                target.setData(ModAttachments.TEAM_COMPONENT.get(), targetTeam.clearLeader());
            } else {
                source.sendFailure(Component.literal(target.getName().getString() + " is already in another team."));
                return 0;
            }
        }

        // Check if target already sent an invite to the leader (auto-accept)
        if (hasPendingInvite(target.getUUID(), leader.getUUID())) {
            removePendingInvite(target.getUUID(), leader.getUUID());
            TeamUtils.addMember(leader, target);
            source.sendSuccess(() -> Component.literal("You and " + target.getName().getString() + " are now in the same team.").withStyle(s -> s.withColor(0x4CAF50)), false);
            target.sendSystemMessage(Component.literal("You joined " + leader.getName().getString() + "'s team.").withStyle(s -> s.withColor(0x4CAF50)));
            return 1;
        }

        addPendingInvite(leader.getUUID(), target.getUUID());
        PacketHandler.sendToPlayer(target, new PendingTeamInviteS2CPacket(leader.getUUID(), leader.getName().getString()));
        source.sendSuccess(() -> Component.literal("Team invite sent to " + target.getName().getString() + ".").withStyle(s -> s.withColor(0x2196F3)), false);
        return 1;
    }

    private static int executeRemove(CommandSourceStack source, ServerPlayer sender, ServerPlayer target) {
        if (!checkEligible(source, sender)) return 0;
        TeamComponent senderTeam = sender.getData(ModAttachments.TEAM_COMPONENT.get());

        // Allow the leader to remove a member
        if (senderTeam.hasMember(target.getStringUUID())) {
            TeamUtils.removeMember(sender, target);
            source.sendSuccess(() -> Component.literal("Removed " + target.getName().getString() + " from your team.").withStyle(s -> s.withColor(0xFF9800)), false);
            target.sendSystemMessage(Component.literal("You were removed from " + sender.getName().getString() + "'s team.").withStyle(s -> s.withColor(0xFF9800)));
            return 1;
        }

        source.sendFailure(Component.literal(target.getName().getString() + " is not in your team."));
        return 0;
    }

    private static int executeLeave(CommandSourceStack source, ServerPlayer sender) {
        TeamComponent senderTeam = sender.getData(ModAttachments.TEAM_COMPONENT.get());
        if (!senderTeam.isInTeam()) {
            source.sendFailure(Component.literal("You are not in a team."));
            return 0;
        }

        String leaderUUID = senderTeam.leaderUUID();
        ServerPlayer leader = source.getServer().getPlayerList().getPlayer(UUID.fromString(leaderUUID));

        if (leader != null) {
            TeamUtils.removeMember(leader, sender);
            leader.sendSystemMessage(Component.literal(sender.getName().getString() + " left your team.").withStyle(s -> s.withColor(0xFF9800)));
        } else {
            // Leader is offline — clean up manually and clear client data
            sender.setData(ModAttachments.TEAM_COMPONENT.get(), senderTeam.clearLeader());
            PacketHandler.sendToPlayer(sender, new de.jakob.lotm.network.packets.toClient.SyncSharedAbilitiesDataS2CPacket(
                    "", new java.util.ArrayList<>(), new java.util.ArrayList<>(), new java.util.HashMap<>(), 0, 0));
        }

        source.sendSuccess(() -> Component.literal("You left the team.").withStyle(s -> s.withColor(0xFF9800)), false);
        return 1;
    }

    private static int executeDisband(CommandSourceStack source, ServerPlayer sender) {
        if (!checkEligible(source, sender)) return 0;
        TeamComponent senderTeam = sender.getData(ModAttachments.TEAM_COMPONENT.get());
        if (senderTeam.memberCount() == 0 && !senderTeam.isInTeam()) {
            source.sendFailure(Component.literal("You do not have a team to disband."));
            return 0;
        }
        if (senderTeam.isInTeam()) {
            source.sendFailure(Component.literal("Only the team leader can disband the team. Use /team leave to leave."));
            return 0;
        }

        TeamUtils.disbandTeam(sender, source.getServer());
        source.sendSuccess(() -> Component.literal("Your team has been disbanded.").withStyle(s -> s.withColor(0xF44336)), false);
        return 1;
    }

    // ===== Admin commands =====

    private static int executeAdminReset(CommandSourceStack source, ServerPlayer target) {
        TeamComponent team = target.getData(ModAttachments.TEAM_COMPONENT.get());

        // If target is a leader, disband their whole team first
        if (team.memberCount() > 0) {
            TeamUtils.disbandTeam(target, source.getServer());
        }

        // If target is a member, remove them from their leader's team
        if (team.isInTeam()) {
            ServerPlayer leader = source.getServer().getPlayerList()
                    .getPlayer(UUID.fromString(team.leaderUUID()));
            if (leader != null) {
                TeamUtils.removeMember(leader, target);
            } else {
                // Leader offline — clear manually
                target.setData(ModAttachments.TEAM_COMPONENT.get(), new TeamComponent());
                PacketHandler.sendToPlayer(target, new de.jakob.lotm.network.packets.toClient.SyncSharedAbilitiesDataS2CPacket(
                        "", new java.util.ArrayList<>(), new java.util.ArrayList<>(), new java.util.HashMap<>(), 0, 0));
            }
        }

        // Force-clear regardless, in case data was stale
        target.setData(ModAttachments.TEAM_COMPONENT.get(), new TeamComponent());
        PacketHandler.sendToPlayer(target, new de.jakob.lotm.network.packets.toClient.SyncSharedAbilitiesDataS2CPacket(
                "", new java.util.ArrayList<>(), new java.util.ArrayList<>(), new java.util.HashMap<>(), 0, 0));

        source.sendSuccess(() -> Component.literal("Reset team data for " + target.getName().getString() + ".").withStyle(s -> s.withColor(0x4CAF50)), true);
        target.sendSystemMessage(Component.literal("An admin has reset your team status.").withStyle(s -> s.withColor(0xFF9800)));
        return 1;
    }

    private static int executeAdminInfo(CommandSourceStack source, ServerPlayer target) {
        TeamComponent team = target.getData(ModAttachments.TEAM_COMPONENT.get());
        String name = target.getName().getString();

        if (!team.isInTeam() && team.memberCount() == 0) {
            source.sendSuccess(() -> Component.literal(name + " is not in any team."), false);
            return 1;
        }

        if (team.isInTeam()) {
            String leaderUUID = team.leaderUUID();
            ServerPlayer leader = source.getServer().getPlayerList().getPlayer(UUID.fromString(leaderUUID));
            String leaderName = leader != null ? leader.getName().getString() : "offline (" + leaderUUID + ")";
            source.sendSuccess(() -> Component.literal(name + " is a member of " + leaderName + "'s team."), false);
        } else {
            StringBuilder sb = new StringBuilder(name + " is a leader with " + team.memberCount() + " member(s): ");
            for (String uuid : team.memberUUIDs()) {
                ServerPlayer member = source.getServer().getPlayerList().getPlayer(UUID.fromString(uuid));
                sb.append(member != null ? member.getName().getString() : "offline(" + uuid + ")").append(", ");
            }
            String msg = sb.toString().replaceAll(", $", "");
            source.sendSuccess(() -> Component.literal(msg), false);
        }
        return 1;
    }

    // ===== Invite management =====

    public static void acceptInvite(ServerPlayer accepter, UUID leaderUUID) {
        if (!hasPendingInvite(leaderUUID, accepter.getUUID())) {
            accepter.sendSystemMessage(Component.literal("No pending invite from that player.").withStyle(s -> s.withColor(0xF44336)));
            return;
        }
        removePendingInvite(leaderUUID, accepter.getUUID());

        ServerPlayer leader = accepter.getServer().getPlayerList().getPlayer(leaderUUID);
        if (leader == null) {
            accepter.sendSystemMessage(Component.literal("The leader is no longer online.").withStyle(s -> s.withColor(0xF44336)));
            return;
        }

        boolean added = TeamUtils.addMember(leader, accepter);
        if (!added) {
            accepter.sendSystemMessage(Component.literal("Could not join the team (it may be full or you are already in one).").withStyle(s -> s.withColor(0xF44336)));
            return;
        }

        accepter.sendSystemMessage(Component.literal("You joined " + leader.getName().getString() + "'s team.").withStyle(s -> s.withColor(0x4CAF50)));
        leader.sendSystemMessage(Component.literal(accepter.getName().getString() + " joined your team.").withStyle(s -> s.withColor(0x4CAF50)));
    }

    public static void declineInvite(ServerPlayer decliner, UUID leaderUUID) {
        removePendingInvite(leaderUUID, decliner.getUUID());
        decliner.sendSystemMessage(Component.literal("Team invite declined.").withStyle(s -> s.withColor(0xFF9800)));

        ServerPlayer leader = decliner.getServer().getPlayerList().getPlayer(leaderUUID);
        if (leader != null) {
            leader.sendSystemMessage(Component.literal(decliner.getName().getString() + " declined your team invite.").withStyle(s -> s.withColor(0xFF9800)));
        }
    }

    private static void addPendingInvite(UUID leader, UUID target) {
        pendingInvites.computeIfAbsent(leader, k -> new ConcurrentHashMap<>())
                .put(target, System.currentTimeMillis() + INVITE_TIMEOUT);
    }

    private static void removePendingInvite(UUID leader, UUID target) {
        Map<UUID, Long> map = pendingInvites.get(leader);
        if (map != null) {
            map.remove(target);
            if (map.isEmpty()) pendingInvites.remove(leader);
        }
    }

    private static boolean hasPendingInvite(UUID leader, UUID target) {
        cleanupExpired();
        Map<UUID, Long> map = pendingInvites.get(leader);
        if (map == null) return false;
        Long expiry = map.get(target);
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            map.remove(target);
            return false;
        }
        return true;
    }

    private static void cleanupExpired() {
        long now = System.currentTimeMillis();
        pendingInvites.forEach((leader, targets) -> targets.entrySet().removeIf(e -> e.getValue() < now));
        pendingInvites.entrySet().removeIf(e -> e.getValue().isEmpty());
    }
}
