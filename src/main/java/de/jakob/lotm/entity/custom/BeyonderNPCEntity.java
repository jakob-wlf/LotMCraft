package de.jakob.lotm.entity.custom;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.AbilityItemHandler;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.entity.custom.goals.AbilityUseGoal;
import de.jakob.lotm.entity.custom.goals.RangedCombatGoal;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.*;

public class BeyonderNPCEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> IS_HOSTILE = SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> SKIN_NAME = SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.STRING);

    private boolean defaultHostile;
    private ArrayList<AbilityItem> usableAbilities = new ArrayList<>();

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        this(entityType, level, false); // Default to neutral
    }

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile) {
        this(entityType, level, hostile, "default");
    }

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile, String skinName) {
        this(entityType, level, hostile, skinName, BeyonderData.implementedPathways.get((new Random()).nextInt(BeyonderData.implementedPathways.size())), (new Random()).nextInt(5, 10));
    }

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile, String pathway, int sequence) {
        this(entityType, level, hostile, "default", pathway, sequence);
    }

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile, String skinName, String pathway, int sequence) {
        super(entityType, level);
        this.defaultHostile = hostile;
        this.setHostile(hostile);
        this.setSkinName(skinName);

        if(usableAbilities == null)
            usableAbilities = new ArrayList<>();

        if(!level.isClientSide) {
            BeyonderData.setBeyonder(this, pathway, sequence);
        }

        if (!pathway.isEmpty()) {
            initializeAbilities(pathway, sequence);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_HOSTILE, false);
        builder.define(SKIN_NAME, "default");
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

        // Clear existing combat goals
        this.goalSelector.removeAllGoals(goal -> goal instanceof MeleeAttackGoal ||
                goal instanceof WaterAvoidingRandomStrollGoal ||
                goal instanceof MoveThroughVillageGoal ||
                goal instanceof RangedCombatGoal);
        this.targetSelector.removeAllGoals(goal -> goal instanceof NearestAttackableTargetGoal ||
                goal instanceof HurtByTargetGoal);

        // Always retaliate when hurt (both hostile and neutral)
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));

        // Choose combat strategy based on abilities
        if (hasRangedOption()) {
            // Has ranged abilities - prefer ranged combat with occasional melee
            this.goalSelector.addGoal(3, new RangedCombatGoal(this, 1.0D, 8.0F, 16.0F)); // Stay 8-16 blocks away
            this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.2D, false)); // Occasional melee (lower priority)
        } else {
            // No ranged abilities - focus on melee combat
            this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        }

        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));

        if (isHostile()) {
            // Hostile behavior - actively seek players
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }
        // Neutral entities only fight back when attacked (handled by HurtByTargetGoal)
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
        return ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");
    }

    public String getPathway() {
        return BeyonderData.getPathway(this);
    }

    public int getSequence() {
        return BeyonderData.getSequence(this);
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
        setHostile(compound.getBoolean("IsHostile"));
        setSkinName(compound.getString("SkinName"));

        // Reinitialize abilities after loading
        if (!getPathway().isEmpty()) {
            initializeAbilities(getPathway(), getSequence());
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


    int tickCounter = 0;

    @Override
    public void tick() {
        super.tick();

        //TODO: Change to goal based tick

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
        for (AbilityItem ability : usableAbilities) {
            if (ability.hasOptimalDistance) {
                return false;
            }
        }
        return true;
    }

    public void useAbility(Level level, BeyonderNPCEntity npcEntity) {

        System.out.println("Using ability from pool of " + usableAbilities.size() + " abilities.");

        // Get a random ability from the pool
        AbilityItem randomAbility = usableAbilities.get(level.random.nextInt(usableAbilities.size()));

        System.out.println("Selected ability: " + randomAbility);

        randomAbility.useAsNpcAbility(level, npcEntity);
    }

    public void tryUseAbility() {
        System.out.println("Trying to use ability...");
        if (usableAbilities.isEmpty() || !isInCombat()) {
            return;
        }

        useAbility(this.level(), this);
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
                .forEach(usableAbilities::add);
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

