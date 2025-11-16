package de.jakob.lotm.network.packets;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gamerule.ClientGameruleCache;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncGriefingGamerulePacket(boolean griefingEnabled) implements CustomPacketPayload {
    public static final Type<SyncGriefingGamerulePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_griefing_gamerule_state"));

    public static final StreamCodec<FriendlyByteBuf, SyncGriefingGamerulePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncGriefingGamerulePacket::griefingEnabled,
                    SyncGriefingGamerulePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncGriefingGamerulePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientGameruleCache.isGlobalGriefingEnabled = packet.griefingEnabled();
        });
    }
}