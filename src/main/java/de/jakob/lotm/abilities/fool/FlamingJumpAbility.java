package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

public class FlamingJumpAbility extends AbilityItem {
    public FlamingJumpAbility(Properties properties) {
        super(properties, .05f);

        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 7));
    }

    @Override
    protected float getSpiritualityCost() {
        return 12;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        BlockPos block = AbilityUtil.getTargetBlock(entity, 50, false);

        if(!level.getBlockState(block).is(Blocks.FIRE))
            return;

        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 3, 1, false, false, false));
        entity.teleportTo(block.getCenter().x, block.getCenter().y + .75, block.getCenter().z);

        level.playSound(null, block, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 1, 1);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.FLAME, block.getCenter().add(0, .8, 0), 60, .3, .8, .3, .05);
    }

    @Override
    public void onHold(Level level, LivingEntity entity) {
        if(!level.isClientSide)
            return;

        BlockPos block = AbilityUtil.getTargetBlock(entity, 50, false);

        if(!level.getBlockState(block).is(Blocks.FIRE))
            return;

        ParticleUtil.spawnParticles((ClientLevel) level, ParticleTypes.FLASH, block.getCenter(), 20, .1, 0);
    }
}
