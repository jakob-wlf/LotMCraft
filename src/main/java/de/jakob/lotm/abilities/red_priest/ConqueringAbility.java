package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.rendering.effectRendering.impl.BaptismEffect;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class ConqueringAbility extends Ability {
    public ConqueringAbility(String id) {
        super(id, 40, "morale_boost");
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 8000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 startPos = entity.position();

        level.playSound(null, BlockPos.containing(startPos), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 10, 1);
        level.playSound(null, BlockPos.containing(startPos), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 10, 1);

        EffectManager.playEffect(EffectManager.Effect.CONQUERING, entity.getX(), entity.getY(), entity.getZ(), serverLevel, entity);

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        double radius = 4*multiplier(entity);

        AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), radius, false).forEach(e -> {
            if(entitySeq < BeyonderData.getSequence(e)) {
                e.addEffect(new MobEffectInstance(ModEffects.CONQUERED, (int) (20 * 30 *multiplier(entity)), 18));
            }else if (entitySeq > BeyonderData.getSequence(e)){
                e.addEffect(new MobEffectInstance(ModEffects.CONQUERED, 20, 1));
            }else{
                e.addEffect(new MobEffectInstance(ModEffects.CONQUERED, (int) (35*multiplier(entity)/multiplier(e)), 2));
            };
        });
    }
}
