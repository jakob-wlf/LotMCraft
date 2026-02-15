package de.jakob.lotm.abilities.darkness;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SwordOfDarknessAbility extends Ability {
    public SwordOfDarknessAbility(String id) {
        super(id, 2);
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            ClientHandler.applyCameraShakeToPlayersInRadius(2, 30, (ClientLevel) level, entity.position(), 60);
            return;
        }

        AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, null, 25, entity.position(), new MobEffectInstance(MobEffects.BLINDNESS, 20, 0, false, false, false));

        Vec3 startLoc = entity.getEyePosition().add(entity.getLookAngle().scale(1.5));
        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 60, 4);
        Vec3 direction = targetLoc.subtract(startLoc).normalize();

        double offsetRight = random.nextDouble(2, 5) * (random.nextBoolean() ? 1 : -1);

        Vec3 slashStart = VectorUtil.getRelativePosition(startLoc, direction, 0, offsetRight, 8);
        Vec3 slashEnd = VectorUtil.getRelativePosition(startLoc, direction, 0, -offsetRight, -8);


        AtomicDouble distance = new AtomicDouble(0);
        ServerScheduler.scheduleForDuration(0, 1, 20 * 6, () -> {
            for (int j = 0; j < 3; j++) {
                Vec3 currentSlashStart = slashStart.add(direction.scale(distance.get()));
                Vec3 currentSlashEnd = slashEnd.add(direction.scale(distance.get()));

                Vec3 slashDirection = currentSlashEnd.subtract(currentSlashStart).normalize();
                double slashLength = currentSlashStart.distanceTo(currentSlashEnd);
                for(double i = 0; i < slashLength; i += 0.25) {
                    System.out.println(i);
                    Vec3 point = currentSlashStart.add(slashDirection.scale(i));
                    ParticleUtil.spawnParticles(serverLevel, ModParticles.BLACK.get(), point, 3, 0.2, 0);
                    AbilityUtil.damageNearbyEntities(serverLevel, entity, 3, multiplier(entity) * DamageLookup.lookupDamage(1, .8), point, true, false);
                    if(BeyonderData.isGriefingEnabled(entity)) {
                        serverLevel.setBlockAndUpdate(BlockPos.containing(point), Blocks.AIR.defaultBlockState());
                    }
                }

                distance.addAndGet(.5);
            }
        });
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 2000;
    }
}
