package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DisableAbilityUsageForTimePacket(int entityId, String cause, int ticks) implements CustomPacketPayload {
    
    public static final Type<DisableAbilityUsageForTimePacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "disable_ability_usage_for_time"));
    
    public static final StreamCodec<ByteBuf, DisableAbilityUsageForTimePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        DisableAbilityUsageForTimePacket::entityId,
        ByteBufCodecs.STRING_UTF8,
        DisableAbilityUsageForTimePacket::cause,
        ByteBufCodecs.INT,
        DisableAbilityUsageForTimePacket::ticks,
        DisableAbilityUsageForTimePacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(DisableAbilityUsageForTimePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleDisableAbilityUsageForTimePacket(packet);
            }
        });
    }
}