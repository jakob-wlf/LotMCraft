package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ConqueringAbility extends Ability {
    public ConqueringAbility(String id) {
        super(id, 40, "morale_boost");
        canBeShared = false;

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(16000f, 8000f));

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(10, 25));
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

        EffectManager.playEffect(EffectIds.CONQUERING, entity.getX(), entity.getY(), entity.getZ(), serverLevel, entity);

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        double radius = 15;

        AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), radius, false).forEach(e -> {
            int targetSeq = BeyonderData.getSequence(e);

            if(!(BeyonderData.getPathway(e).equals("death") && entitySeq + 2 >= targetSeq)){
                if(entitySeq < targetSeq) {
                    e.addEffect(new MobEffectInstance(ModEffects.CONQUERED, (int) (20 * 15), 11));
                }
                else{
                    e.addEffect(new MobEffectInstance(ModEffects.CONQUERED, (int) (20 * 4), 5));
                }
            }
        });
    }
}
