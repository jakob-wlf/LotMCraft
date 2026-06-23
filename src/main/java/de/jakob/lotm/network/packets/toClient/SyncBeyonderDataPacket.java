package de.jakob.lotm.network.packets.toClient;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.playerMap.Characteristic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import de.jakob.lotm.attachments.ReceivedBlessingComponent;
import java.util.ArrayList;
import java.util.List;

public record SyncBeyonderDataPacket(String pathway, int sequence, float spirituality, boolean griefingEnabled, float digestionProgress, String[] pathwayHistory, ArrayList<Characteristic> charList, List<ReceivedBlessingComponent.ReceivedBlessing> blessings) implements CustomPacketPayload {
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
                            ByteBufCodecs.VAR_INT.encode(buf, characteristic.getDisabledStacks());
                        }
                    },
                    buf -> {
                        int size = ByteBufCodecs.VAR_INT.decode(buf);
                        ArrayList<Characteristic> chars = new ArrayList<>(size);
                        for (int i = 0; i < size; i++) {
                            String path = ByteBufCodecs.STRING_UTF8.decode(buf);
                            int stack = ByteBufCodecs.VAR_INT.decode(buf);
                            int seq = ByteBufCodecs.VAR_INT.decode(buf);
                            int disabled = ByteBufCodecs.VAR_INT.decode(buf);
                            Characteristic characteristic = new Characteristic(path, stack, seq);
                            characteristic.setDisabledStacks(disabled);
                            chars.add(characteristic);
                        }
                        return chars;
                    }
            );

    private static final StreamCodec<FriendlyByteBuf, List<ReceivedBlessingComponent.ReceivedBlessing>> BLESSING_LIST_CODEC =
            StreamCodec.of(
                    (buf, blessings) -> {
                        ByteBufCodecs.VAR_INT.encode(buf, blessings.size());
                        for (ReceivedBlessingComponent.ReceivedBlessing b : blessings) {
                            ByteBufCodecs.STRING_UTF8.encode(buf, b.blessingId());
                            ByteBufCodecs.STRING_UTF8.encode(buf, b.pathway());
                            ByteBufCodecs.VAR_INT.encode(buf, b.sequence());
                            ByteBufCodecs.VAR_INT.encode(buf, b.ticksLeft());
                        }
                    },
                    buf -> {
                        int size = ByteBufCodecs.VAR_INT.decode(buf);
                        List<ReceivedBlessingComponent.ReceivedBlessing> blessings = new ArrayList<>(size);
                        for (int i = 0; i < size; i++) {
                            blessings.add(new ReceivedBlessingComponent.ReceivedBlessing(
                                    ByteBufCodecs.STRING_UTF8.decode(buf),
                                    ByteBufCodecs.STRING_UTF8.decode(buf),
                                    ByteBufCodecs.VAR_INT.decode(buf),
                                    ByteBufCodecs.VAR_INT.decode(buf)
                            ));
                        }
                        return blessings;
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
                        BLESSING_LIST_CODEC.encode(buf, packet.blessings());
                    },
                    buf -> new SyncBeyonderDataPacket(
                            ByteBufCodecs.STRING_UTF8.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.FLOAT.decode(buf),
                            ByteBufCodecs.BOOL.decode(buf),
                            ByteBufCodecs.FLOAT.decode(buf),
                            PATHWAY_HISTORY_CODEC.decode(buf),
                            CHAR_LIST_CODEC.decode(buf),
                            BLESSING_LIST_CODEC.decode(buf)
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncBeyonderDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleSyncBeyonderData(packet, context);
        });
    }
}