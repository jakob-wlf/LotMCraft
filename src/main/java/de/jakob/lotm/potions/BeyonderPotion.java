package de.jakob.lotm.potions;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BeyonderPotion extends Item {

    private final int sequence;
    private final String pathway;

    public BeyonderPotion(Properties properties, int sequence, String pathway) {
        super(properties);

        this.sequence = sequence;
        this.pathway = pathway;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            BeyonderData.advance(entity, pathway, sequence);
        }

        return new ItemStack(PotionItemHandler.EMPTY_BOTTLE.get());
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32; // Standard drink time
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    public int getSequence() {
        return sequence;
    }

    public String getPathway() {
        return pathway;
    }
}
