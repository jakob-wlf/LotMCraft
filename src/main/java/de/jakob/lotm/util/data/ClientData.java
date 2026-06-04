package de.jakob.lotm.util.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientData {
    private static List<String> abilityWheelAbilities = new ArrayList<>();
    private static int selectedAbility = 0;

    // When true, the ability wheel is showing shared abilities rather than the player's own wheel
    public static boolean sharedAbilityMode = false;

    // Personal shared wheel ordering, set from IntrospectScreen's Shared tab
    private static List<String> sharedWheelAbilities = new ArrayList<>();
    private static int selectedSharedAbility = 0;

    // Sefirot Authority: available and unlocked cross-path ability IDs
    private static List<String> sefirotAvailableAbilityIds = new ArrayList<>();
    private static List<String> sefirotUnlockedAbilityIds  = new ArrayList<>();
    /** True when the player owns any sefirot (even those with no cross-path neighbours). */
    private static boolean ownsSefirot = false;

    public static List<String> getSefirotAvailableAbilityIds() {
        return Collections.unmodifiableList(sefirotAvailableAbilityIds);
    }

    public static void setSefirotAvailableAbilityIds(List<String> ids) {
        sefirotAvailableAbilityIds = new ArrayList<>(ids);
    }

    public static List<String> getSefirotUnlockedAbilityIds() {
        return Collections.unmodifiableList(sefirotUnlockedAbilityIds);
    }

    public static void setSefirotUnlockedAbilityIds(List<String> ids) {
        sefirotUnlockedAbilityIds = new ArrayList<>(ids);
    }

    public static boolean isOwningSefirot() {
        return ownsSefirot;
    }

    public static void setOwnsSefirot(boolean owns) {
        ownsSefirot = owns;
    }

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
}