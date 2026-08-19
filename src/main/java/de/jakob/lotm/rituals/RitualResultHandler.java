package de.jakob.lotm.rituals;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public interface RitualResultHandler {
    void perform(Map<String, Object> params, ServerPlayer player);
}