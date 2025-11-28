package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class LightOfHolinessAbility extends AbilityItem {
    public LightOfHolinessAbility(Properties properties) {
        super(properties, .9f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        Map<String, Integer> reqs = new HashMap<>();
        reqs.put("sun", 5);
        return reqs;
    }

    @Override
    protected float getSpiritualityCost() {
        return 50;
    }

    final int radius = 45;

    DustParticleOptions dustOptions = new DustParticleOptions(
            new Vector3f(255 / 255f, 180 / 255f, 66 / 255f),
            2.25f
    );

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        Vec3 initialPos = AbilityUtil.getTargetLocation(entity, radius, 1.5f, true).add(0, 18, 0);

        List<BlockPos> lights = new ArrayList<>();

        if (!level.isClientSide) {
            AtomicReference<Vec3> currentPos = new AtomicReference<>(initialPos);

            level.playSound(null, initialPos.x, initialPos.y - 18, initialPos.z, SoundEvents.BEACON_ACTIVATE, entity.getSoundSource(), 3.0f, 1.0f);

            EffectManager.playEffect(EffectManager.Effect.LIGHT_OF_HOLINESS, initialPos.x, initialPos.y - 18, initialPos.z, (ServerLevel) level);

            ServerScheduler.scheduleForDuration(0, 1, 22, () -> {
                Vec3 pos = currentPos.get();

                BlockPos blockPos = BlockPos.containing(pos);
                //Set the light blocks
                if (level.getBlockState(blockPos).isAir()) {
                    level.setBlockAndUpdate(blockPos, Blocks.LIGHT.defaultBlockState());
                    lights.add(blockPos);
                }

                AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 8f, DamageLookup.lookupDamage(5, .8) * multiplier(entity), pos, true, false, false, 10);

                currentPos.set(pos.subtract(0, 1, 0));
            }, (ServerLevel) level);

            ServerScheduler.scheduleDelayed(40, () -> {
                lights.forEach(l -> level.setBlockAndUpdate(l, Blocks.AIR.defaultBlockState()));
            }, (ServerLevel) level);
        }
    }
}
