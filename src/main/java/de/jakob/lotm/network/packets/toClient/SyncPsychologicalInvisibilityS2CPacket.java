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

public record SyncPsychologicalInvisibilityS2CPacket(Map<UUID, Integer> data)
        implements CustomPacketPayload {

    public static final Type<SyncPsychologicalInvisibilityS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_psychological_invisibility"));

    public static final StreamCodec<FriendlyByteBuf, SyncPsychologicalInvisibilityS2CPacket> STREAM_CODEC =
            StreamCodec.of(
                    SyncPsychologicalInvisibilityS2CPacket::write,
                    SyncPsychologicalInvisibilityS2CPacket::read
            );

    private static SyncPsychologicalInvisibilityS2CPacket read(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<UUID, Integer> map = new HashMap<>();

        for (int i = 0; i < size; i++) {
            map.put(buf.readUUID(), buf.readInt());
        }

        return new SyncPsychologicalInvisibilityS2CPacket(map);
    }

    private static void write(FriendlyByteBuf buf, SyncPsychologicalInvisibilityS2CPacket packet) {
        buf.writeInt(packet.data.size());

        for (var entry : packet.data.entrySet()) {
            buf.writeUUID(entry.getKey());
            buf.writeInt(entry.getValue());
        }
    }

    @Override
    public Type<SyncPsychologicalInvisibilityS2CPacket> type() {
        return TYPE;
    }

    public static void handle(SyncPsychologicalInvisibilityS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            PsychologicalInvisibilityAbility.invisiblePlayersClient = new HashMap<>();
            PsychologicalInvisibilityAbility.invisiblePlayersClient = (HashMap<UUID, Integer>) packet.data;
        });
    }
}
