package de.jakob.lotm.network.packets.toClient;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AddEntityTagS2CPacket(String tag, int entityId) implements CustomPacketPayload {
    public static final Type<AddEntityTagS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "add_client_side_tag"));

    public static final StreamCodec<FriendlyByteBuf, AddEntityTagS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, AddEntityTagS2CPacket::tag,
                    ByteBufCodecs.INT, AddEntityTagS2CPacket::entityId,
                    AddEntityTagS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AddEntityTagS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleAddClientSideTagPacket(packet);
        });
    }
}