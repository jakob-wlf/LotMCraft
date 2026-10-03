package de.jakob.lotm.entity.custom.spirits;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.custom.projectiles.SpiritBallEntity;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.EnumSet;

public class SpiritTranslucentWizardEntity extends Animal {

    public final AnimationState IDLE_ANIMATION = new AnimationState();
    public final AnimationState WALK_ANIMATION = new AnimationState();

    private static final int FREEZE_DURATION = 20;
    private static final float AOE_RADIUS = 9.0f;
    private static final float AOE_DAMAGE_FRACTION = 0.5f;
    private static final double JUMP_SLAM_DISTANCE = 3.0;

    // Translucent wizard uses purple/pink colors
    private static final DustParticleOptions TRANSLUCENT_DUST = new DustParticleOptions(new Vector3f(0.7f, 0.2f, 0.9f), 2.5f);
    private static final DustParticleOptions TRANSLUCENT_DUST_SMALL = new DustParticleOptions(new Vector3f(0.8f, 0.4f, 1.0f), 1.7f);

    public SpiritTranslucentWizardEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);

        this.moveControl = new MoveControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new TranslucentWizardAttackGoal(this));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 1.2, 32));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public @NotNull ResourceKey<LootTable> getDefaultLootTable() {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "entities/spirit_translucent_wizard")
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 280.0)
                .add(Attributes.MOVEMENT_SPEED, 1)
                .add(Attributes.FLYING_SPEED, 1.5)
                .add(Attributes.SCALE, 1.35)
                .add(Attributes.ATTACK_DAMAGE, 90.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.FOLLOW_RANGE, 50.0);
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

    static class TranslucentWizardAttackGoal extends Goal {

        private enum AttackType { AOE, FREEZE_PROJECTILE, JUMP_SLAM }

        private final SpiritTranslucentWizardEntity wizard;
        private int attackCooldown;
        private int attackPhase;
        private AttackType currentAttack;

        private boolean jumping;
        private int jumpTimer;

        public TranslucentWizardAttackGoal(SpiritTranslucentWizardEntity wizard) {
            this.wizard = wizard;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            this.attackCooldown = 0;
            this.attackPhase = 0;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = wizard.getTarget();
            return target != null && target.isAlive() && wizard.canAttack(target) && AbilityUtil.mayTarget(wizard, target);
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() || jumping;
        }

        @Override
        public void start() {
            attackCooldown = 20;
            attackPhase = 0;
            jumping = false;
        }

        @Override
        public void stop() {
            wizard.getNavigation().stop();
            jumping = false;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = wizard.getTarget();
            if (target == null || !target.isAlive()) return;

            double distanceSq = wizard.distanceToSqr(target);
            wizard.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (jumping) {
                tickJumpSlam(target);
                return;
            }

            --attackCooldown;

            if (attackCooldown > 0) {
                if (distanceSq > 10 * 10) {
                    wizard.getNavigation().moveTo(target.getX(), target.getEyeY(), target.getZ(), 1.5);
                }
                return;
            }

            currentAttack = chooseAttack(distanceSq);

            switch (currentAttack) {
                case AOE -> performAoEAttack(target);
                case FREEZE_PROJECTILE -> performFreezeProjectileAttack(target);
                case JUMP_SLAM -> performJumpSlam(target);
            }

            attackPhase = (attackPhase + 1) % 3;
        }

        private AttackType chooseAttack(double distanceSq) {
            return switch (attackPhase) {
                case 0 -> distanceSq < AOE_RADIUS * AOE_RADIUS * 4 ? AttackType.AOE : AttackType.FREEZE_PROJECTILE;
                case 1 -> AttackType.FREEZE_PROJECTILE;
                case 2 -> AttackType.JUMP_SLAM;
                default -> AttackType.FREEZE_PROJECTILE;
            };
        }

        private void performAoEAttack(LivingEntity target) {
            if (!(wizard.level() instanceof ServerLevel serverLevel)) return;

            float damage = (float) wizard.getAttributeValue(Attributes.ATTACK_DAMAGE) * AOE_DAMAGE_FRACTION;
            Vec3 center = wizard.position();

            // Purple/pink sphere particles
            ParticleUtil.spawnSphereParticles(serverLevel, TRANSLUCENT_DUST, center.add(0, 1, 0), AOE_RADIUS, 250);
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.WITCH, center.add(0, 1, 0), AOE_RADIUS * 0.7, 120);

            AbilityUtil.damageNearbyEntities(serverLevel, wizard, AOE_RADIUS, damage, center, true, true);

            attackCooldown = 60;
        }

        private void performFreezeProjectileAttack(LivingEntity target) {
            if (!(wizard.level() instanceof ServerLevel serverLevel)) return;

            float damage = (float) wizard.getAttributeValue(Attributes.ATTACK_DAMAGE);

            // Freeze the target
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, FREEZE_DURATION, 100, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.JUMP, FREEZE_DURATION, 128, false, false));

            // Purple freeze visual particles on target
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.WITCH, target.position().add(0, 1, 0), 1.0, 60);
            ParticleUtil.spawnParticles(serverLevel, TRANSLUCENT_DUST_SMALL, target.position().add(0, 1, 0), 40, 0.5);

            // Fire a magical projectile at the frozen target
            Vec3 projectilePos = wizard.position().add(0, 1.5, 0);
            Vec3 projectileDir = target.getEyePosition().subtract(projectilePos).normalize();
            SpiritBallEntity spiritBall = new SpiritBallEntity(wizard.level(), wizard, damage, projectileDir, 6);
            spiritBall.setPos(projectilePos);
            wizard.level().addFreshEntity(spiritBall);

            attackCooldown = 40;
        }

        private void performJumpSlam(LivingEntity target) {
            jumping = true;
            jumpTimer = 0;

            Vec3 toTarget = target.position().subtract(wizard.position()).normalize();
            double distance = wizard.distanceTo(target);
            double jumpSpeed = Math.min(distance, 15.0);

            wizard.setDeltaMovement(toTarget.x * jumpSpeed * 0.15, 0.8, toTarget.z * jumpSpeed * 0.15);
            wizard.hasImpulse = true;
        }

        private void tickJumpSlam(LivingEntity target) {
            jumpTimer++;

            if (!(wizard.level() instanceof ServerLevel serverLevel)) return;

            // Trail particles
            ParticleUtil.spawnParticles(serverLevel, TRANSLUCENT_DUST, wizard.position(), 3, 0.2);

            double distanceSq = wizard.distanceToSqr(target);

            if ((distanceSq <= JUMP_SLAM_DISTANCE * JUMP_SLAM_DISTANCE && jumpTimer > 5) || jumpTimer > 40) {
                // Slam impact
                float damage = (float) wizard.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.2f;
                Vec3 impactPos = wizard.position();

                // Impact particles - purple/pink themed
                ParticleUtil.spawnSphereParticles(serverLevel, TRANSLUCENT_DUST, impactPos, 3.0, 40);
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.EXPLOSION, impactPos, 5, 1.0);

                AbilityUtil.damageNearbyEntities(serverLevel, wizard, 4.0, damage, impactPos, true, true);

                jumping = false;
                jumpTimer = 0;
                attackCooldown = 50;
            }
        }
    }
}
