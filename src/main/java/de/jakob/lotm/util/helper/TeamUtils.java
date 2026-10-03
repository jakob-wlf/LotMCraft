package de.jakob.lotm.util.helper;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SharedAbilitiesComponent;
import de.jakob.lotm.attachments.TeamComponent;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSharedAbilitiesDataPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamUtils {

    /** Max team members (not counting the leader) based on leader's sequence. */
    public static int getMaxTeamSize(int sequence) {
        if (sequence <= 1) return 6;
        if (sequence <= 2) return 5;
        return 4; // sequence 3 and 4
    }

    /** Slots each member may contribute based on leader's sequence. */
    public static int getSlotsPerMember(int sequence) {
        if (sequence <= 1) return 3;
        if (sequence <= 2) return 2;
        return 1; // sequence 3 and 4
    }

    /** Returns true if the player is a Red Priest at sequence <= 3. */
    public static boolean isEligibleLeader(ServerPlayer player) {
        return BeyonderData.getPathway(player).equals("red_priest")
                && BeyonderData.getSequence(player) <= 3;
    }

    /**
     * Adds a member to the leader's team and sets the member's leaderUUID.
     * Does not send the invite — call this after the invite is accepted.
     */
    public static boolean addMember(ServerPlayer leader, ServerPlayer member) {
        int sequence = BeyonderData.getSequence(leader);
        TeamComponent leaderTeam = leader.getData(ModAttachments.TEAM_COMPONENT.get());

        if (leaderTeam.memberCount() >= getMaxTeamSize(sequence)) {
            return false;
        }
        if (leaderTeam.hasMember(member.getStringUUID())) {
            return false;
        }

        leader.setData(ModAttachments.TEAM_COMPONENT.get(), leaderTeam.addMember(member.getStringUUID()));

        TeamComponent memberTeam = member.getData(ModAttachments.TEAM_COMPONENT.get());
        member.setData(ModAttachments.TEAM_COMPONENT.get(), memberTeam.withLeader(leader.getStringUUID()));

        // Clear any stale contributions from a previous team so slots start empty
        SharedAbilitiesComponent memberShared = member.getData(ModAttachments.SHARED_ABILITIES_COMPONENT.get());
        member.setData(ModAttachments.SHARED_ABILITIES_COMPONENT.get(),
                memberShared.removeContributions(leader.getStringUUID()));

        syncToTeam(leader);
        return true;
    }

    /**
     * Removes a member from the leader's team and clears the member's leaderUUID.
     * Also clears the member's shared ability contributions for this team.
     */
    public static void removeMember(ServerPlayer leader, ServerPlayer member) {
        TeamComponent leaderTeam = leader.getData(ModAttachments.TEAM_COMPONENT.get());
        leader.setData(ModAttachments.TEAM_COMPONENT.get(), leaderTeam.removeMember(member.getStringUUID()));

        TeamComponent memberTeam = member.getData(ModAttachments.TEAM_COMPONENT.get());
        member.setData(ModAttachments.TEAM_COMPONENT.get(), memberTeam.clearLeader());

        SharedAbilitiesComponent memberShared = member.getData(ModAttachments.SHARED_ABILITIES_COMPONENT.get());
        member.setData(ModAttachments.SHARED_ABILITIES_COMPONENT.get(),
                memberShared.removeContributions(leader.getStringUUID()));

        // Tell the removed member to clear their team data immediately
        PacketHandler.sendToPlayer(member, new SyncSharedAbilitiesDataPacket(
                "", new java.util.ArrayList<>(), new java.util.ArrayList<>(), new java.util.HashMap<>(), 0, 0));

        syncToTeam(leader);
    }

    /**
     * Disbands the entire team of the given leader, clearing all member data.
     */
    public static void disbandTeam(ServerPlayer leader, MinecraftServer server) {
        TeamComponent leaderTeam = leader.getData(ModAttachments.TEAM_COMPONENT.get());
        SyncSharedAbilitiesDataPacket clearPacket = new SyncSharedAbilitiesDataPacket(
                "", new java.util.ArrayList<>(), new java.util.ArrayList<>(), new java.util.HashMap<>(), 0, 0);
        for (String memberUUID : new ArrayList<>(leaderTeam.memberUUIDs())) {
            ServerPlayer member = server.getPlayerList().getPlayer(
                    java.util.UUID.fromString(memberUUID));
            if (member != null) {
                member.setData(ModAttachments.TEAM_COMPONENT.get(),
                        member.getData(ModAttachments.TEAM_COMPONENT.get()).clearLeader());
                SharedAbilitiesComponent shared = member.getData(ModAttachments.SHARED_ABILITIES_COMPONENT.get());
                member.setData(ModAttachments.SHARED_ABILITIES_COMPONENT.get(),
                        shared.removeContributions(leader.getStringUUID()));
                // Tell each member to clear their team data immediately
                PacketHandler.sendToPlayer(member, clearPacket);
            }
        }
        leader.setData(ModAttachments.TEAM_COMPONENT.get(), new TeamComponent());
        // Also clear the leader's own client data
        PacketHandler.sendToPlayer(leader, clearPacket);
    }

    /**
     * Syncs the full shared abilities data to all online team members and the leader.
     */
    public static void syncToTeam(ServerPlayer leader) {
        TeamComponent leaderTeam = leader.getData(ModAttachments.TEAM_COMPONENT.get());
        int sequence = BeyonderData.getSequence(leader);
        int maxSize = getMaxTeamSize(sequence);
        int slots = getSlotsPerMember(sequence);

        List<String> memberUUIDs = new ArrayList<>(leaderTeam.memberUUIDs());
        List<String> memberNames = new ArrayList<>();
        Map<String, List<String>> contributions = new HashMap<>();

        MinecraftServer server = leader.getServer();
        if (server == null) return;

        // Include leader's own contributions keyed by their UUID
        SharedAbilitiesComponent leaderShared = leader.getData(ModAttachments.SHARED_ABILITIES_COMPONENT.get());
        contributions.put(leader.getStringUUID(), new ArrayList<>(leaderShared.getContributions(leader.getStringUUID())));

        for (String uuid : memberUUIDs) {
            ServerPlayer member = server.getPlayerList().getPlayer(
                    java.util.UUID.fromString(uuid));
            memberNames.add(member != null ? member.getName().getString() : uuid);

            SharedAbilitiesComponent shared = member != null
                    ? member.getData(ModAttachments.SHARED_ABILITIES_COMPONENT.get())
                    : new SharedAbilitiesComponent();
            contributions.put(uuid, new ArrayList<>(shared.getContributions(leader.getStringUUID())));
        }

        SyncSharedAbilitiesDataPacket packet = new SyncSharedAbilitiesDataPacket(
                leader.getStringUUID(), memberUUIDs, memberNames, contributions, maxSize, slots);

        PacketHandler.sendToPlayer(leader, packet);
        for (String uuid : memberUUIDs) {
            ServerPlayer member = server.getPlayerList().getPlayer(
                    java.util.UUID.fromString(uuid));
            if (member != null) {
                PacketHandler.sendToPlayer(member, packet);
            }
        }
    }

    /**
     * Called when a member updates their contributed abilities.
     * Validates slot count and persists, then re-syncs the team.
     */
    public static boolean updateMemberContributions(ServerPlayer member, List<String> abilityIds) {
        TeamComponent memberTeam = member.getData(ModAttachments.TEAM_COMPONENT.get());
        if (member.getServer() == null) return false;

        ServerPlayer leader;
        if (!memberTeam.isInTeam()) {
            // Player is the leader — use themselves
            if (memberTeam.memberCount() == 0) return false;
            leader = member;
        } else {
            leader = member.getServer().getPlayerList().getPlayer(
                    java.util.UUID.fromString(memberTeam.leaderUUID()));
            if (leader == null) return false;
        }

        int maxSlots = getSlotsPerMember(BeyonderData.getSequence(leader));
        List<String> capped = abilityIds.size() > maxSlots
                ? new ArrayList<>(abilityIds.subList(0, maxSlots))
                : new ArrayList<>(abilityIds);

        // Key contributions by leader's UUID (own UUID for the leader)
        SharedAbilitiesComponent shared = member.getData(ModAttachments.SHARED_ABILITIES_COMPONENT.get());
        member.setData(ModAttachments.SHARED_ABILITIES_COMPONENT.get(),
                shared.setContributions(leader.getStringUUID(), capped));

        syncToTeam(leader);
        return true;
    }
}
