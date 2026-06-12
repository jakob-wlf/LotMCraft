package de.jakob.lotm.item.custom;

import de.jakob.lotm.attachments.BlasphemySlateData;
import de.jakob.lotm.events.BlasphemySlateItemHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.OpenRecipeMenuPacket;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import java.util.UUID;

/**
 * A Blasphemy Card for a single beyonder pathway.
 * Only one card per pathway can exist in the world at a time.
 * Right-clicking opens a recipe viewer GUI (handled in BlasphemySlateItemHandler).
 * Can be used in a Brewing Cauldron as a wildcard main ingredient for its pathway.
 */
public class BlasphemyCardItem extends Item {

    public static final String KEY_CARD_ID = "BlasphemyCardId";

    private final String pathway;

    public BlasphemyCardItem(Properties properties, String pathway) {
        super(properties);
        this.pathway = pathway;
    }

    public String getPathway() {
        return pathway;
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        return Component.literal(prettify(pathway) + " Blasphemy Card");
    }

    private static String prettify(String id) {
        String[] words = id.split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (BlasphemySlateItemHandler.isEnvisionSummoned(stack)) {
            int uses = BlasphemySlateItemHandler.getEnvisionedUses(stack);
            tooltip.add(Component.literal("§3Envisioned §7(" + uses + "/" + BlasphemySlateItemHandler.ENVISION_MAX_USES + " brews remaining)"));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            PacketHandler.sendToServer(new OpenRecipeMenuPacket(9, pathway, true));
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    // ─── UUID helpers ─────────────────────────────────────────────────────────

    public static UUID getCardId(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        CompoundTag tag = data.copyTag();
        return tag.hasUUID(KEY_CARD_ID) ? tag.getUUID(KEY_CARD_ID) : null;
    }

    public static void setCardId(ItemStack stack, UUID id) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putUUID(KEY_CARD_ID, id);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Validates uniqueness for this card stack. Assigns a new UUID if none exists
     * and the world allows it; returns false if the item must be destroyed.
     */
    public boolean syncCard(ServerLevel level, ItemStack stack) {
        BlasphemySlateData data = BlasphemySlateData.get(level.getServer());
        long tick = level.getGameTime();

        // If the full slate exists all pathway cards must be gone
        if (data.hasSlate()) return false;
        // If the corresponding half exists, its pathway cards must be gone
        if (BlasphemySlateData.LEFT_HALF_PATHWAYS.contains(pathway)  && data.hasLeftHalf())  return false;
        if (BlasphemySlateData.RIGHT_HALF_PATHWAYS.contains(pathway) && data.hasRightHalf()) return false;
        // If the chaos sea sefirot is claimed, left/right pathway cards must be gone
        if ((BlasphemySlateData.LEFT_HALF_PATHWAYS.contains(pathway) || BlasphemySlateData.RIGHT_HALF_PATHWAYS.contains(pathway))
                && de.jakob.lotm.attachments.SefirotData.get(level.getServer()).isSefirotClaimed("chaos_sea")) return false;

        UUID stackId = getCardId(stack);
        if (stackId == null) {
            if (data.canSpawnCard(pathway)) {
                stackId = UUID.randomUUID();
                setCardId(stack, stackId);
                data.markCardExists(pathway, stackId, tick);
                return true;
            }
            return false;
        }

        UUID recordedId = data.getCardId(pathway);
        if (!stackId.equals(recordedId)) {
            return false;
        }

        data.markCardSeen(pathway, stackId, tick);
        return true;
    }
}
