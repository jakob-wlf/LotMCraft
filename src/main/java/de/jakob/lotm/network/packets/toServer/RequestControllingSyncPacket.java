package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.beyonders.abilities.fool.marionettes.ControllingUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static de.jakob.lotm.LOTMCraft.MOD_ID;

public record RequestControllingSyncPacket() implements CustomPacketPayload {

    public static final Type<RequestControllingSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "request_controlling_sync"));

    public static final StreamCodec<ByteBuf, RequestControllingSyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buffer, RequestControllingSyncPacket packet) {
            // No data to encode
        }

        @Override
        public RequestControllingSyncPacket decode(ByteBuf buffer) {
            return new RequestControllingSyncPacket();
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestControllingSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ControllingUtils.syncControllingData(serverPlayer);
            }
        });
    }
}