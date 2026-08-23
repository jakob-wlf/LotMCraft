package de.jakob.lotm.addons.rituals;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class RitualEffectHandlerEvent {
    public static final int FAIL_TIME = 300; //in sec - 300
    private static final HashMap<UUID, Long> timestamp = new HashMap<>();

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()){
            var controlling = player.getData(ModAttachments.CONTROLLING_DATA.get());
            var idealism = player.getData(ModAttachments.DISCERNMENT_DATA.get());

            if(controlling.isControlling() || idealism.isDiscerning()){
                component.setCompleted(false);
                component.setStage(0);
            }

            if(!timestamp.containsKey(player.getUUID())){
                timestamp.put(player.getUUID(), System.currentTimeMillis());

                player.sendSystemMessage(Component.literal("You have completed the ritual!" +
                                "\nYou have " + FAIL_TIME + " seconds to drink the potion.\n")
                        .withStyle(ChatFormatting.GREEN));
            }
            else{
                var saved = timestamp.get(player.getUUID());

                if(System.currentTimeMillis() >= saved + (FAIL_TIME*1000)){
                    player.sendSystemMessage(Component.literal("The effect of ritual has faded!")
                            .withStyle(ChatFormatting.RED));

                    timestamp.remove(player.getUUID());
                    component.setCompleted(false);
                    component.setStage(0);
                }
            }
        }
    }

    public static void removeRitual(ServerPlayer player){
        var component = player.getData(ModAttachments.RITUALS.get());

        component.setCompleted(false);
        component.setStage(0);

        timestamp.remove(player.getUUID());
    }

    public static void removeRitualWithMessage(ServerPlayer player){
        var component = player.getData(ModAttachments.RITUALS.get());

        player.sendSystemMessage(Component.literal("The effect of ritual has faded!")
                .withStyle(ChatFormatting.RED));

        component.setCompleted(false);
        component.setStage(0);

        timestamp.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        removeRitual(player);
    }
}
