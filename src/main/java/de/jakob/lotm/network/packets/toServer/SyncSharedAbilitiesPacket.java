package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.TeamUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;

/**
 * Sent by a team member when they update their contributed shared abilities.
 */
public record SyncSharedAbilitiesPacket(ArrayList<String> abilityIds) implements CustomPacketPayload {

    public static final Type<SyncSharedAbilitiesPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_shared_abilities"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSharedAbilitiesPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
                    SyncSharedAbilitiesPacket::abilityIds,
                    SyncSharedAbilitiesPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSharedAbilitiesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            TeamUtils.updateMemberContributions(player, packet.abilityIds());
        });
    }
}
