package de.jakob.lotm.entity.custom;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

public class CustomPlayerEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> IS_HOSTILE = SynchedEntityData.defineId(CustomPlayerEntity.class, EntityDataSerializers.BOOLEAN);

    private boolean defaultHostile;

    public CustomPlayerEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        this(entityType, level, false); // Default to neutral
    }

    public CustomPlayerEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile) {
        super(entityType, level);
        this.defaultHostile = hostile;
        this.setHostile(hostile);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_HOSTILE, false);
    }

    @Override
    protected void registerGoals() {
        // Basic goals that both hostile and neutral mobs need
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new PanicGoal(this, 1.25D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // Conditional goals based on hostility
        updateGoalsBasedOnHostility();
    }

    private void updateGoalsBasedOnHostility() {
        // Clear existing combat goals
        this.goalSelector.removeAllGoals(goal -> goal instanceof MeleeAttackGoal ||
                goal instanceof WaterAvoidingRandomStrollGoal ||
                goal instanceof MoveThroughVillageGoal);
        this.targetSelector.removeAllGoals(goal -> goal instanceof NearestAttackableTargetGoal ||
                goal instanceof HurtByTargetGoal);

        // Both hostile and neutral entities can fight back when attacked
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));

        // Always retaliate when hurt (both hostile and neutral)
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));

        if (isHostile()) {
            // Hostile behavior - actively seek players
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }
        // Neutral entities only fight back when attacked (handled by HurtByTargetGoal)
    }

    public boolean isHostile() {
        return this.entityData.get(IS_HOSTILE);
    }

    public void setHostile(boolean hostile) {
        this.entityData.set(IS_HOSTILE, hostile);
        if (!this.level().isClientSide) {
            updateGoalsBasedOnHostility();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsHostile", isHostile());
        compound.putBoolean("DefaultHostile", defaultHostile);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.defaultHostile = compound.getBoolean("DefaultHostile");
        setHostile(compound.getBoolean("IsHostile"));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ARMOR, 0.0D);
    }

    // Eye height is handled automatically based on entity dimensions

    // Make the mob neutral by default (only attacks when provoked if not hostile)
    @Override
    public boolean isAggressive() {
        return isHostile() || super.isAggressive();
    }

    /**
     * Gets the current target entity that this CustomPlayerEntity is attacking
     * @return The target entity, or null if not targeting anything
     */
    public LivingEntity getCurrentTarget() {
        return this.getTarget();
    }

    /**
     * Checks if the entity is currently in combat (has a target)
     * @return true if the entity has a target, false otherwise
     */
    public boolean isInCombat() {
        return this.getTarget() != null;
    }

    /**
     * Gets the reason this entity is currently attacking
     * @return AttackReason enum indicating why the entity is attacking
     */
    public AttackReason getAttackReason() {
        if (!isInCombat()) {
            return AttackReason.NOT_ATTACKING;
        }

        if (isHostile()) {
            return AttackReason.HOSTILE_BEHAVIOR;
        } else {
            return AttackReason.RETALIATION;
        }
    }

    /**
     * Enum to describe why the entity is attacking
     */
    public enum AttackReason {
        NOT_ATTACKING,      // Entity has no target
        HOSTILE_BEHAVIOR,   // Entity is hostile and actively seeking targets
        RETALIATION         // Entity is neutral but fighting back after being attacked
    }
}

