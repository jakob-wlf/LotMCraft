package de.jakob.lotm.network.packets.toClient;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gamerule.ClientGameruleCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncPreLoadGamerulePacket(boolean preLoadEffectsEnabled) implements CustomPacketPayload {
    public static final Type<SyncPreLoadGamerulePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_preload_gamerule_state"));

    public static final StreamCodec<FriendlyByteBuf, SyncPreLoadGamerulePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncPreLoadGamerulePacket::preLoadEffectsEnabled,
                    SyncPreLoadGamerulePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPreLoadGamerulePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientGameruleCache.isPreLoadEffectsEnabled = packet.preLoadEffectsEnabled();
        });
    }
}