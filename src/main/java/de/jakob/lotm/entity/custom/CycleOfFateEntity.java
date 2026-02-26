package de.jakob.lotm.entity.custom;

import de.jakob.lotm.util.ControllingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cycle of Fate Entity - Records and restores a region of the world
 *
 * Features:
 * - Records all blocks, blockstates, and block entities in an 80x80x80 radius
 * - Records all entities with their complete state (health, effects, inventory, etc.)
 * - Only spawnable via code
 * - Non-persistent (doesn't save)
 * - Disappears when owner is offline, in different dimension, or >100 blocks away
 * - Prevents overlapping instances
 * - Can restore the recorded state via trigger() method
 * - Prevents entities from leaving the recorded area
 */
public class CycleOfFateEntity extends Entity {
    private static final int RECORD_RADIUS = 40; // 80 block diameter = 40 block radius
    private static final int OWNER_MAX_DISTANCE = 100;

    // Track all active Cycle of Fate entities
    private static final Map<Level, Set<CycleOfFateEntity>> ACTIVE_ENTITIES = new WeakHashMap<>();

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(CycleOfFateEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    // Recorded world state
    private final Map<BlockPos, RecordedBlock> recordedBlocks = new HashMap<>();
    private final Map<UUID, RecordedEntity> recordedEntities = new HashMap<>();
    private final Set<UUID> trackedEntityUUIDs = new HashSet<>();
    private BlockPos centerPos;
    private boolean hasRecorded = false;
    private boolean isRestoring = false;

    public CycleOfFateEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_UUID, Optional.empty());
    }

    /**
     * Sets the owner of this Cycle of Fate
     */
    public void setOwner(@Nullable LivingEntity owner) {
        if (owner != null) {
            this.entityData.set(OWNER_UUID, Optional.of(owner.getUUID()));
        } else {
            this.entityData.set(OWNER_UUID, Optional.empty());
        }
    }

    /**
     * Gets the owner entity if present
     */
    @Nullable
    public LivingEntity getOwner() {
        Optional<UUID> ownerUUID = this.entityData.get(OWNER_UUID);
        if (ownerUUID.isPresent() && this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(ownerUUID.get());
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
        }
        return null;
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    /**
     * Check if another Cycle of Fate would overlap with this position
     */
    public static boolean wouldOverlap(Level level, BlockPos pos) {
        Set<CycleOfFateEntity> entities = ACTIVE_ENTITIES.get(level);
        if (entities == null) return false;

        // Clean up dead entities
        entities.removeIf(Entity::isRemoved);

        for (CycleOfFateEntity existing : entities) {
            if (existing.centerPos != null) {
                double distance = Math.sqrt(pos.distSqr(existing.centerPos));
                // Two circles overlap if distance < radius1 + radius2
                if (distance < RECORD_RADIUS * 2) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Records the current state of the area
     */
    public void recordArea() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (hasRecorded) return;

        this.centerPos = this.blockPosition();

        // Record all blocks in radius
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int x = -RECORD_RADIUS; x <= RECORD_RADIUS; x++) {
            for (int y = -RECORD_RADIUS; y <= RECORD_RADIUS; y++) {
                for (int z = -RECORD_RADIUS; z <= RECORD_RADIUS; z++) {
                    // Check if within spherical radius
                    if (x*x + y*y + z*z <= RECORD_RADIUS * RECORD_RADIUS) {
                        mutablePos.set(centerPos.getX() + x, centerPos.getY() + y, centerPos.getZ() + z);
                        BlockPos immutablePos = mutablePos.immutable();

                        BlockState state = serverLevel.getBlockState(immutablePos);
                        BlockEntity blockEntity = serverLevel.getBlockEntity(immutablePos);

                        RecordedBlock recordedBlock = new RecordedBlock(
                                state,
                                blockEntity != null ? blockEntity.saveWithFullMetadata(serverLevel.registryAccess()) : null
                        );
                        recordedBlocks.put(immutablePos, recordedBlock);
                    }
                }
            }
        }

        // Record all entities in radius
        AABB boundingBox = new AABB(centerPos).inflate(RECORD_RADIUS);
        List<Entity> entities = serverLevel.getEntities(this, boundingBox, entity ->
                !(entity instanceof CycleOfFateEntity));

        for (Entity entity : entities) {
            if (entity.isAlive()) {
                RecordedEntity recordedEntity = RecordedEntity.fromEntity(entity);
                if (recordedEntity != null) {
                    recordedEntities.put(entity.getUUID(), recordedEntity);
                    trackedEntityUUIDs.add(entity.getUUID());
                }
            }
        }

        hasRecorded = true;

        // Register this entity as active
        ACTIVE_ENTITIES.computeIfAbsent(level(), k -> Collections.newSetFromMap(new WeakHashMap<>()))
                .add(this);
    }

    /**
     * Triggers the restoration of the recorded area
     * @param trigger The entity attempting to trigger (must be owner)
     * @return true if restoration was successful
     */
    public boolean trigger(LivingEntity trigger) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return false;
        if (!hasRecorded) return false;
        if (isRestoring) return false;

        // Check if trigger is the owner
        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID == null || !ownerUUID.equals(trigger.getUUID())) {
            return false;
        }

        isRestoring = true;

        // Restore all blocks
        for (Map.Entry<BlockPos, RecordedBlock> entry : recordedBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            RecordedBlock recordedBlock = entry.getValue();

            // Set block state
            serverLevel.setBlock(pos, recordedBlock.state, 3);

            // Restore block entity if it had one
            if (recordedBlock.blockEntityData != null) {
                BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
                if (blockEntity != null) {
                    blockEntity.loadWithComponents(recordedBlock.blockEntityData, serverLevel.registryAccess());
                    blockEntity.setChanged();
                }
            }
        }



        // Remove entities that weren't in the recording
        AABB boundingBox = new AABB(centerPos).inflate(RECORD_RADIUS);
        List<Entity> currentEntities = serverLevel.getEntities(this, boundingBox, entity ->
                !(entity instanceof CycleOfFateEntity) && !(entity.getUUID().equals(ownerUUID)));

        for (Entity entity : currentEntities) {
            if (!trackedEntityUUIDs.contains(entity.getUUID()) && !(entity instanceof Player)) {
                entity.discard();
            }
        }

        // Undo Marionette Controlling
        List<Player> players = serverLevel.getEntitiesOfClass(Player.class, boundingBox);
        for(Player player : players) {
            if(player instanceof ServerPlayer serverPlayer) {
                ControllingUtil.reset(serverPlayer, serverPlayer.serverLevel(), true);
            }
        }

        // Restore recorded entities
        for (Map.Entry<UUID, RecordedEntity> entry : recordedEntities.entrySet()) {
            UUID entityUUID = entry.getKey();
            RecordedEntity recordedEntity = entry.getValue();

            Entity existingEntity = serverLevel.getEntity(entityUUID);

            // Skip if entity has died
            if (existingEntity != null && !existingEntity.isAlive()) {
                continue;
            }

            // Restore or recreate entity
            if (existingEntity != null) {
                recordedEntity.restoreTo(existingEntity, serverLevel);
            } else {
                // Entity was removed, recreate it
                Entity newEntity = recordedEntity.recreate(serverLevel);
                if (newEntity != null) {
                    serverLevel.addFreshEntity(newEntity);
                }
            }
        }

        // Remove this entity
        this.discard();
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        // Check if should be removed
        LivingEntity owner = getOwner();
        if (owner == null || owner.isRemoved() || !owner.isAlive()) {
            this.discard();
            return;
        }

        // Check if owner is in different dimension
        if (!owner.level().equals(this.level())) {
            this.discard();
            return;
        }

        // Check if owner is too far away
        if (owner.distanceToSqr(this) > OWNER_MAX_DISTANCE * OWNER_MAX_DISTANCE) {
            this.discard();
            return;
        }

        // Prevent entities from leaving the radius
        if (hasRecorded && centerPos != null) {
            AABB boundingBox = new AABB(centerPos).inflate(RECORD_RADIUS);
            List<Entity> entities = serverLevel.getEntities(this, boundingBox);

            for (Entity entity : entities) {
                if (trackedEntityUUIDs.contains(entity.getUUID())) {
                    Vec3 entityPos = entity.position();
                    Vec3 centerVec = Vec3.atCenterOf(centerPos);
                    double distSq = entityPos.distanceToSqr(centerVec);

                    // If entity is at or beyond the boundary
                    if (distSq >= RECORD_RADIUS * RECORD_RADIUS) {
                        // Get the last recorded position
                        RecordedEntity recordedEntity = recordedEntities.get(entity.getUUID());
                        if (recordedEntity != null) {
                            // Calculate the position just inside the boundary
                            Vec3 direction = entityPos.subtract(centerVec).normalize();
                            Vec3 boundaryPos = centerVec.add(direction.scale(RECORD_RADIUS - 1));

                            entity.teleportTo(boundaryPos.x, boundaryPos.y, boundaryPos.z);
                            entity.setDeltaMovement(Vec3.ZERO);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        // Clean up from active entities list
        Set<CycleOfFateEntity> entities = ACTIVE_ENTITIES.get(level());
        if (entities != null) {
            entities.remove(this);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        // Intentionally empty - entity should not persist
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        // Intentionally empty - entity should not persist
    }

    @Override
    public boolean shouldBeSaved() {
        return false; // Never save this entity
    }

    // Recorded block data
    private static class RecordedBlock {
        final BlockState state;
        final CompoundTag blockEntityData;

        RecordedBlock(BlockState state, @Nullable CompoundTag blockEntityData) {
            this.state = state;
            this.blockEntityData = blockEntityData;
        }
    }

    // Recorded entity data
    private static class RecordedEntity {
        final CompoundTag entityData;
        final Vec3 position;
        final Vec3 motion;
        final float yRot;
        final float xRot;

        // Living entity specific data
        final Float health;
        final Integer airSupply;
        final List<MobEffectInstance> effects;
        final CompoundTag inventoryData;
        final Integer foodLevel;
        final Float saturation;

        private RecordedEntity(CompoundTag entityData, Vec3 position, Vec3 motion,
                               float yRot, float xRot, @Nullable Float health,
                               @Nullable Integer airSupply, @Nullable List<MobEffectInstance> effects,
                               @Nullable CompoundTag inventoryData, @Nullable Integer foodLevel,
                               @Nullable Float saturation) {
            this.entityData = entityData;
            this.position = position;
            this.motion = motion;
            this.yRot = yRot;
            this.xRot = xRot;
            this.health = health;
            this.airSupply = airSupply;
            this.effects = effects;
            this.inventoryData = inventoryData;
            this.foodLevel = foodLevel;
            this.saturation = saturation;
        }

        @Nullable
        static RecordedEntity fromEntity(Entity entity) {
            CompoundTag nbt = new CompoundTag();
            entity.saveWithoutId(nbt);

            Vec3 position = entity.position();
            Vec3 motion = entity.getDeltaMovement();
            float yRot = entity.getYRot();
            float xRot = entity.getXRot();

            Float health = null;
            Integer airSupply = null;
            List<MobEffectInstance> effects = null;
            CompoundTag inventoryData = null;
            Integer foodLevel = null;
            Float saturation = null;

            if (entity instanceof LivingEntity livingEntity) {
                health = livingEntity.getHealth();
                airSupply = livingEntity.getAirSupply();
                effects = new ArrayList<>(livingEntity.getActiveEffects());

                // Save inventory for players and mobs with inventory
                if (entity instanceof Player player) {
                    inventoryData = new CompoundTag();
                    ListTag inventoryList = new ListTag();
                    player.getInventory().save(inventoryList);
                    inventoryData.put("Inventory", inventoryList);

                    foodLevel = player.getFoodData().getFoodLevel();
                    saturation = player.getFoodData().getSaturationLevel();
                } else if (livingEntity instanceof Mob mob) {
                    // Save mob inventory if applicable
                    inventoryData = new CompoundTag();
                    ListTag armorItems = new ListTag();
                    ListTag handItems = new ListTag();

                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        ItemStack itemStack = mob.getItemBySlot(slot);
                        if (!itemStack.isEmpty()) {
                            CompoundTag itemTag = new CompoundTag();
                            itemStack.save(mob.level().registryAccess(), itemTag);
                            if (slot.getType() == EquipmentSlot.Type.HAND) {
                                handItems.add(itemTag);
                            } else if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                                armorItems.add(itemTag);
                            }
                        }
                    }
                    inventoryData.put("ArmorItems", armorItems);
                    inventoryData.put("HandItems", handItems);
                }
            }

            return new RecordedEntity(nbt, position, motion, yRot, xRot, health,
                    airSupply, effects, inventoryData, foodLevel, saturation);
        }

        void restoreTo(Entity entity, ServerLevel level) {
            // Restore position and rotation
            entity.teleportTo(position.x, position.y, position.z);
            entity.setYRot(yRot);
            entity.setXRot(xRot);
            entity.setDeltaMovement(motion);

            // Restore living entity data
            if (entity instanceof LivingEntity livingEntity) {
                if (health != null) {
                    livingEntity.setHealth(health);
                }
                if (airSupply != null) {
                    livingEntity.setAirSupply(airSupply);
                }
                if (effects != null) {
                    livingEntity.removeAllEffects();
                    for (MobEffectInstance effect : effects) {
                        livingEntity.addEffect(new MobEffectInstance(effect));
                    }
                }

                // Restore inventory
                if (inventoryData != null) {
                    if (entity instanceof Player player) {
                        if (inventoryData.contains("Inventory")) {
                            ListTag inventoryList = inventoryData.getList("Inventory", 10);
                            player.getInventory().load(inventoryList);
                        }
                        if (foodLevel != null) {
                            player.getFoodData().setFoodLevel(foodLevel);
                        }
                        if (saturation != null) {
                            player.getFoodData().setSaturation(saturation);
                        }
                    } else if (livingEntity instanceof Mob mob) {
                        if (inventoryData.contains("ArmorItems")) {
                            ListTag armorItems = inventoryData.getList("ArmorItems", 10);
                            // Restore armor items
                        }
                        if (inventoryData.contains("HandItems")) {
                            ListTag handItems = inventoryData.getList("HandItems", 10);
                            // Restore hand items
                        }
                    }
                }
            }

            // Restore additional NBT data
            entity.load(entityData);
        }

        @Nullable
        Entity recreate(ServerLevel level) {
            try {
                Optional<EntityType<?>> entityTypeOpt = EntityType.by(entityData);
                if (entityTypeOpt.isPresent()) {
                    Entity entity = entityTypeOpt.get().create(level);
                    if (entity != null) {
                        entity.load(entityData);
                        restoreTo(entity, level);
                        return entity;
                    }
                }
            } catch (Exception e) {
                // Log error if entity recreation fails
                e.printStackTrace();
            }
            return null;
        }
    }
}