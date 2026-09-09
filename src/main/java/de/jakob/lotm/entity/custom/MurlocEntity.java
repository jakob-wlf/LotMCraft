package de.jakob.lotm.entity.custom;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.WaterAnimal;
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

public class MurlocEntity extends WaterAnimal {

    public final AnimationState IDLE_ANIMATION = new AnimationState();
    public final AnimationState SWIM_ANIMATION = new AnimationState();

    private int outOfWaterTicks = 0;
    private static final int OUT_OF_WATER_GRACE_TICKS = 100;
    private static final int OUT_OF_WATER_DAMAGE_INTERVAL = 20;

    public MurlocEntity(EntityType<? extends WaterAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimAttackGoal(this));
        this.goalSelector.addGoal(1, new RandomSwimmingGoal(this, 1.0, 20));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return WaterAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 130.0)
                .add(Attributes.MOVEMENT_SPEED, 0.38)
                .add(Attributes.SCALE, 1.5)
                .add(Attributes.ATTACK_DAMAGE, 26.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 3.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (this.isInWater()) {
                outOfWaterTicks = 0;
            } else {
                outOfWaterTicks++;
                if (outOfWaterTicks > OUT_OF_WATER_GRACE_TICKS
                        && outOfWaterTicks % OUT_OF_WATER_DAMAGE_INTERVAL == 0) {
                    this.hurt(this.damageSources().drown(), 1.0f);
                }
            }
        }
    }

    @Override
    public @NotNull ResourceKey<LootTable> getDefaultLootTable() {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "entities/murloc")
        );
    }

    private static final DustParticleOptions MURLOC_DUST = new DustParticleOptions(new Vector3f(0.1f, 0.55f, 0.65f), 2.2f);
    private static final DustParticleOptions MURLOC_DUST_SMALL = new DustParticleOptions(new Vector3f(0.2f, 0.7f, 0.75f), 1.5f);

    protected DustParticleOptions getPrimaryDust() {
        return MURLOC_DUST;
    }

    protected DustParticleOptions getSecondaryDust() {
        return MURLOC_DUST_SMALL;
    }

    static class SwimAttackGoal extends Goal {

        private enum AttackType { NORMAL, HEAVY }

        private static final float MELEE_RANGE = 3.0f;
        private static final double HEAVY_CHANCE = 0.2;

        private final MurlocEntity murloc;
        private int attackCooldown;
        private AttackType currentAttack;

        public SwimAttackGoal(MurlocEntity murloc) {
            this.murloc = murloc;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            this.attackCooldown = 0;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = murloc.getTarget();
            return target != null && target.isAlive() && murloc.canAttack(target) && AbilityUtil.mayTarget(murloc, target);
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
            murloc.getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = murloc.getTarget();
            if (target == null || !target.isAlive()) return;

            double distanceSq = murloc.distanceToSqr(target);
            murloc.getLookControl().setLookAt(target, 30.0F, 30.0F);

            --attackCooldown;

            if (attackCooldown > 0) {
                if (distanceSq > MELEE_RANGE * MELEE_RANGE) {
                    murloc.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.2);
                } else {
                    murloc.getNavigation().stop();
                }
                return;
            }

            if (distanceSq > MELEE_RANGE * MELEE_RANGE) {
                // Not in range yet — keep swimming towards the target.
                murloc.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.2);
                return;
            }

            currentAttack = chooseAttack();

            switch (currentAttack) {
                case NORMAL -> performNormalAttack(target);
                case HEAVY -> performHeavyAttack(target);
            }
        }

        private AttackType chooseAttack() {
            if (murloc.getRandom().nextDouble() < HEAVY_CHANCE) {
                return AttackType.HEAVY;
            }
            return AttackType.NORMAL;
        }

        private void performNormalAttack(LivingEntity target) {
            strikeMelee(target, 1.0f, 0.4);
            attackCooldown = 15; // fast — this is the bread-and-butter attack
        }

        private void performHeavyAttack(LivingEntity target) {
            if (!(murloc.level() instanceof ServerLevel serverLevel)) return;

            // Quick lunge towards the target before the heavier strike lands.
            Vec3 lungeDir = target.position().subtract(murloc.position()).normalize();
            murloc.setDeltaMovement(murloc.getDeltaMovement().add(lungeDir.x * 0.6, lungeDir.y * 0.2, lungeDir.z * 0.6));

            ParticleUtil.spawnSphereParticles(serverLevel, murloc.getPrimaryDust(), murloc.position().add(0, 1, 0), 1.0, 30);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.BUBBLE_POP, murloc.position().add(0, 1, 0), 20, 0.4);
            murloc.playSound(SoundEvents.GENERIC_SPLASH, 1.0f, 0.9f);

            strikeMelee(target, 1.7f, 0.7);
            attackCooldown = 50; // powerful — longer cooldown than the normal strike
        }

        private void strikeMelee(LivingEntity target, float damageMultiplier, double knockback) {
            if (!(murloc.level() instanceof ServerLevel serverLevel)) return;

            float damage = (float) murloc.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
            murloc.swing(InteractionHand.MAIN_HAND);

            DamageSource source = murloc.damageSources().mobAttack(murloc);
            target.hurt(source, damage);
            target.knockback(knockback, murloc.getX() - target.getX(), murloc.getZ() - target.getZ());

            ParticleUtil.spawnParticles(serverLevel, murloc.getSecondaryDust(), target.position().add(0, 1, 0), 20, 0.4);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SPLASH, target.position().add(0, 1, 0), 15, 0.3);
        }
    }
}