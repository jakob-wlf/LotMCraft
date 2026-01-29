package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.DistortionFieldEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
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

public class DistortionFieldAbility extends Ability {
    public DistortionFieldAbility(String id) {
        super(id, 40);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 2));
    }

    @Override
    public float getSpiritualityCost() {
        return 1400;
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(56 / 255f, 19 / 255f, 102 / 255f), 5f);

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
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

        DistortionFieldEntity distortionFieldEntity = new DistortionFieldEntity(ModEntities.DISTORTION_FIELD.get(), level, 20 * 40, entity.getUUID(), false);
        distortionFieldEntity.setPos(startPos);
        serverLevel.addFreshEntity(distortionFieldEntity);

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
            });

            // Replace area near caster with air
            AbilityUtil.getBlocksInSphereRadius(serverLevel, entity.position(), 4.5, true).forEach(b -> {
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
            // Remove Distortion Field Entity
            distortionFieldEntity.discard();

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
