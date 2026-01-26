package de.jakob.lotm.entity.custom;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.AbilityItemHandler;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.custom.goals.RangedCombatGoal;
import de.jakob.lotm.entity.custom.goals.avatar.AvatarAbilityUseGoal;
import de.jakob.lotm.entity.custom.goals.avatar.AvatarRangedCombatGoal;
import de.jakob.lotm.entity.custom.goals.avatar.AvatarTargetGoal;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.potions.PotionRecipeItem;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AvatarEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> PATHWAY = SynchedEntityData.defineId(AvatarEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> SEQUENCE = SynchedEntityData.defineId(AvatarEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Optional<UUID>> ORIGINAL = SynchedEntityData.defineId(AvatarEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private String pathway = "error";
    private int sequence = 5;

    private ArrayList<AbilityItem> usableAbilities = new ArrayList<>();

    LivingEntity ownerEntity;

    public AvatarEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        this(entityType, level, null, "error", 5);
    }

    public AvatarEntity(EntityType<? extends PathfinderMob> entityType, Level level, UUID owner, String pathway, int sequence) {
        super(entityType, level);
        setOriginalOwner(owner);

        if(usableAbilities == null)
            usableAbilities = new ArrayList<>();

        if(!level.isClientSide) {
            this.pathway = pathway;
            this.sequence = sequence;
            BeyonderData.setBeyonder(this, pathway, sequence);

            this.entityData.set(PATHWAY, pathway);
            this.entityData.set(SEQUENCE, sequence);
        }

        if (!pathway.isEmpty()) {
            initializeAbilities(pathway, sequence);
        }
    }

    public ResourceLocation getSkinTexture() {
        String skinName = "amon";
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/npc/" + skinName + ".png");
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PATHWAY, "none");
        builder.define(SEQUENCE, 5);
        builder.define(ORIGINAL, Optional.empty());
    }

    private void syncEntityDataWithBeyonderData() {
        if (!this.level().isClientSide) {
            String currentPathway = BeyonderData.getPathway(this);
            int currentSequence = BeyonderData.getSequence(this);

            this.entityData.set(PATHWAY, currentPathway);
            this.entityData.set(SEQUENCE, currentSequence);
            this.entityData.set(ORIGINAL, Optional.ofNullable(getOriginalOwner()));
        }
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!this.level().isClientSide) {
            Entity owner = ((ServerLevel) level()).getEntity(getOriginalOwner());
            if (!(owner instanceof LivingEntity livingOwner)) {
                this.discard();
                return;
            }
            this.ownerEntity = livingOwner;
            // Sync beyonder data
            if (this.sequence != -1 && !this.pathway.equals("none")) {
                BeyonderData.setBeyonder(this, this.pathway, sequence);
                syncEntityDataWithBeyonderData();
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AvatarTargetGoal(this));
        this.goalSelector.addGoal(4, new AvatarAbilityUseGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        updateGoals();
    }

    private void updateGoals() {
        if(!getPathway().isEmpty())
            initializeAbilities(getPathway(), getSequence());
        else
            return;

        this.goalSelector.removeAllGoals(goal -> goal instanceof MeleeAttackGoal ||
                goal instanceof WaterAvoidingRandomStrollGoal ||
                goal instanceof MoveThroughVillageGoal ||
                goal instanceof RangedCombatGoal);
        this.targetSelector.removeAllGoals(goal -> goal instanceof NearestAttackableTargetGoal ||
                goal instanceof HurtByTargetGoal);

        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));

        if (hasRangedOption()) {
            this.goalSelector.addGoal(3, new AvatarRangedCombatGoal(this, 1.0D, 8.0F, 16.0F));
            this.goalSelector.addGoal(6, new MeleeAttackGoal(this, 1.2D, false));
        } else {
            this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        }

        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    public void setOriginalOwner(UUID ownerUUID) {
        this.entityData.set(ORIGINAL, Optional.ofNullable(ownerUUID));
    }

    public UUID getOriginalOwner() {
        return this.entityData.get(ORIGINAL).orElse(null);
    }

    public String getPathway() {
        if (this.level().isClientSide) {
            return this.entityData.get(PATHWAY);
        } else {
            return BeyonderData.getPathway(this);
        }
    }

    public int getSequence() {
        if (this.level().isClientSide) {
            return this.entityData.get(SEQUENCE);
        } else {
            return BeyonderData.getSequence(this);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        super.onSyncedDataUpdated(dataAccessor);

        if (this.level().isClientSide) {
            if (dataAccessor.equals(PATHWAY) || dataAccessor.equals(SEQUENCE)) {
                String pathway = this.entityData.get(PATHWAY);
                int sequence = this.entityData.get(SEQUENCE);

                ClientBeyonderCache.updateData(
                        this.getUUID(),
                        pathway,
                        sequence,
                        BeyonderData.getMaxSpirituality(sequence),
                        false,
                        false,
                        0.0f
                );
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("Pathway", getPathway());
        compound.putInt("Sequence", getSequence());
        compound.putUUID("OriginalOwner", getOriginalOwner() != null ? getOriginalOwner() : UUID.fromString("00000000-0000-0000-0000-000000000000"));
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
                this.entityData.set(ORIGINAL, originalOwner.equals(UUID.fromString("00000000-0000-0000-0000-000000000000")) ? Optional.empty() : Optional.of(originalOwner));
            }
        }

        if (!getPathway().isEmpty()) {
            initializeAbilities(getPathway(), getSequence());
        }
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NonNull DamageSource damageSource, boolean recentlyHit) {
        String pathway = getPathway();
        int sequence = getSequence();

        if(!BeyonderData.beyonderMap.check(pathway, sequence)) return;

        Random random = new Random();

        BeyonderCharacteristicItem characteristicItem = BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(pathway, sequence);
        if(characteristicItem != null) {
            this.spawnAtLocation(characteristicItem);
        }

        if(random.nextInt(4) == 0) {
            PotionRecipeItem recipeItem = PotionRecipeItemHandler.selectRecipeOfPathwayAndSequence(pathway, sequence);
            if(recipeItem != null) {
                this.spawnAtLocation(recipeItem);
            }
        }

        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    @Override
    protected void dropFromLootTable(DamageSource damageSource, boolean attackedRecently) {
        if(!BeyonderData.beyonderMap.check(pathway, sequence)) return;

        super.dropFromLootTable(damageSource, attackedRecently);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ARMOR, 0.0D);
    }

    @Override
    public void setTarget(@javax.annotation.Nullable LivingEntity target) {
        // Don't target the owner
        if (target != null && target.getUUID().equals(getOriginalOwner())) {
            return;
        }

        // Don't target entities that shouldn't be targeted
        if (target != null && !AbilityUtil.mayTarget(this, target)) {
            return;
        }

        super.setTarget(target);
    }

    @Override
    public void tick() {
        super.tick();

        if(this.level().isClientSide) {
            return;
        }

        if(tickCount > 20 * 60 * 60) {
            this.discard();
            return;
        }

        if(ownerEntity == null || !ownerEntity.isAlive()) {
            this.discard();
            return;
        }

        if(getTarget() != null) {
            if(getTarget().getUUID().equals(getOriginalOwner())) {
                this.setTarget(null);
            }
            if(!AbilityUtil.mayTarget(this, getTarget())) {
                this.setTarget(null);
            }
        }

        if(getCurrentTarget() != null) {
            if(getCurrentTarget().getUUID().equals(getOriginalOwner())) {
                this.setTarget(null);
            }
            if(!AbilityUtil.mayTarget(this, getCurrentTarget())) {
                this.setTarget(null);
            }
        }

        if(tickCount % 5 == 0) {
            PassiveAbilityHandler.ITEMS.getEntries().forEach(itemHolder -> {
                if (itemHolder.get() instanceof PassiveAbilityItem abilityItem) {
                    if (abilityItem.shouldApplyTo(this)) {
                        abilityItem.tick(this.level(), this);
                    }
                }
            });
        }
        if(tickCount == 1) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, 255, false, true, true));
        }
    }

    // Ability system methods
    private boolean hasRangedOption() {
        if(usableAbilities == null) {
            usableAbilities = new ArrayList<>();
            initializeAbilities(getPathway(), getSequence());
        }
        if(usableAbilities.isEmpty())
            return false;
        for (AbilityItem ability : usableAbilities) {
            if (ability.hasOptimalDistance) {
                return false;
            }
        }
        return true;
    }

    public void useAbility(Level level) {

        List<AbilityItem> usableAbilities = this.getUsableAbilities().stream().filter(a -> a.shouldUseAbility(this)).sorted(Comparator.comparing(AbilityItem::lowestSequenceUsable)).toList();

        if (usableAbilities.isEmpty()) {
            return;
        }

        // Calculate weights inversely proportional to sequence position
        // First item gets highest weight, last item gets weight of 1
        int size = usableAbilities.size();
        int totalWeight = (size * (size + 1)) / 2; // Sum of 1+2+3+...+n

        // Pick a random number in the weight range
        int randomValue = level.random.nextInt(totalWeight);

        // Find which ability this corresponds to
        int cumulativeWeight = 0;
        AbilityItem selectedAbility = usableAbilities.get(0); // fallback

        for (int i = 0; i < size; i++) {
            // Weight decreases: first item gets 'size' weight, last gets 1
            int weight = size - i;
            cumulativeWeight += weight;

            if (randomValue < cumulativeWeight) {
                selectedAbility = usableAbilities.get(i);
                break;
            }
        }

        selectedAbility.useAsNpcAbility(level, this);
    }

    public void tryUseAbility() {
        if (usableAbilities == null || usableAbilities.isEmpty()) {
            return;
        }

        useAbility(this.level());
    }
    private void initializeAbilities(String pathway, int sequence) {
        if(usableAbilities == null)
            usableAbilities = new ArrayList<>();
        else
            usableAbilities.clear();
        AbilityItemHandler.ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(a -> a instanceof AbilityItem)
                .map(a -> (AbilityItem) a)
                .filter(
                        a -> a.getRequirements().containsKey(pathway) && a.getRequirements().get(pathway) >= sequence
                )
                .filter(a -> a.canBeUsedByNPC)
                .forEach(usableAbilities::add);

        MarionetteComponent component = this.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if(component.isMarionette()) {
            Player controller = getController();
            if(controller == null)
                return;
            if(BeyonderData.isBeyonder(controller) && BeyonderData.getSequence(controller) <= 4) {
                String controllerPathway = BeyonderData.getPathway(controller);
                int controllerSequence = BeyonderData.getSequence(controller);
                AbilityItemHandler.ITEMS.getEntries()
                        .stream()
                        .map(DeferredHolder::get)
                        .filter(a -> a instanceof AbilityItem)
                        .map(a -> (AbilityItem) a)
                        .filter(
                                a -> a.getRequirements().containsKey(controllerPathway) && a.getRequirements().get(controllerPathway) >= controllerSequence
                        )
                        .filter(a -> a.canBeUsedByNPC)
                        .forEach(usableAbilities::add);

            }
        }
    }

    private Player getController() {
        Player controller;
        MarionetteComponent component = this.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (!component.isMarionette()) return null;

        try {
            UUID controllerUUID = UUID.fromString(component.getControllerUUID());
            controller = this.level().getPlayerByUUID(controllerUUID);
        } catch (IllegalArgumentException e) {
            return null;
        }

        return controller == null || !controller.isAlive() ? null : controller;
    }

    public ArrayList<AbilityItem> getUsableAbilities() {
        return usableAbilities;
    }

    /**
     * Gets the current target entity that this CustomPlayerEntity is attacking
     * @return The target entity, or null if not targeting anything
     */
    public LivingEntity getCurrentTarget() {
        return this.getTarget();
    }

    /**
     * Checks if the entity is currently in combat (has a target)
     * @return true if the entity has a target, false otherwise
     */
    public boolean isInCombat() {
        return this.getTarget() != null;
    }

    /**
     * Gets the reason this entity is currently attacking
     * @return AttackReason enum indicating why the entity is attacking
     */
    public AttackReason getAttackReason() {
        if (!isInCombat()) {
            return AttackReason.NOT_ATTACKING;
        }

        return AttackReason.RETALIATION;
    }

    /**
     * Enum to describe why the entity is attacking
     */
    public enum AttackReason {
        NOT_ATTACKING,      // Entity has no target
        HOSTILE_BEHAVIOR,   // Entity is hostile and actively seeking targets
        RETALIATION         // Entity is neutral but fighting back after being attacked
    }
}

