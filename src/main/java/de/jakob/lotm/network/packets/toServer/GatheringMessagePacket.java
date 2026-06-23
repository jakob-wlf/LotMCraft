package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.GatheringData;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Set;
import java.util.UUID;

/**
 * Client → Server: the Sefirah Castle owner sends a private message to all gathering members.
 */
public record GatheringMessagePacket(String message) implements CustomPacketPayload {

    public static final Type<GatheringMessagePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "gathering_message"));

    public static final StreamCodec<ByteBuf, GatheringMessagePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, GatheringMessagePacket::message,
                    GatheringMessagePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(GatheringMessagePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer owner)) return;

            // Security: only sefirah_castle owner
            if (!"sefirah_castle".equals(SefirahHandler.getClaimedSefirot(owner))) return;

            String text = packet.message().trim();
            if (text.isEmpty()) return;
            // Cap length to prevent abuse
            if (text.length() > 256) text = text.substring(0, 256);

            Component msg = Component.literal("[Gathering] ")
                    .withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.literal(owner.getName().getString() + ": ")
                            .withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(text)
                            .withStyle(ChatFormatting.WHITE));

            GatheringData data = GatheringData.get(owner.server);
            Set<UUID> members = data.getMembers(owner.getUUID());

            // Send to all online members
            for (UUID memberUUID : members) {
                ServerPlayer member = owner.server.getPlayerList().getPlayer(memberUUID);
                if (member != null) {
                    member.sendSystemMessage(msg);
                }
            }

            // Also echo back to the owner
            owner.sendSystemMessage(msg);
        });
    }
}
