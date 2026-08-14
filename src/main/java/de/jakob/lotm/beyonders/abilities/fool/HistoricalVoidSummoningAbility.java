package de.jakob.lotm.beyonders.abilities.fool;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.CopiedInventoryComponent;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.HistoricalVoidComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.gui.custom.historical_void.HistoricalVoidMenu;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenHistoricalVoidBorrowingScreenPacket;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.beyonders.potions.BeyonderPotion;
import de.jakob.lotm.network.packets.toClient.SyncHistoricalVoidSummoningCountPacket;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.Config;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber
public class HistoricalVoidSummoningAbility extends SelectableAbility {
    public static final String MARKED_ENTITIES_TAG = "MarkedEntities";
    private static final int MAX_MARKED_ENTITIES = 54;
    public static final String MARKED_ITEMS_TAG = "MarkedItems";
    private static final int MAX_MARKED_ITEMS = 54;

    // Track placed blocks and their summon times (thread-safe)
    private static final Map<BlockPos, PlacedBlockData> placedBlocks = new ConcurrentHashMap<>();

    public enum SummonType {
        ITEM, ENTITY, HEALTH, SPIRITUALITY, CLEANSED_STATE, SEQUENCE
    }

    private static class PlacedBlockData {
        final long summonTime;
        final UUID playerUUID;

        PlacedBlockData(long summonTime, UUID playerUUID) {
            this.summonTime = summonTime;
            this.playerUUID = playerUUID;
        }
    }

    public HistoricalVoidSummoningAbility(String id) {
        super(id, 1);

        canBeUsedByNPC = false;
        cannotBeStolen = true;
        canBeUsedInArtifact = false;
        canBeShared = false;
        canBeCopied = false;
        canBeReplicated = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.historical_void_summoning.summon_item",
                "ability.lotmcraft.historical_void_summoning.summon_entity",
                "ability.lotmcraft.historical_void_summoning.mark_items",
                "ability.lotmcraft.historical_void_summoning.mark_entity",
                "ability.lotmcraft.historical_void_summoning.mark_self",
                "ability.lotmcraft.historical_void_summoning.historical_void_borrowing",
                "ability.lotmcraft.historical_void_summoning.return_all"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) {
            return;
        }

