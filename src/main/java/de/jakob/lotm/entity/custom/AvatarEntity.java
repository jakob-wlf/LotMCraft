package de.jakob.lotm.entity.custom;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.goals.AbilityUseGoal;
import de.jakob.lotm.entity.custom.goals.RangedCombatGoal;
import de.jakob.lotm.entity.custom.goals.avatar.AvatarTargetGoal;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public class AvatarEntity extends PathfinderMob {

    private static final String DEFAULT_SKIN = "amon";
    private static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final EntityDataAccessor<String> PATHWAY =
            SynchedEntityData.defineId(AvatarEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> SEQUENCE =
            SynchedEntityData.defineId(AvatarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> ORIGINAL =
            SynchedEntityData.defineId(AvatarEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private String pathway = "none";
    private int sequence = 5;

    public AvatarEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        this(entityType, level, null, "none", 5);
    }

    public AvatarEntity(EntityType<? extends PathfinderMob> entityType, Level level,
                        UUID owner, String pathway, int sequence) {
        super(entityType, level);
        setOriginalOwner(owner);

        if (!level.isClientSide && !pathway.equalsIgnoreCase("none") && !pathway.isEmpty()) {
            this.pathway = pathway;
            this.sequence = sequence;
            BeyonderData.setBeyonder(this, pathway, sequence);
            this.entityData.set(PATHWAY, pathway);
            this.entityData.set(SEQUENCE, sequence);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PATHWAY, "none");
        builder.define(SEQUENCE, 5);
        builder.define(ORIGINAL, Optional.empty());
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        if (!this.level().isClientSide) {
            if (this.sequence != LOTMCraft.NON_BEYONDER_SEQ && !this.pathway.equals("none")) {
                BeyonderData.setBeyonder(this, this.pathway, this.sequence);
                this.entityData.set(PATHWAY, this.pathway);
                this.entityData.set(SEQUENCE, this.sequence);
                this.entityData.set(ORIGINAL, Optional.ofNullable(getOriginalOwner()));
            }
            updateGoals();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        super.onSyncedDataUpdated(dataAccessor);

        if (this.level().isClientSide && (dataAccessor.equals(PATHWAY) || dataAccessor.equals(SEQUENCE))) {
            String p = this.entityData.get(PATHWAY);
            int s = this.entityData.get(SEQUENCE);
            ClientBeyonderCache.updateData(this.getUUID(), p, s,
                    BeyonderData.getMaxSpirituality(p, s), false, false, 0.0f, 0);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AvatarTargetGoal(this));
        this.goalSelector.addGoal(4, new AbilityUseGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        updateGoals();
    }

    private void updateGoals() {
        if (getPathway().isEmpty() || getPathway().equals("none")) return;

        this.goalSelector.removeAllGoals(goal -> goal instanceof MeleeAttackGoal
                || goal instanceof WaterAvoidingRandomStrollGoal
                || goal instanceof RangedCombatGoal);
        this.targetSelector.removeAllGoals(goal -> goal instanceof NearestAttackableTargetGoal
                || goal instanceof HurtByTargetGoal);

        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));

        if (AbilityUseGoal.hasRangedOption(this)) {
            this.goalSelector.addGoal(3, new RangedCombatGoal(this, 1.0D, 8.0F, 16.0F));
        } else {
            this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        }

        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        validateTarget(getTarget());

        if (tickCount == 1) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, 255, false, true, true));
        }
    }

    private void validateTarget(LivingEntity target) {
        if (target == null) return;

        UUID owner = getOriginalOwner();
        if (owner != null && target.getUUID().equals(owner)) {
            this.setTarget(null);
            return;
        }

        if (!AbilityUtil.mayTarget(this, target)) {
            this.setTarget(null);
        }
    }

    @Override
    public void setTarget(@javax.annotation.Nullable LivingEntity target) {
        UUID owner = getOriginalOwner();
        if (target != null && owner != null && target.getUUID().equals(owner)) return;
        if (target != null && !AbilityUtil.mayTarget(this, target)) return;
        super.setTarget(target);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("Pathway", getPathway());
        compound.putInt("Sequence", getSequence());
        UUID owner = getOriginalOwner();
        compound.putUUID("OriginalOwner", owner != null ? owner : NULL_UUID);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains("Pathway") && compound.contains("Sequence")) {
            this.pathway = compound.getString("Pathway");
            this.sequence = compound.getInt("Sequence");
            UUID originalOwner = compound.getUUID("OriginalOwner");

            if (!this.level().isClientSide) {
                BeyonderData.setBeyonder(this, this.pathway, this.sequence);
                this.entityData.set(PATHWAY, this.pathway);
                this.entityData.set(SEQUENCE, this.sequence);
                this.entityData.set(ORIGINAL,
                        originalOwner.equals(NULL_UUID) ? Optional.empty() : Optional.of(originalOwner));
            }
        }
    }

    @Override
    protected void dropFromLootTable(DamageSource damageSource, boolean attackedRecently) {}

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ARMOR, 0.0D);
    }

    public ResourceLocation getSkinTexture() {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID,
                "textures/entity/npc/" + DEFAULT_SKIN + ".png");
    }

    public String getPathway() {
        return this.level().isClientSide ? this.entityData.get(PATHWAY) : BeyonderData.getPathway(this);
    }

    public int getSequence() {
        return this.level().isClientSide ? this.entityData.get(SEQUENCE) : BeyonderData.getSequence(this);
    }

    public void setOriginalOwner(UUID ownerUUID) {
        this.entityData.set(ORIGINAL, Optional.ofNullable(ownerUUID));
    }

    public UUID getOriginalOwner() {
        return this.entityData.get(ORIGINAL).orElse(null);
    }

    public LivingEntity getCurrentTarget() {
        return this.getTarget();
    }

    public boolean isInCombat() {
        return this.getTarget() != null;
    }

    public AttackReason getAttackReason() {
        return isInCombat() ? AttackReason.RETALIATION : AttackReason.NOT_ATTACKING;
    }

    public enum AttackReason {
        NOT_ATTACKING,
        HOSTILE_BEHAVIOR,
        RETALIATION
    }
}