package de.jakob.lotm.events;

import com.mojang.datafixers.util.Pair;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class HonorificNamesEventHandler {
    public static HashMap<UUID, LinkedList<String>> input = new HashMap<>();
    public static HashMap<UUID, Long> timeout = new HashMap<>();
    public static HashMap<UUID, UUID> isInTransferring = new HashMap<>();

    public static LinkedList<Pair<UUID, UUID>> answerState = new LinkedList<>();

    @SubscribeEvent
    public static void onChatMessageSent(ServerChatEvent event) {
        UUID playerUUID = event.getPlayer().getUUID();

        String DEBUG_nickname = event.getPlayer().getName().getString();

        if(timeout.containsKey(playerUUID)
                && timeout.get(playerUUID) <= System.currentTimeMillis() - 60000) {

            timeout.remove(playerUUID);
            input.remove(playerUUID);

            event.getPlayer().sendSystemMessage(Component.translatable("lotmcraft.honorific_timeout")
                    .withStyle(ChatFormatting.DARK_RED));

            return;
        }

        String rawMessage = event.getRawText();

        if(isInTransferring.containsKey(playerUUID)){
            var target = event.getPlayer().server.getPlayerList().
                    getPlayer(isInTransferring.get(playerUUID));

            if(target != null){
                target.sendSystemMessage(Component.translatable("lotmcraft.honorific_praying_message", rawMessage)
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
        else if (input.containsKey(playerUUID) && isHonorificNamePart(rawMessage)){
            var list = input.get(playerUUID);
            list.add(rawMessage);

        } else if (!isHonorificNamePart(rawMessage)) {
            return;
        }

        if(input.containsKey(playerUUID)
                && input.get(playerUUID).size() >= 3
                && isHonorificNameLastLine(rawMessage)){

            var targetUUID = BeyonderData.beyonderMap.findCandidate(input.get(playerUUID));

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

            if(targetUUID.equals(playerUUID)){
                target.sendSystemMessage(Component.translatable("lotmcraft.own_praying")
                        .withStyle(ChatFormatting.GREEN));
                return;
            }

            if(BeyonderData.getSequence(target) == 3 && target.distanceTo(event.getPlayer()) >= 4000.0f){
                target.sendSystemMessage(Component.translatable("lotmcraft.far_away_praying")
                        .withStyle(ChatFormatting.RED));
                return;
            }

            isInTransferring.put(playerUUID, targetUUID);

            target.getData(ModAttachments.SANITY_COMPONENT).increaseSanityAndSync(.01f, target);

            target.sendSystemMessage(formMessage(event.getPlayer(), target));
        }
    }

    public static boolean isHonorificNameFirstLine(String str){
        return BeyonderData.beyonderMap.containsHonorificNameWithFirstLine(str);
    }

    public static boolean isHonorificNameLastLine(String str){
        return BeyonderData.beyonderMap.containsHonorificNameWithLastLine(str);
    }

    public static boolean isHonorificNamePart(String str){
        return BeyonderData.beyonderMap.containsHonorificNameWithLine(str);
    }

    public static Component formMessage(LivingEntity player, LivingEntity target){
        answerState.add(new Pair<>(target.getUUID(), player.getUUID()));

        MutableComponent message = Component.empty();

        Component spacer = Component.literal("\n--- ").withStyle(ChatFormatting.DARK_GREEN);

        Component generalInfo = Component.translatable("lotmcraft.honorific_praying",
                player.getName().getString(), BeyonderData.getPathway(player),
                BeyonderData.getSequence(player), player.getX(), player.getY(), player.getZ())
                .withStyle(ChatFormatting.GREEN);

        Component sendMessageButton = Component.translatable( "lotmcraft.honorific_praying_send_message_button")
                .withStyle(style -> style
                        .withColor(ChatFormatting.GOLD)
                        .withBold(true)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/honorificname ui sendmessage " + player.getName().getString()
                                        + " " + target.getName().getString()
                        ))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("lotmcraft.honorific_praying_send_message_desc")
                                        .withStyle(ChatFormatting.GRAY)
                        ))
                );

        Component teleportButton = Component.translatable( "lotmcraft.honorific_praying_teleport_button")
                .withStyle(style -> style
                        .withColor(ChatFormatting.GOLD)
                        .withBold(true)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/honorificname ui teleport " + player.getName().getString()
                                        + " " + target.getName().getString()
                        ))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("lotmcraft.honorific_praying_teleport_desc")
                                        .withStyle(ChatFormatting.GRAY)
                        ))
                );

        return message.append(generalInfo).append(spacer)
                .append(sendMessageButton).append(spacer)
                .append(teleportButton);
    }
}
