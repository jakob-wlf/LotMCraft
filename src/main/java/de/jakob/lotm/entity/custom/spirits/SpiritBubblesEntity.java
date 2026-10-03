package de.jakob.lotm.entity.custom.spirits;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpiritBubblesEntity extends Animal {

    public final AnimationState IDLE_ANIMATION = new AnimationState();


    public SpiritBubblesEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);

        // Enable flying movement
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.navigation = new FlyingPathNavigation(this, level);
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
                .add(Attributes.FLYING_SPEED, 4)
                .add(Attributes.SCALE, 1);
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
        flyingNavigation.setCanFloat(true);
        flyingNavigation.setCanPassDoors(true);
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
}
