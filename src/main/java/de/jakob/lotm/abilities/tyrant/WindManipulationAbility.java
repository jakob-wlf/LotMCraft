package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.entity.custom.WindBladeEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.data.LocationSupplier;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.particle.SuspendedParticle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class WindManipulationAbility extends SelectableAbilityItem {
    private final HashSet<UUID> isFlying = new HashSet<>();

    public WindManipulationAbility(Properties properties) {
        super(properties, .8f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 30;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.wind_manipulation.wind_blade",
                "ability.lotmcraft.wind_manipulation.binding",
                "ability.lotmcraft.wind_manipulation.flight",
                "ability.lotmcraft.wind_manipulation.boost"
        };
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(entity instanceof Player) && abilityIndex == 2) {
            abilityIndex = 0;
        }

        switch (abilityIndex) {
            case 0 -> windBlade(level, entity);
            case 1 -> binding(level, entity);
            case 2 -> flight(level, entity);
            case 3 -> boost(level, entity);
        }
    }

    private void flight(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(!(entity instanceof Player player))
            return;

        if(isFlying.contains(player.getUUID())) {
            isFlying.remove(player.getUUID());
            return;
        }

        isFlying.add(player.getUUID());

        Location supplier = new Location(entity.position(), entity.level());

        List<AtomicBoolean> canceled = ParticleUtil.createParticleSpirals(ParticleTypes.EFFECT, supplier, .25, 1.35, entity.getEyeHeight(), .75, 8, 20 * 60 * 12, 18, 2);
        AtomicBoolean shouldStop = new AtomicBoolean(false);
        ServerScheduler.scheduleForDuration(0, 1, 20 * 60 * 12, () -> {
            if(shouldStop.get()) {
                return;
            }

            if(!isFlying.contains(player.getUUID())) {
                canceled.forEach(b -> b.set(true));
                shouldStop.set(true);
                return;
            }

            if(BeyonderData.getSpirituality(player) < 3) {
                isFlying.remove(player.getUUID());
                return;
            }

            BeyonderData.reduceSpirituality(player, 3);

            if(player.isShiftKeyDown())
                player.setDeltaMovement(new Vec3(0, 0, 0));
            else
                player.setDeltaMovement(player.getLookAngle().normalize().multiply(.4, .4, .4));
            player.resetFallDistance();
            player.hurtMarked = true;

            if(!player.level().isClientSide) {
                supplier.setPosition(player.position());
                supplier.setLevel(player.level());
            }

        }, (ServerLevel) level);
    }

    private void binding(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 25, 1.5f, true);
        ParticleUtil.createParticleSpirals((ServerLevel) level, ParticleTypes.EFFECT, targetPos, 2, 2, 2.5, .5, 8, 20 * 13, 15, 10);

        ServerScheduler.scheduleForDuration(0, 1, 20 * 13, () -> {
            for(LivingEntity e : AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, targetPos, 2.5)) {
                e.setDeltaMovement(new Vec3(0, 0, 0));
                e.hurtMarked = true;
            }
        }, (ServerLevel) level);
    }

    private void boost(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(AbilityUtil.distanceToGround(level, entity) > 3.5) {
            return;
        }

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BREEZE_WIND_CHARGE_BURST, entity.getSoundSource(), 1.0f, .1f);

        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.CLOUD, entity.position(), 45, .5, .25);

        Vec3 dir = entity.getLookAngle().normalize();
        dir = dir.multiply(2, 3, 2);
        entity.setDeltaMovement(dir);

        entity.hurtMarked = true;


    }

    private void windBlade(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(1, 2.85f), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, 50, 1.4f).subtract(startPos).normalize();

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BREEZE_WIND_CHARGE_BURST, entity.getSoundSource(), 1.0f, 1.0f);

        WindBladeEntity blade = new WindBladeEntity(level, entity, 19 * multiplier(entity), BeyonderData.isGriefingEnabled(entity));
        blade.setPos(startPos.x, startPos.y, startPos.z); // Set initial position
        blade.shoot(direction.x, direction.y, direction.z, 1.2f, 0);
        level.addFreshEntity(blade);
    }
}
