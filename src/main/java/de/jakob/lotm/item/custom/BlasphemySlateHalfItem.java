package de.jakob.lotm.item.custom;

import de.jakob.lotm.attachments.BlasphemySlateData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * One half of the Blasphemy Slate (left or right).
 * Only one of each half can exist in the world at a time.
 * When a half exists the pathway cards that were used to craft it cannot exist.
 */
public class BlasphemySlateHalfItem extends Item {

    public static final String KEY_HALF_ID = "BlasphemyHalfId";

    public enum HalfType { LEFT, RIGHT }

    private final HalfType halfType;

    public BlasphemySlateHalfItem(Properties properties, HalfType halfType) {
        super(properties);
        this.halfType = halfType;
    }

    public HalfType getHalfType() {
        return halfType;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // Screen opening is handled client-side in KeyInputHandler to avoid
        // referencing Minecraft (client-only class) in this shared class.
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
    // ─── UUID helpers ─────────────────────────────────────────────────────────

    public static UUID getHalfId(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        CompoundTag tag = data.copyTag();
        return tag.hasUUID(KEY_HALF_ID) ? tag.getUUID(KEY_HALF_ID) : null;
    }

    public static void setHalfId(ItemStack stack, UUID id) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putUUID(KEY_HALF_ID, id);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Validates uniqueness for this half-slate stack.
     * Returns false if the item must be destroyed.
     */
    public boolean syncHalf(ServerLevel level, ItemStack stack) {
        BlasphemySlateData data = BlasphemySlateData.get(level.getServer());
        long tick = level.getGameTime();

        // If the full slate now exists, halves must not exist (consumed to craft it)
        if (data.hasSlate()) return false;
        // If the chaos sea has been claimed, halves can no longer exist
        if (de.jakob.lotm.attachments.SefirotData.get(level.getServer()).isSefirotClaimed("chaos_sea")) return false;

        UUID stackId = getHalfId(stack);
        if (stackId == null) {
            boolean canSpawn = halfType == HalfType.LEFT
                    ? data.canSpawnLeftHalf()
                    : data.canSpawnRightHalf();
            if (canSpawn) {
                stackId = UUID.randomUUID();
                setHalfId(stack, stackId);
                if (halfType == HalfType.LEFT) {
                    data.markLeftHalfExists(stackId, tick);
                } else {
                    data.markRightHalfExists(stackId, tick);
                }
                return true;
            }
            return false;
        }

        if (halfType == HalfType.LEFT) {
            if (!data.isLeftHalfOwner(stackId)) return false;
            data.markLeftHalfSeen(stackId, tick);
        } else {
            if (!data.isRightHalfOwner(stackId)) return false;
            data.markRightHalfSeen(stackId, tick);
        }
        return true;
    }
}
