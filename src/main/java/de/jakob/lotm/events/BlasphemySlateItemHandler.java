package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.BlasphemySlateData;
import de.jakob.lotm.attachments.SummonedBlasphemyData;
import de.jakob.lotm.item.custom.BlasphemyCardItem;
import de.jakob.lotm.item.custom.BlasphemySlateHalfItem;
import de.jakob.lotm.item.custom.BlasphemySlateItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSummonedBlasphemyPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles:
 *  1. Uniqueness enforcement for BlasphemyCardItem, BlasphemySlateHalfItem, BlasphemySlateItem.
 *  2. Right-click on a BlasphemyCardItem → opens the recipe-viewer chest GUI.
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class 
BlasphemySlateItemHandler {

    private static final int PLAYER_CHECK_INTERVAL = 1;
    private static final int CLEANUP_INTERVAL      = 20;
    private static final long UNSEEN_TICKS         = 40L;

    public static HashMap<String, Boolean> BlashphemyMap;

    // ─── Tick events (uniqueness sync) ───────────────────────────────────────

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();

        if (entity instanceof ItemEntity itemEntity) {
            if (itemEntity.level().isClientSide) return;
            if (!(itemEntity.level() instanceof ServerLevel level)) return;
            if (!syncStack(level, itemEntity.getItem())) {
                itemEntity.discard();
            }
            return;
        }

        if (!(entity instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        ServerLevel level = (ServerLevel) player.level();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!syncStack(level, stack)) {
                player.getInventory().removeItem(i, 1);
            }
        }

        // Also sync the cursor (carried) item so markCardSeen stays fresh while dragging,
        // and so duplicate/invalid cards on the cursor are cleared.
        ItemStack cursorStack = player.containerMenu.getCarried();
        if (!cursorStack.isEmpty()) {
            if (!syncStack(level, cursorStack)) {
                player.containerMenu.setCarried(ItemStack.EMPTY);
                // Tell the client its cursor is now empty
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(
                        -1, player.containerMenu.getStateId(), -1, ItemStack.EMPTY));
            }
        }

        // If the player has an unauthorized container open, return any non-envisioned Blasphemy Cards
        // from its slots back to the player's inventory (or drop them if inventory is full).
        AbstractContainerMenu menu = player.containerMenu;
        boolean isAllowedMenu = menu instanceof InventoryMenu
                || menu instanceof de.jakob.lotm.gui.custom.BrewingCauldron.BrewingCauldronMenu
                || menu instanceof net.minecraft.world.inventory.CraftingMenu;
        if (!isAllowedMenu) {
            for (Slot slot : new java.util.ArrayList<>(menu.slots)) {
                // Only scan slots that belong to a block-entity container, not the player's own inventory
                if (slot.container instanceof Inventory) continue;
                ItemStack s = slot.getItem();
                if (s.isEmpty()) continue;
                if (!(s.getItem() instanceof BlasphemyCardItem)) continue;
                if (isEnvisionSummoned(s)) continue;
                // Remove from the container and return to the player
                ItemStack toReturn = s.copy();
                slot.set(ItemStack.EMPTY);
                if (slot.container instanceof Container c) c.setChanged();
                if (!player.getInventory().add(toReturn)) {
                    level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), toReturn));
                }
            }
        }

        // Sync envision-summoned card data: remove any tracked pathways
        // whose tagged card is no longer in this player's inventory, on cursor, or in a cauldron.
        SummonedBlasphemyData sbd = SummonedBlasphemyData.get(player.getServer());
        if (sbd.activeCount(player.getUUID()) > 0) {
            boolean changed = false;
            for (String pathway : new java.util.ArrayList<>(sbd.getCards(player.getUUID()).keySet())) {
                boolean found = false;
                // 1. Check personal inventory
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack s = player.getInventory().getItem(i);
                    if (!s.isEmpty()
                            && s.getItem() instanceof BlasphemyCardItem card
                            && card.getPathway().equals(pathway)
                            && isEnvisionSummoned(s)) {
                        found = true;
                        break;
                    }
                }
                // 2. Check cursor (item held while dragging in a container GUI)
                if (!found) {
                    ItemStack carried = player.containerMenu.getCarried();
                    if (!carried.isEmpty()
                            && carried.getItem() instanceof BlasphemyCardItem card
                            && card.getPathway().equals(pathway)
                            && isEnvisionSummoned(carried)) {
                        found = true;
                    }
                }
                // 3. Check open container slots (cauldron recipe slot, etc.)
                if (!found && player.containerMenu instanceof de.jakob.lotm.gui.custom.BrewingCauldron.BrewingCauldronMenu cauldronMenu
                        && cauldronMenu.blockEntity != null) {
                    ItemStack s = cauldronMenu.blockEntity.itemHandler.getStackInSlot(4);
                    if (!s.isEmpty() && s.getItem() instanceof BlasphemyCardItem card
                            && card.getPathway().equals(pathway)
                            && isEnvisionSummoned(s)) {
                        found = true;
                    }
                }
                if (!found) {
                    // Also check any loaded cauldron instances (fallback)
                    for (de.jakob.lotm.block.custom.BrewingCauldronBlockEntity cauldron :
                            new java.util.ArrayList<>(de.jakob.lotm.block.custom.BrewingCauldronBlockEntity.INSTANCES)) {
                        ItemStack s = cauldron.itemHandler.getStackInSlot(4);
                        if (!s.isEmpty() && s.getItem() instanceof BlasphemyCardItem c
                                && c.getPathway().equals(pathway)
                                && isEnvisionSummoned(s)) {
                            found = true;
                            break;
                        }
                    }
                }
                // 4. Check world ItemEntities (envisioned card was thrown/dropped out of inventory)
                if (!found) {
                    levelScan:
                    for (ServerLevel lvl : player.getServer().getAllLevels()) {
                        for (Entity e : lvl.getAllEntities()) {
                            if (e instanceof ItemEntity ie) {
                                ItemStack s = ie.getItem();
                                if (!s.isEmpty()
                                        && s.getItem() instanceof BlasphemyCardItem c
                                        && c.getPathway().equals(pathway)
                                        && isEnvisionSummoned(s)) {
                                    found = true;
                                    break levelScan;
                                }
                            }
                        }
                    }
                }
                if (!found) {
                    sbd.forceDismiss(player.getUUID(), pathway);
                    changed = true;
                }
            }
            if (changed) {
                PacketHandler.sendToPlayer(player,
                        new SyncSummonedBlasphemyPacket(sbd.getCards(player.getUUID()), sbd.getLockedCards(player.getUUID()), sbd.getCooldownExpiryMap(player.getUUID())));
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        long time = overworld.getGameTime();

        if (time % CLEANUP_INTERVAL != 0) return;

        BlasphemySlateData data = BlasphemySlateData.get(event.getServer());

        // Cards
        for (String pathway : List.of("fool","door","error","sun","tyrant","visionary",
                "darkness","death","twilight_giant","demoness","red_priest","mother",
                "abyss","wheel_of_fortune","black_emperor","justiciar")) {
            if (data.hasCard(pathway)
                    && time - data.getCardLastSeen(pathway) > UNSEEN_TICKS
                    && !isCardPresent(event.getServer(), pathway, data.getCardId(pathway))) {
                data.clearCard(pathway);
            }
        }

        // Left half
        if (data.hasLeftHalf()
                && time - data.getLeftHalfLastSeen() > UNSEEN_TICKS
                && !isLeftHalfPresent(event.getServer(), data.getLeftHalfId())) {
            data.clearLeftHalf();
        }

        // Right half
        if (data.hasRightHalf()
                && time - data.getRightHalfLastSeen() > UNSEEN_TICKS
                && !isRightHalfPresent(event.getServer(), data.getRightHalfId())) {
            data.clearRightHalf();
        }

        // Slate
        if (data.hasSlate()
                && time - data.getSlateLastSeen() > UNSEEN_TICKS
                && !isSlatePresent(event.getServer(), data.getSlateId())) {
            data.clearSlate();
        }

        // Keep markCardSeen fresh for real Blasphemy Cards sitting in cauldron recipe slots.
        // We deliberately do NOT call syncStack here — syncCard would delete cards whose pathway
        // is restricted (e.g. Chaos Sea owner with left/right-half cards). The isCardPresent()
        // check above already prevents clearCard() from firing while the card is in the cauldron.
        for (de.jakob.lotm.block.custom.BrewingCauldronBlockEntity cauldron :
                new java.util.ArrayList<>(de.jakob.lotm.block.custom.BrewingCauldronBlockEntity.INSTANCES)) {
            if (cauldron.isRemoved()) continue;
            ItemStack recipeStack = cauldron.itemHandler.getStackInSlot(4);
            if (recipeStack.isEmpty()) continue;
            if (!(recipeStack.getItem() instanceof BlasphemyCardItem bc)) continue;
            if (isEnvisionSummoned(recipeStack)) continue; // envisioned cards are tracked separately
            UUID cardId = BlasphemyCardItem.getCardId(recipeStack);
            if (cardId != null && cardId.equals(data.getCardId(bc.getPathway()))) {
                data.markCardSeen(bc.getPathway(), cardId, time);
            }
        }

        // Eject any non-envisioned Blasphemy Cards from unauthorized block-entity containers.
        // Allowed containers: BrewingCauldronBlockEntity and anything from the "corpse" mod.
        // We only eject when no player currently has that container open, to avoid client desync.
        for (ServerLevel lvl : event.getServer().getAllLevels()) {
            for (LevelChunk chunk : getLoadedChunks(lvl)) {
                for (BlockEntity be : new java.util.ArrayList<>(chunk.getBlockEntities().values())) {
                    if (be instanceof de.jakob.lotm.block.custom.BrewingCauldronBlockEntity) continue;
                    if (isCorpseModBlockEntity(be)) continue;
                    if (!(be instanceof Container container)) continue;
                    // Skip if any player currently has this container open
                    boolean viewed = event.getServer().getPlayerList().getPlayers().stream()
                            .anyMatch(p -> isMenuLinkedTo(p.containerMenu, container));
                    if (viewed) continue;
                    BlockPos pos = be.getBlockPos();
                    boolean dirty = false;
                    for (int i = 0; i < container.getContainerSize(); i++) {
                        ItemStack s = container.getItem(i);
                        if (s.isEmpty()) continue;
                        if (!(s.getItem() instanceof BlasphemyCardItem)) continue;
                        if (isEnvisionSummoned(s)) continue;
                        container.setItem(i, ItemStack.EMPTY);
                        dirty = true;
                        lvl.addFreshEntity(new ItemEntity(lvl,
                                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, s.copy()));
                    }
                    if (dirty) be.setChanged();
                }
            }
        }
    }

    /** Returns all currently loaded chunks in a level. */
    private static java.util.List<LevelChunk> getLoadedChunks(ServerLevel level) {
        java.util.List<LevelChunk> result = new java.util.ArrayList<>();
        java.util.Set<net.minecraft.world.level.ChunkPos> positions = new java.util.HashSet<>();
        for (ServerPlayer p : level.players()) {
            net.minecraft.world.level.ChunkPos center = new net.minecraft.world.level.ChunkPos(p.blockPosition());
            for (int dx = -8; dx <= 8; dx++)
                for (int dz = -8; dz <= 8; dz++)
                    positions.add(new net.minecraft.world.level.ChunkPos(center.x + dx, center.z + dz));
        }
        for (net.minecraft.world.level.ChunkPos pos : positions) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
            if (chunk != null) result.add(chunk);
        }
        return result;
    }

    /** Returns true when the block entity is part of the Corpse mod (by maxhenkel). */
    private static boolean isCorpseModBlockEntity(BlockEntity be) {
        ResourceLocation key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType());
        return key != null && "corpse".equals(key.getNamespace());
    }

    /** Returns true when any slot in the given menu belongs to the supplied container. */
    private static boolean isMenuLinkedTo(AbstractContainerMenu menu, Container container) {
        for (Slot slot : menu.slots) {
            if (slot.container == container) return true;
        }
        return false;
    }

    // ─── Uniqueness scan helpers ──────────────────────────────────────────────

    private static boolean isCardPresent(MinecraftServer server, String pathway, UUID id) {
        if (id == null) return false;
        // Online players (inventory + cursor/carried item)
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
                ItemStack s = p.getInventory().getItem(i);
                if (s.getItem() instanceof BlasphemyCardItem c && c.getPathway().equals(pathway)
                        && id.equals(BlasphemyCardItem.getCardId(s))) return true;
            }
            // Also check the cursor (item held mid-drag in a container GUI)
            ItemStack carried = p.containerMenu.getCarried();
            if (!carried.isEmpty() && carried.getItem() instanceof BlasphemyCardItem c
                    && c.getPathway().equals(pathway) && id.equals(BlasphemyCardItem.getCardId(carried))) return true;
        }
        // Brewing Cauldron recipe slots (slot index 4)
        for (de.jakob.lotm.block.custom.BrewingCauldronBlockEntity cauldron :
                new java.util.ArrayList<>(de.jakob.lotm.block.custom.BrewingCauldronBlockEntity.INSTANCES)) {
            ItemStack s = cauldron.itemHandler.getStackInSlot(4);
            if (s.getItem() instanceof BlasphemyCardItem c && c.getPathway().equals(pathway)
                    && id.equals(BlasphemyCardItem.getCardId(s))) return true;
        }
        // World ItemEntities (card was thrown/dropped and is lying in a loaded chunk)
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity e : level.getAllEntities()) {
                if (e instanceof ItemEntity ie) {
                    ItemStack s = ie.getItem();
                    if (s.getItem() instanceof BlasphemyCardItem c && c.getPathway().equals(pathway)
                            && id.equals(BlasphemyCardItem.getCardId(s))) return true;
                }
            }
        }
        // Offline players
        return offlineNbtContains(server, LOTMCraft.MOD_ID + ":" + pathway + "_blasphemy_card");
    }

    private static boolean isLeftHalfPresent(MinecraftServer server, UUID id) {
        if (id == null) return false;
        // Online players
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
                ItemStack s = p.getInventory().getItem(i);
                if (s.getItem() instanceof BlasphemySlateHalfItem h
                        && h.getHalfType() == BlasphemySlateHalfItem.HalfType.LEFT
                        && id.equals(BlasphemySlateHalfItem.getHalfId(s))) return true;
            }
        }
        // Offline players
        return offlineNbtContains(server, LOTMCraft.MOD_ID + ":blasphemy_slate_left_half");
    }

    private static boolean isRightHalfPresent(MinecraftServer server, UUID id) {
        if (id == null) return false;
        // Online players
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
                ItemStack s = p.getInventory().getItem(i);
                if (s.getItem() instanceof BlasphemySlateHalfItem h
                        && h.getHalfType() == BlasphemySlateHalfItem.HalfType.RIGHT
                        && id.equals(BlasphemySlateHalfItem.getHalfId(s))) return true;
            }
        }
        // Offline players
        return offlineNbtContains(server, LOTMCraft.MOD_ID + ":blasphemy_slate_right_half");
    }

    private static boolean isSlatePresent(MinecraftServer server, UUID id) {
        if (id == null) return false;
        // Online players
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
                ItemStack s = p.getInventory().getItem(i);
                if (s.getItem() instanceof BlasphemySlateItem
                        && id.equals(BlasphemySlateItem.getSlateId(s))) return true;
            }
        }
        // Offline players
        return offlineNbtContains(server, LOTMCraft.MOD_ID + ":blasphemy_slate");
    }

    // ─── Offline player data helpers ──────────────────────────────────────────

    private static boolean offlineNbtContains(MinecraftServer server, String itemId) {
        Path dir = server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
        try (Stream<Path> stream = Files.list(dir)) {
            List<UUID> uuids = stream
                    .filter(p -> p.getFileName().toString().endsWith(".dat"))
                    .map(p -> {
                        String name = p.getFileName().toString();
                        try { return UUID.fromString(name.substring(0, name.length() - 4)); }
                        catch (IllegalArgumentException e) { return null; }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            for (UUID uuid : uuids) {
                if(BlashphemyMap == null) BlashphemyMap = new HashMap<String, Boolean>();
                if(BlashphemyMap.get(itemId)) return true;
                if (server.getPlayerList().getPlayer(uuid) != null) continue; // online — already checked
                CompoundTag root = readPlayerNbt(server, uuid);
                if (root != null && nbtInventoryContains(root, itemId)){
                    BlashphemyMap.put(itemId, true);
                    return true;
                }
            }
        } catch (IOException e) {
            LOTMCraft.LOGGER.warn("BlasphemySlateItemHandler: failed to list playerdata", e);
        }
        return false;
    }

    private static CompoundTag readPlayerNbt(MinecraftServer server, UUID uuid) {
        Path file = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(uuid + ".dat");
        if (!Files.exists(file)) return null;
        try { return NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap()); }
        catch (IOException e) { LOTMCraft.LOGGER.warn("Failed to read playerdata for {}", uuid, e); return null; }
    }

    private static boolean nbtInventoryContains(CompoundTag root, String itemId) {
        ListTag inv = root.getList("Inventory", Tag.TAG_COMPOUND);
        for (int i = 0; i < inv.size(); i++) {
            if (itemId.equals(inv.getCompound(i).getString("id"))) return true;
        }
        return false;
    }

    // ─── syncStack dispatcher ─────────────────────────────────────────────────

    /** NBT key written onto cards summoned via the Envisioning ability. */
    public static final String ENVISION_SUMMONED_KEY = "EnvisionSummoned";

    /** Returns true if this stack was summoned through Envisioning (bypasses uniqueness). */
    public static boolean isEnvisionSummoned(ItemStack stack) {
        net.minecraft.world.item.component.CustomData data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBoolean(ENVISION_SUMMONED_KEY);
    }

    public static final String ENVISION_USES_KEY = "EnvisionedUses";
    public static final int ENVISION_MAX_USES = 5;

    /** Tag {@code stack} as Envisioning-summoned so uniqueness checks are skipped. Also sets the use counter. */
    public static void markEnvisionSummoned(ItemStack stack) {
        net.minecraft.nbt.CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        tag.putBoolean(ENVISION_SUMMONED_KEY, true);
        tag.putInt(ENVISION_USES_KEY, ENVISION_MAX_USES);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
    }

    /** Returns the remaining uses on an envisioned card, or -1 if not an envisioned card. */
    public static int getEnvisionedUses(ItemStack stack) {
        net.minecraft.world.item.component.CustomData data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (data == null) return -1;
        net.minecraft.nbt.CompoundTag tag = data.copyTag();
        if (!tag.getBoolean(ENVISION_SUMMONED_KEY)) return -1;
        return tag.contains(ENVISION_USES_KEY) ? tag.getInt(ENVISION_USES_KEY) : ENVISION_MAX_USES;
    }

    /** Decrements the use counter on an envisioned card in-place. Returns the new count. */
    public static int decrementEnvisionedUses(ItemStack stack) {
        net.minecraft.nbt.CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        int current = tag.contains(ENVISION_USES_KEY) ? tag.getInt(ENVISION_USES_KEY) : ENVISION_MAX_USES;
        int next = Math.max(0, current - 1);
        tag.putInt(ENVISION_USES_KEY, next);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        return next;
    }

    private static boolean syncStack(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) return true;
        if (stack.getItem() instanceof BlasphemyCardItem card) {
            // Cards summoned via Envisioning bypass the world-uniqueness system
            if (isEnvisionSummoned(stack)) return true;
            return card.syncCard(level, stack);
        }
        if (stack.getItem() instanceof BlasphemySlateHalfItem half) {
            return half.syncHalf(level, stack);
        }
        if (stack.getItem() instanceof BlasphemySlateItem slate) {
            return slate.syncSlate(level, stack);
        }
        return true;
    }
}
