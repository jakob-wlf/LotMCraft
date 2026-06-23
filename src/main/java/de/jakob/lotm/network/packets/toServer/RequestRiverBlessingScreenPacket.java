package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.events.HonorificNamesEventHandler;
import de.jakob.lotm.gui.custom.RiverBlessing.RiverBlessingMenu;
import de.jakob.lotm.sefirah.RiverBlessingManager;
import de.jakob.lotm.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Client → Server: River owner requests to open the Blessings GUI.
 */
public record RequestRiverBlessingScreenPacket() implements CustomPacketPayload {

    public static final Type<RequestRiverBlessingScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID,
                    "request_river_blessing_screen"));

    public static final StreamCodec<ByteBuf, RequestRiverBlessingScreenPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestRiverBlessingScreenPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RequestRiverBlessingScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer player)) return;

            // Security: only the river owner may open this screen
            if (!"river_of_eternal_darkness".equals(SefirahHandler.getClaimedSefirot(player))) return;

            // Build the prayer list (players who have prayed to this River owner)
            List<RiverBlessingMenu.BlessingEntry> prayers = HonorificNamesEventHandler
                    .getPendingPrayers(player.getUUID())
                    .stream()
                    .filter(p -> {
                        // Deduplicate by UUID — keep only the first entry per sender
                        return true;
                    })
                    .map(p -> new RiverBlessingMenu.BlessingEntry(
                            p.senderUUID(),
                            p.senderName(),
                            p.senderPathway(),
                            p.senderSequence()))
                    .collect(Collectors.toList());

            // Deduplicate entries by UUID (a player may have prayed multiple times)
            List<UUID> seen = new ArrayList<>();
            List<RiverBlessingMenu.BlessingEntry> deduped = new ArrayList<>();
            for (RiverBlessingMenu.BlessingEntry e : prayers) {
                if (!seen.contains(e.uuid())) {
                    seen.add(e.uuid());
                    deduped.add(e);
                }
            }

            // Also include players who are already blessed but may no longer appear in the prayer list,
            // so the owner can still remove the blessing from the GUI.
            Set<UUID> alreadyBlessed = RiverBlessingManager.getBlessedByOwner(player.getUUID());
            for (UUID blessedUUID : alreadyBlessed) {
                if (!seen.contains(blessedUUID)) {
                    // Try to look up current data from the server
                    ServerPlayer blessedPlayer = player.getServer().getPlayerList().getPlayer(blessedUUID);
                    String name = blessedPlayer != null ? blessedPlayer.getName().getString() : blessedUUID.toString();
                    String path = blessedPlayer != null ? BeyonderData.getPathway(blessedPlayer) : "unknown";
                    int seq = blessedPlayer != null ? BeyonderData.getSequence(blessedPlayer) : 9;
                    deduped.add(new RiverBlessingMenu.BlessingEntry(blessedUUID, name, path, seq));
                    seen.add(blessedUUID);
                }
            }

            List<UUID> blessed = new ArrayList<>(alreadyBlessed);
            int maxBlessings = RiverBlessingManager.getMaxBlessings(BeyonderData.getSequence(player));

            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new RiverBlessingMenu(id, inv, deduped, blessed, maxBlessings),
                            Component.literal("River Blessings")),
                    buf -> RiverBlessingMenu.writeToBuffer(buf, deduped, blessed, maxBlessings));
        });
    }
}
