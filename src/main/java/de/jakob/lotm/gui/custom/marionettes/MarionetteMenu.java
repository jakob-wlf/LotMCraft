package de.jakob.lotm.gui.custom.marionettes;

import de.jakob.lotm.gui.ModMenuTypes;
import de.jakob.lotm.network.packets.toServer.RequestMarionetteSyncPacket.MarionetteEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MarionetteMenu extends AbstractContainerMenu {

    private final List<Integer> entityIds;
    private final List<LivingEntity> marionettes = new ArrayList<>();
    private final Map<Integer, MarionetteEntry> syncedData = new HashMap<>();

    public MarionetteMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, readEntityIds(buf));
    }

    private static List<Integer> readEntityIds(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<Integer> ids = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ids.add(buf.readVarInt());
        }
        return ids;
    }

    public MarionetteMenu(int containerId, Inventory playerInventory, List<Integer> entityIds) {
        super(ModMenuTypes.MARIONETTE_MENU.get(), containerId);
        this.entityIds = entityIds;

        if (playerInventory.player.level().isClientSide) {
            for (int id : entityIds) {
                if (playerInventory.player.level().getEntity(id) instanceof LivingEntity living) {
                    marionettes.add(living);
                }
            }
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public List<Integer> getEntityIds() {
        return entityIds;
    }

    public List<LivingEntity> getMarionettes() {
        return marionettes;
    }

    public MarionetteEntry getSyncedData(int entityId) {
        return syncedData.get(entityId);
    }

    public void applySync(List<MarionetteEntry> entries) {
        for (MarionetteEntry entry : entries) {
            syncedData.put(entry.entityId(), entry);
        }
        if (Minecraft.getInstance().screen instanceof MarionetteControlScreen screen
                && screen.getMenu() == this) {
            screen.applySync(entries);
        }
    }


}