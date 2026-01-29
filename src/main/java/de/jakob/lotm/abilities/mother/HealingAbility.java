package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class HealingAbility extends SelectableAbility {
    public HealingAbility(String id) {
        super(id, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 8));
    }

    @Override
    protected float getSpiritualityCost() {
        return 25;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.healing.self", "ability.lotmcraft.healing.others"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(entity instanceof Player))
            abilityIndex = 0;

        switch(abilityIndex) {
            case 0 -> healYourself(level, entity);
            case 1 -> healOthers(level, entity);
        }
    }

    private void healOthers(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);

        float restoredHealth = entity.getMaxHealth() / 3f;

        for(LivingEntity e : AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 6)) {
            e.setHealth(Math.max(e.getMaxHealth(), e.getHealth() + restoredHealth));
            ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.HEALING.get(), e.getEyePosition().subtract(0, .3, 0), 35, .9);
        }
    }

    private void healYourself(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);

        float restoredHealth = entity.getMaxHealth() / 3f;
        entity.setHealth(Math.max(entity.getMaxHealth(), entity.getHealth() + restoredHealth));

        ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.HEALING.get(), entity.getEyePosition().subtract(0, .3, 0), 35, .9);
    }

}