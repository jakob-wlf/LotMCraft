package de.jakob.lotm.entity.custom;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.AbilityItemHandler;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.custom.goals.AbilityUseGoal;
import de.jakob.lotm.entity.custom.goals.RangedCombatGoal;
import de.jakob.lotm.entity.quests.PlayerQuestData;
import de.jakob.lotm.entity.quests.Quest;
import de.jakob.lotm.entity.quests.QuestRegistry;
import de.jakob.lotm.entity.quests.impl.CompoundQuest;
import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.potions.*;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import de.jakob.lotm.util.helper.marionettes.MarionetteUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BeyonderNPCEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> IS_HOSTILE = SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> SKIN_NAME = SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> PATHWAY = SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> SEQUENCE = SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> IS_PUPPET_WARRIOR = SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MAX_LIFETIME_IF_IS_PUPPET = SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> HAS_QUEST = SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> QUEST_ACCEPTED = SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> QUEST_INDEX = SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> QUEST_ID = SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.STRING);

    private String pathway = "none";
    private int sequence = -1;

    private static final String[] SKINS = {
            "amon",
            "steampunk_1",
            "steampunk_2",
            "mage",
            "sorcerer",
            "medieval_guy",
            "gentleman",
            "victorian_1",
            "victorian_2",
            "victorian_3",
            "victorian_4",
            "victorian_5",
            "victorian_6",
            "victorian_7",
            "victorian_8",
            "victorian_9",
            "victorian_10",
    };

    private boolean defaultHostile;
    private ArrayList<AbilityItem> usableAbilities = new ArrayList<>();

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        this(entityType, level, false); // Default to neutral
    }

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile) {
        this(entityType, level, hostile, SKINS[new Random().nextInt(SKINS.length)]);
    }

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile, String skinName) {
        this(entityType, level, hostile, skinName,
                BeyonderData.implementedPathways.get((new Random()).nextInt(BeyonderData.implementedPathways.size())),
                getWeightedHighSequence());
    }

    private static int getWeightedHighSequence() {
        Random random = new Random();
        // Quadratic weighting: square a random value to bias toward higher numbers
        double normalizedValue = random.nextDouble(); // 0.0 to 1.0
        double weighted = Math.pow(normalizedValue, 0.5); // Square root gives strong bias toward higher values

        return (int) Math.ceil(weighted * 9);
    }
    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile, String pathway, int sequence) {
        this(entityType, level, hostile, SKINS[new Random().nextInt(SKINS.length)], pathway, sequence);
    }

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile, String skinName, String pathway, int sequence) {
        super(entityType, level);
        this.defaultHostile = hostile;
        this.setHostile(hostile);
        this.setSkinName(skinName);

        if(sequence < BeyonderData.getHighestImplementedSequence(pathway)) {
            sequence = (new Random()).nextInt(BeyonderData.getHighestImplementedSequence(pathway), 10);
        }

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

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_HOSTILE, false);
        builder.define(SKIN_NAME, "amon");
        builder.define(PATHWAY, "none");
        builder.define(SEQUENCE, -1);
        builder.define(HAS_QUEST, false);
        builder.define(QUEST_INDEX, 0);
        builder.define(QUEST_ACCEPTED, false);
        builder.define(QUEST_ID, "");
        builder.define(IS_PUPPET_WARRIOR, false);
        builder.define(MAX_LIFETIME_IF_IS_PUPPET, 20 * 60 * 4);
    }

    private void syncEntityDataWithBeyonderData() {
        if (!this.level().isClientSide) {
            String currentPathway = BeyonderData.getPathway(this);
            int currentSequence = BeyonderData.getSequence(this);

            this.entityData.set(PATHWAY, currentPathway);
            this.entityData.set(SEQUENCE, currentSequence);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.CONSUME;
        }

        PlayerQuestData questData = player.getData(ModAttachments.PLAYER_QUEST_DATA.get());

        // Check if player already has a quest from this NPC
        Quest existingQuest = questData.getQuestByNPC(this.getUUID());

        if (existingQuest != null) {
            // Player has an active quest from this NPC
            if (existingQuest.checkCompletion(player, this.level())) {
                // Quest is complete, turn it in
                existingQuest.onComplete(player);
                questData.removeQuest(existingQuest);

                // Mark this NPC as having no quest and not having one accepted
                setHasQuest(false);
                setQuestAccepted(false);

                player.sendSystemMessage(Component.literal("§a§l[Quest Complete] §r§a" + existingQuest.getTitle()));
                player.sendSystemMessage(Component.literal("§eRewards received!"));

                return InteractionResult.SUCCESS;
            } else {
                // Quest in progress, show status
                player.sendSystemMessage(Component.literal("§e[Quest In Progress] §r" + existingQuest.getTitle()));
                player.sendSystemMessage(existingQuest.getProgressText());

                // Show detailed progress for compound quests
                if (existingQuest instanceof CompoundQuest compoundQuest) {
                    for (Component progress : compoundQuest.getDetailedProgress()) {
                        player.sendSystemMessage(progress);
                    }
                }

                return InteractionResult.SUCCESS;
            }
        } else if (hasQuest() && !wasQuestAccepted()) {
            // NPC has a quest to offer and it hasn't been accepted yet
            Quest newQuest = QuestRegistry.getQuestById(getQuestId());

            if (newQuest == null) {
                // Generate a new quest if none exists
                newQuest = QuestRegistry.getRandomQuest(new Random(), this.blockPosition());
                if (newQuest != null) {
                    setQuestId(newQuest.getQuestId());
                }
            } else {
                // Create a new instance from the stored quest ID
                newQuest = QuestRegistry.createQuestInstance(
                        QuestRegistry.getQuestById(getQuestId()),
                        new Random(),
                        this.blockPosition()
                );
            }

            if (newQuest != null) {
                // Assign quest to player
                newQuest.onAccept(player);
                questData.addQuest(newQuest, this.getUUID());
                setQuestAccepted(true);

                player.sendSystemMessage(Component.literal("§6§l[New Quest] §r§6" + newQuest.getTitle()));
                player.sendSystemMessage(Component.literal("§7" + newQuest.getDescription()));
                player.sendSystemMessage(newQuest.getProgressText());

                // Show detailed objectives for compound quests
                if (newQuest instanceof CompoundQuest compoundQuest) {
                    player.sendSystemMessage(Component.literal("§7Objectives:"));
                    for (Component progress : compoundQuest.getDetailedProgress()) {
                        player.sendSystemMessage(progress);
                    }
                }

                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!this.level().isClientSide) {
            // Only generate a quest if this NPC doesn't already have one
            if (this.random.nextFloat() <= .2f && !hasQuest() && !isPuppetWarrior() && !MarionetteUtils.isMarionette(this)) {
                setHasQuest(true);

                // Generate and store the quest ID
                Quest quest = QuestRegistry.getRandomQuest(new Random(), this.blockPosition());
                if (quest != null) {
                    setQuestId(quest.getQuestId());
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
    protected void registerGoals() {
        // Basic goals that both hostile and neutral mobs need
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new AbilityUseGoal(this)); // Custom ability usage goal
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // Conditional goals based on hostility
        updateGoalsBasedOnHostility();
    }

    private void updateGoalsBasedOnHostility() {
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

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));

        if (hasRangedOption()) {
            this.goalSelector.addGoal(3, new RangedCombatGoal(this, 1.0D, 8.0F, 16.0F));
            this.goalSelector.addGoal(6, new MeleeAttackGoal(this, 1.2D, false));
        } else {
            this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        }

        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));

        if (isHostile()) {
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }
        else {
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        }
    }

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
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/npc/" + skinName + ".png");
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

    public boolean hasQuest() {
        return this.entityData.get(HAS_QUEST);
    }

    public void setHasQuest(boolean hasQuest) {
        this.entityData.set(HAS_QUEST, hasQuest);
    }

    public boolean wasQuestAccepted() {
        return this.entityData.get(QUEST_ACCEPTED);
    }

    public void setQuestAccepted(boolean accepted) {
        this.entityData.set(QUEST_ACCEPTED, accepted);
    }

    public int getQuestIndex() {
        return this.entityData.get(QUEST_INDEX);
    }

    public void setQuestIndex(int index) {
        this.entityData.set(QUEST_INDEX, index);
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
                        false
                );
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsHostile", isHostile());
        compound.putBoolean("DefaultHostile", defaultHostile);
        compound.putString("SkinName", getSkinName());
        compound.putString("Pathway", getPathway());
        compound.putInt("Sequence", getSequence());
        compound.putInt("QuestIndex", getQuestIndex());
        compound.putBoolean("HasQuest", hasQuest());
        compound.putBoolean("QuestAccepted", wasQuestAccepted());
        compound.putString("QuestId", getQuestId());
        compound.putBoolean("IsPuppetWarrior", isPuppetWarrior());
        compound.putInt("MaxLifetimeIfPuppet", getMaxLifetimeIfPuppet());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.defaultHostile = compound.getBoolean("DefaultHostile");
        setHasQuest(compound.getBoolean("HasQuest"));
        setQuestIndex(compound.getInt("QuestIndex"));
        setQuestAccepted(compound.getBoolean("QuestAccepted"));
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

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NonNull DamageSource damageSource, boolean recentlyHit) {
        String pathway = getPathway();
        int sequence = getSequence();

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

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ARMOR, 0.0D);
    }


    long tickCounter = 0;

    @Override
    public void tick() {
        super.tick();

        if(this.level().isClientSide) {
            return;
        }

        if(isPuppetWarrior() && tickCounter >= getMaxLifetimeIfPuppet()) {
            this.discard();
            return;
        }

        if(this.hasEffect(ModEffects.CONQUERED) || this.hasEffect(ModEffects.PETRIFICATION)){
            setHasQuest(false);
        }

        if(getTarget() != null) {
            if(!AbilityUtil.mayTarget(this, getTarget())) {
                this.setTarget(null);
            }
        }

        if(tickCounter % 5 == 0) {
            PassiveAbilityHandler.ITEMS.getEntries().forEach(itemHolder -> {
                if (itemHolder.get() instanceof PassiveAbilityItem abilityItem) {
                    if (abilityItem.shouldApplyTo(this)) {
                        abilityItem.tick(this.level(), this);
                    }
                }
            });
        }
        tickCounter++;
        if(tickCounter == 1) {
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

    // Make the mob neutral by default (only attacks when provoked if not hostile)
    @Override
    public boolean isAggressive() {
        return isHostile() || super.isAggressive();
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

        if (isHostile()) {
            return AttackReason.HOSTILE_BEHAVIOR;
        } else {
            return AttackReason.RETALIATION;
        }
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

