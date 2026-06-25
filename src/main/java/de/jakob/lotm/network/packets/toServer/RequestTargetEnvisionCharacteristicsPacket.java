package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.BeyonderComponent;
import de.jakob.lotm.attachments.EnvisionedCharacteristicsData;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.beyonders.sefirah.GreatOldOneManager;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.PathwayInfos;
import de.jakob.lotm.util.playerMap.Characteristic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Arrays;

/**
 * Client → Server: Chaos Sea Authority — envision a characteristic onto a TARGET player.
 *
 * Eligibility rules (enforced server-side):
 *   1. Sender must own the "chaos_sea" sefirot.
 *   2. Target must be online.
 *   3. Target's sequence must be >= owner's sequence + 2 (target is at least 2 ranks weaker).
 *   4. Characteristic pathway must NOT be a neighbouring pathway of the target's current path.
 *   5. Characteristic sequence must be >= 4 (only Seq 4 through 9 are eligible).
 *   6. Slot limit on target not exceeded.
 *   7. Characteristic is not on cooldown for the target.
 *
 * actions:
 *   "SYNC"    — send back the target's current envision state.
 *   "ENVISION" — envision a characteristic onto the target.
 *   "RELEASE"  — release one of the target's envisioned slots.
 */
public record RequestTargetEnvisionCharacteristicsPacket(
        String action,
        String pathway,
        int sequence,
        int slot,
        String targetName)
        implements CustomPacketPayload {

    private static final String CHAOS_SEA_ID = "chaos_sea";

    public static final Type<RequestTargetEnvisionCharacteristicsPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "req_target_envision_characteristics"));

    public static final StreamCodec<FriendlyByteBuf, RequestTargetEnvisionCharacteristicsPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeUtf(pkt.action());
                        buf.writeUtf(pkt.pathway());
                        buf.writeInt(pkt.sequence());
                        buf.writeInt(pkt.slot());
                        buf.writeUtf(pkt.targetName());
                    },
                    buf -> new RequestTargetEnvisionCharacteristicsPacket(
                            buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readInt(), buf.readUtf())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RequestTargetEnvisionCharacteristicsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer owner)) return;

            // Must be Chaos Sea owner
            if (!CHAOS_SEA_ID.equals(SefirahHandler.getClaimedSefirot(owner))) return;

            // Resolve target
            ServerPlayer target = owner.getServer().getPlayerList().getPlayerByName(packet.targetName());

            switch (packet.action()) {
                case "SYNC" -> {
                    if (target == null) return;
                    EnvisionedCharacteristicsData.sendTargetSync(owner, target,
                            EnvisionedCharacteristicsData.get(owner.getServer()));
                    return;
                }

                case "ENVISION" -> {
                    if (target == null) {
                        owner.sendSystemMessage(Component.literal("§cTarget player is not online."));
                        break;
                    }

                    String path = packet.pathway().trim();
                    int seq = packet.sequence();

                    if (path.isEmpty() || seq < 1 || seq > 9) break;

                    // Rule 3: target must be 2+ sequences weaker
                    int ownerSeq = BeyonderData.getSequence(owner);
                    int targetSeq = BeyonderData.getSequence(target);
                    if (targetSeq < ownerSeq + 2) {
                        owner.sendSystemMessage(Component.literal(
                                "§cTarget must be at least 2 sequences weaker than you (Seq " + (ownerSeq + 2) + " or higher)."));
                        break;
                    }

                    // Rule 5: characteristic must be at seq >= 4 (seq 4–9)
                    if (seq < 4) {
                        owner.sendSystemMessage(Component.literal(
                                "§cOnly characteristics at Seq 4 and below (4–9) can be envisioned onto targets."));
                        break;
                    }

                    // Rule 4: pathway must NOT be a neighbour of target's current pathway
                    String targetPath = BeyonderData.getPathway(target);
                    if (!isNonNeighbouring(path, targetPath)) {
                        owner.sendSystemMessage(Component.literal(
                                "§c" + prettify(path) + " is a neighbouring pathway to the target's " + prettify(targetPath) + " — forbidden."));
                        break;
                    }

                    EnvisionedCharacteristicsData data = EnvisionedCharacteristicsData.get(owner.getServer());

                    // Rule 6: slot limit (target envisioning allows only 1 slot at a time)
                    if (data.activeCount(target.getUUID()) >= EnvisionedCharacteristicsData.TARGET_MAX_SLOTS) {
                        owner.sendSystemMessage(Component.literal(
                                "§cTarget already has an envisioned characteristic active. Release it first."));
                        break;
                    }

                    // Rule 7: cooldown
                    if (data.isOnCooldown(target.getUUID(), path, seq)) {
                        long remaining = data.getCooldownRemainingMs(target.getUUID(), path, seq);
                        owner.sendSystemMessage(Component.literal(
                                "§c" + prettify(path) + " Seq " + seq + " is on cooldown for target: "
                                + formatTime(remaining / 1000) + " remaining."));
                        break;
                    }

                    // Add characteristic to target
                    BeyonderComponent component = target.getData(ModAttachments.BEYONDER_COMPONENT);
                    int currentStack = component.getCharacteristicList().stream()
                            .filter(c -> c.pathway().equals(path) && c.sequence() == seq)
                            .mapToInt(Characteristic::stack)
                            .findFirst().orElse(0);
                    component.setCharacteristic(currentStack + 1, seq, path);
                    de.jakob.lotm.events.BeyonderDataTickHandler.invalidateCache(target);
                    PacketHandler.syncBeyonderDataToPlayer(target);

                    // Duration: 1 min if target is a Great Old One or has a sefirot, else 5 min
                    boolean targetIsSpecial = GreatOldOneManager.isGreatOldOne(target)
                            || !SefirahHandler.getClaimedSefirot(target).isEmpty();
                    long slotDuration = targetIsSpecial
                            ? EnvisionedCharacteristicsData.TARGET_SLOT_SHORT_DURATION_MS
                            : EnvisionedCharacteristicsData.TARGET_SLOT_DURATION_MS;
                    String durationLabel = targetIsSpecial ? "1 minute" : "5 minutes";

                    data.addSlot(target.getUUID(), path, seq, slotDuration);

                    owner.sendSystemMessage(Component.literal(
                            "§a[Chaos Sea] Envisioned " + prettify(path) + " Seq " + seq
                            + " onto " + target.getName().getString() + " for " + durationLabel + "."));
                    target.sendSystemMessage(Component.literal(
                            "§5[Chaos Sea] The Authority has envisioned " + prettify(path)
                            + " Seq " + seq + " onto you for " + durationLabel + "."));
                }

                case "RELEASE" -> {
                    if (target == null) {
                        owner.sendSystemMessage(Component.literal("§cTarget player is not online."));
                        break;
                    }

                    int slotIdx = packet.slot();
                    EnvisionedCharacteristicsData data = EnvisionedCharacteristicsData.get(owner.getServer());
                    EnvisionedCharacteristicsData.Slot released = data.releaseSlot(target.getUUID(), slotIdx);
                    if (!released.isEmpty()) {
                        data.addCooldown(target.getUUID(), released.pathway, released.sequence);
                        EnvisionedCharacteristicsData.removeCharacteristicFromPlayer(
                                target, released.pathway, released.sequence);
                        owner.sendSystemMessage(Component.literal(
                                "§7[Chaos Sea] Released " + prettify(released.pathway) + " Seq "
                                + released.sequence + " from " + target.getName().getString() + " (3h cooldown)."));
                        target.sendSystemMessage(Component.literal(
                                "§7[Chaos Sea] Envisioned characteristic removed: §e"
                                + prettify(released.pathway) + " Seq " + released.sequence));
                    }
                }

                default -> { /* fall through to sync */ }
            }

            // Sync updated target state back to owner
            if (target != null) {
                EnvisionedCharacteristicsData.sendTargetSync(owner, target,
                        EnvisionedCharacteristicsData.get(owner.getServer()));
                // Also sync to target if they're online
                EnvisionedCharacteristicsData.sendSync(target,
                        EnvisionedCharacteristicsData.get(owner.getServer()));
            }
        });
    }

    /**
     * Returns true if {@code pathway} is NOT a neighbouring path to {@code targetPathway},
     * and NOT the same as {@code targetPathway}.
     */
    private static boolean isNonNeighbouring(String pathway, String targetPathway) {
        if (pathway.equals(targetPathway)) return false;
        PathwayInfos info = BeyonderData.pathwayInfos.get(targetPathway);
        if (info == null) return true; // unknown pathway — allow
        return Arrays.stream(info.neighboringPathways()).noneMatch(pathway::equals);
    }

    private static String prettify(String s) {
        if (s == null || s.isEmpty()) return "Unknown";
        var sb = new StringBuilder();
        for (String w : s.split("_"))
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(' ');
        return sb.toString().trim();
    }

    private static String formatTime(long secs) {
        long h = secs / 3600, m = (secs % 3600) / 60, s = secs % 60;
        return h > 0 ? h + "h " + m + "m" : m + "m " + s + "s";
    }
}
