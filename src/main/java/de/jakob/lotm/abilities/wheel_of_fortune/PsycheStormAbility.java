package de.jakob.lotm.abilities.wheel_of_fortune;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class PsycheStormAbility extends AbilityItem {
    public PsycheStormAbility(Properties properties) {
        super(properties, 4);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 80;
    }

    private static final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(192 / 255f, 246 / 255f, 252 / 255f),
            3.5f
    );

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AbilityUtil.damageNearbyEntities(serverLevel, entity, 10, DamageLookup.lookupDamage(6, .875) * multiplier(entity), entity.getEyePosition(), true, false);
        AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, entity, 10, entity.getEyePosition(), new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 7, random.nextInt(1) + 3));

        Location loc = new Location(entity.position(), serverLevel);

        ParticleUtil.createExpandingParticleSpirals(dust, loc, 3, 4, 2, .5, 4, 90, 7, 2);
        ParticleUtil.createExpandingParticleSpirals(dust, loc, 5, 6, 2, .5, 4, 90, 7, 2);
        ParticleUtil.createExpandingParticleSpirals(dust, loc, 7, 8, 2, .5, 4, 90, 7, 2);
        ParticleUtil.createExpandingParticleSpirals(dust, loc, 9, 10, 2, .5, 4, 90, 7, 2);
        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, entity.getEyePosition(), 150, 7, 3, 7, 0);

        serverLevel.playSound(null, BlockPos.containing(loc.getPosition()), SoundEvents.BREEZE_DEATH, SoundSource.BLOCKS);

    }
}
