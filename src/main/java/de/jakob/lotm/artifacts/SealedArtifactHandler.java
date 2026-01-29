package de.jakob.lotm.artifacts;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles the creation and logic for sealed artifacts
 */
public class SealedArtifactHandler {

    private static final Set<Item> VALID_BASE_ITEMS = new HashSet<>(Arrays.asList(
            Items.BELL,
            Items.CHAIN,
            Items.IRON_SWORD,
            Items.DIAMOND_SWORD,
            Items.DIAMOND,
            Items.GOLD_INGOT,
            Items.NETHER_STAR
    ));

    private static final Random RANDOM = new Random();

    /**
     * Checks if an item can be used as a base for sealed artifacts
     */
    public static boolean isValidBaseItem(Item item) {
        return VALID_BASE_ITEMS.contains(item);
    }

    /**
     * Gets the base type name for display purposes
     */
    public static String getBaseTypeName(Item item) {
        if (item == Items.BELL) return "bell";
        if (item == Items.CHAIN) return "chain";
        if (item == Items.IRON_SWORD || item == Items.DIAMOND_SWORD) return "sword";
        if (item == Items.DIAMOND || item == Items.GOLD_INGOT) return "gem";
        if (item == Items.NETHER_STAR) return "star";
        return "item";
    }

    /**
     * Creates sealed artifact data from a characteristic
     */
    public static SealedArtifactData createSealedArtifactData(BeyonderCharacteristicItem characteristic) {
        String pathway = characteristic.getPathway();
        int sequence = characteristic.getSequence();

        // Get 1-2 random abilities
        List<Ability> abilities = selectRandomAbilities(pathway, sequence);
        
        // Create negative effect
        NegativeEffect negativeEffect = NegativeEffect.createRandom(pathway, sequence, RANDOM);

        return new SealedArtifactData(pathway, sequence, abilities, negativeEffect);
    }

    /**
     * Selects 1-2 random abilities from the pathway at or above the sequence
     */
    private static List<Ability> selectRandomAbilities(String pathway, int sequence) {
        // Get all abilities for this pathway
        List<Ability> pathwayAbilities = getPathwayAbilities(pathway, sequence);

        if (pathwayAbilities.isEmpty()) {
            return new ArrayList<>();
        }

        int abilityCount = 1;
        abilityCount = Math.min(abilityCount, pathwayAbilities.size());

        // Randomly select abilities
        Collections.shuffle(pathwayAbilities);
        return pathwayAbilities.subList(0, abilityCount);
    }

    /**
     * Gets all abilities for a pathway at or above a sequence (higher sequence number = lower rank)
     */
    private static List<Ability> getPathwayAbilities(String pathway, int targetSequence) {
        List<Ability> validAbilities = new ArrayList<>();

        // Iterate through all registered items
        validAbilities.addAll(LOTMCraft.abilityHandler.getByPathwayAndSequenceExact(pathway, targetSequence));

        // If no abilities found at target sequence, try higher sequences
        if (validAbilities.isEmpty() && targetSequence < 9) {
            return getPathwayAbilities(pathway, targetSequence + 1);
        }

        return validAbilities;
    }
}