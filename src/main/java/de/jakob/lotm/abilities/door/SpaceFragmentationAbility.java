package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.TimeChangeEntity;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.PlanetEntity;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SpaceFragmentationAbility extends Ability {
    public SpaceFragmentationAbility(String id) {
        super(id, 20);
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        Vec3 targetLoc =  AbilityUtil.getTargetLocation(entity, 35*(int) Math.max(multiplier(entity)/4,1), 2);

        EffectManager.playEffect(EffectManager.Effect.SPACE_TEARING, targetLoc.x(), targetLoc.y(), targetLoc.z(), (ServerLevel) level, entity);

        double radius = 20.0;
        int planetCount = 9;

        HashSet<PlanetEntity> planets = new HashSet<>();

        for (int i = 0; i < planetCount; i++) {
            double angle = (2 * Math.PI / planetCount) * i;

            double x = targetLoc.x + Math.cos(angle) * radius;
            double z = targetLoc.z + Math.sin(angle) * radius;
            double y = targetLoc.y + 2; // slight height offset (optional)

            PlanetEntity planet = new PlanetEntity(ModEntities.PLANET.get(), level);
            planet.setPos(x, y, z);

            level.addFreshEntity(planet);
            planets.add(planet);
        }

        AtomicInteger tick = new AtomicInteger(0);
        ServerScheduler.scheduleForDuration(0, 1, 20 * 5, () -> {
            if(tick.get() % 20 == 0) {
                AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 25, DamageLookup.lookupDamage(1, .75f) *(int) Math.max(multiplier(entity)/6,1), targetLoc, true, true);
            }
            if (BeyonderData.isGriefingEnabled(entity))
            {
                AbilityUtil.getBlocksInSphereRadius(level, targetLoc, Math.max(2, tick.get() / 6f), true, true, false).forEach(pos -> {
                    if (level.random.nextFloat() < 0.3f && level.getBlockState(pos).getDestroySpeed(level, pos) >= 0 && !pos.getCenter().equals(BlockPos.containing(entity.position().subtract(0, 1, 0)).getCenter())) {
                        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    }
                });
            };

            tick.getAndIncrement();
        }, () -> planets.forEach(PlanetEntity::discard), (ServerLevel) level);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 12000;
    }
}
