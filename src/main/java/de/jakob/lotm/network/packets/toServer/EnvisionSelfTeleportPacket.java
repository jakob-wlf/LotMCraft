package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Client → Server: teleport the sending player to (x, y, z) in the given dimension.
 * The dimension is identified by a ResourceLocation string (e.g. "minecraft:overworld").
 */
public record EnvisionSelfTeleportPacket(double x, double y, double z, String dimensionId)
        implements CustomPacketPayload {

    public static final Type<EnvisionSelfTeleportPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "envision_self_teleport"));

    public static final StreamCodec<FriendlyByteBuf, EnvisionSelfTeleportPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeDouble(pkt.x());
                        buf.writeDouble(pkt.y());
                        buf.writeDouble(pkt.z());
                        buf.writeUtf(pkt.dimensionId());
                    },
                    buf -> new EnvisionSelfTeleportPacket(
                            buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readUtf())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    // ── Friendly name → ResourceLocation ─────────────────────────────────────

    /** Resolve short/friendly names to dimension ResourceLocation strings. */
    public static String resolveDimensionId(String input) {
        if (input == null || input.isBlank()) return "minecraft:overworld";
        String lower = input.trim().toLowerCase();
        // Explicit aliases
        Map<String, String> aliases = new HashMap<>();
        aliases.put("overworld",               "minecraft:overworld");
        aliases.put("ow",                      "minecraft:overworld");
        aliases.put("nether",                  "minecraft:the_nether");
        aliases.put("the_nether",              "minecraft:the_nether");
        aliases.put("hell",                    "minecraft:the_nether");
        aliases.put("end",                     "minecraft:the_end");
        aliases.put("the_end",                 "minecraft:the_end");
        aliases.put("chaos_sea",               "lotmcraft:chaos_sea");
        aliases.put("chaos",                   "lotmcraft:chaos_sea");
        aliases.put("sefirah_castle",          "lotmcraft:sefirah_castle");
        aliases.put("castle",                  "lotmcraft:sefirah_castle");
        aliases.put("river",                   "lotmcraft:river_of_eternal_darkness");
        aliases.put("river_of_eternal_darkness","lotmcraft:river_of_eternal_darkness");
        aliases.put("dream_maze",              "lotmcraft:dream_maze");
        aliases.put("dream",                   "lotmcraft:dream_maze");
        aliases.put("space",                   "lotmcraft:space");
        aliases.put("nature",                  "lotmcraft:nature");
        aliases.put("spirit_world",            "lotmcraft:spirit_world");
        aliases.put("spirit",                  "lotmcraft:spirit_world");

        if (aliases.containsKey(lower)) return aliases.get(lower);
        // If it already looks like a namespaced id, pass through
        if (lower.contains(":")) return lower;
        // Otherwise assume lotmcraft namespace
        return "lotmcraft:" + lower;
    }

    // ── Server handler ────────────────────────────────────────────────────────

    public static void handle(EnvisionSelfTeleportPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            String resolvedId = resolveDimensionId(packet.dimensionId());
            ResourceLocation loc = ResourceLocation.tryParse(resolvedId);
            if (loc == null) {
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("§cUnknown dimension: " + packet.dimensionId()));
                return;
            }

            ResourceKey<Level> key = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, loc);
            ServerLevel target = player.getServer().getLevel(key);
            if (target == null) {
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("§cDimension not found: " + resolvedId));
                return;
            }

            player.teleportTo(target,
                    packet.x(), packet.y(), packet.z(),
                    player.getYRot(), player.getXRot());
        });
    }
}
