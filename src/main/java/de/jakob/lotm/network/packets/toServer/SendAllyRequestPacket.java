package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.AllyUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record SendAllyRequestPacket(String allyName) implements CustomPacketPayload {
    public static final Type<SendAllyRequestPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "send_ally_request"));

    public static final StreamCodec<ByteBuf, SendAllyRequestPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SendAllyRequestPacket::allyName,
            SendAllyRequestPacket::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SendAllyRequestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            Player target = player.server.getPlayerList().getPlayerByName(packet.allyName());
            if(target == null) {
                player.sendSystemMessage(Component.translatable("lotm.ally.request.not_found", packet.allyName()).withStyle(style -> style.withColor(0xF44336)));
                return;
            }
            AllyUtil.sendAllyRequest(player, target);
        });
    }
}
