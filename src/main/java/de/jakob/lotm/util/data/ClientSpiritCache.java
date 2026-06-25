package de.jakob.lotm.util.data;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientSpiritCache {
    private static int spiritTypeOrdinal = -1;

    public static int getSpiritTypeOrdinal() {
        return spiritTypeOrdinal;
    }

    public static void setSpiritTypeOrdinal(int ordinal) {
        spiritTypeOrdinal = ordinal;
    }
}
