package de.jakob.lotm.entity.custom.spirits;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.projectiles.SpiritBallEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
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
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.EnumSet;

public class RabbitOfKnowledgeEntity extends Rabbit {

    public final AnimationState IDLE_ANIMATION = new AnimationState();
    public final AnimationState HOP_ANIMATION = new AnimationState();

    public RabbitOfKnowledgeEntity(EntityType<? extends Rabbit> entityType, Level level) {
        super(entityType, level);
        // Rabbit's own constructor already installs RabbitJumpControl / RabbitMoveControl,
        // so the hopping gait comes for free as long as we call super() here.
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RabbitAttackGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Rabbit.createAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.SCALE, 1.2)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.ARMOR_TOUGHNESS, 1.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public boolean isFood(@NotNull ItemStack itemStack) {
        // Not a normal tameable/breedable rabbit — disable the vanilla carrot-feeding behavior.
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
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "entities/rabbit_of_knowledge")
        );
    }

    /**
     * Particle colors used for this entity's effects. Subclass-friendly, in case
     * a variant wants a different color scheme.
     */
    private static final DustParticleOptions MYSTIC_DUST = new DustParticleOptions(new Vector3f(0.55f, 0.2f, 0.85f), 2.2f);
    private static final DustParticleOptions MYSTIC_DUST_SMALL = new DustParticleOptions(new Vector3f(0.7f, 0.4f, 0.95f), 1.5f);

    protected DustParticleOptions getPrimaryDust() {
        return MYSTIC_DUST;
    }

    protected DustParticleOptions getSecondaryDust() {
        return MYSTIC_DUST_SMALL;
    }

    static class RabbitAttackGoal extends Goal {

        private static final float MELEE_RANGE = 2.2f;

        private final RabbitOfKnowledgeEntity rabbit;
        private int meleeCooldown;
        private int mysticCooldown;

        public RabbitAttackGoal(RabbitOfKnowledgeEntity rabbit) {
            this.rabbit = rabbit;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = rabbit.getTarget();
            return target != null && target.isAlive() && rabbit.canAttack(target) && AbilityUtil.mayTarget(rabbit, target);
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            meleeCooldown = 5;
            mysticCooldown = 20;
        }

        @Override
        public void stop() {
            rabbit.getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = rabbit.getTarget();
            if (target == null || !target.isAlive()) return;

            double distanceSq = rabbit.distanceToSqr(target);
            rabbit.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (meleeCooldown > 0) meleeCooldown--;
            if (mysticCooldown > 0) mysticCooldown--;

            boolean inMeleeRange = distanceSq <= MELEE_RANGE * MELEE_RANGE;

            if (inMeleeRange && meleeCooldown <= 0) {
                performMeleeAttack(target);
                return;
            }

            if (!inMeleeRange && mysticCooldown <= 0) {
                performMysticAttack(target);
                // still close the distance while casting, so it doesn't just kite forever
                rabbit.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.1);
                return;
            }

            if (!inMeleeRange) {
                rabbit.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.2);
            } else {
                rabbit.getNavigation().stop();
            }
        }

        private void performMeleeAttack(LivingEntity target) {
            if (!(rabbit.level() instanceof ServerLevel serverLevel)) return;

            // A rabbit's melee is a pounce — trigger a real hop plus a small lunge toward the target.
            rabbit.getJumpControl().jump();
            Vec3 lungeDir = target.position().subtract(rabbit.position()).normalize();
            rabbit.setDeltaMovement(rabbit.getDeltaMovement().add(lungeDir.x * 0.35, 0.1, lungeDir.z * 0.35));

            float damage = (float) rabbit.getAttributeValue(Attributes.ATTACK_DAMAGE);
            rabbit.swing(InteractionHand.MAIN_HAND);

            DamageSource source = rabbit.damageSources().mobAttack(rabbit);
            target.hurt(source, damage);
            target.knockback(0.3, rabbit.getX() - target.getX(), rabbit.getZ() - target.getZ());

            ParticleUtil.spawnParticles(serverLevel, rabbit.getSecondaryDust(), target.position().add(0, 1, 0), 15, 0.3);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.CRIT, target.position().add(0, 1, 0), 10, 0.25);

            meleeCooldown = 20;
        }

        private void performMysticAttack(LivingEntity target) {
            if (!(rabbit.level() instanceof ServerLevel serverLevel)) return;

            float damage = (float) rabbit.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5f;

            ParticleUtil.spawnParticles(serverLevel, rabbit.getPrimaryDust(), rabbit.position().add(0, 0.7, 0), 25, 0.35);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.ENCHANT, rabbit.position().add(0, 0.7, 0), 20, 0.4);

            Vec3 projectilePos = rabbit.position().add(0, 0.7, 0);
            Vec3 projectileDir = target.getEyePosition().subtract(projectilePos).normalize();
            SpiritBallEntity mysticBolt = new SpiritBallEntity(rabbit.level(), rabbit, damage, projectileDir, 8);
            mysticBolt.setPos(projectilePos);
            rabbit.level().addFreshEntity(mysticBolt);

            mysticCooldown = 45;
        }
    }
}