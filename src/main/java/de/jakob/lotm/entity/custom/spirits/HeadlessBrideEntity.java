package de.jakob.lotm.entity.custom.spirits;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.PlayPhotonBlockEffectPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class HeadlessBrideEntity extends Monster {

    public final AnimationState IDLE_ANIMATION = new AnimationState();
    public final AnimationState WALK_ANIMATION = new AnimationState();
    public final AnimationState ATTACK_ANIMATION = new AnimationState();
    public final AnimationState CURSE_ANIMATION = new AnimationState();

    private static final EntityDataAccessor<Boolean> DATA_ATTACKING =
            SynchedEntityData.defineId(HeadlessBrideEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CURSING =
            SynchedEntityData.defineId(HeadlessBrideEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int ATTACK_ANIMATION_DURATION = 14;
    private static final int CURSE_ANIMATION_DURATION = 26;

    private static final double MIN_HOVER_HEIGHT = 0.3;
    private static final double MAX_HOVER_HEIGHT = 1.4;
    private static final double ALTITUDE_PUSH_UP = 0.05;
    private static final double ALTITUDE_PUSH_DOWN = -0.05;
    private static final int GROUND_SCAN_DEPTH = 16;

    private static final int MELEE_COOLDOWN = 25;
    private static final double MELEE_RANGE = 2.5;
    private static final int MELEE_WINDUP_TICKS = 6;

    private static final int CURSE_COOLDOWN = 110;
    private static final double CURSE_MIN_RANGE = 4.0;
    private static final double CURSE_MAX_RANGE = 16.0;
    private static final int CURSE_WINDUP_TICKS = 14;
    private static final float CURSE_DAMAGE = 35.0F;
    private static final int CURSE_WITHER_DURATION = 100;
    private static final int CURSE_UNLUCK_DURATION = 600;
    private static final int CURSE_WEAKNESS_DURATION = 150;

    private static final double KITE_TOO_CLOSE_RANGE = 5.0;
    private static final double KITE_RETREAT_DISTANCE = 8.0;
    private static final float MELEE_COMMIT_CHANCE = 0.25F;
    private static final int MELEE_DECISION_COOLDOWN = 40;

    private int meleeCooldown = 0;
    private int curseCooldown = CURSE_COOLDOWN / 2;
    private int attackAnimationTicks = 0;
    private int curseAnimationTicks = 0;

    private boolean meleeChosenThisEncounter = false;
    private int meleeDecisionCooldown = 0;

    public HeadlessBrideEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.navigation = new FlyingPathNavigation(this, level);
        this.setNoGravity(true);
        this.xpReward = 32;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, false);
        builder.define(DATA_CURSING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BrideCurseAttackGoal(this));
        this.goalSelector.addGoal(2, new BrideMeleeAttackGoal(this));
        this.goalSelector.addGoal(3, new BridePositioningGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 340.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 1.0)
                .add(Attributes.SCALE, 1.15)
                .add(Attributes.ATTACK_DAMAGE, 100.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.FOLLOW_RANGE, 100.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7);
    }

    @Override
    public @NotNull ResourceKey<LootTable> getDefaultLootTable() {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "entities/headless_bride")
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

        if (meleeCooldown > 0) meleeCooldown--;
        if (curseCooldown > 0) curseCooldown--;
        if (meleeDecisionCooldown > 0) meleeDecisionCooldown--;

        if (this.getTarget() == null) {
            meleeChosenThisEncounter = false;
            meleeDecisionCooldown = 0;
        }

        if (attackAnimationTicks > 0) {
            attackAnimationTicks--;
            if (attackAnimationTicks == 0) {
                this.entityData.set(DATA_ATTACKING, false);
            }
        }
        if (curseAnimationTicks > 0) {
            curseAnimationTicks--;
            if (curseAnimationTicks == 0) {
                this.entityData.set(DATA_CURSING, false);
            }
        }

        maintainHoverAltitude();
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

    private void triggerAttackAnimation() {
        this.attackAnimationTicks = ATTACK_ANIMATION_DURATION;
        this.entityData.set(DATA_ATTACKING, true);
    }

    private void triggerCurseAnimation() {
        this.curseAnimationTicks = CURSE_ANIMATION_DURATION;
        this.entityData.set(DATA_CURSING, true);
    }

    public boolean isMeleeAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public boolean isCursing() {
        return this.entityData.get(DATA_CURSING);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.VEX_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }

    private static class BridePositioningGoal extends Goal {

        private final HeadlessBrideEntity bride;

        public BridePositioningGoal(HeadlessBrideEntity bride) {
            this.bride = bride;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = bride.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            LivingEntity target = bride.getTarget();
            if (target == null) return;

            double distanceSq = bride.distanceToSqr(target);

            if (distanceSq > CURSE_MAX_RANGE * CURSE_MAX_RANGE) {
                bride.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.0);
            } else if (distanceSq < KITE_TOO_CLOSE_RANGE * KITE_TOO_CLOSE_RANGE && !bride.meleeChosenThisEncounter) {
                Vec3 awayDir = bride.position().subtract(target.position());
                if (awayDir.lengthSqr() < 1.0E-4) {
                    awayDir = new Vec3(bride.random.nextDouble() - 0.5, 0, bride.random.nextDouble() - 0.5);
                }
                awayDir = awayDir.normalize();
                Vec3 retreatTarget = bride.position().add(awayDir.scale(KITE_RETREAT_DISTANCE));
                bride.getNavigation().moveTo(retreatTarget.x, retreatTarget.y, retreatTarget.z, 1.0);
            } else {
                bride.getNavigation().stop();
            }
        }
    }

    private static class BrideMeleeAttackGoal extends Goal {

        private final HeadlessBrideEntity bride;
        private int windupRemaining = 0;

        public BrideMeleeAttackGoal(HeadlessBrideEntity bride) {
            this.bride = bride;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = bride.getTarget();
            if (target == null || !target.isAlive() || bride.meleeCooldown > 0) {
                return false;
            }
            boolean inRange = bride.distanceToSqr(target) <= MELEE_RANGE * MELEE_RANGE;
            if (!inRange) {
                bride.meleeChosenThisEncounter = false;
                return false;
            }

            if (!bride.meleeChosenThisEncounter && bride.meleeDecisionCooldown <= 0) {
                bride.meleeChosenThisEncounter = bride.random.nextFloat() < MELEE_COMMIT_CHANCE;
                bride.meleeDecisionCooldown = MELEE_DECISION_COOLDOWN;
            }
            return bride.meleeChosenThisEncounter;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = bride.getTarget();
            return windupRemaining > 0 && target != null && target.isAlive();
        }

        @Override
        public void start() {
            windupRemaining = MELEE_WINDUP_TICKS;
            bride.triggerAttackAnimation();
        }

        @Override
        public void tick() {
            LivingEntity target = bride.getTarget();
            if (target != null) {
                bride.getLookControl().setLookAt(target, 30F, 30F);
            }
            windupRemaining--;
            if (windupRemaining == 0 && target != null) {
                bride.doHurtTarget(target);
                bride.meleeCooldown = MELEE_COOLDOWN;
                bride.meleeChosenThisEncounter = false;
            }
        }
    }

    private static class BrideCurseAttackGoal extends Goal {

        private final HeadlessBrideEntity bride;
        private int windupRemaining = 0;

        public BrideCurseAttackGoal(HeadlessBrideEntity bride) {
            this.bride = bride;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = bride.getTarget();
            if (target == null || !target.isAlive() || bride.curseCooldown > 0) {
                return false;
            }
            double distanceSq = bride.distanceToSqr(target);
            return distanceSq >= CURSE_MIN_RANGE * CURSE_MIN_RANGE
                    && distanceSq <= CURSE_MAX_RANGE * CURSE_MAX_RANGE
                    && bride.getSensing().hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = bride.getTarget();
            return windupRemaining > 0 && target != null && target.isAlive()
                    && bride.getSensing().hasLineOfSight(target);
        }

        @Override
        public void start() {
            windupRemaining = CURSE_WINDUP_TICKS;
            bride.triggerCurseAnimation();
            bride.playSound(SoundEvents.WITCH_CELEBRATE, 1.0F, 0.8F);

            if(bride.level() instanceof ServerLevel serverLevel) {
                BlockPos originBlock = BlockPos.containing(bride.position());

                Vec3 blockCenter = Vec3.atCenterOf(originBlock);
                Vec3 entityCorrection = bride.position().subtract(blockCenter);

                float yawRad = bride.getYRot() * Mth.DEG_TO_RAD;
                Vec3 forward = new Vec3(-Mth.sin(yawRad), 0.0, Mth.cos(yawRad));

                double distanceInFront = .8;
                Vec3 frontOffset = forward.scale(distanceInFront);

                double heightOffset = bride.getBbHeight() * 0.5;

                Vec3 totalOffset = entityCorrection
                        .add(frontOffset)
                        .add(new Vec3(0.0, heightOffset, 0.0));

                PlayPhotonBlockEffectPacket packet = new PlayPhotonBlockEffectPacket(
                        "headless_bride_curse_start",
                        originBlock,
                        totalOffset.x,
                        totalOffset.y,
                        totalOffset.z,
                        1.25,
                        null,
                        -1,
                        false,
                        false
                );

                PacketHandler.sendToNearbyPlayers(packet, serverLevel, bride.position(), 128);
            }

            LivingEntity target = bride.getTarget();
            if(target != null && target.level() instanceof ServerLevel serverLevel) {
                BlockPos originBlock = BlockPos.containing(target.position());

                Vec3 blockCenter = Vec3.atCenterOf(originBlock);
                Vec3 entityCorrection = target.position().subtract(blockCenter);

                PlayPhotonBlockEffectPacket packet = new PlayPhotonBlockEffectPacket(
                        "headless_bride_curse",
                        originBlock,
                        entityCorrection.x,
                        entityCorrection.y,
                        entityCorrection.z,
                        2,
                        null,
                        -1,
                        false,
                        false
                );

                PacketHandler.sendToNearbyPlayers(packet, serverLevel, target.position(), 128);
            }
        }

        @Override
        public void tick() {
            LivingEntity target = bride.getTarget();
            if (target != null) {
                bride.getLookControl().setLookAt(target, 30F, 30F);
            }
            windupRemaining--;
            if (windupRemaining == 0 && target != null) {
                target.hurt(bride.damageSources().mobAttack(bride), CURSE_DAMAGE);
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, CURSE_WITHER_DURATION, 0, false, true));
                target.addEffect(new MobEffectInstance(MobEffects.UNLUCK, CURSE_UNLUCK_DURATION, 0, false, true));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, CURSE_WEAKNESS_DURATION, 0, false, true));
                bride.curseCooldown = CURSE_COOLDOWN;
            }
        }

        @Override
        public void stop() {
            windupRemaining = 0;
        }
    }
}