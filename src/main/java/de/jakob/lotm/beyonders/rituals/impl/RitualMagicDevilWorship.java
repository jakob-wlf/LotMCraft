package de.jakob.lotm.beyonders.rituals.impl;

import de.jakob.lotm.beyonders.abilities.abyss.DevilWorshipAbility;
import de.jakob.lotm.beyonders.rituals.RitualResultHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class RitualMagicDevilWorship implements RitualResultHandler {

    @Override
    public void perform(Map<String, Object> params, ServerPlayer player, BlockPos ritualCenter) {
        DevilWorshipAbility.perform(player);
    }
}