package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record SyncSealPayload(
        boolean sealed,
        double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ
) implements CustomPacketPayload {

    public static final Type<SyncSealPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_seal"));

    public static final StreamCodec<FriendlyByteBuf, SyncSealPayload> STREAM_CODEC =
            NeoForgeStreamCodecs.composite(
                    ByteBufCodecs.BOOL,   SyncSealPayload::sealed,
                    ByteBufCodecs.DOUBLE, SyncSealPayload::minX,
                    ByteBufCodecs.DOUBLE, SyncSealPayload::minY,
                    ByteBufCodecs.DOUBLE, SyncSealPayload::minZ,
                    ByteBufCodecs.DOUBLE, SyncSealPayload::maxX,
                    ByteBufCodecs.DOUBLE, SyncSealPayload::maxY,
                    ByteBufCodecs.DOUBLE, SyncSealPayload::maxZ,
                    SyncSealPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static SyncSealPayload cleared() {
        return new SyncSealPayload(false, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }

    public static SyncSealPayload forBounds(
            boolean sealed,
            double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ
    ) {
        return new SyncSealPayload(sealed, minX, minY, minZ, maxX, maxY, maxZ);
    }
}