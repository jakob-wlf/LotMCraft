package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.sefirah.GreatOldOneManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent by the client when the player clicks "Transcend Sequence" in the Introspect screen.
 * Server validates conditions and starts the Transcendence ritual.
 */
public record RequestTranscendencePacket() implements CustomPacketPayload {

    public static final Type<RequestTranscendencePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_transcendence"));

    public static final StreamCodec<ByteBuf, RequestTranscendencePacket> STREAM_CODEC =
            StreamCodec.unit(new RequestTranscendencePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestTranscendencePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            if (GreatOldOneManager.isGreatOldOne(player)) return;
            if (GreatOldOneManager.isTranscending(player)) return;

            // Must be in the overworld
            if (!player.level().dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "You must be in the Overworld to begin transcendence."));
                return;
            }

            java.util.List<String> missing = GreatOldOneManager.getMissingRequirements(player);
            if (!missing.isEmpty()) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§cYou cannot transcend sequence yet:").withStyle(s -> s.withColor(0xFF4444)));
                for (String reason : missing) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7 - " + reason));
                }
                return;
            }

            GreatOldOneManager.startTranscendence(player);
        });
    }
}
