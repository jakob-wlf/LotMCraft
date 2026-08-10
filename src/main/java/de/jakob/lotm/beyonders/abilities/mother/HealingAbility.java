package de.jakob.lotm.beyonders.abilities.mother;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HealingAbility extends SelectableAbility {
    public HealingAbility(String id) {
        super(id, 10);

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(1, 1, 2, 4, 5, 7, 8, 9, 10));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(4000f, 1700f, 1100f, 625f, 550f, 310f, 240f, 190f, 66f));

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

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        float restoredHealth = getAmount(entitySeq);

        for(LivingEntity e : AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 6, false, true)) {
            e.setHealth(Math.min(e.getMaxHealth(), e.getHealth() + restoredHealth));
            ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.HEALING.get(), e.getEyePosition().subtract(0, .3, 0), 35, .9);
        }
    }

    private void healYourself(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        float restoredHealth = getAmount(entitySeq);

        entity.setHealth(Math.min(entity.getMaxHealth(), entity.getHealth() + restoredHealth));

        ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.HEALING.get(), entity.getEyePosition().subtract(0, .3, 0), 35, .9);
    }

    private static int getAmount(int seq){
        return switch (seq){
            case 8 -> 10;
            case 7 -> 15;
            case 6 -> 20;
            case 5 -> 25;
            case 4 -> 30;
            case 3 -> 35;
            case 2 -> 40;
            case 1 -> 45;
            case 0 -> 55;
            default -> 0;
        };
    }

}