package de.jakob.lotm.data;

import com.mojang.serialization.Codec;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, LOTMCraft.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> IS_RECORDED =
            DATA_COMPONENT_TYPES.register("is_recorded", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> IS_REPLICATED =
            DATA_COMPONENT_TYPES.register("is_replicated", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .build());


    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
