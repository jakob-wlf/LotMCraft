package de.jakob.lotm.villager;

import com.google.common.collect.ImmutableSet;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.block.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModVillagers {

    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, LOTMCraft.MOD_ID);

    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, LOTMCraft.MOD_ID);

    public static final Holder<PoiType> BEYONDER_POI = POI_TYPES.register("beyonder_poi", () -> new PoiType(ImmutableSet.copyOf(ModBlocks.BREWING_CAULDRON.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final Holder<PoiType> EVERNIGHT_POI = POI_TYPES.register("evernight_poi", () -> new PoiType(ImmutableSet.copyOf(Blocks.RED_CANDLE.getStateDefinition().getPossibleStates()), 1, 1));
    public static final Holder<VillagerProfession> BEYONDER_PROFESSION = VILLAGER_PROFESSIONS
            .register("beyonder_merchant", () -> new VillagerProfession(
                    "beyonder_merchant",
                    (holder) -> holder.value() == BEYONDER_POI.value(),
                    (poiHolder) -> poiHolder.value() == BEYONDER_POI.value(),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.ENCHANTMENT_TABLE_USE));

    public static final Holder<VillagerProfession> EVERNIGHT_PROFESSION = VILLAGER_PROFESSIONS
            .register("evernight_clergyman", () -> new VillagerProfession(
                    "evernight_clergymant",
                    (holder) -> holder.value() == EVERNIGHT_POI.value(),
                    (poiHolder) -> poiHolder.value() == EVERNIGHT_POI.value(),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.ENCHANTMENT_TABLE_USE));

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
        VILLAGER_PROFESSIONS.register(eventBus);
    }

}
