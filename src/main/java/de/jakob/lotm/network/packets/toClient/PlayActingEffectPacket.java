package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayActingEffectPacket() implements CustomPacketPayload {
    public static final Type<PlayActingEffectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "play_acting_effect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayActingEffectPacket> STREAM_CODEC =
            StreamCodec.unit(new PlayActingEffectPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlayActingEffectPacket payload, IPayloadContext context) {
        context.enqueueWork(ClientHandler::playActingEffect);
    }
}