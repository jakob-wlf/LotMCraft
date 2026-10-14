package de.jakob.lotm.util.helper;

import com.mojang.authlib.GameProfile;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TeamComponent;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.PendingTeamInvitePacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.subordinates.SubordinateComponent;
import de.jakob.lotm.util.helper.subordinates.SubordinateUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

public class CohortUtil {

    // Pending invites: leaderUUID -> (targetUUID -> expiry time ms)
    public static final Map<UUID, Map<UUID, Long>> pendingInvites = new ConcurrentHashMap<>();
    public static final long INVITE_TIMEOUT = 30_000L;


    public static boolean checkEligible(ServerPlayer player) {
        if (!TeamUtils.isEligibleLeader(player)) {
            player.sendSystemMessage(Component.literal("Only Red Priest Beyonders at sequence 3 or higher can use this command.").withStyle(ChatFormatting.RED));

            return false;
        }
        return true;
    }

    public static int addMember(ServerPlayer leader, ServerPlayer target) {
        if (!checkEligible(leader)) return 0;
        if (leader.equals(target)) {
            leader.sendSystemMessage(Component.literal("You cannot invite yourself.").withStyle(ChatFormatting.RED));
            return 0;
        }

        TeamComponent leaderTeam = leader.getData(ModAttachments.TEAM_COMPONENT.get()).withLeader(leader.getStringUUID());
        int maxSize = TeamUtils.getMaxTeamSize(BeyonderData.getSequence(leader));

        if (leaderTeam.memberCount() >= maxSize) {
            leader.sendSystemMessage(Component.literal("Your team is full (" + maxSize + " members max at your sequence).").withStyle(ChatFormatting.RED));
            return 0;
        }
        TeamComponent targetTeam = target.getData(ModAttachments.TEAM_COMPONENT.get());

        if (leaderTeam.hasMember(target.getStringUUID())) {
            if (!targetTeam.isInTeam()) {
                // Stale entry — member left while leader was offline, clean it up
                leader.setData(ModAttachments.TEAM_COMPONENT.get(), leaderTeam.removeMember(target.getStringUUID()));
                leaderTeam = leader.getData(ModAttachments.TEAM_COMPONENT.get());
            } else {
                leader.sendSystemMessage(Component.literal(target.getName().getString() + " is already in your team.").withStyle(ChatFormatting.RED));
                return 0;
            }
        }

        if (targetTeam.isInTeam()) {
            // Check if the membership is stale (leader no longer claims this player)
            ServerPlayer claimedLeader = leader.getServer().getPlayerList()
                    .getPlayer(UUID.fromString(targetTeam.leaderUUID()));
            boolean stale = claimedLeader == null ||
                    !claimedLeader.getData(ModAttachments.TEAM_COMPONENT.get()).hasMember(target.getStringUUID());
            if (stale) {
                target.setData(ModAttachments.TEAM_COMPONENT.get(), targetTeam.clearAll());
            } else {
                leader.sendSystemMessage(Component.literal(target.getName().getString() + " is already in another team.").withStyle(ChatFormatting.RED));
                return 0;
            }
        }

        // Check if target already sent an invite to the leader (auto-accept)
        if (hasPendingInvite(target.getUUID(), leader.getUUID())) {
            removePendingInvite(target.getUUID(), leader.getUUID());
            TeamUtils.addMember(leader, target);
            leader.sendSystemMessage(Component.literal("You and " + target.getName().getString() + " are now in the same team.").withStyle(s -> s.withColor(0x4CAF50)));
            target.sendSystemMessage(Component.literal("You joined " + leader.getName().getString() + "'s team.").withStyle(s -> s.withColor(0x4CAF50)));
            return 1;
        }

        addPendingInvite(leader.getUUID(), target.getUUID());
        PacketHandler.sendToPlayer(target, new PendingTeamInvitePacket(leader.getUUID(), leader.getName().getString()));
        leader.sendSystemMessage(Component.literal("Team invite sent to " + target.getName().getString() + ".").withStyle(s -> s.withColor(0x2196F3)));
        return 1;
    }

