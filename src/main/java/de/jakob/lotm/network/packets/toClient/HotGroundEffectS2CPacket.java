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

public record HotGroundEffectS2CPacket(
    List<BlockPos> blockPositions,
    boolean restore,
    int waveNumber
) implements CustomPacketPayload {
    
    public static final Type<HotGroundEffectS2CPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "hot_ground_effect"));
    
    public static final StreamCodec<ByteBuf, HotGroundEffectS2CPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
        HotGroundEffectS2CPacket::blockPositions,
        ByteBufCodecs.BOOL,
        HotGroundEffectS2CPacket::restore,
        ByteBufCodecs.VAR_INT,
        HotGroundEffectS2CPacket::waveNumber,
        HotGroundEffectS2CPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(HotGroundEffectS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleHotGroundEffectPacket
                    (packet.restore, packet.blockPositions, packet.waveNumber);
        });
    }
}
