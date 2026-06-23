package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CloseAbilityWheelC2SPacket() implements CustomPacketPayload {
    public static final Type<CloseAbilityWheelC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "close_ability_wheel"));

    public static final StreamCodec<FriendlyByteBuf, CloseAbilityWheelC2SPacket> STREAM_CODEC =
            StreamCodec.unit(new CloseAbilityWheelC2SPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CloseAbilityWheelC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer()) {
                ServerPlayer player = (ServerPlayer) context.player();
                player.closeContainer();
            }
        });
    }

}