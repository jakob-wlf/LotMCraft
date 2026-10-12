package de.jakob.lotm.gui.custom.pathway_selection;

import com.mojang.logging.LogUtils;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.beyonders.potions.PotionRecipeItemHandler;
import de.jakob.lotm.gui.ModMenuTypes;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PathwaySelectionMenu extends AbstractContainerMenu {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int MAX_ID_LENGTH = 32;
    public static final int MAX_PATHWAYS = 32;
    private static final int MAX_INVALID_CLICKS = 3;

    private final List<String> offered;

    private boolean consumed = false;
    private int invalidClicks = 0;

    public PathwaySelectionMenu(int containerId, Inventory inventory, List<String> offered) {
        super(ModMenuTypes.PATHWAY_SELECTION.get(), containerId);
        this.offered = Collections.unmodifiableList(new ArrayList<>(offered));
    }

    public PathwaySelectionMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        this(containerId, inventory, readOffered(buf));
    }

    private static List<String> readOffered(FriendlyByteBuf buf) {
        return buf.readCollection(ArrayList::new, b -> b.readUtf(MAX_ID_LENGTH));
    }

    public static boolean open(ServerPlayer player, Collection<String> candidates) {
        if (BeyonderData.isBeyonder(player)) return false;

        List<String> offered = candidates.stream()
                .filter(PathwaySelectionMenu::isSelectable)
                .distinct()
                .limit(MAX_PATHWAYS)
                .toList();
        if (offered.isEmpty()) return false;

        PathwaySelectionMenuProvider provider = new PathwaySelectionMenuProvider(offered);
        player.openMenu(provider, provider::writeExtraData);
        return true;
    }

    public static boolean openWithAllPathways(ServerPlayer player) {
        return open(player, BeyonderData.implementedPathways);
    }

    public static boolean isSelectable(String id) {
        return id != null
                && BeyonderData.implementedPathways.contains(id)
                && BeyonderData.pathwayInfos.containsKey(id);
    }

    public List<String> getOfferedPathways() {
        return offered;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        if (consumed) return false;

        if (serverPlayer.isRemoved() || BeyonderData.isBeyonder(serverPlayer)) {
            serverPlayer.closeContainer();
            return false;
        }

        if (id < 0 || id >= offered.size()) {
            invalidClicks++;
            LOGGER.warn("Player {} sent invalid pathway index {} ({}/{})",
                    serverPlayer.getGameProfile().getName(), id, invalidClicks, MAX_INVALID_CLICKS);
            if (invalidClicks >= MAX_INVALID_CLICKS) serverPlayer.closeContainer();
            return false;
        }

        String pathway = offered.get(id);
        if (!isSelectable(pathway)) {
            serverPlayer.closeContainer();
            return false;
        }

        consumed = true;
        serverPlayer.closeContainer();
        onPathwaySelected(serverPlayer, pathway);
        return true;
    }

    protected void onPathwaySelected(ServerPlayer player, String pathwayId) {
        Item characteristic = BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(pathwayId, 9);
        Item recipe = PotionRecipeItemHandler.selectRecipeOfPathwayAndSequence(pathwayId, 9);

        if(characteristic != null && recipe != null) {
            player.addItem(new ItemStack(characteristic));
            player.addItem(new ItemStack(recipe));
        }

    }

    @Override
    public boolean stillValid(Player player) {
        if (player.level().isClientSide) return true;
        return !consumed && !player.isRemoved() && !BeyonderData.isBeyonder(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}