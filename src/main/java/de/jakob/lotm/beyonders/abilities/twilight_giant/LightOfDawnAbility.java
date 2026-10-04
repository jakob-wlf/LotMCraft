package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LightOfDawnAbility extends Ability {

    private static final int RADIUS = 45;
    private static final int DURATION = 20 * 30;
    private static final int LIGHT_SPACING = 9;
    private static final int DEBUFF_AMPLIFIER = 1;
    private static final int DEBUFF_REFRESH = 40;
    private static final DustParticleOptions DAWN_DUST = new DustParticleOptions(new Vector3f(1f, 0.97f, 0.9f), 2.5f);

    public LightOfDawnAbility(String id) {
        super(id, 35, "purification", "light_source", "light_strong");
        interactionRadius = RADIUS;
        interactionCacheTicks = DURATION;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 350;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 center = entity.position();
        List<BlockPos> lights = placeLights(serverLevel, entity.blockPosition());

        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE, entity.getSoundSource(), 3f, 1.4f);
        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.FLASH, center.add(0, 1, 0), 1, 0, 0);
        for (int ring = 1; ring <= 5; ring++) {
            double radius = RADIUS * ring / 5.0;
            ServerScheduler.scheduleDelayed(ring * 2, () -> {
                ParticleUtil.spawnCircleParticles(serverLevel, ParticleTypes.END_ROD, center.add(0, 1, 0), radius, (int) (radius * 6));
                ParticleUtil.spawnCircleParticles(serverLevel, DAWN_DUST, center.add(0, 1.5, 0), radius, (int) (radius * 4));
            }, serverLevel);
        }

        ServerScheduler.scheduleForDuration(0, 20, DURATION, () -> {
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, center.add(0, 6, 0), 60, RADIUS / 2.0, 4, RADIUS / 2.0, 0.01);
            for (LivingEntity target : AbilityUtil.getNearbyEntities(entity, serverLevel, center, RADIUS)) {
                if (!ArsenalOfDawnAbility.isEvil(target)) continue;
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DEBUFF_REFRESH, DEBUFF_AMPLIFIER, false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, DEBUFF_REFRESH, DEBUFF_AMPLIFIER, false, true, true));
            }
        }, () -> lights.forEach(pos -> {
            if (serverLevel.getBlockState(pos).is(Blocks.LIGHT)) serverLevel.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }), serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(center, serverLevel)));
    }

    private static List<BlockPos> placeLights(ServerLevel level, BlockPos origin) {
        List<BlockPos> placed = new ArrayList<>();
        for (int x = -RADIUS; x <= RADIUS; x += LIGHT_SPACING) {
            for (int z = -RADIUS; z <= RADIUS; z += LIGHT_SPACING) {
                if (x * x + z * z > RADIUS * RADIUS) continue;
                for (int y = 2; y >= -2; y--) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (level.getBlockState(pos).isAir()) {
                        level.setBlockAndUpdate(pos, Blocks.LIGHT.defaultBlockState());
                        placed.add(pos);
                        break;
                    }
                }
            }
        }
        return placed;
    }
}
