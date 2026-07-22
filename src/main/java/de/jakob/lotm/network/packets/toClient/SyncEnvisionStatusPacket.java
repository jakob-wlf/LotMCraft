package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Server → Client: sync the caster's 3 self-status Envisioning slots for GUI display.
 */
public record SyncEnvisionStatusPacket(List<SlotInfo> slots,
                                       boolean hasActiveRestore,
                                       long    restoreExpiryMs)
        implements CustomPacketPayload {

    public static final Type<SyncEnvisionStatusPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_envision_status"));

    /** Minimal display info per slot — no full snapshot data sent. */
    public record SlotInfo(boolean isEmpty,
                           String  pathway,
                           int     sequence,
                           boolean hasUniqueness,
                           String  uniquenessPathway,
                           int     effectCount,
                           long    captureTimeMs,
                           long    cooldownRemainingMs,
                           boolean blockedByDeath) {

        public static SlotInfo empty() {
            return new SlotInfo(true, "", 0, false, "", 0, 0L, 0L, false);
        }

        static void write(FriendlyByteBuf buf, SlotInfo s) {
            buf.writeBoolean(s.isEmpty);
            if (!s.isEmpty) {
                buf.writeUtf(s.pathway);
                buf.writeInt(s.sequence);
                buf.writeBoolean(s.hasUniqueness);
                buf.writeUtf(s.uniquenessPathway);
                buf.writeInt(s.effectCount);
                buf.writeLong(s.captureTimeMs);
                buf.writeLong(s.cooldownRemainingMs);
                buf.writeBoolean(s.blockedByDeath);
            }
        }

        static SlotInfo read(FriendlyByteBuf buf) {
            boolean empty = buf.readBoolean();
            if (empty) return empty();
            return new SlotInfo(false,
                    buf.readUtf(), buf.readInt(), buf.readBoolean(), buf.readUtf(),
                    buf.readInt(), buf.readLong(), buf.readLong(), buf.readBoolean());
        }
    }

    public static final StreamCodec<FriendlyByteBuf, SyncEnvisionStatusPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeInt(pkt.slots.size());
                        pkt.slots.forEach(s -> SlotInfo.write(buf, s));
                        buf.writeBoolean(pkt.hasActiveRestore);
                        buf.writeLong(pkt.restoreExpiryMs);
                    },
                    buf -> {
                        int count = buf.readInt();
                        List<SlotInfo> slots = new ArrayList<>(count);
                        for (int i = 0; i < count; i++) slots.add(SlotInfo.read(buf));
                        boolean has  = buf.readBoolean();
                        long    exp  = buf.readLong();
                        return new SyncEnvisionStatusPacket(slots, has, exp);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    /** Thread-safe cache read by the Self Status screen. */
    public static volatile SyncEnvisionStatusPacket CLIENT_CACHE = null;

    public static void handle(SyncEnvisionStatusPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> CLIENT_CACHE = packet);
    }
}
