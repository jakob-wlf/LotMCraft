package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class MutationCreationAbility extends AbilityItem {
    public MutationCreationAbility(Properties properties) {
        super(properties, 4);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 800;
    }

    private final DustParticleOptions dustGreen = new DustParticleOptions(
            new Vector3f(68 / 255f, 110 / 255f, 76 / 255f),
            2
    );

    private final DustParticleOptions dustWhite = new DustParticleOptions(
            new Vector3f(138 / 255f, 189 / 255f, 147 / 255f),
            2
    );

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);
        if(target == null) return;

        float targetHeight = target.getBbHeight();
        Vec3 targetPos = target.position().add(0, targetHeight / 2, 0);
        ParticleUtil.spawnParticles(serverLevel, dustGreen, targetPos, 200, .5, targetHeight / 2, .5, 0);
        ParticleUtil.spawnParticles(serverLevel, dustWhite, targetPos, 200, .5, targetHeight / 2, .5, 0);

        if(!target.hasEffect(ModEffects.MUTATED)) {
            int duration = getDuration(entity, target);
            target.addEffect(new MobEffectInstance(ModEffects.MUTATED, duration, (int) (2 * multiplier(entity))));
        }
        else {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.mutation_creation.already_afflicted").withColor(0x8abd93));
        }

    }

    private int getDuration(LivingEntity entity, LivingEntity target) {
        if(!BeyonderData.isBeyonder(entity) || !BeyonderData.isBeyonder(target) || AbilityUtil.isTargetSignificantlyWeaker(entity, target)) {
            return 20 * 60;
        }
        if(AbilityUtil.isTargetSignificantlyStronger(entity, target)) {
            return 20 * 5;
        }
        return 20 * 30;
    }
}
