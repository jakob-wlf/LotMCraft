package de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsHelper;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.UUID;

public class TriggerItemsContext extends TriggerContextBase {
    public LinkedList<ItemStack> stacksList;

    public static String NBT_STACK_LIST = "stack_list";

    public TriggerItemsContext(UUID id){
        super(id);
        stacksList = new LinkedList<>();
    }

    @Override
    public TriggerContextEnum getType() {
        return TriggerContextEnum.ITEM;
    }

    @Override
    public TriggerContextBase fillFromStream(TokenStream stream) {
        while (!stream.isEmpty()) {
            if(stream.match("then")) break;

            var stack = ActionsHelper.getItemFromString(stream.peek());
            if (stack == null) {
                stream.next();
                continue;
            }

            stacksList.add(new ItemStack(stack));
            stream.next();
        }

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

        return tag;
    }

    public static TriggerItemsContext load(CompoundTag tag, UUID id, HolderLookup.Provider provider) {
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

        TriggerItemsContext context = new TriggerItemsContext(id);
        context.stacksList = new LinkedList<>(items);

        return context;
    }
}
