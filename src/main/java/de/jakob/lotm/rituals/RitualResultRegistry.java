package de.jakob.lotm.rituals;

import de.jakob.lotm.rituals.impl.RitualMagicPotionEffect;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class RitualResultRegistry {

    private static final Map<String, RitualResultHandler> HANDLERS = new HashMap<>();

    static {
        register("potion_effect", new RitualMagicPotionEffect());
    }

    private RitualResultRegistry() {}

    public static void register(String type, RitualResultHandler handler) {
        HANDLERS.put(type, handler);
    }

    @Nullable
    public static RitualResultHandler get(String type) {
        return HANDLERS.get(type);
    }
}