package de.jakob.lotm.beyonders.potions;

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
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}
