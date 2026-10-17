package de.jakob.lotm.titles;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class AreaTitleManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static volatile Map<ResourceLocation, AreaTitle> ALL = Map.of();
    private static volatile Map<ResourceLocation, Map.Entry<ResourceLocation, AreaTitle>> BY_DIMENSION = Map.of();
    private static volatile Map<ResourceLocation, AreaTitle> STRUCTURES = Map.of();

    public AreaTitleManager() { super(new Gson(), "area_titles"); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager rm, ProfilerFiller profiler) {
        Map<ResourceLocation, AreaTitle> all = new HashMap<>();
        Map<ResourceLocation, Map.Entry<ResourceLocation, AreaTitle>> dims = new HashMap<>();
        Map<ResourceLocation, AreaTitle> structs = new HashMap<>();

        files.forEach((id, json) -> AreaTitle.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, json)
                .resultOrPartial(err -> LOGGER.error("Bad area title {}: {}", id, err))
                .ifPresent(def -> {
                    System.out.println("loaded " + id);
                    all.put(id, def);
                    if (def.trigger() == AreaTitle.Trigger.DIMENSION) dims.put(def.target(), Map.entry(id, def));
                    else structs.put(id, def);
                }));

        ALL = Map.copyOf(all);
        BY_DIMENSION = Map.copyOf(dims);
        STRUCTURES = Map.copyOf(structs);
        LOGGER.info("Loaded {} area titles", all.size());
    }

    public static Map.Entry<ResourceLocation, AreaTitle> forDimension(ResourceLocation dim) { return BY_DIMENSION.get(dim); }
    public static Map<ResourceLocation, AreaTitle> structureTitles() { return STRUCTURES; }
    public static Map<ResourceLocation, AreaTitle> all() { return ALL; }
}
