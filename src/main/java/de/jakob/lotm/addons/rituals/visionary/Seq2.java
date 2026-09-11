package de.jakob.lotm.addons.rituals.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq2 {
    private static final int AMOUNT = 5;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!(player.level() instanceof ServerLevel level))return;
        if(!BeyonderData.getPathway(player).equals("visionary") ||
                BeyonderData.getSequence(player) != 3) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if (component.getTargetUuids().size() >= AMOUNT) {
            component.setCompleted(true);
        }
    }

//    @SubscribeEvent
//    private static void onSkillUsage(AbilityUsedEvent event){
//        if(!(event.getEntity() instanceof ServerPlayer player)) return;
//        if(!(player.level() instanceof ServerLevel level))return;
//        if(!BeyonderData.getPathway(player).equals("visionary") ||
//                BeyonderData.getSequence(player) != 3) return;
//
//        if(event.getAbility().getId().equals("dream_weave_ability")){
//            var target = event.getAbilityTarget();
//
//            if(target instanceof ServerPlayer targetPlayer){
//                if(!map.containsKey(player.getUUID())){
//                    map.put(player.getUUID(), new LinkedList<>(List.of(targetPlayer.getUUID())));
//                }
//                else{
//                    var list = map.get(player.getUUID());
//                    if(list.contains(targetPlayer.getUUID()))
//                        return;
//
//                    list.add(targetPlayer.getUUID());
//                    map.put(player.getUUID(), list);
//                }
//            }
//        }
//    }
}
