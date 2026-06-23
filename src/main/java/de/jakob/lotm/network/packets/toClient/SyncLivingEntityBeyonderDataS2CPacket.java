package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncLivingEntityBeyonderDataS2CPacket(
        int entityId,
        String pathway,
        int sequence,
        float spirituality
) implements CustomPacketPayload {

    public static final Type<SyncLivingEntityBeyonderDataS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_living_beyonder_data"));

    public static final StreamCodec<FriendlyByteBuf, SyncLivingEntityBeyonderDataS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SyncLivingEntityBeyonderDataS2CPacket::entityId,
                    ByteBufCodecs.STRING_UTF8, SyncLivingEntityBeyonderDataS2CPacket::pathway,
                    ByteBufCodecs.VAR_INT, SyncLivingEntityBeyonderDataS2CPacket::sequence,
                    ByteBufCodecs.FLOAT, SyncLivingEntityBeyonderDataS2CPacket::spirituality,
                    SyncLivingEntityBeyonderDataS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncLivingEntityBeyonderDataS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.syncLivingEntityBeyonderData(packet);
            }
        });
    }
}
