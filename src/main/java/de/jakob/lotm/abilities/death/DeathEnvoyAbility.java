package de.jakob.lotm.abilities.death;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class DeathEnvoyAbility extends Ability {

    private static final int RADIUS = 5;
    private static final int DURATION = 20 * 20;
    private static final int SPIRIT_CALLED_DURATION = 20 * 10;

    private static final DustParticleOptions SOUL_DUST =
            new DustParticleOptions(new Vector3f(0.15f, 0.85f, 0.75f), 1.8f);
    private static final DustParticleOptions DARK_DUST =
            new DustParticleOptions(new Vector3f(0.05f, 0.0f, 0.2f), 1.4f);

    public DeathEnvoyAbility(String id) {
        super(id, 10);
        canBeCopied = false;
        cannotBeStolen = true;
        canBeUsedInArtifact = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 800;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(entity.position(), serverLevel), "purification", BeyonderData.getSequence(entity), -1)) return;

        Vec3 center = entity.position().add(0, 0.5, 0);

        level.playSound(null, entity.blockPosition(),
                SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 3.0f, 0.5f);
        level.playSound(null, entity.blockPosition(),
                SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 2.5f, 0.7f);

        ParticleUtil.spawnCircleParticles(serverLevel, SOUL_DUST,
                center, RADIUS, 120);
        ParticleUtil.spawnCircleParticles(serverLevel, DARK_DUST,
                center, RADIUS * 0.6, 100);
        ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.SOUL,
                center, RADIUS, 1000);

        for (int i = 0; i < 60; i++) {
            double ox = (random.nextDouble() - 0.5) * 2;
            double oy = random.nextDouble() * 2;
            double oz = (random.nextDouble() - 0.5) * 2;
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    center.x + ox, center.y + oy, center.z + oz,
                    1, 0, 0.05, 0, 0.02);
        }

        int casterSeq = BeyonderData.getSequence(entity);

        for (LivingEntity target : AbilityUtil.getNearbyEntities(entity, serverLevel, center, RADIUS)) {
            int targetSeq = BeyonderData.getSequence(target);
            if (targetSeq - casterSeq <= -2) continue;

            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
                    DURATION, 1, false, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    DURATION, 2, false, false, false));
            target.setTicksFrozen(target.getTicksRequiredToFreeze() + DURATION);
            target.addEffect(new MobEffectInstance(ModEffects.SPIRIT_CALLED,
                    SPIRIT_CALLED_DURATION, 2, false, false, false));
        }
    }
}
