package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;

public record SyncAbilityWheelDataS2CPacket(ArrayList<String> abilityIds) implements CustomPacketPayload {
    
    public static final Type<SyncAbilityWheelDataS2CPacket> TYPE = 
        new Type<>(ResourceLocation.fromNamespaceAndPath("lotm", "sync_ability_wheel_data"));

    public static final StreamCodec<ByteBuf, SyncAbilityWheelDataS2CPacket> STREAM_CODEC = 
        StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            SyncAbilityWheelDataS2CPacket::abilityIds,
            SyncAbilityWheelDataS2CPacket::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncAbilityWheelDataS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleSyncAbilityWheelDataPacket(packet);
        });
    }
}