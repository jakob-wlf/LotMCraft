package de.jakob.lotm.util.pathways;

import de.jakob.lotm.util.BeyonderData;

public record PathwayInfos(
        String name,
        String id,
        int color,
        String[] sequenceNames
) {
   public static String getSequenceName(String convertedSequenceName) {
       for(String pathway : BeyonderData.pathways) {
           for(String s : BeyonderData.pathwayInfos.get(pathway).sequenceNames) {
               if(convertedSequenceName.equals(convertSequenceName(s)))
                   return s;
           }
       }

       return convertedSequenceName;
   }

    private static String convertSequenceName(String sequenceName) {
        return sequenceName.toLowerCase().replace(" ", "_");
    }
}
