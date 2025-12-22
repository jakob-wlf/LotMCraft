package de.jakob.lotm.util.helper;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AllyRequestResponsePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side manager for ally requests
 */
public class AllyRequestManager {
    
    private static final Map<UUID, String> pendingRequests = new ConcurrentHashMap<>();
    private static final long REQUEST_DISPLAY_TIME = 30000; // 30 seconds
    
    /**
     * Add a pending request (called when packet is received)
     */
    public static void addPendingRequest(UUID requesterUUID, String requesterName) {
        pendingRequests.put(requesterUUID, requesterName);
        
        // Display the request to the player
        displayRequest(requesterUUID, requesterName);
        
        // Schedule removal after timeout
        scheduleRemoval(requesterUUID);
    }
    
    /**
     * Accept an ally request
     */
    public static void acceptRequest(UUID requesterUUID) {
        if (!pendingRequests.containsKey(requesterUUID)) {
            return;
        }
        
        // Send packet to server
        PacketHandler.sendToServer(new AllyRequestResponsePacket(requesterUUID, true));
        pendingRequests.remove(requesterUUID);
    }
    
    /**
     * Deny an ally request
     */
    public static void denyRequest(UUID requesterUUID) {
        if (!pendingRequests.containsKey(requesterUUID)) {
            return;
        }
        
        // Send packet to server
        PacketHandler.sendToServer(new AllyRequestResponsePacket(requesterUUID, false));
        pendingRequests.remove(requesterUUID);
    }
    
    /**
     * Display the request with clickable accept/deny buttons
     */
    private static void displayRequest(UUID requesterUUID, String requesterName) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        Component message = Component.translatable("lotm.ally.request_received", requesterName)
                .withColor(0x2196F3)
                .append(Component.literal(" "))
                .append(Component.literal("[✓ Accept]")
                        .withColor(0x4CAF50)
                        .withStyle(style -> style
                                .withClickEvent(new ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/lotm_accept_ally " + requesterUUID.toString()
                                ))
                                .withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Component.literal("Click to accept ally request")
                                ))
                        ))
                .append(Component.literal(" "))
                .append(Component.literal("[✗ Deny]")
                        .withColor(0xF44336)
                        .withStyle(style -> style
                                .withClickEvent(new ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/lotm_deny_ally " + requesterUUID.toString()
                                ))
                                .withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Component.literal("Click to deny ally request")
                                ))
                        ));
        
        mc.player.sendSystemMessage(message);
    }
    
    /**
     * Schedule removal of request after timeout
     */
    private static void scheduleRemoval(UUID requesterUUID) {
        new Thread(() -> {
            try {
                Thread.sleep(REQUEST_DISPLAY_TIME);
                pendingRequests.remove(requesterUUID);
            } catch (InterruptedException ignored) {}
        }).start();
    }
    
    /**
     * Check if a request is pending
     */
    public static boolean hasPendingRequest(UUID requesterUUID) {
        return pendingRequests.containsKey(requesterUUID);
    }
    
    /**
     * Clear all pending requests
     */
    public static void clearAll() {
        pendingRequests.clear();
    }
}