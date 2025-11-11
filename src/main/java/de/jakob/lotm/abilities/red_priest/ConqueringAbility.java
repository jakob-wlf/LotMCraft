package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
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

public class ConqueringAbility extends AbilityItem {
    public ConqueringAbility(Properties properties) {
        super(properties, 1.5f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1900;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 startPos = entity.position();

        level.playSound(null, BlockPos.containing(startPos), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 10, 1);
        level.playSound(null, BlockPos.containing(startPos), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 10, 1);

        EffectManager.playEffect(EffectManager.Effect.CONQUERING, entity.getX(), entity.getY(), entity.getZ(), serverLevel);

        AbilityUtil.getNearbyEntities(entity, serverLevel, startPos, 40).forEach(e -> e.addEffect(new MobEffectInstance(ModEffects.CONQUERED, 20 * 60 * 30, 9, false, false, false)));
    }
}
