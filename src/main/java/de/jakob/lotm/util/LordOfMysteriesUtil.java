package de.jakob.lotm.util;

import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.sefirah.SefirahHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;

public final class LordOfMysteriesUtil {
    public static final String PATHWAY_ID = "lord_of_mysteries";
    public static final int SEQUENCE = -1;
    public static final Set<String> TRINITY_PATHWAYS = Set.of("fool", "door", "error");

    private LordOfMysteriesUtil() {
    }

    public static boolean isLordOfMysteries(LivingEntity entity) {
        return BeyonderData.isBeyonder(entity)
                && PATHWAY_ID.equals(BeyonderData.getPathway(entity))
                && BeyonderData.getSequence(entity) == SEQUENCE;
    }

    public static boolean matchesRequirement(String entityPathway, int entitySequence, String requiredPathway, int requiredSequence) {
        if (entityPathway == null || requiredPathway == null || requiredSequence < entitySequence) {
            return false;
        }
        if (entityPathway.equals(requiredPathway)) {
            return true;
        }
        return PATHWAY_ID.equals(entityPathway)
                && (PATHWAY_ID.equals(requiredPathway) || TRINITY_PATHWAYS.contains(requiredPathway));
    }

    public static boolean matchesAnyRequirement(String entityPathway, int entitySequence, Map<String, Integer> requirements) {
        return requirements.entrySet().stream()
                .anyMatch(entry -> matchesRequirement(entityPathway, entitySequence, entry.getKey(), entry.getValue()));
    }

    public static boolean canAscendToLordOfMysteries(ServerPlayer player) {
        if (!BeyonderData.isBeyonder(player)) {
            return false;
        }
        String pathway = BeyonderData.getPathway(player);
        if (!TRINITY_PATHWAYS.contains(pathway) || BeyonderData.getSequence(player) != 0) {
            return false;
        }
        if (!"sefirah_castle".equalsIgnoreCase(SefirahHandler.getClaimedSefirot(player))) {
            return false;
        }
        return hasSequenceOneCharacteristic(player, "fool")
                && hasSequenceOneCharacteristic(player, "door")
                && hasSequenceOneCharacteristic(player, "error")
                && hasItem(player, ModItems.FOOL_UNIQUENESS.get())
                && hasItem(player, ModItems.DOOR_UNIQUENESS.get())
                && hasItem(player, ModItems.ERROR_UNIQUENESS.get());
    }

    private static boolean hasSequenceOneCharacteristic(ServerPlayer player, String pathway) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof BeyonderCharacteristicItem characteristic
                    && pathway.equals(characteristic.getPathway())
                    && characteristic.getSequence() == 1) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasItem(ServerPlayer player, Item item) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(item)) {
                return true;
            }
        }
        return false;
    }
}
