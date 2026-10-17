package de.jakob.lotm.beyonders.abilities.mother;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//TODO: Rework effects using geckolib
public class CleansingAbility extends SelectableAbility {
    public CleansingAbility(String id) {
        super(id, 14, "cleansing");

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(1, 1, 2, 4, 5, 7, 8, 9, 10));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(1200f, 800f, 470f, 280f, 260f, 160f, 134f, 100f, 66f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 8));
    }

    @Override
    public float getSpiritualityCost() {
        return 25;
    }

    @Override
    public String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.cleansing.self",
                "ability.lotmcraft.cleansing.others"
        };
    }

    @Override
    public void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
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

        RingEffectManager.createRingForAll(entity.getEyePosition().subtract(0, .4, 0), 8, 60, 122 / 255f, 235 / 255f, 124 / 255f, 1, 1f, .75f, (ServerLevel) level);

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);

        for(LivingEntity e : AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 6, false, true)) {
            cleanEntity(e);
        }
    }

    private void cleanseYourself(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        RingEffectManager.createRingForAll(entity.getEyePosition().subtract(0, .4, 0), 2, 60, 122 / 255f, 235 / 255f, 124 / 255f, 1, .5f, .75f, (ServerLevel) level);

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);
        
        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);

        cleanEntity(entity);
    }

    private void cleanEntity(LivingEntity entity) {
        if (entity == null || entity.isRemoved()) {
            return;
        }

        entity.setRemainingFireTicks(0);

        List<MobEffectInstance> buff = new LinkedList<>();

        for(var effect : entity.getActiveEffects()){
            if(effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL &&
            !effect.equals(ModEffects.LOOSING_CONTROL))
                buff.add(effect);

        }

        for (var effect : buff) {
            entity.removeEffect(effect.getEffect());
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int value = getValue(entitySeq);

        if (entity instanceof Player player) {
            var foodData = player.getFoodData();

            player.getFoodData().setSaturation(foodData.getSaturationLevel() + value);
            player.getFoodData().setFoodLevel(foodData.getFoodLevel() + value);
        }
    }

    private static int getValue(int seq){
        return switch (seq){
          case 9, 8 -> 3;
          case 7,6 -> 5;
          case 5 -> 6;
          case 4 -> 10;
          case 3 -> 12;
          case 2, 1, 0 -> 20;
            default -> 0;
        };
    }
}