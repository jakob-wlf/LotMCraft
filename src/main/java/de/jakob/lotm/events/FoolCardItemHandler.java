package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.FoolCardData;
import de.jakob.lotm.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks the global Fool Card item and enforces the 48 h cooldown when it is destroyed.
 * The fool_card gains a UUID stored in CustomData so it can be uniquely identified worldwide.
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class FoolCardItemHandler {

    public static final String KEY_FOOL_CARD_UUID = "FoolCardUUID";

    // ─── UUID helpers ─────────────────────────────────────────────────────────

    public static UUID getCardId(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        CompoundTag tag = data.copyTag();
        return tag.hasUUID(KEY_FOOL_CARD_UUID) ? tag.getUUID(KEY_FOOL_CARD_UUID) : null;
    }

    public static void setCardId(ItemStack stack, UUID id) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putUUID(KEY_FOOL_CARD_UUID, id);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    // ─── Server tick — cleanup ────────────────────────────────────────────────

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.overworld();
        long time = overworld.getGameTime();

        // Only check every 20 ticks
        if (time % 20 != 0) return;

        FoolCardData data = FoolCardData.get(server);
        if (!data.hasCard()) return;

        UUID id = data.getCardId();

        // If last seen recently: scan to refresh last-seen tick
        if (isFoolCardPresent(server, id)) {
            data.markSeen(id, time);
            return;
        }

        // Card was not found this tick — check if enough unseen ticks have passed
        if (time - data.getLastSeen() > FoolCardData.UNSEEN_TICKS) {
            data.markDestroyed();
            LOTMCraft.LOGGER.info("[FoolCardItemHandler] Fool Card destroyed — 48 h cooldown started.");
        }
    }

    // ─── Presence scan ────────────────────────────────────────────────────────

    /**
     * Returns true if the tracked fool card (with the given UUID) exists somewhere in the world:
     *  1. In any online player's inventory or cursor
     *  2. As a dropped ItemEntity in any loaded level
     *  3. In any loaded container block entity (chest, barrel, shulker box, etc.)
     */
    public static boolean isFoolCardPresent(MinecraftServer server, UUID id) {
        if (id == null) return false;

        // 1. Online player inventories + cursor
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
                ItemStack s = p.getInventory().getItem(i);
                if (matchesFoolCard(s, id)) return true;
            }
            ItemStack carried = p.containerMenu.getCarried();
            if (matchesFoolCard(carried, id)) return true;
        }

        // 2. World item entities + container block entities in all loaded levels
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity e : level.getAllEntities()) {
                if (e instanceof ItemEntity ie && matchesFoolCard(ie.getItem(), id)) return true;
            }
            Set<LevelChunk> checked = new HashSet<>();
            for (ServerPlayer p : level.players()) {
                ChunkPos center = new ChunkPos(p.blockPosition());
                for (int dx = -4; dx <= 4; dx++) {
                    for (int dz = -4; dz <= 4; dz++) {
                        LevelChunk chunk = level.getChunkSource().getChunkNow(center.x + dx, center.z + dz);
                        if (chunk == null || !checked.add(chunk)) continue;
                        for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                            BlockEntity be = entry.getValue();
                            if (be instanceof Container container) {
                                for (int i = 0; i < container.getContainerSize(); i++) {
                                    if (matchesFoolCard(container.getItem(i), id)) return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private static boolean matchesFoolCard(ItemStack stack, UUID id) {
        return !stack.isEmpty()
                && stack.getItem() == ModItems.FOOL_Card.get()
                && id.equals(getCardId(stack));
    }
}
