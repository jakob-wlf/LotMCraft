package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.entity.custom.FireballEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ClientScheduler;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class FlameMasteryAbility extends SelectableAbilityItem {
    private final HashSet<UUID> transformedEntities = new HashSet<>();
    public FlameMasteryAbility(Properties properties) {
        super(properties, 2.5f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 100;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.flame_mastery.fireball_barrage",
                "ability.lotmcraft.flame_mastery.eruption",
                "ability.lotmcraft.flame_mastery.flame_transformation",
        };
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(level.isClientSide)
            return;
        if(!(entity instanceof Player)) {
            abilityIndex = 1;
        }

        switch (abilityIndex) {
            case 0 -> fireballBarrage((ServerLevel) level, entity);
            case 1 -> eruption((ServerLevel) level, entity);
            case 2 -> flameTransformation((ServerLevel) level, entity);
        }
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(1.0f, .95f, .95f), 2.0f);

    private void flameTransformation(ServerLevel level, LivingEntity entity) {
        level.playSound(null, entity.blockPosition(), SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);
        if(transformedEntities.contains(entity.getUUID())) {
            transformedEntities.remove(entity.getUUID());
            return;
        }

        transformedEntities.add(entity.getUUID());
        AtomicBoolean shouldStop = new AtomicBoolean(false);
        ServerScheduler.scheduleUntil(level, () -> {
            if(!transformedEntities.contains(entity.getUUID())) {
                shouldStop.set(true);
                return;
            }
            ParticleUtil.spawnParticles(level, ParticleTypes.FLAME, entity.getEyePosition(), 60, 1.2, .05);
            ParticleUtil.spawnParticles(level, dust, entity.getEyePosition(), 30, 1.2, .05);
            if(!entity.isShiftKeyDown())
                entity.setDeltaMovement(entity.getLookAngle().normalize());
            else
                entity.setDeltaMovement(0, 0, 0);

            entity.hurtMarked = true;
        }, 2, null, shouldStop);
    }

    private void eruption(ServerLevel level, LivingEntity entity) {
        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 20, 1.4f);
        boolean griefing = BeyonderData.isGriefingEnabled(entity);
        level.explode(entity, targetPos.x, targetPos.y, targetPos.z, 9, griefing, griefing ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
        level.explode(entity, targetPos.x, targetPos.y + 1, targetPos.z, 9, griefing, griefing ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
        level.explode(entity, targetPos.x, targetPos.y + 2, targetPos.z, 9, griefing, griefing ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
        ParticleUtil.spawnParticles(level, ParticleTypes.FLAME, targetPos, 1500, 2, 6, 2, .02);
        ParticleUtil.spawnParticles(level, ParticleTypes.SMOKE, targetPos, 300, 2, 6, 2, .02);
        ParticleUtil.spawnParticles(level, ParticleTypes.EXPLOSION, targetPos, 90, 2, 6, 2, .02);
        ParticleUtil.spawnParticles(level, dust, targetPos, 400, 2, 6, 2, 0);

        AbilityUtil.damageNearbyEntities(level, entity, 9, DamageLookup.lookupDamage(4, .85) * multiplier(entity), targetPos, true, false);

        for(int i = 0; i < 25; i++) {
            FallingBlockEntity falling = FallingBlockEntity.fall(
                    level,
                    BlockPos.containing(targetPos.x, targetPos.y, targetPos.z).offset(random.nextInt(-1, 1), 2, random.nextInt(-1, 1)),
                    i % 2 == 0 ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.BASALT.defaultBlockState()
            );

            double xVel = random.nextDouble(-3, 3);
            double yVel = random.nextDouble(3.5, 5);
            double zVel = random.nextDouble(-3, 3);
            Vec3 motion = new Vec3(xVel, yVel, zVel).normalize().scale(1.4);
            falling.setDeltaMovement(motion);
            ServerScheduler.scheduleForDuration(0, 1, 40, () -> {
                falling.setDeltaMovement(falling.getDeltaMovement().x, falling.getDeltaMovement().y - 0.03, falling.getDeltaMovement().z);
                falling.hurtMarked = true;
            });
            if(!griefing)
                falling.disableDrop();
        }
    }

    private void fireballBarrage(ServerLevel level, LivingEntity entity) {
        double shots = 15 * multiplier(entity);
        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 50, 1.4f);
        Vec3 pos = entity.getEyePosition();
        Vec3 dir = entity.getLookAngle();
        for (int i = 0; i < shots; i++) {
            ServerScheduler.scheduleDelayed(i * 7, () -> fireball(level, entity, pos, dir, targetPos));
        }
    }

    private void fireball(ServerLevel level, LivingEntity entity, Vec3 pos, Vec3 dir, Vec3 targetPos) {
        Vec3 startPos = VectorUtil.getRelativePosition(pos.add(entity.getLookAngle().normalize()), dir.normalize(), random.nextDouble(-.2, 2.5f), random.nextDouble(-13, 13f), random.nextDouble(-1, 6));
        Vec3 direction = targetPos.subtract(startPos).normalize();

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);

        FireballEntity fireball = new FireballEntity(level, entity, DamageLookup.lookupDamage(4, .4) * multiplier(entity), BeyonderData.isGriefingEnabled(entity), 1.75f);
        fireball.setPos(startPos.x, startPos.y, startPos.z); // Set initial position,
        fireball.shoot(direction.x, direction.y, direction.z, 1.85f, 0);
        level.addFreshEntity(fireball);
    }
}
