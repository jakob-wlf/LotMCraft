package de.jakob.lotm.beyonders.artifacts;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.attachments.DoorAuthorityData;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

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

        if(!level.getGameRules().getBoolean(ModGameRules.ALLOW_ARTIFACTS)){
            player.setItemInHand(hand, ItemStack.EMPTY);

            return InteractionResultHolder.success(ItemStack.EMPTY);
        }

        DoorAuthorityData doorData = DoorAuthorityData.get((ServerLevel) level);
        if (doorData.isActive() && doorData.getEffectId().equalsIgnoreCase("strengthen")) {
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.END_ROD, player.getEyePosition(), 40, .5, .05);
            return InteractionResultHolder.fail(stack);
        }

        SealedArtifactData data = stack.get(ModDataComponents.SEALED_ARTIFACT_DATA);
        if (data == null || data.abilities().isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }

        // Get the currently selected ability
        int selectedIndex = stack.getOrDefault(ModDataComponents.SEALED_ARTIFACT_SELECTED, 0);

        Ability ability = data.abilities().get(selectedIndex);

        AbilityUtil.setArtifactScaling(player, data.pathway(), data.sequence());

        // Use the ability
        ability.useAbility((ServerLevel) level, player, true, false, true);

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
        int pathwayColor = BeyonderData.pathwayInfos.get(data.pathway()).color();

        addDivider(tooltipComponents);
        addPathwayInfo(tooltipComponents, data, pathwayColor);
        addSelectedAbility(tooltipComponents, selectedAbility, pathwayColor);
        addDivider(tooltipComponents);
        addAbilityList(tooltipComponents, data, pathwayColor);
        addDivider(tooltipComponents);
        addNegativeEffects(tooltipComponents, data);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected){
        if (level.isClientSide) return;

        if (!(entity instanceof ServerPlayer player)) return;

        boolean generated = stack.getOrDefault(ModDataComponents.SEALED_ARTIFACT_GENERATED, false);
        boolean failed = stack.getOrDefault(ModDataComponents.SEALED_ARTIFACT_GENERATED_FAILED, false);
        if (generated) return;

        String baseType = stack.get(ModDataComponents.SEALED_ARTIFACT_BASE_TYPE);
        Integer sequence = stack.get(ModDataComponents.SEALED_ARTIFACT_GENERATED_SEQ);
        String path = stack.get(ModDataComponents.SEALED_ARTIFACT_GENERATED_PATH);

        if(baseType == null || sequence == null || path == null) return;

        if(!failed){
            SealedArtifactData data = SealedArtifactHandler.createSealedArtifactData(path, sequence, baseType);

            stack.set(ModDataComponents.SEALED_ARTIFACT_DATA, data);
            stack.set(ModDataComponents.SEALED_ARTIFACT_GENERATED, true);
        }
        else{
            ItemStack newStack = new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler
                    .selectCharacteristicOfPathwayAndSequence(path, sequence)));

            level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_PLACE, player.getSoundSource(), 1.0f, 1.0f);
            player.closeContainer();
            Vec3 explosionPos = player.position().add(0, player.getBbHeight() / 2, 0);
            ServerScheduler.scheduleDelayed(10, () -> EffectManager.playEffect(EffectManager.Effect.ARTIFACT_EXPLOSION, explosionPos.x, explosionPos.y, explosionPos.z, (ServerLevel) level));
            player.setDeltaMovement(new Vec3(level.random.nextDouble(), .65, level.random.nextDouble()).normalize());
            player.hurtMarked = true;
            player.getInventory().setItem(slot, newStack);
        }

    }

    // ── Sections ────────────────────────────────────────────────

    private void addPathwayInfo(List<Component> tooltip, SealedArtifactData data, int pathwayColor) {
        tooltip.add(
                label("lotm.pathway")
                        .append(Component.translatable("lotm.pathway." + data.pathway()).withColor(pathwayColor))
        );
        tooltip.add(
                label("lotm.sequence")
                        .append(Component.literal(String.valueOf(data.sequence())).withColor(pathwayColor))
        );
    }

    private void addSelectedAbility(List<Component> tooltip, Ability ability, int pathwayColor) {
        tooltip.add(Component.empty());
        tooltip.add(
                Component.literal("✦ ").withColor(pathwayColor)
                        .append(Component.translatable("lotm.sealed_artifact.selected_ability")
                                .withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(" › ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.translatable("lotmcraft." + ability.getId())
                                .withStyle(ChatFormatting.WHITE))
        );
        tooltip.add(Component.empty());
    }

    private void addAbilityList(List<Component> tooltip, SealedArtifactData data, int pathwayColor) {
        tooltip.add(sectionHeader("lotm.sealed_artifact.abilities", pathwayColor));

        for (Ability ability : data.abilities()) {
            tooltip.add(
                    Component.literal("  ▸ ").withColor(pathwayColor)
                            .append(Component.translatable("lotmcraft." + ability.getId())
                                    .withStyle(ChatFormatting.WHITE))
            );
        }
    }

    private void addNegativeEffects(List<Component> tooltip, SealedArtifactData data) {
        tooltip.add(sectionHeader("lotm.sealed_artifact.negative_effect", ChatFormatting.DARK_RED));

        for (NegativeEffect effect : data.negativeEffect()) {
            tooltip.add(
                    Component.literal("  ▸ ").withStyle(ChatFormatting.DARK_RED)
                            .append(effect.getDisplayName().copy().withStyle(ChatFormatting.RED))
            );
        }
    }

    // ── Tooltip Helpers ─────────────────────────────────────────────────

    private static void addDivider(List<Component> tooltip) {
        tooltip.add(
                Component.literal("─────────────────")
                        .withStyle(ChatFormatting.DARK_GRAY)
                        .withStyle(ChatFormatting.BOLD)
        );
    }

    private static MutableComponent sectionHeader(String key, int color) {
        return Component.literal("◆ ").withColor(color)
                .append(Component.translatable(key).withStyle(ChatFormatting.GRAY));
    }

    private static MutableComponent sectionHeader(String key, ChatFormatting color) {
        return Component.literal("◆ ").withStyle(color)
                .append(Component.translatable(key).withStyle(ChatFormatting.GRAY));
    }

    private static MutableComponent label(String key) {
        return Component.translatable(key)
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY));
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