package de.jakob.lotm.beyonders.abilities.mother;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class UndergroundTravelAbility extends Ability {
    public UndergroundTravelAbility(String id) { super(id, 6); }
    @Override public Map<String, Integer> getRequirements() { return new HashMap<>(Map.of("mother", 5)); }
    @Override public float getSpiritualityCost() { return 180; }
    @Override public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Vec3 target = AbilityUtil.getTargetLocation(entity, 28, 1.5f, false); BlockPos safePos = findSafePosition(serverLevel, BlockPos.containing(target));
        if (safePos == null) { AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.underground_travel.invalid").withColor(0x8ed38f)); return; }
        burst(serverLevel, entity.blockPosition()); serverLevel.playSound(null, entity.blockPosition(), SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 1f, 0.8f);
        entity.teleportTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
        burst(serverLevel, safePos); serverLevel.playSound(null, safePos, SoundEvents.ROOTED_DIRT_BREAK, SoundSource.BLOCKS, 1f, 1.15f);
    }
    private BlockPos findSafePosition(ServerLevel level, BlockPos targetPos) {
        for (int y = 4; y >= -4; y--) { BlockPos pos = targetPos.offset(0, y, 0); BlockState feet = level.getBlockState(pos), head = level.getBlockState(pos.above()), ground = level.getBlockState(pos.below()); if (feet.getCollisionShape(level, pos).isEmpty() && head.getCollisionShape(level, pos.above()).isEmpty() && !ground.getCollisionShape(level, pos.below()).isEmpty()) return pos; }
        return null;
    }
    private void burst(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos.below()); if (state.isAir()) state = Blocks.DIRT.defaultBlockState();
        ParticleUtil.spawnParticles(level, new BlockParticleOption(ParticleTypes.BLOCK, state), Vec3.atCenterOf(pos), 28, 0.6, 0.35, 0.6, 0.08);
    }
}
