package de.jakob.lotm.util;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<List<? extends String>> BANNED_BIOMES = BUILDER
            .comment("A list of biomes to ban npc spawn.")
            .defineListAllowEmpty("banned_biomes", List.of("minecraft:mushroom_fields", "minecraft:deep_dark"),
                    obj -> obj instanceof String && ((String) obj).contains(":"));

    private static final ModConfigSpec.ConfigValue<List<? extends String>> BANNED_SUMMONED_ITEMS = BUILDER
            .comment("A list of items to ban from being summoned by historical summon.")
            .defineListAllowEmpty("banned_summoned_items", List.of(),
                    obj -> obj instanceof String && ((String) obj).contains(":"));


    public static final ModConfigSpec SPEC = BUILDER.build();

    public static Set<ResourceLocation> biomes;
    public static Set<ResourceLocation> items;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        biomes = Config.BANNED_BIOMES.get().stream()
                .map(ResourceLocation::parse)
                .collect(Collectors.toSet());

        items = Config.BANNED_SUMMONED_ITEMS.get().stream()
                .map(ResourceLocation::parse)
                .collect(Collectors.toSet());
    }
}
