package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.PlanetEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.PlayPhotonBlockEffectPacket;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SpaceFragmentationAbility extends Ability {
    public SpaceFragmentationAbility(String id) {
        super(id, 20);

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(22500f, 12000f));

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(5, 15));

        baseDamage = 6f;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        Vec3 targetLoc =  AbilityUtil.getTargetLocation(entity, baseDistance, 2);

        BlockPos blockPos = BlockPos.containing(targetLoc);

        double offsetX = targetLoc.x - (blockPos.getX() + 0.5);
        double offsetY = targetLoc.y - (blockPos.getY() + 0.5) + .25;
        double offsetZ = targetLoc.z - (blockPos.getZ() + 0.5);

        PacketHandler.sendToNearbyPlayers(
                new PlayPhotonBlockEffectPacket("space_fragmentation", blockPos, offsetX, offsetY, offsetZ, 1, null, -1, false, true, null),
                (ServerLevel) level, targetLoc, 128
        );

        final int BLOW_TICK = 160;
        final int GRIEF_START = 60;
        final int PHASE2_END = 100;
        final int mult = (int) Math.max(multiplier(entity) / 4, 1);

        AtomicInteger tick = new AtomicInteger(0);
        ServerScheduler.scheduleForDuration(0, 1, BLOW_TICK + 1, () -> {
            int t = tick.get();
            float progress = Math.min(t / (float) BLOW_TICK, 1f);

            if (t == BLOW_TICK) {
                AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 40,
                        DamageLookup.lookupDamage(1, 2f) * mult, targetLoc, true, true);

                if (BeyonderData.isGriefingEnabled(entity)) {
                    AbilityUtil.getBlocksInSphereRadius(level, targetLoc, 37, true, true, false).forEach(pos -> {
                        if (level.getBlockState(pos).getDestroySpeed(level, pos) >= 0
                                && !pos.getCenter().equals(BlockPos.containing(entity.position().subtract(0, 1, 0)).getCenter())
                                && level.random.nextFloat() < 0.75f) {
                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        }
                    });
                    AbilityUtil.getBlocksInSphereRadius(level, targetLoc, 32, true, true, false).forEach(pos -> {
                        if (level.getBlockState(pos).getDestroySpeed(level, pos) >= 0
                                && !pos.getCenter().equals(BlockPos.containing(entity.position().subtract(0, 1, 0)).getCenter())) {
                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        }
                    });
                }
                tick.getAndIncrement();
                return;
            }

            if (t % 20 == 0) {
                float scale = progress * 0.5f;
                double dmgRadius = 8 + progress * 22;
                AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, dmgRadius,
                        DamageLookup.lookupDamage(1, scale) * mult, targetLoc, true, true);
            }

            AABB box = new AABB(targetLoc, targetLoc).inflate(40);
            for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (living == entity) continue;
                Vec3 toCenter = targetLoc.subtract(living.position());
                double dist = toCenter.length();
                if (dist < 1.5 || dist > 40) continue;

                double strength = 0.10 + 0.05 * progress;       // moderate, slightly stronger over time
                Vec3 motion = living.getDeltaMovement().add(toCenter.normalize().scale(strength));
                if (motion.length() > 0.7) motion = motion.normalize().scale(0.7); // cap speed
                living.setDeltaMovement(motion);
                living.hurtMarked = true;
            }

            if (t >= GRIEF_START && BeyonderData.isGriefingEnabled(entity)) {
                float radius;
                if (t < PHASE2_END) {
                    radius = 3 + 3 * (t - GRIEF_START) / (float) (PHASE2_END - GRIEF_START);
                } else {
                    radius = 7 + 2 * (t - PHASE2_END) / (float) (BLOW_TICK - PHASE2_END);
                }

                AbilityUtil.getBlocksInSphereRadius(level, targetLoc, radius, true, true, false).forEach(pos -> {
                    if (level.random.nextFloat() < 0.3f
                            && level.getBlockState(pos).getDestroySpeed(level, pos) >= 0
                            && !pos.getCenter().equals(BlockPos.containing(entity.position().subtract(0, 1, 0)).getCenter())) {
                        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    }
                });
            }

            tick.getAndIncrement();
        }, null, (ServerLevel) level);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 12000;
    }
}
