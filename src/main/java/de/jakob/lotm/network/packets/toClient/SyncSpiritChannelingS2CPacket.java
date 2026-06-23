package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.death.SpiritChannelingAbility;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent to the client when the player's captured spirit changes (gained or released).
 * {@code spiritType} is the ordinal of {@link SpiritChannelingAbility.SpiritType},
 * or -1 if no spirit is currently held.
 */
public record SyncSpiritChannelingS2CPacket(int spiritType) implements CustomPacketPayload {

    public static final Type<SyncSpiritChannelingS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_spirit_channeling"));

    public static final StreamCodec<ByteBuf, SyncSpiritChannelingS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SyncSpiritChannelingS2CPacket::spiritType,
            SyncSpiritChannelingS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSpiritChannelingS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleSpiritChannelingPacket(packet);
            }
        });
    }
}
