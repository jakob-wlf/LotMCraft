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

        String DEBUG_nickname = event.getPlayer().getName().getString();

        if(timeout.containsKey(playerUUID)
                && timeout.get(playerUUID) <= System.currentTimeMillis() - 60000) {

            LOTMCraft.LOGGER.info("HN stage 0: {} --- {}, timed out for {}",
                    System.currentTimeMillis() - 60000, timeout.get(playerUUID), DEBUG_nickname);

            timeout.remove(playerUUID);
            input.remove(playerUUID);

            event.getPlayer().sendSystemMessage(Component.translatable("lotmcraft.honorific_timeout")
                    .withStyle(ChatFormatting.DARK_RED));

            return;
        }

        String rawMessage = event.getRawText();

        if(isInTransferring.containsKey(playerUUID)){
            LOTMCraft.LOGGER.info("NH stage 2: is in transfer for {}", DEBUG_nickname);

            var target = event.getPlayer().server.getPlayerList().
                    getPlayer(isInTransferring.get(playerUUID));

            if(target != null){
                target.sendSystemMessage(Component.translatable("lotmcraft.honorific_prayings_message", rawMessage)
                        .withStyle(ChatFormatting.DARK_GREEN));

                LOTMCraft.LOGGER.info("NH stage 2: transferred from {} to {}", DEBUG_nickname, target.getDisplayName().toString());
            }

            isInTransferring.remove(playerUUID);

            LOTMCraft.LOGGER.info("NH stage 2: delete from transfer for {}", DEBUG_nickname);

            return;
        }

        if(!input.containsKey(playerUUID) && isHonorificNameFirstLine(rawMessage)){
            LOTMCraft.LOGGER.info("NH stage 3: detected first line of HN for {}, line {}", DEBUG_nickname, rawMessage);

            input.put(playerUUID, new LinkedList<>(List.of(rawMessage)));
            timeout.put(playerUUID, System.currentTimeMillis());
            return;
        }
        else if (input.containsKey(playerUUID) && isHonorificNamePart(rawMessage)){
            LOTMCraft.LOGGER.info("NH stage 3: add line line of HN for {}, line {}", DEBUG_nickname, rawMessage);
            var list = input.get(playerUUID);
            list.add(rawMessage);

        } else if (!isHonorificNamePart(rawMessage)) {
            return;
        }

        if(input.containsKey(playerUUID)
                && input.get(playerUUID).size() >= 3
                && isHonorificNameLastLine(rawMessage)){

            LOTMCraft.LOGGER.info("NH stage 4: detected last line of HN for {}, line {}", DEBUG_nickname, rawMessage);
            var targetUUID = BeyonderData.beyonderMap.findCandidat(input.get(playerUUID));

            if(targetUUID == null){
                LOTMCraft.LOGGER.info("NH stage 4: target not found for {}", DEBUG_nickname);

                input.remove(playerUUID);
                timeout.remove(playerUUID);
                return;
            }

            var target = event.getPlayer().server.getPlayerList().getPlayer(targetUUID);

            if(target == null){
                LOTMCraft.LOGGER.info("NH stage 4: target is offline for {}", DEBUG_nickname);

                input.remove(playerUUID);
                timeout.remove(playerUUID);
                return;
            }

            if(targetUUID.equals(playerUUID)){
                LOTMCraft.LOGGER.info("NH stage 4: target is the same person as player for {}", DEBUG_nickname);

                target.sendSystemMessage(Component.translatable("lotmcraft.own_prayings")
                        .withStyle(ChatFormatting.GREEN));
                return;
            }

            if(BeyonderData.getSequence(target) == 3 && target.distanceTo(event.getPlayer()) >= 4000.0f){
                LOTMCraft.LOGGER.info("NH stage 4: target is too far away from player for {}", DEBUG_nickname);

                target.sendSystemMessage(Component.translatable("lotmcraft.far_away_prayings")
                        .withStyle(ChatFormatting.RED));
                return;
            }

            isInTransferring.put(playerUUID, targetUUID);

            LOTMCraft.LOGGER.info("NH stage 5: Form message for target for {}, target {}", DEBUG_nickname, target.getName().toString());

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
        return BeyonderData.beyonderMap.containsHonorificNamewithLine(str);
    }

    public static Component formMessage(LivingEntity player, LivingEntity target){
        MutableComponent message = Component.empty();

        Component spacer = Component.literal("\n---").withStyle(ChatFormatting.DARK_GREEN);

        Component generalInfo = Component.translatable("lotmcraft.honorific_prayings",
                player.getName().getString(), BeyonderData.getPathway(player),
                BeyonderData.getSequence(player), player.getX(), player.getY(), player.getZ())
                .withStyle(ChatFormatting.GREEN);

        Component sendMessageButton = Component.translatable( "lotmcraft.honorific_prayings_send_message_button")
                .withStyle(style -> style
                        .withColor(ChatFormatting.GOLD)
                        .withBold(true)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/honorificname ui sendmessage " + player.getName().getString()
                                        + " " + target.getName().getString()
                        )));

        return message.append(generalInfo).append(spacer)
                .append(sendMessageButton).append(spacer);
    }
}
