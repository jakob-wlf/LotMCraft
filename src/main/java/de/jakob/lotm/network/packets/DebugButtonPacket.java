package de.jakob.lotm.network.packets;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DebugButtonPacket() implements CustomPacketPayload {
    public static final Type<DebugButtonPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "debug_button"));

    public static final StreamCodec<FriendlyByteBuf, DebugButtonPacket> STREAM_CODEC =
            StreamCodec.unit(new DebugButtonPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DebugButtonPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            player.displayClientMessage(
                    Component.literal("Debug button clicked!").withStyle(ChatFormatting.GREEN),
                    true
            );
        });
    }
}