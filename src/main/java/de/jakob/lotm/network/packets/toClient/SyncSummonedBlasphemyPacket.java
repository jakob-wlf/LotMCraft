package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Server → Client: update the client's cached summoned-blasphemy-card state.
 * Contains a snapshot of the player's currently summoned cards, their use counts,
 * locked pathways, and per-pathway cooldown expiry times (epoch ms).
 */
public record SyncSummonedBlasphemyPacket(Map<String, Integer> cards, Set<String> locked, Map<String, Long> cooldowns)
        implements CustomPacketPayload {

    public static final Type<SyncSummonedBlasphemyPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_summoned_blasphemy"));

    public static final StreamCodec<FriendlyByteBuf, SyncSummonedBlasphemyPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeVarInt(pkt.cards().size());
                        pkt.cards().forEach((pathway, uses) -> {
                            buf.writeUtf(pathway);
                            buf.writeVarInt(uses);
                        });
                        buf.writeVarInt(pkt.locked().size());
                        pkt.locked().forEach(buf::writeUtf);
                        buf.writeVarInt(pkt.cooldowns().size());
                        pkt.cooldowns().forEach((pathway, expiry) -> {
                            buf.writeUtf(pathway);
                            buf.writeLong(expiry);
                        });
                    },
                    buf -> {
                        int size = buf.readVarInt();
                        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
                        for (int i = 0; i < size; i++) {
                            String pathway = buf.readUtf();
                            int uses = buf.readVarInt();
                            map.put(pathway, uses);
                        }
                        int lockedSize = buf.readVarInt();
                        LinkedHashSet<String> locked = new LinkedHashSet<>();
                        for (int i = 0; i < lockedSize; i++) locked.add(buf.readUtf());
                        int cdSize = buf.readVarInt();
                        Map<String, Long> cooldowns = new HashMap<>();
                        for (int i = 0; i < cdSize; i++) {
                            String pathway = buf.readUtf();
                            long expiry = buf.readLong();
                            cooldowns.put(pathway, expiry);
                        }
                        return new SyncSummonedBlasphemyPacket(map, locked, cooldowns);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    // ── Client-side cache (safe to hold on server – just never populated there) ──
    /** Latest summoned card state for the local player. Updated by handle(). */
    public static volatile Map<String, Integer> CLIENT_CACHE = Collections.emptyMap();
    /** Locked card pathways for the local player (cannot be dismissed via GUI). Updated by handle(). */
    public static volatile Set<String> CLIENT_LOCKED = Collections.emptySet();
    /** pathway → expiry epoch ms for pathways on cooldown. Updated by handle(). */
    public static volatile Map<String, Long> CLIENT_COOLDOWNS = Collections.emptyMap();

    public static void handle(SyncSummonedBlasphemyPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            CLIENT_CACHE     = Collections.unmodifiableMap(packet.cards());
            CLIENT_LOCKED    = Collections.unmodifiableSet(packet.locked());
            CLIENT_COOLDOWNS = Collections.unmodifiableMap(packet.cooldowns());
        });
    }
}
