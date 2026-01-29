package de.jakob.lotm.abilities.error;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.item.custom.SunItem;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConceptualTheftAbility extends SelectableAbility {
    public ConceptualTheftAbility(String id) {
        super(id, 25);

        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 2000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.conceptual_theft.day_night", "ability.lotmcraft.conceptual_theft.area"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        switch (abilityIndex) {
            case 0 -> stealDayNight(serverLevel, entity);
            case 1 -> stealArea(serverLevel, entity);
        }
    }

    private void stealDayNight(ServerLevel serverLevel, LivingEntity entity) {
        boolean isDay = serverLevel.isDay();

        setDayNight(serverLevel, isDay);

        if(!(entity instanceof Player player)) {
            return;
        }

        if(isDay) {
            player.addItem(new ItemStack(ModItems.SUN_ITEM.get()));
        }
        else {
            player.addItem(new ItemStack(ModItems.MOON_ITEM.get()));
        }
    }

    private static void setDayNight(ServerLevel serverLevel, boolean isDay) {
        long currentTime = serverLevel.getDayTime();
        long currentDayTime = currentTime % 24000;

        long targetTime;
        if (isDay) {
            // Advance to midnight (18000 ticks)
            targetTime = 18000;
        } else {
            // Advance to noon (6000 ticks)
            targetTime = 6000;
        }

        // Calculate ticks to add to reach target time
        long ticksToAdd;
        if (currentDayTime <= targetTime) {
            // Target time hasn't occurred yet today
            ticksToAdd = targetTime - currentDayTime;
        } else {
            // Target time has already passed, advance to target time tomorrow
            ticksToAdd = (24000 - currentDayTime) + targetTime;
        }

        serverLevel.setDayTime(currentTime + ticksToAdd);
    }

    private void stealArea(ServerLevel serverLevel, LivingEntity entity) {
        if(!BeyonderData.isGriefingEnabled(entity)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.petrification.griefing_disabled").withColor(0xa0e2fa));
            return;
        }

        Vec3 targetLocation = AbilityUtil.getTargetLocation(entity, 50, 0.1f);

        List<BlockPos> blocks = AbilityUtil.getBlocksInEllipsoid(
                serverLevel,
                targetLocation,
                24,
                9,
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
