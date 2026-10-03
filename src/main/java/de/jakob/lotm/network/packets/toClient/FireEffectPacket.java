package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record FireEffectPacket(
    List<BlockPos> blockPositions,
    boolean restore,
    int waveNumber
) implements CustomPacketPayload {
    
    public static final Type<FireEffectPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "fire_effect"));
    
    public static final StreamCodec<ByteBuf, FireEffectPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
        FireEffectPacket::blockPositions,
        ByteBufCodecs.BOOL,
        FireEffectPacket::restore,
        ByteBufCodecs.VAR_INT,
        FireEffectPacket::waveNumber,
        FireEffectPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(FireEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleFireEffectPacket(packet.restore, packet.blockPositions, packet.waveNumber);
        });
    }
}
