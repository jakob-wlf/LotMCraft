package de.jakob.lotm.network.packets;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncGriefingStatePacket(boolean griefingEnabled) implements CustomPacketPayload {
    public static final Type<SyncGriefingStatePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_griefing_state"));

    public static final StreamCodec<FriendlyByteBuf, SyncGriefingStatePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncGriefingStatePacket::griefingEnabled,
                    SyncGriefingStatePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncGriefingStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            BeyonderData.setGriefingEnabled(context.player(), packet.griefingEnabled());
        });
    }
}