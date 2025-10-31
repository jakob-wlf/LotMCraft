package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistortionField extends AbilityItem {
    public DistortionField(Properties properties) {
        super(properties, 40);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1400;
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(56 / 255f, 19 / 255f, 102 / 255f), 5f);

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 startPos = entity.position();

        // Initialize and populate Blocklist
        List<BlockPos> barrierBlocks = new ArrayList<>();
        for(int i = -9; i < 9; i++) {
            barrierBlocks.addAll(AbilityUtil.getBlocksInCircle(serverLevel, new Vec3(startPos.x, startPos.y + i, startPos.z), 40));
        }

        barrierBlocks.removeIf(b -> !serverLevel.getBlockState(b).isAir());
        barrierBlocks.removeIf(b -> random.nextInt(2) != 0);

        ServerScheduler.scheduleForDuration(0, 6, 20 * 30, () -> {
            if(entity.level() != serverLevel) {
                return;
            }

            // Add Barrier Blocks and Particles
            barrierBlocks.forEach(b -> {
                if(!serverLevel.getBlockState(b).isAir() && !serverLevel.getBlockState(b).is(Blocks.BARRIER)) {
                    return;
                }

                serverLevel.setBlockAndUpdate(b, Blocks.BARRIER.defaultBlockState());

                // Make sure there are not too many particles at once
                if(random.nextInt(100) != 0) {
                    return;
                }

                ParticleUtil.spawnParticles(serverLevel, ModParticles.STAR.get(), b.getCenter(), 5, .5, .5, .5, 0);
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, b.getCenter(), 1, .5, .5, .5, 0);
                ParticleUtil.spawnParticles(serverLevel, dust, b.getCenter(), 5, .5, .5, .5, 0);
            });

            // Replace area near caster with air
            AbilityUtil.getBlocksInSphereRadius(serverLevel, entity.position(), 2.5, true).forEach(b -> {
                if(serverLevel.getBlockState(b).getBlock() != Blocks.BARRIER) {
                    return;
                }

                serverLevel.setBlockAndUpdate(b, Blocks.AIR.defaultBlockState());
            });

            // Randomly teleport entities around and disable ability use
            AbilityUtil.getNearbyEntities(entity, serverLevel, startPos, 40).forEach(e -> {
                if(random.nextInt(15) == 0) {
                    BeyonderData.disableAbilityUse(e, "distortion_field");
                    ServerScheduler.scheduleDelayed(20 * 2, () -> {
                        if(e.distanceToSqr(startPos) > 30 * 30) {
                            BeyonderData.enableAbilityUse(e, "distortion_field");
                        }
                    });
                }

                e.teleportTo(e.getX() + random.nextDouble(-8, 8), e.getY() + random.nextDouble(-1, 2), e.getZ() + random.nextDouble(-8, 8));
            });

        }, () -> {
            // Remove Barriers
            barrierBlocks.forEach(b -> {
                if(!serverLevel.getBlockState(b).is(Blocks.BARRIER)) {
                    return;
                }

                serverLevel.setBlockAndUpdate(b, Blocks.AIR.defaultBlockState());
            });

            // Enable Ability use
            AbilityUtil.getNearbyEntities(entity, serverLevel, startPos, 45).forEach(e -> {
                BeyonderData.enableAbilityUse(e, "distortion_field");
            });
        }, serverLevel);
    }

    @Override
    public boolean shouldUseAbility(LivingEntity entity) {
        return random.nextInt(10) == 0;
    }
}
