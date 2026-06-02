package de.jakob.lotm.network.packets.toClient;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.playerMap.Characteristic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;

public record SyncBeyonderDataPacket(String pathway, int sequence, float spirituality, boolean griefingEnabled, float digestionProgress, String[] pathwayHistory, ArrayList<Characteristic> charList) implements CustomPacketPayload {
    public static final Type<SyncBeyonderDataPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_beyonder_data"));

    private static final StreamCodec<FriendlyByteBuf, String[]> PATHWAY_HISTORY_CODEC =
            StreamCodec.of(
                    (buf, strings) -> {
                        for (int i = 0; i < 10; i++) {
                            String s = i < strings.length ? strings[i] : null;
                            buf.writeUtf(s != null ? s : "");
                        }
                    },
                    buf -> {
                        String[] strings = new String[10];
                        for (int i = 0; i < 10; i++) {
                            String s = buf.readUtf();
                            strings[i] = s.isEmpty() ? null : s;
                        }
                        return strings;
                    }
            );

    private static final StreamCodec<FriendlyByteBuf, ArrayList<Characteristic>> CHAR_LIST_CODEC =
            StreamCodec.of(
                    (buf, chars) -> {
                        ByteBufCodecs.VAR_INT.encode(buf, chars.size());
                        for (Characteristic characteristic : chars) {
                            ByteBufCodecs.STRING_UTF8.encode(buf, characteristic.pathway());
                            ByteBufCodecs.VAR_INT.encode(buf, characteristic.stack());
                            ByteBufCodecs.VAR_INT.encode(buf, characteristic.sequence());
                        }
                    },
                    buf -> {
                        int size = ByteBufCodecs.VAR_INT.decode(buf);
                        ArrayList<Characteristic> chars = new ArrayList<>(size);
                        for (int i = 0; i < size; i++) {
                            String path = ByteBufCodecs.STRING_UTF8.decode(buf);
                            int stack = ByteBufCodecs.VAR_INT.decode(buf);
                            int seq = ByteBufCodecs.VAR_INT.decode(buf);
                            chars.add(new Characteristic(path, stack, seq));
                        }
                        return chars;
                    }
            );


    public static final StreamCodec<FriendlyByteBuf, SyncBeyonderDataPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        ByteBufCodecs.STRING_UTF8.encode(buf, packet.pathway());
                        ByteBufCodecs.VAR_INT.encode(buf, packet.sequence());
                        ByteBufCodecs.FLOAT.encode(buf, packet.spirituality());
                        ByteBufCodecs.BOOL.encode(buf, packet.griefingEnabled());
                        ByteBufCodecs.FLOAT.encode(buf, packet.digestionProgress());
                        PATHWAY_HISTORY_CODEC.encode(buf, packet.pathwayHistory());
                        CHAR_LIST_CODEC.encode(buf, packet.charList());
                    },
                    buf -> new SyncBeyonderDataPacket(
                            ByteBufCodecs.STRING_UTF8.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.FLOAT.decode(buf),
                            ByteBufCodecs.BOOL.decode(buf),
                            ByteBufCodecs.FLOAT.decode(buf),
                            PATHWAY_HISTORY_CODEC.decode(buf),
                            CHAR_LIST_CODEC.decode(buf)
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncBeyonderDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientBeyonderCache.updateData(
                    context.player().getUUID(),
                    packet.pathway(),
                    packet.sequence(),
                    packet.spirituality(),
                    packet.griefingEnabled(),
                    true,
                    packet.digestionProgress(),
                    packet.pathwayHistory(),
                    packet.charList()
            );
        });
    }
}