package de.jakob.lotm.entity.custom;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.potions.*;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.custom.goals.AbilityUseGoal;
import de.jakob.lotm.entity.custom.goals.RangedCombatGoal;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.gui.custom.Trades.BeyonderTradeMenu;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.quest.QuestManager;
import de.jakob.lotm.quest.QuestRegistry;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import de.jakob.lotm.util.helper.marionettes.MarionetteUtils;
import de.jakob.lotm.util.shapeShifting.PlayerSkinData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
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
    private static final float TRADE_SPAWN_CHANCE = 0.075f;
    private static final int RECIPE_DROP_CHANCE = 4;
    private static final int DEFAULT_PUPPET_LIFETIME = 20 * 60 * 4; // 4 minutes

    // ========================= Entity Data Accessors =========================
    private static final EntityDataAccessor<Boolean> IS_HOSTILE =
            SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> SKIN_NAME =
            SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> IS_PUPPET_WARRIOR =
            SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MAX_LIFETIME_IF_IS_PUPPET =
            SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> QUEST_ID =
            SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<CompoundTag> TRADES =
            SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Optional<UUID>> TARGET_PLAYER_UUID =
            SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> IS_PERSISTENT =
            SynchedEntityData.defineId(BeyonderNPCEntity.class, EntityDataSerializers.BOOLEAN);

    // ========================= Instance Fields =========================
    private String _pathway = "";
    private int _sequence = -1;
    private Boolean _hasQuest = null;
    private Boolean _hasTrade = null;
    private boolean defaultHostile;
    private long tickCounter = 0;

    // ========================= Constructors =========================
    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        this(entityType, level, false);
    }

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile) {
        this(entityType, level, hostile, getRandomBeyonderSkin());
    }

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile, String skinName) {
        this(entityType, level, hostile, skinName, "", -1, null, false, false);
    }

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile, String pathway, int sequence) {
        this(entityType, level, hostile, getRandomBeyonderSkin(), pathway, sequence, null, false, false);
    }

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile, String pathway, int sequence, boolean persistent) {
        this(entityType, level, hostile, getRandomBeyonderSkin(), pathway, sequence, null, false, persistent);
    }

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile,
                             String skinName, String pathway, int sequence, Boolean _hasQuest, Boolean _hasTrades) {
        this(entityType, level, hostile, skinName, pathway, sequence, _hasQuest, _hasTrades, false);
    }

    public BeyonderNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level, boolean hostile,
                             String skinName, String pathway, int sequence, Boolean _hasQuest, Boolean _hasTrades, boolean persistent) {
        super(entityType, level);
        this.defaultHostile = hostile;

        this.setHostile(hostile);
        this.setSkinName(skinName);
        this.setPersistentNPC(persistent);

        if (pathway != null && !pathway.isEmpty() && !pathway.equalsIgnoreCase("none")) {
            this._pathway = pathway;
            this._sequence = sequence;
        } else {
            this._pathway = getRandomPathway();
            this._sequence = getWeightedHighSequence();
        }
        this._hasQuest = _hasQuest;
        this._hasTrade = _hasTrades;
    }

    public static String getRandomBeyonderSkin() {
        return SKINS[new Random().nextInt(SKINS.length)];
    }

    private static String getRandomPathway() {
        List<String> pathways = BeyonderData.implementedPathways;
        return pathways.get(new Random().nextInt(pathways.size()));
    }

    private static int getWeightedHighSequence() {
        Random random = new Random();
        double normalizedValue = random.nextDouble();
        double weighted = Math.pow(normalizedValue, SEQUENCE_WEIGHT_EXPONENT);
        return MIN_SEQUENCE + (int) (weighted * (MAX_SEQUENCE - MIN_SEQUENCE + 1));
    }

    // ========================= Entity Data Initialization =========================
    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType type) {
        return super.checkSpawnRules(level, type);
    }

    public static boolean canSpawn(EntityType<BeyonderNPCEntity> type,
                                   LevelAccessor level,
                                   MobSpawnType reason,
                                   BlockPos pos,
                                   RandomSource random) {

        ServerLevel serverLevel = level.getServer().overworld();
        if (!serverLevel.getGameRules().getBoolean(ModGameRules.ALLOW_BEYONDER_SPAWNING)) {
            return false;
        }

        if(pos.getY() >= 100)
            return false;

        int nearby = level.getEntitiesOfClass(
                BeyonderNPCEntity.class,
                new AABB(pos).inflate(60)
        ).size();

        return nearby < 2;
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (this.level().isClientSide) return;

        boolean freshSpawn = !this.getPersistentData().getBoolean("Initialized");
        if (freshSpawn) {
            this.getPersistentData().putBoolean("Initialized", true);

            // Set Beyonder Data
            if (this._sequence != -1 && !this._pathway.isEmpty() && !this._pathway.equals("none")) {
                BeyonderData.setBeyonder(this, this._pathway, this._sequence, true, true, true, true, true, false, true);
            } else {
                if (!BeyonderData.isBeyonder(this)) {
                    BeyonderData.setBeyonder(this, getRandomPathway(), getWeightedHighSequence(), true, true, true, true, true, false, true);
                }
            }

            // Quests and Trades
            if ((random.nextFloat() < QUEST_SPAWN_CHANCE || Boolean.TRUE.equals(this._hasQuest)) && !Boolean.FALSE.equals(this._hasQuest)) {
                String randomQuestId = QuestRegistry.getRandomMatchingQuest(this);
                if (randomQuestId != null) {
                    setQuestId(randomQuestId);
                }
            } else if ((random.nextFloat() < TRADE_SPAWN_CHANCE || Boolean.TRUE.equals(this._hasTrade)) && !Boolean.FALSE.equals(this._hasTrade)) {
                ListTag trades = new ListTag();
                for (int i = 0; i < random.nextInt(2, 5); i++) {
                    TradeEntry entry = generateRandomTrade(random);
                    if (entry == null) continue;
                    CompoundTag tradeTag = writeTrade(entry.costA, entry.costB, entry.result, this.registryAccess());
                    trades.add(tradeTag);
                }
                CompoundTag tradesCompound = new CompoundTag();
                tradesCompound.put("trades", trades);
                setTrades(tradesCompound);
            }

            this.setHealth(this.getMaxHealth());
        } else {
            // If already initialized but re-added to level (e.g. reload), ensure characteristics are set
            // because they are not stored in the entity's regular data but in the BeyonderComponent attachment
            // which should be serialized, but let's be safe.
            String pathway = BeyonderData.getPathway(this);
            int sequence = BeyonderData.getSequence(this);
            if (!pathway.equals("none")) {
                BeyonderData.setBeyonder(this, pathway, sequence, true, false, false, false, false, false, true);
            }
        }

        updateGoalsBasedOnHostilityAndTrades();
    }

    // ========================= AI Goals =========================
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new AbilityUseGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));

        updateGoalsBasedOnHostilityAndTrades();
    }

    private void updateGoalsBasedOnHostilityAndTrades() {
        this.goalSelector.removeAllGoals(goal -> goal instanceof MeleeAttackGoal ||
                goal instanceof WaterAvoidingRandomStrollGoal ||
                goal instanceof MoveThroughVillageGoal ||
                goal instanceof RangedCombatGoal);
        this.targetSelector.removeAllGoals(goal -> goal instanceof NearestAttackableTargetGoal);

        if (AbilityUseGoal.hasRangedOption(this)) {
            this.goalSelector.addGoal(3, new RangedCombatGoal(this, 1.0D, 8.0F, 16.0F));
        } else {
            this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        }

        if (!hasTrades()) {
            this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        }

        if (isHostile()) {
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true, (e) -> e != this && !(e instanceof BeyonderNPCEntity b && b.getPathway().equals(this.getPathway()) && !this.getSkinName().equals("amon"))));
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

        if (isPuppetWarrior() && tickCounter >= getMaxLifetimeIfPuppet()) {
            this.discard();
            return;
        }

        if (getTarget() != null && !AbilityUtil.mayTarget(this, getTarget())) {
            this.setTarget(null);
        }

        if (tickCounter % 20 == 0) {
            if (shouldClearQuest()) {
                setQuestId("");
                setTrades(new CompoundTag());
            }
        }

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

    // ========================= Loot and Drops =========================
    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NonNull DamageSource damageSource, boolean recentlyHit) {
        if (!BeyonderData.playerMap.check(_pathway, _sequence)) {
            super.dropCustomDeathLoot(level, damageSource, recentlyHit);
            return;
        }

        Random random = new Random();

        // don't drop anything if historical summoned
        if (this.getPersistentData().contains("VoidSummoned")) {
            super.dropCustomDeathLoot(level, damageSource, recentlyHit);
            return;
        }

        // Drop characteristic
        BeyonderCharacteristicItem characteristicItem =
                BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(_pathway, _sequence);
        if (characteristicItem != null) {
            this.spawnAtLocation(characteristicItem);
        }

        // Drop recipe with chance
        if (random.nextInt(RECIPE_DROP_CHANCE) == 0) {
            PotionRecipeItem recipeItem =
                    PotionRecipeItemHandler.selectRecipeOfPathwayAndSequence(_pathway, _sequence);
            if (recipeItem != null) {
                this.spawnAtLocation(recipeItem);
            }
        }

        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    @Override
    protected void dropFromLootTable(DamageSource damageSource, boolean attackedRecently) {
        if (!BeyonderData.playerMap.check(_pathway, _sequence)) {
            return;
        }
        super.dropFromLootTable(damageSource, attackedRecently);
    }

    // ========================= Player Interaction =========================
    @Override
    protected @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        if (hasTrades()) {
            serverPlayer.openMenu(new net.minecraft.world.SimpleMenuProvider(
                    (windowId, inv, p) -> new BeyonderTradeMenu(windowId, inv, this.getId()),
                    net.minecraft.network.chat.Component.literal("Trades")
            ), buf -> buf.writeVarInt(this.getId()));
            return InteractionResult.SUCCESS;
        }

        if (getQuestId().isEmpty()) {
            return InteractionResult.PASS;
        }

        QuestManager.openQuestDialog(serverPlayer, getQuestId(), getId());
        return InteractionResult.SUCCESS;
    }

    // ========================= Trade System =========================

    private CompoundTag writeTrade(ItemStack costA, ItemStack costB, ItemStack result, HolderLookup.Provider registries) {
        CompoundTag trade = new CompoundTag();
        trade.put("CostA", costA.save(registries));
        if (!costB.isEmpty()) trade.put("CostB", costB.save(registries));
        trade.put("Result", result.save(registries));
        return trade;
    }

    private TradeEntry readTrade(CompoundTag tag, HolderLookup.Provider registries) {
        ItemStack costA = ItemStack.parse(registries, tag.getCompound("CostA")).orElse(ItemStack.EMPTY);
        ItemStack costB = tag.contains("CostB")
                ? ItemStack.parse(registries, tag.getCompound("CostB")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        ItemStack result = ItemStack.parse(registries, tag.getCompound("Result")).orElse(ItemStack.EMPTY);
        return new TradeEntry(costA, costB, result);
    }

    public List<TradeEntry> getCurrentTrades() {
        HolderLookup.Provider registries = this.registryAccess();
        ListTag list = getTrades().getList("trades", Tag.TAG_COMPOUND);
        List<TradeEntry> result = new ArrayList<>();
        for (Tag t : list) {
            result.add(readTrade((CompoundTag) t, registries));
        }
        return result;
    }



    private TradeEntry generateRandomTrade(RandomSource random) {
        int itemSequence = Math.clamp(BeyonderData.getSequence(this) + (random.nextInt(3) - 1), 1, 9);
        
        float randomFloat = random.nextFloat();
        Item item;
        if (randomFloat < .4f) {
            item = PotionRecipeItemHandler.selectRandomRecipeOfSequence(random, itemSequence);
        } else if (randomFloat < .5f) {
            item = PotionItemHandler.selectRandomPotionOfSequence(random, itemSequence);
        } else if (randomFloat < .94f) {
            item = BeyonderCharacteristicItemHandler.selectRandomCharacteristicOfSequence(random, itemSequence);
        } else {
            item = ModItems.UNIQUENESS_MAP.get();
        }

        if (item == null) {
            return null;
        }

        ItemStack itemStack = new ItemStack(item);

        int[] charPriceInSoli = {0, 2560, 1920, 1600, 1100, 400, 200, 100, 30, 10};

        int baseSoli = charPriceInSoli[itemSequence];

        boolean isRecipe = item instanceof PotionRecipeItem;
        boolean isPotion = item instanceof BeyonderPotion;
        int multiplierPct;
        if (isRecipe) {
            multiplierPct = 33;
        } else if (isPotion) {
            multiplierPct = 170;
        } else {
            multiplierPct = 100;
        }

        long totalSoli = (long) baseSoli * multiplierPct / 100;
        totalSoli = totalSoli * (85 + random.nextInt(31)) / 100;
        totalSoli = Math.max(1, totalSoli);

        final int SOLI_PER_POUND = 20;
        final int MAX_STACK = 64;

        int pounds = (int) Math.min(totalSoli / SOLI_PER_POUND, MAX_STACK);
        long remainingSoli = totalSoli - ((long) pounds * SOLI_PER_POUND);
        int soli = (int) Math.min(remainingSoli, MAX_STACK);

        int extraPounds = (pounds == MAX_STACK && !isRecipe && !isPotion)
                ? (int) Math.min(totalSoli / SOLI_PER_POUND - MAX_STACK, MAX_STACK)
                : 0;

        ItemStack costA;
        ItemStack costB;

        if (pounds > 0) {
            costA = new ItemStack(ModItems.ONE_POUND.get(), pounds);
            if (extraPounds > 0) {
                costB = new ItemStack(ModItems.ONE_POUND.get(), extraPounds);
            } else if (soli > 0) {
                costB = new ItemStack(ModItems.ONE_SOLI.get(), soli);
            } else {
                costB = ItemStack.EMPTY;
            }
        } else {
            costA = new ItemStack(ModItems.ONE_SOLI.get(), soli);
            costB = ItemStack.EMPTY;
        }

        if(randomFloat >= .94) {
            costA = new ItemStack(ModItems.ONE_POUND.get(), 64);
            costB = new ItemStack(ModItems.ONE_POUND.get(), 64);
        }

        return new TradeEntry(costA, costB, itemStack);
    }

    public void removeTrade(int index) {
        CompoundTag root = getTrades().copy();
        ListTag trades = root.getList("trades", Tag.TAG_COMPOUND);
        if (index >= 0 && index < trades.size()) {
            ListTag newTrades = new ListTag();
            for (int i = 0; i < trades.size(); i++) {
                if (i != index) newTrades.add(trades.get(i));
            }
            root.put("trades", newTrades);
            setTrades(root);
        }

        updateGoalsBasedOnHostilityAndTrades();
    }

    public boolean hasTrades() {
        return !getTrades().getList("trades", Tag.TAG_COMPOUND).isEmpty();
    }

    public record TradeEntry(ItemStack costA, ItemStack costB, ItemStack result) {}

    // ========================= Attributes =========================
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ARMOR, 0.0D);
    }

    // ========================= Synced Data and Persistence =========================

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        super.onSyncedDataUpdated(dataAccessor);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_HOSTILE, false);
        builder.define(SKIN_NAME, "amon");
        builder.define(QUEST_ID, "");
        builder.define(TRADES, new CompoundTag());
        builder.define(IS_PUPPET_WARRIOR, false);
        builder.define(MAX_LIFETIME_IF_IS_PUPPET, DEFAULT_PUPPET_LIFETIME);
        builder.define(TARGET_PLAYER_UUID, Optional.empty());
        builder.define(IS_PERSISTENT, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsHostile", isHostile());
        compound.putBoolean("DefaultHostile", defaultHostile);
        compound.putString("SkinName", getSkinName());
        compound.putString("QuestId", getQuestId());
        compound.put("Trades", getTrades());
        compound.putBoolean("IsPuppetWarrior", isPuppetWarrior());
        compound.putInt("MaxLifetimeIfPuppet", getMaxLifetimeIfPuppet());
        compound.putBoolean("IsPersistentNPC", isPersistentNPC());
        if (getTargetPlayerUUID().isPresent()) {
            compound.putUUID("TargetPlayerUUID", getTargetPlayerUUID().get());
        }

        compound.putString("Pathway", _pathway);
        compound.putInt("Sequence", _sequence);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        this.defaultHostile = compound.getBoolean("DefaultHostile");
        setPuppetWarrior(compound.getBoolean("IsPuppetWarrior"));
        setMaxLifetimeIfPuppet(compound.getInt("MaxLifetimeIfPuppet"));
        setTrades(compound.getCompound("Trades"));
        setPersistentNPC(compound.getBoolean("IsPersistentNPC"));

        if (compound.contains("QuestId"))  setQuestId(compound.getString("QuestId"));
        if (compound.contains("IsHostile")) setHostile(compound.getBoolean("IsHostile"));
        if (compound.contains("SkinName"))  setSkinName(compound.getString("SkinName"));
        if (compound.contains("TargetPlayerUUID")) setTargetPlayerUUID(compound.getUUID("TargetPlayerUUID"));

        if (compound.contains("Pathway")) _pathway = compound.getString("Pathway");
        if (compound.contains("Sequence")) _sequence = compound.getInt("Sequence");
    }

    // ========================= Getters and Setters =========================
    public boolean isHostile() {
        return this.entityData.get(IS_HOSTILE);
    }

    public void setHostile(boolean hostile) {
        this.entityData.set(IS_HOSTILE, hostile);
        if (!this.level().isClientSide) {
            updateGoalsBasedOnHostilityAndTrades();
        }
    }

    public String getSkinName() {
        return this.entityData.get(SKIN_NAME);
    }

    public void setSkinName(String skinName) {
        this.entityData.set(SKIN_NAME, skinName);
    }

    public ResourceLocation getSkinTexture() {
        if (getTargetPlayerUUID().isPresent()) {
            ResourceLocation cached = PlayerSkinData.getSkinTexture(getTargetPlayerUUID().get());
            if (cached != null) {
                return cached;
            }
            if (this.level().isClientSide) {
                PlayerSkinData.fetchAndCacheSkin(getTargetPlayerUUID().get());
            }
        }
        String skinName = getSkinName();
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID,
                "textures/entity/npc/" + skinName + ".png");
    }

    public void setTargetPlayerUUID(UUID uuid) {
        this.entityData.set(TARGET_PLAYER_UUID, Optional.ofNullable(uuid));
    }

    public Optional<UUID> getTargetPlayerUUID() {
        return this.entityData.get(TARGET_PLAYER_UUID);
    }

    public String getPathway() {
        return BeyonderData.getPathway(this);
    }

    public int getSequence() {
        return BeyonderData.getSequence(this);
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

    public CompoundTag getTrades() {
        return this.entityData.get(TRADES);
    }

    public void setTrades(CompoundTag trades) {
        this.entityData.set(TRADES, trades);
    }

    public boolean isPersistentNPC() {
        return this.entityData.get(IS_PERSISTENT);
    }

    public void setPersistentNPC(boolean persistent) {
        this.entityData.set(IS_PERSISTENT, persistent);
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
        NOT_ATTACKING,
        HOSTILE_BEHAVIOR,
        RETALIATION
    }

    // ========================= Despawn =========================

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        MarionetteComponent component = this.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (component.isMarionette() || isPersistentNPC()) {
            return false;
        }
        return true;
    }

    @Override
    public void checkDespawn() {
        MarionetteComponent component = this.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (!component.isMarionette() && !isPersistentNPC()) {
            super.checkDespawn();
        }
    }
}