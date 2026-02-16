package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.entity.custom.BigMoonEntity;
import de.jakob.lotm.entity.custom.GiantLightningEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.DarknessEffectPacket;
import de.jakob.lotm.network.packets.toClient.FireEffectPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class WrathOfNatureAbility extends SelectableAbility {
    public WrathOfNatureAbility(String id) {
        super(id, 3);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.wrath_of_nature.lightning", "ability.lotmcraft.wrath_of_nature.fire", "ability.lotmcraft.wrath_of_nature.moon"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch (selectedAbility) {
            case 0 -> lightning(level, entity);
            case 1 -> fire(level, entity);
            case 2 -> moon(level, entity);
        }
    }

    private void moon(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(!serverLevel.getEntitiesOfClass(BigMoonEntity.class, entity.getBoundingBox().inflate(100)).isEmpty()) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.wrath_of_nature.moon_on_cooldown").withColor(0xF44336));
            return;
        }

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 30, 1.5f, true);
        BigMoonEntity moonEntity = new BigMoonEntity(serverLevel, (float) DamageLookup.lookupDps(2, .7f, 2, 20) * (float) BeyonderData.getMultiplierForSequence(2), BeyonderData.isGriefingEnabled(entity), entity.getUUID(), 20 * 30);
        moonEntity.setPos(targetPos.x, targetPos.y + 25, targetPos.z);
        serverLevel.addFreshEntity(moonEntity);
    }

    private void fire(Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            Vec3 center = entity.position();

            // Affect entities
            ServerScheduler.scheduleForDuration(0, 4, 20 * 15, () -> {
                AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 55, DamageLookup.lookupDps(1, .5, 4, 20) * multiplier(entity), center, true, false, 20 * 8);
            });

            List<BlockPos> affectedBlocks = AbilityUtil.getBlocksInEllipsoid((ServerLevel) level, center, 45, 18, true, false, true)
                    .stream().filter(blockPos -> !level.getBlockState(blockPos).isAir()).map(BlockPos::above).toList();

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
                    FireEffectPacket packet = new FireEffectPacket(waveBlocks, false, currentWave);
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
                FireEffectPacket packet = new FireEffectPacket(sortedBlocks, true, 0);
                PacketHandler.sendToAllPlayersInSameLevel(packet, (ServerLevel) level);

                // Force block updates server-side to ensure sync
                sortedBlocks.forEach(b -> {
                    level.sendBlockUpdated(b, level.getBlockState(b), level.getBlockState(b), Block.UPDATE_ALL);
                });
            }, (ServerLevel) level);



        }
    }

    private void lightning(Level level, LivingEntity entity) {
        Vec3 targetLocFinak = AbilityUtil.getTargetLocation(entity, 70, 2, true);
        ServerScheduler.scheduleForDuration(0, 20, 20 * 4, () -> {
            Vec3 targetLoc = new Vec3(targetLocFinak.x, targetLocFinak.y, targetLocFinak.z);
            for(int i = 0; i < 35; i++) {
                BlockState state = level.getBlockState(BlockPos.containing(targetLoc.subtract(0, 1, 0)));
                if(state.getCollisionShape(level, BlockPos.containing(targetLoc)).isEmpty())
                    targetLoc = targetLoc.subtract(0, 1, 0);
            }

            GiantLightningEntity lightning = new GiantLightningEntity(level, entity, targetLoc, 50, 6, DamageLookup.lookupDamage(1, .4) * multiplier(entity), BeyonderData.isGriefingEnabled(entity), 13, 200, 0x6522a8);
            level.addFreshEntity(lightning);
        });
    }
}
