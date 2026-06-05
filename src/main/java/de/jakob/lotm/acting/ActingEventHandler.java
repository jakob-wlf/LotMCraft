package de.jakob.lotm.acting;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ActingEventHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();
        BlockPos pos = event.getPos();
        Level level = event.getLevel();

        BlockState clickedBlock = level.getBlockState(pos);

        // Crop Planting
        if (clickedBlock.is(Blocks.FARMLAND) && item.getItem() instanceof BlockItem blockItem) {
            Block placedBlock = blockItem.getBlock();
            if (placedBlock instanceof CropBlock) {
                ActingHandler.onActingEvent(player, "plant_crop");
            }
        }
    }

}
