package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Server → Client: open the Daily Spin slot-machine screen.
 * When {@code canSpin} is false the screen just shows "Already claimed today".
 */
public record OpenDailySpinScreenPacket(List<String> reelNames, int landingIndex, boolean canSpin)
        implements CustomPacketPayload {

    public static final Type<OpenDailySpinScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_daily_spin_screen"));

    public static final StreamCodec<ByteBuf, OpenDailySpinScreenPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public OpenDailySpinScreenPacket decode(ByteBuf buf) {
            int size = ByteBufCodecs.VAR_INT.decode(buf);
            List<String> names = new ArrayList<>(size);
            for (int i = 0; i < size; i++) names.add(ByteBufCodecs.STRING_UTF8.decode(buf));
            int landing = ByteBufCodecs.VAR_INT.decode(buf);
            boolean can = ByteBufCodecs.BOOL.decode(buf);
            return new OpenDailySpinScreenPacket(names, landing, can);
        }

        @Override
        public void encode(ByteBuf buf, OpenDailySpinScreenPacket p) {
            ByteBufCodecs.VAR_INT.encode(buf, p.reelNames().size());
            for (String name : p.reelNames()) ByteBufCodecs.STRING_UTF8.encode(buf, name);
            ByteBufCodecs.VAR_INT.encode(buf, p.landingIndex());
            ByteBufCodecs.BOOL.encode(buf, p.canSpin());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(OpenDailySpinScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isClient()) return;
            ClientHandler.openDailySpinScreen(packet);
        });
    }
}
