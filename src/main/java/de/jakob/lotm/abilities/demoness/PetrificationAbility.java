package de.jakob.lotm.abilities.demoness;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.checkerframework.checker.units.qual.A;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class PetrificationAbility extends AbilityItem {
    public PetrificationAbility(Properties properties) {
        super(properties, 5);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 500;
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(.35f, .35f, .35f), 3f);

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AtomicDouble radius = new AtomicDouble(.5);

        ServerScheduler.scheduleForDuration(0, 4, 90, () -> {
            for(double i = 0; i < radius.get(); i += .4) {
                ParticleUtil.spawnCircleParticles(serverLevel, dust, entity.position().add(0, .1, 0), i, (int) Math.round(9 * i));
            }

            radius.set(radius.get() + .5);
        });
    }
}
