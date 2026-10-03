package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.gui.custom.marionettes.MarionetteMenu;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncMarionettesToClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record RequestMarionetteSyncPacket(List<Integer> entityIds) implements CustomPacketPayload {

    public static final Type<RequestMarionetteSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_marionette_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestMarionetteSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT.apply(ByteBufCodecs.list()),
                    RequestMarionetteSyncPacket::entityIds,
                    RequestMarionetteSyncPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestMarionetteSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            if (!(serverPlayer.containerMenu instanceof MarionetteMenu menu)) return; // not this menu -> ignore

            ServerLevel level = serverPlayer.serverLevel();

            List<MarionetteEntry> marionetteEntries = packet.entityIds().stream()
                    .map(level::getEntity)
                    .filter(e -> e instanceof LivingEntity)
                    .map(e -> (LivingEntity) e)
                    .map(living -> {
                        var data = living.getData(ModAttachments.MARIONETTE_COMPONENT);
                        return new MarionetteEntry(living.getId(), data.getCurrentMode().ordinal(),
                                data.shouldAttack(), data.isMarionette());
                    }).toList();

            PacketHandler.sendToPlayer(serverPlayer, new SyncMarionettesToClient(menu.containerId, marionetteEntries));
        });
    }

    public record MarionetteEntry(int entityId, int modeOrdinal, boolean shouldAttack, boolean isMarionette) { }
}