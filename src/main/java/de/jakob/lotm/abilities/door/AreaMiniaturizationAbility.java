package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AreaMiniaturizationAbility extends AbilityItem {

    private static final double RADIUS = 20.0;
    private static final double Y_RADIUS = 9.0;

    public AreaMiniaturizationAbility(Properties properties) {
        super(properties, 2);

        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1200f;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 targetLocation = AbilityUtil.getTargetLocation(entity, 50, 0.1f);

        List<BlockPos> blocks = AbilityUtil.getBlocksInEllipsoid(
                serverLevel,
                targetLocation,
                RADIUS,
                Y_RADIUS,
                true,
                true,
                false
        );

        // Store block data
        Map<String, String> blockData = new HashMap<>();
        int blockCount = 0;

        for (BlockPos pos : blocks) {
            BlockState state = serverLevel.getBlockState(pos);

            // Skip air and bedrock
            if (state.isAir() || state.is(Blocks.BEDROCK)) {
                continue;
            }

            // Store block state using registry name and properties
            String posKey = pos.getX() + "," + pos.getY() + "," + pos.getZ();

            // Get the block's registry name
            String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();

            // Get properties as string
            StringBuilder stateStr = new StringBuilder(blockId);
            if (!state.getProperties().isEmpty()) {
                stateStr.append("[");
                boolean first = true;
                for (var property : state.getProperties()) {
                    if (!first) stateStr.append(",");
                    first = false;
                    stateStr.append(property.getName()).append("=").append(state.getValue(property));
                }
                stateStr.append("]");
            }

            blockData.put(posKey, stateStr.toString());

            // Remove the block
            serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            blockCount++;

            // Limit to prevent performance issues
            if (blockCount >= 10000) break;
        }

        // Create the excavated area item
        ItemStack excavatedArea = new ItemStack(ModItems.EXCAVATED_AREA_ITEM.get());
        excavatedArea.set(ModDataComponents.EXCAVATED_BLOCKS, blockData);
        excavatedArea.set(ModDataComponents.EXCAVATION_CENTER,
                targetLocation.x + "," + targetLocation.y + "," + targetLocation.z);

        // Give item to player
        if (entity instanceof net.minecraft.world.entity.player.Player player) {
            if (!player.getInventory().add(excavatedArea)) {
                player.drop(excavatedArea, false);
            }
        }
    }
}