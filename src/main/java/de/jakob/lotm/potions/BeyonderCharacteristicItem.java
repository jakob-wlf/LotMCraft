package de.jakob.lotm.potions;

import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.pathways.PathwayInfos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BeyonderCharacteristicItem extends Item {

    private final String pathway;
    private final int sequence;

    public BeyonderCharacteristicItem(Properties properties, String pathway, int sequence) {
        super(properties);

        this.pathway = pathway;
        this.sequence = sequence;
    }

    public String getPathway() {
        return pathway;
    }

    public int getSequence() {
        return sequence;
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        return Component.literal(PathwayInfos.getSequenceNameByRegisteredItemName(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().replace("_characteristic", "")) + " ").append(Component.translatable("lotm.beyonder_characteristic")).append(
                Component.literal(" (").append(Component.translatable("lotm.sequence")).append(Component.literal(" " + getSequence() + ")")));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if(level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        var item = stack.getItem();

        if(!(item instanceof BeyonderCharacteristicItem beChar)) return InteractionResultHolder.fail(stack);

        int seq = beChar.getSequence();
        String path = beChar.getPathway();

        if(path.equals(BeyonderData.getPathway(player))){
            if(seq >= BeyonderData.getSequence(player)){
                var stacks = BeyonderData.getCharStacks(player);

                if(stacks[seq] >= 0 && seq >= 1 && BeyonderData.getDigestionProgress(player) == 1.0){
                    BeyonderData.setCharStack(player, (stacks[seq] + 1), seq, true, pathway);
                    BeyonderData.setDigestionProgress(player, 0);
                    player.setItemInHand(hand, ItemStack.EMPTY);
                    return InteractionResultHolder.success(ItemStack.EMPTY);
                }
            }
        }

        return InteractionResultHolder.fail(stack);
    }
}
