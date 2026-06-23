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
public record SyncSharedAbilitiesC2SPacket(ArrayList<String> abilityIds) implements CustomPacketPayload {

    public static final Type<SyncSharedAbilitiesC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_shared_abilities"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSharedAbilitiesC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
                    SyncSharedAbilitiesC2SPacket::abilityIds,
                    SyncSharedAbilitiesC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSharedAbilitiesC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            TeamUtils.updateMemberContributions(player, packet.abilityIds());
        });
    }
}
