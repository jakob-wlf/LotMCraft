package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
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

public record HandleAllyRequestPacket(UUID allyUUID, String allyName, boolean accepted) implements CustomPacketPayload {
    public static final Type<HandleAllyRequestPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "handle_ally_request"));

    public static final StreamCodec<ByteBuf, HandleAllyRequestPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
            HandleAllyRequestPacket::allyUUID,
            ByteBufCodecs.STRING_UTF8,
            HandleAllyRequestPacket::allyName,
            ByteBufCodecs.BOOL,
            HandleAllyRequestPacket::accepted,
            HandleAllyRequestPacket::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HandleAllyRequestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            if(!packet.accepted()) {
                player.sendSystemMessage(Component.translatable("lotm.ally.request.declined", packet.allyName()).withStyle(style -> style.withColor(0xF44336)));
                Player target = player.serverLevel().getPlayerByUUID(packet.allyUUID());
                if(target != null) {
                    target.sendSystemMessage(Component.translatable("lotm.ally.request.declined_by", player.getName()).withStyle(style -> style.withColor(0xF44336)));
                }
                AllyUtil.declineAllyRequest(player, packet.allyUUID());
                return;
            }
            else {
                Player target = player.serverLevel().getPlayerByUUID(packet.allyUUID());
                if(target == null) {
                    return;
                }

                AllyUtil.acceptAllyRequest(player, packet.allyUUID());
            }
        });
    }
}
