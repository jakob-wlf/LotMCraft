package de.jakob.lotm.abilities.red_priest;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.entity.custom.FireRavenEntity;
import de.jakob.lotm.entity.custom.FireballEntity;
import de.jakob.lotm.entity.custom.FlamingSpearProjectileEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class PyrokinesisAbility extends SelectableAbility {

    public PyrokinesisAbility(String id) {
        super(id, .75f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "red_priest", 7
        ));
    }

    @Override
    protected float getSpiritualityCost() {
        return 30;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.pyrokinesis.fireball",
                "ability.lotmcraft.pyrokinesis.flame_wave",
                "ability.lotmcraft.pyrokinesis.wall_of_fire",
                "ability.lotmcraft.pyrokinesis.fire_ravens",
                "ability.lotmcraft.pyrokinesis.flaming_spear"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch(abilityIndex) {
            case 0 -> fireball(level, entity);
            case 1 -> flameWave(level, entity);
            case 2 -> wallOfFire(level, entity);
            case 3 -> fireRavens(level, entity);
            case 4 -> flamingSpear(level, entity);
        }
    }

    private void flamingSpear(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(1, 2.85f), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, 50, 1.4f).subtract(startPos).normalize();

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);

        FlamingSpearProjectileEntity spear = new FlamingSpearProjectileEntity(level, entity, DamageLookup.lookupDamage(7, .8) * multiplier(entity), BeyonderData.isGriefingEnabled(entity));
        spear.setPos(startPos.x, startPos.y, startPos.z); // Set initial position
        spear.shoot(direction.x, direction.y, direction.z, 1.6f, 0);
        level.addFreshEntity(spear);

    }

    private void fireRavens(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        for(int i = 0; i < 8; i++) {
            Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), random.nextDouble(.5, 11f), random.nextDouble(-10.5, 10.5), random.nextDouble(.1, 9));

            LivingEntity target = AbilityUtil.getTargetEntity(entity, 40, 1.4f);
            FireRavenEntity fireRaven;
            if(target == null) {
                Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 40, 1.4f);
                fireRaven = new FireRavenEntity(level, targetPos, entity, DamageLookup.lookupDamage(7, .5) * multiplier(entity), BeyonderData.isGriefingEnabled(entity));
            }
            else {
                fireRaven = new FireRavenEntity(level, target, entity, DamageLookup.lookupDamage(7, .5) * multiplier(entity), BeyonderData.isGriefingEnabled(entity));
            }

            fireRaven.setInvulnerable(true);

            fireRaven.setPos(startPos);
            level.addFreshEntity(fireRaven);
        }
    }

    private void wallOfFire(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 10, 1.4f);

        Vec3 perpendicular = VectorUtil.getPerpendicularVector(entity.getLookAngle()).normalize();

        ServerScheduler.scheduleForDuration(0, 1, 20 * 20, () -> {
            if(random.nextInt(10) == 0)
                level.playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);


            for(int i = -1; i < 6; i++) {
                for(int j = -7; j < 8; j++) {
                    Vec3 pos = targetPos.add(perpendicular.scale(j)).add(0, i, 0);

                    ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.FLAME, pos, 1, 0.5, 0.02);
                    ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SMOKE, pos, 1, 0.5, 0.02);

                    AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 1f, DamageLookup.lookupDamage(7, .2) * multiplier(entity), pos, true, false, false, 15, 20 * 4);

                    for(LivingEntity target : AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, pos, 1f)) {
                        Vec3 knockback = target.position().subtract(pos).normalize().add(0, .2, 0).scale(0.8f);
                        target.setDeltaMovement(knockback);
                    }
                }
            }
        }, (ServerLevel) level);
    }

    //TODO: PLace flame blocks on griefing
    private void flameWave(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = entity.getEyePosition().add(0, .5, 0);

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);

        ServerScheduler.scheduleDelayed(18, () -> AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 5.5, DamageLookup.lookupDamage(7, .9) * multiplier(entity), entity.position().add(0, .2, 0), true, false, true, 0, 20 * 5));

        AtomicDouble i = new AtomicDouble(0.6);
        ServerScheduler.scheduleForDuration(0, 1, 24, () -> {
            double ySubtraction = 2 * ((1/((10 * i.get()) - 9)) - 1);
            Vec3 currentPos = startPos.add(0, ySubtraction, 0);
            double radius = i.get() < .71 ? i.get() : i.get() * 2;
            ParticleUtil.spawnCircleParticles((ServerLevel) level, ParticleTypes.FLAME, currentPos, radius, (int) (radius * 25));
            ParticleUtil.spawnCircleParticles((ServerLevel) level, ParticleTypes.SMOKE, currentPos, radius, (int) (radius * 6));
            i.set(i.get() + .1);
        }, (ServerLevel) level);
    }

    private void fireball(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(1, 2.85f), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, 50, 1.4f).subtract(startPos).normalize();

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);

        FireballEntity fireball = new FireballEntity(level, entity, DamageLookup.lookupDamage(7, .775) * multiplier(entity), BeyonderData.isGriefingEnabled(entity));
        fireball.setPos(startPos.x, startPos.y, startPos.z); // Set initial position
        fireball.shoot(direction.x, direction.y, direction.z, 1.2f, 0);
        level.addFreshEntity(fireball);
    }
}
