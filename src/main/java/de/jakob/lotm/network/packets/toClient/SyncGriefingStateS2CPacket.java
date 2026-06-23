package de.jakob.lotm.network.packets.toClient;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncGriefingStateS2CPacket(boolean griefingEnabled) implements CustomPacketPayload {
    public static final Type<SyncGriefingStateS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_griefing_state"));

    public static final StreamCodec<FriendlyByteBuf, SyncGriefingStateS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncGriefingStateS2CPacket::griefingEnabled,
                    SyncGriefingStateS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncGriefingStateS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            BeyonderData.setGriefingEnabled(context.player(), packet.griefingEnabled());
        });
    }
}