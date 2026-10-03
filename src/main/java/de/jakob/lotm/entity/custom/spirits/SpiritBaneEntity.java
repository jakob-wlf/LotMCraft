package de.jakob.lotm.entity.custom.spirits;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.custom.projectiles.SpiritBlockProjectileEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class SpiritBaneEntity extends Animal {

    public final AnimationState IDLE_ANIMATION = new AnimationState();
    public final AnimationState WALK_ANIMATION = new AnimationState();

    private static final DustParticleOptions BANE_DUST = new DustParticleOptions(new Vector3f(0.6f, 0.0f, 0.0f), 2.0f);
    private static final DustParticleOptions BANE_DUST_DARK = new DustParticleOptions(new Vector3f(0.3f, 0.0f, 0.1f), 1.5f);

    // Sequence 4 threshold: lower number = stronger, so seq <= 4 means seq 0,1,2,3,4
    private static final int LIFT_SLAM_MAX_SEQUENCE = 4;
    private static final float LIFT_SLAM_DAMAGE_MULTIPLIER = 2.5f;
    private static final double LIFT_HEIGHT = 8.0;
    private static final int BLOCK_HURL_COUNT = 8;
    private static final int BLOCK_SEARCH_RADIUS = 5;

    public SpiritBaneEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new MoveControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BaneAttackGoal(this));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 1.2, 40));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 560.0)
                .add(Attributes.MOVEMENT_SPEED, 1)
                .add(Attributes.FLYING_SPEED, 1.2)
                .add(Attributes.SCALE, 1.35)
                .add(Attributes.ATTACK_DAMAGE, 100.0)
                .add(Attributes.ARMOR, 20.0)
                .add(Attributes.FOLLOW_RANGE, 60.0);
    }

    @Override
    public ResourceKey<LootTable> getDefaultLootTable() {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "entities/spirit_bane")
        );
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
    public void aiStep() {
        super.aiStep();
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
    }

    /**
     * Checks whether the target can be affected by the lift-slam attack.
     * Only targets at sequence 4 or weaker can be lifted.
     * In this system lower sequence numbers are more powerful (0 is strongest).
     * A target whose sequence number is >= 4 (i.e., 4, 5, 6, 7, 8, 9, or non-beyonder)
     * is considered weak enough to be lifted.
     */
    private static boolean canLiftTarget(LivingEntity target) {
        int targetSequence = BeyonderData.getSequence(target);
        return targetSequence >= LIFT_SLAM_MAX_SEQUENCE;
    }

    /**
     * Gathers block states from around the Bane spirit to use as projectile blocks.
     */
    private List<BlockState> getLocalBlockStates() {
        List<BlockState> states = new ArrayList<>();
        Level level = this.level();
        BlockPos center = this.blockPosition();

        for (int x = -BLOCK_SEARCH_RADIUS; x <= BLOCK_SEARCH_RADIUS; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -BLOCK_SEARCH_RADIUS; z <= BLOCK_SEARCH_RADIUS; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir() && state.getBlock() != Blocks.BEDROCK && state.isSolid()) {
                        states.add(state);
                    }
                }
            }
        }

        // Fallback if no blocks found
        if (states.isEmpty()) {
            states.add(Blocks.STONE.defaultBlockState());
            states.add(Blocks.COBBLESTONE.defaultBlockState());
            states.add(Blocks.DIRT.defaultBlockState());
        }

        return states;
    }

    static class BaneAttackGoal extends Goal {

        private final SpiritBaneEntity bane;
        private int attackCooldown;
        private boolean performingLiftSlam;
        private int liftSlamPhase; // 0=not active, 1=lifting, 2=holding, 3=slamming
        private int liftSlamTimer;
        private LivingEntity liftSlamTarget;

        public BaneAttackGoal(SpiritBaneEntity bane) {
            this.bane = bane;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            this.attackCooldown = 30;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = bane.getTarget();
            return target != null && target.isAlive() && bane.canAttack(target) && AbilityUtil.mayTarget(bane, target);
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() || performingLiftSlam;
        }

        @Override
        public void start() {
            attackCooldown = 30;
            performingLiftSlam = false;
        }

        @Override
        public void stop() {
            bane.getNavigation().stop();
            performingLiftSlam = false;
            liftSlamPhase = 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = bane.getTarget();
            if (target == null || !target.isAlive()) return;

            bane.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (performingLiftSlam) {
                tickLiftSlam();
                return;
            }

            --attackCooldown;
            double distanceSq = bane.distanceToSqr(target);

            // Move towards target
            if (distanceSq > 15 * 15) {
                bane.getNavigation().moveTo(target.getX(), target.getEyeY(), target.getZ(), 1.5);
            }

            if (attackCooldown <= 0) {
                // Try lift-slam on targets with sequence 4 or below
                if (canLiftTarget(target) && distanceSq < 20 * 20) {
                    performLiftSlam(target);
                } else {
                    performBlockHurl(target);
                }
            }
        }

        private void performLiftSlam(LivingEntity target) {
            performingLiftSlam = true;
            liftSlamPhase = 1; // Lifting
            liftSlamTimer = 0;
            liftSlamTarget = target;

            if (!(bane.level() instanceof ServerLevel serverLevel)) return;

            // Initial particle burst around the target
            ParticleUtil.spawnSphereParticles(serverLevel, BANE_DUST, target.position().add(0, 1, 0), 2.0, 30);
        }

        private void tickLiftSlam() {
            if (!(bane.level() instanceof ServerLevel serverLevel) || liftSlamTarget == null || !liftSlamTarget.isAlive()) {
                performingLiftSlam = false;
                liftSlamPhase = 0;
                attackCooldown = 40;
                return;
            }

            liftSlamTimer++;

            switch (liftSlamPhase) {
                case 1 -> tickLifting(serverLevel);
                case 2 -> tickHolding(serverLevel);
                case 3 -> tickSlamming(serverLevel);
            }
        }

        private void tickLifting(ServerLevel serverLevel) {
            // Lift the target upward over 20 ticks
            liftSlamTarget.setDeltaMovement(0, 0.7, 0);
            liftSlamTarget.hurtMarked = true;
            liftSlamTarget.hasImpulse = true;
            liftSlamTarget.fallDistance = 0;

            // Rising particles around target
            ParticleUtil.spawnParticles(serverLevel, BANE_DUST_DARK, liftSlamTarget.position(), 3, 0.3);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SMOKE, liftSlamTarget.position(), 2, 0.5);

            if (liftSlamTimer >= 20) {
                liftSlamPhase = 2; // Transition to holding
                liftSlamTimer = 0;
            }
        }

        private void tickHolding(ServerLevel serverLevel) {
            // Hold target in the air for 15 ticks
            liftSlamTarget.setDeltaMovement(0, 0.05, 0); // slight upward to counter gravity
            liftSlamTarget.hurtMarked = true;
            liftSlamTarget.hasImpulse = true;
            liftSlamTarget.fallDistance = 0;

            // Swirling particles while held
            ParticleUtil.spawnSphereParticles(serverLevel, BANE_DUST, liftSlamTarget.position().add(0, 1, 0), 1.5, 10);

            if (liftSlamTimer >= 15) {
                liftSlamPhase = 3; // Transition to slamming
                liftSlamTimer = 0;
            }
        }

        private void tickSlamming(ServerLevel serverLevel) {
            // Apply massive downward velocity
            liftSlamTarget.setDeltaMovement(0, -5.0, 0);
            liftSlamTarget.hurtMarked = true;
            liftSlamTarget.hasImpulse = true;

            // Trailing particles
            ParticleUtil.spawnParticles(serverLevel, BANE_DUST, liftSlamTarget.position(), 5, 0.3);

            // Check if target has landed or timer expired
            if (liftSlamTarget.onGround() || liftSlamTimer > 30) {
                // Impact!
                float damage = (float) bane.getAttributeValue(Attributes.ATTACK_DAMAGE) * LIFT_SLAM_DAMAGE_MULTIPLIER;
                Vec3 impactPos = liftSlamTarget.position();

                // Big impact particle burst
                ParticleUtil.spawnSphereParticles(serverLevel, BANE_DUST, impactPos, 4.0, 60);
                ParticleUtil.spawnSphereParticles(serverLevel, BANE_DUST_DARK, impactPos, 3.0, 40);
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.EXPLOSION, impactPos, 8, 2.0);
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.CAMPFIRE_COSY_SMOKE, impactPos, 10, 1.0);

                // Deal damage
                DamageSource damageSource = ModDamageTypes.source(bane.level(), ModDamageTypes.BEYONDER_GENERIC, bane);
                liftSlamTarget.hurt(damageSource, damage);
                liftSlamTarget.invulnerableTime = 0;

                // Also damage nearby entities at impact
                AbilityUtil.damageNearbyEntities(serverLevel, bane, 5.0, damage * 0.3f, impactPos, true, true);

                performingLiftSlam = false;
                liftSlamPhase = 0;
                attackCooldown = 80;
            }
        }

        private void performBlockHurl(LivingEntity target) {
            if (!(bane.level() instanceof ServerLevel serverLevel)) return;

            List<BlockState> localBlocks = bane.getLocalBlockStates();
            float damage = (float) bane.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4f; // per block
            Vec3 banePos = bane.position().add(0, 2, 0);

            // Spawn particles around the bane to show it's gathering blocks
            ParticleUtil.spawnSphereParticles(serverLevel, BANE_DUST, banePos, 3.0, 30);

            for (int i = 0; i < BLOCK_HURL_COUNT; i++) {
                // Select a random block state from nearby blocks
                BlockState blockState = localBlocks.get(bane.getRandom().nextInt(localBlocks.size()));

                // Spawn position: spread around the bane
                double offsetX = (bane.getRandom().nextDouble() - 0.5) * 4.0;
                double offsetY = bane.getRandom().nextDouble() * 3.0 + 1.0;
                double offsetZ = (bane.getRandom().nextDouble() - 0.5) * 4.0;
                Vec3 spawnPos = banePos.add(offsetX, offsetY, offsetZ);

                // Direction towards target with some spread
                Vec3 toTarget = target.getEyePosition().subtract(spawnPos).normalize();
                double spread = 0.15;
                Vec3 velocity = toTarget.add(
                        (bane.getRandom().nextDouble() - 0.5) * spread,
                        (bane.getRandom().nextDouble() - 0.5) * spread + 0.1,
                        (bane.getRandom().nextDouble() - 0.5) * spread
                ).scale(1.5);

                // Stagger the launches
                final Vec3 finalSpawnPos = spawnPos;
                final Vec3 finalVelocity = velocity;
                ServerScheduler.scheduleDelayed(i * 3, () -> {
                    SpiritBlockProjectileEntity blockProjectile = new SpiritBlockProjectileEntity(
                            bane.level(), bane, blockState, damage, finalSpawnPos, finalVelocity
                    );
                    bane.level().addFreshEntity(blockProjectile);

                    // Spawn particles at block launch point
                    ParticleUtil.spawnParticles(serverLevel, BANE_DUST_DARK, finalSpawnPos, 3, 0.3);
                }, serverLevel);
            }

            attackCooldown = 60;
        }
    }
}
