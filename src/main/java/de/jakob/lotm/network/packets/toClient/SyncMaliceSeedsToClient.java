package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.abyss.MaliceSeedAbility.SeedInfo;
import de.jakob.lotm.gui.custom.malice_seed.MaliceSeedMenu;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

public record SyncMaliceSeedsToClient(int containerId, List<SeedInfo> seeds) implements CustomPacketPayload {
    public static final Type<SyncMaliceSeedsToClient> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_malice_seeds_to_client"));
    private static final StreamCodec<ByteBuf, SeedInfo> ENTRY_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), SeedInfo::hostUUID,
            ByteBufCodecs.STRING_UTF8, SeedInfo::hostName,
            ByteBufCodecs.VAR_INT, SeedInfo::phase,
            ByteBufCodecs.VAR_INT, SeedInfo::progressPercent,
            ByteBufCodecs.STRING_UTF8, SeedInfo::dimension,
            ByteBufCodecs.BOOL, SeedInfo::isPlayer,
            SeedInfo::new
    );
    public static final StreamCodec<ByteBuf, SyncMaliceSeedsToClient> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncMaliceSeedsToClient::containerId,
            ENTRY_CODEC.apply(ByteBufCodecs.list()), SyncMaliceSeedsToClient::seeds,
            SyncMaliceSeedsToClient::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {return TYPE;}
    public static void handle(SyncMaliceSeedsToClient packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = ClientHandler.getPlayer();
            if (player != null && player.containerMenu.containerId == packet.containerId() && player.containerMenu instanceof MaliceSeedMenu menu) {menu.applySync(packet.seeds());}
        });
    }
}