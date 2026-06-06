package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.DeathImprintData;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenAbilitySealScreenPacket;
import de.jakob.lotm.sefirah.SefirahHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Client → Server: River owner requests the ability-seal selector for the given imprinted player.
 * The server responds with {@link OpenAbilitySealScreenPacket}.
 */
public record RequestAbilitySealScreenPacket(String targetUUIDStr) implements CustomPacketPayload {

    public static final Type<RequestAbilitySealScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_ability_seal_screen"));

    public static final StreamCodec<ByteBuf, RequestAbilitySealScreenPacket> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(RequestAbilitySealScreenPacket::new, RequestAbilitySealScreenPacket::targetUUIDStr);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RequestAbilitySealScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer owner)) return;

            // Security: only the river owner
            if (!"river_of_eternal_darkness".equals(SefirahHandler.getClaimedSefirot(owner))) return;

            UUID targetUUID;
            try {
                targetUUID = UUID.fromString(packet.targetUUIDStr());
            } catch (IllegalArgumentException ignored) {
                return;
            }

            DeathImprintData data = DeathImprintData.get(owner.server);

            // Security: target needs at least 2 imprints
            if (data.getImprintCount(targetUUID) < 2) return;

            // Prefer live player data so abilities always match current seq/pathway.
            // Fall back to snapshot when the target is offline.
            net.minecraft.server.level.ServerPlayer targetPlayer =
                    owner.server.getPlayerList().getPlayer(targetUUID);

            String pathway;
            int    sequence;
            String name;

            if (targetPlayer != null) {
                pathway  = BeyonderData.getPathway(targetPlayer);
                sequence = BeyonderData.getSequence(targetPlayer);
                name     = targetPlayer.getGameProfile().getName();
                // Keep snapshot fresh while we're here
                data.saveSnapshot(targetUUID, name, pathway, sequence);
            } else {
                pathway  = data.getSnapshotPathway(targetUUID);
                sequence = data.getSnapshotSequence(targetUUID);
                name     = data.getSnapshotName(targetUUID);
            }

            List<String> ids   = new ArrayList<>();
            List<String> names = new ArrayList<>();

            // All abilities the player currently has (seq >= their current seq means they've unlocked it)
            List<Ability> abilities = LOTMCraft.abilityHandler.getByPathwayAndSequenceOrderedBySequence(pathway, sequence);
            for (Ability ab : abilities) {
                ids.add(ab.getId());
                names.add(ab.getName().getString());
            }

            List<String> currentlySealed = data.getSealedAbilities(targetUUID);

            PacketHandler.sendToPlayer(owner, new OpenAbilitySealScreenPacket(
                    targetUUID.toString(), name, ids, names, currentlySealed));
        });
    }
}
