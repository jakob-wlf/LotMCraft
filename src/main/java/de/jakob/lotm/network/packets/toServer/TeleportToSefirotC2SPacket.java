package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TeleportToSefirotC2SPacket() implements CustomPacketPayload {
    public static final Type<TeleportToSefirotC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "teleport_to_sefirot"));

    public static final StreamCodec<FriendlyByteBuf, TeleportToSefirotC2SPacket> STREAM_CODEC =
            StreamCodec.unit(new TeleportToSefirotC2SPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    // TODO: Claiming here is temporary, add proper ritual for claiming
    public static void handle(TeleportToSefirotC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer() && context.player() instanceof ServerPlayer serverPlayer) {
                if(!SefirahHandler.hasSefirot(serverPlayer)) {
                    AbilityUtil.sendActionBar(serverPlayer, Component.translatable("lotm.sefirot.no_sefirot").withColor(0x942de3));
                    return;
                }

                SefirahHandler.teleportToSefirot(serverPlayer, true);
            }
        });
    }
}