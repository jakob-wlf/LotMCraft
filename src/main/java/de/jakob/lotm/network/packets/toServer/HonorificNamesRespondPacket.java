package de.jakob.lotm.network.packets.toServer;

import com.mojang.datafixers.util.Pair;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.events.HonorificNamesEventHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Sent by the client when the player responds to a pending prayer from the Honorific Names menu.
 *
 * @param senderUUID UUID of the player who sent the prayer (the one who typed the honorific name in chat)
 * @param teleport   true → teleport the sender to the target; false → queue a message transfer
 */
public record HonorificNamesRespondPacket(UUID senderUUID, boolean teleport) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<HonorificNamesRespondPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "honorific_names_respond"));

    public static final StreamCodec<FriendlyByteBuf, HonorificNamesRespondPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeUUID(pkt.senderUUID());
                        buf.writeBoolean(pkt.teleport());
                    },
                    buf -> new HonorificNamesRespondPacket(buf.readUUID(), buf.readBoolean())
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HonorificNamesRespondPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;

            ServerPlayer target = (ServerPlayer) context.player();
            UUID targetUUID = target.getUUID();
            UUID senderUUID = pkt.senderUUID();

            // Validate that this prayer is still pending
            Pair<UUID, UUID> key = new Pair<>(targetUUID, senderUUID);
            if (!HonorificNamesEventHandler.answerState.contains(key)) {
                return;
            }

            ServerPlayer sender = target.server.getPlayerList().getPlayer(senderUUID);
            if (sender == null) {
                HonorificNamesEventHandler.answerState.remove(key);
                HonorificNamesEventHandler.removePendingPrayer(targetUUID, senderUUID);
                return;
            }

            if (pkt.teleport()) {
                // Teleport target (prayer receiver) to sender's (the one who prayed) location
                target.teleportTo(
                        sender.serverLevel(),
                        sender.getX(), sender.getY(), sender.getZ(),
                        sender.getYRot(), sender.getXRot()
                );
            } else {
                // Queue message transfer: target's next chat message goes to sender
                HonorificNamesEventHandler.isInTransferring.put(targetUUID, senderUUID);
                target.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("Your next message will be sent to " + sender.getName().getString() + ".")
                                .withStyle(net.minecraft.ChatFormatting.GREEN)
                );
            }

            HonorificNamesEventHandler.answerState.remove(key);
            HonorificNamesEventHandler.removePendingPrayer(targetUUID, senderUUID);
        });
    }
}
