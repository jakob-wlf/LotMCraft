package de.jakob.lotm.artifacts;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.data.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SealedArtifactItem extends Item {

    public SealedArtifactItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if(level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        SealedArtifactData data = stack.get(ModDataComponents.SEALED_ARTIFACT_DATA);
        if (data == null || data.abilities().isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }

        // Get the currently selected ability
        int selectedIndex = stack.getOrDefault(ModDataComponents.SEALED_ARTIFACT_SELECTED, 0);

        Ability ability = data.abilities().get(selectedIndex);

        // Use the ability
        ability.useAbility((ServerLevel) level, player, false, false, true);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        SealedArtifactData data = stack.get(ModDataComponents.SEALED_ARTIFACT_DATA);
        if (data == null) {
            return;
        }

        // Show pathway and sequence
        tooltipComponents.add(Component.translatable("lotm.pathway")
                .append(": ")
                .append(Component.translatable("lotm.pathway." + data.pathway()))
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        tooltipComponents.add(Component.translatable("lotm.sequence")
                .append(": " + data.sequence())
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        tooltipComponents.add(Component.empty());

        // Show selected ability
        int selectedIndex = stack.getOrDefault(ModDataComponents.SEALED_ARTIFACT_SELECTED, 0);
        Ability ability = data.abilities().get(selectedIndex);

        tooltipComponents.add(Component.literal("Selected Ability")
                .append(": ")
                .append(Component.translatable("lotmcraft." + ability.getId()))
                .withStyle(ChatFormatting.AQUA));

//        // show sub abilities only if the ability is selectable ability
//        if(ability instanceof SelectableAbility selectableAbility) {
//            tooltipComponents.add(Component.literal("Sub ability")
//                    .append(": ")
//                    .append(Component.translatable(selectableAbility.getSelectedAbility(player)))
//                    .withStyle(ChatFormatting.AQUA));
//        }
        tooltipComponents.add(Component.empty());
//        if (!itemInAnvilOutputSlot(player,stack)) {
//
//        }

        // Show abilities
        tooltipComponents.add(Component.translatable("lotm.sealed_artifact.abilities")
                .append(":")
                .withStyle(ChatFormatting.AQUA)); // aqua = soft blue highlight

        for (int i = 0; i < data.abilities().size(); i++) {
            Ability foundAbility = data.abilities().get(i);
            Component abilityName = Component.translatable("lotmcraft." + foundAbility.getId());

            tooltipComponents.add(
                    Component.literal("  → ")
                            .withStyle(ChatFormatting.AQUA)
                            .append(abilityName.copy().withStyle(ChatFormatting.AQUA))
            );
        }

        tooltipComponents.add(Component.empty());

        // Show negative effect
        tooltipComponents.add(Component.translatable("lotm.sealed_artifact.negative_effect")
                .append(":")
                .withStyle(ChatFormatting.DARK_PURPLE));

        for (NegativeEffect effect : data.negativeEffect()) {
            tooltipComponents.add(Component.literal("  - ")
                    .append(effect.getDisplayName())
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    private boolean itemInAnvilOutputSlot(Player player, ItemStack stack){
        if (player != null && player.containerMenu instanceof AnvilMenu anvil) {
            if (anvil.getSlot(2).getItem() == stack) {
                return true;
            }
        }
        return false;
    }


    @Override
    public @NotNull Component getName(ItemStack stack) {
        SealedArtifactData data = stack.get(ModDataComponents.SEALED_ARTIFACT_DATA);
        if (data == null) {
            return Component.translatable(this.getDescriptionId(stack));
        }

        String baseType = stack.getOrDefault(ModDataComponents.SEALED_ARTIFACT_BASE_TYPE, "item");
        String pathway = data.pathway();
        
        // Fall back to generic name if no specific translation exists
        return Component.translatable("lotm.sealed_artifact.generic", 
                Component.translatable("lotm.sealed_artifact.type." + baseType),
                Component.translatable("lotm.sealed_artifact.pathway." + pathway + "_1"))
                .withStyle(ChatFormatting.DARK_PURPLE);
    }

    /**
     * Switches to the next ability in the sealed artifact
     */
    public static void switchAbility(ItemStack stack) {
        SealedArtifactData data = stack.get(ModDataComponents.SEALED_ARTIFACT_DATA);
        if (data == null || data.abilities().size() <= 1) {
            return;
        }

        int current = stack.getOrDefault(ModDataComponents.SEALED_ARTIFACT_SELECTED, 0);
        int next = (current + 1) % data.abilities().size();
        stack.set(ModDataComponents.SEALED_ARTIFACT_SELECTED, next);
    }
}