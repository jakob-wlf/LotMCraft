package de.jakob.lotm.artifacts;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.common.*;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.demoness.MirrorWorldTraversalAbility;
import de.jakob.lotm.abilities.red_priest.ConqueringAbility;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.*;

/**
 * Handles the creation and logic for sealed artifacts
 */
public class SealedArtifactHandler {

    private static final Set<Item> VALID_BASE_ITEMS = new HashSet<>(Arrays.asList(
            //Items.BELL,
            //Items.CHAIN,
            Items.NETHERITE_SWORD,
            Items.HEART_OF_THE_SEA,
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
        // if (item == Items.BELL) return "bell";
        // if (item == Items.CHAIN) return "chain";
        if (item == Items.NETHERITE_SWORD) return "sword";
        if (item == Items.HEART_OF_THE_SEA) return "gem";
        if (item == Items.NETHER_STAR) return "star";
        return "item";
    }

    /**
     * Creates sealed artifact data from a characteristic
     */
    public static SealedArtifactData createSealedArtifactData(BeyonderCharacteristicItem characteristic, String baseItem) {
        String pathway = characteristic.getPathway();
        int sequence = characteristic.getSequence();

        return createSealedArtifactData(pathway, sequence, baseItem);
    }

    public static SealedArtifactData createSealedArtifactData(String pathway, Integer sequence, String baseItem) {
        // Get 1-3 random abilities
        List<Ability> abilities = selectRandomAbilities(pathway, sequence);

        // Create negative effect
        List<NegativeEffect> negativeEffect = NegativeEffect.createRandom(pathway, sequence, RANDOM, baseItem);

        return new SealedArtifactData(pathway, sequence, abilities, negativeEffect);
    }

    /**
     * Selects 1-2 random abilities from the pathway at or above the sequence
     */
    private static List<Ability> selectRandomAbilities(String pathway, int sequence) {
        List<Ability> pathwayAbilities = new ArrayList<>();

        // always add one ability from the same sequence
        pathwayAbilities.add(getPathwayAbilities(pathway, sequence, true, pathwayAbilities));

        if (sequence <= 2) {
            pathwayAbilities.add(getPathwayAbilities(pathway, sequence, false, pathwayAbilities));
            pathwayAbilities.add(getPathwayAbilities(pathway, sequence, false, pathwayAbilities));
        } else if (sequence <= 4) {
            pathwayAbilities.add(getPathwayAbilities(pathway, sequence, false, pathwayAbilities));
        }

        return pathwayAbilities;
    }

    /**
     * Gets all abilities for a pathway at or above a sequence (higher sequence number = lower rank)
     */
    private static Ability getPathwayAbilities(String pathway, int targetSequence, boolean exact, List<Ability> excluded) {
        Ability validAbility;
        if (exact){
            validAbility = LOTMCraft.abilityHandler.getRandomAbility(pathway, targetSequence, RANDOM, exact, excluded);
        } else {
            validAbility = LOTMCraft.abilityHandler.getRandomAbility(pathway, targetSequence, RANDOM, exact, excluded);
        }

        // if it loops to seq10, return a default ability
        if (targetSequence > 9) {
            return LOTMCraft.abilityHandler.getById("ally_ability");
        }

        // If no ability found at target sequence, try higher sequences
        if ((validAbility == null) || !validAbility.canBeUsedInArtifact) {
            return getPathwayAbilities(pathway, targetSequence + 1, exact, excluded);
        }

        return validAbility;
    }
}