package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.MysteriousTabletData;
import de.jakob.lotm.item.custom.MysteriousTabletFragmentItem;
import de.jakob.lotm.item.custom.MysteriousTabletItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MysteriousTabletItemHandler {

    private static final int PLAYER_CHECK_INTERVAL = 20;
    private static final int CLEANUP_INTERVAL = 100;
    private static final long UNSEEN_TICKS = 200L;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();

        if (entity instanceof ItemEntity itemEntity) {
            if (itemEntity.level().isClientSide) {
                return;
            }
            if (!(itemEntity.level() instanceof ServerLevel level)) {
                return;
            }
            if (!syncStack(level, itemEntity.getItem())) {
                itemEntity.discard();
            }
            return;
        }

        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        if (player.level().isClientSide) {
            return;
        }
        if (player.tickCount % PLAYER_CHECK_INTERVAL != 0) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!syncStack(level, stack)) {
                player.getInventory().removeItem(i, 1);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        long time = overworld.getGameTime();
        if (time % CLEANUP_INTERVAL != 0) {
            return;
        }

        MysteriousTabletData data = MysteriousTabletData.get(event.getServer());
        if (data.isLockedByCastle()) {
            return;
        }

        if (data.tabletExists() && time - data.getTabletLastSeen() > UNSEEN_TICKS) {
            data.clearTablet();
        }

        for (MysteriousTabletData.FragmentType type : MysteriousTabletData.FragmentType.values()) {
            if (!data.hasFragment(type)) {
                continue;
            }
            if (time - data.getFragmentLastSeen(type) > UNSEEN_TICKS) {
                data.clearFragment(type);
            }
        }
    }

    private static boolean syncStack(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        if (stack.getItem() instanceof MysteriousTabletFragmentItem fragmentItem) {
            return fragmentItem.syncFragment(level, stack);
        }

        if (stack.getItem() instanceof MysteriousTabletItem tabletItem) {
            return tabletItem.syncTablet(level, stack);
        }

        return true;
    }
}
