package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DailySpinComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenSellYourSoulGatePacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client → Server: player clicked "Sell Soul" — request cooldown state to open the gate screen. */
public record RequestSellYourSoulInfoPacket() implements CustomPacketPayload {

    public static final Type<RequestSellYourSoulInfoPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_sell_soul_info"));

    public static final StreamCodec<ByteBuf, RequestSellYourSoulInfoPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestSellYourSoulInfoPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RequestSellYourSoulInfoPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer player)) return;

            DailySpinComponent comp = player.getData(ModAttachments.DAILY_SPIN_COMPONENT);
            PacketHandler.sendToPlayer(player, new OpenSellYourSoulGatePacket(comp.getSellSoulReadyTime()));
        });
    }
}
