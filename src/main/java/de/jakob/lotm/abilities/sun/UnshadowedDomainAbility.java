package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnshadowedDomainAbility extends AbilityItem {
    public UnshadowedDomainAbility(Properties properties) {
        super(properties, 50);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 600;
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(1f, 185 / 255f, 3 / 255f), 10f);

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = entity.position();

        List<BlockPos> blocks = AbilityUtil.getBlocksInSphereRadius((ServerLevel) level, startPos, 40, true, false, false).stream().filter(
                b -> {
                    BlockState state = level.getBlockState(b);
                    return state.isAir() &&
                            (!level.getBlockState(b.below()).isAir() ||
                                    !level.getBlockState(b.above()).isAir() ||
                                    !level.getBlockState(b.north()).isAir() ||
                                    !level.getBlockState(b.south()).isAir() ||
                                    !level.getBlockState(b.east()).isAir() ||
                                    !level.getBlockState(b.west()).isAir());
                }
        ).toList();

        blocks.forEach(b -> level.setBlockAndUpdate(b, Blocks.LIGHT.defaultBlockState()));

        ServerScheduler.scheduleForDuration(0, 10, 20 * 30, () -> {
            ParticleUtil.spawnParticles((ServerLevel) level, dust, startPos, 120, 25, 5, 25, 0);
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.END_ROD, startPos, 120, 25, 5, 25, 0);

            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) level, entity, 40, startPos, new MobEffectInstance(MobEffects.GLOWING, 20 * 2, 1, false, false, false));
            AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, startPos, 40)
                    .stream()
                    .filter(e -> (!BeyonderData.isBeyonder(e) || BeyonderData.getSequence(e) > BeyonderData.getSequence(entity)) && (e instanceof Mob || e instanceof Player))
                    .forEach(e -> e.hurt(e.damageSources().mobAttack(entity), (float) (DamageLookup.lookupDps(4, .4, 10, 20) * multiplier(entity))));
        }, () -> blocks.forEach(b -> {
            BlockState state = level.getBlockState(b);
            if(state.is(Blocks.LIGHT))
                level.setBlockAndUpdate(b, Blocks.AIR.defaultBlockState());
        }), (ServerLevel) level);
    }
}
