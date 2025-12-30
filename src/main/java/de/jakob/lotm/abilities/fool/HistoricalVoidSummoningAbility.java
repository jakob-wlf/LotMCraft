package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.*;

@EventBusSubscriber
public class HistoricalVoidSummoningAbility extends SelectableAbilityItem {
    private static final String MARKED_ENTITIES_TAG = "MarkedEntities";
    private static final String SUMMONED_COUNT_TAG = "SummonedCount_Session"; // Changed to session-only
    private static final String PLACED_BLOCKS_TAG = "VoidPlacedBlocks";
    private static final int MAX_MARKED_ENTITIES = 54;
    private static final int MAX_SUMMONED = 5;
    private static final int SUMMON_DURATION_TICKS = 20 * 20;

    // Track placed blocks and their summon times
    private static final Map<BlockPos, PlacedBlockData> placedBlocks = new HashMap<>();

    private static class PlacedBlockData {
        long summonTime;
        UUID playerUUID;

        PlacedBlockData(long summonTime, UUID playerUUID) {
            this.summonTime = summonTime;
            this.playerUUID = playerUUID;
        }
    }

    public HistoricalVoidSummoningAbility(Properties properties) {
        super(properties, 1);

        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 920;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.historical_void_summoning.summon_item", "ability.lotmcraft.historical_void_summoning.summon_entity", "ability.lotmcraft.historical_void_summoning.mark_items", "ability.lotmcraft.historical_void_summoning.mark_entity"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) {
            return;
        }

