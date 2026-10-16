package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.data.ClientRecognitionCache;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RecognitionSyncPacket(boolean active, boolean hasTarget, int entityId,
                                    String name, String pathway, int sequence)
        implements CustomPacketPayload {

    public static final Type<RecognitionSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "recognition_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RecognitionSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, RecognitionSyncPacket::active,
                    ByteBufCodecs.BOOL, RecognitionSyncPacket::hasTarget,
                    ByteBufCodecs.VAR_INT, RecognitionSyncPacket::entityId,
                    ByteBufCodecs.STRING_UTF8, RecognitionSyncPacket::name,
                    ByteBufCodecs.STRING_UTF8, RecognitionSyncPacket::pathway,
                    ByteBufCodecs.VAR_INT, RecognitionSyncPacket::sequence,
                    RecognitionSyncPacket::new
            );

    public static RecognitionSyncPacket off() {
        return new RecognitionSyncPacket(false, false, -1, "", "", 0);
    }

    public static RecognitionSyncPacket noTarget() {
        return new RecognitionSyncPacket(true, false, -1, "", "", 0);
    }

    public static RecognitionSyncPacket target(int entityId, String name, String pathway, int sequence) {
        return new RecognitionSyncPacket(true, true, entityId, name, pathway, sequence);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RecognitionSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientRecognitionCache.update(packet));
    }
}