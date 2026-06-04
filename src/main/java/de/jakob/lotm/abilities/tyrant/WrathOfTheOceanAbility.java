package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WrathOfTheOceanAbility extends SelectableAbility {
    public WrathOfTheOceanAbility(String id) {
        super(id, 50);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 2500;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.wrath_of_the_ocean.boiling_water", "ability.lotmcraft.wrath_of_the_ocean.repel"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (level.isClientSide) return;

        switch (selectedAbility) {
            case 0 -> boilingWater((ServerLevel) level, entity);
            case 1 -> repel((ServerLevel) level, entity);
        }
    }

    private boolean isAquatic(LivingEntity entity) {
        return entity.canBreatheUnderwater();
    }

    private void boilingWater(ServerLevel level, LivingEntity caster) {
        level.playSound(null, caster.blockPosition(), SoundEvents.LAVA_AMBIENT, SoundSource.PLAYERS, 1.5f, 0.7f);

        Vec3 origin = caster.position();

        BlockPos min = BlockPos.containing(origin.x - 100, origin.y - 100, origin.z - 100);
        BlockPos max = BlockPos.containing(origin.x + 100, origin.y + 100, origin.z + 100);

        List<BlockPos> waterSurfacePositions = BlockPos.betweenClosedStream(min, max)
                .filter(pos -> {
                    BlockState state = level.getBlockState(pos);
                    BlockState above = level.getBlockState(pos.above());
                    return (state.getBlock() == Blocks.WATER || state.getFluidState().is(FluidTags.WATER))
                            && !above.getFluidState().is(FluidTags.WATER)
                            && above.isAir();
                })
                .map(BlockPos::immutable)
                .toList();

        ServerScheduler.scheduleForDuration(0, 10, 1000, () -> {
            AbilityUtil.getNearbyEntities(caster, level, origin, 100, false).stream()
                    .filter(t -> !isAquatic(t) && t.isInWater())
                    .forEach(t -> {
                        t.hurt(level.damageSources().genericKill(), (float) DamageLookup.lookupDamage(3, .05f) * Math.max(multiplier(caster) / 4, 1));
                        level.playSound(null, t.blockPosition(), SoundEvents.GENERIC_BURN, SoundSource.PLAYERS, 0.6f, 0.8f + level.random.nextFloat() * 0.4f);
                        ParticleUtil.spawnParticles(level, ParticleTypes.BUBBLE_POP,
                                t.position().add(0, 0.5, 0), 12, 0.3, 0.3, 0.3, 0.1);
                    });

            waterSurfacePositions.stream()
                    .filter(pos -> random.nextInt(5) == 0)
                    .forEach(pos -> ParticleUtil.spawnParticles(level,
                            random.nextBoolean() ? ParticleTypes.LARGE_SMOKE : random.nextBoolean() ? ParticleTypes.CLOUD : ParticleTypes.LAVA,
                            new Vec3(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5),
                            3, 0.4, 0.1, 0.4, 0.02));
        }, level);
    }

    private void repel(ServerLevel level, LivingEntity caster) {
        level.playSound(null, caster.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 1.2f, 0.6f);

        Vec3 origin = caster.position();

        ServerScheduler.scheduleForDuration(0, 5, 1000, () -> {
            AbilityUtil.getNearbyEntities(caster, level, origin, 100, false).stream()
                    .filter(t -> !isAquatic(t) && t.isInWater())
                    .forEach(t -> {
                        Vec3 direction = t.position().subtract(origin);

                        if (direction.horizontalDistanceSqr() < 0.001) {
                            direction = new Vec3(1, 0, 0);
                        }

                        Vec3 push = new Vec3(direction.x, 0, direction.z)
                                .normalize()
                                .scale(1.5)
                                .add(0, 0.8, 0);

                        t.setDeltaMovement(push);
                        t.hurtMarked = true;

                        level.playSound(null, t.blockPosition(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.8f, 1.1f + level.random.nextFloat() * 0.3f);

                        ParticleUtil.spawnParticles(level, ParticleTypes.SPLASH,
                                t.position().add(0, 0.3, 0), 20, 0.4, 0.2, 0.4, 0.1);
                        ParticleUtil.spawnParticles(level, ParticleTypes.BUBBLE,
                                t.position().add(0, 0.5, 0), 10, 0.3, 0.3, 0.3, 0.05);
                        ParticleUtil.spawnCircleParticles(level, ParticleTypes.BUBBLE_POP,
                                t.position().add(0, 0.2, 0), 1.2, 16);
                    });
        }, level);
    }
}