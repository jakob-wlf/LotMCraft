package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.data.ClientLuckPerceptionCache;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record LuckPerceptionPacket(boolean active, double selfLuck, List<Entry> entries)
        implements CustomPacketPayload {

    public record Entry(int entityId, double luck) {}

    public static final Type<LuckPerceptionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "luck_perception"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LuckPerceptionPacket> STREAM_CODEC =
            StreamCodec.of(LuckPerceptionPacket::write, LuckPerceptionPacket::read);

    public static LuckPerceptionPacket inactive() {
        return new LuckPerceptionPacket(false, 0, List.of());
    }

    private static void write(RegistryFriendlyByteBuf buf, LuckPerceptionPacket p) {
        buf.writeBoolean(p.active);
        buf.writeDouble(p.selfLuck);
        buf.writeVarInt(p.entries.size());
        for (Entry e : p.entries) {
            buf.writeVarInt(e.entityId());
            buf.writeDouble(e.luck());
        }
    }

    private static LuckPerceptionPacket read(RegistryFriendlyByteBuf buf) {
        boolean active = buf.readBoolean();
        double self = buf.readDouble();
        int n = buf.readVarInt();
        List<Entry> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(new Entry(buf.readVarInt(), buf.readDouble()));
        }
        return new LuckPerceptionPacket(active, self, list);
    }

    public static void handle(LuckPerceptionPacket p, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (p.active) ClientLuckPerceptionCache.update(p.selfLuck, p.entries);
            else ClientLuckPerceptionCache.clear();
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}