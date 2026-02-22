package de.jakob.lotm.block;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.block.custom.BrewingCauldronBlockEntity;
import de.jakob.lotm.block.custom.MysticalRingBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LOTMCraft.MOD_ID);

    public static final Supplier<BlockEntityType<BrewingCauldronBlockEntity>> BREWING_BLOCK_BE =
            BLOCK_ENTITIES.register("brewing_block_be", () -> BlockEntityType.Builder.of(
                    BrewingCauldronBlockEntity::new, ModBlocks.BREWING_CAULDRON.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MysticalRingBlockEntity>> MYSTICAL_RING_BE =
            BLOCK_ENTITIES.register("mystical_ring_be", () ->
                    BlockEntityType.Builder.of(MysticalRingBlockEntity::new,
                            ModBlocks.MYSTICAL_RING.get()).build(null));
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}