package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class HonorificNamesEventHandler {
    public static HashMap<UUID, LinkedList<String>> input = new HashMap<>();
    public static HashMap<UUID, Long> timeout = new HashMap<>();
    public static HashMap<UUID, UUID> isInTransferring = new HashMap<>();

    @SubscribeEvent
    public static void onChatMessageSent(ServerChatEvent event) {
        UUID playerUUID = event.getPlayer().getUUID();

        if(timeout.containsKey(playerUUID)
                && timeout.get(playerUUID) <= System.currentTimeMillis() - 60000) {
            timeout.remove(playerUUID);
            input.remove(playerUUID);
        }

        String rawMessage = event.getRawText();

        if(isInTransferring.containsKey(playerUUID)){
            var target = event.getPlayer().server.getPlayerList().
                    getPlayer(isInTransferring.get(playerUUID));

            if(target != null){
                target.sendSystemMessage(Component.translatable("lotmcraft.honorific_prayings_message", rawMessage)
                        .withStyle(ChatFormatting.DARK_GREEN));
            }

            isInTransferring.remove(playerUUID);
            return;
        }

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

            isInTransferring.put(playerUUID, targetUUID);

            target.sendSystemMessage(formMessage(event.getPlayer(), target));
        }
    }

    public static boolean isHonorificNameFirstLine(String str){
        return BeyonderData.beyonderMap.containsHonorificNameWithFirstLine(str);
    }

    public static boolean isHonorificNameLastLine(String str){
        return BeyonderData.beyonderMap.containsHonorificNameWithLastLine(str);
    }

    public static Component formMessage(LivingEntity player, LivingEntity target){
        MutableComponent message = Component.empty();

        Component spacer = Component.literal("\n---").withStyle(ChatFormatting.DARK_GREEN);

        Component generalInfo = Component.translatable("lotmcraft.honorific_prayings",
                player.getDisplayName().getString(), BeyonderData.getPathway(player),
                BeyonderData.getSequence(player), player.getX(), player.getY(), player.getZ())
                .withStyle(ChatFormatting.GREEN);

        Component sendMessageButton = Component.translatable( "lotmcraft.honorific_prayings_send_message_button")
                .withStyle(style -> style
                        .withColor(ChatFormatting.GOLD)
                        .withBold(true)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/honorificname ui sendmessage " + player.getDisplayName().getString()
                                        + " " + target.getDisplayName()
                        )));

        return message.append(generalInfo).append(spacer)
                .append(sendMessageButton).append(spacer);
    }
}
