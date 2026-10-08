package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.PendingTeleportRequests;
import de.jakob.lotm.network.packets.handlers.TeleportUse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TeleportToCoordinatesPacket(TeleportUse use, int x, int y, int z) implements CustomPacketPayload {
    public static final Type<TeleportToCoordinatesPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "teleport_to_coordinates"));

    public static final StreamCodec<FriendlyByteBuf, TeleportToCoordinatesPacket> STREAM_CODEC =
            StreamCodec.composite(
                    TeleportUse.STREAM_CODEC, TeleportToCoordinatesPacket::use,
                    ByteBufCodecs.INT, TeleportToCoordinatesPacket::x,
                    ByteBufCodecs.INT, TeleportToCoordinatesPacket::y,
                    ByteBufCodecs.INT, TeleportToCoordinatesPacket::z,
                    TeleportToCoordinatesPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TeleportToCoordinatesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (packet.use() == null) return;

            PendingTeleportRequests.Pending pending = PendingTeleportRequests.consume(player, packet.use());
            if (pending == null) {
                reject(player, packet, "no pending request");
                return;
            }

            ServerLevel level = player.serverLevel();
            BlockPos target = new BlockPos(packet.x(), packet.y(), packet.z());

            if (!level.getWorldBorder().isWithinBounds(packet.x(), packet.z())) {
                reject(player, packet, "outside world border");
                return;
            }
            if (packet.y() < level.getMinBuildHeight() || packet.y() >= level.getMaxBuildHeight()) {
                reject(player, packet, "outside build height");
                return;
            }

            double dx = packet.x() + 0.5 - player.getX();
            double dy = packet.y() - player.getY();
            double dz = packet.z() + 0.5 - player.getZ();
            if (dx * dx + dy * dy + dz * dz > pending.maxRange() * pending.maxRange()) {
                reject(player, packet, "out of range");
                return;
            }

            double tx = packet.x() + 0.5;
            double ty = packet.y();
            double tz = packet.z() + 0.5;

            player.teleportTo(level, tx, ty, tz, player.getYRot(), player.getXRot());
            player.resetFallDistance();
        });
    }

    private static void reject(ServerPlayer player, TeleportToCoordinatesPacket packet, String reason) {
        LOTMCraft.LOGGER.warn("Rejected teleport from {} ({}) to {} {} {}: {}",
                player.getGameProfile().getName(), packet.use(), packet.x(), packet.y(), packet.z(), reason);
    }
}