package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;

public record BiomeDivinationSelectedPacket(String biomeId) implements CustomPacketPayload {
    public static final Type<BiomeDivinationSelectedPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "biome_divination_selected"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BiomeDivinationSelectedPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    BiomeDivinationSelectedPacket::biomeId,
                    BiomeDivinationSelectedPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BiomeDivinationSelectedPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        ServerLevel level = player.serverLevel();
        ResourceLocation biomeKey = ResourceLocation.tryParse(packet.biomeId());

        if (biomeKey == null) {
            player.sendSystemMessage(Component.literal("§cInvalid biome id"));
            return;
        }

        int playerSequence = BeyonderData.getSequence(player);
        double worldBorder = player.level().getWorldBorder().getSize();
        int maxDistance = switch (playerSequence) {
            case 9, 8, 7, 6, 5 -> 500 * (10 - playerSequence);
            case 4             -> 5000;
            case 3             -> 15000;
            case 2             -> ((int) (worldBorder * 0.001) > 15000) ? (int) (worldBorder * 0.001) : 30000;
            case 1             -> ((int) (worldBorder * 0.01) > 30000) ? (int) (worldBorder * 0.01) : 60000;
            case 0             -> ((int) (worldBorder * 0.1) > 60000) ? (int) (worldBorder * 0.01) : 600000;
            default            -> 100;
        };

        CompletableFuture.runAsync(() -> {
            var biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
            var biomeHolder = biomeRegistry.getHolder(biomeKey).orElse(null);

            if (biomeHolder == null) return;

            var result = level.findClosestBiome3d(
                    holder -> holder.equals(biomeHolder),
                    player.blockPosition(),
                    maxDistance,
                    64,
                    64
            );

            context.enqueueWork(() -> {
                if (result == null) {
                    player.sendSystemMessage(Component.literal("§cNo such biome nearby in the sensed area"));
                    return;
                }

                BlockPos biomePos = result.getFirst();
                BlockPos playerPos = player.blockPosition();

                int dx = biomePos.getX() - playerPos.getX();
                int dz = biomePos.getZ() - playerPos.getZ();
                int distance = (int) Math.sqrt(dx * dx + dz * dz);

                String direction = getDirection(dx, dz);
                String biomeName = packet.biomeId();

                int colonIndex = biomeName.indexOf(':');
                if (colonIndex != -1) {
                    biomeName = biomeName.substring(colonIndex + 1);
                }
                biomeName = biomeName.replace('_', ' ');

                player.sendSystemMessage(Component.literal(String.format(
                        "§5You sense §d%s§5 to the §d%s§5, about §d%d blocks §5away...",
                        biomeName,
                        direction,
                        distance
                )));
            });
        });
    }

    private static String getDirection(int dx, int dz) {
        if (dx == 0 && dz == 0) return "here";

        double angle = Math.toDegrees(Math.atan2(dz, dx));
        if (angle < 0) angle += 360;

        String[] directions = {
                "East", "South-East", "South", "South-West",
                "West", "North-West", "North", "North-East", "East"
        };

        int index = (int) Math.floor((angle + 22.5) / 45);
        return directions[index];
    }
}