        if(abilityIndex == 0 || abilityIndex == 1 || abilityIndex == 5) {
            if(BeyonderData.getSpirituality(entity) < 5000) return;
            BeyonderData.reduceSpirituality(entity, 5000);
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
            case 4: // Mark the player
                markSelf(serverLevel, player);
                break;
            case 5: // Borrow from History
                historicalVoidBorrowing(player);
                break;
            case 6: // Return everything summoned back to the void
                returnAllSummoned(player);
                break;
        }
    }

    @Override
    public void onHold(Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            PacketHandler.sendToPlayer(player, new SyncHistoricalVoidSummoningCountPacket(player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).summonedCount));
        }
    }

    private static void returnAllSummoned(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        UUID ownerId = player.getUUID();
        int itemsReturned = 0;
        int entitiesReturned = 0;

        for (ServerLevel serverLevel : server.getAllLevels()) {
            List<Entity> toRemove = new ArrayList<>();
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity.getPersistentData().getBoolean("VoidSummoned")
                        && entity.getPersistentData().hasUUID("VoidSummonOwner")
                        && entity.getPersistentData().getUUID("VoidSummonOwner").equals(ownerId)) {
                    toRemove.add(entity);
                }
            }
            for (Entity entity : toRemove) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                entitiesReturned++;
            }
        }

        for (ServerPlayer onlinePlayer : server.getPlayerList().getPlayers()) {
            itemsReturned += clearSummonedItemsFrom(onlinePlayer.getInventory(), ownerId);
        }

        for (ServerLevel serverLevel : server.getAllLevels()) {
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof ItemEntity itemEntity && isSummonedByOwner(itemEntity.getItem(), ownerId)) {
                    itemEntity.discard();
                    itemsReturned++;
                }
            }
        }

        for (ServerLevel serverLevel : server.getAllLevels()) {
            for (ChunkHolder holder : getLoadedChunkHolders(serverLevel.getChunkSource().chunkMap)) {
                LevelChunk chunk = holder.getTickingChunk();
                if (chunk == null) continue;
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof Container container) {
                        itemsReturned += clearSummonedItemsFrom(container, ownerId);
                    }
                }
            }
        }

        List<BlockPos> blocksToRemove = new ArrayList<>();
        for (Map.Entry<BlockPos, PlacedBlockData> entry : placedBlocks.entrySet()) {
            if (entry.getValue().playerUUID.equals(ownerId)) {
                for (ServerLevel serverLevel : server.getAllLevels()) {
                    serverLevel.removeBlock(entry.getKey(), false);
                }
                blocksToRemove.add(entry.getKey());
            }
        }
        blocksToRemove.forEach(placedBlocks::remove);

        HistoricalVoidComponent data = player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
        data.activeSummonTimes.entrySet().removeIf(e ->
                e.getValue().type() == SummonType.ITEM || e.getValue().type() == SummonType.ENTITY);
        data.summonedCount = 0;

        player.sendSystemMessage(Component.translatable(
                "ability.lotmcraft.historical_void_summoning.returned_all", itemsReturned, entitiesReturned
        ).withStyle(ChatFormatting.GRAY));
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Container craftMatrix = event.getInventory();
        ItemStack crafted = event.getCrafting();

        if (crafted.isEmpty()) return;

        long earliestExpiry = Long.MAX_VALUE;
        UUID ownerId = null;
        boolean usedVoidItem = false;

        for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
            ItemStack ingredient = craftMatrix.getItem(i);
            if (ingredient.isEmpty()) continue;

            CustomData customData = ingredient.get(DataComponents.CUSTOM_DATA);
            if (customData == null) continue;

            CompoundTag tag = customData.copyTag();
            if (tag.contains("VoidSummonTime") && tag.contains("VoidSummonOwner")) {
                usedVoidItem = true;
                long time = tag.getLong("VoidSummonTime");
                if (time < earliestExpiry) {
                    earliestExpiry = time;
                }
                ownerId = tag.getUUID("VoidSummonOwner");
            }
        }

        if (!usedVoidItem) return;

        // Bind the crafted result to the void too, so it disappears just like its ingredients would have.
        final UUID finalOwnerId = ownerId;
        final long finalExpiry = earliestExpiry;
        CustomData.update(DataComponents.CUSTOM_DATA, crafted, tag -> {
            tag.putLong("VoidSummonTime", finalExpiry);
            tag.putUUID("VoidSummonOwner", finalOwnerId);
            tag.putUUID("UniqueSummonID", UUID.randomUUID());
        });
    }

    // I tried way to long to use accesstranformes, didnbt work so now we are using reflecton :,(
    private static final Method GET_CHUNKS_METHOD;
    static {
        Method method;
        try {
            method = ChunkMap.class.getDeclaredMethod("getChunks");
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            method = null;
            e.printStackTrace();
        }
        GET_CHUNKS_METHOD = method;
    }

    @SuppressWarnings("unchecked")
    private static Iterable<ChunkHolder> getLoadedChunkHolders(ChunkMap chunkMap) {
        if (GET_CHUNKS_METHOD == null) return List.of();
        try {
            return (Iterable<ChunkHolder>) GET_CHUNKS_METHOD.invoke(chunkMap);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private static boolean isSummonedByOwner(ItemStack stack, UUID ownerId) {
        if (stack.isEmpty()) return false;
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return false;
        CompoundTag tag = customData.copyTag();
        return tag.contains("VoidSummonOwner") && tag.getUUID("VoidSummonOwner").equals(ownerId);
    }

    private static int clearSummonedItemsFrom(Container container, UUID ownerId) {
        int removed = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (isSummonedByOwner(stack, ownerId)) {
                container.setItem(i, ItemStack.EMPTY);
                removed++;
            }
        }
        return removed;
    }

    private void summonItem(ServerLevel level, ServerPlayer player) {
        int currentSummoned = getSummonedCount(player);
        if(currentSummoned >= getMaxSummoned(player)) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.max_summoned").withStyle(ChatFormatting.RED));
            return;
        }

        List<ItemStack> markedItems = getMarkedItems(level, player);
        if (markedItems.isEmpty()) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.no_marked_items").withStyle(ChatFormatting.RED));
            return;
        }

        SimpleContainer displayContainer = new SimpleContainer(HistoricalVoidMenu.SLOT_COUNT) {
            @Override
            public boolean canTakeItem(Container target, int index, ItemStack stack) {
                return false; // Prevent taking items normally
            }
        };

        for(int i = 0; i < Math.min(markedItems.size(), HistoricalVoidMenu.SLOT_COUNT); i++) {
            displayContainer.setItem(i, markedItems.get(i).copy());
        }

        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new HistoricalVoidMenu(id, inv, displayContainer) {
                    @Override
                    public void clicked(int slotId, int button, ClickType clickType, Player clickPlayer) {
                        if(slotId >= 0 && slotId < HistoricalVoidMenu.SLOT_COUNT) {
                            ItemStack clickedItem = displayContainer.getItem(slotId);
                            if(!clickedItem.isEmpty()) {
                                // Re-check count before summoning
                                if(getSummonedCount(player) < getMaxSummoned(player)) {
                                    createTemporaryItem(level, player, clickedItem.copy());
                                    player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.summoned_item", clickedItem.getHoverName().getString()).withStyle(ChatFormatting.GREEN));
                                } else {
                                    player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.max_summoned").withStyle(ChatFormatting.RED));
                                }
                                player.closeContainer();
                            }
                        }
                    }
                },
                Component.translatable("ability.lotmcraft.historical_void_summoning.select_item")
        ));
    }

    private void createTemporaryItem(ServerLevel level, ServerPlayer player, ItemStack item) {
        long summonTime = level.getGameTime() + getSummonDurationTicks(player);
        CompoundTag customTag = new CompoundTag();
        customTag.putLong("VoidSummonTime", summonTime);
        customTag.putUUID("VoidSummonOwner", player.getUUID());
        customTag.putUUID("UniqueSummonID", UUID.randomUUID());

        item.set(DataComponents.CUSTOM_DATA,
                CustomData.of(customTag)
        );

        player.getInventory().add(item);
        EffectManager.playEffect(EffectManager.Effect.HISTORICAL_VOID_SUMMONING, player.getX(), player.getY(), player.getZ(), level);

        incrementSummonedCount(player, summonTime, SummonType.ITEM, null);

        ServerScheduler.scheduleDelayed(getSummonDurationTicks(player), () -> {
            ServerPlayer onlinePlayer = level.getServer().getPlayerList().getPlayer(player.getUUID());
            if(onlinePlayer != null) {
                removeTemporaryItem(level, onlinePlayer, summonTime);
            }
        }, level);
    }

    private static void removeTemporaryItem(ServerLevel level, ServerPlayer player, long summonTime) {
        // Find and remove the temporary item from player's inventory
        boolean foundAndRemoved = false;
        for(int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if(!stack.isEmpty()) {
                CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                if(customData != null) {
                    CompoundTag tag = customData.copyTag();
                    if(tag.contains("VoidSummonTime") && tag.contains("VoidSummonOwner")) {
                        long itemSummonTime = tag.getLong("VoidSummonTime");
                        UUID ownerId = tag.getUUID("VoidSummonOwner");
                        if(ownerId.equals(player.getUUID()) && itemSummonTime == summonTime) {
                            player.getInventory().removeItem(i, stack.getCount());
                            foundAndRemoved = true;
                            break;
                        }
                    }
                }
            }
        }

        // Also remove any placed blocks from this summon
        removeTemporaryBlocks(level, player, summonTime);

        // Decrement count and notify
        if(foundAndRemoved || hasPlacedBlocksForSummon(player.getUUID(), summonTime)) {
            decrementSummonedCount(player, summonTime);
            if(player.isAlive()) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.item_returned").withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private static boolean hasPlacedBlocksForSummon(UUID playerUUID, long summonTime) {
        return placedBlocks.values().stream()
                .anyMatch(data -> data.playerUUID.equals(playerUUID) && data.summonTime == summonTime);
    }

    private static void removeTemporaryBlocks(ServerLevel level, ServerPlayer player, long summonTime) {
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



    private void summonEntity(ServerLevel level, ServerPlayer player) {
        int currentSummoned = getSummonedCount(player);
        if(currentSummoned >= getMaxSummoned(player)) {
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

        ItemStack deleteItem = new ItemStack(Items.BARRIER);
        deleteItem.set(DataComponents.CUSTOM_NAME, Component.literal("Clear Entity Mode").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        deleteItem.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("Click an entity after this to remove it from your records").withStyle(ChatFormatting.GRAY))));

        CompoundTag deleteTag = new CompoundTag();
        deleteTag.putBoolean("isDeleteMode", true);
        deleteItem.set(DataComponents.CUSTOM_DATA, CustomData.of(deleteTag));
        entityContainer.setItem(0, deleteItem);

        for(int i = 0; i < Math.min(markedEntities.size(), 53); i++) {
            CompoundTag entityData = markedEntities.get(i);
            ItemStack displayItem = createEntityDisplayItem(entityData);

            if (entityData.contains("EntityNBT")) {
                CompoundTag entityNBT = entityData.getCompound("EntityNBT");
                CompoundTag nfd = entityNBT.getCompound("neoforge:attachments").getCompound("lotmcraft:beyonder_component");

                if (nfd.contains("pathway") && BeyonderData.pathwayInfos.get(nfd.get("pathway")) != null) {
                    boolean isMarionette = Optional.of(entityNBT.getCompound("neoforge:attachments").getCompound("lotmcraft:marionette_component")).map(c -> c.getBoolean("isMarionette")).orElse(false);
                    displayItem.set(
                            DataComponents.LORE,
                            new ItemLore(List.of(
                                    Component.literal("-------------------").withStyle(style -> style.withColor(0xFFa742f5).withItalic(false)),
                                    Component.translatable("lotm.pathway").append(Component.literal(": ")).append(Component.literal(BeyonderData.pathwayInfos.get(nfd.getString("pathway")).getSequenceName(9))).withColor(0xa26fc9).withStyle(style -> style.withItalic(false)),
                                    Component.translatable("lotm.sequence").append(Component.literal(": ")).append(Component.literal(nfd.getInt("sequence") + "")).withColor(0xa26fc9).withStyle(style -> style.withItalic(false)),
                                    Component.translatable("lotm.marionette").append(Component.literal(": ")).append(Component.literal(isMarionette + "")).withColor(0xa26fc9).withStyle(style -> style.withItalic(false))
                            )));
                }
            }
            entityContainer.setItem(i + 1, displayItem);
        }

        final int finalContainerSize = entityContainer.getContainerSize();

        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new HistoricalVoidMenu(id, inv, entityContainer) {
                    private boolean isDeleting = false;
                    @Override
                    public void clicked(int slotId, int button, ClickType clickType, Player clickPlayer) {
                        if(slotId >= 0 && slotId < finalContainerSize) {
                            ItemStack clickedItem = entityContainer.getItem(slotId);

                            if(clickedItem.isEmpty()) return;

                            CustomData customData = clickedItem.get(DataComponents.CUSTOM_DATA);

                            if(customData == null) return;

                            CompoundTag tag = customData.copyTag();

                            if(tag.contains("isDeleteMode")) {
                                this.isDeleting = !this.isDeleting;
                                return;
                            }

                            if(tag.contains("EntityData")) {
                                CompoundTag entityData = tag.getCompound("EntityData");
                                if(isDeleting) {
                                    removedMarkedEntity(player, entityData);
                                    player.closeContainer();
                                } else {
                                    if(getSummonedCount(player) < getMaxSummoned(player)) {
                                        level.getServer().execute(() -> {
                                            spawnTemporaryEntity(level, player, entityData);
                                        });
                                    } else {
                                        player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.max_summoned").withStyle(ChatFormatting.RED));
                                    }
                                    player.closeContainer();
                                }
                            }
                        }
                    }
                },
                Component.translatable("ability.lotmcraft.historical_void_summoning.select_entity")
        ));
    }

    private static ItemStack createEntityDisplayItem(CompoundTag entityData) {
        String entityId = entityData.getString("EntityType");
        String customName = entityData.getString("CustomName");

        // Create a spawn egg or representation item
        ItemStack display = new ItemStack(Items.PLAYER_HEAD);
        display.set(DataComponents.CUSTOM_NAME,
                Component.literal(customName.isEmpty() ? entityId : customName));

        CompoundTag customTag = new CompoundTag();
        customTag.put("EntityData", entityData);

        display.set(DataComponents.CUSTOM_DATA,
                CustomData.of(customTag)
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
            Entity entity;

            boolean isPlayer = entityTypeId.equals("minecraft:player");

            if(entityData.getBoolean("IsBeyonderNPC") || isPlayer) {
                CompoundTag entityNBT = entityData.getCompound("EntityNBT");
                CompoundTag nfd = entityNBT.getCompound("neoforge:attachments").getCompound("lotmcraft:beyonder_component");

                String pathway = nfd.getString("pathway");
                int sequence = nfd.getInt("sequence");
                String skin = entityData.getString("BeyonderSkin");
                boolean hostile = entityData.getBoolean("BeyonderHostile");

                EntityType<? extends BeyonderNPCEntity> npcType = isPlayer ?
                        ModEntities.BEYONDER_NPC.get() :
                        (EntityType<? extends BeyonderNPCEntity>) entityType;

                entity = new BeyonderNPCEntity(
                        npcType,
                        level,
                        hostile,
                        skin,
                        pathway,
                        sequence,
                        false,
                        false
                );

                if(isPlayer && entityData.contains("EntityNBT")) {
                    CompoundTag playerNbt = entityData.getCompound("EntityNBT");
                    if(playerNbt.hasUUID("UUID")) {
                        if (entity instanceof BeyonderNPCEntity npc) {
                            npc.setTargetPlayerUUID(playerNbt.getUUID("UUID"));
                        }
                    }
                }

                ((BeyonderNPCEntity) entity).setQuestId("");
                entity.getPersistentData().putBoolean("Initialized", true);
            } else {
                entity = entityType.create(level);
            }

            if(entity == null) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.failed_create_entity").withStyle(ChatFormatting.RED));
                return;
            }

            if(!(entityData.getBoolean("IsBeyonderNPC") || isPlayer) && entityData.contains("EntityNBT")) {
                CompoundTag entityNBT = entityData.getCompound("EntityNBT").copy();

                entityNBT.remove("UUID");
                entity.load(entityNBT);
            } else if(entityData.getBoolean("IsBeyonderNPC") && entityData.contains("EntityNBT")) {
                CompoundTag entityNBT = entityData.getCompound("EntityNBT").copy();

                entityNBT.remove("UUID");
                entityNBT.remove("skin");
                entityNBT.remove("hostile");

                entity.load(entityNBT);
            }

            CopiedInventoryComponent data = entity.getData(ModAttachments.COPIED_INVENTORY);
            SimpleContainer container = data.getInv();
            long summonTimeInv = level.getGameTime() + getSummonDurationTicks(player);

            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);

                if (!stack.isEmpty()) {
                    CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                        tag.putLong("VoidSummonTime", summonTimeInv);
                        tag.putUUID("VoidSummonOwner", player.getUUID());

                    });
                }
                if (stack.is((ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "shulker_boxes"))))) {
                    container.setItem(i, ItemStack.EMPTY);
                }
            }
            entity.setData(ModAttachments.COPIED_INVENTORY, data);

            // Position in front of player (after loading NBT to override any position data)
            Vec3 lookVec = player.getLookAngle();
            Vec3 pos = player.position().add(lookVec.x * 2, 0, lookVec.z * 2);
            entity.moveTo(pos.x, pos.y, pos.z, player.getYRot(), 0);

            // Ensure entity has a new UUID
            entity.setUUID(UUID.randomUUID());

            // Mark as temporary
            long summonTime = level.getGameTime() + getSummonDurationTicks(player);
            CompoundTag tag = entity.getPersistentData();
            tag.putLong("VoidSummonTime", summonTime);
            tag.putUUID("VoidSummonOwner", player.getUUID());
            tag.putBoolean("VoidSummoned", true);

            // Add entity to world
            boolean spawned = level.addFreshEntity(entity);

            if(spawned) {
                EffectManager.playEffect(EffectManager.Effect.HISTORICAL_VOID_SUMMONING, entity.getX(), entity.getY(), entity.getZ(), level);
                final UUID entityUUID = entity.getUUID();

                // Track this summon
                incrementSummonedCount(player, summonTime, SummonType.ENTITY, entityUUID);

                player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.summoned_entity", entity.getName().getString()).withStyle(ChatFormatting.GREEN));
                if(entity instanceof LivingEntity living)
                    AllyUtil.makeAllies(player, living);

                // Schedule removal after duration
                ServerScheduler.scheduleDelayed(getSummonDurationTicks(player), () -> {
                    // Verify player is still online
                    ServerPlayer onlinePlayer = level.getServer().getPlayerList().getPlayer(player.getUUID());
                    if(onlinePlayer != null) {
                        removeTemporaryEntity(level, onlinePlayer, summonTime, entityUUID);
                    }
                }, level);
            } else {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.failed_spawn_entity").withStyle(ChatFormatting.RED));
            }
        } catch(Exception e) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.error_summoning", e.getMessage()).withStyle(ChatFormatting.RED));
            e.printStackTrace();
        }
    }

    private void removeTemporaryEntity(ServerLevel level, ServerPlayer player, long summonTime, UUID entityUUID) {
        boolean removed = false;

        // Try direct UUID lookup first
        Entity entity = level.getEntity(entityUUID);

        if(entity != null && entity.getPersistentData().getBoolean("VoidSummoned")) {
            long entitySummonTime = entity.getPersistentData().getLong("VoidSummonTime");
            UUID ownerId = entity.getPersistentData().getUUID("VoidSummonOwner");

            if(entitySummonTime == summonTime && ownerId.equals(player.getUUID())) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                removed = true;
            }
        }

        // Fallback: search nearby if direct UUID lookup failed
        if(!removed) {
            AABB searchBox = new AABB(player.blockPosition()).inflate(100);
            List<Entity> entities = level.getEntities((Entity)null, searchBox, e -> {
                if(e.getPersistentData().getBoolean("VoidSummoned")) {
                    long entitySummonTime = e.getPersistentData().getLong("VoidSummonTime");
                    UUID ownerId = e.getPersistentData().getUUID("VoidSummonOwner");
                    return entitySummonTime == summonTime && ownerId.equals(player.getUUID());
                }
                return false;
            });

            for(Entity e : entities) {
                e.remove(Entity.RemovalReason.DISCARDED);
                removed = true;
            }
        }

        // Decrement count and notify
        if(removed) {
            decrementSummonedCount(player, summonTime);
            if(player.isAlive()) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.entity_returned").withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private static List<ItemStack> getMarkedItems(ServerLevel level, ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        List<ItemStack> items = new ArrayList<>();

        if (data.contains(MARKED_ITEMS_TAG)) {
            ListTag list = data.getList(MARKED_ITEMS_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                ItemStack.parse(level.registryAccess(), list.getCompound(i)).ifPresent(items::add);
            }
        }

        return items;
    }

    private static void addMarkedItem(ServerLevel level, ServerPlayer player, ItemStack stack) {
        CompoundTag data = player.getPersistentData();
        ListTag list;

        if (data.contains(MARKED_ITEMS_TAG)) {
            list = data.getList(MARKED_ITEMS_TAG, Tag.TAG_COMPOUND);
        } else {
            list = new ListTag();
        }

        Tag saved = stack.save(level.registryAccess(), new CompoundTag());
        list.add(saved);

        // Remove oldest if over limit
        while (list.size() > MAX_MARKED_ITEMS) {
            list.remove(0);
        }

        data.put(MARKED_ITEMS_TAG, list);
    }

    private static boolean isExcludedFromSummoning(ItemStack stack) {
        if (stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "shulker_boxes")))) return true;
        if (stack.getItem() instanceof BeyonderCharacteristicItem) return true;
        if (stack.getItem() instanceof BeyonderPotion) return true;
        return Config.items.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private static List<CompoundTag> getMarkedEntities(ServerPlayer player) {
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

    private void removedMarkedEntity(ServerPlayer player, CompoundTag entityData) {
        CompoundTag data = player.getPersistentData();
        if (data.contains(MARKED_ENTITIES_TAG)) {
            ListTag list = data.getList(MARKED_ENTITIES_TAG, Tag.TAG_COMPOUND);

            list.remove(entityData);

            data.put(MARKED_ENTITIES_TAG, list);
        }
    }



    private void markItems(ServerLevel level, ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        ItemStack toMark = !mainHand.isEmpty() ? mainHand : offHand;

        if (toMark.isEmpty()) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.no_item_held").withStyle(ChatFormatting.RED));
            return;
        }

        if (isExcludedFromSummoning(toMark)) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.item_cannot_be_marked").withStyle(ChatFormatting.RED));
            return;
        }

        addMarkedItem(level, player, toMark.copy());

        player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.marked_item", toMark.getHoverName().getString()).withStyle(ChatFormatting.GREEN));
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
        closest.saveWithoutId(entityNBT);
        entityData.put("EntityNBT", entityNBT);

        String entityTypeId = entityData.getString("EntityType");
        if (entityTypeId.equals("minecraft:player")) {
            CompoundTag playerNbt = entityData.getCompound("EntityNBT");
            if(playerNbt.hasUUID("UUID")) {
                entityData.putUUID("OriginalPlayerUUID", playerNbt.getUUID("UUID"));
            }
        }

        // Special handling for BeyonderNPCEntity
        if(closest instanceof BeyonderNPCEntity beyonderNPC) {
            entityData.putBoolean("IsBeyonderNPC", true);
            entityData.putString("BeyonderSkin", beyonderNPC.getSkinName());
            entityData.putBoolean("BeyonderHostile", beyonderNPC.isHostile());
        } else {
            entityData.putBoolean("IsBeyonderNPC", false);
        }

        addMarkedEntity(player, entityData);

        player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.marked_entity", closest.getName().getString()).withStyle(ChatFormatting.GREEN));
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

    private void markSelf(ServerLevel level, ServerPlayer player) {
        // Save entity data
        CompoundTag entityData = new CompoundTag();
        entityData.putString("EntityType", EntityType.getKey(player.getType()).toString());
        entityData.putString("CustomName", player.hasCustomName() ? player.getCustomName().getString() : player.getName().getString());

        CompoundTag entityNBT = new CompoundTag();
        player.saveWithoutId(entityNBT);

        entityData.put("EntityNBT", entityNBT);
        entityData.putBoolean("IsBeyonderNPC", false);

        String entityTypeId = entityData.getString("EntityType");
        if (entityTypeId.equals("minecraft:player")) {
            CompoundTag playerNbt = entityData.getCompound("EntityNBT");
            entityData.putUUID("OriginalPlayerUUID", playerNbt.getUUID("UUID"));
        }
        addMarkedEntity(player, entityData);

        player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.marked_entity", player.getName().getString()).withStyle(ChatFormatting.GREEN));
    }



    private int getSummonedCount(ServerPlayer player) {
        HistoricalVoidComponent data = player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
        return data.summonedCount;
    }

    private void incrementSummonedCount(ServerPlayer player, long summonTime, SummonType type, UUID entityUUID) {
        HistoricalVoidComponent data = player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
        data.summonedCount++;
        HistoricalVoidComponent.SummonInfo info = new HistoricalVoidComponent.SummonInfo(
                summonTime,
                type,
                entityUUID,
                new CompoundTag()
        );
        data.activeSummonTimes.put(summonTime, info);
    }

    private static void decrementSummonedCount(ServerPlayer player, long summonTime) {
        HistoricalVoidComponent data = player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
        data.summonedCount = Math.max(0, data.summonedCount - 1);
        data.activeSummonTimes.remove(summonTime);
    }



    private static int getHistoricalBorrowingCount(ServerPlayer player) {
        HistoricalVoidComponent data = player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
        return data.historicalBorrowingCount;
    }

    private static void incrementHistoricalBorrowingCount(ServerPlayer player, long borrowTime, SummonType type, UUID entityUUID, CompoundTag originalBeforeBorrowing) {
        HistoricalVoidComponent data = player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
        data.historicalBorrowingCount++;
        HistoricalVoidComponent.SummonInfo info = new HistoricalVoidComponent.SummonInfo(
                borrowTime,
                type,
                entityUUID,
                originalBeforeBorrowing
        );
        data.activeSummonTimes.put(borrowTime, info);
    }

    private static void decrementHistoricalBorrowingCount(ServerPlayer player, long borrowTime) {
        HistoricalVoidComponent data = player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
        data.historicalBorrowingCount = Math.max(0, data.historicalBorrowingCount - 1);

        HistoricalVoidComponent.SummonInfo specificInfo = data.activeSummonTimes.get(borrowTime);
        if(specificInfo != null) {

            if(specificInfo.type() == SummonType.HEALTH) {
                player.setHealth(specificInfo.originalBeforeBorrowing().getFloat("health"));
            }
            else if (specificInfo.type() == SummonType.SPIRITUALITY) {
                BeyonderData.setSpirituality(player, specificInfo.originalBeforeBorrowing().getFloat("spirituality"));
            }
            else if (specificInfo.type() == SummonType.CLEANSED_STATE) {
                CompoundTag tag = specificInfo.originalBeforeBorrowing();
                if (tag.getBoolean("WalkStolen")) {
                    AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
                    movementSpeed.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mundane_conceptual_theft_walk"), -100, AttributeModifier.Operation.ADD_VALUE));
                    ServerScheduler.scheduleDelayed(20 * 20, () -> {
                        AttributeInstance movementSpeedInner = player.getAttribute(Attributes.MOVEMENT_SPEED);

                        if(movementSpeedInner != null) {
                            movementSpeedInner.removeModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mundane_conceptual_theft_walk"));
                        }
                    });
                }
                if (tag.contains("StolenEffects")) {
                    ListTag effectsList = tag.getList("StolenEffects", Tag.TAG_COMPOUND);
                    for (int i = 0; i < effectsList.size(); i++) {
                        MobEffectInstance effect = MobEffectInstance.load(effectsList.getCompound(i));
                        if (effect != null) {
                            player.addEffect(effect);
                        }
                    }
                }
                if (tag.contains("DisabledAbilities")) {
                    ListTag disabledAbilitiesList = tag.getList("StolenEffects", Tag.TAG_COMPOUND);
                    DisabledAbilitiesComponent disabledComponent = player.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
                    for (int i = 0; i < disabledAbilitiesList.size(); i++) {
                        disabledComponent.disableSpecificAbilityForTime(disabledAbilitiesList.getCompound(i).getString("AbilityName"), "theft_", 30 * 20);
                    }
                }
            } else if (specificInfo.type() == SummonType.SEQUENCE) {
                BeyonderData.setPathway(player, specificInfo.originalBeforeBorrowing().getString("pathway"));
                BeyonderData.setSequence(player, specificInfo.originalBeforeBorrowing().getInt("sequence"));
            }
            data.activeSummonTimes.remove(borrowTime);
        }
    }



    private void historicalVoidBorrowing(ServerPlayer player) {
        if (getHistoricalBorrowingCount(player) <= getMaxHistoricalBorrowingCount(player)) {
            PacketDistributor.sendToPlayer(
                    player,
                    new OpenHistoricalVoidBorrowingScreenPacket(List.of("Borrow Health", "Borrow Spirituality", "Borrow Cleansed State", "Borrow Sequence"))
            );
        }
    }

    public static void historicalVoidBorrowHealth(ServerPlayer player, ServerLevel level) {
        if (getHistoricalBorrowingCount(player) <= getMaxHistoricalBorrowingCount(player)) {
            if (player.getHealth() < player.getMaxHealth()) {
                // save current health
                long borrowTime = level.getGameTime() + getMaxHistoricalBorrowingDurationTicks(player);
                CompoundTag tag = new CompoundTag();
                tag.putFloat("health", player.getHealth());

                incrementHistoricalBorrowingCount(player, borrowTime, SummonType.HEALTH, player.getUUID(), tag);

                // set health to max
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    public static void historicalVoidBorrowSpirituality(ServerPlayer player, ServerLevel level) {
        if (getHistoricalBorrowingCount(player) <= getMaxHistoricalBorrowingCount(player)) {
            if (BeyonderData.getSpirituality(player) < BeyonderData.getMaxSpirituality(BeyonderData.getPathway(player), BeyonderData.getSequence(player))) {
                // save current spirituality
                long borrowTime = level.getGameTime() + getMaxHistoricalBorrowingDurationTicks(player);
                CompoundTag tag = new CompoundTag();
                tag.putFloat("spirituality", BeyonderData.getSpirituality(player));

                incrementHistoricalBorrowingCount(player, borrowTime, SummonType.SPIRITUALITY, player.getUUID(), tag);

                // set spirituality to max
                BeyonderData.setSpirituality(player, BeyonderData.getMaxSpirituality(BeyonderData.getPathway(player), BeyonderData.getSequence(player)));
            }
        }
    }

    public static void historicalVoidBorrowCleansedState(ServerPlayer player, ServerLevel level) {
        if (getHistoricalBorrowingCount(player) <= getMaxHistoricalBorrowingCount(player)) {
            long borrowTime = level.getGameTime() + getMaxHistoricalBorrowingDurationTicks(player);
            CompoundTag tag = new CompoundTag();

            AttributeInstance movementSpeedInner = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if(movementSpeedInner != null) {
                tag.putBoolean("WalkStolen", true);
            }

            ListTag effectsList = new ListTag();
            for (MobEffectInstance instance : new ArrayList<>(player.getActiveEffects())) {
                if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                    effectsList.add(instance.save());

                }
            }
            if(!effectsList.isEmpty()) {
                tag.put("StolenEffects", effectsList);
            }

            var component = player.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
            ListTag abilitiesList = new ListTag();

            for (DisabledAbilitiesComponent.DisabledAbility entry : component.getAllDisabledAbilities()) {
                CompoundTag abilityTag = new CompoundTag();
                abilityTag.putString("AbilityName", entry.ability());
                abilityTag.putInt("Amount", entry.amountDisabled());
                abilitiesList.add(abilityTag);
            }
            tag.put("DisabledAbilities", abilitiesList);

            incrementHistoricalBorrowingCount(player, borrowTime, SummonType.CLEANSED_STATE, player.getUUID(), tag);

            for (MobEffectInstance instance : new ArrayList<>(player.getActiveEffects())) {
                if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                    player.removeEffect(instance.getEffect());
                }
            }

            if(movementSpeedInner != null) {
                movementSpeedInner.removeModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mundane_conceptual_theft_walk"));
            }

            component.enableAllAbilities();
        }
    }

    public static void historicalVoidBorrowSequence(ServerPlayer player, ServerLevel level) {
        if (getHistoricalBorrowingCount(player) <= getMaxHistoricalBorrowingCount(player)) {
            SimpleContainer entityContainer = new SimpleContainer(54) {
                @Override
                public boolean canTakeItem(Container target, int index, ItemStack stack) {
                    return false; // Prevent taking items normally
                }
            };
            List<CompoundTag> markedEntities = getMarkedEntities(player);

            for(int i = 0; i < Math.min(markedEntities.size(), 53); i++) {
                CompoundTag entityData = markedEntities.get(i);
                ItemStack displayItem = createEntityDisplayItem(entityData);
                if (entityData.contains("EntityNBT")) {
                    CompoundTag entityNBT = entityData.getCompound("EntityNBT");
                    CompoundTag nfd = entityNBT.getCompound("neoforge:attachments").getCompound("lotmcraft:beyonder_component");

                    if (nfd.contains("pathway")) {

                        if (entityData.contains("OriginalPlayerUUID")) {
                            if (entityData.getUUID("OriginalPlayerUUID").equals(player.getUUID()) && nfd.getInt("sequence") > 0) {
                                boolean isMarionette = Optional.of(entityNBT.getCompound("neoforge:attachments").getCompound("lotmcraft:marionette_component")).map(c -> c.getBoolean("isMarionette")).orElse(false);
                                displayItem.set(
                                        DataComponents.LORE,
                                        new ItemLore(List.of(
                                                Component.literal("-------------------").withStyle(style -> style.withColor(0xFFa742f5).withItalic(false)),
                                                Component.translatable("lotm.pathway").append(Component.literal(": ")).append(Component.literal(BeyonderData.pathwayInfos.get(nfd.getString("pathway")).getSequenceName(9))).withColor(0xa26fc9).withStyle(style -> style.withItalic(false)),
                                                Component.translatable("lotm.sequence").append(Component.literal(": ")).append(Component.literal(nfd.getInt("sequence") + "")).withColor(0xa26fc9).withStyle(style -> style.withItalic(false)),
                                                Component.translatable("lotm.marionette").append(Component.literal(": ")).append(Component.literal(isMarionette + "")).withColor(0xa26fc9).withStyle(style -> style.withItalic(false))
                                        )));
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                }
                entityContainer.setItem(i + 1, displayItem);
            }

            final int finalContainerSize = entityContainer.getContainerSize();

            player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x6, id, inv, entityContainer, 6) {
                        @Override
                        public void clicked(int slotId, int button, ClickType clickType, Player clickPlayer) {
                            if(slotId >= 0 && slotId < finalContainerSize) {
                                ItemStack clickedItem = entityContainer.getItem(slotId);

                                if(clickedItem.isEmpty()) return;

                                CustomData customData = clickedItem.get(DataComponents.CUSTOM_DATA);

                                if(customData == null) return;

                                CompoundTag tag = customData.copyTag();

                                if(tag.contains("EntityData")) {
                                    CompoundTag entityData = tag.getCompound("EntityData");
                                    long borrowTime = level.getGameTime() + getMaxHistoricalBorrowingDurationTicks(player);
                                    CompoundTag anotherTag = new CompoundTag();
                                    anotherTag.putFloat("sequence", BeyonderData.getSequence(player));
                                    anotherTag.putString("pathway", BeyonderData.getPathway(player));

                                    incrementHistoricalBorrowingCount(player, borrowTime, SummonType.SEQUENCE, player.getUUID(), anotherTag);

                                    BeyonderData.setPathway(player, entityData.getCompound("EntityNBT").getCompound("neoforge:attachments").getCompound("lotmcraft:beyonder_component").getString("pathway"));
                                    BeyonderData.setSequence(player, entityData.getCompound("EntityNBT").getCompound("neoforge:attachments").getCompound("lotmcraft:beyonder_component").getInt("sequence"));
                                    player.closeContainer();
                                }
                            }
                        }
                    },
                    Component.literal("select your strongest marked version")
            ));
        }
    }


    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();
        CustomData Data = stack.get(DataComponents.CUSTOM_DATA);
        if (BeyonderData.getSequence(player) <= 2 || (BeyonderData.getSequence(player) <= 3 && BeyonderData.getPathway(player).equals("fool"))) {
            if (Data != null && Data.contains("VoidSummonTime")) {
                event.getToolTip().add(Component.literal("§7[Void Summoned]§r").withStyle(ChatFormatting.GRAY));
            }
        }

    }

    @SubscribeEvent
    public static void onItemTickInPlayerInventory(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (player.tickCount % 600 != 0) return;
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) return;

        HistoricalVoidComponent data = serverPlayer.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
        for (HistoricalVoidComponent.SummonInfo info : data.activeSummonTimes.values()) {
            if (info.type() == SummonType.HEALTH ||
                    info.type() == SummonType.SPIRITUALITY ||
                    info.type() == SummonType.CLEANSED_STATE||
                    info.type() == SummonType.SEQUENCE) {

                if (serverLevel.getGameTime() > info.summonTime()) {
                    decrementHistoricalBorrowingCount(serverPlayer, info.summonTime());
                }
            }
        }


        for (int i = 0; i < serverPlayer.getInventory().getContainerSize(); i++) {
            ItemStack stack = serverPlayer.getInventory().getItem(i);

            if (!stack.isEmpty()) {
                CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                if(customData != null) {
                    CompoundTag tag = customData.copyTag();
                    if (tag.contains("VoidSummonTime") && tag.contains("VoidSummonOwner")) {
                        if (tag.getLong("VoidSummonTime") < serverLevel.getGameTime()) {
                            player.getInventory().removeItem(i, stack.getCount());
                            decrementSummonedCount(serverPlayer, tag.getLong("VoidSummonTime"));
                            if(player.isAlive()) {
                                player.sendSystemMessage(Component.translatable("ability.lotmcraft.historical_void_summoning.item_returned").withStyle(ChatFormatting.GRAY));
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onSummonedEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        Level level = entity.level();

        if (entity.tickCount % 600 != 0) return;
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) return;

        if(entity.getPersistentData().getBoolean("VoidSummoned")) {
            if (entity.getPersistentData().getLong("VoidSummonTime") < serverLevel.getGameTime()) {
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
            if (serverLevel.getPlayerByUUID(entity.getPersistentData().getUUID("VoidSummonOwner")) instanceof ServerPlayer serverPlayer) {
                decrementSummonedCount(serverPlayer, entity.getPersistentData().getLong("VoidSummonTime"));
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack heldItem = player.getMainHandItem();

        if(heldItem.isEmpty()) {
            heldItem = player.getOffhandItem();
        }

        if(!heldItem.isEmpty()) {
            CustomData customData = heldItem.get(DataComponents.CUSTOM_DATA);
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

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if(event.getPlayer().level().isClientSide) return;

        ItemStack tossedItem = event.getEntity().getItem();

        CustomData customData = tossedItem.get(DataComponents.CUSTOM_DATA);
        if(customData != null) {
            CompoundTag tag = customData.copyTag();
            if(tag.contains("VoidSummonTime") && tag.contains("VoidSummonOwner")) {
                // This is a summoned item being tossed - make it disappear
                // only make it disappear if the player is crouching, good qol to force items to disappear
                if (event.getPlayer().isCrouching()) {
                    long summonTime = tag.getLong("VoidSummonTime");

                    removeTemporaryItem((ServerLevel) event.getPlayer().level(), (ServerPlayer) event.getPlayer(), summonTime);
                    event.getEntity().discard();
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;

        returnAllSummoned(player);

        player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).reset();
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;

        ServerLevel level = (ServerLevel) player.level();
        UUID playerUUID = player.getUUID();

        // Remove all summoned entities
        for(Entity entity : level.getAllEntities()) {
            if(entity.getPersistentData().getBoolean("VoidSummoned") &&
                    entity.getPersistentData().getUUID("VoidSummonOwner").equals(playerUUID)) {
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    private static int getMaxSummoned(ServerPlayer serverPlayer){
        return getMaxSummonedForSequence(BeyonderData.getSequence(serverPlayer));
    }

    public static int getMaxSummonedForSequence(int sequence){
        return switch (sequence){
            case 0 -> 100;
            case 1 -> 40;
            case 2 -> 12;
            default -> 5;
        };
    }

    private static int getSummonDurationTicks(ServerPlayer serverPlayer){
        return switch (BeyonderData.getSequence(serverPlayer)){
            case 0 -> 60 * 60 * 20;
            case 1 -> 10 * 60 * 20;
            case 2 -> 4 * 60 * 20;
            default -> 60 * 20;
        };
    }

    private static int getMaxHistoricalBorrowingCount(ServerPlayer serverPlayer){
        return switch (BeyonderData.getSequence(serverPlayer)){
            case 0 -> 50;
            case 1 -> 20;
            case 2 -> 10;
            default -> 5;
        };
    }

    private static int getMaxHistoricalBorrowingDurationTicks(ServerPlayer serverPlayer){
        return switch (BeyonderData.getSequence(serverPlayer)){
            case 0 -> 60 * 60 * 20;
            case 1 -> 10 * 60 * 20;
            case 2 -> 4 * 60 * 20;
            default -> 60 * 20;
        };
    }
}