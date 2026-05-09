package de.jakob.lotm.abilities.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.events.custom.TargetEntityEvent;
import de.jakob.lotm.events.custom.TargetLocationEvent;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ErrorAbility extends Ability {

    private static final int EFFECT_DURATION_TICKS = 20 * 30;
    private static final double AOE_RADIUS = 24.0;
    private static final double RETARGET_CHANCE = 0.45;
    private static final double RELOCATION_CHANCE = 0.55;
    private static final double HEAL_INSTEAD_CHANCE = 0.35;
    private static final double REDIRECT_DAMAGE_CHANCE = 0.35;

    private static final Map<UUID, ErrorState> ACTIVE_ERRORS = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> REDIRECTING_DAMAGE = ThreadLocal.withInitial(() -> false);

    private record ErrorState(UUID casterUUID, long expiresAt) {}

    public ErrorAbility(String id) {
        super(id, 30f);
        canBeCopied = false;
        canBeReplicated = false;
        canBeUsedInArtifact = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 0));
    }

    @Override
    public float getSpiritualityCost() {
        return 80;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                entity.getBoundingBox().inflate(AOE_RADIUS),
                target -> target != entity && AbilityUtil.mayTarget(entity, target)
        );

        long expiresAt = serverLevel.getGameTime() + EFFECT_DURATION_TICKS;
        List<UUID> affectedTargets = new ArrayList<>(targets.size());

        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, entity.getSoundSource(), 1.8f, 0.65f);
        serverLevel.sendParticles(ParticleTypes.WITCH, entity.getX(), entity.getY() + 1.0, entity.getZ(), 50, 1.2, 0.7, 1.2, 0.02);
        serverLevel.sendParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY() + 1.0, entity.getZ(), 35, 1.0, 0.5, 1.0, 0.25);

        for (LivingEntity target : targets) {
            ACTIVE_ERRORS.put(target.getUUID(), new ErrorState(entity.getUUID(), expiresAt));
            affectedTargets.add(target.getUUID());
            serverLevel.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), 18, 0.35, 0.6, 0.35, 0.04);
        }

        ServerScheduler.scheduleForDuration(20, 20, EFFECT_DURATION_TICKS, () -> {
            for (UUID targetUUID : affectedTargets) {
                var found = serverLevel.getEntity(targetUUID);
                if (!(found instanceof LivingEntity target) || !target.isAlive() || target.isRemoved()) {
                    continue;
                }

                ErrorState state = ACTIVE_ERRORS.get(targetUUID);
                if (state == null || state.expiresAt() != expiresAt) {
                    continue;
                }

                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, false, false));

                if (random.nextInt(4) == 0) {
                    DisabledAbilitiesComponent component = target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
                    component.disableAbilityUsageForTime("error_ability", 20 * 2, target);
                }

                if (random.nextInt(3) == 0) {
                    Vec3 destination = findErrorTeleportLocation(serverLevel, target);
                    if (destination.distanceToSqr(target.position()) > 0.25) {
                        target.teleportTo(destination.x, destination.y, destination.z);
                    }
                }

                serverLevel.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), 10, 0.25, 0.5, 0.25, 0.02);
            }
        }, () -> {
            for (UUID targetUUID : affectedTargets) {
                ErrorState state = ACTIVE_ERRORS.get(targetUUID);
                if (state != null && state.expiresAt() == expiresAt) {
                    ACTIVE_ERRORS.remove(targetUUID);
                }
            }
        }, serverLevel);
    }

    private Vec3 findErrorTeleportLocation(ServerLevel level, LivingEntity target) {
        Vec3 origin = target.position();

        for (int i = 0; i < 8; i++) {
            Vec3 candidate = origin.add(
                    random.nextDouble(-5.5, 5.5),
                    random.nextDouble(-1.5, 1.5),
                    random.nextDouble(-5.5, 5.5)
            );

            BlockPos feetPos = BlockPos.containing(candidate);
            BlockPos headPos = feetPos.above();

            if (!level.getBlockState(feetPos).getCollisionShape(level, feetPos).isEmpty()) {
                continue;
            }
            if (!level.getBlockState(headPos).getCollisionShape(level, headPos).isEmpty()) {
                continue;
            }

            return candidate;
        }

        return origin;
    }

    private static ErrorState getErrorState(LivingEntity entity) {
        ErrorState state = ACTIVE_ERRORS.get(entity.getUUID());
        if (state == null) {
            return null;
        }
        if (entity.level().getGameTime() > state.expiresAt()) {
            ACTIVE_ERRORS.remove(entity.getUUID());
            return null;
        }
        return state;
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (REDIRECTING_DAMAGE.get()) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ErrorState state = getErrorState(entity);
        if (state == null) {
            return;
        }

        double roll = entity.getRandom().nextDouble();
        float amount = event.getAmount();

        if (roll < HEAL_INSTEAD_CHANCE) {
            event.setCanceled(true);
            entity.heal(amount);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(), 12, 0.3, 0.45, 0.3, 0.02);
            return;
        }

        if (roll >= HEAL_INSTEAD_CHANCE + REDIRECT_DAMAGE_CHANCE) {
            return;
        }

        LivingEntity redirectTarget = getRedirectTarget(serverLevel, entity, state);
        if (redirectTarget == null) {
            return;
        }

        event.setCanceled(true);
        REDIRECTING_DAMAGE.set(true);
        try {
            redirectTarget.hurt(event.getSource(), amount);
        } finally {
            REDIRECTING_DAMAGE.set(false);
        }

        serverLevel.sendParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(), 10, 0.25, 0.4, 0.25, 0.08);
        serverLevel.sendParticles(ParticleTypes.PORTAL, redirectTarget.getX(), redirectTarget.getY() + redirectTarget.getBbHeight() * 0.5, redirectTarget.getZ(), 10, 0.25, 0.4, 0.25, 0.08);
    }

    private static LivingEntity getRedirectTarget(ServerLevel level, LivingEntity originalTarget, ErrorState state) {
        AABB area = originalTarget.getBoundingBox().inflate(20);
        List<LivingEntity> candidates = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                candidate -> candidate != originalTarget
                        && candidate.isAlive()
                        && !candidate.isRemoved()
                        && hasMatchingState(candidate, state)
        );

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(originalTarget.getRandom().nextInt(candidates.size()));
    }

    private static boolean hasMatchingState(LivingEntity entity, ErrorState expectedState) {
        ErrorState candidateState = getErrorState(entity);
        return candidateState != null
                && candidateState.casterUUID().equals(expectedState.casterUUID())
                && candidateState.expiresAt() == expectedState.expiresAt();
    }

    @SubscribeEvent
    public static void onTargetEntity(TargetEntityEvent event) {
        LivingEntity source = event.getSourceEntity();
        if (!(source.level() instanceof ServerLevel serverLevel) || getErrorState(source) == null) {
            return;
        }
        if (source.getRandom().nextDouble() >= RETARGET_CHANCE) {
            return;
        }

        int radius = Math.max(8, event.getRadius());
        List<LivingEntity> candidates = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                source.getBoundingBox().inflate(radius),
                candidate -> candidate != source
                        && candidate != event.getTargetEntity()
                        && AbilityUtil.mayTarget(source, candidate, event.isAllowAllies(), false)
        );

        if (candidates.isEmpty()) {
            return;
        }

        event.setTargetEntity(candidates.get(source.getRandom().nextInt(candidates.size())));
    }

    @SubscribeEvent
    public static void onTargetLocation(TargetLocationEvent event) {
        LivingEntity source = event.getSourceEntity();
        if (!(source.level() instanceof ServerLevel serverLevel) || getErrorState(source) == null) {
            return;
        }
        if (source.getRandom().nextDouble() >= RELOCATION_CHANCE) {
            return;
        }

        Vec3 original = event.getTargetLocation();
        double y;
        y = Mth.clamp(
                original.y + source.getRandom().nextDouble(),
                serverLevel.getMinBuildHeight(),
                serverLevel.getMaxBuildHeight()
        );

        event.setTargetLocation(original.add(
                source.getRandom().nextDouble(),
                y - original.y,
                source.getRandom().nextDouble()
        ));
    }
}
