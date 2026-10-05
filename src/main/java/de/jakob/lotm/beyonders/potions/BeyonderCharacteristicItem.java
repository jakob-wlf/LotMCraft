package de.jakob.lotm.beyonders.potions;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.PathwayInfos;
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

        int playerSeq = BeyonderData.getSequence(player);
        String playerPath = BeyonderData.getPathway(player);

        if(playerPath.equals("red_priest") && path.equals(playerPath)){
            if(playerSeq == 3 && seq <= 2){
                var component = player.getData(ModAttachments.RITUALS.get());
                if(component.getStage() == 3) {
                    component.setStage(4);

                    return InteractionResultHolder.success(ItemStack.EMPTY);
                }
            }
        }

        if(path.equals(playerPath)){
            if(seq >= playerSeq){
                var stacks = BeyonderData.getCharStacks(player);

                if(stacks[seq] >= 0 && seq >= 1 && BeyonderData.getDigestionProgress(player) == 1.0){
                    BeyonderData.setCharStack(player, (stacks[seq] + 1), seq, true);
                    BeyonderData.setDigestionProgress(player, 0);
                    player.setItemInHand(hand, ItemStack.EMPTY);
                    return InteractionResultHolder.success(ItemStack.EMPTY);
                }
            }
        }

        return InteractionResultHolder.fail(stack);
    }
}
