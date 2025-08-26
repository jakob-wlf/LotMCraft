package de.jakob.lotm.abilities.demoness;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.entity.custom.FrostSpearProjectileEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

//TODO: Add snow/ice when griefing is enabled
public class FrostAbility extends SelectableAbilityItem {

    public FrostAbility(Properties properties) {
        super(properties, .75f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "demoness", 7
        ));
    }

    @Override
    protected float getSpiritualityCost() {
        return 30;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.frost.shoot",
                "ability.lotmcraft.frost.spear",
                "ability.lotmcraft.frost.freeze_area"
        };
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch(abilityIndex) {
            case 0 -> shoot(level, entity);
            case 1 -> spear(level, entity);
            case 2 -> freezeArea(level, entity);
        }
    }

    private void freezeArea(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = entity.position();
        level.playSound(null, startPos.x, startPos.y, startPos.z, Blocks.ICE.getSoundType(Blocks.ICE.defaultBlockState(), level, BlockPos.containing(startPos.x, startPos.y, startPos.z), null).getBreakSound(), entity.getSoundSource(), 1.0f, 1.0f);

        AtomicDouble radius = new AtomicDouble(.5);

        ServerScheduler.scheduleForDuration(0, 2, 20 * 3, () -> {
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SNOWFLAKE, startPos, 70, radius.get(), 0.3, radius.get(), 0);

            AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, radius.get() - .4, radius.get() + .4, 15 * multiplier(entity), startPos, true, false, true, 0, 0);
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) level, entity, radius.get(), startPos, new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 5, 10, false, false, false));

            if(BeyonderData.isGriefingEnabled(entity)) {
                AbilityUtil.getBlocksInCircleOutline((ServerLevel) level, startPos, radius.get(), 100).forEach(b -> {
                    if(!level.getBlockState(b).isAir())
                        level.setBlockAndUpdate(b, Blocks.PACKED_ICE.defaultBlockState());
                });
                AbilityUtil.getBlocksInCircleOutline((ServerLevel) level, startPos.add(0, 1, 0), radius.get(), 100).forEach(b -> {
                    if(!level.getBlockState(b).isAir())
                        level.setBlockAndUpdate(b, Blocks.PACKED_ICE.defaultBlockState());
                });
                AbilityUtil.getBlocksInCircleOutline((ServerLevel) level, startPos.subtract(0, 1, 0), radius.get(), 100).forEach(b -> {
                    if(!level.getBlockState(b).isAir())
                        level.setBlockAndUpdate(b, Blocks.PACKED_ICE.defaultBlockState());
                });
                AbilityUtil.getBlocksInCircleOutline((ServerLevel) level, startPos.subtract(0, 2, 0), radius.get(), 100).forEach(b -> {
                    if(!level.getBlockState(b).isAir())
                        level.setBlockAndUpdate(b, Blocks.PACKED_ICE.defaultBlockState());
                });
            }

            radius.addAndGet(.5);
        }, (ServerLevel) level);
    }

    private void spear(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(1, 2.85f), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, 50, 1.4f).subtract(startPos).normalize();

        level.playSound(null, startPos.x, startPos.y, startPos.z, Blocks.ICE.getSoundType(Blocks.ICE.defaultBlockState(), level, BlockPos.containing(startPos.x, startPos.y, startPos.z), null).getBreakSound(), entity.getSoundSource(), 1.0f, 1.0f);

        FrostSpearProjectileEntity spear = new FrostSpearProjectileEntity(level, entity, 18 * multiplier(entity), BeyonderData.isGriefingEnabled(entity));
        spear.setPos(startPos.x, startPos.y, startPos.z); // Set initial position
        spear.shoot(direction.x, direction.y, direction.z, 1.6f, 0);
        level.addFreshEntity(spear);
    }


    private void shoot(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(-.65, .65), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, 10, 1.4f).subtract(startPos).normalize();

        AtomicReference<Vec3> currentPos = new AtomicReference<>(startPos);

        AtomicBoolean hasHit = new AtomicBoolean(false);

        level.playSound(null, startPos.x, startPos.y, startPos.z, Blocks.ICE.getSoundType(Blocks.ICE.defaultBlockState(), level, BlockPos.containing(startPos.x, startPos.y, startPos.z), null).getBreakSound(), SoundSource.BLOCKS, 1.0f, 1.0f);

        ServerScheduler.scheduleForDuration(0, 1, 20 * 40, () -> {
            if(hasHit.get())
                return;

            Vec3 pos = currentPos.get();

            if(AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5f, 15 * multiplier(entity), pos, true, false, true, 0)) {
                hasHit.set(true);
                AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) level, entity, 2.5f, pos, new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 3, 5, false, false, false));
                return;
            }

            if(!level.getBlockState(BlockPos.containing(pos.x, pos.y, pos.z)).isAir()) {
                if(BeyonderData.isGriefingEnabled(entity))
                    level.setBlockAndUpdate(BlockPos.containing(pos.x, pos.y, pos.z), Blocks.PACKED_ICE.defaultBlockState());
                hasHit.set(true);
                return;
            }

            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SNOWFLAKE, pos, 45, 0.25, 0.02);

            currentPos.set(pos.add(direction));
        }, (ServerLevel) level);
    }
}
