package de.jakob.lotm.entity.custom;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.custom.goals.AbilityUseGoal;
import de.jakob.lotm.entity.custom.goals.RangedCombatGoal;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.potions.PotionRecipeItem;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
import de.jakob.lotm.quest.QuestManager;
import de.jakob.lotm.quest.QuestRegistry;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import de.jakob.lotm.util.helper.marionettes.MarionetteUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BeyonderNPCEntity extends PathfinderMob {
    // ========================= Constants =========================
    private static final String[] SKINS = {
            "amon", "steampunk_1", "steampunk_2", "mage", "sorcerer",
            "medieval_guy", "gentleman", "victorian_1", "victorian_2",
            "victorian_3", "victorian_4", "victorian_5", "victorian_6",
            "victorian_7", "victorian_8", "victorian_9", "victorian_10",
            "victorian_11", "victorian_12", "victorian_13", "victorian_14",
            "victorian_15", "victorian_16", "gehrman_sparrow"
    };

    private static final int MIN_SEQUENCE = 3;
    private static final int MAX_SEQUENCE = 9;
    private static final double SEQUENCE_WEIGHT_EXPONENT = 0.35;
    private static final float QUEST_SPAWN_CHANCE = 0.55f;
    private static final int RECIPE_DROP_CHANCE = 4;
    private static final int DEFAULT_PUPPET_LIFETIME = 20 * 60 * 4; // 4 minutes

    // ========================= Entity Data Accessors =========================
    private static final EntityDataAccessor<Boolean> IS_HOSTILE =
            SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> SKIN_NAME =
            SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> PATHWAY =
            SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> SEQUENCE =
            SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_PUPPET_WARRIOR =
            SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MAX_LIFETIME_IF_IS_PUPPET =
            SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> QUEST_ID =
            SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.STRING);

    // ========================= Instance Fields =========================
    private String pathway = "none";
    private int sequence = -1;
    private boolean defaultHostile;
    private ArrayList<Ability> usableAbilities = new ArrayList<>();
    private long tickCounter = 0;

    // ========================= Constructors =========================
    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        this(entityType, level, false);
    }

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile) {
        this(entityType, level, hostile, getRandomSkin());
    }

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile, String skinName) {
        this(entityType, level, hostile, skinName, getRandomPathway(), getWeightedHighSequence());
    }

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile, String pathway, int sequence) {
        this(entityType, level, hostile, getRandomSkin(), pathway, sequence);
    }

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile,
                             String skinName, String pathway, int sequence) {
        super(entityType, level);
        this.defaultHostile = hostile;
        this.setHostile(hostile);
        this.setSkinName(skinName);

        // Validate and adjust sequence if needed
        if (sequence < BeyonderData.getHighestImplementedSequence(pathway)) {
            Random random = new Random();
            sequence = random.nextInt(BeyonderData.getHighestImplementedSequence(pathway), 10);
        }

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

    // ========================= Helper Methods for Construction =========================
    private static String getRandomSkin() {
        return SKINS[new Random().nextInt(SKINS.length)];
    }

    private static String getRandomPathway() {
        List<String> pathways = BeyonderData.implementedPathways;
        return pathways.get(new Random().nextInt(pathways.size()));
    }

    /**
     * Generates a weighted random sequence number favoring higher sequences (3-9).
     * Uses exponential distribution to make higher sequences more likely.
     */
    private static int getWeightedHighSequence() {
        Random random = new Random();
        double normalizedValue = random.nextDouble();
        double weighted = Math.pow(normalizedValue, SEQUENCE_WEIGHT_EXPONENT);
        return MIN_SEQUENCE + (int) (weighted * (MAX_SEQUENCE - MIN_SEQUENCE + 1));
    }

    // ========================= Entity Data Initialization =========================
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_HOSTILE, false);
        builder.define(SKIN_NAME, "amon");
        builder.define(PATHWAY, "none");
        builder.define(SEQUENCE, -1);
        builder.define(QUEST_ID, "");
        builder.define(IS_PUPPET_WARRIOR, false);
        builder.define(MAX_LIFETIME_IF_IS_PUPPET, DEFAULT_PUPPET_LIFETIME);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        if (!this.level().isClientSide) {
            // Initialize quest data on first spawn
            if (!this.getPersistentData().getBoolean("Initialized")) {
                this.getPersistentData().putBoolean("Initialized", true);

                if (random.nextFloat() < QUEST_SPAWN_CHANCE) {
                    String randomQuestId = QuestRegistry.getRandomMatchingQuest(this);
                    if (randomQuestId != null) {
                        setQuestId(randomQuestId);
                    }
                }
            }

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
        }
    }

    // ========================= NBT Data Persistence =========================
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsHostile", isHostile());
        compound.putBoolean("DefaultHostile", defaultHostile);
        compound.putString("SkinName", getSkinName());
        compound.putString("Pathway", getPathway());
        compound.putInt("Sequence", getSequence());
        compound.putString("QuestId", getQuestId());
        compound.putBoolean("IsPuppetWarrior", isPuppetWarrior());
        compound.putInt("MaxLifetimeIfPuppet", getMaxLifetimeIfPuppet());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        this.defaultHostile = compound.getBoolean("DefaultHostile");
        setPuppetWarrior(compound.getBoolean("IsPuppetWarrior"));
        setMaxLifetimeIfPuppet(compound.getInt("MaxLifetimeIfPuppet"));

        if (compound.contains("QuestId")) {
            setQuestId(compound.getString("QuestId"));
        }

        if (compound.contains("IsHostile")) {
            setHostile(compound.getBoolean("IsHostile"));
        }

        if (compound.contains("SkinName")) {
            setSkinName(compound.getString("SkinName"));
        }

        if (compound.contains("Pathway") && compound.contains("Sequence")) {
            this.pathway = compound.getString("Pathway");
            this.sequence = compound.getInt("Sequence");

            if (!this.level().isClientSide) {
                BeyonderData.setBeyonder(this, this.pathway, this.sequence);
                this.entityData.set(PATHWAY, this.pathway);
                this.entityData.set(SEQUENCE, this.sequence);
            }
        }

        if (!getPathway().isEmpty()) {
            initializeAbilities(getPathway(), getSequence());
        }
    }

    // ========================= AI Goals =========================
    @Override
    protected void registerGoals() {
        // Basic goals for all NPCs
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new AbilityUseGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        updateGoalsBasedOnHostility();
    }

    private void updateGoalsBasedOnHostility() {
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
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));

        // Add combat goals based on abilities
        if (hasRangedOption()) {
            this.goalSelector.addGoal(3, new RangedCombatGoal(this, 1.0D, 8.0F, 16.0F));
        } else {
            this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        }

        // Add movement goal
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));

        // Add targeting behavior based on hostility
        if (isHostile()) {
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        } else {
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        }
    }

    // ========================= Tick Logic =========================
    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        // Handle puppet lifetime
        if (isPuppetWarrior() && tickCounter >= getMaxLifetimeIfPuppet()) {
            this.discard();
            return;
        }

        // Validate current target
        if (getTarget() != null && !AbilityUtil.mayTarget(this, getTarget())) {
            this.setTarget(null);
        }

        // Clear quests for controlled entities
        if (shouldClearQuest()) {
            setQuestId("");
        }

        // Apply initial regeneration
        if (tickCounter == 1) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, 255, false, true, true));
        }

        tickCounter++;
    }

    private boolean shouldClearQuest() {
        return MarionetteUtils.isMarionette(this) ||
                this.hasEffect(ModEffects.CONQUERED) ||
                this.hasEffect(ModEffects.PETRIFICATION) ||
                this.hasEffect(ModEffects.MUTATED) ||
                isPuppetWarrior() ||
                isHostile() ||
                getCurrentTarget() != null;
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

        // Get abilities that can be used right now
        List<Ability> availableAbilities = usableAbilities.stream()
                .filter(a -> a.canUse(this))
                .toList();

        if (availableAbilities.isEmpty()) {
            return;
        }

        // Separate abilities by combat suitability
        List<Ability> combatAbilities = availableAbilities.stream()
                .filter(a -> a.shouldUseAbility(this))
                .sorted(Comparator.comparing(Ability::lowestSequenceUsable))
                .toList();

        // In combat - prefer combat abilities
        List<Ability> toSelect;
        if (isInCombat() && !combatAbilities.isEmpty()) {
            toSelect = combatAbilities;
        } else if (!isInCombat()) {
            // Out of combat - only use non-combat abilities
            List<Ability> nonCombatAbilities = availableAbilities.stream()
                    .filter(a -> !a.shouldUseAbility(this))
                    .toList();

            if (nonCombatAbilities.isEmpty()) {
                return; // Don't use combat abilities outside of combat
            }
            toSelect = nonCombatAbilities;
        } else {
            // Fallback to all available
            toSelect = availableAbilities;
        }

        // Select and use ability
        Ability selectedAbility = selectWeightedAbility(toSelect, new Random());
        if(selectedAbility == null) return;

        selectedAbility.useAbility((ServerLevel) level, this);
    }

    /**
     * Improved weighted selection with distance consideration
     */
    private Ability selectWeightedAbility(List<Ability> abilities, Random random) {
        if (abilities.isEmpty()) {
            return null;
        }

        // If in combat and has target, consider distance
        if (isInCombat() && getTarget() != null) {
            double distance = this.distanceTo(getTarget());

            // Filter by optimal distance if abilities have distance preferences
            List<Ability> distanceAppropriate = abilities.stream()
                    .filter(a -> !a.hasOptimalDistance ||
                            Math.abs(distance - a.optimalDistance) <= 5.0)
                    .toList();

            if (!distanceAppropriate.isEmpty()) {
                abilities = distanceAppropriate;
            }
        }

        // Weighted selection favoring lower sequence (more powerful) abilities
        int size = abilities.size();
        int totalWeight = (size * (size + 1)) / 2;
        int randomValue = random.nextInt(totalWeight);

        int cumulativeWeight = 0;
        for (int i = 0; i < size; i++) {
            int weight = size - i;
            cumulativeWeight += weight;

            if (randomValue < cumulativeWeight) {
                return abilities.get(i);
            }
        }

        return abilities.get(0);
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
    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NonNull DamageSource damageSource, boolean recentlyHit) {
        if (!BeyonderData.beyonderMap.check(pathway, sequence)) {
            super.dropCustomDeathLoot(level, damageSource, recentlyHit);
            return;
        }

        Random random = new Random();

        // Drop characteristic
        BeyonderCharacteristicItem characteristicItem =
                BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(pathway, sequence);
        if (characteristicItem != null) {
            this.spawnAtLocation(characteristicItem);
        }

        // Drop recipe with chance
        if (random.nextInt(RECIPE_DROP_CHANCE) == 0) {
            PotionRecipeItem recipeItem =
                    PotionRecipeItemHandler.selectRecipeOfPathwayAndSequence(pathway, sequence);
            if (recipeItem != null) {
                this.spawnAtLocation(recipeItem);
            }
        }

        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    @Override
    protected void dropFromLootTable(DamageSource damageSource, boolean attackedRecently) {
        if (!BeyonderData.beyonderMap.check(pathway, sequence)) {
            return;
        }
        super.dropFromLootTable(damageSource, attackedRecently);
    }

    // ========================= Player Interaction =========================
    @Override
    protected @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (getQuestId().isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        // Open the quest dialog instead of immediately accepting
        QuestManager.openQuestDialog(serverPlayer, getQuestId(), getId());
        return InteractionResult.SUCCESS;
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
    public boolean isHostile() {
        return this.entityData.get(IS_HOSTILE);
    }

    public void setHostile(boolean hostile) {
        this.entityData.set(IS_HOSTILE, hostile);
        if (!this.level().isClientSide) {
            updateGoalsBasedOnHostility();
        }
    }

    public String getSkinName() {
        return this.entityData.get(SKIN_NAME);
    }

    public void setSkinName(String skinName) {
        this.entityData.set(SKIN_NAME, skinName);
    }

    public ResourceLocation getSkinTexture() {
        String skinName = getSkinName();
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID,
                "textures/entity/npc/" + skinName + ".png");
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

    public String getQuestId() {
        return this.entityData.get(QUEST_ID);
    }

    public void setQuestId(String questId) {
        this.entityData.set(QUEST_ID, questId);
    }

    public int getMaxLifetimeIfPuppet() {
        return this.entityData.get(MAX_LIFETIME_IF_IS_PUPPET);
    }

    public void setMaxLifetimeIfPuppet(int ticks) {
        this.entityData.set(MAX_LIFETIME_IF_IS_PUPPET, ticks);
    }

    public boolean isPuppetWarrior() {
        return this.entityData.get(IS_PUPPET_WARRIOR);
    }

    public void setPuppetWarrior(boolean isPuppet) {
        this.entityData.set(IS_PUPPET_WARRIOR, isPuppet);
    }

    // ========================= Combat Information =========================
    @Override
    public boolean isAggressive() {
        return isHostile() || super.isAggressive();
    }

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
        return isHostile() ? AttackReason.HOSTILE_BEHAVIOR : AttackReason.RETALIATION;
    }

    public enum AttackReason {
        NOT_ATTACKING,      // Entity has no target
        HOSTILE_BEHAVIOR,   // Entity is hostile and actively seeking targets
        RETALIATION         // Entity is neutral but fighting back after being attacked
    }
}