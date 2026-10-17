package de.jakob.lotm.beyonders.abilities.abyss.contract;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public class ItemNameResolver {
    public static @Nullable Item resolve(String rawName) {
        String normalized = rawName.trim().toLowerCase().replace(" ", "_");
        ResourceLocation direct = ResourceLocation.tryParse(normalized);
        if (direct != null && BuiltInRegistries.ITEM.containsKey(direct)) {return BuiltInRegistries.ITEM.get(direct);}
        Item vanillaMatch = null;
        Item anyMatch = null;
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            ResourceLocation id = entry.getKey().location();
            if (id.getPath().equals(normalized)) {
                if (id.getNamespace().equals("minecraft")) { vanillaMatch = entry.getValue(); break; }
                if (anyMatch == null) anyMatch = entry.getValue();
            }
        }
        return vanillaMatch != null ? vanillaMatch : anyMatch;
    }
}