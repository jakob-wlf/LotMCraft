package de.jakob.lotm.util.data;

import java.util.ArrayList;
import java.util.List;

public class ClientData {
    private static List<String> abilityWheelAbilities = new ArrayList<>();
    private static int selectedAbility = 0;

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
}