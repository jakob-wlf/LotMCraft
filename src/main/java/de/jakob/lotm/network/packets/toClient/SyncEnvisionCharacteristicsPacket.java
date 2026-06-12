package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Server → Client: sync the player's 3 envisioned characteristic slots for GUI display.
 */
public record SyncEnvisionCharacteristicsPacket(
        List<SlotInfo> slots,
        List<CooldownInfo> cooldowns,
        boolean targetSync,
        String targetPathway,
        int targetSequence)
        implements CustomPacketPayload {

    public static final Type<SyncEnvisionCharacteristicsPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_envision_characteristics"));

    // ── Inner record ──────────────────────────────────────────────────────────

    public record SlotInfo(boolean empty, String pathway, int sequence, long expiryMs) {

        public static SlotInfo emptySlot() {
            return new SlotInfo(true, "", -1, 0L);
        }

        static void write(FriendlyByteBuf buf, SlotInfo s) {
            buf.writeBoolean(s.empty());
            if (!s.empty()) {
                buf.writeUtf(s.pathway());
                buf.writeInt(s.sequence());
                buf.writeLong(s.expiryMs());
            }
        }

        static SlotInfo read(FriendlyByteBuf buf) {
            boolean empty = buf.readBoolean();
            if (empty) return emptySlot();
            return new SlotInfo(false, buf.readUtf(), buf.readInt(), buf.readLong());
        }

        public long remainingMs() {
            if (empty()) return 0L;
            return Math.max(0L, expiryMs - System.currentTimeMillis());
        }
    }

    // ── CooldownInfo ──────────────────────────────────────────────────────────

    public record CooldownInfo(String pathway, int sequence, long cooldownUntilMs) {
        static void write(FriendlyByteBuf buf, CooldownInfo c) {
            buf.writeUtf(c.pathway());
            buf.writeInt(c.sequence());
            buf.writeLong(c.cooldownUntilMs());
        }
        static CooldownInfo read(FriendlyByteBuf buf) {
            return new CooldownInfo(buf.readUtf(), buf.readInt(), buf.readLong());
        }
        public long remainingMs() { return Math.max(0L, cooldownUntilMs - System.currentTimeMillis()); }
    }

    // ── Codec ─────────────────────────────────────────────────────────────────

    public static final StreamCodec<FriendlyByteBuf, SyncEnvisionCharacteristicsPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeBoolean(pkt.targetSync());
                        buf.writeUtf(pkt.targetPathway());
                        buf.writeInt(pkt.targetSequence());
                        buf.writeInt(pkt.slots().size());
                        for (SlotInfo s : pkt.slots()) SlotInfo.write(buf, s);
                        buf.writeInt(pkt.cooldowns().size());
                        for (CooldownInfo c : pkt.cooldowns()) CooldownInfo.write(buf, c);
                    },
                    buf -> {
                        boolean ts   = buf.readBoolean();
                        String  tp   = buf.readUtf();
                        int     tseq = buf.readInt();
                        int size = buf.readInt();
                        List<SlotInfo> list = new ArrayList<>(size);
                        for (int i = 0; i < size; i++) list.add(SlotInfo.read(buf));
                        int cdSize = buf.readInt();
                        List<CooldownInfo> cds = new ArrayList<>(cdSize);
                        for (int i = 0; i < cdSize; i++) cds.add(CooldownInfo.read(buf));
                        return new SyncEnvisionCharacteristicsPacket(list, cds, ts, tp, tseq);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    // ── Client handler ────────────────────────────────────────────────────────

    /** Client-side cache of the latest self sync. */
    @OnlyIn(Dist.CLIENT)
    public static volatile List<SlotInfo> CLIENT_CACHE = List.of();
    @OnlyIn(Dist.CLIENT)
    public static volatile List<CooldownInfo> CLIENT_COOLDOWNS = List.of();

    /** Client-side cache of the latest target sync. */
    @OnlyIn(Dist.CLIENT)
    public static volatile List<SlotInfo> TARGET_CACHE = List.of();
    @OnlyIn(Dist.CLIENT)
    public static volatile List<CooldownInfo> TARGET_COOLDOWNS = List.of();
    @OnlyIn(Dist.CLIENT)
    public static volatile String TARGET_PATHWAY = "";
    @OnlyIn(Dist.CLIENT)
    public static volatile int TARGET_SEQUENCE = -1;

    public static void handle(SyncEnvisionCharacteristicsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.targetSync()) {
                TARGET_CACHE     = packet.slots();
                TARGET_COOLDOWNS = packet.cooldowns();
                TARGET_PATHWAY   = packet.targetPathway();
                TARGET_SEQUENCE  = packet.targetSequence();
            } else {
                CLIENT_CACHE     = packet.slots();
                CLIENT_COOLDOWNS = packet.cooldowns();
            }
        });
    }
}
