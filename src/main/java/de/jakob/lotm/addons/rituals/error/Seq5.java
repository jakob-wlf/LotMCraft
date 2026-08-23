package de.jakob.lotm.addons.rituals.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq5 {
    private static final int AMOUNT = 10;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!(player.level() instanceof ServerLevel level))return;
        if(!BeyonderData.getPathway(player).equals("error") ||
                BeyonderData.getSequence(player) != 6) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        component.setStage(0);
        var nearby = AbilityUtil.getNearbyEntities(player, level, player.position(), 100);
        for(var obj : nearby){
            if(!(obj instanceof ServerPlayer target)) continue;

            if(target.hasEffect(ModEffects.ASLEEP) || target.isSleeping()){
                component.setStage(component.getStage() + 1);
            }
        }

        if(component.getStage() >= AMOUNT){
            component.setCompleted(true);
        }
    }


}
