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

    public boolean syncFragment(ServerLevel level, ItemStack stack) {
        MysteriousTabletData data = MysteriousTabletData.get(level.getServer());
        if (data.isLockedByCastle() || data.tabletExists()) {
            return false;
        }

        MysteriousTabletData.FragmentType type = getFragmentType();
        UUID currentId = getFragmentId(stack);
        UUID recordedId = data.getFragmentId(type);
        long tick = level.getGameTime();

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

        return false;
    }
}
