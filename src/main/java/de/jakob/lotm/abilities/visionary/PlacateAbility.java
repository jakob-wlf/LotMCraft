package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class PlacateAbility extends SelectableAbilityItem {
    public PlacateAbility(Properties properties) {
        super(properties, 3);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 7));
    }

    @Override
    protected float getSpiritualityCost() {
        return 50;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.placate.self", "ability.lotmcraft.placate.others"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(entity instanceof Player))
            abilityIndex = 0;
        switch (abilityIndex) {
            case 0 -> placateYourself(level, entity);
            case 1 -> placateOthers(level, entity);
        }
    }

    private void placateOthers(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        RingEffectManager.createRingForAll(entity.getEyePosition().subtract(0, .4, 0), 8, 60, 255 / 255f, 211 / 255f, 92 / 255f, 1, 1f, .75f, (ServerLevel) level);

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);

        for(LivingEntity e : AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 18)) {
            RingEffectManager.createRingForAll(e.getEyePosition().subtract(0, .4, 0), 2, 60, 255 / 255f, 211 / 255f, 92 / 255f, 1, .5f, .75f, (ServerLevel) level);

            placateEntity(e);
        }
    }

    private void placateYourself(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        RingEffectManager.createRingForAll(entity.getEyePosition().subtract(0, .4, 0), 2, 60, 255 / 255f, 211 / 255f, 92 / 255f, 1, .5f, .75f, (ServerLevel) level);

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);

        placateEntity(entity);


    }

    private void placateEntity(LivingEntity entity) {
        entity.removeEffect(ModEffects.LOOSING_CONTROL);

        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 4, false, false, false));
    }

    @Override
    public boolean shouldUseAbility(LivingEntity entity) {
        return entity.hasEffect(ModEffects.LOOSING_CONTROL);
    }
}
