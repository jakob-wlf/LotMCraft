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

public record DarknessEffectS2CPacket(
    List<BlockPos> blockPositions,
    boolean restore,
    int waveNumber
) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<DarknessEffectS2CPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "darkness_effect"));
    
    public static final StreamCodec<ByteBuf, DarknessEffectS2CPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
        DarknessEffectS2CPacket::blockPositions,
        ByteBufCodecs.BOOL,
        DarknessEffectS2CPacket::restore,
        ByteBufCodecs.VAR_INT,
        DarknessEffectS2CPacket::waveNumber,
        DarknessEffectS2CPacket::new
    );
    
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(DarknessEffectS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleDarknessEffectPacket(packet.restore, packet.blockPositions, packet.waveNumber);
        });
    }
}
