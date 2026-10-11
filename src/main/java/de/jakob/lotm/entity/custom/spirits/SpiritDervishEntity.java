package de.jakob.lotm.entity.custom.spirits;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.projectiles.SpiritBallEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class SpiritDervishEntity extends Animal {

    public final AnimationState IDLE_ANIMATION = new AnimationState();


    public SpiritDervishEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);

        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.navigation = new FlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DervishAttackGoal(this));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 1.2, 32));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));


        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    public @NotNull ResourceKey<LootTable> getDefaultLootTable() {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "entities/spirit_dervish")
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.FLYING_SPEED, 2.5)
                .add(Attributes.SCALE, 1)
                .add(Attributes.ARMOR, 5.0)
                .add(Attributes.ATTACK_DAMAGE, 25.0)
                .add(Attributes.FOLLOW_RANGE, 30.0);
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
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

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

        // Add some upward movement when the entity is too low
        if (!this.level().isClientSide && this.isAlive()) {
            BlockPos belowPos = this.blockPosition().below(this.getTarget() == null ? 1 : 2);
            if (!this.level().isEmptyBlock(belowPos) && this.getDeltaMovement().y < 0.1) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.02, 0));
            }
        }
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    private void setupAnimationStates() {
        this.IDLE_ANIMATION.startIfStopped(this.tickCount);
    }

    @Override
    public void tick() {
        super.tick();

        Level level = this.level();

        if(level.isClientSide) {
            this.setupAnimationStates();
        }
    }

    static class DervishAttackGoal extends Goal { // Reused code from BlazeAttackGoal
        private final SpiritDervishEntity dervish;
        private int attackStep;
        private int attackTime;
        private int lastSeen;

        public DervishAttackGoal(SpiritDervishEntity dervish) {
            this.dervish = dervish;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        public boolean canUse() {
            LivingEntity livingentity = this.dervish.getTarget();
            return livingentity != null && livingentity.isAlive() && this.dervish.canAttack(livingentity) && AbilityUtil.mayTarget(this.dervish, livingentity);
        }

        public void start() {
            this.attackStep = 0;
        }

        public void stop() {
            this.lastSeen = 0;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            --this.attackTime;
            LivingEntity livingentity = this.dervish.getTarget();
            if (livingentity != null) {
                boolean flag = this.dervish.getSensing().hasLineOfSight(livingentity);
                if (flag) {
                    this.lastSeen = 0;
                } else {
                    ++this.lastSeen;
                }

                double d0 = this.dervish.distanceToSqr(livingentity);
                if (d0 < 4.0) {
                    if (!flag) {
                        return;
                    }

                    if (this.attackTime <= 0) {
                        this.attackTime = 10;
                        this.dervish.doHurtTarget(livingentity);
                    }

                    this.dervish.getMoveControl().setWantedPosition(livingentity.getX(), livingentity.getY(), livingentity.getZ(), 1.0);
                } else if (d0 < this.getFollowDistance() * this.getFollowDistance() && flag) {
                    if (this.attackTime <= 0) {
                        ++this.attackStep;
                        if (this.attackStep == 1) {
                            this.attackTime = 40;
                        } else if (this.attackStep <= 4) {
                            this.attackTime = 6;
                        } else {
                            this.attackTime = 60;
                            this.attackStep = 0;
                        }

                        if (this.attackStep > 1) {
                            for(int i = 0; i < 2; ++i) {
                                Vec3 spiritBallPos = this.dervish.position().offsetRandom(dervish.random, 1.5f);
                                Vec3 spiritBallDir = livingentity.getEyePosition().subtract(spiritBallPos).normalize();
                                SpiritBallEntity spiritBall = new SpiritBallEntity(this.dervish.level(), this.dervish, (float) this.dervish.getAttributeValue(Attributes.ATTACK_DAMAGE), spiritBallDir, 9);
                                spiritBall.setPos(spiritBallPos);
                                this.dervish.level().addFreshEntity(spiritBall);
                            }
                        }
                    }

                    this.dervish.getLookControl().setLookAt(livingentity, 10.0F, 10.0F);
                } else if (this.lastSeen < 5) {
                    this.dervish.getMoveControl().setWantedPosition(livingentity.getX(), livingentity.getY(), livingentity.getZ(), 1.0);
                }

                super.tick();
            }

        }

        private double getFollowDistance() {
            return this.dervish.getAttributeValue(Attributes.FOLLOW_RANGE);
        }
    }
}