    public static int removeMember(ServerPlayer sender, ServerPlayer target) {
        if (!checkEligible(sender)) return 0;
        TeamComponent senderTeam = sender.getData(ModAttachments.TEAM_COMPONENT.get());

        // Allow the leader to remove a member
        if (senderTeam.hasMember(target.getStringUUID())) {
            TeamUtils.removeMember(sender, target);
            sender.sendSystemMessage(Component.literal("Removed " + target.getName().getString() + " from your team.").withStyle(s -> s.withColor(0xFF9800)), false);
            target.sendSystemMessage(Component.literal("You were removed from " + sender.getName().getString() + "'s team.").withStyle(s -> s.withColor(0xFF9800)));
            return 1;
        }

        sender.sendSystemMessage(Component.literal(target.getName().getString() + " is not in your team.").withStyle(ChatFormatting.RED));
        return 0;
    }
    public static int removeMember(ServerPlayer sender, GameProfile target) {
        if (!checkEligible(sender)) return 0;
        TeamComponent senderTeam = sender.getData(ModAttachments.TEAM_COMPONENT.get());

        // Allow the leader to remove a member
        if (senderTeam.hasMember(String.valueOf(target.getId()))) {
//            TeamUtils.removeMember(sender, String.valueOf(target.getId()));
            sender.sendSystemMessage(Component.literal("Removed " + target.getName() + " from your team.").withStyle(s -> s.withColor(0xFF9800)), false);
            return 1;
        }

        sender.sendSystemMessage(Component.literal(target.getName() + " is not in your team.").withStyle(ChatFormatting.RED));
        return 0;
    }

    public static int leave(ServerPlayer sender) {
        TeamComponent senderTeam = sender.getData(ModAttachments.TEAM_COMPONENT.get());
        if (!senderTeam.isInTeam()) {
            sender.sendSystemMessage(Component.literal("You are not in a team.").withStyle(ChatFormatting.RED));
            return 0;
        }

        String leaderUUID = senderTeam.leaderUUID();
        ServerPlayer leader = sender.getServer().getPlayerList().getPlayer(UUID.fromString(leaderUUID));

        if (leader != null) {
            TeamUtils.removeMember(leader, sender);
            leader.sendSystemMessage(Component.literal(sender.getName().getString() + " left your team.").withStyle(s -> s.withColor(0xFF9800)));
        } else {
            // Leader is offline — clean up manually and clear client data
            sender.setData(ModAttachments.TEAM_COMPONENT.get(), senderTeam.clearAll());
            PacketHandler.sendToPlayer(sender, new de.jakob.lotm.network.packets.toClient.SyncSharedAbilitiesDataPacket(
                    "", new java.util.ArrayList<>(), new java.util.ArrayList<>(), new java.util.HashMap<>(), 0, 0));
        }

        sender.sendSystemMessage(Component.literal("You left the team.").withStyle(s -> s.withColor(0xFF9800)), false);
        return 1;
    }

