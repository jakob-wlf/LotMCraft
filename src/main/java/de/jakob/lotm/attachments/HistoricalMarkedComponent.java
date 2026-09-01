package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.ArrayList;
import java.util.List;

public class HistoricalMarkedComponent {
    private List<CompoundTag> markedEntities = new ArrayList<>();
    private List<ItemStack> markedItems = new ArrayList<>();

    public List<CompoundTag> getMarkedEntities() {
        return markedEntities;
    }

    public void setMarkedEntities(List<CompoundTag> markedEntities) {
        this.markedEntities = markedEntities;
    }

    public void addMarkedEntity(CompoundTag entity) {
        this.markedEntities.add(entity);
    }

    public void removeMarkedEntity(CompoundTag entity) {
        this.markedEntities.remove(entity);
    }



    public List<ItemStack> getMarkedItems() {
        return markedItems;
    }

    public void setMarkedItems(List<ItemStack> markedItems) {
        this.markedItems = markedItems;
    }

    public void addMarkedItem(ItemStack item) {
        this.markedItems.add(item);
    }

    public void removeMarkedItem(ItemStack item) {
        this.markedItems.remove(item);
    }


    public static final IAttachmentSerializer<CompoundTag, HistoricalMarkedComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public HistoricalMarkedComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    HistoricalMarkedComponent component = new HistoricalMarkedComponent();

                    if (tag.contains("markedEntities", Tag.TAG_LIST)) {
                        ListTag entitiesTag = tag.getList("markedEntities", Tag.TAG_COMPOUND);
                        for (int i = 0; i < entitiesTag.size(); i++) {
                            component.markedEntities.add(entitiesTag.getCompound(i));
                        }
                    }

                    if (tag.contains("markedItems", Tag.TAG_LIST)) {
                        ListTag itemsTag = tag.getList("markedItems", Tag.TAG_COMPOUND);
                        for (int i = 0; i < itemsTag.size(); i++) {
                            ItemStack stack = ItemStack.parseOptional(lookup, itemsTag.getCompound(i));
                            if (!stack.isEmpty()) {
                                component.markedItems.add(stack);
                            }
                        }
                    }

                    return component;
                }

                @Override
                public CompoundTag write(HistoricalMarkedComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();

                    ListTag entitiesTag = new ListTag();
                    if (component.getMarkedEntities() != null) {
                        entitiesTag.addAll(component.getMarkedEntities());
                    }
                    tag.put("markedEntities", entitiesTag);

                    ListTag itemsTag = new ListTag();
                    if (component.getMarkedItems() != null) {
                        for (ItemStack stack : component.getMarkedItems()) {
                            if (!stack.isEmpty()) {
                                itemsTag.add(stack.save(lookup));
                            }
                        }
                    }
                    tag.put("markedItems", itemsTag);

                    return tag;
                }
            };
}