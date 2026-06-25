package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsHelper;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;

public class ActionItemsContext extends ActionContextBase {
    public LinkedList<ItemStack> stacksList;
    public boolean all;

    public static String NBT_STACK_LIST = "stack_list";
    public static String NBT_ALL_ITEMS = "all_items";

    public ActionItemsContext(UUID entityId, boolean allItems) {
        super(entityId);

        stacksList = new LinkedList<>();
        all = allItems;
    }

    public ActionItemsContext(UUID id){
        super(id);
        stacksList = new LinkedList<>();
        all = false;
    }

    @Override
    public ActionContextEnum getType() {
        return ActionContextEnum.ITEM;
    }

    @Override
    public ActionContextBase fillFromStream(TokenStream stream) {
        if(stream.match("drop"))
            stream.next();

        if(stream.match("all")){
            all = true;
            return this;
        }

        while (!stream.isEmpty()) {
            stacksList.add(new ItemStack(Objects.requireNonNull(ActionsHelper.getItemFromString(stream.peek()))));
            stream.next();
        }

        all = false;
        return this;
    }

    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider){
        var tag = super.toNBT(provider);

        ListTag listTag = new ListTag();

        for (int i = 0; i < stacksList.size(); i++) {
            ItemStack stack = stacksList.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stack.save(provider, itemTag);
                listTag.add(itemTag);
            }
        }
        tag.put(NBT_STACK_LIST, listTag);

        tag.putBoolean(NBT_ALL_ITEMS, all);

        return tag;
    }

    public static ActionItemsContext load(CompoundTag tag, UUID id, HolderLookup.Provider provider) {
        NonNullList<ItemStack> items = NonNullList.withSize(40, ItemStack.EMPTY);

        ListTag listTag = tag.getList(NBT_STACK_LIST, Tag.TAG_COMPOUND);

        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag itemTag = listTag.getCompound(i);
            int slot = itemTag.getInt("Slot");

            ItemStack obj = ItemStack.parse(provider, itemTag).orElse(ItemStack.EMPTY);

            if (slot >= 0 && slot < items.size()) {
                items.set(slot, obj);
            }
        }

        boolean all = tag.getBoolean(NBT_ALL_ITEMS);

        ActionItemsContext context = new ActionItemsContext(id, all);
        context.stacksList = new LinkedList<>(items);

        return context;
    }

}
