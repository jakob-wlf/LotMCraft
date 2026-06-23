package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayActingEffectS2CPacket() implements CustomPacketPayload {
    public static final Type<PlayActingEffectS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "play_acting_effect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayActingEffectS2CPacket> STREAM_CODEC =
            StreamCodec.unit(new PlayActingEffectS2CPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlayActingEffectS2CPacket payload, IPayloadContext context) {
        context.enqueueWork(ClientHandler::playActingEffect);
    }
}