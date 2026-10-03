package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.data.ClientUniquenessCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncUniquenessPacket(boolean hasUniqueness, String pathway, int killCount)
        implements CustomPacketPayload {

    public static final Type<SyncUniquenessPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_uniqueness"));

    public static final StreamCodec<FriendlyByteBuf, SyncUniquenessPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncUniquenessPacket::hasUniqueness,
                    ByteBufCodecs.STRING_UTF8, SyncUniquenessPacket::pathway,
                    ByteBufCodecs.INT, SyncUniquenessPacket::killCount,
                    SyncUniquenessPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncUniquenessPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientUniquenessCache.setHasUniqueness(packet.hasUniqueness());
                ClientUniquenessCache.setPathway(packet.pathway());
                ClientUniquenessCache.setKillCount(packet.killCount());
            }
        });
    }
}
