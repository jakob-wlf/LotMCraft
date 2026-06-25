package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → Server: teleport a named player to (x, y, z) in the given dimension.
 * Only works if the sender has operator permissions.
 */
public record EnvisionTargetTeleportPacket(String playerName, double x, double y, double z, String dimensionId)
        implements CustomPacketPayload {

    public static final Type<EnvisionTargetTeleportPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "envision_target_teleport"));

    public static final StreamCodec<FriendlyByteBuf, EnvisionTargetTeleportPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeUtf(pkt.playerName());
                        buf.writeDouble(pkt.x());
                        buf.writeDouble(pkt.y());
                        buf.writeDouble(pkt.z());
                        buf.writeUtf(pkt.dimensionId());
                    },
                    buf -> new EnvisionTargetTeleportPacket(
                            buf.readUtf(),
                            buf.readDouble(), buf.readDouble(), buf.readDouble(),
                            buf.readUtf())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    // ── Server handler ────────────────────────────────────────────────────────

    public static void handle(EnvisionTargetTeleportPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sender)) return;

            // Resolve the dimension
            String resolvedId = EnvisionSelfTeleportPacket.resolveDimensionId(packet.dimensionId());
            ResourceLocation loc = ResourceLocation.tryParse(resolvedId);
            if (loc == null) {
                sender.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("§cUnknown dimension: " + packet.dimensionId()));
                return;
            }

            ResourceKey<Level> key = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, loc);
            ServerLevel targetLevel = sender.getServer().getLevel(key);
            if (targetLevel == null) {
                sender.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("§cDimension not found: " + resolvedId));
                return;
            }

            // Find the target player
            ServerPlayer target = sender.getServer().getPlayerList().getPlayerByName(packet.playerName());
            if (target == null) {
                sender.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("§cPlayer not found: " + packet.playerName()));
                return;
            }

            target.teleportTo(targetLevel,
                    packet.x(), packet.y(), packet.z(),
                    target.getYRot(), target.getXRot());

            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§aTeleported §f" + target.getName().getString() + "§a to " +
                    String.format("%.1f, %.1f, %.1f", packet.x(), packet.y(), packet.z()) +
                    " in " + resolvedId));
        });
    }
}
