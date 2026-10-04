package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.DawnSpearThrows;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncDawnSpearThrowPacket(int entityId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncDawnSpearThrowPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_dawn_spear_throw"));

    public static final StreamCodec<ByteBuf, SyncDawnSpearThrowPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SyncDawnSpearThrowPacket::entityId,
            SyncDawnSpearThrowPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncDawnSpearThrowPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> DawnSpearThrows.mark(packet.entityId()));
    }
}
