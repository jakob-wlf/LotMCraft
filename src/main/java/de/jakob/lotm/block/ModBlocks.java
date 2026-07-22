package de.jakob.lotm.block;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.block.custom.BrewingCauldronBlock;
import de.jakob.lotm.block.custom.MysticalRingBlock;
import de.jakob.lotm.block.custom.RealityPortalBlock;
import de.jakob.lotm.block.custom.VoidBlock;
import de.jakob.lotm.fluid.ModFluids;
import de.jakob.lotm.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
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

    public static final DeferredBlock<MysticalRingBlock> MYSTICAL_RING = BLOCKS.register("mystical_ring",
            () -> new MysticalRingBlock(BlockBehaviour.Properties.of()
                    .strength(0.1f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .lightLevel(state -> 7)
                    .isViewBlocking((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)
            )
    );

    public static final DeferredBlock<LiquidBlock> DROPS_OF_ETERNAL_DARKNESS = registerBlockWithoutItem(
            "drops_of_eternal_darkness",
            () -> new LiquidBlock(ModFluids.DROPS_OF_ETERNAL_DARKNESS_SOURCE.get(),
                    BlockBehaviour.Properties.of()
                            .noOcclusion()
                            .noCollission()
                            .noLootTable()
                            .replaceable()
                            .liquid()
                            .strength(100.0f)
                            .sound(SoundType.EMPTY)
                            .isSuffocating((state, level, pos) -> false)
                            .isViewBlocking((state, level, pos) -> false))
    );

    public static final DeferredBlock<Block> REALITY_PORTAL = registerBlock("reality_portal",
            () -> new RealityPortalBlock(BlockBehaviour.Properties.of()
                    .noLootTable()
                    .noOcclusion()
                    .noCollission()
                    .isSuffocating((state, world, pos) -> false)
                    .isViewBlocking((state, world, pos) -> false)
                    .strength(-1.0f, 3600000.0F)
            ));

    public static final DeferredBlock<Block> SOLID_VOID = registerBlock("solid_void_block",
            () -> new VoidBlock(BlockBehaviour.Properties.of()
                    .noLootTable()
                    .strength(-1.0f, 3600000.0F)
            ));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

        private static <T extends Block> DeferredBlock<T> registerBlockWithoutItem(String name, Supplier<T> block) {
                return BLOCKS.register(name, block);
        }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
