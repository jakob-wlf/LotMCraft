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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CohortMenu extends AbstractContainerMenu {

    // ---------- Layout constants ----------
    private static final int LEADER_SLOT = 0;
    private static final int PLAYER_SLOTS = 6;
    private static final int NPC_SLOTS    = 20;          // visible per page
    private static final int NPC_COLS     = 5;
    private static final int SLOT_STEP    = 18;

    private static final int LEADER_X = 79,  LEADER_Y = 10;
    private static final int PLAYER_X_START = 34, PLAYER_Y = 36;
    private static final int NPC_X_START    = 43, NPC_Y_START = 68;

    private static final int HANDLER_SIZE  = 1 + PLAYER_SLOTS + NPC_SLOTS; // 27

    // Slot-id ranges (index into this.slots — same as handler index)
    private static final int PLAYER_IDX_START = 1;
    private static final int PLAYER_IDX_END   = PLAYER_IDX_START + PLAYER_SLOTS - 1; // 6
    private static final int NPC_IDX_START    = PLAYER_IDX_END + 1;                  // 7
    private static final int NPC_IDX_END      = NPC_IDX_START + NPC_SLOTS - 1;       // 26

    // Button IDs for clickMenuButton
    public static final int BUTTON_NPC_PREV = 0;
    public static final int BUTTON_NPC_NEXT = 1;

    // ---------- State ----------
    private final ItemStackHandler handler;
    private final List<String[]> allNpcs;
    private int npcPage = 0;
    private final int totalNpcPages;

    // Constructors

    // Client-side (no data, server will sync slots)
    public CohortMenu(int containerId, Inventory playerInventory, FriendlyByteBuf ignored) {
        this(new HashMap<>(), containerId, playerInventory);
    }

    // Server-side
    public CohortMenu(HashMap<String, List<String[]>> allMembers, int containerId, Inventory playerInventory) {
        super(ModMenuTypes.COHORT_MENU.get(), containerId);

        List<String[]> members     = allMembers.getOrDefault("players", List.of());
        List<String[]> subordinates = allMembers.getOrDefault("npc", List.of());
        this.allNpcs      = subordinates;
        this.totalNpcPages = Math.max(1, (int) Math.ceil(subordinates.size() / (double) NPC_SLOTS));

        // ----- Backing inventory -----
        this.handler = new ItemStackHandler(HANDLER_SIZE) {
            @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return false; }
            // Extraction is blocked at the slot level (LockedHeadSlot), so no override needed here.
        };

        // ----- Leader slot (current player) -----
        this.addSlot(new MemberHeadSlot(handler, LEADER_SLOT, LEADER_X, LEADER_Y));
        handler.setStackInSlot(LEADER_SLOT, createMemberHead(new String[]{
                playerInventory.player.getStringUUID(),
                playerInventory.player.getDisplayName().getString()
        }, true, false));

        // ----- Player slots (row of 6, centred) -----
        for (int i = 0; i < PLAYER_SLOTS; i++) {
            this.addSlot(new MemberHeadSlot(
                    handler, PLAYER_IDX_START + i,
                    PLAYER_X_START + i * SLOT_STEP, PLAYER_Y));
        }

        for (int i = 0; i < NPC_SLOTS; i++) {
            int col = i % NPC_COLS;
            int row = i / NPC_COLS;
            this.addSlot(new MemberHeadSlot(
                    handler, NPC_IDX_START + i,
                    NPC_X_START + col * SLOT_STEP,
                    NPC_Y_START + row * SLOT_STEP));
        }

        int playerSize = Math.min(members.size(), PLAYER_SLOTS);
        for (int i = 0; i < playerSize; i++) {
            handler.setStackInSlot(PLAYER_IDX_START + i, createMemberHead(members.get(i), false, false));
        }

        populateNpcPage();
    }


    // Paging


    private void populateNpcPage() {
        // Clear all visible NPC slots first
        for (int i = 0; i < NPC_SLOTS; i++) {
            handler.setStackInSlot(NPC_IDX_START + i, ItemStack.EMPTY);
        }

        int start = npcPage * NPC_SLOTS;
        int end   = Math.min(start + NPC_SLOTS, allNpcs.size());
        for (int i = start; i < end; i++) {
            handler.setStackInSlot(NPC_IDX_START + (i - start),
                    createMemberHead(allNpcs.get(i), false, true));
        }
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int id) {
        switch (id) {
            case BUTTON_NPC_PREV -> {
                if (npcPage > 0) { npcPage--; populateNpcPage(); broadcastChanges(); return true; }
            }
            case BUTTON_NPC_NEXT -> {
                if (npcPage < totalNpcPages - 1) { npcPage++; populateNpcPage(); broadcastChanges(); return true; }
            }
            default -> { return false; }
        }
        return false;
    }

    // Leter imma add npc pages :D
    public int getNpcPage()      { return npcPage; }
    public int getTotalNpcPages(){ return totalNpcPages; }

    // Click handling

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player1) {
        if(player1.level().isClientSide || !(player1 instanceof ServerPlayer player)) return;
        if (slotId == LEADER_SLOT) return;

        // --- Player section ---
        if (slotId >= PLAYER_IDX_START && slotId <= PLAYER_IDX_END) {
            if (clickType == ClickType.THROW) {
                Slot slot = this.slots.get(slotId);
                ItemStack stack = slot.getItem();

                ResolvableProfile profile = stack.get(DataComponents.PROFILE);
                if (profile != null && profile.gameProfile() != null) {
                    UUID uuid = profile.gameProfile().getId();
                    handlePlayerRemoval(player, uuid);
                }
                return;
            }
            return;
        }

        // --- NPC section ---
        if (slotId >= NPC_IDX_START && slotId <= NPC_IDX_END) {
            int visibleIndex = slotId - NPC_IDX_START;
            int realIndex    = npcPage * NPC_SLOTS + visibleIndex;

            if (clickType == ClickType.THROW) {
                handleNpcRemoval(player, realIndex);
            } else if (clickType == ClickType.PICKUP && button == 0) {
                handleNpcObtainController(player, realIndex);
            }
            return;
        }

        super.clicked(slotId, button, clickType, player);
    }

    // Action handlers

    private void handlePlayerRemoval(ServerPlayer sender, UUID memberUuid) {
        ServerPlayer player = sender.getServer().getPlayerList().getPlayer(memberUuid);
        if(player == null) sender.sendSystemMessage(Component.literal("Player is not online.").withStyle(ChatFormatting.RED), false);
        else CohortUtil.removeMember(sender, player);

        sender.closeContainer();
        sender.openMenu(new CohortMenuProvider(CohortUtil.getAllMembersGrouped(sender)));
    }

    private void handleNpcRemoval(Player player, int realIndex) {
        if (realIndex >= allNpcs.size()) return;
        CohortUtil.removeSubordinate(player, realIndex);
        player.closeContainer();
        player.openMenu(new CohortMenuProvider(CohortUtil.getAllMembersGrouped(player)));
    }

    private void handleNpcObtainController(Player player, int realIndex) {
        if (realIndex >= allNpcs.size()) return;
        CohortUtil.obtainController(player, realIndex);
        player.sendSystemMessage(Component.literal("Obtained controller of #" + realIndex));
    }

    // Head construction

    private static String sanitizeProfileName(String raw) {
        if (raw == null || raw.isEmpty()) return "unknown";
        String stripped = raw.replaceAll("§.", "");
        return stripped.length() > 16 ? stripped.substring(0, 16) : stripped;
    }


    private static ItemStack createMemberHead(String[] member, boolean leader, boolean npc) {
        String uuid = member[0];
        String name = sanitizeProfileName(member[1]);
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);

        // --- Profile ---
        if(!npc) {
            GameProfile profile = new GameProfile(UUID.fromString(uuid), name);
            stack.set(DataComponents.PROFILE, new ResolvableProfile(profile));
        }


        // --- Display name ---
        String prefix = leader ? "Commander" : (npc ? "Subordinate" : "Member");
        ChatFormatting nameColor = npc ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GOLD;

        Component customName = Component.literal("[" + prefix + "] " + name)
                .withStyle(style -> style
                        .withColor(nameColor)
                        .withBold(true)
                        .withItalic(false));
        stack.set(DataComponents.CUSTOM_NAME, customName);

        // --- Lore ---
        List<Component> lore = new ArrayList<>();

        if (member.length >= 4) {
            // Pathway + sequence present
            String pathwayName = getPathwayName(member[2]);
            lore.add(Component.literal("Pathway: " + pathwayName + " Sequence: " + member[3])
                    .withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)));
            if (npc) {
                lore.add(Component.literal("Click to obtain controller")
                        .withStyle(style -> style.withColor(ChatFormatting.AQUA).withItalic(false)));
            }
        } else {
            if (!leader) {
                lore.add(Component.literal("Currently offline...")
                        .withStyle(style -> style.withColor(ChatFormatting.DARK_GRAY).withItalic(true)));
            }
        }

        // Everyone except the leader can be removed
        if (!leader) {
            lore.add(Component.literal("Drop: remove from the team")
                    .withStyle(style -> style.withColor(ChatFormatting.RED).withItalic(false)));
        }

        stack.set(DataComponents.LORE, new ItemLore(lore));
        return stack;
    }

    /** Safe pathway lookup — returns the raw ID on any failure. */
    private static String getPathwayName(String pathwayId) {
        var info = BeyonderData.pathwayInfos.get(pathwayId);
        if (info == null) return pathwayId;
        try {
            return info.getSequenceName(9);
        } catch (NumberFormatException e) {
            return pathwayId;
        }
    }

    // ==================================================================
    // AbstractContainerMenu stubs
    // ==================================================================

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}