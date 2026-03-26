package de.jakob.lotm.util.helper;

import de.jakob.lotm.attachments.CopiedAbilityComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.gui.custom.CopiedAbilityWheel.CopiedAbilityWheelMenu;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncCopiedAbilitiesPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for managing copied/stolen/replicated abilities and syncing.
 * Always use these methods to ensure proper client-server synchronization.
 */
public class CopiedAbilityHelper {

    /**
     * Adds a copied ability to the player's storage and syncs to client.
     */
    public static void addAbility(ServerPlayer player, CopiedAbilityComponent.CopiedAbilityData data) {
        CopiedAbilityComponent component = player.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        component.addAbility(data);
        syncToClient(player);
    }

    /**
     * Removes a copied ability at the given index and syncs to client.
     */
    public static void removeAbility(ServerPlayer player, int index) {
        CopiedAbilityComponent component = player.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        component.removeAbility(index);
        syncToClient(player);
    }

    /**
     * Opens the copied ability wheel on the client side by opening the menu from the server.
     */
    public static void openCopiedAbilityWheel(ServerPlayer player) {
        syncToClient(player);
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, p) -> new CopiedAbilityWheelMenu(id, inventory),
                Component.translatable("lotm.copied_ability_wheel.title")
        ));
    }

    /**
     * Syncs the copied abilities data to the client.
     */
    public static void syncToClient(ServerPlayer player) {
        CopiedAbilityComponent component = player.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        List<CopiedAbilityComponent.CopiedAbilityData> abilities = component.getAbilities();

        ArrayList<String> abilityIds = new ArrayList<>();
        ArrayList<String> copyTypes = new ArrayList<>();
        ArrayList<Integer> remainingUses = new ArrayList<>();

        for (CopiedAbilityComponent.CopiedAbilityData data : abilities) {
            abilityIds.add(data.abilityId());
            copyTypes.add(data.copyType());
            remainingUses.add(data.remainingUses());
        }

        PacketHandler.sendToPlayer(player, new SyncCopiedAbilitiesPacket(abilityIds, copyTypes, remainingUses));
    }
}
