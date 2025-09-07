package de.jakob.lotm.abilities.darkness;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class RequiemAbility extends AbilityItem {
    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(250 / 255f, 40 / 255f, 64 / 255f), .5f);

    public RequiemAbility(Properties properties) {
        super(properties, 1);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 45;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        for(int i = 0; i < 8; i++) {
            Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(),  random.nextDouble(-3, 3f), random.nextDouble(-5, 5), random.nextDouble(-1, 3));
            Vec3 direction = AbilityUtil.getTargetLocation(entity, 50, 1.4f).subtract(startPos).normalize();

            animateParticleLine(new Location(startPos, level), direction, 1, .1f, 20, 20 * 20);
        }
    }

    private void animateParticleLine(Location startLoc, Vec3 direction, int interval, float step, double length, int duration) {
        if(!(startLoc.getLevel() instanceof ServerLevel level))
            return;
        AtomicInteger tick = new AtomicInteger(0);

        ServerScheduler.scheduleForDuration(0, interval, duration, () -> {
            for(float i = 0; i <= (length); i+= step) {
                if(tick.get() * step < i)
                    continue;

                float amplitude = (float) (.15f * Math.sin(2 * Math.PI * i));
                Vec3 pos = startLoc.getPosition().add(direction.scale(i)).add(0, amplitude, 0);
                ParticleUtil.spawnParticles(level, dust, pos, 1, 0, 0);
            }

            tick.addAndGet(1);
        });
    }


}
