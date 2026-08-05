package de.jakob.lotm.gamerule;

import net.minecraft.world.level.GameRules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientGameruleCache {

    public static boolean isGlobalGriefingEnabled = true;
    public static GameRules.Key<GameRules.BooleanValue> DO_CHAR_EXCHANGE_WHEEL;
    public static GameRules.Key<GameRules.BooleanValue> DO_CHAR_SLOT_ROLL_WHEEL;
    public static GameRules.Key<GameRules.BooleanValue> DO_DAILY_SPIN_WHEEL;
    public static GameRules.Key<GameRules.BooleanValue> DO_SELL_YOUR_SOUL_WHEEL;

}
