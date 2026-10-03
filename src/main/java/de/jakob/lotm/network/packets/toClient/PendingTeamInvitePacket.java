package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.TeamInviteManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Packet sent from server to client to notify of a pending team invite from a Red Priest leader.
 */
public record PendingTeamInvitePacket(UUID leaderUUID, String leaderName) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PendingTeamInvitePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "pending_team_invite"));

    public static final StreamCodec<ByteBuf, PendingTeamInvitePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
            PendingTeamInvitePacket::leaderUUID,
            ByteBufCodecs.STRING_UTF8,
            PendingTeamInvitePacket::leaderName,
            PendingTeamInvitePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PendingTeamInvitePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            TeamInviteManager.addPendingInvite(packet.leaderUUID(), packet.leaderName());
        });
    }
}
