package de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.ShadowShifter;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbility;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.AvatarEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BloodlineShadowShiftAbility extends PassiveAbility {

    private static final int UNLOCK_SEQUENCE = 6;

    private static final double SHADOW_SHIFT_DISTANCE = 100.0D;

    // Activates when a hit leaves the user at 20% HP or lower.
    private static final float HEALTH_THRESHOLD = 0.20F;

    // 10 min cooldown.
    private static final long COOLDOWN = 20L * 600L;
    // Shadow clone survives for 10 mins.
    private static final long CLONE_DURATION = 20L * 60L;
    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final DustParticleOptions SHADOW_DUST = new DustParticleOptions(new Vector3f(0.05F, 0.0F, 0.08F), 2.0F);
    private static final DustParticleOptions WISP_DUST = new DustParticleOptions(new Vector3f(0.25F, 0.0F, 0.35F), 1.2F);

    public BloodlineShadowShiftAbility(String id) {super(id);}

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", UNLOCK_SEQUENCE));
    }
    @Override
    public boolean meetsBloodlineRequirement(LivingEntity entity) {
        return hasShadowShiftBloodline(entity);
    }
    // DON'T LET CLONES HAVE THE ABILITY FOR THE GAMES SAKE
    private static boolean hasShadowShiftBloodline(LivingEntity entity) {
        if (entity instanceof AvatarEntity) return false;
        return DevilBloodlineAbility.hasBloodline(entity, DevilBloodlineAbility.Bloodline.SHADOW_SHIFTER);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }
    // Runs after the hit has been applied, so getHealth() is already the reduced value.
    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) return;
        if (entity.isDeadOrDying()) return;
        if (!"abyss".equals(BeyonderData.getPathway(entity))) return;
        if (BeyonderData.getSequence(entity) > UNLOCK_SEQUENCE) return;
        if (!hasShadowShiftBloodline(entity)) return;
        tryShadowShift(level, entity);
    }

    private static void tryShadowShift(ServerLevel level, LivingEntity entity) {
        float maxHealth = entity.getMaxHealth();
        if (maxHealth <= 0) {return;}
        if (entity.getHealth() > maxHealth * HEALTH_THRESHOLD) {return;}
        UUID uuid = entity.getUUID();
        long gameTime = level.getGameTime();
        // Cooldown check.
        long cooldownUntil = cooldowns.getOrDefault(uuid, 0L);
        if (gameTime < cooldownUntil) {return;}
        performShadowShift(level, entity);
        cooldowns.put(uuid, gameTime + COOLDOWN);
    }
    private static void performShadowShift(ServerLevel level, LivingEntity entity) {
        Vec3 originPos = entity.position();
        // Calculate escape direction.
        Vec3 lookDir = entity.getLookAngle();
        // Ignore vertical looking direction.
        Vec3 horizontalLook = new Vec3(lookDir.x, 0, lookDir.z);
        if (horizontalLook.lengthSqr() < 0.001D) {horizontalLook = new Vec3(0, 0, 1);}
        Vec3 escapeDir = horizontalLook.normalize().scale(-1);
        // 100 block escape.
        Vec3 desiredEscapePos = originPos.add(escapeDir.x * SHADOW_SHIFT_DISTANCE, 0, escapeDir.z * SHADOW_SHIFT_DISTANCE);
        Vec3 escapePos = findSafePosition(level, desiredEscapePos);
        // Create shadow clone.
        String pathway = BeyonderData.getPathway(entity);
        int sequence = BeyonderData.getSequence(entity);
        AvatarEntity clone = new AvatarEntity(ModEntities.AVATAR.get(), level, entity.getUUID(), pathway, sequence);
        clone.setPos(originPos.x, originPos.y, originPos.z);
        clone.setCustomName(entity.getName());
        clone.setCustomNameVisible(true);
        level.addFreshEntity(clone);
        // Redirect nearby mobs.
        AbilityUtil.getNearbyEntities(entity, level, originPos, 24).forEach(nearby -> {
            if (!(nearby instanceof Mob mob)) {return;}
            if (mob.getTarget() == null) {return;}
            if (mob.getTarget().getUUID().equals(entity.getUUID())) {mob.setTarget(clone);}
        });
        // Shadow departure effect.
        ParticleUtil.spawnParticles(level, SHADOW_DUST, originPos.add(0, 1, 0), 180, 1.0, 0.06);
        ParticleUtil.spawnParticles(level, ParticleTypes.SMOKE, originPos.add(0, 1, 0), 120, 1.0, 0.05);
        level.playSound(null, originPos.x, originPos.y, originPos.z, SoundEvents.ENDERMAN_TELEPORT, entity.getSoundSource(), 1.5F, 0.55F);
        // Teleport.
        entity.teleportTo(escapePos.x, escapePos.y, escapePos.z);
        // Temporary invisibility.
        entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 6, 0, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 4, 3, false, false, false));
        // Arrival effect.
        ParticleUtil.spawnParticles(level, WISP_DUST, escapePos.add(0, 1, 0), 50, 0.6, 0.05);
        level.playSound(null, escapePos.x, escapePos.y, escapePos.z, SoundEvents.ENDERMAN_TELEPORT, entity.getSoundSource(), 1.5F, 1.2F);
        // Shadow clone lifetime.
        ServerScheduler.scheduleDelayed((int) CLONE_DURATION, () -> destroyClone(level, entity, clone), level);
        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.shadow_shift.activated").withColor(0x1A0026));
    }

    private static void destroyClone(ServerLevel level, LivingEntity owner, AvatarEntity clone) {
        if (!clone.isAlive()) {return;}
        Vec3 clonePos = clone.position();
        ParticleUtil.spawnSphereParticles(level, SHADOW_DUST, clonePos.add(0, 1, 0), 6, 250);
        ParticleUtil.spawnParticles(level, ParticleTypes.SMOKE, clonePos.add(0, 1, 0), 150, 3.0, 0.05);
        level.playSound(null, clonePos.x, clonePos.y, clonePos.z, SoundEvents.WITHER_AMBIENT, owner.getSoundSource(), 1.2F, 1.5F);
        clone.discard();
    }
    private static Vec3 findSafePosition(ServerLevel level, Vec3 desiredPos) {
        BlockPos desiredBlock = BlockPos.containing(desiredPos);
        // Exact location is safe.
        if (isSafePosition(level, desiredBlock)) {return centerOf(desiredBlock);}
        // Search around the destination.
        for (int radius = 1; radius <= 8; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos candidate = desiredBlock.offset(dx, 0, dz);
                    if (isSafePosition(level, candidate)) {return centerOf(candidate);
                    }
                }
            }
        }
        // Fallback to the original destination.
        return desiredPos;
    }
    private static boolean isSafePosition(ServerLevel level, BlockPos pos) {return level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir() && !level.getBlockState(pos.below()).isAir();}
    private static Vec3 centerOf(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
    }
}