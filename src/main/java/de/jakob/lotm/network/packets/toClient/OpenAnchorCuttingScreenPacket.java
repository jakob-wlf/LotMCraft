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

public record OpenAnchorCuttingScreenPacket(List<AnchorInfo> anchors) implements CustomPacketPayload {
    public static final Type<OpenAnchorCuttingScreenPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_anchor_cutting_screen"));

    public static final StreamCodec<ByteBuf, OpenAnchorCuttingScreenPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public OpenAnchorCuttingScreenPacket decode(ByteBuf buffer) {
            int size = Math.max(0, Math.min(buffer.readInt(), 1024));
            List<AnchorInfo> anchors = new ArrayList<>(size);
            for (int index = 0; index < size; index++) {
                anchors.add(new AnchorInfo(
                        ByteBufCodecs.STRING_UTF8.decode(buffer),
                        ByteBufCodecs.STRING_UTF8.decode(buffer),
                        buffer.readFloat()));
            }
            return new OpenAnchorCuttingScreenPacket(anchors);
        }

        @Override
        public void encode(ByteBuf buffer, OpenAnchorCuttingScreenPacket packet) {
            buffer.writeInt(packet.anchors.size());
            for (AnchorInfo anchor : packet.anchors) {
                ByteBufCodecs.STRING_UTF8.encode(buffer, anchor.anchorId);
                ByteBufCodecs.STRING_UTF8.encode(buffer, anchor.name);
                buffer.writeFloat(anchor.strength);
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenAnchorCuttingScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.openAnchorCuttingScreen(packet);
            }
        });
    }

    public record AnchorInfo(String anchorId, String name, float strength) {}
}