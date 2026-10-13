package de.jakob.lotm.gui.custom.cohort;

import com.mojang.authlib.GameProfile;
import de.jakob.lotm.gui.ModMenuTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.CohortUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
public class CohortMenu extends AbstractContainerMenu {

    private static final int MEMBER_SLOTS = 7;
    private static final int SLOT_X_START = 52;
    private static final int SLOT_X_STEP  = 32;
    private static final int SLOT_Y       = 64;
    private static final int LEADER_SLOT_X = 132;
    private static final int LEADER_SLOT_Y = 24;

    // Client-side constructor
    public CohortMenu(int containerId, Inventory playerInventory, FriendlyByteBuf ignored) {
        this(new ArrayList<>(), containerId, playerInventory);
    }

    // Server-side constructor
    public CohortMenu(List<String[]> members, int containerId, Inventory playerInventory) {
        super(ModMenuTypes.COHORT_MENU.get(), containerId);

        // Backing inventory for the slots
        ItemStackHandler itemHandler = new ItemStackHandler(MEMBER_SLOTS) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return false; // players can't insert arbitrary items
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                // Allow extraction so the player can pick up / drop the head
                return super.extractItem(slot, amount, simulate);
            }
        };

        // Leader slot + member slots
        this.addSlot(new MemberHeadSlot(itemHandler, 0, LEADER_SLOT_X, LEADER_SLOT_Y));
        itemHandler.setStackInSlot(0, createMemberHead(new String[]{playerInventory.player.getStringUUID(), playerInventory.player.getDisplayName().getString()}, true));
        for (int j = 1; j < MEMBER_SLOTS; j++) {
            this.addSlot(new MemberHeadSlot(
                    itemHandler, j,
                    SLOT_X_START + (SLOT_X_STEP * j),
                    SLOT_Y));
        }

        // Populate heads
        int size = Math.min(members.size(), itemHandler.getSlots());
        for (int i = 0; i < size; i++) {
            itemHandler.setStackInSlot(i + 1, createMemberHead(members.get(i), false));
        }
    }

    /**
     * Builds a player-head item for one member.
     * <p>
     * {@code member} layout: {@code [uuid, name, pathwayId?, sequence?]}
     */
    private static ItemStack createMemberHead(String[] member, boolean leader) {
        String uuid = member[0];
        String name = member[1];

        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);

        // --- Profile ---
        GameProfile profile = new GameProfile(UUID.fromString(uuid), name);
        stack.set(DataComponents.PROFILE, new ResolvableProfile(profile));

        // --- Display name: "[Member] NAME" ---
        Component customName = Component.literal("[" + ((leader) ? "Commander" : "Member")+"] " + name)
                .withStyle(style -> style
                        .withColor(ChatFormatting.GOLD)
                        .withBold(true)
                        .withItalic(false));
        stack.set(DataComponents.CUSTOM_NAME, customName);

        // --- Lore ---
        List<Component> lore = new ArrayList<>();

        if (member.length > 2) {
            String pathwayName = getPathwayName(member[2]);
            lore.add(Component.literal("Pathway: " + pathwayName + " Sequence: " + member[3])
                    .withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)));
        } else {
            if(!leader) lore.add(Component.literal("Currently offline...")
                    .withStyle(style -> style.withColor(ChatFormatting.DARK_GRAY).withItalic(true)));
        }

        if(!leader) lore.add(Component.literal("Drop: remove from team")
                .withStyle(style -> style.withColor(ChatFormatting.RED).withItalic(false)));

        stack.set(DataComponents.LORE, new ItemLore(lore));

        return stack;
    }

    private static String getPathwayName(String pathwayId) {
        var info = BeyonderData.pathwayInfos.get(pathwayId);
        if (info == null) return pathwayId;
        try {
            return info.getSequenceName(9);
        } catch (NumberFormatException e) {
            return pathwayId;
        }
    }


    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player1) {
        if(player1.level().isClientSide || !(player1 instanceof ServerPlayer player)) return;
        if (slotId >= 1 && slotId < this.slots.size() && clickType == ClickType.THROW) {
            Slot slot = this.slots.get(slotId);
            ItemStack stack = slot.getItem();

            ResolvableProfile profile = stack.get(DataComponents.PROFILE);
            if (profile != null && profile.gameProfile() != null) {
                UUID uuid = profile.gameProfile().getId();
                handleMemberDrop(player, uuid);
            }
            return;
        }

        super.clicked(slotId, button, clickType, player);
    }

    private void handleMemberDrop(ServerPlayer sender, UUID memberUuid) {

        ServerPlayer player = sender.getServer().getPlayerList().getPlayer(memberUuid);
        if(player == null) sender.sendSystemMessage(Component.literal("Player is not online.").withStyle(ChatFormatting.RED), false);
        else CohortUtil.removeMember(sender, player);

        sender.closeContainer();
        sender.openMenu(new CohortMenuProvider(CohortUtil.getALlPlayers(sender)));
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}

