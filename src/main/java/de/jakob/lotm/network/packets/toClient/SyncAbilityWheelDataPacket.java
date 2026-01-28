package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;

public record SyncAbilityWheelDataPacket(ArrayList<String> abilityIds) implements CustomPacketPayload {
    
    public static final Type<SyncAbilityWheelDataPacket> TYPE = 
        new Type<>(ResourceLocation.fromNamespaceAndPath("lotm", "sync_ability_wheel_data"));

    public static final StreamCodec<ByteBuf, SyncAbilityWheelDataPacket> STREAM_CODEC = 
        StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            SyncAbilityWheelDataPacket::abilityIds,
            SyncAbilityWheelDataPacket::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncAbilityWheelDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleSyncAbilityWheelDataPacket(packet);
        });
    }
}