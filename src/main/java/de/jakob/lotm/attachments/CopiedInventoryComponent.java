package de.jakob.lotm.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class CopiedInventoryComponent {
    private final SimpleContainer inv;

    // same slots as the player
    public CopiedInventoryComponent() {
        this.inv = new SimpleContainer(41);
    }

    // helper for converting
    public static CopiedInventoryComponent createFromList(List<ItemStack> items) {
        CopiedInventoryComponent component = new CopiedInventoryComponent();
        for (int i = 0; i < Math.min(items.size(), 41); i++) {
            component.getInv().setItem(i, items.get(i));
        }
        return component;
    }

    public SimpleContainer getInv() { return this.inv; }

    public List<ItemStack> getItemsList() {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < this.inv.getContainerSize(); i++) {
            list.add(this.inv.getItem(i));
        }
        return list;
    }

    public static final Codec<CopiedInventoryComponent> SERIALIZER = RecordCodecBuilder.create(instance ->
            instance.group(
                    ItemStack.OPTIONAL_CODEC.listOf()
                            .fieldOf("items")
                            .forGetter(CopiedInventoryComponent::getItemsList)
            ).apply(instance, CopiedInventoryComponent::createFromList)
    );
}