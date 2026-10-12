package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record CancelPhotonBlockEffectPacket(UUID effectUUID) implements CustomPacketPayload {

    public static final Type<CancelPhotonBlockEffectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "cancel_photon_block_effect"));

    public static final StreamCodec<ByteBuf, CancelPhotonBlockEffectPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.effectUUID().toString());
            },
            buf -> {
                String uuidString = ByteBufCodecs.STRING_UTF8.decode(buf);
                UUID effectUUID = UUID.fromString(uuidString);
                return new CancelPhotonBlockEffectPacket(effectUUID);
            }
    );
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(CancelPhotonBlockEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.cancelBlockEffect(packet.effectUUID);
            }
        });
    }
}