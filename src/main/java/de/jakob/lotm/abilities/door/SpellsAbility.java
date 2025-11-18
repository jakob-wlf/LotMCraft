package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.entity.custom.ElectricShockEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class SpellsAbility extends SelectableAbilityItem {
    private final Set<UUID> isCastingWind = new HashSet<>();

    public SpellsAbility(Properties properties) {
        super(properties, .8f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 8));
    }

    @Override
    protected float getSpiritualityCost() {
        return 15;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.spells.wind", "ability.lotmcraft.spells.electric_shock", "ability.lotmcraft.spells.freeze", "ability.lotmcraft.spells.flash"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch(abilityIndex) {
            case 0 -> wind(level, entity);
            case 1 -> electrickShock(level, entity);
            case 2 -> freeze(level, entity);
            case 3 -> flash(level, entity);
        }
    }

    private void freeze(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 4, 2);
        level.playSound(null, targetPos.x, targetPos.y, targetPos.z, Blocks.ICE.getSoundType(Blocks.ICE.defaultBlockState(), level, BlockPos.containing(targetPos.x, targetPos.y, targetPos.z), null).getBreakSound(), entity.getSoundSource(), 1.0f, 1.0f);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SNOWFLAKE, targetPos, 120, .5, .175);
        AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5, DamageLookup.lookupDamage(8, .8) * (float) multiplier(entity), targetPos, true, false);
        AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) level, entity, 2.5, targetPos, new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 2, 2, false, false, false));
    }

    private void flash(Level level, LivingEntity entity) {
        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 12, 1);

        if (!level.isClientSide) {
            BlockState lightBlock = Blocks.LIGHT.defaultBlockState();
            level.setBlock(BlockPos.containing(targetLoc.x, targetLoc.y, targetLoc.z), lightBlock, 3);

            ParticleUtil.spawnParticlesForDuration((ServerLevel) level, ParticleTypes.FLASH, targetLoc, 10, 2, 2, 0);
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) level, entity, 5, targetLoc, new MobEffectInstance(MobEffects.BLINDNESS, 20 * 5, 50, false, false, false), new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 5, false, false, false));

            ServerScheduler.scheduleDelayed(20, () -> {
                if (level.getBlockState(BlockPos.containing(targetLoc.x, targetLoc.y, targetLoc.z)).is(Blocks.LIGHT)) {
                    level.setBlock(BlockPos.containing(targetLoc.x, targetLoc.y, targetLoc.z), Blocks.AIR.defaultBlockState(), 3);
                }
            }, (ServerLevel) level);
        }
    }

    private void electrickShock(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 start = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(1, 2.85f), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, 15, 1.4f).subtract(start).normalize();

        level.playSound(null, start.x, start.y, start.z, Blocks.ICE.getSoundType(Blocks.COPPER_GRATE.defaultBlockState(), level, BlockPos.containing(start.x, start.y, start.z), null).getStepSound(), entity.getSoundSource(), 5.0f, 1.0f);

        ElectricShockEntity shock = new ElectricShockEntity(level, entity, start, direction, 60, DamageLookup.lookupDamage(8, .7) * (float) multiplier(entity));
        level.addFreshEntity(shock);
    }

    private final Random random = new Random();

    private void wind(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(isCastingWind.contains(entity.getUUID()))
            return;

        isCastingWind.add(entity.getUUID());

        ServerScheduler.scheduleForDuration(0, 1, 20 * 6, () -> {
            Vec3 dir = entity.getLookAngle().normalize().scale(.5);
            AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 10).forEach(e -> {
                e.setDeltaMovement(dir);
                e.hurtMarked = true;
            });

            if(random.nextBoolean())
                level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.ENDER_DRAGON_FLAP, SoundSource.BLOCKS, .8f, 1);

            for(int i = 0; i < 10; i++) {
                Vec3 pos = VectorUtil.getRelativePosition(entity.getEyePosition(), dir, random.nextDouble(-2, 2.5), random.nextDouble(-7, 7), random.nextDouble(-3, 3));

                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.CLOUD, pos, 0, dir.x, dir.y, dir.z, 1);
            }

        }, () -> isCastingWind.remove(entity.getUUID()), (ServerLevel) level);
    }
}
