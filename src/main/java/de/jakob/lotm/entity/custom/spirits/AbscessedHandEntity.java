package de.jakob.lotm.entity.custom.spirits;

import de.jakob.lotm.LOTMCraft;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
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

public class AbscessedHandEntity extends Animal {

    public final AnimationState IDLE_ANIMATION = new AnimationState();
    public final AnimationState WALK_ANIMATION = new AnimationState();

    public AbscessedHandEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);

        this.moveControl = new MoveControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HandAttackGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.SCALE, 1.5)
                .add(Attributes.ATTACK_DAMAGE, 38.5)
                .add(Attributes.ARMOR, 22.0)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 64.0);
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
    public boolean onClimbable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public @NotNull ResourceKey<LootTable> getDefaultLootTable() {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "entities/abscessed_hand")
        );
    }

    /**
     * Particle colors used for this entity's effects. Subclass-friendly, in case
     * a variant wants a different color scheme.
     */
    private static final DustParticleOptions HAND_DUST = new DustParticleOptions(new Vector3f(0.35f, 0.55f, 0.2f), 2.5f);
    private static final DustParticleOptions HAND_DUST_SMALL = new DustParticleOptions(new Vector3f(0.45f, 0.65f, 0.25f), 1.7f);

    protected DustParticleOptions getPrimaryDust() {
        return HAND_DUST;
    }

    protected DustParticleOptions getSecondaryDust() {
        return HAND_DUST_SMALL;
    }

    static class HandAttackGoal extends Goal {

        private enum AttackType { MELEE, RANGED, TELEPORT_STRIKE }

        private static final float MELEE_RANGE = 3.5f;
        private static final double TELEPORT_MIN_DISTANCE = 8.0;
        private static final double TELEPORT_CHANCE = 0.35;

        private final AbscessedHandEntity hand;
        private int attackCooldown;
        private AttackType currentAttack;

        public HandAttackGoal(AbscessedHandEntity hand) {
            this.hand = hand;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            this.attackCooldown = 0;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = hand.getTarget();
            return target != null && target.isAlive() && hand.canAttack(target) && AbilityUtil.mayTarget(hand, target);
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            attackCooldown = 10;
        }

        @Override
        public void stop() {
            hand.getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = hand.getTarget();
            if (target == null || !target.isAlive()) return;

            double distanceSq = hand.distanceToSqr(target);
            hand.getLookControl().setLookAt(target, 30.0F, 30.0F);

            --attackCooldown;

            if (attackCooldown > 0) {
                if (distanceSq > MELEE_RANGE * MELEE_RANGE) {
                    hand.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.3);
                } else {
                    hand.getNavigation().stop();
                }
                return;
            }

            currentAttack = chooseAttack(distanceSq);

            switch (currentAttack) {
                case MELEE -> performMeleeAttack(target);
                case RANGED -> performRangedAttack(target);
                case TELEPORT_STRIKE -> performTeleportStrike(target);
            }
        }

        private AttackType chooseAttack(double distanceSq) {
            if (distanceSq <= MELEE_RANGE * MELEE_RANGE) {
                return AttackType.MELEE;
            }

            if (distanceSq >= TELEPORT_MIN_DISTANCE * TELEPORT_MIN_DISTANCE
                    && hand.getRandom().nextDouble() < TELEPORT_CHANCE) {
                return AttackType.TELEPORT_STRIKE;
            }

            return AttackType.RANGED;
        }

        private void performMeleeAttack(LivingEntity target) {
            if (hand.distanceToSqr(target) <= (MELEE_RANGE + 0.5f) * (MELEE_RANGE + 0.5f)) {
                strikeMelee(target, 1.0f);
            }
            attackCooldown = 15; // fast — this is the bread-and-butter attack
        }

        private void performRangedAttack(LivingEntity target) {
            if (!(hand.level() instanceof ServerLevel serverLevel)) return;

            float damage = (float) hand.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6f;

            ParticleUtil.spawnParticles(serverLevel, hand.getSecondaryDust(), hand.position().add(0, 1.5, 0), 30, 0.4);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SOUL_FIRE_FLAME, hand.position().add(0, 1.5, 0), 15, 0.3);

            Vec3 projectilePos = hand.position().add(0, 1.5, 0);
            Vec3 projectileDir = target.getEyePosition().subtract(projectilePos).normalize();
            SpiritBallEntity spiritBall = new SpiritBallEntity(hand.level(), hand, damage, projectileDir, 8);
            spiritBall.setPos(projectilePos);
            hand.level().addFreshEntity(spiritBall);

            attackCooldown = 35;
        }

        private void performTeleportStrike(LivingEntity target) {
            if (!(hand.level() instanceof ServerLevel serverLevel)) return;

            Vec3 origin = hand.position();
            Vec3 destination = findTeleportSpot(target);

            if (destination == null) {
                // No safe spot found nearby — fall back to a ranged attack instead
                performRangedAttack(target);
                return;
            }

            ParticleUtil.spawnSphereParticles(serverLevel, hand.getPrimaryDust(), origin.add(0, 1, 0), 1.5, 60);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.PORTAL, origin.add(0, 1, 0), 40, 0.5);

            hand.teleportTo(destination.x, destination.y, destination.z);
            hand.getNavigation().stop();

            ParticleUtil.spawnSphereParticles(serverLevel, hand.getPrimaryDust(), destination.add(0, 1, 0), 1.5, 60);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.PORTAL, destination.add(0, 1, 0), 40, 0.5);

            hand.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (hand.distanceToSqr(target) <= (MELEE_RANGE + 1.5f) * (MELEE_RANGE + 1.5f)) {
                strikeMelee(target, 1.3f); // surprise follow-up hits harder
            }

            attackCooldown = 70; // powerful — longer cooldown than melee/ranged
        }

        private void strikeMelee(LivingEntity target, float damageMultiplier) {
            if (!(hand.level() instanceof ServerLevel serverLevel)) return;

            float damage = (float) hand.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
            hand.swing(InteractionHand.MAIN_HAND);

            DamageSource source = hand.damageSources().mobAttack(hand);
            target.hurt(source, damage);
            target.knockback(0.4, hand.getX() - target.getX(), hand.getZ() - target.getZ());

            ParticleUtil.spawnParticles(serverLevel, hand.getPrimaryDust(), target.position().add(0, 1, 0), 25, 0.4);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.CRIT, target.position().add(0, 1, 0), 15, 0.3);
        }

        @Nullable
        private Vec3 findTeleportSpot(LivingEntity target) {
            for (int i = 0; i < 8; i++) {
                double angle = hand.getRandom().nextDouble() * Math.PI * 2;
                double dist = 2.0 + hand.getRandom().nextDouble() * 2.0;
                double x = target.getX() + Math.cos(angle) * dist;
                double z = target.getZ() + Math.sin(angle) * dist;
                BlockPos base = BlockPos.containing(x, target.getY(), z);

                for (int dy = -2; dy <= 2; dy++) {
                    BlockPos feet = base.above(dy);
                    BlockState feetState = hand.level().getBlockState(feet);
                    BlockState headState = hand.level().getBlockState(feet.above());
                    BlockState groundState = hand.level().getBlockState(feet.below());

                    if (feetState.isAir() && headState.isAir() && !groundState.isAir()) {
                        return new Vec3(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
                    }
                }
            }
            return null;
        }
    }
}