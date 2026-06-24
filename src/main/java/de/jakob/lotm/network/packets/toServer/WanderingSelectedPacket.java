package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Set;

public record WanderingSelectedPacket(String dimensionId) implements CustomPacketPayload {

    public static final Type<WanderingSelectedPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "wandering_selected"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WanderingSelectedPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    WanderingSelectedPacket::dimensionId,
                    WanderingSelectedPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WanderingSelectedPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            MinecraftServer server = player.getServer();
            if (server == null) return;

            ResourceKey<Level> targetKey = ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    ResourceLocation.parse(packet.dimensionId())
            );

            // Validate the target is not a forbidden dimension
            if (targetKey.equals(ModDimensions.SEFIRAH_CASTLE_DIMENSION_KEY) ||
                    targetKey.equals(ModDimensions.CONCEALMENT_WORLD_DIMENSION_KEY)) {
                return;
            }

            ServerLevel targetLevel = server.getLevel(targetKey);
            if (targetLevel == null) return;

            ServerLevel currentLevel = player.serverLevel();

            double yValue = player.position().y;
            int minY = targetLevel.getMinBuildHeight();
            int maxY = targetLevel.getMaxBuildHeight();
            for (int i = 0; i < 100; i++) {
                if (yValue >= maxY) break;
                BlockPos pos = BlockPos.containing(player.getX(), yValue, player.getZ());
                if (pos.getY() < minY) { yValue = minY; break; }
                if (targetLevel.getBlockState(pos).getCollisionShape(targetLevel, pos).isEmpty()) {
                    break;
                }
                yValue += 1;
            }
            yValue = Math.max(minY, Math.min(yValue, maxY - 2));

            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 5, 1, false, false, false));
            ParticleUtil.spawnParticles(currentLevel, ModParticles.STAR.get(), player.position().add(0, 1, 0), 100, .4, 1.1, .4, .05);
            ParticleUtil.spawnParticles(currentLevel, ParticleTypes.ENCHANT, player.position().add(0, 1, 0), 100, .4, 1.1, .4, .05);
            currentLevel.playSound(null, player.blockPosition(), SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 1, 1);
            player.teleportTo(targetLevel, player.getX(), yValue, player.getZ(), Set.of(), player.getYRot(), player.getXRot());
            ParticleUtil.spawnParticles(targetLevel, ModParticles.STAR.get(), player.position().add(0, 1, 0), 100, .4, 1.1, .4, .05);
            ParticleUtil.spawnParticles(targetLevel, ParticleTypes.ENCHANT, player.position().add(0, 1, 0), 100, .4, 1.1, .4, .05);
            targetLevel.playSound(null, player.blockPosition(), SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 1, 1);
        });
    }
}
