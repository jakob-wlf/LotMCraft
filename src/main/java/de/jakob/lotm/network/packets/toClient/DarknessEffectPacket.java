package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record DarknessEffectPacket(
    List<BlockPos> blockPositions,
    boolean restore,
    int waveNumber
) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<DarknessEffectPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "darkness_effect"));
    
    public static final StreamCodec<ByteBuf, DarknessEffectPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
        DarknessEffectPacket::blockPositions,
        ByteBufCodecs.BOOL,
        DarknessEffectPacket::restore,
        ByteBufCodecs.VAR_INT,
        DarknessEffectPacket::waveNumber,
        DarknessEffectPacket::new
    );
    
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(DarknessEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleDarknessEffectPacket(packet.restore, packet.blockPositions, packet.waveNumber);
        });
    }
}
