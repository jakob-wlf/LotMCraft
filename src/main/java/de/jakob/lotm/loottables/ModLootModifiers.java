package de.jakob.lotm.loottables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import de.jakob.lotm.LOTMCraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, LOTMCraft.MOD_ID);

    public static final Supplier<MapCodec<ChestLootModifier>> CHEST_LOOT =
            LOOT_MODIFIERS.register("chest_loot", ChestLootModifier.CODEC);

    public static final Supplier<MapCodec<DoubleLootModifier>> DOUBLE_LOOT =
            LOOT_MODIFIERS.register("double_loot", () -> DoubleLootModifier.CODEC);

    public static void register(IEventBus modEventBus) {
        LOOT_MODIFIERS.register(modEventBus);
    }
}