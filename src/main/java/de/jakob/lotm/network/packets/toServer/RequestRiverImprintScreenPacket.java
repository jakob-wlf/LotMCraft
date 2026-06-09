package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DeathImprintData;
import de.jakob.lotm.gui.custom.RiverAuthority.RiverAuthorityMenu;
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

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Client → Server: the River Authority screen requests that the server open
 * the Death Imprint list (RiverAuthorityMenu) for the player.
 */
public record RequestRiverImprintScreenPacket() implements CustomPacketPayload {

    public static final Type<RequestRiverImprintScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID,
                    "request_river_imprint_screen"));

    public static final StreamCodec<ByteBuf, RequestRiverImprintScreenPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestRiverImprintScreenPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestRiverImprintScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer player)) return;

            // Security: only the river owner may open this screen
            if (!"river_of_eternal_darkness".equals(SefirahHandler.getClaimedSefirot(player))) return;

            DeathImprintData imprintData = DeathImprintData.get(player.getServer());
            Set<UUID> allImprinted = imprintData.getAllImprintedPlayers();
            final UUID ownerUUID = player.getUUID();
            List<RiverAuthorityMenu.ImprintEntry> entries = allImprinted.stream()
                    .filter(uuid -> !uuid.equals(ownerUUID))
                    .map(uuid -> {
                        ServerPlayer onlineTarget = player.getServer().getPlayerList().getPlayer(uuid);
                        boolean isOnline = onlineTarget != null;
                        String entryPathway = isOnline ? BeyonderData.getPathway(onlineTarget) : imprintData.getSnapshotPathway(uuid);
                        int entrySequence = isOnline ? BeyonderData.getSequence(onlineTarget) : imprintData.getSnapshotSequence(uuid);
                        return new RiverAuthorityMenu.ImprintEntry(
                                uuid,
                                imprintData.getSnapshotName(uuid),
                                entryPathway,
                                entrySequence,
                                imprintData.getImprintCount(uuid),
                                isOnline,
                                imprintData.getSealedAbilities(uuid)
                        );
                    })
                    .sorted(Comparator.comparingInt(RiverAuthorityMenu.ImprintEntry::imprintTier).reversed())
                    .collect(Collectors.toList());

            player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new RiverAuthorityMenu(id, inv, entries),
                    Component.literal("Death Imprints")
            ), buf -> RiverAuthorityMenu.writeEntries(buf, entries));
        });
    }
}
