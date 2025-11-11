package de.jakob.lotm.data;

import com.mojang.serialization.Codec;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;
import java.util.function.Supplier;

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

    public static final Supplier<DataComponentType<Map<String, String>>> EXCAVATED_BLOCKS =
            DATA_COMPONENT_TYPES.register("excavated_blocks",
                    () -> DataComponentType.<Map<String, String>>builder()
                            .persistent(Codec.unboundedMap(Codec.STRING, Codec.STRING))
                            .networkSynchronized(ByteBufCodecs.map(
                                    java.util.HashMap::new,
                                    ByteBufCodecs.STRING_UTF8,
                                    ByteBufCodecs.STRING_UTF8
                            ))
                            .build()
            );

    // Data component for storing the center position of excavation
    public static final Supplier<DataComponentType<String>> EXCAVATION_CENTER =
            DATA_COMPONENT_TYPES.register("excavation_center",
                    () -> DataComponentType.<String>builder()
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                            .build()
            );


    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
