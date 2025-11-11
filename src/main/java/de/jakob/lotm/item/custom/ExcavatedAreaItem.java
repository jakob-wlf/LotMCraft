package de.jakob.lotm.item.custom;

import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExcavatedAreaItem extends Item {

    public ExcavatedAreaItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(itemStack);
        }

        // Get stored block data
        Map<String, String> blockData = itemStack.getOrDefault(ModDataComponents.EXCAVATED_BLOCKS, Map.of());
        String centerStr = itemStack.getOrDefault(ModDataComponents.EXCAVATION_CENTER, "");

        if (blockData.isEmpty() || centerStr.isEmpty()) {
            player.displayClientMessage(Component.literal("No area data found!").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(itemStack);
        }

        ServerLevel serverLevel = (ServerLevel) level;

        // Calculate new center position (where player is looking)
        Vec3 targetLocation = AbilityUtil.getTargetBlock(player, 20, true).getCenter();

        // Parse original center
        String[] originalCenter = centerStr.split(",");
        double origX = Double.parseDouble(originalCenter[0]);
        double origY = Double.parseDouble(originalCenter[1]);
        double origZ = Double.parseDouble(originalCenter[2]);

        // Find minimum Y of saved structure relative to center
        double minYOffset = Double.MAX_VALUE;
        for (String key : blockData.keySet()) {
            String[] pos = key.split(",");
            double y = Double.parseDouble(pos[1]);
            double offsetY = y - origY;
            if (offsetY < minYOffset) {
                minYOffset = offsetY;
            }
        }


        for (Map.Entry<String, String> entry : blockData.entrySet()) {
            String[] pos = entry.getKey().split(",");
            int x = Integer.parseInt(pos[0]);
            int y = Integer.parseInt(pos[1]);
            int z = Integer.parseInt(pos[2]);

            // Calculate offset from original center
            double offsetX = x - origX;
            double offsetY = y - origY;
            double offsetZ = z - origZ;

            // Adjust so the bottom aligns with the target block top
            BlockPos newPos = new BlockPos(
                    (int) (targetLocation.x + offsetX),
                    (int) (targetLocation.y + (offsetY - minYOffset)),
                    (int) (targetLocation.z + offsetZ)
            );

            try {
                BlockState state = parseBlockState(entry.getValue());
                if (state != null && !state.isAir()) {
                    BlockState currentState = serverLevel.getBlockState(newPos);
                    if (currentState.isAir() || currentState.canBeReplaced()) {
                        serverLevel.setBlock(newPos, state, 3);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to place block at " + newPos + ": " + e.getMessage());
            }
        }


        // Consume the item
        if (!player.isCreative()) {
            itemStack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, false);
    }

    private BlockState parseBlockState(String stateString) {
        try {
            // BlockState.toString() format is typically: "Block{minecraft:stone}" or similar
            // Let's handle the actual format from Minecraft

            // Remove "Block{" prefix and "}" suffix
            String cleaned = stateString;
            if (cleaned.startsWith("Block{")) {
                cleaned = cleaned.substring(6, cleaned.length() - 1);
            }

            // Split by '[' to separate block ID from properties
            String[] parts = cleaned.split("\\[");
            String blockId = parts[0];

            // Get block from registry
            ResourceLocation location = ResourceLocation.tryParse(blockId);
            if (location == null) {
                return null;
            }

            Optional<Block> blockOpt = BuiltInRegistries.BLOCK.getOptional(location);
            if (blockOpt.isPresent()) {
                Block block = blockOpt.get();
                BlockState state = block.defaultBlockState();

                // Parse properties if present
                if (parts.length > 1 && parts[1].endsWith("]")) {
                    String propertiesStr = parts[1].substring(0, parts[1].length() - 1);
                    String[] properties = propertiesStr.split(",");

                    for (String prop : properties) {
                        String[] keyValue = prop.split("=");
                        if (keyValue.length == 2) {
                            try {
                                state = parseProperty(state, keyValue[0], keyValue[1]);
                            } catch (Exception ignored) {
                                // Skip invalid properties
                            }
                        }
                    }
                }

                return state;
            }
        } catch (Exception e) {
            // Debug: print the error
            System.err.println("Failed to parse block state: " + stateString + " - " + e.getMessage());
        }

        return null;
    }

    private BlockState parseProperty(BlockState state, String propertyName, String value) {
        for (net.minecraft.world.level.block.state.properties.Property<?> property : state.getProperties()) {
            if (property.getName().equals(propertyName)) {
                return setPropertyValue(state, property, value);
            }
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> BlockState setPropertyValue(BlockState state, net.minecraft.world.level.block.state.properties.Property<T> property, String value) {
        Optional<T> optional = property.getValue(value);
        if (optional.isPresent()) {
            return state.setValue(property, optional.get());
        }
        return state;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        Map<String, String> blockData = stack.getOrDefault(ModDataComponents.EXCAVATED_BLOCKS, Map.of());

        if (!blockData.isEmpty()) {
            tooltipComponents.add(Component.literal("Blocks: " + blockData.size()).withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.literal("Right-click to place").withStyle(ChatFormatting.GOLD));
        } else {
            tooltipComponents.add(Component.literal("Empty excavation").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}