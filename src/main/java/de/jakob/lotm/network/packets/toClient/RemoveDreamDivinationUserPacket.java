package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RemoveDreamDivinationUserPacket() implements CustomPacketPayload {
    public static final Type<RemoveDreamDivinationUserPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "remove_dream_divination_user"));

    public static final StreamCodec<FriendlyByteBuf, RemoveDreamDivinationUserPacket> STREAM_CODEC =
            StreamCodec.unit(new RemoveDreamDivinationUserPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handle(RemoveDreamDivinationUserPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.removeDreamDivinationUser(context.player());
            }
        });
    }
}