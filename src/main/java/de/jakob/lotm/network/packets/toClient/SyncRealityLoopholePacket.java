package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.error.RealityLoopholeAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record SyncRealityLoopholePacket(Set<UUID> data)
        implements CustomPacketPayload {

    public static final Type<SyncRealityLoopholePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_reality_loophole"));

    public static final StreamCodec<FriendlyByteBuf, SyncRealityLoopholePacket> STREAM_CODEC =
            StreamCodec.of(
                    SyncRealityLoopholePacket::write,
                    SyncRealityLoopholePacket::read
            );

    private static SyncRealityLoopholePacket read(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Set<UUID> set = new HashSet<>();

        for (int i = 0; i < size; i++) {
            set.add(buf.readUUID());
        }

        return new SyncRealityLoopholePacket(set);
    }

    private static void write(FriendlyByteBuf buf, SyncRealityLoopholePacket packet) {
        buf.writeInt(packet.data.size());

        for (UUID uuid : packet.data) {
            buf.writeUUID(uuid);
        }
    }

    @Override
    public Type<SyncRealityLoopholePacket> type() {
        return TYPE;
    }

    public static void handle(SyncRealityLoopholePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            RealityLoopholeAbility.phasedOutClient = new HashSet<>(packet.data);
        });
    }
}
