package de.jakob.lotm.entity.custom.spirits;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KnowledgeDemonEntity extends Monster {

    public final AnimationState IDLE_ANIMATION = new AnimationState();
    public final AnimationState FLY_ANIMATION = new AnimationState();
    public final AnimationState DASH_ANIMATION = new AnimationState();
    public final AnimationState ATTACK_ANIMATION = new AnimationState();

    private static final int MIN_HOVER_HEIGHT = 4;
    private static final int MAX_HOVER_HEIGHT = 9;
    private static final double ALTITUDE_PUSH_UP = 0.06;
    private static final double ALTITUDE_PUSH_DOWN = -0.02;
    private static final int GROUND_SCAN_DEPTH = 32;

    private static final int DASH_COOLDOWN = 100;
    private static final double DASH_MIN_RANGE = 6.0;
    private static final double DASH_MAX_RANGE = 22.0;
    private static final int DASH_DURATION = 12;
    private static final double DASH_SPEED = 1.9;
    private static final double DASH_HIT_RADIUS = 1.6;
    private static final float DASH_DAMAGE_MULTIPLIER = 1.3F;
    private static final double DASH_KNOCKBACK_STRENGTH = 1.2;

    private static final int INJECTION_COOLDOWN = 90;
    private static final double INJECTION_MIN_RANGE = 2.0;
    private static final double INJECTION_MAX_RANGE = 10.0;
    private static final float INJECTION_DAMAGE = 14.0F;
    private static final int INJECTION_CONFUSION_DURATION = 160;
    private static final int INJECTION_CONFUSION_AMPLIFIER = 1;
    private static final int INJECTION_DARKNESS_DURATION = 100;
    private static final int INJECTION_HUNGER_DURATION = 200;
    private static final int INJECTION_HUNGER_AMPLIFIER = 1;
    private static final int INJECTION_WEAKNESS_DURATION = 140;

    private int dashCooldown = DASH_COOLDOWN / 2;
    private int injectionCooldown = INJECTION_COOLDOWN / 2;
    private boolean dashing = false;

    public KnowledgeDemonEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 15, true);
        this.navigation = new FlyingPathNavigation(this, level);
        this.setNoGravity(true);
        this.xpReward = 45;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new DashAttackGoal(this));
        this.goalSelector.addGoal(2, new KnowledgeInjectionGoal(this));
        this.goalSelector.addGoal(3, new ApproachTargetGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FLYING_SPEED, 1.4)
                .add(Attributes.SCALE, 1.6)
                .add(Attributes.ATTACK_DAMAGE, 110.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.FOLLOW_RANGE, 120.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    public @NotNull ResourceKey<LootTable> getDefaultLootTable() {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "entities/knowledge_demon")
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
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide || !this.isAlive()) {
            return;
        }

        if (dashCooldown > 0) dashCooldown--;
        if (injectionCooldown > 0) injectionCooldown--;

        if (!dashing) {
            maintainHoverAltitude();
        }
    }

    private void maintainHoverAltitude() {
        int groundY = findGroundY();
        double heightAboveGround = this.getY() - groundY;

        if (heightAboveGround < MIN_HOVER_HEIGHT) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, ALTITUDE_PUSH_UP, 0));
        } else if (heightAboveGround > MAX_HOVER_HEIGHT) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, ALTITUDE_PUSH_DOWN, 0));
        }
    }

    private int findGroundY() {
        BlockPos.MutableBlockPos pos = this.blockPosition().mutable();
        int startY = pos.getY();
        for (int i = 0; i < GROUND_SCAN_DEPTH; i++) {
            pos.setY(startY - i);
            if (!this.level().isEmptyBlock(pos)) {
                return pos.getY() + 1;
            }
        }
        return startY - GROUND_SCAN_DEPTH;
    }

    private void spawnKnowledgeParticles(double x, double y, double z, int count) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT, x, y, z, count, 0.4, 0.4, 0.4, 0.1);
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.EVOKER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.EVOKER_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.EVOKER_DEATH;
    }

    private static class ApproachTargetGoal extends Goal {

        private final KnowledgeDemonEntity demon;

        public ApproachTargetGoal(KnowledgeDemonEntity demon) {
            this.demon = demon;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = demon.getTarget();
            return target != null && target.isAlive() && !demon.dashing;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            LivingEntity target = demon.getTarget();
            if (target == null) return;
            demon.getNavigation().moveTo(target.getX(), target.getEyeY() + 1.0, target.getZ(), 1.0);
        }
    }

    private static class DashAttackGoal extends Goal {

        private final KnowledgeDemonEntity demon;
        private final Set<LivingEntity> hitThisDash = new HashSet<>();
        private Vec3 dashDirection = Vec3.ZERO;
        private int ticksRemaining = 0;

        public DashAttackGoal(KnowledgeDemonEntity demon) {
            this.demon = demon;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = demon.getTarget();
            if (target == null || !target.isAlive() || demon.dashCooldown > 0) {
                return false;
            }
            double distanceSq = demon.distanceToSqr(target);
            return distanceSq >= DASH_MIN_RANGE * DASH_MIN_RANGE
                    && distanceSq <= DASH_MAX_RANGE * DASH_MAX_RANGE
                    && demon.getSensing().hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            return ticksRemaining > 0;
        }

        @Override
        public void start() {
            LivingEntity target = demon.getTarget();
            if (target == null) return;

            dashDirection = target.position()
                    .add(0, target.getBbHeight() * 0.5, 0)
                    .subtract(demon.position())
                    .normalize();
            ticksRemaining = DASH_DURATION;
            hitThisDash.clear();

            demon.dashing = true;
            demon.setDeltaMovement(dashDirection.scale(DASH_SPEED));

            demon.playSound(SoundEvents.PHANTOM_SWOOP, 1.2F, 0.8F);
            demon.spawnKnowledgeParticles(demon.getX(), demon.getY(0.5), demon.getZ(), 12);
        }

        @Override
        public void tick() {
            ticksRemaining--;
            demon.setDeltaMovement(dashDirection.scale(DASH_SPEED));
            demon.spawnKnowledgeParticles(demon.getX(), demon.getY(0.5), demon.getZ(), 3);

            AABB sweptArea = demon.getBoundingBox().inflate(DASH_HIT_RADIUS);
            List<LivingEntity> nearby = demon.level().getEntitiesOfClass(
                    LivingEntity.class,
                    sweptArea,
                    entity -> entity != demon && entity.isAlive() && !hitThisDash.contains(entity)
            );

            for (LivingEntity entity : nearby) {
                float damage = (float) demon.getAttributeValue(Attributes.ATTACK_DAMAGE) * DASH_DAMAGE_MULTIPLIER;
                entity.hurt(demon.damageSources().mobAttack(demon), damage);
                entity.knockback(DASH_KNOCKBACK_STRENGTH, demon.getX() - entity.getX(), demon.getZ() - entity.getZ());
                hitThisDash.add(entity);
            }
        }

        @Override
        public void stop() {
            demon.dashing = false;
            demon.dashCooldown = DASH_COOLDOWN;
            demon.setDeltaMovement(demon.getDeltaMovement().scale(0.2));
            hitThisDash.clear();
            ticksRemaining = 0;
        }
    }

    private static class KnowledgeInjectionGoal extends Goal {

        private final KnowledgeDemonEntity demon;

        public KnowledgeInjectionGoal(KnowledgeDemonEntity demon) {
            this.demon = demon;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = demon.getTarget();
            if (target == null || !target.isAlive() || demon.injectionCooldown > 0 || demon.dashing) {
                return false;
            }
            double distanceSq = demon.distanceToSqr(target);
            return distanceSq >= INJECTION_MIN_RANGE * INJECTION_MIN_RANGE
                    && distanceSq <= INJECTION_MAX_RANGE * INJECTION_MAX_RANGE
                    && demon.getSensing().hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            LivingEntity target = demon.getTarget();
            if (target == null) return;

            demon.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0F, 0.9F);
            demon.spawnKnowledgeParticles(demon.getX(), demon.getY(0.7), demon.getZ(), 14);

            target.hurt(demon.damageSources().mobAttack(demon), INJECTION_DAMAGE);
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, INJECTION_CONFUSION_DURATION, INJECTION_CONFUSION_AMPLIFIER, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, INJECTION_DARKNESS_DURATION, 0, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, INJECTION_HUNGER_DURATION, INJECTION_HUNGER_AMPLIFIER, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, INJECTION_WEAKNESS_DURATION, 0, false, true));
            target.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityWithSequenceDifference(.2f, target, 2, BeyonderData.getSequence(target));

            demon.spawnKnowledgeParticles(target.getX(), target.getY(1.0), target.getZ(), 20);
            demon.injectionCooldown = INJECTION_COOLDOWN;
        }
    }
}