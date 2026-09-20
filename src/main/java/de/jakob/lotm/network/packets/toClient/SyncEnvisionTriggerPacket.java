package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → Client: syncs the sender's currently registered LEODERO trigger word.
 * Sent on login and whenever the trigger is changed.
 * Empty string means no trigger is set.
 */
public record SyncEnvisionTriggerPacket(String word) implements CustomPacketPayload {

    public static final Type<SyncEnvisionTriggerPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_envision_trigger"));

    public static final StreamCodec<FriendlyByteBuf, SyncEnvisionTriggerPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeUtf(pkt.word()),
                    buf -> new SyncEnvisionTriggerPacket(buf.readUtf())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    /** Cached on the client so {@link de.jakob.lotm.gui.custom.ChaosSeaAuthority.TargetBlasphemyScreen} can pre-fill the box. */
    public static volatile String CLIENT_WORD = "";

    public static void handle(SyncEnvisionTriggerPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> CLIENT_WORD = packet.word());
    }
}
