package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.BeyonderComponent;
import de.jakob.lotm.attachments.EnvisionedCharacteristicsData;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.sefirah.SefirotAuthorityManager;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.Characteristic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → Server: Chaos Sea Envisioning > Self > Characteristics action.
 *
 * <pre>
 *   action = "SYNC"    → just send current state back, no change
 *   action = "ENVISION" → envision characteristic (pathway + sequence)
 *   action = "RELEASE"  → release an envisioned slot (by index)
 * </pre>
 */
public record RequestEnvisionCharacteristicsPacket(String action, String pathway, int sequence, int slot)
        implements CustomPacketPayload {

    public static final Type<RequestEnvisionCharacteristicsPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "req_envision_characteristics"));

    public static final StreamCodec<FriendlyByteBuf, RequestEnvisionCharacteristicsPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeUtf(pkt.action());
                        buf.writeUtf(pkt.pathway());
                        buf.writeInt(pkt.sequence());
                        buf.writeInt(pkt.slot());
                    },
                    buf -> new RequestEnvisionCharacteristicsPacket(
                            buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readInt())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    // ── Server handler ────────────────────────────────────────────────────────

    public static void handle(RequestEnvisionCharacteristicsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            EnvisionedCharacteristicsData data = EnvisionedCharacteristicsData.get(player.getServer());

            switch (packet.action()) {
                case "SYNC" -> { /* fall through to sync at end */ }

                case "ENVISION" -> {
                    String path = packet.pathway().trim();
                    int    seq  = packet.sequence();

                    if (path.isEmpty() || seq < 0 || seq > 9) break;

                    // Slot limit
                    if (data.activeCount(player.getUUID()) >= EnvisionedCharacteristicsData.MAX_SLOTS) {
                        player.sendSystemMessage(Component.literal(
                                "§cAll 3 envision slots are occupied. Release one first."));
                        break;
                    }

                    // Pathway must be in history or current
                    String[] history = BeyonderData.getPathwayHistory(player);
                    String currentPath = BeyonderData.getPathway(player);
                    boolean inHistory = currentPath.equals(path);
                    if (!inHistory) {
                        for (String h : history) {
                            if (path.equals(h)) { inHistory = true; break; }
                        }
                    }
                    if (!inHistory) {
                        player.sendSystemMessage(Component.literal(
                                "§cYou have never been on the " + path + " pathway."));
                        break;
                    }

                    // Cooldown check
                    if (data.isOnCooldown(player.getUUID(), path, seq)) {
                        long remaining = data.getCooldownRemainingMs(player.getUUID(), path, seq);
                        player.sendSystemMessage(Component.literal(
                                "§c" + prettify(path) + " Seq " + seq + " is on cooldown: "
                                + formatTime(remaining / 1000) + " remaining."));
                        break;
                    }

                    // Increment the characteristic stack by 1 (preserves any real consumed copies)
                    BeyonderComponent component = player.getData(ModAttachments.BEYONDER_COMPONENT);
                    int currentStack = component.getCharacteristicList().stream()
                            .filter(c -> c.pathway().equals(path) && c.sequence() == seq)
                            .mapToInt(Characteristic::stack)
                            .findFirst().orElse(0);
                    component.setCharacteristic(currentStack + 1, seq, path);
                    de.jakob.lotm.events.BeyonderDataTickHandler.invalidateCache(player);
                    PacketHandler.syncBeyonderDataToPlayer(player);

                    // Record in EnvisionedCharacteristicsData
                    data.addSlot(player.getUUID(), path, seq);

                    player.sendSystemMessage(Component.literal(
                            "§a[Chaos Sea] Envisioning " + prettify(path) + " Seq " + seq
                            + " for 30 minutes."));
                }

                case "RELEASE" -> {
                    int slotIdx = packet.slot();
                    EnvisionedCharacteristicsData.Slot released = data.releaseSlot(player.getUUID(), slotIdx);
                    if (!released.isEmpty()) {
                        data.addCooldown(player.getUUID(), released.pathway, released.sequence);
                        EnvisionedCharacteristicsData.removeCharacteristicFromPlayer(
                                player, released.pathway, released.sequence);
                        player.sendSystemMessage(Component.literal(
                                "§7[Chaos Sea] Released envisioned characteristic: §e"
                                + prettify(released.pathway) + " Seq " + released.sequence
                                + " §7(3h cooldown)"));
                    }
                }

                default -> { /* unknown action, just sync */ }
            }

            // Always sync updated state back to client
            EnvisionedCharacteristicsData.sendSync(player, data);
        });
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static String prettify(String pathway) {
        if (pathway == null || pathway.isEmpty()) return "Unknown";
        String[] words = pathway.split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    private static String formatTime(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        return h > 0 ? h + "h " + m + "m" : m + "m " + s + "s";
    }
}
