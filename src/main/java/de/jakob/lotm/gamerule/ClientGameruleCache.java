package de.jakob.lotm.gamerule;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientGameruleCache {

    public static boolean isGlobalGriefingEnabled = true;

}
