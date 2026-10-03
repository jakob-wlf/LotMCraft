package de.jakob.lotm.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SpacerItem extends Item {
    public SpacerItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(" "); // Single space, not empty
    }

    //@Override
    //    public void appendHoverText(ItemStack stack, TooltipContext context,
    //                                List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
    //        // No tooltip added
    //    }
    //
    //    @Override
    //    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
    //        return false; // This makes it not appear in searches
    //    }
}