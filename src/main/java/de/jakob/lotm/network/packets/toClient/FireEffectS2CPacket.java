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

public record FireEffectS2CPacket(
    List<BlockPos> blockPositions,
    boolean restore,
    int waveNumber
) implements CustomPacketPayload {
    
    public static final Type<FireEffectS2CPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "fire_effect"));
    
    public static final StreamCodec<ByteBuf, FireEffectS2CPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
        FireEffectS2CPacket::blockPositions,
        ByteBufCodecs.BOOL,
        FireEffectS2CPacket::restore,
        ByteBufCodecs.VAR_INT,
        FireEffectS2CPacket::waveNumber,
        FireEffectS2CPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(FireEffectS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleFireEffectPacket(packet.restore, packet.blockPositions, packet.waveNumber);
        });
    }
}
