package de.jakob.lotm.abilities;

import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class AbilityHandler {

    private static final HashMap<Location, AbilityItem> usedAbilities = new HashMap<>();

    public static void useAbilityInArea(AbilityItem ability, Location loc) {
        if(loc.getLevel().isClientSide)
            return;

        usedAbilities.put(loc, ability);
        ServerScheduler.scheduleDelayed(12, () -> usedAbilities.remove(loc), (ServerLevel) loc.getLevel());
    }

    @Nullable
    public static AbilityItem abilityUsedInArea(Location loc, double radius) {
        for(Map.Entry<Location, AbilityItem> entry : usedAbilities.entrySet()) {
            Location abilityLoc = entry.getKey();
            if(loc.getPosition().distanceToSqr(abilityLoc.getPosition()) <= (radius * radius)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public static boolean canUse(Player player, boolean ignoreCreative, Map<String, Integer> requirements, double spiritualityCost) {
        // Creative mode always works
        if (player.isCreative() && !ignoreCreative) {
            return true;
        }

        if (player.level().isClientSide()) {
            // Client-side: use cached data
            String pathway = ClientBeyonderCache.getPathway(player.getUUID());
            int sequence = ClientBeyonderCache.getSequence(player.getUUID());
            float spirituality = ClientBeyonderCache.getSpirituality(player.getUUID());

            // Debug pathway always works
            if (pathway.equalsIgnoreCase("debug")) {
                return true;
            }

            if(!requirements.containsKey(pathway))
                return false;

            // Check if pathway has requirements
            Integer minSeq = requirements.get(pathway);
            if (minSeq == null) {
                return false;
            }

            // Check sequence and spirituality requirements
            return sequence <= minSeq && spirituality >= spiritualityCost;
        } else {
            // Server-side: use your existing logic
            String pathway = BeyonderData.getPathway(player);
            int sequence = BeyonderData.getSequence(player);

            // Debug pathway always works
            if (pathway.equalsIgnoreCase("debug")) {
                return true;
            }

            if(!requirements.containsKey(pathway))
                return false;

            // Check if pathway has requirements
            Integer minSeq = requirements.get(pathway);
            if (minSeq == null) {
                return false;
            }

            // Check sequence and spirituality requirements
            return sequence <= minSeq && BeyonderData.getSpirituality(player) >= spiritualityCost;
        }
    }
}
