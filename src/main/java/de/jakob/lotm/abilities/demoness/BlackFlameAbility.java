package de.jakob.lotm.abilities.demoness;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

//TODO: Create black flame block
public class BlackFlameAbility extends SelectableAbility {

    public BlackFlameAbility(String id) {
        super(id, .75f);
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
                "ability.lotmcraft.black_flame.burn",
                "ability.lotmcraft.black_flame.shoot",
                "ability.lotmcraft.black_flame.expel"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch(abilityIndex) {
            case 0 -> burn(level, entity);
            case 1 -> shoot(level, entity);
            case 2 -> expel(level, entity);
        }
    }

    DustParticleOptions dust = new DustParticleOptions(new Vector3f(0, 0, 0), 2f);

    private void burn(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 10, 1.4f);
        level.playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 2.0f, .5f);

        ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.BLACK_FLAME.get(), targetPos.subtract(0, .75, 0), 700, .3, 1.3, .3, .01);
        ParticleUtil.spawnParticles((ServerLevel) level, dust, targetPos.subtract(0, .75, 0), 190, .3, 1.3, .3, .02);

        AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5, DamageLookup.lookupDamage(7, .7) * multiplier(entity), targetPos, true, false, true, 0, 20 * 2);

        BlockState block = level.getBlockState(BlockPos.containing(targetPos));
        if(block.isAir()) {
            level.setBlockAndUpdate(BlockPos.containing(targetPos), Blocks.LIGHT.defaultBlockState());
        }

        ServerScheduler.scheduleDelayed(25, () -> level.setBlockAndUpdate(BlockPos.containing(targetPos), Blocks.AIR.defaultBlockState()));
    }

    //TODO: Place Black Flames on griefing
    private void expel(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = entity.getEyePosition().add(0, .5, 0);

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);

        AtomicDouble i = new AtomicDouble(0.6);
        ServerScheduler.scheduleForDuration(0, 2, 80, () -> {
            double ySubtraction = i.get() <= 1.5 ? 2 * ((1/((10 * i.get()) - 9)) - 1) : -2;
            Vec3 currentPos = startPos.add(0, ySubtraction, 0);
            double radius = i.get() < .71 ? i.get() : i.get() * 2;
            ParticleUtil.spawnCircleParticles((ServerLevel) level, ModParticles.BLACK_FLAME.get(), currentPos, radius, (int) (radius * 27));
            AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, radius - .3, radius, DamageLookup.lookupDamage(7, .8) * multiplier(entity), startPos.subtract(0, 1, 0), true, false, true, 0, 20 * 5);
            i.set(i.get() + .1);
        }, (ServerLevel) level);
    }

    private void shoot(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(-.65, .65), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, 10, 1.4f).subtract(startPos).normalize();

        AtomicReference<Vec3> currentPos = new AtomicReference<>(startPos);

        AtomicBoolean hasHit = new AtomicBoolean(false);

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);

        ServerScheduler.scheduleForDuration(0, 1, 20 * 40, () -> {
            if(hasHit.get())
                return;

            Vec3 pos = currentPos.get();

            if(AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5f, DamageLookup.lookupDamage(7, 1.1) * multiplier(entity), pos, true, false, true, 0, 20 * 5)) {
                hasHit.set(true);
                return;
            }

            if(!level.getBlockState(BlockPos.containing(pos.x, pos.y, pos.z)).isAir()) {
                if(BeyonderData.isGriefingEnabled(entity)) {
                    pos = pos.subtract(direction);
                    level.setBlockAndUpdate(BlockPos.containing(pos.x, pos.y, pos.z), Blocks.FIRE.defaultBlockState());
                }
                hasHit.set(true);
                return;
            }

            ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.BLACK_FLAME.get(), pos, 45, 0.25, 0.02);

            currentPos.set(pos.add(direction));
        }, (ServerLevel) level);
    }
}
