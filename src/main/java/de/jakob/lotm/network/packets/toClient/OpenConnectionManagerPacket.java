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

public record OpenConnectionManagerPacket(List<ConnectionInfo> connections) implements CustomPacketPayload {
    public static final Type<OpenConnectionManagerPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_connection_manager"));

    public static final StreamCodec<ByteBuf, OpenConnectionManagerPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public OpenConnectionManagerPacket decode(ByteBuf buffer) {
            int size = Math.min(3, buffer.readUnsignedByte());
            List<ConnectionInfo> connections = new ArrayList<>(size);
            for (int index = 0; index < size; index++) {
                connections.add(ConnectionInfo.decode(buffer));
            }
            return new OpenConnectionManagerPacket(connections);
        }

        @Override
        public void encode(ByteBuf buffer, OpenConnectionManagerPacket packet) {
            int size = Math.min(3, packet.connections.size());
            buffer.writeByte(size);
            for (int index = 0; index < size; index++) {
                packet.connections.get(index).encode(buffer);
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenConnectionManagerPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isClient()) return;
            ClientHandler.openConnectionManagerScreen(packet);
        });
    }

    public record ConnectionInfo(String connectionId, String itemId, String itemName,
                                 String holderName, String pathway, int sequence) {
        private static ConnectionInfo decode(ByteBuf buffer) {
            return new ConnectionInfo(
                ByteBufCodecs.STRING_UTF8.decode(buffer),
                ByteBufCodecs.STRING_UTF8.decode(buffer),
                ByteBufCodecs.STRING_UTF8.decode(buffer),
                ByteBufCodecs.STRING_UTF8.decode(buffer),
                ByteBufCodecs.STRING_UTF8.decode(buffer),
                buffer.readInt());
        }

        private void encode(ByteBuf buffer) {
            ByteBufCodecs.STRING_UTF8.encode(buffer, connectionId);
            ByteBufCodecs.STRING_UTF8.encode(buffer, itemId);
            ByteBufCodecs.STRING_UTF8.encode(buffer, itemName);
            ByteBufCodecs.STRING_UTF8.encode(buffer, holderName);
            ByteBufCodecs.STRING_UTF8.encode(buffer, pathway);
            buffer.writeInt(sequence);
        }
    }
}
