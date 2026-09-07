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

public record RemoveAllyPacket(UUID allyUUID, String allyName) implements CustomPacketPayload {
    public static final Type<RemoveAllyPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "remove_ally"));

    public static final StreamCodec<ByteBuf, RemoveAllyPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
            RemoveAllyPacket::allyUUID,
            ByteBufCodecs.STRING_UTF8,
            RemoveAllyPacket::allyName,
            RemoveAllyPacket::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RemoveAllyPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            Player target = player.serverLevel().getPlayerByUUID(packet.allyUUID());
            if(target == null) {
                player.sendSystemMessage(Component.translatable("lotm.ally.remove.not_found", packet.allyName()).withStyle(style -> style.withColor(0xF44336)));
                return;
            }

            AllyUtil.removeAllies(player, target);
            target.sendSystemMessage(Component.translatable("lotm.ally.removed", player.getName()).withStyle(style -> style.withColor(0xF44336)));
            player.sendSystemMessage(Component.translatable("lotm.ally.removed", target.getName()).withStyle(style -> style.withColor(0xF44336)));
        });
    }
}
