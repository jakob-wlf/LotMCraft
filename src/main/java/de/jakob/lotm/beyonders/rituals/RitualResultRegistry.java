package de.jakob.lotm.beyonders.rituals;

import de.jakob.lotm.beyonders.rituals.impl.*;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class RitualResultRegistry {

    private static final Map<String, RitualResultHandler> HANDLERS = new HashMap<>();

    static {
        register("potion_effect", new RitualMagicPotionEffect());
        register("area_effect", new RitualMagicAreaEffect());
        register("summon_entity", new RitualMagicSummonEntity());
        register("open_portal", new RitualMagicOpenPortal());
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