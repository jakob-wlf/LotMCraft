package de.jakob.lotm.entity.custom;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.AbilityItemHandler;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.custom.goals.AbilityUseGoal;
import de.jakob.lotm.entity.custom.goals.RangedCombatGoal;
import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.potions.PotionItemHandler;
import de.jakob.lotm.potions.PotionRecipeItem;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import de.jakob.lotm.util.helper.marionettes.MarionetteUtils;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BeyonderNPCEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> IS_HOSTILE = SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> SKIN_NAME = SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> PATHWAY = SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> SEQUENCE = SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.INT);

    private String pathway = "none";
    private int sequence = -1;

    private static final String[] SKINS = {
            "amon",
            "steampunk_1",
            "steampunk_2",
            "mage",
            "sorcerer",
            "medieval_guy",
            "gentleman"
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
        this(entityType, level, hostile, skinName, BeyonderData.implementedPathways.get((new Random()).nextInt(BeyonderData.implementedPathways.size())), (new Random()).nextInt(2, 10));
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
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if(!this.level().isClientSide && this.sequence != -1 && !this.pathway.equals("none")) {
            BeyonderData.setBeyonder(this, this.pathway, sequence);
            syncEntityDataWithBeyonderData();
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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.defaultHostile = compound.getBoolean("DefaultHostile");

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
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        String pathway = getPathway();
        int sequence = getSequence();

        Random random = new Random();

        for(int i = 0; i < random.nextInt(1, 3); i++) {
            Item drop = switch(random.nextInt(0, 12)) {
                case 0, 1, 2, 3, 4, 9 -> ModIngredients.selectRandomIngredientOfPathwayAndSequence(random, pathway, sequence);
                case 5 -> PotionItemHandler.selectPotionOfPathwayAndSequence(random, pathway, sequence);
                case 6, 7, 8, 10, 11 -> PotionRecipeItemHandler.selectRecipeOfPathwayAndSequence(random, pathway, sequence);
                default -> null;
            };

            if (drop != null) {
                this.spawnAtLocation(drop);
            }
        }
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

        if(this.level().isClientSide)
            return;

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

        List<AbilityItem> usableAbilities = this.getUsableAbilities().stream().filter(a -> a.shouldUseAbility(this)).toList();

        if (usableAbilities.isEmpty()) {
            return;
        }

        AbilityItem randomAbility = usableAbilities.get(level.random.nextInt(usableAbilities.size()));

        randomAbility.useAsNpcAbility(level, this);
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