        switch(abilityIndex) {
            case 0: // Summon Item
                summonItem(serverLevel, player);
                break;
            case 1: // Summon Entity
                summonEntity(serverLevel, player);
                break;
            case 2: // Mark Items
                markItems(serverLevel, player);
                break;
            case 3: // Mark Entity
                markEntity(serverLevel, player);
                break;
        }
    }

    private void summonItem(ServerLevel level, ServerPlayer player) {
        int currentSummoned = getSummonedCount(player);
        if(currentSummoned >= MAX_SUMMONED) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.max_summoned").withStyle(ChatFormatting.RED));
            return;
        }

        // Open a menu showing the player's ender chest
        Container enderChest = player.getEnderChestInventory();
        SimpleContainer displayContainer = new SimpleContainer(27) {
            @Override
            public boolean canTakeItem(Container target, int index, ItemStack stack) {
                return false; // Prevent taking items normally
            }
        };

        for(int i = 0; i < Math.min(27, enderChest.getContainerSize()); i++) {
            displayContainer.setItem(i, enderChest.getItem(i).copy());
        }

        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x3, id, inv, displayContainer, 3) {
                    @Override
                    public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, net.minecraft.world.entity.player.Player clickPlayer) {
                        if(slotId >= 0 && slotId < 27) {
                            ItemStack clickedItem = displayContainer.getItem(slotId);
                            if(!clickedItem.isEmpty()) {
                                createTemporaryItem(level, player, clickedItem.copy());
                                incrementSummonedCount(player);
                                player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.summoned_item", clickedItem.getHoverName().getString()).withStyle(ChatFormatting.GREEN));
                                player.closeContainer();
                            }
                        }
                    }
                },
                Component.translatable("ability.lotmcraft.historical_void_summoning.select_item")
        ));
    }



    private void createTemporaryItem(ServerLevel level, ServerPlayer player, ItemStack item) {
        // Give the item to the player with NBT marking it as temporary
        long summonTime = level.getGameTime();
        CompoundTag customTag = new CompoundTag();
        customTag.putLong("VoidSummonTime", summonTime);
        customTag.putUUID("VoidSummonOwner", player.getUUID());

        item.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(customTag)
        );

        player.getInventory().add(item);

        // Schedule removal after 2 minutes (2400 ticks)
        ServerScheduler.scheduleDelayed(SUMMON_DURATION_TICKS, () -> {
            removeTemporaryItem(level, player, summonTime);
        }, level);
    }

    private void removeTemporaryItem(ServerLevel level, ServerPlayer player, long summonTime) {
        // Find and remove the temporary item from player's inventory
        for(int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if(!stack.isEmpty()) {
                net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                if(customData != null) {
                    CompoundTag tag = customData.copyTag();
                    if(tag.contains("VoidSummonTime") && tag.contains("VoidSummonOwner")) {
                        long itemSummonTime = tag.getLong("VoidSummonTime");
                        UUID ownerId = tag.getUUID("VoidSummonOwner");
                        if(ownerId.equals(player.getUUID()) && itemSummonTime == summonTime) {
                            player.getInventory().removeItem(i, stack.getCount());
                            player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.item_returned").withStyle(ChatFormatting.GRAY));
                            decrementSummonedCount(player);
                            break;
                        }
                    }
                }
            }
        }

        // Also remove any placed blocks from this summon
        removeTemporaryBlocks(level, player, summonTime);
    }

    private void removeTemporaryBlocks(ServerLevel level, ServerPlayer player, long summonTime) {
        List<BlockPos> toRemove = new ArrayList<>();

        for(Map.Entry<BlockPos, PlacedBlockData> entry : placedBlocks.entrySet()) {
            if(entry.getValue().summonTime == summonTime && entry.getValue().playerUUID.equals(player.getUUID())) {
                BlockPos pos = entry.getKey();
                level.removeBlock(pos, false);
                toRemove.add(pos);
            }
        }

        toRemove.forEach(placedBlocks::remove);
    }

    // Event handler for block placement
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack placedItem = event.getPlacedBlock().getBlock().asItem().getDefaultInstance();
        ItemStack heldItem = player.getMainHandItem();

        if(heldItem.isEmpty()) {
            heldItem = player.getOffhandItem();
        }

        if(!heldItem.isEmpty()) {
            net.minecraft.world.item.component.CustomData customData = heldItem.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if(customData != null) {
                CompoundTag tag = customData.copyTag();
                if(tag.contains("VoidSummonTime") && tag.contains("VoidSummonOwner")) {
                    long summonTime = tag.getLong("VoidSummonTime");
                    UUID ownerId = tag.getUUID("VoidSummonOwner");

                    // Track this placed block
                    placedBlocks.put(event.getPos(), new PlacedBlockData(summonTime, ownerId));
                }
            }
        }
    }

    // Event handler for block breaking
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        BlockPos pos = event.getPos();

        if(placedBlocks.containsKey(pos)) {
            // Remove drops from void-summoned blocks
            event.setCanceled(true);
            // Manually remove the block without drops
            if(event.getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.removeBlock(pos, false);
            }
            placedBlocks.remove(pos);
        }
    }

    // Event handler for item toss
    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemStack tossedItem = event.getEntity().getItem();

        net.minecraft.world.item.component.CustomData customData = tossedItem.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if(customData != null) {
            CompoundTag tag = customData.copyTag();
            if(tag.contains("VoidSummonTime") && tag.contains("VoidSummonOwner")) {
                // This is a summoned item being tossed - make it disappear
                event.getEntity().discard();

                // Notify player and decrement count
                UUID ownerId = tag.getUUID("VoidSummonOwner");
                ServerPlayer player = (ServerPlayer) event.getPlayer();

                if(player.getUUID().equals(ownerId)) {
                    player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.item_returned").withStyle(ChatFormatting.GRAY));
                    decrementSummonedCount(player);
                }

                event.setCanceled(true);
            }
        }
    }

    private void summonEntity(ServerLevel level, ServerPlayer player) {
        int currentSummoned = getSummonedCount(player);
        if(currentSummoned >= MAX_SUMMONED) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.max_summoned").withStyle(ChatFormatting.RED));
            return;
        }

        List<CompoundTag> markedEntities = getMarkedEntities(player);
        if(markedEntities.isEmpty()) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.no_marked_entities").withStyle(ChatFormatting.RED));
            return;
        }

        // Create a container with entity representations
        SimpleContainer entityContainer = new SimpleContainer(54) {
            @Override
            public boolean canTakeItem(Container target, int index, ItemStack stack) {
                return false; // Prevent taking items normally
            }
        };

        for(int i = 0; i < Math.min(markedEntities.size(), 54); i++) {
            CompoundTag entityData = markedEntities.get(i);
            ItemStack displayItem = createEntityDisplayItem(entityData);
            entityContainer.setItem(i, displayItem);
        }

        final int finalContainerSize = entityContainer.getContainerSize();

        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x6, id, inv, entityContainer, 6) {
                    @Override
                    public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, net.minecraft.world.entity.player.Player clickPlayer) {
                        if(slotId >= 0 && slotId < finalContainerSize) {
                            ItemStack clickedItem = entityContainer.getItem(slotId);
                            if(!clickedItem.isEmpty()) {
                                net.minecraft.world.item.component.CustomData customData = clickedItem.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                                if(customData != null) {
                                    CompoundTag tag = customData.copyTag();
                                    if(tag.contains("EntityData")) {
                                        CompoundTag entityData = tag.getCompound("EntityData");
                                        // Execute on server thread to avoid threading issues
                                        level.getServer().execute(() -> {
                                            spawnTemporaryEntity(level, player, entityData);
                                        });
                                        player.closeContainer();
                                    }
                                }
                            }
                        }
                    }
                },
                Component.translatable("ability.lotmcraft.historical_void_summoning.select_entity")
        ));
    }



    private ItemStack createEntityDisplayItem(CompoundTag entityData) {
        String entityId = entityData.getString("EntityType");
        String customName = entityData.getString("CustomName");

        // Create a spawn egg or representation item
        ItemStack display = new ItemStack(net.minecraft.world.item.Items.PLAYER_HEAD);
        display.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                Component.literal(customName.isEmpty() ? entityId : customName));

        CompoundTag customTag = new CompoundTag();
        customTag.put("EntityData", entityData);

        display.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(customTag)
        );

        return display;
    }

    private void spawnTemporaryEntity(ServerLevel level, ServerPlayer player, CompoundTag entityData) {
        try {
            String entityTypeId = entityData.getString("EntityType");
            Optional<EntityType<?>> optionalType = EntityType.byString(entityTypeId);

            if(optionalType.isEmpty()) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.unknown_entity_type").withStyle(ChatFormatting.RED));
                return;
            }

            EntityType<?> entityType = optionalType.get();
            Entity entity = null;

            // Special handling for BeyonderNPCEntity
            if(entityData.getBoolean("IsBeyonderNPC")) {
                String pathway = entityData.getString("BeyonderPathway");
                int sequence = entityData.getInt("BeyonderSequence");
                String skin = entityData.getString("BeyonderSkin");
                boolean hostile = entityData.getBoolean("BeyonderHostile");

                // Create BeyonderNPCEntity with proper constructor
                entity = new BeyonderNPCEntity(
                        (EntityType<? extends BeyonderNPCEntity>) entityType,
                        level,
                        hostile,
                        skin,
                        pathway,
                        sequence
                );
            } else {
                entity = entityType.create(level);
            }

            if(entity == null) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.failed_create_entity").withStyle(ChatFormatting.RED));
                return;
            }

            // Load entity data (only for non-BeyonderNPC entities, as BeyonderNPC is already initialized)
            if(!entityData.getBoolean("IsBeyonderNPC") && entityData.contains("EntityNBT")) {
                CompoundTag entityNBT = entityData.getCompound("EntityNBT").copy();

                // Remove UUID to generate a new one and avoid conflicts
                entityNBT.remove("UUID");

                entity.load(entityNBT);
            } else if(entityData.getBoolean("IsBeyonderNPC") && entityData.contains("EntityNBT")) {
                // For BeyonderNPC, load NBT but skip some fields that are already initialized
                CompoundTag entityNBT = entityData.getCompound("EntityNBT").copy();

                // Remove UUID and custom initialization fields to avoid conflicts
                entityNBT.remove("UUID");
                entityNBT.remove("pathway");
                entityNBT.remove("sequence");
                entityNBT.remove("skin");
                entityNBT.remove("hostile");

                // Load remaining data (health, position, etc.)
                entity.load(entityNBT);
            }

            // Position in front of player (after loading NBT to override any position data)
            Vec3 lookVec = player.getLookAngle();
            Vec3 pos = player.position().add(lookVec.x * 2, 0, lookVec.z * 2);
            entity.moveTo(pos.x, pos.y, pos.z, player.getYRot(), 0);

            // Ensure entity has a new UUID
            entity.setUUID(UUID.randomUUID());

            // Mark as temporary
            long summonTime = level.getGameTime();
            CompoundTag tag = entity.getPersistentData();
            tag.putLong("VoidSummonTime", summonTime);
            tag.putUUID("VoidSummonOwner", player.getUUID());
            tag.putBoolean("VoidSummoned", true);

            // Add entity to world
            boolean spawned = level.addFreshEntity(entity);

            if(spawned) {
                incrementSummonedCount(player);
                // Make the summoned entity an ally of the player
                if(entity instanceof LivingEntity livingEntity) {
                    AllyUtil.makeAllies(player, livingEntity);
                }

                player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.summoned_entity", entity.getName().getString()).withStyle(ChatFormatting.GREEN));

                // Schedule removal after 2 minutes
                final UUID entityUUID = entity.getUUID();
                ServerScheduler.scheduleDelayed(SUMMON_DURATION_TICKS, () -> {
                    removeTemporaryEntity(level, player, summonTime, entityUUID);
                }, level);
            } else {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.failed_spawn_entity").withStyle(ChatFormatting.RED));
            }
        } catch(Exception e) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.error_summoning", e.getMessage()).withStyle(ChatFormatting.RED));
        }
    }

    private void removeTemporaryEntity(ServerLevel level, ServerPlayer player, long summonTime, UUID entityUUID) {
        // Find and remove the entity with matching summon time and UUID
        Entity entity = level.getEntity(entityUUID);

        if(entity != null && entity.getPersistentData().contains("VoidSummoned")) {
            long entitySummonTime = entity.getPersistentData().getLong("VoidSummonTime");
            UUID ownerId = entity.getPersistentData().getUUID("VoidSummonOwner");

            if(entitySummonTime == summonTime && ownerId.equals(player.getUUID())) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                if(player.isAlive()) {
                    player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.entity_returned").withStyle(ChatFormatting.GRAY));
                }
                decrementSummonedCount(player);
                return;
            }
        }

        // Fallback: search nearby if direct UUID lookup failed
        AABB searchBox = new AABB(player.blockPosition()).inflate(100);
        List<Entity> entities = level.getEntities((Entity)null, searchBox, e -> {
            if(e.getPersistentData().contains("VoidSummoned")) {
                long entitySummonTime = e.getPersistentData().getLong("VoidSummonTime");
                UUID ownerId = e.getPersistentData().getUUID("VoidSummonOwner");
                return entitySummonTime == summonTime && ownerId.equals(player.getUUID());
            }
            return false;
        });

        for(Entity e : entities) {
            e.remove(Entity.RemovalReason.DISCARDED);
            if(player.isAlive()) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.entity_returned").withStyle(ChatFormatting.GRAY));
            }
            decrementSummonedCount(player);
        }
    }

    private void markItems(ServerLevel level, ServerPlayer player) {
        // Open the player's ender chest for them to add items
        Container enderChest = player.getEnderChestInventory();

        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> ChestMenu.threeRows(id, inv, enderChest),
                Component.translatable("ability.lotmcraft.historical_void_summoning.mark_items_title")
        ));

        player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.mark_items_instruction").withStyle(ChatFormatting.GREEN));
    }

    private void markEntity(ServerLevel level, ServerPlayer player) {
        // Find nearby entities
        AABB searchBox = player.getBoundingBox().inflate(10);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> e != player && e.isAlive());

        if(nearbyEntities.isEmpty()) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.no_nearby_entities").withStyle(ChatFormatting.RED));
            return;
        }

        // Get closest entity
        LivingEntity closest = nearbyEntities.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                .orElse(null);

        if(closest == null) return;

        // Save entity data
        CompoundTag entityData = new CompoundTag();
        entityData.putString("EntityType", EntityType.getKey(closest.getType()).toString());
        entityData.putString("CustomName", closest.hasCustomName() ? closest.getCustomName().getString() : closest.getName().getString());

        CompoundTag entityNBT = new CompoundTag();
        closest.save(entityNBT);
        entityData.put("EntityNBT", entityNBT);

        // Special handling for BeyonderNPCEntity
        if(closest instanceof BeyonderNPCEntity beyonderNPC) {
            entityData.putBoolean("IsBeyonderNPC", true);
            entityData.putString("BeyonderPathway", beyonderNPC.getPathway());
            entityData.putInt("BeyonderSequence", beyonderNPC.getSequence());
            entityData.putString("BeyonderSkin", beyonderNPC.getSkinName());
            entityData.putBoolean("BeyonderHostile", beyonderNPC.isHostile());
        } else {
            entityData.putBoolean("IsBeyonderNPC", false);
        }

        addMarkedEntity(player, entityData);

        player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.marked_entity", closest.getName().getString()).withStyle(ChatFormatting.GREEN));
    }

    private List<CompoundTag> getMarkedEntities(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        List<CompoundTag> entities = new ArrayList<>();

        if(data.contains(MARKED_ENTITIES_TAG)) {
            ListTag list = data.getList(MARKED_ENTITIES_TAG, Tag.TAG_COMPOUND);
            for(int i = 0; i < list.size(); i++) {
                entities.add(list.getCompound(i));
            }
        }

        return entities;
    }

    private void addMarkedEntity(ServerPlayer player, CompoundTag entityData) {
        CompoundTag data = player.getPersistentData();
        ListTag list;

        if(data.contains(MARKED_ENTITIES_TAG)) {
            list = data.getList(MARKED_ENTITIES_TAG, Tag.TAG_COMPOUND);
        } else {
            list = new ListTag();
        }

        list.add(entityData);

        // Remove oldest if over limit
        while(list.size() > MAX_MARKED_ENTITIES) {
            list.remove(0);
        }

        data.put(MARKED_ENTITIES_TAG, list);
    }

    private int getSummonedCount(ServerPlayer player) {
        // Use a session-only tag that doesn't persist
        if(!player.getPersistentData().contains(SUMMONED_COUNT_TAG)) {
            player.getPersistentData().putInt(SUMMONED_COUNT_TAG, 0);
        }
        return player.getPersistentData().getInt(SUMMONED_COUNT_TAG);
    }

    private void incrementSummonedCount(ServerPlayer player) {
        int current = getSummonedCount(player);
        player.getPersistentData().putInt(SUMMONED_COUNT_TAG, current + 1);
    }

    private static void decrementSummonedCount(ServerPlayer player) {
        if(!player.getPersistentData().contains(SUMMONED_COUNT_TAG)) {
            player.getPersistentData().putInt(SUMMONED_COUNT_TAG, 0);
            return;
        }
        int count = Math.max(0, player.getPersistentData().getInt(SUMMONED_COUNT_TAG) - 1);
        player.getPersistentData().putInt(SUMMONED_COUNT_TAG, count);
    }
}