package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.RingExpansionRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

//TODO: Rework effects using geckolib
public class CleansingAbility extends SelectableAbilityItem {
    public CleansingAbility(Properties properties) {
        super(properties, 2);
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
        return new String[]{"ability.lotmcraft.cleansing.self", "ability.lotmcraft.cleansing.others"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(entity instanceof Player))
            abilityIndex = 0;
        switch(abilityIndex) {
            case 0 -> cleanseYourself(level, entity);
            case 1 -> cleanseOthers(level, entity);
        }
    }

    private void cleanseOthers(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        RingExpansionRenderer.createRingForAll(entity.getEyePosition().subtract(0, .4, 0), 8, 60, 122 / 255f, 235 / 255f, 124 / 255f, 1, 1f, .75f, (ServerLevel) level);

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);

        for(LivingEntity e : AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 6)) {
            e.setRemainingFireTicks(0);
            for(MobEffectInstance effect : e.getActiveEffects()) {
                if(effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                    e.removeEffect(effect.getEffect());
            }

            if(e instanceof Player player) {
                player.getFoodData().setSaturation(20);
                player.getFoodData().setFoodLevel(20);
            }
        }
    }

    private void cleanseYourself(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        RingExpansionRenderer.createRingForAll(entity.getEyePosition().subtract(0, .4, 0), 2, 60, 122 / 255f, 235 / 255f, 124 / 255f, 1, .5f, .75f, (ServerLevel) level);

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);

        entity.setRemainingFireTicks(0);
        for(MobEffectInstance effect : entity.getActiveEffects()) {
            if(effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                entity.removeEffect(effect.getEffect());
        }

        if(entity instanceof Player player) {
            player.getFoodData().setSaturation(20);
            player.getFoodData().setFoodLevel(20);
        }
    }

}