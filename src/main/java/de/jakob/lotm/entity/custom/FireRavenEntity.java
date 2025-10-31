package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FireRavenEntity extends Animal {

    public final AnimationState IDLE_ANIMATION = new AnimationState();
    public final AnimationState FLY_ANIMATION = new AnimationState();
    private int idleAnimationTimeout = 0;
    private int trackingStartDelay = 22;

    private boolean hasTarget = false;
    private boolean isTrackingEntity = false;
    private Vec3 targetPosition;
    private LivingEntity targetEntity;
    private LivingEntity sourceEntity;
    private double damage;
    private boolean griefing;

    public FireRavenEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);

        // Enable flying movement
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.navigation = new FlyingPathNavigation(this, level);
    }

    public FireRavenEntity(Level level, LivingEntity target, LivingEntity source, double damage, boolean griefing) {
        super(ModEntities.FIRE_RAVEN.get(), level);

        hasTarget = true;
        isTrackingEntity = true;
        targetEntity = target;
        sourceEntity = source;
        this.damage = damage;
        this.griefing = griefing;

        // Enable flying movement
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.navigation = new FlyingPathNavigation(this, level);

        this.goalSelector.removeAllGoals(goal -> true);

    }

    public FireRavenEntity(Level level, Vec3 target, LivingEntity source, double damage, boolean griefing) {
        super(ModEntities.FIRE_RAVEN.get(), level);

        hasTarget = true;
        isTrackingEntity = false;
        targetPosition = target;
        sourceEntity = source;
        this.damage = damage;
        this.griefing = griefing;

        // Enable flying movement
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.navigation = new FlyingPathNavigation(this, level);

        this.goalSelector.removeAllGoals(goal -> true);

    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0));

        // Flying-specific goals
        this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(3, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 2)
                .add(Attributes.SCALE, 1.35);
    }

    @Override
    public boolean isFood(@NotNull ItemStack itemStack) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    // Essential methods for flying behavior
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource damageSource) {
        return false; // No fall damage
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // Prevent fall damage calculation
    }

    public boolean isFlying() {
        return !this.onGround();
    }
    // Make the entity prefer to fly at a certain height
    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingNavigation = new FlyingPathNavigation(this, level);
        flyingNavigation.setCanOpenDoors(false);
        flyingNavigation.setCanFloat(false);
        flyingNavigation.setCanPassDoors(false);
        return flyingNavigation;
    }

    // Custom flying movement behavior
    @Override
    public void aiStep() {
        super.aiStep();

        // Add some upward movement when the entity is too low
        if (!this.level().isClientSide && this.isAlive()) {
            // Try to maintain altitude above ground
            BlockPos belowPos = this.blockPosition().below(3);
            if (!this.level().isEmptyBlock(belowPos) && this.getDeltaMovement().y < 0.1) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.02, 0));
            }
        }
    }

    // Prevent the entity from being affected by certain ground-based mechanics
    @Override
    public boolean onClimbable() {
        return false;
    }

    private void setupAnimationStates() {
        if (this.isFlying()) {
            this.IDLE_ANIMATION.stop();
            this.FLY_ANIMATION.startIfStopped(this.tickCount);
        } else {
            this.FLY_ANIMATION.stop();
            if(this.idleAnimationTimeout <= 0) {
                this.idleAnimationTimeout = 30;
                this.IDLE_ANIMATION.start(this.tickCount);
            } else {
                --this.idleAnimationTimeout;
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        Level level = this.level();

        if(level.isClientSide) {
            this.setupAnimationStates();
        }
        else {
            if(!hasTarget)
                return;

            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.FLAME, position().add(0, .4, 0), 2, .4, .04);

            Vec3 target = isTrackingEntity ?
                    targetEntity.position().add(0, .4, 0) :
                    targetPosition;

            if(trackingStartDelay == 22) {
                this.navigation.moveTo(target.x + random.nextInt(-20, 20), target.y + random.nextInt(2, 8), target.z + random.nextInt(-20, 20), 1.8);
            }

            if(trackingStartDelay > 1) {
                trackingStartDelay--;
                return;
            }

            this.navigation.moveTo(target.x, target.y, target.z, 10);

            if(this.distanceToSqr(target) < 12) {

                Vec3 direction = target.subtract(this.position().add(0, .3, 0)).normalize();
                this.setDeltaMovement(direction.scale(3f));

                if(this.distanceToSqr(target) < 8) {
                    if(isTrackingEntity) {
                        if(targetEntity.isAlive()) {
                            targetEntity.hurt(sourceEntity.damageSources().mobAttack(sourceEntity), (float) damage);
                        }
                    } else {
                        level.explode(null, target.x, target.y, target.z, 2.2f, griefing, griefing ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
                    }
                    this.discard();
                }
            }
        }
    }
}
