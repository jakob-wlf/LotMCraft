package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.CharExchange.RecipePathExchangeHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestRecipePathExchangePacket(int slotIndex) implements CustomPacketPayload {

    public static final Type<RequestRecipePathExchangePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_recipe_path_exchange"));

    public static final StreamCodec<ByteBuf, RequestRecipePathExchangePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RequestRecipePathExchangePacket::slotIndex,
            RequestRecipePathExchangePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RequestRecipePathExchangePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer player)) return;
            RecipePathExchangeHandler.processExchange(player, packet.slotIndex());
        });
    }
}
