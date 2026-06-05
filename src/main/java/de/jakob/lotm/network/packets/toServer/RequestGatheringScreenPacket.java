package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.GatheringData;
import de.jakob.lotm.events.HonorificNamesEventHandler;
import de.jakob.lotm.gui.custom.Gathering.GatheringMenu;
import de.jakob.lotm.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.PendingPrayer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Client → Server: the Sefirah Castle owner requests to open the Gatherings GUI.
 */
public record RequestGatheringScreenPacket() implements CustomPacketPayload {

    public static final Type<RequestGatheringScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_gathering_screen"));

    public static final StreamCodec<ByteBuf, RequestGatheringScreenPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestGatheringScreenPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RequestGatheringScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer player)) return;

            // Only the sefirah_castle owner may open this
            if (!"sefirah_castle".equals(SefirahHandler.getClaimedSefirot(player))) return;

            GatheringData data = GatheringData.get(player.server);
            Set<UUID> members = data.getMembers(player.getUUID());

            // Collect all players who have prayed to this player
            LinkedList<PendingPrayer> prayers = HonorificNamesEventHandler.getPendingPrayers(player.getUUID());

            // Build entry list: prayers + members not already in the prayer list
            List<GatheringMenu.GatheringEntry> entries = new ArrayList<>();
            List<UUID> seenUUIDs = new ArrayList<>();

            for (PendingPrayer prayer : prayers) {
                if (!seenUUIDs.contains(prayer.senderUUID())) {
                    entries.add(new GatheringMenu.GatheringEntry(
                            prayer.senderUUID(),
                            prayer.senderName(),
                            prayer.senderPathway(),
                            prayer.senderSequence()
                    ));
                    seenUUIDs.add(prayer.senderUUID());
                }
            }

            // Also include existing members even if they haven't prayed recently
            for (UUID memberUUID : members) {
                if (!seenUUIDs.contains(memberUUID)) {
                    // Try to get live data from online player, otherwise show UUID-only entry
                    ServerPlayer memberPlayer = player.server.getPlayerList().getPlayer(memberUUID);
                    String name = memberPlayer != null
                            ? memberPlayer.getName().getString()
                            : memberUUID.toString().substring(0, 8) + "...";
                    String pathway = memberPlayer != null ? BeyonderData.getPathway(memberPlayer) : "none";
                    int seq = memberPlayer != null ? BeyonderData.getSequence(memberPlayer) : 10;
                    entries.add(new GatheringMenu.GatheringEntry(memberUUID, name, pathway, seq));
                    seenUUIDs.add(memberUUID);
                }
            }

            boolean gatheringActive = !GatheringData.getAllGathered().isEmpty();
            List<UUID> memberList = new ArrayList<>(members);

            player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new GatheringMenu(id, inv, entries, memberList, gatheringActive),
                    Component.literal("Gatherings")
            ), buf -> GatheringMenu.writeToBuffer(buf, entries, memberList, gatheringActive));
        });
    }
}
