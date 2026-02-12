package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class HonorificNamesEventHandler {
    public static HashMap<UUID, LinkedList<String>> input = new HashMap<>();
    public static HashMap<UUID, Long> timeout = new HashMap<>();

    @SubscribeEvent
    public static void onChatMessageSent(ServerChatEvent event) {
        UUID playerUUID = event.getPlayer().getUUID();

        if(timeout.containsKey(playerUUID)
                && timeout.get(playerUUID) <= System.currentTimeMillis() - 60000) {
            timeout.remove(playerUUID);
            input.remove(playerUUID);
        }

        String rawMessage = event.getRawText();

        if(!input.containsKey(playerUUID) && isHonorificNameFirstLine(rawMessage)){
            input.put(playerUUID, new LinkedList<>(List.of(rawMessage)));
            timeout.put(playerUUID, System.currentTimeMillis());
            return;
        }
        else if (input.containsKey(playerUUID)){
            var list = input.get(playerUUID);
            list.add(rawMessage);

            input.put(playerUUID, list);
        }

        if(isHonorificNameLastLine(rawMessage)){
            var targetUUID = BeyonderData.beyonderMap.findCandidat(input.get(playerUUID));

            if(targetUUID == null){
                input.remove(playerUUID);
                timeout.remove(playerUUID);
                return;
            }

            var target = event.getPlayer().server.getPlayerList().getPlayer(targetUUID);

            if(target == null){
                input.remove(playerUUID);
                timeout.remove(playerUUID);
                return;
            }

            if(targetUUID == playerUUID){
                target.sendSystemMessage(Component.translatable("lotmcraft.own_prayings")
                        .withStyle(ChatFormatting.GREEN));
                return;
            }

            if(BeyonderData.getSequence(target) == 3 && target.distanceTo(event.getPlayer()) >= 4000.0f){
                target.sendSystemMessage(Component.translatable("lotmcraft.far_away_prayings")
                        .withStyle(ChatFormatting.RED));
                return;
            }

            
        }
    }

    public static boolean isHonorificNameFirstLine(String str){
        return BeyonderData.beyonderMap.containsHonorificNameWithFirstLine(str);
    }

    public static boolean isHonorificNameLastLine(String str){
        return BeyonderData.beyonderMap.containsHonorificNameWithLastLine(str);
    }

}
