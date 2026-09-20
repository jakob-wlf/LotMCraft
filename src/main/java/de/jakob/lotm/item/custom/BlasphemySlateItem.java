package de.jakob.lotm.item.custom;

import de.jakob.lotm.attachments.BlasphemySlateData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.UUID;

/**
 * The assembled Blasphemy Slate — right-clicking it starts the Chaos Sea
 * accommodation ritual (handled in ChaosSeaEventHandler).
 * Only one slate can exist in the world at a time.
 */
public class BlasphemySlateItem extends Item {

    public static final String KEY_SLATE_ID = "BlasphemySlateId";

    public BlasphemySlateItem(Properties properties) {
        super(properties);
    }

    // ─── UUID helpers ─────────────────────────────────────────────────────────

    public static UUID getSlateId(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        CompoundTag tag = data.copyTag();
        return tag.hasUUID(KEY_SLATE_ID) ? tag.getUUID(KEY_SLATE_ID) : null;
    }

    public static void setSlateId(ItemStack stack, UUID id) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putUUID(KEY_SLATE_ID, id);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Validates uniqueness for this slate stack.
     * Returns false if the item must be destroyed.
     */
    public boolean syncSlate(ServerLevel level, ItemStack stack) {
        BlasphemySlateData data = BlasphemySlateData.get(level.getServer());
        long tick = level.getGameTime();

        // If the chaos sea has been claimed the slate can no longer exist
        if (de.jakob.lotm.attachments.SefirotData.get(level.getServer()).isSefirotClaimed("chaos_sea")) return false;

        UUID stackId = getSlateId(stack);
        if (stackId == null) {
            if (data.canSpawnSlate()) {
                stackId = UUID.randomUUID();
                setSlateId(stack, stackId);
                data.markSlateExists(stackId, tick);
                return true;
            }
            return false;
        }

        if (!data.isSlateOwner(stackId)) return false;
        data.markSlateSeen(stackId, tick);
        return true;
    }
}
