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
 * Server → Client: sync the caster's 2 target-status Envisioning slots for GUI display.
 */
public record SyncTargetEnvisionStatusPacket(List<TargetSlotInfo> slots,
                                             long restoreCooldownRemainingMs)
        implements CustomPacketPayload {

    public static final Type<SyncTargetEnvisionStatusPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_target_envision_status"));

    public record TargetSlotInfo(boolean isEmpty,
                                 String  targetName,
                                 String  pathway,
                                 int     sequence,
                                 boolean hasUniqueness,
                                 String  uniquenessPathway,
                                 int     effectCount,
                                 long    captureTimeMs,
                                 long    cooldownRemainingMs,
                                 boolean targetIsBeingRestored) {

        public static TargetSlotInfo empty() {
            return new TargetSlotInfo(true, "", "", 0, false, "", 0, 0L, 0L, false);
        }

        static void write(FriendlyByteBuf buf, TargetSlotInfo s) {
            buf.writeBoolean(s.isEmpty);
            if (!s.isEmpty) {
                buf.writeUtf(s.targetName);
                buf.writeUtf(s.pathway);
                buf.writeInt(s.sequence);
                buf.writeBoolean(s.hasUniqueness);
                buf.writeUtf(s.uniquenessPathway);
                buf.writeInt(s.effectCount);
                buf.writeLong(s.captureTimeMs);
                buf.writeLong(s.cooldownRemainingMs);
                buf.writeBoolean(s.targetIsBeingRestored);
            }
        }

        static TargetSlotInfo read(FriendlyByteBuf buf) {
            boolean empty = buf.readBoolean();
            if (empty) return empty();
            return new TargetSlotInfo(false,
                    buf.readUtf(), buf.readUtf(), buf.readInt(),
                    buf.readBoolean(), buf.readUtf(),
                    buf.readInt(), buf.readLong(), buf.readLong(), buf.readBoolean());
        }
    }

    public static final StreamCodec<FriendlyByteBuf, SyncTargetEnvisionStatusPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeInt(pkt.slots.size());
                        pkt.slots.forEach(s -> TargetSlotInfo.write(buf, s));
                        buf.writeLong(pkt.restoreCooldownRemainingMs);
                    },
                    buf -> {
                        int count = buf.readInt();
                        List<TargetSlotInfo> slots = new ArrayList<>(count);
                        for (int i = 0; i < count; i++) slots.add(TargetSlotInfo.read(buf));
                        long cd = buf.readLong();
                        return new SyncTargetEnvisionStatusPacket(slots, cd);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static volatile SyncTargetEnvisionStatusPacket CLIENT_CACHE = null;

    public static void handle(SyncTargetEnvisionStatusPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> CLIENT_CACHE = packet);
    }
}
