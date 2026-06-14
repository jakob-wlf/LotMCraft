package de.jakob.lotm.beyonders.abilities.darkness;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.level.Level;

import java.util.HashMap;

import java.util.Map;

public class LesserConcealmentAbility extends SelectableAbility {
    public LesserConcealmentAbility(String id) {
        super(id, 4f);
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1500;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[] {
                "ability.lotmcraft.minor_concealment.conceal_self",
                "ability.lotmcraft.minor_concealment.conceal_target"

        };
    }


    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch (abilityIndex) {
            case 0 -> conceal_self(level, entity);
            case 1 -> conceal_target(level, entity);

        }
    }

    private void conceal_self(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        if(!(entity instanceof ServerPlayer player)) return;
        int durationTicks =300*20*(10-BeyonderData.getSequence(entity));
        int totalPower= 10-BeyonderData.getSequence(entity);
        if (entity.hasEffect(ModEffects.CONCEALMENT)) {
            entity.removeEffect(ModEffects.CONCEALMENT);
        }else{
            entity.addEffect(new MobEffectInstance(ModEffects.CONCEALMENT, durationTicks, totalPower, false, false));
        };
        level.playSound(null,
                player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS,
                10.0f,
                1.0f);
    }

    private void conceal_target(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        if(!(entity instanceof ServerPlayer player)) return;

        level.playSound(null,
                player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS,
                10.0f,
                1.0f);
            LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 16, 2);
            if(targetEntity == null){
                return;
            }
            int durationTicks =300*20*(10-BeyonderData.getSequence(entity));
            int totalPower= 10-BeyonderData.getSequence(entity);
            if (targetEntity.hasEffect(ModEffects.CONCEALMENT)) {
                targetEntity.removeEffect(ModEffects.CONCEALMENT);
            }else{
                targetEntity.addEffect(new MobEffectInstance(ModEffects.CONCEALMENT, durationTicks, totalPower, false, false));
            };
        };
};

