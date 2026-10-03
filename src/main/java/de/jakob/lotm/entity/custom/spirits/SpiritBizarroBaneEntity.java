package de.jakob.lotm.entity.custom.spirits;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
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
import java.util.List;

public class SpiritBizarroBaneEntity extends Animal {

    public final AnimationState IDLE_ANIMATION = new AnimationState();
    public final AnimationState WALK_ANIMATION = new AnimationState();

    boolean isMarionettizing = false;
    boolean mustChargeBeforeMarionette = false;
    int chargeCooldown = 0;

    private static final int CHARGE_WINDUP = 35;
    private static final int CHARGE_ACTIVE = 20;
    private static final int CHARGE_COOLDOWN = 20 * 10;

    private int invisibilityCooldown = 20 * 15 + random.nextInt(20 * 10);

    private final DustParticleOptions threadParticle = new DustParticleOptions(new Vector3f(0.62f, 0.48f, 0.82f), 0.85f);

    public SpiritBizarroBaneEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new MoveControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MarionettizeGoal());
        this.goalSelector.addGoal(2, new ChargeGoal());
        this.goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.ATTACK_DAMAGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 2.0)
                .add(Attributes.SCALE, 1.35)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    int getMarionettizeDuration(LivingEntity target) {
        if (!BeyonderData.isBeyonder(target)) return 20 * 10;
        return switch (BeyonderData.getSequence(target)) {
            case 3 -> 20 * 210;
            case 4 -> 20 * 120;
            case 5 -> 20 * 35;
            default -> 20 * 10;
        };
    }

    boolean canBeMarionettized(LivingEntity target) {
        if (!BeyonderData.isBeyonder(target)) return true;
        return BeyonderData.getSequence(target) >= 3;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        boolean wasHurt = super.hurt(source, amount);
        if (wasHurt && isMarionettizing) {
            mustChargeBeforeMarionette = true;
        }
        return wasHurt;
    }

    @Override
    public @NotNull ResourceKey<LootTable> getDefaultLootTable() {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "entities/spirit_bizarro_bane")
        );
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (chargeCooldown > 0) chargeCooldown--;

        if (!isMarionettizing && !this.hasEffect(MobEffects.INVISIBILITY)) {
            if (invisibilityCooldown > 0) {
                invisibilityCooldown--;
            } else {
                this.addEffect(new MobEffectInstance(
                        MobEffects.INVISIBILITY, 20 * (3 + random.nextInt(4)), 0, false, false, false));
                invisibilityCooldown = 20 * (20 + random.nextInt(20));
            }
        }
    }

    private class MarionettizeGoal extends Goal {

        private LivingEntity target;
        private int ticksRemaining;
        private float targetHealthSnapshot;

        private static final double PREFERRED_DISTANCE = 12.0;
        private static final double TOO_CLOSE = 7.0;

        MarionettizeGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (mustChargeBeforeMarionette || chargeCooldown > 0) return false;
            LivingEntity t = getTarget();
            if (t == null || !t.isAlive()) return false;
            if (!canBeMarionettized(t)) return false;
            return distanceTo(t) <= 20;
        }

        @Override
        public boolean canContinueToUse() {
            if (mustChargeBeforeMarionette) return false;
            if (target == null || !target.isAlive()) return false;
            if (target.getHealth() < targetHealthSnapshot) {
                mustChargeBeforeMarionette = true;
                return false;
            }
            return ticksRemaining > 0;
        }

        @Override
        public void start() {
            target = getTarget();
            ticksRemaining = getMarionettizeDuration(target);
            targetHealthSnapshot = target.getHealth();
            isMarionettizing = true;
        }

        @Override
        public void stop() {
            isMarionettizing = false;
            if (target != null && target.isAlive() && ticksRemaining <= 0) {
                target.hurt(target.damageSources().generic(), Float.MAX_VALUE);
            }
            target = null;
        }

        @Override
        public void tick() {
            if (target == null) return;
            ticksRemaining--;

            getLookControl().setLookAt(target, 30, 30);

            double dist = distanceTo(target);
            if (dist < TOO_CLOSE) {
                Vec3 away = position().subtract(target.position()).normalize().scale(0.35);
                setDeltaMovement(away);
                hasImpulse = true;
            } else if (dist > PREFERRED_DISTANCE + 2) {
                getNavigation().moveTo(target, 0.6);
            } else {
                getNavigation().stop();
            }

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 4, false, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false, false));

            if (level() instanceof ServerLevel serverLevel) {
                spawnThreadParticles(serverLevel);
            }
        }

        private void spawnThreadParticles(ServerLevel serverLevel) {
            Vec3 start = getEyePosition();
            Vec3 end = target.getEyePosition();
            Vec3 axis = end.subtract(start);

            Vec3 perp1 = VectorUtil.getRandomPerpendicular(axis);
            Vec3 perp2 = VectorUtil.getRandomPerpendicular(axis);

            for (int i = 0; i < 2; i++) {
                Vec3 perp = i == 0 ? perp1 : perp2;
                Vec3 threadStart = start.add(perp.scale(i == 0 ? -0.25 : 0.25));

                float distance = (float) end.distanceTo(threadStart);
                int maxPoints = Math.max(2, Math.min(8, (int) Math.ceil(distance * 1.2)));

                List<Vec3> points = VectorUtil.createBezierCurve(
                        threadStart, end, perp, 0.03f, random.nextInt(1, maxPoints + 1));

                for (Vec3 point : points) {
                    ParticleUtil.spawnParticles(serverLevel, threadParticle, point, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    private class ChargeGoal extends Goal {

        private int windupTicks;
        private int activeTicks;
        private Vec3 chargeDirection;
        private LivingEntity target;

        ChargeGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!mustChargeBeforeMarionette) return false;
            LivingEntity t = getTarget();
            return t != null && t.isAlive() && distanceTo(t) < 28;
        }

        @Override
        public boolean canContinueToUse() {
            return windupTicks > 0 || (chargeDirection != null && activeTicks < CHARGE_ACTIVE);
        }

        @Override
        public void start() {
            target = getTarget();
            windupTicks = CHARGE_WINDUP;
            activeTicks = 0;
            chargeDirection = null;
            getNavigation().stop();
        }

        @Override
        public void stop() {
            mustChargeBeforeMarionette = false;
            chargeCooldown = CHARGE_COOLDOWN;
            chargeDirection = null;
        }

        @Override
        public void tick() {
            if (windupTicks > 0) {
                windupTicks--;
                if (target != null) getLookControl().setLookAt(target, 30, 30);

                if (windupTicks == 0) {
                    if (target != null && target.isAlive()) {
                        chargeDirection = target.getEyePosition().subtract(getEyePosition()).normalize();
                    }
                }
                return;
            }

            if (chargeDirection != null) {
                activeTicks++;
                setDeltaMovement(chargeDirection.scale(2.8));
                hasImpulse = true;

                if (target != null && target.isAlive() && distanceTo(target) < 2.5f) {
                    target.hurt(damageSources().mobAttack(SpiritBizarroBaneEntity.this),
                            (float) getAttributeValue(Attributes.ATTACK_DAMAGE));
                    activeTicks = CHARGE_ACTIVE;
                }
            }
        }
    }

    @Override
    public boolean isFood(@NotNull ItemStack itemStack) { return false; }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) { return null; }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource damageSource) { return false; }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}

    public boolean isFlying() { return !this.onGround(); }

    @Override
    public boolean onClimbable() { return false; }

    @Override
    public void aiStep() { super.aiStep(); }
}