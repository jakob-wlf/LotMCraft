package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.GatheringData;
import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Set;
import java.util.UUID;

/**
 * Client → Server: action taken inside the Gatherings GUI.
 *
 * Actions:
 *   MARK   (0) — add a player to the gathering member list
 *   UNMARK (1) — remove a player from the gathering member list
 *   CALL   (2) — teleport all members to sefirah castle
 *   END    (3) — return all currently gathered players
 */
public record GatheringActionPacket(int action, String targetUUIDStr) implements CustomPacketPayload {

    public static final int MARK   = 0;
    public static final int UNMARK = 1;
    public static final int CALL   = 2;
    public static final int END    = 3;

    public static final Type<GatheringActionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "gathering_action"));

    public static final StreamCodec<ByteBuf, GatheringActionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,    GatheringActionPacket::action,
                    ByteBufCodecs.STRING_UTF8, GatheringActionPacket::targetUUIDStr,
                    GatheringActionPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(GatheringActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer owner)) return;

            // Security: only sefirah_castle owner
            if (!"sefirah_castle".equals(SefirahHandler.getClaimedSefirot(owner))) return;

            GatheringData data = GatheringData.get(owner.server);

            switch (packet.action()) {
                case MARK -> {
                    UUID target = parseUUID(packet.targetUUIDStr());
                    if (target != null) data.addMember(owner.getUUID(), target);
                }
                case UNMARK -> {
                    UUID target = parseUUID(packet.targetUUIDStr());
                    if (target != null) {
                        data.removeMember(owner.getUUID(), target);
                        // If they're gathered, return them
                        ServerPlayer memberPlayer = owner.server.getPlayerList().getPlayer(target);
                        if (memberPlayer != null && GatheringData.isGathered(target)) {
                            GatheringData.returnPlayer(memberPlayer, owner.server);
                            memberPlayer.sendSystemMessage(
                                    net.minecraft.network.chat.Component.literal(
                                            "You have been removed from the gathering."));
                        }
                    }
                }
                case CALL -> {
                    // Teleport the owner to the head-of-table position.
                    // We do NOT use gatherOwner() here because that would save a GatheringData
                    // return location for the owner, which conflicts with the SefirotData
                    // isInSefirot / return-location tracking used by the U-key (teleportToSefirot).
                    // Instead we update SefirotData directly so the U-key always works correctly.
                    if (!GatheringData.isGathered(owner.getUUID())) {
                        ResourceKey<Level> castleDim = ResourceKey.create(Registries.DIMENSION,
                                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sefirah_castle"));
                        ServerLevel castleLevel = owner.server.getLevel(castleDim);
                        if (castleLevel != null) {
                            // If the owner is not already inside the castle, register the entry in
                            // SefirotData so pressing U later correctly returns them to the overworld.
                            SefirotData sefirotData = SefirotData.get(owner.server);
                            if (!owner.level().dimension().equals(castleDim) && !sefirotData.isInSefirot(owner)) {
                                sefirotData.setLastReturnLocation(owner);
                                sefirotData.setIsInSefirot(owner.getUUID(), true);
                            }
                            owner.teleportTo(castleLevel,
                                    GatheringData.OWNER_POSITION[0],
                                    GatheringData.OWNER_POSITION[1],
                                    GatheringData.OWNER_POSITION[2],
                                    180f, 0f);
                            GatheringData.markGathered(owner.getUUID());
                            owner.sendSystemMessage(
                                    net.minecraft.network.chat.Component.literal(
                                            "You have called a gathering in Sefirah Castle.")
                                            .withStyle(net.minecraft.ChatFormatting.GOLD));
                        }
                    }
                    // Teleport all beyonder members to chairs
                    Set<UUID> members = data.getMembers(owner.getUUID());
                    int slot = 0;
                    for (UUID memberUUID : members) {
                        if (memberUUID.equals(owner.getUUID())) continue;
                        ServerPlayer memberPlayer = owner.server.getPlayerList().getPlayer(memberUUID);
                        if (memberPlayer != null && !GatheringData.isGathered(memberUUID)) {
                            // Only gather players who are above the grey fog (actual beyonders)
                            if (!BeyonderData.isBeyonder(memberPlayer)) continue;
                            GatheringData.gatherPlayer(memberPlayer, slot, owner.server);
                            memberPlayer.sendSystemMessage(
                                    net.minecraft.network.chat.Component.literal(
                                            "You have been called to a gathering in Sefirah Castle.")
                                            .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
                            slot++;
                        }
                    }
                }
                case END -> {
                    // Return all gathered players.
                    // The owner is NOT returned — they are the castle lord and stay in the castle.
                    // Their SefirotData state (isInSefirot) was set correctly on CALL, so pressing
                    // U afterwards will still take them to the overworld.
                    Set<UUID> gathered = new java.util.HashSet<>(GatheringData.getAllGathered());
                    UUID ownerUUID = owner.getUUID();
                    for (UUID gatheredUUID : gathered) {
                        if (gatheredUUID.equals(ownerUUID)) {
                            // Owner stays in castle — just unmark them
                            GatheringData.unmarkGathered(ownerUUID);
                            continue;
                        }
                        ServerPlayer gatheredPlayer = owner.server.getPlayerList().getPlayer(gatheredUUID);
                        if (gatheredPlayer != null) {
                            GatheringData.returnPlayer(gatheredPlayer, owner.server);
                            gatheredPlayer.sendSystemMessage(
                                    net.minecraft.network.chat.Component.literal(
                                            "The gathering has ended. You have been returned."));
                        } else {
                            // Player offline — just unmark them and clear their return location
                            GatheringData.unmarkGathered(gatheredUUID);
                            data.clearReturnLocation(gatheredUUID);
                        }
                    }
                }
            }
        });
    }

    private static UUID parseUUID(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return UUID.fromString(s); } catch (IllegalArgumentException e) { return null; }
    }
}
