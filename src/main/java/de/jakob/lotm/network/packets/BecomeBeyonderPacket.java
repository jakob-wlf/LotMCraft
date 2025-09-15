package de.jakob.lotm.network.packets;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BecomeBeyonderPacket(String pathway, int sequence) implements CustomPacketPayload {
    public static final Type<BecomeBeyonderPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "become_beyonder"));

    public static final StreamCodec<FriendlyByteBuf, BecomeBeyonderPacket> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.of(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf),
                    BecomeBeyonderPacket::pathway,
                    StreamCodec.of(FriendlyByteBuf::writeInt, FriendlyByteBuf::readInt),
                    BecomeBeyonderPacket::sequence,
                    BecomeBeyonderPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BecomeBeyonderPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            // Server-side logic
            BeyonderData.setBeyonder(player, packet.pathway(), packet.sequence());

            if(BeyonderData.pathwayInfos.get(packet.pathway()) == null) {
                return;
            }

            Component message = Component.translatable(
                    "lotm.beyonder_message.full",
                    Component.literal(BeyonderData.pathwayInfos.get(packet.pathway).getName()).withColor(BeyonderData.pathwayInfos.get(packet.pathway).color()),
                    Component.literal(String.valueOf(packet.sequence())).withColor(BeyonderData.pathwayInfos.get(packet.pathway).color())
            ).withColor(0x808080);
            player.displayClientMessage(message, true);
        });
    }
}
