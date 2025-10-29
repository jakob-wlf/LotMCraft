package de.jakob.lotm.abilities.wheel_of_fortune.calamities;

import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Earthquake extends Calamity{
    @Override
    public Component getName() {
        return Component.translatable("lotm.calamity.earthquake");
    }

    private final Random random = new Random();

    @Override
    public void spawnCalamity(ServerLevel level, Vec3 startPos, float multiplier, boolean griefing) {
        int radius = 28;
        List<BlockPos> blocks = new ArrayList<>(AbilityUtil.getBlocksInCircle(level, startPos.add(0, -2, 0), 30));
        for(int i = -12; i < 13; i++) {
            blocks.addAll(AbilityUtil.getBlocksInCircle(level, startPos.add(0, i, 0), radius));
        }

        List<BlockPos> validBlocks = blocks.stream().filter(b -> !level.getBlockState(b).getCollisionShape(level, b).isEmpty() && level.getBlockState(b.above()).getCollisionShape(level, b).isEmpty() && !level.getBlockState(b).is(Blocks.WATER)).toList();

        ServerScheduler.scheduleForDuration(0, 8, 20 * 15, () -> {
            AbilityUtil.getNearbyEntities(null, level, startPos, radius + 6).forEach(e -> {
                if(AbilityUtil.distanceToGround(level, e) < 1.5) {
                    if(random.nextBoolean())
                        e.hurt(e.damageSources().generic(), 9.5f * multiplier);
                    if(random.nextInt(12) == 0)
                        e.setDeltaMovement(new Vec3((0.5 - random.nextDouble()) * 0.5, 0.25 + random.nextDouble() * .75, (0.5 - random.nextDouble()) * 0.25));
                }
            });

            for(BlockPos b : validBlocks) {
                if(random.nextInt(35) == 0)
                    ParticleUtil.spawnParticles(level, ModParticles.EARTHQUAKE.get(), new Vec3(b.getCenter().x, b.getCenter().y + .85, b.getCenter().z), 1, .2, 0);

                if(random.nextInt(200) == 0)
                    ParticleUtil.spawnParticles(level, ParticleTypes.EXPLOSION, new Vec3(b.getCenter().x, b.getCenter().y + .85, b.getCenter().z), 1, .2, 0);
            }

            for (int i = 0; i < 80; i++) {
                BlockPos pos = validBlocks.get(random.nextInt(validBlocks.size()));
                BlockState state = level.getBlockState(pos);

                if (!state.isAir()) {
                    double y = pos.getY() + 1;
                    for(int j = 0; j < 10; j++) {
                        if(!level.getBlockState(BlockPos.containing(pos.getX(), y, pos.getZ())).isAir())
                            y++;
                        else {
                            break;
                        }
                    }

                    FallingBlockEntity falling = FallingBlockEntity.fall(
                            level,
                            BlockPos.containing(pos.getCenter().x, y, pos.getCenter().z),
                            state
                    );

                    double xVel = (random.nextDouble() - 0.5) * 0.15;
                    double yVel = 0.5 + random.nextDouble() * .6;
                    double zVel = (random.nextDouble() - 0.5) * 0.15;
                    falling.setDeltaMovement(xVel, yVel, zVel);

                    ServerScheduler.scheduleForDuration(0, 1, 40, () -> {
                        falling.setDeltaMovement(falling.getDeltaMovement().x, falling.getDeltaMovement().y - 0.03, falling.getDeltaMovement().z);
                        falling.hurtMarked = true;
                    });

                    falling.dropItem = false;
                    if(!griefing)
                        falling.disableDrop();


                    level.addFreshEntity(falling);
                }
            }
        }, level);
    }
}
