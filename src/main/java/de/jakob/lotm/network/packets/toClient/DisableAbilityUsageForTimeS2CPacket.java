package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DisableAbilityUsageForTimeS2CPacket(int entityId, String cause, int ticks) implements CustomPacketPayload {
    
    public static final Type<DisableAbilityUsageForTimeS2CPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "disable_ability_usage_for_time"));
    
    public static final StreamCodec<ByteBuf, DisableAbilityUsageForTimeS2CPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        DisableAbilityUsageForTimeS2CPacket::entityId,
        ByteBufCodecs.STRING_UTF8,
        DisableAbilityUsageForTimeS2CPacket::cause,
        ByteBufCodecs.INT,
        DisableAbilityUsageForTimeS2CPacket::ticks,
        DisableAbilityUsageForTimeS2CPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(DisableAbilityUsageForTimeS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleDisableAbilityUsageForTimePacket(packet);
            }
        });
    }
}