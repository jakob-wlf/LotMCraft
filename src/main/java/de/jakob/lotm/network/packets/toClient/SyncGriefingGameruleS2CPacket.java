package de.jakob.lotm.network.packets.toClient;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gamerule.ClientGameruleCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncGriefingGameruleS2CPacket(boolean griefingEnabled) implements CustomPacketPayload {
    public static final Type<SyncGriefingGameruleS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_griefing_gamerule_state"));

    public static final StreamCodec<FriendlyByteBuf, SyncGriefingGameruleS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncGriefingGameruleS2CPacket::griefingEnabled,
                    SyncGriefingGameruleS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncGriefingGameruleS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientGameruleCache.isGlobalGriefingEnabled = packet.griefingEnabled();
        });
    }
}