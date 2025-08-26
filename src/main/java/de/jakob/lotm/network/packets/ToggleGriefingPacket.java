package de.jakob.lotm.network.packets;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleGriefingPacket() implements CustomPacketPayload {
    public static final Type<ToggleGriefingPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "toggle_griefing"));

    public static final StreamCodec<FriendlyByteBuf, ToggleGriefingPacket> STREAM_CODEC =
            StreamCodec.unit(new ToggleGriefingPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleGriefingPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            boolean isGriefingEnabled = BeyonderData.isGriefingEnabled(player);
            BeyonderData.setGriefingEnabled(player, !isGriefingEnabled);

            Component message = isGriefingEnabled ?
                    Component.literal("Griefing disabled").withStyle(ChatFormatting.RED) :
                    Component.literal("Griefing enabled").withStyle(ChatFormatting.GREEN);

            player.displayClientMessage(message, true);
        });
    }
}