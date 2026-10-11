package de.jakob.lotm.entity.custom.spirits;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
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
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.List;

public class SpiritMalmouthEntity extends Animal {

    public final AnimationState IDLE_ANIMATION = new AnimationState();
    public final AnimationState WALK_ANIMATION = new AnimationState();

    private static final float SOUL_BREATH_RANGE = 8.0f;
    private static final float SOUL_BREATH_ANGLE = 45.0f; // degrees, half-angle of the cone
    private static final double SOUL_BREATH_COS_THRESHOLD = Math.cos(Math.toRadians(SOUL_BREATH_ANGLE));
    private static final DustParticleOptions SOUL_DUST = new DustParticleOptions(new Vector3f(0.1f, 0.8f, 0.6f), 1.2f); // Teal/soul color
    private static final DustParticleOptions SOUL_DUST_DARK = new DustParticleOptions(new Vector3f(0.05f, 0.4f, 0.3f), 1.5f); // Dark soul

    public SpiritMalmouthEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.navigation = new FlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SoulBreathAttackGoal(this));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 1.2, 32));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 160.0)
                .add(Attributes.MOVEMENT_SPEED, 1)
                .add(Attributes.FLYING_SPEED, 1.2)
                .add(Attributes.SCALE, 1.35)
                .add(Attributes.ATTACK_DAMAGE, 50.0)
                .add(Attributes.ARMOR, 5.0)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public @NotNull ResourceKey<LootTable> getDefaultLootTable() {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "entities/spirit_malmouth")
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
            BlockPos belowPos = this.blockPosition().below(3);
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

    static class SoulBreathAttackGoal extends Goal {

        private final SpiritMalmouthEntity malmouth;
        private int attackCooldown;
        private int breathTimer; // ticks remaining in current breath attack
        private boolean breathing;
        private Vec3 breathDirection;

        public SoulBreathAttackGoal(SpiritMalmouthEntity malmouth) {
            this.malmouth = malmouth;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            this.attackCooldown = 0;
            this.breathing = false;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = malmouth.getTarget();
            return target != null && target.isAlive() && malmouth.canAttack(target) && AbilityUtil.mayTarget(malmouth, target);
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() || breathing;
        }

        @Override
        public void start() {
            attackCooldown = 20;
            breathing = false;
        }

        @Override
        public void stop() {
            malmouth.getNavigation().stop();
            breathing = false;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = malmouth.getTarget();
            if (target == null || !target.isAlive()) return;

            malmouth.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (breathing) {
                tickBreathAttack(target);
                return;
            }

            --attackCooldown;
            double distanceSq = malmouth.distanceToSqr(target);

            // Move closer if too far
            if (distanceSq > SOUL_BREATH_RANGE * SOUL_BREATH_RANGE) {
                malmouth.getNavigation().moveTo(target.getX(), target.getEyeY(), target.getZ(), 1.5);
                return;
            }

            if (attackCooldown <= 0) {
                // Start soul breath
                breathing = true;
                breathTimer = 30; // 1.5 seconds of breath
                breathDirection = target.position().subtract(malmouth.position()).normalize();
                malmouth.getNavigation().stop();
            } else {
                // Slowly orbit/approach target
                malmouth.getNavigation().moveTo(target.getX(), target.getEyeY(), target.getZ(), 0.8);
            }
        }

        private void tickBreathAttack(LivingEntity target) {
            if (!(malmouth.level() instanceof ServerLevel serverLevel)) return;

            breathTimer--;

            // Update breath direction to follow target somewhat
            Vec3 toTarget = target.position().subtract(malmouth.position()).normalize();
            breathDirection = breathDirection.scale(0.7).add(toTarget.scale(0.3)).normalize();

            Vec3 mouthPos = malmouth.position().add(0, 1.2, 0);
            float damage = (float) malmouth.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.15f; // per-tick damage

            // Spawn soul flame particles in a cone
            for (int i = 0; i < 8; i++) {
                double distance = malmouth.getRandom().nextDouble() * SOUL_BREATH_RANGE;
                double spreadAngle = (malmouth.getRandom().nextDouble() - 0.5) * Math.toRadians(SOUL_BREATH_ANGLE * 2);
                double verticalSpread = (malmouth.getRandom().nextDouble() - 0.5) * 0.5;

                // Rotate breath direction by spread angle
                double cos = Math.cos(spreadAngle);
                double sin = Math.sin(spreadAngle);
                double rotatedX = breathDirection.x * cos - breathDirection.z * sin;
                double rotatedZ = breathDirection.x * sin + breathDirection.z * cos;

                Vec3 particlePos = mouthPos.add(rotatedX * distance, breathDirection.y * distance + verticalSpread, rotatedZ * distance);

                // Alternate between soul flame and soul particles
                if (i % 2 == 0) {
                    serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, particlePos.x, particlePos.y, particlePos.z, 1, 0.1, 0.1, 0.1, 0.02);
                } else {
                    serverLevel.sendParticles(ParticleTypes.SOUL, particlePos.x, particlePos.y, particlePos.z, 1, 0.1, 0.1, 0.1, 0.02);
                }
            }

            // Additional soul dust particles for the "flame" look
            ParticleUtil.spawnParticles(serverLevel, SOUL_DUST, mouthPos.add(breathDirection.scale(SOUL_BREATH_RANGE * 0.5)), 3, 1.5);
            ParticleUtil.spawnParticles(serverLevel, SOUL_DUST_DARK, mouthPos.add(breathDirection.scale(2)), 2, 0.5);

            // Damage entities in the cone
            AABB detectionBox = new AABB(
                    mouthPos.x - SOUL_BREATH_RANGE, mouthPos.y - 2, mouthPos.z - SOUL_BREATH_RANGE,
                    mouthPos.x + SOUL_BREATH_RANGE, mouthPos.y + 2, mouthPos.z + SOUL_BREATH_RANGE
            );
            List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, detectionBox);

            double cosThreshold = SOUL_BREATH_COS_THRESHOLD;

            for (LivingEntity entity : entities) {
                if (entity == malmouth) continue;
                if (!AbilityUtil.mayTarget(malmouth, entity)) continue;

                Vec3 toEntity = entity.position().add(0, entity.getBbHeight() / 2, 0).subtract(mouthPos);
                double dist = toEntity.length();

                if (dist > SOUL_BREATH_RANGE || dist < 0.5) continue;

                Vec3 toEntityNorm = toEntity.normalize();
                double dot = toEntityNorm.dot(breathDirection);

                if (dot >= cosThreshold) {
                    // Entity is within the cone
                    DamageSource damageSource = ModDamageTypes.source(malmouth.level(), ModDamageTypes.BEYONDER_GENERIC, malmouth);
                    entity.hurt(damageSource, damage);
                    entity.invulnerableTime = 0; // Allow continuous breath damage
                }
            }

            if (breathTimer <= 0) {
                breathing = false;
                attackCooldown = 50;
            }
        }
    }
}
