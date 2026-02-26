package de.jakob.lotm.entity.custom;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.core.Ability;
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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AvatarEntity extends PathfinderMob {
    // ========================= Constants =========================
    private static final String DEFAULT_SKIN = "amon";
    private static final String DEFAULT_PATHWAY = "error";
    private static final int DEFAULT_SEQUENCE = 5;
    private static final int MAX_LIFETIME_TICKS = 20 * 60 * 2; // 1 hour
    private static final int RECIPE_DROP_CHANCE = 4;
    private static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    // ========================= Entity Data Accessors =========================
    private static final EntityDataAccessor<String> PATHWAY =
            SynchedEntityData.defineId(AvatarEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> SEQUENCE =
            SynchedEntityData.defineId(AvatarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> ORIGINAL =
            SynchedEntityData.defineId(AvatarEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    // ========================= Instance Fields =========================
    private String pathway = DEFAULT_PATHWAY;
    private int sequence = DEFAULT_SEQUENCE;
    private ArrayList<Ability> usableAbilities = new ArrayList<>();
    private LivingEntity ownerEntity;

    // ========================= Constructors =========================
    public AvatarEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        this(entityType, level, null, DEFAULT_PATHWAY, DEFAULT_SEQUENCE);
    }

    public AvatarEntity(EntityType<? extends PathfinderMob> entityType, Level level,
                        UUID owner, String pathway, int sequence) {
        super(entityType, level);
        setOriginalOwner(owner);

        // Initialize beyonder data on server side
        if (!level.isClientSide) {
            this.pathway = pathway;
            this.sequence = sequence;
            BeyonderData.setBeyonder(this, pathway, sequence);
            this.entityData.set(PATHWAY, pathway);
            this.entityData.set(SEQUENCE, sequence);
        }

        // Initialize abilities if pathway is valid
        if (!pathway.isEmpty()) {
            initializeAbilities(pathway, sequence);
        }
    }

    // ========================= Entity Data Initialization =========================
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PATHWAY, "none");
        builder.define(SEQUENCE, DEFAULT_SEQUENCE);
        builder.define(ORIGINAL, Optional.empty());
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        if (!this.level().isClientSide) {
            // Validate owner exists
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

    // ========================= Data Synchronization =========================
    private void syncEntityDataWithBeyonderData() {
        if (!this.level().isClientSide) {
            String currentPathway = BeyonderData.getPathway(this);
            int currentSequence = BeyonderData.getSequence(this);
            this.entityData.set(PATHWAY, currentPathway);
            this.entityData.set(SEQUENCE, currentSequence);
            this.entityData.set(ORIGINAL, Optional.ofNullable(getOriginalOwner()));
        }
    }

    // ========================= NBT Data Persistence =========================
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

        if (!getPathway().isEmpty()) {
            initializeAbilities(getPathway(), getSequence());
        }
    }

    // ========================= AI Goals =========================
    @Override
    protected void registerGoals() {
        // Basic goals for avatars
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AvatarTargetGoal(this));
        this.goalSelector.addGoal(4, new AvatarAbilityUseGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        updateGoals();
    }

    private void updateGoals() {
        if (getPathway().isEmpty()) {
            return;
        }

        initializeAbilities(getPathway(), getSequence());

        // Clear combat-related goals
        this.goalSelector.removeAllGoals(goal -> goal instanceof MeleeAttackGoal ||
                goal instanceof WaterAvoidingRandomStrollGoal ||
                goal instanceof MoveThroughVillageGoal ||
                goal instanceof RangedCombatGoal);
        this.targetSelector.removeAllGoals(goal -> goal instanceof NearestAttackableTargetGoal ||
                goal instanceof HurtByTargetGoal);

        // Add retaliation behavior
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));

        // Add combat goals based on abilities
        if (hasRangedOption()) {
            this.goalSelector.addGoal(3, new AvatarRangedCombatGoal(this, 1.0D, 8.0F, 16.0F));
        } else {
            this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        }

        // Add movement and targeting goals
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    // ========================= Tick Logic =========================
    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        // Handle lifetime limit
        if (tickCount > MAX_LIFETIME_TICKS) {
            this.discard();
            return;
        }

        // Validate owner is still alive
        if (ownerEntity == null || !ownerEntity.isAlive()) {
            this.discard();
            return;
        }

        // Validate targets
        validateTarget(getTarget());
        validateTarget(getCurrentTarget());

        // Apply initial regeneration
        if (tickCount == 1) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, 255, false, true, true));
        }
    }

    private void validateTarget(LivingEntity target) {
        if (target == null) {
            return;
        }

        UUID originalOwner = getOriginalOwner();
        if (originalOwner != null && target.getUUID().equals(originalOwner)) {
            this.setTarget(null);
            return;
        }

        if (!AbilityUtil.mayTarget(this, target)) {
            this.setTarget(null);
        }
    }

    @Override
    public void setTarget(@javax.annotation.Nullable LivingEntity target) {
        // Don't target the owner
        UUID originalOwner = getOriginalOwner();
        if (target != null && originalOwner != null && target.getUUID().equals(originalOwner)) {
            return;
        }

        // Don't target entities that shouldn't be targeted
        if (target != null && !AbilityUtil.mayTarget(this, target)) {
            return;
        }

        super.setTarget(target);
    }

    // ========================= Ability System =========================
    private void initializeAbilities(String pathway, int sequence) {
        if (usableAbilities == null) {
            usableAbilities = new ArrayList<>();
        }
        usableAbilities.clear();

        LOTMCraft.abilityHandler.getByPathwayAndSequence(pathway, sequence).stream()
                .filter(a -> a.canBeUsedByNPC)
                .forEach(usableAbilities::add);

        // Add controller abilities if this is a marionette
        MarionetteComponent component = this.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (component.isMarionette()) {
            Player controller = getController();
            if (controller != null && BeyonderData.isBeyonder(controller) && BeyonderData.getSequence(controller) <= 4) {
                String controllerPathway = BeyonderData.getPathway(controller);
                int controllerSequence = BeyonderData.getSequence(controller);
                LOTMCraft.abilityHandler.getByPathwayAndSequence(controllerPathway, controllerSequence).stream()
                        .filter(a -> a.canBeUsedByNPC)
                        .forEach(usableAbilities::add);
            }
        }
    }

    private boolean hasRangedOption() {
        if (usableAbilities.isEmpty()) {
            return false;
        }

        return usableAbilities.stream().anyMatch(ability -> ability.hasOptimalDistance);
    }

    public void useAbility(Level level) {
        if (level.isClientSide) {
            return;
        }

        List<Ability> availableAbilities = usableAbilities.stream()
                .filter(a -> a.shouldUseAbility(this) && a.canUse(this))
                .sorted(Comparator.comparing(Ability::lowestSequenceUsable))
                .toList();

        if (availableAbilities.isEmpty()) {
            return;
        }

        Ability selectedAbility = selectWeightedAbility(availableAbilities, new Random());
        selectedAbility.useAbility((ServerLevel) level, this);
    }

    /**
     * Selects an ability with weighted probability favoring earlier abilities in the list.
     */
    private Ability selectWeightedAbility(List<Ability> abilities, Random random) {
        int size = abilities.size();
        int totalWeight = (size * (size + 1)) / 2; // Sum of 1+2+3+...+n
        int randomValue = random.nextInt(totalWeight);

        int cumulativeWeight = 0;
        for (int i = 0; i < size; i++) {
            int weight = size - i;
            cumulativeWeight += weight;

            if (randomValue < cumulativeWeight) {
                return abilities.get(i);
            }
        }

        return abilities.get(0); // Fallback
    }

    public void tryUseAbility() {
        if (!usableAbilities.isEmpty()) {
            useAbility(this.level());
        }
    }

    public ArrayList<Ability> getUsableAbilities() {
        return usableAbilities;
    }

    // ========================= Helper Methods =========================
    private Player getController() {
        MarionetteComponent component = this.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (!component.isMarionette()) {
            return null;
        }

        try {
            UUID controllerUUID = UUID.fromString(component.getControllerUUID());
            Player controller = this.level().getPlayerByUUID(controllerUUID);
            return (controller != null && controller.isAlive()) ? controller : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ========================= Loot and Drops =========================
    // commented out for now, to prevent
//    @Override
//    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NonNull DamageSource damageSource, boolean recentlyHit) {
//        if (!BeyonderData.beyonderMap.check(pathway, sequence)) {
//            super.dropCustomDeathLoot(level, damageSource, recentlyHit);
//            return;
//        }
//
//
//        Random random = new Random();
//
//        // Drop characteristic
//        BeyonderCharacteristicItem characteristicItem =
//                BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(pathway, sequence);
//        if (characteristicItem != null) {
//            this.spawnAtLocation(characteristicItem);
//        }
//
//        // Drop recipe with chance
//        if (random.nextInt(RECIPE_DROP_CHANCE) == 0) {
//            PotionRecipeItem recipeItem =
//                    PotionRecipeItemHandler.selectRecipeOfPathwayAndSequence(pathway, sequence);
//            if (recipeItem != null) {
//                this.spawnAtLocation(recipeItem);
//            }
//        }
//
//        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
//    }

    @Override
    protected void dropFromLootTable(DamageSource damageSource, boolean attackedRecently) {
        if (!BeyonderData.beyonderMap.check(pathway, sequence)) {
            return;
        }
        super.dropFromLootTable(damageSource, attackedRecently);
    }

    // ========================= Attributes =========================
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ARMOR, 0.0D);
    }

    // ========================= Getters and Setters =========================
    public ResourceLocation getSkinTexture() {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID,
                "textures/entity/npc/" + DEFAULT_SKIN + ".png");
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

    public void setOriginalOwner(UUID ownerUUID) {
        this.entityData.set(ORIGINAL, Optional.ofNullable(ownerUUID));
    }

    public UUID getOriginalOwner() {
        return this.entityData.get(ORIGINAL).orElse(null);
    }

    // ========================= Combat Information =========================
    public LivingEntity getCurrentTarget() {
        return this.getTarget();
    }

    public boolean isInCombat() {
        return this.getTarget() != null;
    }

    public AttackReason getAttackReason() {
        if (!isInCombat()) {
            return AttackReason.NOT_ATTACKING;
        }
        return AttackReason.RETALIATION;
    }

    public enum AttackReason {
        NOT_ATTACKING,      // Entity has no target
        HOSTILE_BEHAVIOR,   // Entity is hostile and actively seeking targets
        RETALIATION         // Entity is neutral but fighting back after being attacked
    }
}