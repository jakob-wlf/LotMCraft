package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class FlaringSunAbility extends AbilityItem {
    public FlaringSunAbility(Properties properties) {
        super(properties, 8);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 500;
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(1f, 185 / 255f, 3 / 255f), 3.5f);

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 25, 2);
        Vec3 startPos = targetPos.add(0, 3, 0);

        BlockPos blockPos = BlockPos.containing(startPos);
        BlockState state = level.getBlockState(blockPos);
        if(state.getCollisionShape(level, blockPos).isEmpty()) {
            level.setBlockAndUpdate(blockPos, Blocks.LIGHT.defaultBlockState());
        }

        ServerScheduler.scheduleForDuration(0, 4, 20 * 12, () -> {
            ParticleUtil.spawnSphereParticles((ServerLevel) level, dust, startPos, 4.5f, 90);
            ParticleUtil.spawnSphereParticles((ServerLevel) level, ParticleTypes.FLAME, startPos, 4.75f, 90);
            ParticleUtil.spawnSphereParticles((ServerLevel) level, ParticleTypes.END_ROD, startPos, 4.75f, 45);
        }, () -> {
            if(level.getBlockState(blockPos).getBlock() == Blocks.LIGHT) {
                level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
            }
        }, (ServerLevel) level);
    }
}
