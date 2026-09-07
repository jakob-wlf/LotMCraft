package de.jakob.lotm.util.helper;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncAbilityBarPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;

public class AbilityBarHelper {
    
    public static ArrayList<String> getAbilities(Player player) {
        return player.getData(ModAttachments.ABILITY_BAR_COMPONENT).getAbilities();
    }
    
    public static void setAbilities(Player player, ArrayList<String> abilities) {
        player.getData(ModAttachments.ABILITY_BAR_COMPONENT).setAbilities(abilities);
        if(player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendToPlayer(serverPlayer, new SyncAbilityBarPacket(abilities));
        }
    }
}