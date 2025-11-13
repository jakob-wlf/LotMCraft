package de.jakob.lotm.block;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.block.custom.BrewingCauldronBlock;
import de.jakob.lotm.block.custom.VoidBlock;
import de.jakob.lotm.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(LOTMCraft.MOD_ID);

    public static final DeferredBlock<Block> BREWING_CAULDRON = registerBlock("brewing_cauldron",
            () -> new BrewingCauldronBlock(BlockBehaviour.Properties.of()
                    .strength(2.0f)
                    .noOcclusion()
                    .sound(SoundType.METAL)
            ));

    public static final DeferredBlock<Block> VOID = registerBlock("void_block",
            () -> new VoidBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .noCollission()
                    .isSuffocating((state, world, pos) -> false)
                    .isViewBlocking((state, world, pos) -> false)
                    .noLootTable()
                    .strength(-1.0f, 3600000.0F)
            ));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