    public static int disband(ServerPlayer sender) {
        if (!checkEligible(sender)) return 0;
        TeamComponent senderTeam = sender.getData(ModAttachments.TEAM_COMPONENT.get());
        if (senderTeam.memberCount() == 0 && !senderTeam.isInTeam()) {
            sender.sendSystemMessage(Component.literal("You do not have a team to disband.").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (senderTeam.isInTeam()) {
            sender.sendSystemMessage(Component.literal("Only the team leader can disband the team. Use /team leave to leave.").withStyle(ChatFormatting.RED));
            return 0;
        }

        TeamUtils.disbandTeam(sender, sender.getServer());
        sender.sendSystemMessage(Component.literal("Your team has been disbanded.").withStyle(s -> s.withColor(0xF44336)), false);
        return 1;
    }


    public static List<String[]> getALlPlayers(LivingEntity leader) {
        List<String[]> list = new ArrayList<>();
        TeamComponent senderTeam = leader.getData(ModAttachments.TEAM_COMPONENT.get());
        if (senderTeam.memberCount() > 0) {
            List<String> memberUUIDs = senderTeam.memberUUIDs();
            for (String uuid : memberUUIDs) {
                ServerPlayer member = leader.getServer().getPlayerList().getPlayer(UUID.fromString(uuid));
                if (member != null) {
                    list.add(new String[]{member.getStringUUID(), member.getName().getString(), BeyonderData.getPathway(member), String.valueOf(BeyonderData.getSequence(member))});
                }
                else {
                    GameProfile profile = getPlayerFromUUID(leader.getServer(), uuid);
                    if (profile != null) {
                        list.add(new String[]{profile.getId().toString(), profile.getName()});
                    }
                }

            }
        }
        return list;
    }


    public static List<String[]> getAllMembers(LivingEntity leader) {
        List<String[]> players = getAllMembersGrouped(leader).get("players");
        List<String[]> subordinates = getAllMembersGrouped(leader).get("npc");
        List<String[]> list = new ArrayList<>(players);
        list.addAll(subordinates);
        return list;
    }

    public static HashMap<String, List<String[]>> getAllMembersGrouped(LivingEntity leader) {
       HashMap<String, List<String[]>> list = new HashMap<>();

        List<String[]> players = getALlPlayers(leader);
        List<LivingEntity> subordinates = getAllSubordinates(leader);
        list.put("players", players);
        List<String[]> subordinatesList = new ArrayList<>();
        for (LivingEntity subordinate : subordinates) {
            if(BeyonderData.isBeyonder(subordinate)) subordinatesList.add(new String[]{subordinate.getStringUUID(), subordinate.getDisplayName().getString(), BeyonderData.getPathway(subordinate), String.valueOf(BeyonderData.getSequence(subordinate))});
            else subordinatesList.add(new String[]{subordinate.getStringUUID(), subordinate.getDisplayName().getString()});
        }
        list.put("npc", subordinatesList);
        return list;
    }

    // get player's name if they are offline
    public static GameProfile getPlayerFromUUID(MinecraftServer server, String uuid) {
        GameProfileCache profileCache = server.getProfileCache();

        if (profileCache != null) {
            Optional<GameProfile> profile = profileCache.get(UUID.fromString(uuid));
            if (profile.isPresent()) {
                return profile.get();
            }
        }

        // Return a fallback or null if the player has never joined this server before
        return null;
    }
    // ===== Admin commands =====

    public static int adminReset(ServerPlayer target, ServerPlayer source) {
        TeamComponent team = target.getData(ModAttachments.TEAM_COMPONENT.get());

        // If target is a leader, disband their whole team first
        if (team.memberCount() > 0) {
            TeamUtils.disbandTeam(target,target.getServer());
        }

        // If target is a member, remove them from their leader's team
        if (team.isInTeam()) {
            ServerPlayer leader = target.getServer().getPlayerList()
                    .getPlayer(UUID.fromString(team.leaderUUID()));
            if (leader != null) {
                TeamUtils.removeMember(leader, target);
            } else {
                // Leader offline — clear manually
                target.setData(ModAttachments.TEAM_COMPONENT.get(), new TeamComponent());
                PacketHandler.sendToPlayer(target, new de.jakob.lotm.network.packets.toClient.SyncSharedAbilitiesDataPacket(
                        "", new java.util.ArrayList<>(), new java.util.ArrayList<>(), new java.util.HashMap<>(), 0, 0));
            }
        }

        // Force-clear regardless, in case data was stale
        target.setData(ModAttachments.TEAM_COMPONENT.get(), new TeamComponent());
        PacketHandler.sendToPlayer(target, new de.jakob.lotm.network.packets.toClient.SyncSharedAbilitiesDataPacket(
                "", new java.util.ArrayList<>(), new java.util.ArrayList<>(), new java.util.HashMap<>(), 0, 0));

        source.sendSystemMessage(Component.literal("Reset team data for " + target.getName().getString() + ".").withStyle(s -> s.withColor(0x4CAF50)), true);
        target.sendSystemMessage(Component.literal("An admin has reset your team status.").withStyle(s -> s.withColor(0xFF9800)));
        return 1;
    }

    public static int adminInfo(ServerPlayer target, ServerPlayer source) {
        TeamComponent team = target.getData(ModAttachments.TEAM_COMPONENT.get());
        String name = target.getName().getString();

        if (!team.isInTeam() && team.memberCount() == 0) {
            source.sendSystemMessage(Component.literal(name + " is not in any team."), false);
            return 1;
        }

        if (team.isInTeam()) {
            String leaderUUID = team.leaderUUID();
            ServerPlayer leader = source.getServer().getPlayerList().getPlayer(UUID.fromString(leaderUUID));
            String leaderName = leader != null ? leader.getName().getString() : "offline (" + leaderUUID + ")";
            source.sendSystemMessage(Component.literal(name + " is a member of " + leaderName + "'s team."), false);
        } else {
            StringBuilder sb = new StringBuilder(name + " is a leader with " + team.memberCount() + " member(s): ");
            for (String uuid : team.memberUUIDs()) {
                ServerPlayer member = source.getServer().getPlayerList().getPlayer(UUID.fromString(uuid));
                sb.append(member != null ? member.getName().getString() : "offline(" + uuid + ")").append(", ");
            }
            String msg = sb.toString().replaceAll(", $", "");
            source.sendSystemMessage(Component.literal(msg), false);
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

    public static void addPendingInvite(UUID leader, UUID target) {
        pendingInvites.computeIfAbsent(leader, k -> new ConcurrentHashMap<>())
                .put(target, System.currentTimeMillis() + INVITE_TIMEOUT);
    }

    public static void removePendingInvite(UUID leader, UUID target) {
        Map<UUID, Long> map = pendingInvites.get(leader);
        if (map != null) {
            map.remove(target);
            if (map.isEmpty()) pendingInvites.remove(leader);
        }
    }

    public static boolean hasPendingInvite(UUID leader, UUID target) {
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

    public static String getLeaderUUID(Entity entity) {
        return entity.getData(ModAttachments.TEAM_COMPONENT.get()).leaderUUID();
    }

    public static void cleanupExpired() {
        long now = System.currentTimeMillis();
        pendingInvites.forEach((leader, targets) -> targets.entrySet().removeIf(e -> e.getValue() < now));
        pendingInvites.entrySet().removeIf(e -> e.getValue().isEmpty());
    }

    public static void setAlleviation(ServerPlayer player, boolean state) {
        TeamComponent team = player.getData(ModAttachments.TEAM_COMPONENT.get()).withLeader(player.getUUID().toString());
        player.setData(ModAttachments.TEAM_COMPONENT.get(), team.setDamageAlleviation(state));
        CohortUtil.syncToTeam(player);
    }

    public static boolean isAlleviation(ServerPlayer player) {
        TeamComponent team = player.getData(ModAttachments.TEAM_COMPONENT.get());
        return team.isDamageAlleviation();
    }

    public static void setFocus(ServerPlayer player, boolean state) {
        TeamComponent team = player.getData(ModAttachments.TEAM_COMPONENT.get()).withLeader(player.getUUID().toString());
        player.setData(ModAttachments.TEAM_COMPONENT.get(), team.setDamageFocus(state));
        CohortUtil.syncToTeam(player);
    }


    public static boolean isFocus(ServerPlayer player) {
        TeamComponent team = player.getData(ModAttachments.TEAM_COMPONENT.get());
        return team.isDamageFocus();
    }

    public static void syncToTeam(ServerPlayer player) {
        TeamComponent team = player.getData(ModAttachments.TEAM_COMPONENT.get()).withLeader(player.getUUID().toString());

        for(String uuid: team.memberUUIDs()) {
            ServerPlayer member = player.getServer().getPlayerList().getPlayer(UUID.fromString(uuid));
            if(member != null) member.setData(ModAttachments.TEAM_COMPONENT.get(), team);
        }
    }

    public static ArrayList<LivingEntity> getAllSubordinates(LivingEntity entity) {
        Level level = entity.level();

        if(level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return new ArrayList<>();
        }

        if(entity.getServer() == null) {
            return new ArrayList<>();
        }

        final ArrayList<LivingEntity> subordinates = new ArrayList<>(StreamSupport.stream(serverLevel.getAllEntities().spliterator(), false).filter(e -> e instanceof LivingEntity).map(e -> (LivingEntity) e).toList());

        for(ServerLevel l : entity.getServer().getAllLevels()) {
            if(l == level)
                continue;
            subordinates.addAll(StreamSupport.stream(l.getAllEntities().spliterator(), false).filter(e -> e instanceof LivingEntity).map(e -> (LivingEntity) e).toList());
        }

        subordinates.removeIf(e -> {
            if(e == entity)
                return true;
            SubordinateComponent component = e.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
            if (!component.isSubordinate()) {
                return true;
            }

            return !component.getControllerUUID().equals(entity.getStringUUID());
        });

        subordinates.sort(Comparator.comparingInt(LivingEntity::getId));
        return subordinates;
    }


    public static void removeSubordinate(Player player, int s) {
        Entity entity = CohortUtil.getAllSubordinates(player).get(s);
        if (!(entity instanceof LivingEntity livingEntity)) {
            player.sendSystemMessage(Component.literal("Subordinate not found!").withStyle(ChatFormatting.RED));
            return;
        }
        SubordinateComponent component = livingEntity.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
        if(!Objects.equals(component.getControllerUUID(), player.getStringUUID())) {
            player.sendSystemMessage(Component.literal("This subordinate does not belong to you!").withStyle(ChatFormatting.RED));
            return;
        }
        if(!component.isSubordinate()) return;
        component.setSubordinate(false);
        component.setControllerUUID("");
        livingEntity.setHealth(0);
        player.sendSystemMessage(Component.translatable("ability.lotm.chain_of_command.entity_released").withColor(0xa26fc9));
    }

    public static void obtainController(Player player, int s) {
        LivingEntity entity = CohortUtil.getAllSubordinates(player).get(s);
        if (!(entity instanceof LivingEntity livingEntity)) {
            player.sendSystemMessage(Component.literal("Subordinate not found!").withStyle(ChatFormatting.RED));
            return;
        }
        SubordinateComponent component = livingEntity.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
        if(!Objects.equals(component.getControllerUUID(), player.getStringUUID())) {
            player.sendSystemMessage(Component.literal("This subordinate does not belong to you!").withStyle(ChatFormatting.RED));
            return;
        }
        if(!component.isSubordinate()) return;
        ItemStack controllerItem = SubordinateUtils.createController(entity);


        if (!player.getInventory().add(controllerItem)) {
            player.drop(controllerItem, false);
        }
    }
}
