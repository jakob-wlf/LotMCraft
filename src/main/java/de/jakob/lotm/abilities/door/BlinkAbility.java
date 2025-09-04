package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class BlinkAbility extends AbilityItem {
    public BlinkAbility(Properties properties) {
        super(properties, .01f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 20;
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(.5f, .78f, .93f),
            1.75f
    );

    private final DustParticleOptions dust2 = new DustParticleOptions(
            new Vector3f(.8f, .34f, .93f),
            1.75f
    );

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetLoc = AbilityUtil.getTargetBlock(entity, 8, true).getCenter().add(0, 1, 0);
        level.playSound(null, targetLoc.x, targetLoc.y, targetLoc.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, .5f, 1);

        entity.teleportTo(targetLoc.x, targetLoc.y, targetLoc.z);
        ParticleUtil.spawnParticles((ServerLevel) level, dust, targetLoc.add(0, .5, 0), 30, .4, 1, .4, 0);
        ParticleUtil.spawnParticles((ServerLevel) level, dust2, targetLoc.add(0, .5, 0), 30, .4, 1, .4, 0);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.END_ROD, targetLoc.add(0, .5, 0), 30, .4, 1, .4, 0);
    }
}
