package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SharedAbilitiesComponent;
import de.jakob.lotm.attachments.TeamComponent;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UseSharedAbilityC2SPacket(String abilityId) implements CustomPacketPayload {

    public static final Type<UseSharedAbilityC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "use_shared_ability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UseSharedAbilityC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    UseSharedAbilityC2SPacket::abilityId,
                    UseSharedAbilityC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UseSharedAbilityC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            String abilityId = packet.abilityId();
            Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
            if (ability == null) return;
            if (!(serverPlayer.level() instanceof ServerLevel serverLevel)) return;
            if (serverPlayer.getServer() == null) return;

            TeamComponent team = serverPlayer.getData(ModAttachments.TEAM_COMPONENT.get());

            if (team.isInTeam()) {
                // Member: deny if leader is offline
                ServerPlayer leader = serverPlayer.getServer().getPlayerList().getPlayer(
                        java.util.UUID.fromString(team.leaderUUID()));
                if (leader == null) return;
            } else if (team.memberCount() > 0) {
                // Leader: deny if the member who contributed this ability is offline
                if (!isContributingMemberOnline(serverPlayer, abilityId)) return;
            }

            // Server-side sequence gate: player must be at least as strong as the ability requires
            int playerSeq = BeyonderData.getSequence(serverPlayer);
            int reqSeq = ability.lowestSequenceUsable();
            if (reqSeq >= 0 && playerSeq > reqSeq) return;

            boolean isShared = isSharedAbility(serverPlayer, abilityId);
            ability.useAbility(serverLevel, serverPlayer, true, !isShared, true);
        });
    }

    /** Returns true if the member who contributed this ability to the leader's team is currently online. */
    private static boolean isContributingMemberOnline(ServerPlayer leader, String abilityId) {
        TeamComponent team = leader.getData(ModAttachments.TEAM_COMPONENT.get());
        for (String memberUUID : team.memberUUIDs()) {
            ServerPlayer member = leader.getServer().getPlayerList().getPlayer(
                    java.util.UUID.fromString(memberUUID));
            if (member == null) continue;
            SharedAbilitiesComponent shared = member.getData(ModAttachments.SHARED_ABILITIES_COMPONENT.get());
            if (shared.getContributions(leader.getStringUUID()).contains(abilityId)) return true;
        }
        return false;
    }

    private static boolean isSharedAbility(ServerPlayer player, String abilityId) {
        TeamComponent team = player.getData(ModAttachments.TEAM_COMPONENT.get());
        if (player.getServer() == null) return false;

        if (!team.isInTeam()) {
            // Leader: check all online members' contributions keyed by own UUID
            for (String memberUUID : team.memberUUIDs()) {
                ServerPlayer member = player.getServer().getPlayerList().getPlayer(
                        java.util.UUID.fromString(memberUUID));
                if (member == null) continue;
                SharedAbilitiesComponent shared = member.getData(ModAttachments.SHARED_ABILITIES_COMPONENT.get());
                if (shared.getContributions(player.getStringUUID()).contains(abilityId)) return true;
            }
            return false;
        }

        // Member: check all members' contributions (including self) and leader's own contributions
        ServerPlayer leader = player.getServer().getPlayerList().getPlayer(
                java.util.UUID.fromString(team.leaderUUID()));
        if (leader == null) return false;

        // Check leader's own contributions
        SharedAbilitiesComponent leaderShared = leader.getData(ModAttachments.SHARED_ABILITIES_COMPONENT.get());
        if (leaderShared.getContributions(leader.getStringUUID()).contains(abilityId)) return true;

        // Check all members' contributions (including this player)
        TeamComponent leaderTeam = leader.getData(ModAttachments.TEAM_COMPONENT.get());
        for (String memberUUID : leaderTeam.memberUUIDs()) {
            ServerPlayer member = player.getServer().getPlayerList().getPlayer(
                    java.util.UUID.fromString(memberUUID));
            if (member == null) continue;
            SharedAbilitiesComponent shared = member.getData(ModAttachments.SHARED_ABILITIES_COMPONENT.get());
            if (shared.getContributions(team.leaderUUID()).contains(abilityId)) return true;
        }
        return false;
    }
}
