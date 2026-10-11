package de.jakob.lotm.entity.custom.spirits;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpiritGhostEntity extends Animal {

    public final AnimationState IDLE_ANIMATION = new AnimationState();
    public final AnimationState WALK_ANIMATION = new AnimationState();

    private static final int FLEE_DURATION_TICKS = 50;
    private static final double RAM_HIT_DISTANCE = 2.5;
    private static final int BLINDNESS_DURATION = 100;

    public SpiritGhostEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.navigation = new FlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RamAndFleeGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 140.0)
                .add(Attributes.MOVEMENT_SPEED, 1)
                .add(Attributes.FLYING_SPEED, 1)
                .add(Attributes.SCALE, 1.35)
                .add(Attributes.ATTACK_DAMAGE, 45.0)
                .add(Attributes.FOLLOW_RANGE, 100.0);
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

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity livingTarget) {
            livingTarget.addEffect(new MobEffectInstance(
                    MobEffects.BLINDNESS,
                    BLINDNESS_DURATION,
                    1,
                    false,
                    true
            ));
        }
        return hit;
    }

    @Override
    public @NotNull ResourceKey<LootTable> getDefaultLootTable() {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "entities/spirit_ghost")
        );
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}

    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingNavigation = new FlyingPathNavigation(this, level);
        flyingNavigation.setCanOpenDoors(false);
        flyingNavigation.setCanFloat(false);
        flyingNavigation.setCanPassDoors(false);
        return flyingNavigation;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.isAlive()) {
            BlockPos belowPos = this.blockPosition().below(1);
            if (!this.level().isEmptyBlock(belowPos) && this.getDeltaMovement().y < 0.1) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.02, 0));
            }
        }
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
    }

    private static class RamAndFleeGoal extends Goal {

        private enum Phase { CHARGING, FLEEING }

        private final SpiritGhostEntity spirit;
        private Phase phase = Phase.CHARGING;
        private int fleeTimer = 0;
        private Vec3 fleeTarget = null;

        public RamAndFleeGoal(SpiritGhostEntity spirit) {
            this.spirit = spirit;
        }

        @Override
        public boolean canUse() {
            return spirit.getTarget() != null && spirit.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() || phase == Phase.FLEEING && fleeTimer > 0;
        }

        @Override
        public void start() {
            phase = Phase.CHARGING;
            fleeTimer = 0;
        }

        @Override
        public void stop() {
            spirit.getNavigation().stop();
            phase = Phase.CHARGING;
            fleeTimer = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = spirit.getTarget();

            if (phase == Phase.CHARGING) {
                if (target == null || !target.isAlive()) {
                    spirit.getNavigation().stop();
                    return;
                }

                spirit.getNavigation().moveTo(
                        target.getX(), target.getEyeY(), target.getZ(), 4
                );

                double distanceSq = spirit.distanceToSqr(target);

                if (distanceSq <= RAM_HIT_DISTANCE * RAM_HIT_DISTANCE) {
                    spirit.doHurtTarget(target);
                    beginFlee(target);
                }

            } else {
                fleeTimer--;

                if (fleeTarget != null) {
                    spirit.getNavigation().moveTo(
                            fleeTarget.x, fleeTarget.y, fleeTarget.z, 1.5
                    );
                }

                if (fleeTimer <= 0) {
                    phase = Phase.CHARGING;
                    fleeTarget = null;
                }
            }
        }

        private void beginFlee(LivingEntity target) {
            phase = Phase.FLEEING;
            fleeTimer = FLEE_DURATION_TICKS;

            Vec3 awayDir = spirit.position().subtract(target.position()).normalize();
            double fleeDistance = 12.0 + spirit.getRandom().nextDouble() * 6.0;
            double fleeHeight  =  4.0;

            fleeTarget = spirit.position()
                    .add(awayDir.scale(fleeDistance))
                    .add(0, fleeHeight, 0);

            spirit.getNavigation().moveTo(
                    fleeTarget.x, fleeTarget.y, fleeTarget.z, 1.5
            );
        }
    }
}