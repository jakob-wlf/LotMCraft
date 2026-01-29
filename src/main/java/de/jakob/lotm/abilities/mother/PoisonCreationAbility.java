package de.jakob.lotm.abilities.mother;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PoisonCreationAbility extends SelectableAbility {
    public PoisonCreationAbility(String id) {
        super(id, 3);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 250;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.poison_creation.area", "ability.lotmcraft.poison_creation.threads"};
    }

    private final DustParticleOptions dustBig = new DustParticleOptions(new Vector3f(112 / 255f, 212 / 255f, 130 / 255f), 5);
    private final DustParticleOptions dustSmall = new DustParticleOptions(new Vector3f(112 / 255f, 212 / 255f, 130 / 255f), 1);

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        switch (abilityIndex) {
            case 0 -> createPoisonArea(serverLevel, entity);
            case 1 -> createPoisonThreads(serverLevel, entity);
        }
    }

    private void createPoisonThreads(ServerLevel serverLevel, LivingEntity entity) {
        for(int i = 0; i < 8; i++) {
            Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(),  random.nextDouble(-4.5, 3f), random.nextDouble(-7, 7), random.nextDouble(-2, 5));
            Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 16, 1.4f);

            final float step = .15f;
            final float length = (float) startPos.distanceTo(targetLoc);
            final int duration = (int) Math.ceil(length / step) + 20 * 3;

            animateParticleLine(new Location(startPos, serverLevel), targetLoc, 2, 0, duration);
        }

        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 16, 2);
        if(targetEntity == null)
            return;

        targetEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 5, 8));
        targetEntity.hurt(serverLevel.damageSources().generic(), (float) (DamageLookup.lookupDamage(6, .8) * multiplier(entity)));
    }

    private void animateParticleLine(Location startLoc, Vec3 end, int step, int interval, int duration) {
        if(!(startLoc.getLevel() instanceof ServerLevel level))
            return;
        AtomicInteger tick = new AtomicInteger(0);

        float distance = (float) end.distanceTo(startLoc.getPosition());
        float bezierSteps = .15f / distance;

        int maxPoints = Math.max(2, Math.min(10, (int) Math.ceil(distance * 1.5)));

        List<Vec3> points = VectorUtil.createBezierCurve(startLoc.getPosition(), end, bezierSteps, random.nextInt(1, maxPoints + 1));

        ServerScheduler.scheduleForDuration(0, interval, duration, () -> {
            for(int i = 0; i < Math.min(tick.get(), points.size() - step); i+=step) {
                for(int j = 0; j < step; j++) {
                    ParticleUtil.spawnParticles(level, dustSmall, points.get(i + j), 1, 0, 0);
                }
            }

            tick.addAndGet(1);
        });
    }

    private void createPoisonArea(ServerLevel serverLevel, LivingEntity entity) {
        AtomicDouble radius = new AtomicDouble(0);
        Vec3 startPos = entity.position().add(0, 0.185, 0);
        ServerScheduler.scheduleForDuration(0, 2, 20 * 5, () -> {
            radius.addAndGet(0.5);
            ParticleUtil.spawnParticles(serverLevel, dustBig, startPos, (int) (radius.get() * 12), radius.get(), 0.1, radius.get(), 0);
            AbilityUtil.damageNearbyEntities(serverLevel, entity, radius.get(), DamageLookup.lookupDps(6, .95, 2, 20) * multiplier(entity), startPos, true, false);
            AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, entity, radius.get(), startPos, new MobEffectInstance(MobEffects.POISON, 20 * 5, 8));
        });
    }
}
