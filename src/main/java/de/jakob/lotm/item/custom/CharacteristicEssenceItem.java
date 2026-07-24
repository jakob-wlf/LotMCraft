package de.jakob.lotm.item.custom;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CharacteristicEssenceItem extends Item {
    public CharacteristicEssenceItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (!(level instanceof ServerLevel)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!BeyonderData.isBeyonder(player)) {
            return InteractionResultHolder.fail(stack);
        }

        int sequence = BeyonderData.getSequence(player);
        if (sequence < 1) {
            return InteractionResultHolder.fail(stack);
        }

        BeyonderData.addCharStack(player, sequence);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.success(stack);
    }
}
