package de.jakob.lotm.util.helper;

import de.jakob.lotm.attachments.AbilityWheelComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncAbilityWheelPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;

/**
 * Helper class for managing ability wheel operations and syncing.
 * Always use these methods to ensure proper client-server synchronization.
 */
public class AbilityWheelHelper {

    /**
     * Adds an ability to the player's wheel and syncs to client.
     * @param player The player to add the ability to
     * @param abilityId The ID of the ability to add
     */
    public static void addAbility(ServerPlayer player, String abilityId) {
        AbilityWheelComponent component = player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
        ArrayList<String> abilities = component.getAbilities();
        
        if (!abilities.contains(abilityId)) {
            abilities.add(abilityId);
            component.setAbilities(abilities);
            syncToClient(player);
        }
    }

    /**
     * Removes an ability from the player's wheel and syncs to client.
     * @param player The player to remove the ability from
     * @param abilityId The ID of the ability to remove
     */
    public static void removeAbility(ServerPlayer player, String abilityId) {
        AbilityWheelComponent component = player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
        ArrayList<String> abilities = component.getAbilities();
        
        if (abilities.remove(abilityId)) {
            component.setAbilities(abilities);
            
            // Adjust selected ability if needed
            int selected = component.getSelectedAbility();
            if (selected >= abilities.size()) {
                component.setSelectedAbility(Math.max(0, abilities.size() - 1));
            }
            
            syncToClient(player);
        }
    }

    /**
     * Sets all abilities for the player's wheel and syncs to client.
     * @param player The player to set abilities for
     * @param abilities The list of ability IDs to set
     */
    public static void setAbilities(ServerPlayer player, ArrayList<String> abilities) {
        AbilityWheelComponent component = player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
        component.setAbilities(abilities);
        
        // Adjust selected ability if needed
        int selected = component.getSelectedAbility();
        if (selected >= abilities.size()) {
            component.setSelectedAbility(Math.max(0, abilities.size() - 1));
        }
        
        syncToClient(player);
    }

    /**
     * Clears all abilities from the player's wheel and syncs to client.
     * @param player The player to clear abilities for
     */
    public static void clearAbilities(ServerPlayer player) {
        AbilityWheelComponent component = player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
        component.setAbilities(new ArrayList<>());
        component.setSelectedAbility(0);
        syncToClient(player);
    }

    /**
     * Sets the selected ability index and syncs to client.
     * @param player The player to set selection for
     * @param index The index of the ability to select
     */
    public static void setSelectedAbility(ServerPlayer player, int index) {
        AbilityWheelComponent component = player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
        
        if (index >= 0 && index < component.getAbilities().size()) {
            component.setSelectedAbility(index);
            syncToClient(player);
        }
    }

    /**
     * Syncs the ability wheel data to the client.
     * Call this after any manual modification to AbilityWheelComponent.
     * @param player The player to sync to
     */
    public static void syncToClient(ServerPlayer player) {
        AbilityWheelComponent component = player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
        PacketHandler.sendToPlayer(
                player,
                new SyncAbilityWheelPacket(component.getAbilities(), component.getSelectedAbility())
        );
    }

    /**
     * Gets the currently selected ability ID for a player.
     * @param player The player to get the selected ability for
     * @return The ability ID, or null if no abilities or invalid selection
     */
    public static String getSelectedAbilityId(ServerPlayer player) {
        AbilityWheelComponent component = player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
        
        if (component.getAbilities().isEmpty()) {
            return null;
        }
        
        int selectedIndex = component.getSelectedAbility();
        if (selectedIndex >= 0 && selectedIndex < component.getAbilities().size()) {
            return component.getAbilities().get(selectedIndex);
        }
        
        return null;
    }
}