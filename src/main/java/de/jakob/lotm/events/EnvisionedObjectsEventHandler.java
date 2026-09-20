package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.EnvisionedObjectsData;
import de.jakob.lotm.beyonders.sefirah.GreatOldOneManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class EnvisionedObjectsEventHandler {
    public static final String ENVISIONED_EXPIRY = "LotmEnvisionedExpiryMs";
    public static final String ENVISIONED_OWNER = "LotmEnvisionedOwner";
    private static final long LIFETIME_MS = 15L * 60L * 1000L;
    private static final Map<UUID, PendingPlacement> pendingPlacements = new HashMap<>();

    private EnvisionedObjectsEventHandler() {
    }

    public static ItemStack markEnvisioned(ItemStack source, ServerPlayer creator) {
        ItemStack stack = source.copy();
        if (stack.isEmpty() || GreatOldOneManager.isGreatOldOne(creator)) return stack;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData == null ? new CompoundTag() : customData.copyTag();
        tag.putLong(ENVISIONED_EXPIRY, System.currentTimeMillis() + LIFETIME_MS);
        tag.putUUID(ENVISIONED_OWNER, creator.getUUID());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static long getExpiry(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return 0L;
        CompoundTag tag = customData.copyTag();
        return tag.contains(ENVISIONED_EXPIRY) ? tag.getLong(ENVISIONED_EXPIRY) : 0L;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) return;

        long now = System.currentTimeMillis();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isExpired(stack, now)) player.getInventory().setItem(slot, ItemStack.EMPTY);
        }
        for (int slot = 0; slot < player.getEnderChestInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getEnderChestInventory().getItem(slot);
            if (isExpired(stack, now)) player.getEnderChestInventory().setItem(slot, ItemStack.EMPTY);
        }
        if (isExpired(player.containerMenu.getCarried(), now)) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
        }
        for (var slot : player.containerMenu.slots) {
            if (isExpired(slot.getItem(), now)) slot.set(ItemStack.EMPTY);
        }
        player.containerMenu.broadcastChanges();
    }

    @SubscribeEvent
    public static void onItemEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)
                || itemEntity.level().isClientSide
                || itemEntity.tickCount % 20 != 0) return;

        if (isExpired(itemEntity.getItem(), System.currentTimeMillis())) {
            itemEntity.discard();
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack stack = event.getItemStack();
        long expiry = stack.getItem() instanceof BlockItem ? getExpiry(stack) : 0L;
        if (expiry == 0L) {
            pendingPlacements.remove(player.getUUID());
            return;
        }
        pendingPlacements.put(player.getUUID(),
                new PendingPlacement(player.level().getGameTime(), expiry));
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(event.getLevel() instanceof ServerLevel level)) return;

        ItemStack stack = player.getMainHandItem();
        long expiry = getExpiry(stack);
        if (expiry == 0L) {
            stack = player.getOffhandItem();
            expiry = getExpiry(stack);
        }
        PendingPlacement pending = pendingPlacements.get(player.getUUID());
        if (expiry == 0L && pending != null
                && pending.gameTime() == level.getGameTime()) {
            expiry = pending.expiryMs();
        }
        if (expiry == 0L) return;

        String dimension = level.dimension().location().toString();
        String blockId = BuiltInRegistries.BLOCK.getKey(event.getPlacedBlock().getBlock()).toString();
        EnvisionedObjectsData.get(level.getServer()).put(
                dimension, event.getPos().asLong(), expiry, blockId);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        String dimension = level.dimension().location().toString();
        if (EnvisionedObjectsData.get(level.getServer()).remove(dimension, event.getPos().asLong()) == null) return;

        event.setCanceled(true);
        level.removeBlock(event.getPos(), false);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % 20 != 0) return;

        EnvisionedObjectsData data = EnvisionedObjectsData.get(level.getServer());
        String dimension = level.dimension().location().toString();
        long now = System.currentTimeMillis();
        List<EnvisionedObjectsData.PlacedObject> expired = new ArrayList<>();

        for (EnvisionedObjectsData.PlacedObject object : data.getPlacedObjects().values()) {
            if (!object.dimension().equals(dimension) || object.expiryMs() > now) continue;
            BlockPos pos = BlockPos.of(object.position());
            if (!level.hasChunkAt(pos)) continue;

            String currentBlockId = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString();
            if (currentBlockId.equals(object.blockId())) level.removeBlock(pos, false);
            expired.add(object);
        }

        for (EnvisionedObjectsData.PlacedObject object : expired) {
            data.remove(object.dimension(), object.position());
        }
    }

    private static boolean isExpired(ItemStack stack, long now) {
        long expiry = getExpiry(stack);
        return expiry > 0L && expiry <= now;
    }

    private record PendingPlacement(long gameTime, long expiryMs) {
    }
}