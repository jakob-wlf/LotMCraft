package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BlockBreakingEventHandler {

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();

        if(!BeyonderData.isBeyonder(player)) return;

        ItemStack fakeTool = getToolMapped(BeyonderData.getSequence(player));

        if (!fakeTool.isEmpty()) {
            float speed = fakeTool.getDestroySpeed(event.getState());
            event.setNewSpeed(speed);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();

        if(!BeyonderData.isBeyonder(player)) return;

        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        ItemStack fakeTool = getToolMapped(BeyonderData.getSequence(player));

        if (fakeTool.isEmpty()) return;

        ItemStack held = player.getMainHandItem();

        if (!held.isCorrectToolForDrops(state)
                && fakeTool.isCorrectToolForDrops(state)) {

            event.setCanceled(true);

            if (!level.isClientSide) {

                BlockEntity blockEntity = level.getBlockEntity(pos);

                LootParams.Builder builder = new LootParams.Builder((ServerLevel) level)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.TOOL, fakeTool)
                        .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
                        .withParameter(LootContextParams.THIS_ENTITY, player);

                List<ItemStack> drops = state.getDrops(builder);

                for (ItemStack drop : drops) {
                    Block.popResource(level, pos, drop);
                }

                level.destroyBlock(pos, false);
            }
        }
    }

    private static ItemStack getToolMapped(int seq){
        return switch (seq){
          case 4 -> new ItemStack(Items.WOODEN_PICKAXE);
          case 3 -> new ItemStack(Items.STONE_PICKAXE);
          case 2 -> new ItemStack(Items.IRON_PICKAXE);
          case 1 -> new ItemStack(Items.DIAMOND_PICKAXE);
          case 0 -> new ItemStack(Items.NETHERITE_PICKAXE);
            default -> ItemStack.EMPTY;
        };
    }

}
