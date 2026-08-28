package de.jakob.lotm.beyonders.rituals;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record  RitualRecipe(
        String id,
        String discoveredFrom,
        String candle,
        List<Sacrifice> sacrifices,
        Honorific honorific,
        Conditions conditions,
        ParticleColor particleColor,
        String bookColor,
        int durationTicks,
        Result result
) {

    public record Sacrifice(String item, int count) {}

    public record Honorific(List<String> lines) {}

    public record Conditions(
            int minSequence
    ) {}

    public record ParticleColor(
            float r,
            float g,
            float b
    ) {}

    public record Result(
            String type,
            Map<String, Object> params
    ) {}

    // getPath() returns only the name (e.g.) cobblestone, toString() returns the full identifier (e.g.) minecraft:cobblestone :)
    public boolean matches(
            ItemStack candle,
            List<ItemStack> sacrifices,
            int sequence,
            List<String> honorificLines
    ) {
        if (!BuiltInRegistries.ITEM.getKey(candle.getItem()).toString().equals(this.candle)) {
            return false;
        }

        if (sacrifices.size() != this.sacrifices.size()) {
            return false;
        }

        Map<String, Integer> providedSacrifices = new HashMap<>();
        for (ItemStack sacrifice : sacrifices) {
            String key = BuiltInRegistries.ITEM.getKey(sacrifice.getItem()) + "|" + sacrifice.getCount();
            providedSacrifices.merge(key, 1, Integer::sum);
        }

        Map<String, Integer> requiredSacrifices = new HashMap<>();
        for (Sacrifice requiredSacrifice : this.sacrifices) {
            String key = requiredSacrifice.item() + "|" + requiredSacrifice.count();
            requiredSacrifices.merge(key, 1, Integer::sum);
        }

        if (!providedSacrifices.equals(requiredSacrifices)) {
            return false;
        }

        if (sequence > this.conditions.minSequence()) {
            return false;
        }

        if (!honorificLines.stream()
                .map(s -> s.toLowerCase().strip().replace(",", "").replace(".", "")).toList()
                .equals(this.honorific.lines().stream().map(s -> s.toLowerCase().strip().replace(",", "").replace(".", "")).toList())) {
            return false;
        }
        return true;
    }
}