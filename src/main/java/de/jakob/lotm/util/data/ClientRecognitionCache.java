package de.jakob.lotm.util.data;

import de.jakob.lotm.network.packets.toClient.RecognitionSyncPacket;
import net.minecraft.client.Minecraft;

public final class ClientRecognitionCache {

    private ClientRecognitionCache() {
    }

    private static final long TIMEOUT_TICKS = 30;

    private static boolean active;
    private static boolean hasTarget;
    private static int targetId = -1;
    private static String name = "";
    private static String pathway = "";
    private static int sequence;
    private static long lastUpdate;

    public static void update(RecognitionSyncPacket p) {
        active = p.active();
        hasTarget = p.hasTarget();
        targetId = p.entityId();
        name = p.name();
        pathway = p.pathway();
        sequence = p.sequence();
        Minecraft mc = Minecraft.getInstance();
        lastUpdate = mc.level != null ? mc.level.getGameTime() : 0;
    }

    public static boolean isActive() {
        Minecraft mc = Minecraft.getInstance();
        if (!active || mc.level == null) return false;
        return mc.level.getGameTime() - lastUpdate <= TIMEOUT_TICKS;
    }

    public static boolean hasTarget() { return hasTarget; }
    public static int getTargetId() { return targetId; }
    public static String getName() { return name; }
    public static String getPathway() { return pathway; }
    public static int getSequence() { return sequence; }

    public static void clear() {
        active = false;
        hasTarget = false;
        targetId = -1;
        name = "";
        pathway = "";
        sequence = 0;
    }
}