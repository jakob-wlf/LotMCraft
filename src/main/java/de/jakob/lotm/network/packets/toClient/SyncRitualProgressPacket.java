package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.ClientRitualProgressCache;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncRitualProgressPacket(int current, int total, String label, boolean timed, boolean active)
        implements CustomPacketPayload {
    public static final Type<SyncRitualProgressPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_ritual_progress"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncRitualProgressPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, SyncRitualProgressPacket::current,
                    ByteBufCodecs.INT, SyncRitualProgressPacket::total,
                    ByteBufCodecs.STRING_UTF8, SyncRitualProgressPacket::label,
                    ByteBufCodecs.BOOL, SyncRitualProgressPacket::timed,
                    ByteBufCodecs.BOOL, SyncRitualProgressPacket::active,
                    SyncRitualProgressPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncRitualProgressPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientRitualProgressCache.update(
                packet.current(), packet.total(), packet.label(), packet.timed(), packet.active()));
    }
}