package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class ToxicSmokeAbility extends Ability {
    public ToxicSmokeAbility(String id) {
        super(id, 5.5f);

        hasOptimalDistance = true;
        optimalDistance = 7;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "abyss", 8
        ));
    }

    @Override
    public float getSpiritualityCost() {
        return 22;
    }

    private final DustParticleOptions dustOptions = new DustParticleOptions(new Vector3f(35 / 255f, 168 / 255f, 102 / 255f), 3f);

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 pos = entity.getEyePosition();

        ServerScheduler.scheduleForDuration(0, 6, 20 * 5, () -> {
            // Toxic smoke is completely cancelled by purification
            Location smokeLoc = new Location(pos, level);
            int seq = BeyonderData.getSequence(entity);
            if(InteractionHandler.isInteractionPossible(smokeLoc, "purification", seq))
                return;

            AbilityUtil.damageNearbyEntities((ServerLevel) level,
                    entity,
                    6.5,
                    DamageLookup.lookupDps(8, .8, 6, 20) * multiplier(entity),
                    pos,
                    true,
                    false,
                    true,
                    0,
                    ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity)
            );
            ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.TOXIC_SMOKE.get(), pos, 25, 3, .01);
            ParticleUtil.spawnParticles((ServerLevel) level, dustOptions, pos, 25, 3, .01);
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SMOKE, pos, 25, 3, .01);
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) level, entity, 5, pos,
                    new MobEffectInstance(MobEffects.POISON, 30, 1),
                    new MobEffectInstance(MobEffects.BLINDNESS, 10, 0));
            }, null, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(pos, level)));
    }
}
