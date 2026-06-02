package de.jakob.lotm.item.custom;

import de.jakob.lotm.attachments.MysteriousTabletData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.UUID;

public class MysteriousTabletItem extends Item {

    public static final String KEY_TABLET_ID = "TabletId";

    public MysteriousTabletItem(Properties properties) {
        super(properties);
    }

    public static UUID getTabletId(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        CompoundTag tag = data.copyTag();
        return tag.hasUUID(KEY_TABLET_ID) ? tag.getUUID(KEY_TABLET_ID) : null;
    }

    public static void setTabletId(ItemStack stack, UUID id) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putUUID(KEY_TABLET_ID, id);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public boolean syncTablet(ServerLevel level, ItemStack stack) {
        MysteriousTabletData data = MysteriousTabletData.get(level.getServer());
        if (data.isLockedByCastle()) {
            return false;
        }

        UUID currentId = getTabletId(stack);
        UUID recordedId = data.getTabletId();
        long tick = level.getGameTime();

        if (recordedId == null) {
            if (!data.canSpawnTablet()) {
                return false;
            }
            if (currentId == null) {
                currentId = UUID.randomUUID();
                setTabletId(stack, currentId);
            }
            data.markTabletExists(currentId, tick);
            return true;
        }

        if (currentId == null) {
            return false;
        }

        if (recordedId.equals(currentId)) {
            data.markTabletSeen(currentId, tick);
            return true;
        }

        return false;
    }
}
