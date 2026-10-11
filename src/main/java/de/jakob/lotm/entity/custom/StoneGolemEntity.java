package de.jakob.lotm.entity.custom;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StoneGolemEntity extends Monster {

    public final AnimationState IDLE_ANIMATION = new AnimationState();
    public final AnimationState WALK_ANIMATION = new AnimationState();
    public final AnimationState ATTACK_ANIMATION = new AnimationState();
    public final AnimationState STOMP_ANIMATION = new AnimationState();

    private static final EntityDataAccessor<Boolean> DATA_ATTACKING =
            SynchedEntityData.defineId(StoneGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_STOMPING =
            SynchedEntityData.defineId(StoneGolemEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int ATTACK_ANIMATION_DURATION = 12;
    private static final int STOMP_ANIMATION_DURATION = 24;

    private static final int MELEE_COOLDOWN = 30;
    private static final double MELEE_RANGE = 2.5;
    private static final int MELEE_WINDUP_TICKS = 6;

    private static final int STOMP_COOLDOWN = 80;
    private static final double STOMP_RANGE = 7;
    private static final float STOMP_DAMAGE = 25.0F;
    private static final double STOMP_KNOCKBACK_STRENGTH = 1.4;
    private static final int STOMP_WINDUP_TICKS = 10;

    private int meleeCooldown = 0;
    private int stompCooldown = STOMP_COOLDOWN / 2;
    private int attackAnimationTicks = 0;
    private int stompAnimationTicks = 0;

    public StoneGolemEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 12;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, false);
        builder.define(DATA_STOMPING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new GolemStompAttackGoal(this));
        this.goalSelector.addGoal(2, new GolemMeleeAttackGoal(this));
        this.goalSelector.addGoal(3, new GolemChaseGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 220.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 40.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7);
    }

    @Override
    public @NotNull ResourceKey<LootTable> getDefaultLootTable() {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "entities/stone_golem")
        );
    }

    public static boolean checkStoneGolemSpawnRules(EntityType<StoneGolemEntity> type, ServerLevelAccessor level,
                                                    MobSpawnType spawnType, net.minecraft.core.BlockPos pos, RandomSource random) {
        final int CAVE_MAX_Y = 40;

        return pos.getY() <= CAVE_MAX_Y
                && !level.canSeeSky(pos)
                && Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide || !this.isAlive()) {
            return;
        }

        if (meleeCooldown > 0) meleeCooldown--;
        if (stompCooldown > 0) stompCooldown--;

        if (attackAnimationTicks > 0) {
            attackAnimationTicks--;
            if (attackAnimationTicks == 0) {
                this.entityData.set(DATA_ATTACKING, false);
            }
        }
        if (stompAnimationTicks > 0) {
            stompAnimationTicks--;
            if (stompAnimationTicks == 0) {
                this.entityData.set(DATA_STOMPING, false);
            }
        }
    }

    private void triggerAttackAnimation() {
        this.attackAnimationTicks = ATTACK_ANIMATION_DURATION;
        this.entityData.set(DATA_ATTACKING, true);
    }

    private void triggerStompAnimation() {
        this.stompAnimationTicks = STOMP_ANIMATION_DURATION;
        this.entityData.set(DATA_STOMPING, true);
    }

    public boolean isMeleeAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public boolean isStomping() {
        return this.entityData.get(DATA_STOMPING);
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    private static class GolemMeleeAttackGoal extends Goal {

        private final StoneGolemEntity golem;
        private int windupRemaining = 0;

        public GolemMeleeAttackGoal(StoneGolemEntity golem) {
            this.golem = golem;
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = golem.getTarget();
            if (target == null || !target.isAlive() || golem.meleeCooldown > 0) {
                return false;
            }
            return golem.distanceToSqr(target) <= MELEE_RANGE * MELEE_RANGE;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = golem.getTarget();
            return windupRemaining > 0 && target != null && target.isAlive();
        }

        @Override
        public void start() {
            windupRemaining = MELEE_WINDUP_TICKS;
            golem.getNavigation().stop();
            golem.triggerAttackAnimation();
        }

        @Override
        public void tick() {
            LivingEntity target = golem.getTarget();
            if (target != null) {
                golem.getLookControl().setLookAt(target, 30F, 30F);
            }
            windupRemaining--;
            if (windupRemaining == 0 && target != null) {
                golem.doHurtTarget(target);
                golem.meleeCooldown = MELEE_COOLDOWN;
            }
        }
    }

    private static class GolemChaseGoal extends Goal {

        private static final double CHASE_SPEED = 0.8;
        private static final int REPATH_INTERVAL = 10;

        private final StoneGolemEntity golem;

        public GolemChaseGoal(StoneGolemEntity golem) {
            this.golem = golem;
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        private boolean shouldYieldToStomp(LivingEntity target) {
            return golem.stompCooldown <= 0
                    && golem.distanceToSqr(target) <= STOMP_RANGE * STOMP_RANGE;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = golem.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (shouldYieldToStomp(target)) {
                return false;
            }
            return golem.distanceToSqr(target) > MELEE_RANGE * MELEE_RANGE;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = golem.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            return golem.distanceToSqr(target) > MELEE_RANGE * MELEE_RANGE;
        }

        @Override
        public void start() {
            LivingEntity target = golem.getTarget();
            if (target != null) {
                golem.getNavigation().moveTo(target, CHASE_SPEED);
            }
        }

        @Override
        public void tick() {
            LivingEntity target = golem.getTarget();
            if (target == null) {
                return;
            }

            golem.getLookControl().setLookAt(target, 30F, 30F);

            if (golem.tickCount % REPATH_INTERVAL == 0) {
                golem.getNavigation().moveTo(target, CHASE_SPEED);
            }
        }

        @Override
        public void stop() {
            golem.getNavigation().stop();
        }
    }

    private static class GolemStompAttackGoal extends Goal {

        private final StoneGolemEntity golem;
        private int windupRemaining = 0;

        private int shockwaveTick = -1;
        private static final int SHOCKWAVE_DURATION_TICKS = 10;

        public GolemStompAttackGoal(StoneGolemEntity golem) {
            this.golem = golem;
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = golem.getTarget();
            if (target == null || !target.isAlive() || golem.stompCooldown > 0) {
                return false;
            }
            return golem.distanceToSqr(target) <= STOMP_RANGE * STOMP_RANGE;
        }

        @Override
        public boolean canContinueToUse() {
            return windupRemaining > 0 || shockwaveTick >= 0;
        }

        @Override
        public void start() {
            windupRemaining = STOMP_WINDUP_TICKS;
            shockwaveTick = -1;
            golem.getNavigation().stop();
            golem.triggerStompAnimation();
        }

        @Override
        public void tick() {
            LivingEntity target = golem.getTarget();
            if (target != null) {
                golem.getLookControl().setLookAt(target, 30F, 30F);
            }

            if (windupRemaining > 0) {
                float progress = 1F - (windupRemaining / (float) STOMP_WINDUP_TICKS);
                spawnGroundGrabParticles(progress);

                windupRemaining--;
                if (windupRemaining == 0) {
                    performGroundSlam();
                    golem.stompCooldown = STOMP_COOLDOWN;
                    shockwaveTick = 0;
                }
                return;
            }

            if (shockwaveTick >= 0) {
                float t = shockwaveTick / (float) SHOCKWAVE_DURATION_TICKS;
                float radius = (float) (STOMP_RANGE * t);
                spawnShockwaveRing(radius, t);

                shockwaveTick++;
                if (shockwaveTick > SHOCKWAVE_DURATION_TICKS) {
                    shockwaveTick = -1;
                }
            }
        }

        private void performGroundSlam() {
            AABB stompArea = golem.getBoundingBox().inflate(STOMP_RANGE);
            List<LivingEntity> nearby = golem.level().getEntitiesOfClass(
                    LivingEntity.class,
                    stompArea,
                    entity -> entity != golem && entity.isAlive()
            );

            for (LivingEntity entity : nearby) {
                entity.hurt(golem.damageSources().mobAttack(golem), STOMP_DAMAGE);
                entity.knockback(STOMP_KNOCKBACK_STRENGTH, golem.getX() - entity.getX(), golem.getZ() - entity.getZ());
            }

            spawnImpactBurst();

            golem.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 0.7F);
            golem.playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.6F, 1.6F);
        }

        private void spawnGroundGrabParticles(float progress) {
            if (!(golem.level() instanceof ServerLevel serverLevel)) return;

            BlockParticleOption debris = groundDebrisParticle();
            float radius = (float) (STOMP_RANGE * (1F - progress) + 0.3F);
            int particlesThisTick = 2 + (int) (progress * 3);

            for (int i = 0; i < particlesThisTick; i++) {
                float angle = golem.getRandom().nextFloat() * (float) (Math.PI * 2);
                double px = golem.getX() + Mth.cos(angle) * radius;
                double pz = golem.getZ() + Mth.sin(angle) * radius;
                double py = golem.getY() + 0.1;

                double dx = (golem.getX() - px) * 0.15;
                double dz = (golem.getZ() - pz) * 0.15;
                double dy = 0.04 + progress * 0.08;

                serverLevel.sendParticles(debris, px, py, pz, 0, dx, dy, dz, 1.0);
            }

            if (golem.getRandom().nextFloat() < 0.3F) {
                serverLevel.sendParticles(ParticleTypes.POOF,
                        golem.getX(), golem.getY() + 0.1, golem.getZ(),
                        1, 0.15, 0.02, 0.15, 0.01);
            }
        }

        private void spawnImpactBurst() {
            if (!(golem.level() instanceof ServerLevel serverLevel)) return;

            BlockParticleOption debris = groundDebrisParticle();
            serverLevel.sendParticles(debris,
                    golem.getX(), golem.getY() + 0.1, golem.getZ(),
                    40, 0.6, 0.1, 0.6, 0.25);

            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    golem.getX(), golem.getY() + 0.2, golem.getZ(),
                    1, 0.0, 0.0, 0.0, 0.0);

            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    golem.getX(), golem.getY() + 0.1, golem.getZ(),
                    12, 0.3, 0.05, 0.3, 0.02);
        }

        private void spawnShockwaveRing(float radius, float t) {
            if (!(golem.level() instanceof ServerLevel serverLevel)) return;
            if (radius <= 0.05F) return;

            BlockParticleOption debris = groundDebrisParticle();
            int points = Math.max(12, (int) (radius * 8));

            for (int i = 0; i < points; i++) {
                float angle = i * ((float) (Math.PI * 2) / points);
                double x = golem.getX() + Mth.cos(angle) * radius;
                double z = golem.getZ() + Mth.sin(angle) * radius;
                double y = golem.getY() + 0.1;

                serverLevel.sendParticles(debris, x, y, z, 0, 0.0, 0.06, 0.0, 0.0);

                if (i % 3 == 0) {
                    serverLevel.sendParticles(ParticleTypes.POOF, x, y, z,
                            1, 0.05, 0.02, 0.05, 0.01);
                }
            }

            if (t > 0.8F && golem.getRandom().nextFloat() < 0.4F) {
                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        golem.getX(), golem.getY() + 0.1, golem.getZ(),
                        4, radius * 0.5, 0.02, radius * 0.5, 0.01);
            }
        }

        private BlockParticleOption groundDebrisParticle() {
            BlockState groundState = golem.level().getBlockState(golem.blockPosition().below());
            BlockState state = groundState.isAir() ? Blocks.STONE.defaultBlockState() : groundState;
            return new BlockParticleOption(ParticleTypes.BLOCK, state);
        }
    }
}