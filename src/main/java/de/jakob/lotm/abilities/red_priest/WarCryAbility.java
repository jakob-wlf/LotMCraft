package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class WarCryAbility extends Ability {
    public WarCryAbility(String id) {
        super(id, 3);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 350;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = entity.position();
        boolean griefing = BeyonderData.isGriefingEnabled(entity);

        level.playSound(null, BlockPos.containing(startPos), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 3, 1);

        AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, startPos, 19).forEach(e -> {
            e.hurt(e.damageSources().mobAttack(entity), (float) (DamageLookup.lookupDamage(3, .65) * multiplier(entity)));
            Vec3 knockBack = new Vec3(e.position().subtract(startPos).normalize().x, .75, e.position().subtract(startPos).normalize().z).normalize().scale(1.5);
            e.setDeltaMovement(knockBack);
        });

        BeyonderData.addModifier(entity, "war_cry", 1.5f);
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 12, 3, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 12, 4, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 12, 3, false, false, false));

        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.CLOUD, startPos.add(0, 1, 0), 600, .75, .75, .75, .15);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.CLOUD, startPos, 600, 7, .2, 7, .005);

        AbilityUtil.getBlocksInCircleOutline((ServerLevel) level, startPos.subtract(0, 1, 0), 5).forEach(b -> {
            spawnFallingBlocks(level, startPos, b, griefing);
        });
        AbilityUtil.getBlocksInCircleOutline((ServerLevel) level, startPos.subtract(0, 1, 0), 3).forEach(b -> {
            spawnFallingBlocks(level, startPos, b, griefing);
        });
    }

    private void spawnFallingBlocks(Level level, Vec3 startPos, BlockPos b, boolean griefing) {
        if(random.nextInt(2) != 0)
            return;

        BlockState state = level.getBlockState(b);
        BlockState above = level.getBlockState(b.above());
        if(state.getCollisionShape(level, b).isEmpty() || !above.getCollisionShape(level, b.above()).isEmpty())
            return;

        Vec3 vectorFromCenter = new Vec3(b.getX() + 0.5 - startPos.x, 0, b.getZ() + 0.5 - startPos.z).normalize();
        Vec3 movement = (new Vec3(vectorFromCenter.x, 1, vectorFromCenter.z)).normalize().scale(.75);

        FallingBlockEntity block = FallingBlockEntity.fall(level, b.above(), state);
        block.setDeltaMovement(movement);
        if(!griefing)
            block.disableDrop();
        else {
            level.setBlockAndUpdate(b, Blocks.AIR.defaultBlockState());
        }
        block.hurtMarked = true;
    }
}
