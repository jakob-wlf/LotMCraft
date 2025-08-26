package de.jakob.lotm.network.packets;


import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncBeyonderDataPacket(String pathway, int sequence, float spirituality, boolean griefingEnabled) implements CustomPacketPayload {
    public static final Type<SyncBeyonderDataPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_beyonder_data"));

    public static final StreamCodec<FriendlyByteBuf, SyncBeyonderDataPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, SyncBeyonderDataPacket::pathway,
                    ByteBufCodecs.VAR_INT, SyncBeyonderDataPacket::sequence,
                    ByteBufCodecs.FLOAT, SyncBeyonderDataPacket::spirituality,
                    ByteBufCodecs.BOOL, SyncBeyonderDataPacket::griefingEnabled,
                    SyncBeyonderDataPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncBeyonderDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Update client-side cache
            ClientBeyonderCache.updatePlayerData(
                    context.player().getUUID(),
                    packet.pathway(),
                    packet.sequence(),
                    packet.spirituality(),
                    packet.griefingEnabled()
            );
        });
    }
}