package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncLivingEntityBeyonderDataPacket(
        int entityId,
        String pathway,
        int sequence,
        float spirituality
) implements CustomPacketPayload {

    public static final Type<SyncLivingEntityBeyonderDataPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_living_beyonder_data"));

    public static final StreamCodec<FriendlyByteBuf, SyncLivingEntityBeyonderDataPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SyncLivingEntityBeyonderDataPacket::entityId,
                    ByteBufCodecs.STRING_UTF8, SyncLivingEntityBeyonderDataPacket::pathway,
                    ByteBufCodecs.VAR_INT, SyncLivingEntityBeyonderDataPacket::sequence,
                    ByteBufCodecs.FLOAT, SyncLivingEntityBeyonderDataPacket::spirituality,
                    SyncLivingEntityBeyonderDataPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncLivingEntityBeyonderDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.syncLivingEntityBeyonderData(packet);
            }
        });
    }
}
