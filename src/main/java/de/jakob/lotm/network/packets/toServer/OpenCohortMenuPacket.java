package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.cohort.CohortMenuProvider;
import io.netty.buffer.ByteBufAllocator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record OpenCohortMenuPacket(List<String[]> members) implements CustomPacketPayload {
    public static final Type<OpenCohortMenuPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_cohort"));

public static final StreamCodec<FriendlyByteBuf, OpenCohortMenuPacket> STREAM_CODEC = CustomPacketPayload.codec(
        // ENCODER (Writing to buffer)
        (payload, buf) -> {
            buf.writeVarInt(payload.members.size());
            for (String[] array : payload.members) {
                buf.writeVarInt(array.length);
                for (String str : array) {
                    buf.writeUtf(str);
                }
            }
        },
        // DECODER (Reading from buffer)
        buf -> {
            int listSize = buf.readVarInt();
            List<String[]> dataList = new ArrayList<>(listSize);
            for (int i = 0; i < listSize; i++) {
                int arrayLength = buf.readVarInt();
                String[] array = new String[arrayLength];
                for (int j = 0; j < arrayLength; j++) {
                    array[j] = buf.readUtf();
                }
                dataList.add(array);
            }
            return new OpenCohortMenuPacket(dataList);
        }
);


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenCohortMenuPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer()) {
                ServerPlayer player = (ServerPlayer) context.player();

                List<String[]> members = packet.members();



//                player.openMenu(new CohortMenuProvider(members));
            }
        });
    }

}