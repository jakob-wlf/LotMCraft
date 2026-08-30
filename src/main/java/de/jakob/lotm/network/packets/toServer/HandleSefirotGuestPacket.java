package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record HandleSefirotGuestPacket(UUID allyId, int action) implements CustomPacketPayload {
    public static final Type<HandleSefirotGuestPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "handle_sefirot_guest"));

    public static final StreamCodec<ByteBuf, HandleSefirotGuestPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
            HandleSefirotGuestPacket::allyId,
            ByteBufCodecs.INT,
            HandleSefirotGuestPacket::action,
            HandleSefirotGuestPacket::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HandleSefirotGuestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ServerPlayer target = player.getServer().getPlayerList().getPlayer(packet.allyId());

            if(target == null) {
                player.sendSystemMessage(Component.translatable("lotm.sefirot.cannot_reach_player").withStyle(ChatFormatting.RED));
                return;
            }

            player.closeContainer();
            switch (packet.action()) {
                case 0 -> {
                    if(BeyonderData.getSequence(player) < BeyonderData.getSequence(target)) {
                        SefirahHandler.teleportToSefirot(target, SefirahHandler.getSefirot(player), true);
                    }
                    SefirahHandler.inviteToSefirot(player, target);
                }
                case 1 -> SefirahHandler.kickOutOfSefirot(player, target);
            }
        });
    }
}
