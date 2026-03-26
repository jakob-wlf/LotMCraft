package de.jakob.lotm.artifacts;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

        // Apply Use-Only Negative Effects
        for (NegativeEffect effect : data.negativeEffect()) {
            if (NegativeEffect.useOnlyTick.contains(effect.getType())) {
                effect.apply(player, true, List.of(data.pathway()));
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        SealedArtifactData data = stack.get(ModDataComponents.SEALED_ARTIFACT_DATA);
        if (data == null) return;

        int selectedIndex = stack.getOrDefault(ModDataComponents.SEALED_ARTIFACT_SELECTED, 0);
        Ability selectedAbility = data.abilities().get(selectedIndex);

        addDivider(tooltipComponents);
        addPathwayInfo(tooltipComponents, data);
        addSelectedAbility(tooltipComponents, selectedAbility);
        addDivider(tooltipComponents);
        addAbilityList(tooltipComponents, data);
        addDivider(tooltipComponents);
        addNegativeEffects(tooltipComponents, data);
    }

// ── Sections ────────────────────────────────────────────────

    private void addPathwayInfo(List<Component> tooltip, SealedArtifactData data) {
        tooltip.add(
                label("lotm.pathway", ChatFormatting.LIGHT_PURPLE)
                        .append(value("lotm.pathway." + data.pathway(), ChatFormatting.WHITE))
        );
        tooltip.add(
                label("lotm.sequence", ChatFormatting.LIGHT_PURPLE)
                        .append(Component.literal(String.valueOf(data.sequence())).withStyle(ChatFormatting.WHITE))
        );
    }

    private void addSelectedAbility(List<Component> tooltip, Ability ability) {
        tooltip.add(Component.empty());
        tooltip.add(
                Component.literal("✦ ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.translatable("lotm.sealed_artifact.selected_ability")
                                .withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(" › ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.translatable("lotmcraft." + ability.getId())
                                .withStyle(ChatFormatting.AQUA))
        );
        tooltip.add(Component.empty());
    }

    private void addAbilityList(List<Component> tooltip, SealedArtifactData data) {
        tooltip.add(sectionHeader("lotm.sealed_artifact.abilities", ChatFormatting.AQUA));

        for (Ability ability : data.abilities()) {
            tooltip.add(
                    Component.literal("  ▸ ")
                            .withStyle(ChatFormatting.DARK_AQUA)
                            .append(Component.translatable("lotmcraft." + ability.getId())
                                    .withStyle(ChatFormatting.AQUA))
            );
        }
    }

    private void addNegativeEffects(List<Component> tooltip, SealedArtifactData data) {
        tooltip.add(sectionHeader("lotm.sealed_artifact.negative_effect", ChatFormatting.DARK_PURPLE));

        for (NegativeEffect effect : data.negativeEffect()) {
            tooltip.add(
                    Component.literal("  ▸ ")
                            .withStyle(ChatFormatting.DARK_PURPLE)
                            .append(effect.getDisplayName().copy().withStyle(ChatFormatting.LIGHT_PURPLE))
            );
        }
    }

    // ── Tooltip Helpers ─────────────────────────────────────────────────

    private static void addDivider(List<Component> tooltip) {
        tooltip.add(
                Component.literal("─────────────────")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );
    }

    private static MutableComponent sectionHeader(String key, ChatFormatting color) {
        return Component.literal("◆ ")
                .withStyle(color)
                .append(Component.translatable(key).withStyle(color));
    }

    private static MutableComponent label(String key, ChatFormatting color) {
        return Component.translatable(key)
                .withStyle(color)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static MutableComponent value(String key, ChatFormatting color) {
        return Component.translatable(key).withStyle(color);
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

        int color = BeyonderData.pathwayInfos.get(pathway).color();
        
        // Fall back to generic name if no specific translation exists
        return Component.translatable("lotm.sealed_artifact.generic", 
                Component.translatable("lotm.sealed_artifact.type." + baseType),
                Component.translatable("lotm.sealed_artifact.pathway." + pathway + "_1"))
                .withColor(color);
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