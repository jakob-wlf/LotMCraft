package de.jakob.lotm.potions;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    @Override
    public @NotNull Component getName(ItemStack stack) {
        return Component.literal(BeyonderData.getSequenceName(pathway, sequence) + " ").append(Component.translatable("lotm.potion"));
    }

    @Override
    public void appendHoverText(ItemStack stack,
                                TooltipContext context,
                                List<Component> tooltipComponents,
                                TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.literal(BeyonderData.getSequenceName(pathway, 9) + " ").append(Component.translatable("lotm.pathway")).withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.add(Component.translatable("lotm.sequence").append(Component.literal(" " + sequence)).withStyle(ChatFormatting.DARK_GRAY));
    }

    public int getSequence() {
        return sequence;
    }

    public String getPathway() {
        return pathway;
    }
}
