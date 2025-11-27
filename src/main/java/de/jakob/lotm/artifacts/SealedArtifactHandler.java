package de.jakob.lotm.artifacts;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.ToggleAbilityItem;
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
        List<AbilityItem> abilities = selectRandomAbilities(pathway, sequence);
        
        // Create negative effect
        NegativeEffect negativeEffect = NegativeEffect.createRandom(pathway, sequence, RANDOM);

        return new SealedArtifactData(pathway, sequence, abilities, negativeEffect);
    }

    /**
     * Selects 1-2 random abilities from the pathway at or above the sequence
     */
    private static List<AbilityItem> selectRandomAbilities(String pathway, int sequence) {
        // Get all abilities for this pathway
        List<AbilityItem> pathwayAbilities = getPathwayAbilities(pathway, sequence);

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
    private static List<AbilityItem> getPathwayAbilities(String pathway, int targetSequence) {
        List<AbilityItem> validAbilities = new ArrayList<>();

        // Iterate through all registered items
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof AbilityItem abilityItem && !(item instanceof ToggleAbilityItem)) {
                if(!abilityItem.canBeCopied) {
                    continue;
                }
                // Check if this ability belongs to the pathway
                Map<String, Integer> requirements = abilityItem.getRequirements();
                
                if (requirements.containsKey(pathway)) {
                    int requiredSequence = requirements.get(pathway);
                    
                    // Ability is valid if its required sequence is >= target sequence
                    // (remember: higher number = lower rank, so Sequence 9 < Sequence 7)
                    if (requiredSequence == targetSequence) {
                        validAbilities.add(abilityItem);
                    }
                }
            }
        }

        // If no abilities found at target sequence, try higher sequences
        if (validAbilities.isEmpty() && targetSequence < 9) {
            return getPathwayAbilities(pathway, targetSequence + 1);
        }

        return validAbilities;
    }

    /**
     * Gets all ability items in the registry for debugging
     */
    public static List<AbilityItem> getAllAbilities() {
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof AbilityItem && !(item instanceof ToggleAbilityItem))
                .map(item -> (AbilityItem) item)
                .collect(Collectors.toList());
    }

    /**
     * Debug method to check pathway coverage
     */
    public static Map<String, Integer> getPathwayAbilityCounts() {
        Map<String, Integer> counts = new HashMap<>();
        
        for (String pathway : BeyonderData.implementedPathways) {
            int count = getPathwayAbilities(pathway, 0).size();
            counts.put(pathway, count);
        }
        
        return counts;
    }
}