package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.visionary.PsychologicalInvisibilityAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record SyncPsychologicalInvisibilityPacket(Map<UUID, Integer> data)
        implements CustomPacketPayload {

    public static final Type<SyncPsychologicalInvisibilityPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_psychological_invisibility"));

    public static final StreamCodec<FriendlyByteBuf, SyncPsychologicalInvisibilityPacket> STREAM_CODEC =
            StreamCodec.of(
                    SyncPsychologicalInvisibilityPacket::write,
                    SyncPsychologicalInvisibilityPacket::read
            );

    private static SyncPsychologicalInvisibilityPacket read(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<UUID, Integer> map = new HashMap<>();

        for (int i = 0; i < size; i++) {
            map.put(buf.readUUID(), buf.readInt());
        }

        return new SyncPsychologicalInvisibilityPacket(map);
    }

    private static void write(FriendlyByteBuf buf, SyncPsychologicalInvisibilityPacket packet) {
        buf.writeInt(packet.data.size());

        for (var entry : packet.data.entrySet()) {
            buf.writeUUID(entry.getKey());
            buf.writeInt(entry.getValue());
        }
    }

    @Override
    public Type<SyncPsychologicalInvisibilityPacket> type() {
        return TYPE;
    }

    public static void handle(SyncPsychologicalInvisibilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            PsychologicalInvisibilityAbility.invisiblePlayersClient = new HashMap<>();
            PsychologicalInvisibilityAbility.invisiblePlayersClient = (HashMap<UUID, Integer>) packet.data;
        });
    }
}
