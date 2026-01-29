package de.jakob.lotm.abilities.abyss;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.entity.custom.FireballEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class FlameSpellsAbility extends SelectableAbility {

    public FlameSpellsAbility(String id) {
        super(id, .75f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "abyss", 6
        ));
    }

    @Override
    protected float getSpiritualityCost() {
        return 30;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.flame_spells.sulfur_fireball",
                "ability.lotmcraft.flame_spells.volcanic_eruption",
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch(abilityIndex) {
            case 0 -> fireball(level, entity);
            case 1 -> eruption(level, entity);
        }
    }

    private void eruption(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = entity.position();
        boolean griefing = BeyonderData.isGriefingEnabled(entity);

        level.playSound(null, BlockPos.containing(startPos), SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 3, 1);
        level.playSound(null, BlockPos.containing(startPos), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 3, 1);

        AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, startPos, 5).forEach(e -> {
            e.hurt(e.damageSources().mobAttack(entity), (float) (DamageLookup.lookupDamage(6, .875) * multiplier(entity)));
            e.setRemainingFireTicks(20 * 3);
            Vec3 knockBack = new Vec3(e.position().subtract(startPos).normalize().x, .75, e.position().subtract(startPos).normalize().z).normalize().scale(.5);
            e.setDeltaMovement(knockBack);
        });

        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.FLAME, startPos.add(0, 1, 0), 600, .75, 1.25, .75, .025);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.EXPLOSION, startPos.add(0, 1, 0), 20, .75, 1.25, .75, .025);

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

    private void fireball(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(1, 2.85f), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, 50, 1.4f).subtract(startPos).normalize();

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);

        FireballEntity fireball = new FireballEntity(level, entity, DamageLookup.lookupDamage(6, .75) * multiplier(entity), BeyonderData.isGriefingEnabled(entity));
        fireball.setPos(startPos.x, startPos.y, startPos.z); // Set initial position
        fireball.shoot(direction.x, direction.y, direction.z, 1.2f, 0);
        level.addFreshEntity(fireball);
    }
}
