package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → Client: open the Sell Your Soul gate screen.
 * {@code cooldownEndMillis} is the epoch-millis time when the cooldown expires (0 = ready now).
 */
public record OpenSellYourSoulGatePacket(long cooldownEndMillis) implements CustomPacketPayload {

    public static final Type<OpenSellYourSoulGatePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_sell_soul_gate"));

    public static final StreamCodec<ByteBuf, OpenSellYourSoulGatePacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public OpenSellYourSoulGatePacket decode(ByteBuf buf) {
            return new OpenSellYourSoulGatePacket(ByteBufCodecs.VAR_LONG.decode(buf));
        }
        @Override
        public void encode(ByteBuf buf, OpenSellYourSoulGatePacket p) {
            ByteBufCodecs.VAR_LONG.encode(buf, p.cooldownEndMillis());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(OpenSellYourSoulGatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isClient()) return;
            ClientHandler.openSellYourSoulGateScreen(packet);
        });
    }
}
