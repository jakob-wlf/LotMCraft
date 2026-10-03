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

public record HotGroundEffectPacket(
    List<BlockPos> blockPositions,
    boolean restore,
    int waveNumber
) implements CustomPacketPayload {
    
    public static final Type<HotGroundEffectPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "hot_ground_effect"));
    
    public static final StreamCodec<ByteBuf, HotGroundEffectPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
        HotGroundEffectPacket::blockPositions,
        ByteBufCodecs.BOOL,
        HotGroundEffectPacket::restore,
        ByteBufCodecs.VAR_INT,
        HotGroundEffectPacket::waveNumber,
        HotGroundEffectPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(HotGroundEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleHotGroundEffectPacket
                    (packet.restore, packet.blockPositions, packet.waveNumber);
        });
    }
}
