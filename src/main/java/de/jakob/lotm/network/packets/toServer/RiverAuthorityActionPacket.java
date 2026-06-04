package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.events.DeathImprintHandler;
import de.jakob.lotm.sefirah.SefirahHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Client → Server: player performed an action in the River Authority GUI.
 *
 * actionType:
 *   0 = Rivers Call  (requires imprint tier 3)
 *   1 = Locate       (requires imprint tier 2)
 */
public record RiverAuthorityActionPacket(int actionType, UUID targetUUID)
        implements CustomPacketPayload {

    public static final Type<RiverAuthorityActionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "river_authority_action"));

    public static final StreamCodec<ByteBuf, RiverAuthorityActionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, RiverAuthorityActionPacket::actionType,
                    ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), RiverAuthorityActionPacket::targetUUID,
                    RiverAuthorityActionPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RiverAuthorityActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer player)) return;

            // Security: only the river owner can use these actions
            if (!"river_of_eternal_darkness".equals(SefirahHandler.getClaimedSefirot(player))) return;

            switch (packet.actionType()) {
                case 0 -> DeathImprintHandler.executeRiversCall(player, packet.targetUUID());
                case 1 -> DeathImprintHandler.executeLocate(player, packet.targetUUID());
            }
        });
    }
}
