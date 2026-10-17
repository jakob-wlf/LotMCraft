package de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Shapeshifter;

import de.jakob.lotm.util.shapeShifting.ShapeShiftingUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * One-time use. Right click to transform into the shape stored on the mask.
 * The shape string uses the same format as ShapeShiftingUtil:
 *   "minecraft:zombie", or "player:Name:UUID" for players.
 * Does not need the Shapeshifter toggle.
 */
public class SkinMaskItem extends Item {

    private static final String SHAPE_KEY = "mask_shape";
    private static final String UPGRADED_KEY = "mask_upgraded";
    public SkinMaskItem(Properties properties) {
        super(properties.stacksTo(16));
    }
    public static ItemStack create(Item maskItem, LivingEntity victim, boolean upgraded) {
        ItemStack stack = new ItemStack(maskItem);
        CompoundTag tag = new CompoundTag();
        tag.putString(SHAPE_KEY, ShapeShiftingUtil.getEntityTypeString(victim));
        tag.putBoolean(UPGRADED_KEY, upgraded);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }
    @Nullable
    public static String getShape(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        String shape = data.copyTag().getString(SHAPE_KEY);
        return shape.isEmpty() ? null : shape;
    }
    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        String shape = getShape(stack);
        if (shape == null) return super.getName(stack);
        if (shape.startsWith("player:")) {
            String[] parts = shape.split(":");
            return Component.literal("Skin Mask: " + (parts.length > 1 ? parts[1] : "Player"));
        }
        ResourceLocation id = ResourceLocation.tryParse(shape);
        if (id != null) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
            if (type != null) {
                return Component.literal("Skin Mask: ").append(type.getDescription());
            }
        }
        return super.getName(stack);
    }
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {return InteractionResultHolder.success(stack);}
        String shape = getShape(stack);
        if (shape == null) {return InteractionResultHolder.fail(stack);}
        // sequenceRestrict = false skips the size/sequence checks and applies the shape directly
        ShapeShiftingUtil.shapeShift(serverPlayer, shape, false);
        serverPlayer.getPersistentData().putBoolean(BloodlineSkinshifterAbility.MASK_FORM_TAG, true);
        serverPlayer.getPersistentData().putBoolean(BloodlineSkinshifterAbility.MASK_UPGRADED_TAG, isUpgraded(stack));
        serverPlayer.getPersistentData().putFloat(BloodlineSkinshifterAbility.MASK_DAMAGE_TAG, 0f);
        serverPlayer.displayClientMessage(Component.literal("You put on the skin mask."), true);
        // One-time use
        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.consume(stack);
    }
    public static boolean isUpgraded(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBoolean(UPGRADED_KEY);
    }
}