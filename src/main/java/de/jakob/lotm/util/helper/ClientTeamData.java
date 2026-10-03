package de.jakob.lotm.util.helper;

import de.jakob.lotm.network.packets.toClient.SyncSharedAbilitiesDataPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side cache of team data, populated by SyncSharedAbilitiesDataPacket.
 * Used by IntrospectScreen to render the Shared Abilities tab.
 */
public class ClientTeamData {

    private static String leaderUUID = "";
    private static List<String> memberUUIDs = new ArrayList<>();
    private static List<String> memberNames = new ArrayList<>();
    private static Map<String, List<String>> contributions = new HashMap<>();
    private static int maxTeamSize = 4;
    private static int slotsPerMember = 1;

    public static void update(SyncSharedAbilitiesDataPacket packet) {
        leaderUUID = packet.leaderUUID();
        memberUUIDs = new ArrayList<>(packet.teamMemberUUIDs());
        memberNames = new ArrayList<>(packet.teamMemberNames());
        contributions = new HashMap<>(packet.contributions());
        maxTeamSize = packet.maxTeamSize();
        slotsPerMember = packet.slotsPerMember();
    }

    public static void clear() {
        leaderUUID = "";
        memberUUIDs = new ArrayList<>();
        memberNames = new ArrayList<>();
        contributions = new HashMap<>();
        maxTeamSize = 4;
        slotsPerMember = 1;
    }

    public static String getLeaderUUID() { return leaderUUID; }
    public static List<String> getMemberUUIDs() { return memberUUIDs; }
    public static List<String> getMemberNames() { return memberNames; }
    public static Map<String, List<String>> getContributions() { return contributions; }
    public static int getMaxTeamSize() { return maxTeamSize; }
    public static int getSlotsPerMember() { return slotsPerMember; }

    public static List<String> getContributionsFor(String memberUUID) {
        return contributions.getOrDefault(memberUUID, new ArrayList<>());
    }

    public static boolean hasTeam() {
        return !leaderUUID.isEmpty();
    }

    /**
     * Returns a flat list of all shared ability IDs contributed by teammates
     * (excluding the calling player's own contributions).
     * Used to populate the shared ability wheel.
     */
    public static List<String> getSharedAbilityIds(String myUUID) {
        List<String> result = new ArrayList<>();
        for (String memberUUID : memberUUIDs) {
            if (memberUUID.equals(myUUID)) continue;
            result.addAll(getContributionsFor(memberUUID));
        }
        return result;
    }
}
