package de.jakob.lotm.network.packets;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClearBeyonderDataPacket() implements CustomPacketPayload {
    public static final Type<ClearBeyonderDataPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "clear_beyonder_data"));

    public static final StreamCodec<FriendlyByteBuf, ClearBeyonderDataPacket> STREAM_CODEC =
            StreamCodec.unit(new ClearBeyonderDataPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClearBeyonderDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            BeyonderData.clearBeyonderData(player);
            player.displayClientMessage(
                    Component.literal("Beyonder data cleared").withStyle(ChatFormatting.YELLOW),
                    true
            );
        });
    }
}