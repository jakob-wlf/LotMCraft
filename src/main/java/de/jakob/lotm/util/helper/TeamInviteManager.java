package de.jakob.lotm.util.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side manager for pending team invites.
 * Mirrors AllyRequestManager but for team invites from Red Priest leaders.
 */
public class TeamInviteManager {

    private static final Map<UUID, String> pendingInvites = new HashMap<>();

    public static void addPendingInvite(UUID leaderUUID, String leaderName) {
        pendingInvites.put(leaderUUID, leaderName);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Component acceptButton = Component.literal("[Accept]")
                .withStyle(style -> style
                        .withColor(0x4CAF50)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/lotm_accept_team " + leaderUUID)));

        Component declineButton = Component.literal("[Decline]")
                .withStyle(style -> style
                        .withColor(0xF44336)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/lotm_decline_team " + leaderUUID)));

        Component message = Component.literal(leaderName + " has invited you to their team. ")
                .append(acceptButton)
                .append(Component.literal(" "))
                .append(declineButton);

        mc.player.sendSystemMessage(message);
    }

    public static void removePendingInvite(UUID leaderUUID) {
        pendingInvites.remove(leaderUUID);
    }

    public static boolean hasPendingInvite(UUID leaderUUID) {
        return pendingInvites.containsKey(leaderUUID);
    }
}
