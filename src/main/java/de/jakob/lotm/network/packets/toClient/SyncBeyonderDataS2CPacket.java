package de.jakob.lotm.network.packets.toClient;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncBeyonderDataS2CPacket(String pathway, int sequence, float spirituality, boolean griefingEnabled, float digestionProgress, String[] pathwayHistory, int[] charStacks, int cowardWormAmount) implements CustomPacketPayload {
    public static final Type<SyncBeyonderDataS2CPacket> TYPE =
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

    private static final StreamCodec<FriendlyByteBuf, int[]> CHAR_STACKS_CODEC =
            StreamCodec.of(
                    (buf, stacks) -> { for (int i = 0; i < 10; i++) ByteBufCodecs.VAR_INT.encode(buf, i < stacks.length ? stacks[i] : 0); },
                    buf -> { int[] stacks = new int[10]; for (int i = 0; i < 10; i++) stacks[i] = ByteBufCodecs.VAR_INT.decode(buf); return stacks; }
            );

    public static final StreamCodec<FriendlyByteBuf, SyncBeyonderDataS2CPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        ByteBufCodecs.STRING_UTF8.encode(buf, packet.pathway());
                        ByteBufCodecs.VAR_INT.encode(buf, packet.sequence());
                        ByteBufCodecs.FLOAT.encode(buf, packet.spirituality());
                        ByteBufCodecs.BOOL.encode(buf, packet.griefingEnabled());
                        ByteBufCodecs.FLOAT.encode(buf, packet.digestionProgress());
                        PATHWAY_HISTORY_CODEC.encode(buf, packet.pathwayHistory());
                        CHAR_STACKS_CODEC.encode(buf, packet.charStacks());
                        ByteBufCodecs.VAR_INT.encode(buf, packet.cowardWormAmount);
                    },
                    buf -> new SyncBeyonderDataS2CPacket(
                            ByteBufCodecs.STRING_UTF8.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.FLOAT.decode(buf),
                            ByteBufCodecs.BOOL.decode(buf),
                            ByteBufCodecs.FLOAT.decode(buf),
                            PATHWAY_HISTORY_CODEC.decode(buf),
                            CHAR_STACKS_CODEC.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf)
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncBeyonderDataS2CPacket packet, IPayloadContext context) {
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
                    packet.charStacks(),
                    packet.cowardWormAmount()
            );
        });
    }
}