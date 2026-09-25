package de.jakob.lotm.entity.custom.spirits;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpiritGrimReaperEntity extends Monster {

    public final AnimationState IDLE_ANIMATION = new AnimationState();
    public final AnimationState WALK_ANIMATION = new AnimationState();
    public final AnimationState ATTACK_ANIMATION = new AnimationState();

    // --- tuning ---
    private static final float LIFESTEAL_PERCENT = 0.35F;
    private static final int WITHER_DURATION = 100;
    private static final int WITHER_AMPLIFIER = 1;

    private static final int DEATH_TOUCH_COOLDOWN = 140;
    private static final double DEATH_TOUCH_MIN_RANGE = 5.0;
    private static final double DEATH_TOUCH_MAX_RANGE = 18.0;
    private static final int DEATH_TOUCH_WITHER_DURATION = 160;
    private static final float DEATH_TOUCH_DAMAGE_MULTIPLIER = 1.5F;

    private static final int SOUL_PULSE_COOLDOWN = 100;
    private static final double SOUL_PULSE_RADIUS = 4.5;
    private static final float SOUL_PULSE_DAMAGE = 6.0F;
    private static final int SOUL_PULSE_WEAKNESS_DURATION = 140;

    // cooldowns live on the entity so they tick down even while the
    // corresponding goal isn't the one currently running
    private int deathTouchCooldown = DEATH_TOUCH_COOLDOWN / 2;
    private int soulPulseCooldown = SOUL_PULSE_COOLDOWN / 2;

    public SpiritGrimReaperEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 25;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new DeathTouchGoal(this));
        this.goalSelector.addGoal(2, new SoulPulseGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // roughly twice the ghost: 140 -> 280 hp, 45 -> 90 attack
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 280.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 90.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.FOLLOW_RANGE, 100.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity livingTarget) {
            applyDeathTouch(livingTarget, WITHER_DURATION, WITHER_AMPLIFIER);
            lifesteal((float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
            spawnSoulParticles(livingTarget.getX(), livingTarget.getY(1.0), livingTarget.getZ(), 12);
        }
        return hit;
    }

    private void applyDeathTouch(LivingEntity target, int witherDuration, int witherAmplifier) {
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, witherDuration, witherAmplifier, false, true));
    }

    private void lifesteal(float damageDealt) {
        this.heal(damageDealt * LIFESTEAL_PERCENT);
    }

    private void spawnSoulParticles(double x, double y, double z, int count) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL, x, y, z, count, 0.3, 0.4, 0.3, 0.02);
        }
    }

    @Override
    public @NotNull ResourceKey<LootTable> getDefaultLootTable() {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "entities/spirit_grim_reaper")
        );
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (deathTouchCooldown > 0) deathTouchCooldown--;
            if (soulPulseCooldown > 0) soulPulseCooldown--;
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }

    /**
     * Death Touch: a burst attack. The reaper blinks to a spot right next to its
     * target from range, lands an empowered strike with a heavier wither, then
     * goes back to normal melee behavior. On a cooldown independent of distance.
     */
    private static class DeathTouchGoal extends Goal {

        private final SpiritGrimReaperEntity spirit;

        public DeathTouchGoal(SpiritGrimReaperEntity spirit) {
            this.spirit = spirit;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = spirit.getTarget();
            if (target == null || !target.isAlive() || spirit.deathTouchCooldown > 0) {
                return false;
            }
            double distanceSq = spirit.distanceToSqr(target);
            return distanceSq >= DEATH_TOUCH_MIN_RANGE * DEATH_TOUCH_MIN_RANGE
                    && distanceSq <= DEATH_TOUCH_MAX_RANGE * DEATH_TOUCH_MAX_RANGE;
        }

        @Override
        public boolean canContinueToUse() {
            return false; // one-shot burst, all handled in start()
        }

        @Override
        public void start() {
            LivingEntity target = spirit.getTarget();
            if (target == null) return;

            spirit.spawnSoulParticles(spirit.getX(), spirit.getY(0.5), spirit.getZ(), 20);

            Vec3 lookDir = target.getLookAngle();
            Vec3 besideTarget = target.position()
                    .subtract(lookDir.x, 0, lookDir.z)
                    .normalize()
                    .scale(1.5)
                    .add(target.position());

            boolean teleported = spirit.randomTeleport(besideTarget.x, besideTarget.y, besideTarget.z, true);
            if (!teleported) {
                spirit.randomTeleport(target.getX(), target.getY(), target.getZ(), true);
            }

            spirit.getLookControl().setLookAt(target, 180F, 180F);
            spirit.swing(spirit.getUsedItemHand());

            boolean hit = spirit.doHurtTarget(target);
            if (hit) {
                spirit.applyDeathTouch(target, DEATH_TOUCH_WITHER_DURATION, WITHER_AMPLIFIER + 1);
                float bonusDamage = (float) spirit.getAttributeValue(Attributes.ATTACK_DAMAGE)
                        * (DEATH_TOUCH_DAMAGE_MULTIPLIER - 1.0F);
                target.hurt(spirit.damageSources().mobAttack(spirit), bonusDamage);
                spirit.spawnSoulParticles(target.getX(), target.getY(1.0), target.getZ(), 20);
            }

            spirit.deathTouchCooldown = DEATH_TOUCH_COOLDOWN;
        }
    }

    /**
     * Soul Pulse: an AOE burst around the reaper that weakens and chips away at
     * anything standing too close, rewarding kiting it instead of surrounding it.
     */
    private static class SoulPulseGoal extends Goal {

        private final SpiritGrimReaperEntity spirit;

        public SoulPulseGoal(SpiritGrimReaperEntity spirit) {
            this.spirit = spirit;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = spirit.getTarget();
            if (target == null || !target.isAlive() || spirit.soulPulseCooldown > 0) {
                return false;
            }
            return spirit.distanceToSqr(target) <= SOUL_PULSE_RADIUS * SOUL_PULSE_RADIUS;
        }

        @Override
        public boolean canContinueToUse() {
            return false; // one-shot pulse, all handled in start()
        }

        @Override
        public void start() {
            AABB pulseArea = spirit.getBoundingBox().inflate(SOUL_PULSE_RADIUS);
            List<LivingEntity> nearby = spirit.level().getEntitiesOfClass(
                    LivingEntity.class,
                    pulseArea,
                    entity -> entity != spirit && entity.isAlive()
            );

            for (LivingEntity entity : nearby) {
                entity.hurt(spirit.damageSources().mobAttack(spirit), SOUL_PULSE_DAMAGE);
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, SOUL_PULSE_WEAKNESS_DURATION, 1, false, true));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SOUL_PULSE_WEAKNESS_DURATION, 1, false, true));
            }

            spirit.spawnSoulParticles(spirit.getX(), spirit.getY(0.5), spirit.getZ(), 40);
            spirit.soulPulseCooldown = SOUL_PULSE_COOLDOWN;
        }
    }
}