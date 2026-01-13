package de.jakob.lotm.abilities.darkness;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.network.packets.toClient.DarknessEffectPacket;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ClientScheduler;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class SurgeOfDarknessAbility extends AbilityItem {
    public SurgeOfDarknessAbility(Properties properties) {
        super(properties, 11);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1000;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            Vec3 center = entity.position();

            // Affect entities
            ServerScheduler.scheduleForDuration(0, 4, 20 * 15, () -> {
                AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) level, entity, 45,
                        center, new MobEffectInstance(MobEffects.BLINDNESS, 20 * 10, 5, false, false, false));

                AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, center, 45).forEach(e -> {
                    SanityComponent sanityComponent = e.getData(ModAttachments.SANITY_COMPONENT);
                    sanityComponent.increaseSanityAndSync(-.0025f, e);
                });

                AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 45, DamageLookup.lookupDps(3, .5, 4, 20) * multiplier(entity), center, true, false);
            });

            List<BlockPos> affectedBlocks = AbilityUtil.getBlocksInEllipsoid((ServerLevel) level, center, 45, 18, true, false, true)
                    .stream().filter(blockPos -> !level.getBlockState(blockPos).isAir()).toList();

            // Sort blocks by distance from center for spreading effect
            List<BlockPos> sortedBlocks = affectedBlocks.stream()
                    .sorted(Comparator.comparingDouble(pos -> pos.distSqr(BlockPos.containing(center))))
                    .toList();

            // Group blocks into waves based on distance
            int currentIndex = 0;
            int waveNumber = 0;

            while (currentIndex < sortedBlocks.size()) {
                // Calculate wave size - increases as we go outward
                int waveSize = Math.min(1 + (waveNumber * 2), sortedBlocks.size() - currentIndex);

                // Get blocks for this wave
                List<BlockPos> waveBlocks = new ArrayList<>(sortedBlocks.subList(currentIndex, currentIndex + waveSize));

                // Schedule this wave to turn black
                int delay = waveNumber; // 1 tick per wave
                final int currentWave = waveNumber;

                ServerScheduler.scheduleDelayed(delay, () -> {
                    // Send packet to all players in the level
                    DarknessEffectPacket packet = new DarknessEffectPacket(waveBlocks, false, currentWave);
                    PacketHandler.sendToAllPlayersInSameLevel(packet, (ServerLevel) level);
                }, (ServerLevel) level);

                currentIndex += waveSize;
                waveNumber++;
            }

            // Calculate total spread time based on number of waves
            int totalSpreadTime = waveNumber;

            int restorationTime = (20 * 10) + totalSpreadTime;

            ServerScheduler.scheduleDelayed(restorationTime, () -> {
                // Send restoration packet to clients
                DarknessEffectPacket packet = new DarknessEffectPacket(sortedBlocks, true, 0);
                PacketHandler.sendToAllPlayersInSameLevel(packet, (ServerLevel) level);

                // Force block updates server-side to ensure sync
                sortedBlocks.forEach(b -> {
                    level.sendBlockUpdated(b, level.getBlockState(b), level.getBlockState(b), Block.UPDATE_ALL);
                });
            }, (ServerLevel) level);



        }
    }
}