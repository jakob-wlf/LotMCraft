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

    private final boolean servants;
    private final List<Integer> entityIds;
    private final List<ServantRow> servantRows;
    private final List<LivingEntity> marionettes = new ArrayList<>();
    private final Map<Integer, MarionetteEntry> syncedData = new HashMap<>();

    public MarionetteMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        super(ModMenuTypes.MARIONETTE_MENU.get(), containerId);
        this.servants = buf.readBoolean();
        if (this.servants) {
            this.entityIds = List.of();
            this.servantRows = readServants(buf);
        } else {
            this.entityIds = readEntityIds(buf);
            this.servantRows = List.of();
            if (playerInventory.player.level().isClientSide) {
                for (int id : entityIds) {
                    if (playerInventory.player.level().getEntity(id) instanceof LivingEntity living) {
                        marionettes.add(living);
                    }
                }
            }
        }
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
        this.servants = false;
        this.entityIds = entityIds;
        this.servantRows = List.of();

        if (playerInventory.player.level().isClientSide) {
            for (int id : entityIds) {
                if (playerInventory.player.level().getEntity(id) instanceof LivingEntity living) {
                    marionettes.add(living);
                }
            }
        }
    }

    public static void writeServants(RegistryFriendlyByteBuf buf, List<ServantRow> rows) {
        buf.writeBoolean(true);
        buf.writeVarInt(rows.size());
        for (ServantRow row : rows) {
            buf.writeUUID(row.id());
            buf.writeUtf(row.name());
            buf.writeUtf(row.detail());
            buf.writeBoolean(row.beyonder());
            buf.writeUtf(row.pathway());
            buf.writeVarInt(row.sequence());
        }
    }

    private static List<ServantRow> readServants(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ServantRow> rows = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            rows.add(new ServantRow(buf.readUUID(), buf.readUtf(), buf.readUtf(), buf.readBoolean(), buf.readUtf(), buf.readVarInt()));
        }
        return rows;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public boolean isServants() {
        return servants;
    }

    public List<ServantRow> getServants() {
        return servantRows;
    }

    public void removeServant(UUID id) {
        servantRows.removeIf(row -> row.id().equals(id));
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

    public record ServantRow(UUID id, String name, String detail, boolean beyonder, String pathway, int sequence) {}


}