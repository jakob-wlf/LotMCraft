package de.jakob.lotm.item.custom;

import de.jakob.lotm.rendering.GuidingBookRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GuidingBookItem extends Item {
    
    public GuidingBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            GuidingBookRenderer.nextPage();
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}