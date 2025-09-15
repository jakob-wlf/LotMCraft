package de.jakob.lotm.util.pathways;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;

public record PathwayInfos(
        String id,
        int color,
        String[] sequenceNames
) {
   public static String getSequenceNameByRegisteredItemName(String sequenceName) {

       return Component.translatable("lotm.sequence." + sequenceName).getString();
   }

   public String getName() {
       return Component.translatable("lotm.pathway." + id).getString();
    }

    public String getSequenceName(int sequence) {
        if(sequence < 0 || sequence > sequenceNames.length)
            return "";
        return Component.translatable("lotm.sequence." + sequenceNames[sequence]).getString();
    }
}
