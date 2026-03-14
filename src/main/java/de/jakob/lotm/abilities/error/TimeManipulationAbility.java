package de.jakob.lotm.abilities.error;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.TimeChangeEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class TimeManipulationAbility extends SelectableAbility {
    public TimeManipulationAbility(String id) {
        super(id, 17);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1200;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.time_manipulation.stop_time", "ability.lotmcraft.time_manipulation.accelerate_time", "ability.lotmcraft.time_manipulation.slow_time"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(level.isClientSide)
            return;

        level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1f, 1f);

        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.ENCHANT, entity.getEyePosition(), 400, 10, 2, 10, 0.05);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.END_ROD, entity.getEyePosition(), 100, 10, 2, 10, 0.05);

        float timeMultiplier = selectedAbility == 0 ? 0.001f : (selectedAbility == 1 ? 4f : 0.2f);
        TimeChangeEntity timeChangeEntity = new TimeChangeEntity(ModEntities.TIME_CHANGE.get(), level, 20 * 15, entity.getUUID(), 50, timeMultiplier);
        timeChangeEntity.setPos(entity.getX(), entity.getY(), entity.getZ());
        level.addFreshEntity(timeChangeEntity);
    }
}
