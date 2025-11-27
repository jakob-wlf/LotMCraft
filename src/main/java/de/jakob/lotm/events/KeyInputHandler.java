package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.OpenAbilitySelectionPacket;
import de.jakob.lotm.network.packets.toServer.TeleportToSefirotPacket;
import de.jakob.lotm.network.packets.toServer.ToggleAbilityHotbarPacket;
import de.jakob.lotm.network.packets.toServer.ToggleGriefingPacket;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (LOTMCraft.openAbilitySelectionKey != null && LOTMCraft.openAbilitySelectionKey.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                int sequence = ClientBeyonderCache.getSequence(player.getUUID());
                String pathway = ClientBeyonderCache.getPathway(player.getUUID());
                PacketHandler.sendToServer(new OpenAbilitySelectionPacket(sequence, pathway));
            }
        }

        if(LOTMCraft.toggleGriefingKey != null && LOTMCraft.toggleGriefingKey.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                PacketHandler.sendToServer(new ToggleGriefingPacket());
            }
        }

        if(LOTMCraft.enterSefirotKey != null && LOTMCraft.enterSefirotKey.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                PacketHandler.sendToServer(new TeleportToSefirotPacket());
            }
        }

        if(LOTMCraft.toggleAbilityHotbarKey != null && LOTMCraft.toggleAbilityHotbarKey.consumeClick()) {
            PacketHandler.sendToServer(new ToggleAbilityHotbarPacket(true));
        }

        if(LOTMCraft.cycleAbilityHotbarKey != null && LOTMCraft.cycleAbilityHotbarKey.consumeClick()) {
            PacketHandler.sendToServer(new ToggleAbilityHotbarPacket(false));

        }

        if(LOTMCraft.nextAbilityKey != null && LOTMCraft.nextAbilityKey.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if(player != null && ClientBeyonderCache.isBeyonder(player.getUUID())) {
                if(player.getMainHandItem().getItem() instanceof SelectableAbilityItem abilityItem && abilityItem.canUse(player, player.getMainHandItem()))
                    abilityItem.nextAbility(player);
            }
        }

        if(LOTMCraft.previousAbilityKey != null && LOTMCraft.previousAbilityKey.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if(player != null && ClientBeyonderCache.isBeyonder(player.getUUID())) {
                if(player.getMainHandItem().getItem() instanceof SelectableAbilityItem abilityItem && abilityItem.canUse(player, player.getMainHandItem()))
                    abilityItem.previousAbility(player);
            }
        }
    }
}
