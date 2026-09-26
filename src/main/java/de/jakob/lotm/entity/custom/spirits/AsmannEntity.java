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
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AsmannEntity extends Monster {

    public final AnimationState IDLE_ANIMATION = new AnimationState();
    public final AnimationState WALK_ANIMATION = new AnimationState();
    public final AnimationState ATTACK_ANIMATION = new AnimationState();

    private static final double HOVER_HEIGHT_PUSH = 0.02;
    private static final int BREACH_DURATION = 25;
    private static final int BREACH_MIN_INTERVAL = 100;
    private static final int BREACH_MAX_INTERVAL = 220;
    private static final double BREACH_LAUNCH_VELOCITY = 0.45;
    private static final double BREACH_HOLD_PUSH = 0.05;
    private static final double RESUBMERGE_PUSH = -0.08;

    private static final int MELEE_NAUSEA_DURATION = 120;
    private static final int MELEE_WEAKNESS_DURATION = 100;

    private static final int MIND_SPIKE_COOLDOWN = 100;
    private static final double MIND_SPIKE_MIN_RANGE = 4.0;
    private static final double MIND_SPIKE_MAX_RANGE = 14.0;
    private static final float MIND_SPIKE_DAMAGE = 8.0F;
    private static final int MIND_SPIKE_BLIND_DURATION = 60;
    private static final int MIND_SPIKE_NAUSEA_DURATION = 100;
    private static final int MIND_SPIKE_DARKNESS_DURATION = 80;

    private static final int TELEKINESIS_COOLDOWN = 160;
    private static final double TELEKINESIS_MIN_RANGE = 6.0;
    private static final double TELEKINESIS_MAX_RANGE = 16.0;
    private static final double TELEKINESIS_PULL_STRENGTH = 0.6;
    private static final int TELEKINESIS_WEAKNESS_DURATION = 100;
    private static final int TELEKINESIS_NAUSEA_DURATION = 100;

    private int mindSpikeCooldown = MIND_SPIKE_COOLDOWN / 2;
    private int telekinesisCooldown = TELEKINESIS_COOLDOWN / 2;

    private int waterCooldownTicks = 100 + this.random.nextInt(BREACH_MAX_INTERVAL - 100);
    private int breachTicksLeft = 0;

    public AsmannEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.navigation = new FlyingPathNavigation(this, level);
        this.xpReward = 15;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MindSpikeGoal(this));
        this.goalSelector.addGoal(2, new TelekineticPullGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 140.0)
                .add(Attributes.MOVEMENT_SPEED, 1.0)
                .add(Attributes.FLYING_SPEED, 1.0)
                .add(Attributes.SCALE, 1.1)
                .add(Attributes.ATTACK_DAMAGE, 40.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 100.0);
    }

    @Override
    public void tick() {
        this.setAirSupply(getMaxAirSupply());
        super.tick();
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity livingTarget) {
            applyMindEffects(livingTarget, MELEE_NAUSEA_DURATION, MELEE_WEAKNESS_DURATION, 0);
            spawnPsychicParticles(livingTarget.getX(), livingTarget.getY(1.0), livingTarget.getZ(), 10);
        }
        return hit;
    }

    private void applyMindEffects(LivingEntity target, int nauseaDuration, int weaknessDuration, int amplifier) {
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, nauseaDuration, amplifier, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, weaknessDuration, amplifier, false, true));
    }

    private void spawnPsychicParticles(double x, double y, double z, int count) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.PORTAL, x, y, z, count, 0.3, 0.4, 0.3, 0.05);
        }
    }

    @Override
    public @NotNull ResourceKey<LootTable> getDefaultLootTable() {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "entities/spirit_asmann")
        );
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingNavigation = new FlyingPathNavigation(this, level);
        flyingNavigation.setCanOpenDoors(false);
        flyingNavigation.setCanFloat(true);
        flyingNavigation.setCanPassDoors(false);
        return flyingNavigation;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide || !this.isAlive()) {
            return;
        }

        if (mindSpikeCooldown > 0) mindSpikeCooldown--;
        if (telekinesisCooldown > 0) telekinesisCooldown--;

        if (this.isInWater()) {
            handleWaterBehavior();
        } else {
            breachTicksLeft = 0;
            handleGroundHover();
        }
    }

    private void handleGroundHover() {
        BlockPos belowPos = this.blockPosition().below(1);
        if (!this.level().isEmptyBlock(belowPos) && this.getDeltaMovement().y < 0.1) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, HOVER_HEIGHT_PUSH, 0));
        }
    }

    private void handleWaterBehavior() {
        if (breachTicksLeft > 0) {
            breachTicksLeft--;
            this.setDeltaMovement(this.getDeltaMovement().add(0, BREACH_HOLD_PUSH, 0));
            if (breachTicksLeft == 0) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, RESUBMERGE_PUSH, 0));
            }
            return;
        }

        waterCooldownTicks--;
        if (waterCooldownTicks <= 0) {
            breachTicksLeft = BREACH_DURATION;
            waterCooldownTicks = BREACH_MIN_INTERVAL + this.random.nextInt(BREACH_MAX_INTERVAL - BREACH_MIN_INTERVAL);
            this.setDeltaMovement(this.getDeltaMovement().add(0, BREACH_LAUNCH_VELOCITY, 0));
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ILLUSIONER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.ILLUSIONER_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ILLUSIONER_DEATH;
    }

    private static class MindSpikeGoal extends Goal {

        private final AsmannEntity asmann;

        public MindSpikeGoal(AsmannEntity asmann) {
            this.asmann = asmann;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = asmann.getTarget();
            if (target == null || !target.isAlive() || asmann.mindSpikeCooldown > 0) {
                return false;
            }
            double distanceSq = asmann.distanceToSqr(target);
            return distanceSq >= MIND_SPIKE_MIN_RANGE * MIND_SPIKE_MIN_RANGE
                    && distanceSq <= MIND_SPIKE_MAX_RANGE * MIND_SPIKE_MAX_RANGE
                    && asmann.getSensing().hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            LivingEntity target = asmann.getTarget();
            if (target == null) return;

            asmann.spawnPsychicParticles(asmann.getX(), asmann.getY(0.7), asmann.getZ(), 10);
            asmann.playSound(SoundEvents.ILLUSIONER_CAST_SPELL, 1.0F, 1.0F);

            target.hurt(asmann.damageSources().mobAttack(asmann), MIND_SPIKE_DAMAGE);
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, MIND_SPIKE_BLIND_DURATION, 0, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, MIND_SPIKE_NAUSEA_DURATION, 0, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, MIND_SPIKE_DARKNESS_DURATION, 0, false, true));

            asmann.spawnPsychicParticles(target.getX(), target.getY(1.0), target.getZ(), 14);
            asmann.mindSpikeCooldown = MIND_SPIKE_COOLDOWN;
        }
    }

    private static class TelekineticPullGoal extends Goal {

        private final AsmannEntity asmann;

        public TelekineticPullGoal(AsmannEntity asmann) {
            this.asmann = asmann;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = asmann.getTarget();
            if (target == null || !target.isAlive() || asmann.telekinesisCooldown > 0) {
                return false;
            }
            double distanceSq = asmann.distanceToSqr(target);
            return distanceSq >= TELEKINESIS_MIN_RANGE * TELEKINESIS_MIN_RANGE
                    && distanceSq <= TELEKINESIS_MAX_RANGE * TELEKINESIS_MAX_RANGE
                    && asmann.getSensing().hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            LivingEntity target = asmann.getTarget();
            if (target == null) return;

            Vec3 pullDirection = asmann.position().subtract(target.position()).normalize();
            target.setDeltaMovement(target.getDeltaMovement().add(
                    pullDirection.x * TELEKINESIS_PULL_STRENGTH,
                    0.15,
                    pullDirection.z * TELEKINESIS_PULL_STRENGTH
            ));
            target.hasImpulse = true;

            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, TELEKINESIS_WEAKNESS_DURATION, 1, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, TELEKINESIS_NAUSEA_DURATION, 0, false, true));

            asmann.playSound(SoundEvents.ILLUSIONER_CAST_SPELL, 1.0F, 0.7F);
            asmann.spawnPsychicParticles(asmann.getX(), asmann.getY(0.7), asmann.getZ(), 16);
            asmann.spawnPsychicParticles(target.getX(), target.getY(1.0), target.getZ(), 16);

            asmann.telekinesisCooldown = TELEKINESIS_COOLDOWN;
        }
    }
}