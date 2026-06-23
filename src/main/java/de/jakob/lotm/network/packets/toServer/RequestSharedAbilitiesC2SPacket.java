package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TeamComponent;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSharedAbilitiesDataS2CPacket;
import de.jakob.lotm.util.helper.TeamUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Sent by the client when the Introspect screen opens, to request a sync of team/shared abilities data.
 */
public record RequestSharedAbilitiesC2SPacket() implements CustomPacketPayload {

    public static final Type<RequestSharedAbilitiesC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_shared_abilities"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestSharedAbilitiesC2SPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestSharedAbilitiesC2SPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestSharedAbilitiesC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            TeamComponent team = player.getData(ModAttachments.TEAM_COMPONENT.get());

            // Sync from leader perspective if they have members
            if (team.memberCount() > 0) {
                TeamUtils.syncToTeam(player);
                return;
            }

            // Sync from member perspective if they belong to a team and leader is online
            if (team.isInTeam() && player.getServer() != null) {
                ServerPlayer leader = player.getServer().getPlayerList().getPlayer(
                        java.util.UUID.fromString(team.leaderUUID()));
                if (leader != null) {
                    TeamUtils.syncToTeam(leader);
                    return;
                }
            }

            // No active team or leader offline — tell the client to clear team data
            PacketHandler.sendToPlayer(player, new SyncSharedAbilitiesDataS2CPacket(
                    "", new ArrayList<>(), new ArrayList<>(), new HashMap<>(), 0, 0));
        });
    }
}
