package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.abyss.MaliceSeedAbility;
import de.jakob.lotm.gui.custom.malice_seed.MaliceSeedMenu;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncMaliceSeedsToClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestMaliceSeedSyncPacket() implements CustomPacketPayload {
    public static final Type<RequestMaliceSeedSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_malice_seed_sync"));
    public static final StreamCodec<ByteBuf, RequestMaliceSeedSyncPacket> STREAM_CODEC = StreamCodec.unit(new RequestMaliceSeedSyncPacket());
    @Override
    public Type<? extends CustomPacketPayload> type() {return TYPE;}
    public static void handle(RequestMaliceSeedSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof MaliceSeedMenu menu)) return;
            var seeds = MaliceSeedAbility.collectSeeds(player);
            PacketHandler.sendToPlayer(player, new SyncMaliceSeedsToClient(menu.containerId, seeds));
        });
    }
}