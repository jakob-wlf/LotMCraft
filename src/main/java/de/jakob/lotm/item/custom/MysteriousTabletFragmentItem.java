package de.jakob.lotm.item.custom;

import de.jakob.lotm.attachments.MysteriousTabletData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.UUID;

public class MysteriousTabletFragmentItem extends Item {

    public static final String KEY_FRAGMENT_ID = "FragmentId";
    private static final String KEY_CHEST_COPY = "ChestCopy";

    private final MysteriousTabletData.FragmentType fragmentType;

    public MysteriousTabletFragmentItem(Properties properties, MysteriousTabletData.FragmentType fragmentType) {
        super(properties);
        this.fragmentType = fragmentType;
    }

    public MysteriousTabletData.FragmentType getFragmentType() {
        return fragmentType;
    }

    public static UUID getFragmentId(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        CompoundTag tag = data.copyTag();
        return tag.hasUUID(KEY_FRAGMENT_ID) ? tag.getUUID(KEY_FRAGMENT_ID) : null;
    }

    public static void setFragmentId(ItemStack stack, UUID id) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putUUID(KEY_FRAGMENT_ID, id);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /** Returns true if this stack is a naturally-generated chest copy (not player-owned). */
    public static boolean isChestCopy(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().getBoolean(KEY_CHEST_COPY);
    }

    /** Marks this stack as a chest copy (true) or a player-owned fragment (false). */
    public static void setChestCopy(ItemStack stack, boolean value) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (value) {
            tag.putBoolean(KEY_CHEST_COPY, true);
        } else {
            tag.remove(KEY_CHEST_COPY);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public boolean syncFragment(ServerLevel level, ItemStack stack) {
        MysteriousTabletData data = MysteriousTabletData.get(level.getServer());
        if (data.isLockedByCastle()) {
            return false;
        }

        if (data.tabletExists()
                && !MysteriousTabletItem.isTabletPresent(level.getServer(), data.getTabletId())) {
            data.clearTablet();
        }

        if (data.tabletExists()) {
            return false;
        }

        MysteriousTabletData.FragmentType type = getFragmentType();
        UUID recordedId = data.getFragmentId(type);
        long tick = level.getGameTime();

        // This item was a chest copy that just left a structure chest (picked up or dropped).
        // Strip the flag and either claim ownership (first player) or discard (duplicate).
        if (isChestCopy(stack)) {
            setChestCopy(stack, false);
            if (recordedId != null) {
                // Another player already owns this fragment type — discard this duplicate.
                return false;
            }
            UUID freshId = UUID.randomUUID();
            setFragmentId(stack, freshId);
            data.markFragmentExists(type, freshId, tick);
            return true;
        }

        UUID currentId = getFragmentId(stack);

        if (recordedId == null) {
            if (currentId == null) {
                currentId = UUID.randomUUID();
                setFragmentId(stack, currentId);
            }
            data.markFragmentExists(type, currentId, tick);
            return true;
        }

        if (currentId == null) {
            return false;
        }

        if (recordedId.equals(currentId)) {
            data.markFragmentSeen(type, currentId, tick);
            return true;
        }

        if (!MysteriousTabletItem.isFragmentPresent(level.getServer(), type, recordedId)) {
            data.clearFragment(type);
            data.markFragmentExists(type, currentId, tick);
            return true;
        }

        return false;
    }
}
