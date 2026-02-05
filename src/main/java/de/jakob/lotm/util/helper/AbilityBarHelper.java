package de.jakob.lotm.util.helper;

import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;

public class AbilityBarHelper {
    
    public static ArrayList<String> getAbilities(Player player) {
        return player.getData(ModAttachments.ABILITY_BAR_COMPONENT).getAbilities();
    }
    
    public static void setAbilities(Player player, ArrayList<String> abilities) {
        player.getData(ModAttachments.ABILITY_BAR_COMPONENT).setAbilities(abilities);
    }
}