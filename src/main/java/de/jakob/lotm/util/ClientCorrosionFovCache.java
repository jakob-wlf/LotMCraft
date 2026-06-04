package de.jakob.lotm.util;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientCorrosionFovCache {

    private static float fovMultiplier = 1.0f;

    public static void setFovMultiplier(float multiplier) {
        fovMultiplier = multiplier;
    }

    public static float getFovMultiplier() {
        return fovMultiplier;
    }

    public static void reset() {
        fovMultiplier = 1.0f;
    }
}
