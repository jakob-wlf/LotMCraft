package de.jakob.lotm.network.packets.toClient;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AddClientSideTagPacket(String tag, int entityId) implements CustomPacketPayload {
    public static final Type<AddClientSideTagPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "add_client_side_tag"));

    public static final StreamCodec<FriendlyByteBuf, AddClientSideTagPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, AddClientSideTagPacket::tag,
                    ByteBufCodecs.INT, AddClientSideTagPacket::entityId,
                    AddClientSideTagPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AddClientSideTagPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleAddClientSideTagPacket(packet);
        });
    }
}