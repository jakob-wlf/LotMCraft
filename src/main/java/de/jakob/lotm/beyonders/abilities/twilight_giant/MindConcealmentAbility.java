package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MindConcealmentAbility extends ToggleAbility {

    public static final int CONCEALMENT_POWER = 14;

    public MindConcealmentAbility(String id) {
        super(id);
        tickRate = 20;
        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(160f, 120f, 80f, 60f, 40f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 40;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level instanceof ServerLevel serverLevel) {
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.WHITE_ASH, entity.getEyePosition(), 30, 0.4, 0.4, 0.4, 0.01);
        }
        level.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, entity.getSoundSource(), 1f, 0.6f);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        level.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, entity.getSoundSource(), 1f, 0.6f);
    }

    public static boolean isActive(LivingEntity entity) {
        return ToggleAbility.getActiveAbilitiesForEntity(entity).stream().anyMatch(ability -> ability instanceof MindConcealmentAbility);
    }

    public static boolean isHiddenFrom(LivingEntity observer, LivingEntity target) {
        return isActive(target) && BeyonderData.getSequence(observer) >= BeyonderData.getSequence(target);
    }
}
