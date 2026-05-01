package de.jakob.lotm.util.data;

import java.util.ArrayList;
import java.util.List;

public class ClientData {
    private static List<String> abilityWheelAbilities = new ArrayList<>();
    private static int selectedAbility = 0;

    // When true, the ability wheel is showing shared abilities rather than the player's own wheel
    public static boolean sharedAbilityMode = false;

    // Personal shared wheel ordering, set from IntrospectScreen's Shared tab
    private static List<String> sharedWheelAbilities = new ArrayList<>();
    private static int selectedSharedAbility = 0;

    public static List<String> getSharedWheelAbilities() {
        return sharedWheelAbilities;
    }

    public static void setSharedWheelAbilities(List<String> abilities) {
        sharedWheelAbilities = new ArrayList<>(abilities);
    }

    public static int getSelectedSharedAbility() {
        return selectedSharedAbility;
    }

    public static void setSelectedSharedAbility(int index) {
        selectedSharedAbility = index;
    }

    private static List<String> copiedAbilityIds = new ArrayList<>();
    private static List<String> copiedAbilityCopyTypes = new ArrayList<>();
    private static List<Integer> copiedAbilityRemainingUses = new ArrayList<>();

    public static List<String> getAbilityWheelAbilities() {
        return abilityWheelAbilities;
    }

    public static int getSelectedAbility() {
        return selectedAbility;
    }

    public static void setAbilityWheelData(ArrayList<String> abilities, int selected) {
        abilityWheelAbilities = abilities;
        selectedAbility = selected;
    }

    public static List<String> getCopiedAbilityIds() {
        return copiedAbilityIds;
    }

    public static List<String> getCopiedAbilityCopyTypes() {
        return copiedAbilityCopyTypes;
    }

    public static List<Integer> getCopiedAbilityRemainingUses() {
        return copiedAbilityRemainingUses;
    }

    public static void setCopiedAbilityData(ArrayList<String> abilityIds, ArrayList<String> copyTypes, ArrayList<Integer> remainingUses) {
        copiedAbilityIds = abilityIds;
        copiedAbilityCopyTypes = copyTypes;
        copiedAbilityRemainingUses = remainingUses;
    }

    public static void clearCache() {
        abilityWheelAbilities.clear();
        selectedAbility = 0;
        sharedAbilityMode = false;
        sharedWheelAbilities.clear();
        selectedSharedAbility = 0;
        copiedAbilityIds.clear();
        copiedAbilityCopyTypes.clear();
        copiedAbilityRemainingUses.clear();
    }
}