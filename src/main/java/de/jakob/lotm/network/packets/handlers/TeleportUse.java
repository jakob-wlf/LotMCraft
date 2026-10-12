package de.jakob.lotm.network.packets.handlers;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;

public enum TeleportUse {
    TELEPORTATION,
    ENVISION_LOCATION;

    public static final StreamCodec<ByteBuf, TeleportUse> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(TeleportUse::fromId, TeleportUse::ordinal);

    @Nullable
    public static TeleportUse fromId(int id) {
        TeleportUse[] values = values();
        return id >= 0 && id < values.length ? values[id] : null;
    }
}