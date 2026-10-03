package de.jakob.lotm.item.custom;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class StoryBookItem extends Item {

    public static final String KEY_AUTHOR = "AuthorUUID";
    public static final String KEY_TARGET = "TargetUUID";
    public static final String KEY_TARGET_NAME = "TargetName";
    public static final String KEY_USES = "UsesRemaining";

    public StoryBookItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(ServerPlayer author, ServerPlayer target) {
        ItemStack stack = new ItemStack(de.jakob.lotm.item.ModItems.STORY_BOOK.get());
        CompoundTag tag = new CompoundTag();
        tag.putInt(KEY_USES, 3);
        tag.putUUID(KEY_AUTHOR, author.getUUID());
        tag.putUUID(KEY_TARGET, target.getUUID());
        tag.putString(KEY_TARGET_NAME, target.getName().getString());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Story of " + target.getName().getString())
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
        return stack;
    }

    public static UUID getAuthorUUID(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        CompoundTag tag = data.copyTag();
        return tag.hasUUID(KEY_AUTHOR) ? tag.getUUID(KEY_AUTHOR) : null;
    }

    public static UUID getTargetUUID(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        CompoundTag tag = data.copyTag();
        return tag.hasUUID(KEY_TARGET) ? tag.getUUID(KEY_TARGET) : null;
    }

    public static int getUsesRemaining(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return 0;
        CompoundTag tag = data.copyTag();
        return tag.contains(KEY_USES) ? tag.getInt(KEY_USES) : 0;
    }

    public static void decrementUses(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        CompoundTag tag = data.copyTag();
        tag.putInt(KEY_USES, Math.max(0, tag.getInt(KEY_USES) - 1));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }


    /**
     * When the book is tossed, discard and cancel — mirrors HistoricalVoidSummoningAbility.
     */
    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (!(event.getEntity().getItem().getItem() instanceof StoryBookItem)) return;
        event.getEntity().discard();
        event.setCanceled(true);
    }

    /**
     * When the target entity dies, remove the story book from the author's inventory.
     * Mirrors MarionetteControllerItem behaviour on marionette death.
     */
    @SubscribeEvent
    public static void onTargetDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;

        UUID deadUUID = event.getEntity().getUUID();

        // Find any author holding a book targeting this entity
        for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!(stack.getItem() instanceof StoryBookItem)) continue;

                UUID targetUUID = getTargetUUID(stack);
                if (deadUUID.equals(targetUUID)) {
                    player.getInventory().removeItem(i, 1);
                    break;
                }
            }
        }
    }

    /**
     * Tick check: remove story books from non-authors, discard dropped story book entities,
     * and ensure no author has more than one story book at a time.
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        // World item entity — discard any dropped story books immediately
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            if (!(itemEntity.getItem().getItem() instanceof StoryBookItem)) return;
            if (!itemEntity.level().isClientSide) itemEntity.discard();
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;
        if (player.tickCount % 20 != 0) return;

        int bookCount = 0;
        UUID authorUUID = player.getUUID();

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!(stack.getItem() instanceof StoryBookItem)) continue;

            UUID stackAuthor = getAuthorUUID(stack);

            // Remove if not authored by this player
            if (stackAuthor == null || !stackAuthor.equals(authorUUID)) {
                player.getInventory().removeItem(i, 1);
                continue;
            }

            bookCount++;
            // If author has more than one book, remove extras
            if (bookCount > 1) {
                player.getInventory().removeItem(i, 1);
            }
        }
    }
}