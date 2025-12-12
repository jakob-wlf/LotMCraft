package de.jakob.lotm.potions;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.OpenRecipeMenuPacket;
import de.jakob.lotm.rendering.GuidingBookRenderer;
import de.jakob.lotm.rendering.PotionRecipeOverlay;
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

public class PotionRecipeItem extends Item {
    private PotionRecipe recipe = null;

    public PotionRecipeItem(Properties properties) {
        super(properties);
    }

    public PotionRecipe getRecipe() {
        return recipe;
    }

    public void setRecipe(PotionRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide) {
            PacketHandler.sendToServer(new OpenRecipeMenuPacket(recipe.potion().getSequence(), recipe.potion().getPathway()));
        }
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        return Component.literal(PathwayInfos.getSequenceNameByRegisteredItemName(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()) + " ").append(Component.translatable("lotm.potion_recipe")).append(
                recipe == null ? Component.literal("") : Component.literal(" (").append(Component.translatable("lotm.sequence")).append(Component.literal(" " + recipe.potion().getSequence() + ")")));
    }
}
