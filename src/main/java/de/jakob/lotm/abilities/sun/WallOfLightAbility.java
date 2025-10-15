package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WallOfLightAbility extends AbilityItem {
    public WallOfLightAbility(Properties properties) {
        super(properties, 5);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 800;
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(1f, 185 / 255f, 3 / 255f), 10f);


    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 12, 1.4f);

        Vec3 perpendicular = VectorUtil.getPerpendicularVector(entity.getLookAngle()).normalize();

        List<BlockPos> blocks = new ArrayList<>();
        for(int i = -2; i < 17; i++) {
            for(int j = -30; j < 31; j++) {
                Vec3 pos = targetPos.add(perpendicular.scale(j)).add(0, i, 0);
                blocks.add(BlockPos.containing(pos));
            }
        }

        for(BlockPos pos : blocks) {
            BlockState state = level.getBlockState(pos);
            if(state.getCollisionShape(level, pos).isEmpty()) {
                level.setBlockAndUpdate(pos, Blocks.BARRIER.defaultBlockState());
            }
        }

        ServerScheduler.scheduleForDuration(0, 7, 20 * 20, () -> {
            for(BlockPos pos : blocks) {
                if(random.nextBoolean())
                    ParticleUtil.spawnParticles((ServerLevel) level, random.nextBoolean() ? dust : ParticleTypes.END_ROD, pos.getCenter(), 1, 0.5, 0.02);

                AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 1.2f, 16 * multiplier(entity), pos.getCenter(), true, false, false, 15);

                for(LivingEntity target : AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, pos.getCenter(), 1f)) {
                    Vec3 knockback = target.position().subtract(pos.getCenter()).normalize().add(0, .2, 0).scale(1.4f);
                    target.setDeltaMovement(knockback);
                }
            }
        }, () -> {
            for(BlockPos pos : blocks) {
                BlockState state = level.getBlockState(pos);
                if(state.getBlock() == Blocks.BARRIER) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            }
        }, (ServerLevel) level);
    }
}
