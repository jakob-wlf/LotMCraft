package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Quaternionf;

import java.util.UUID;

public record CancelPhotonEntityEffectPacket(UUID effectUUID) implements CustomPacketPayload {

    public static final Type<CancelPhotonEntityEffectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "cancel_photon_entity_effect"));

    public static final StreamCodec<ByteBuf, CancelPhotonEntityEffectPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.effectUUID().toString());
            },
            buf -> {
                String uuidString = ByteBufCodecs.STRING_UTF8.decode(buf);
                UUID effectUUID = UUID.fromString(uuidString);
                return new CancelPhotonEntityEffectPacket(effectUUID);
            }
    );
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(CancelPhotonEntityEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.cancelEntityEffect(packet.effectUUID);
            }
        });
    }
}