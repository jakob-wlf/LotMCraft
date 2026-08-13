package de.jakob.lotm.addons.rituals.wheel_of_fortune;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq4 {

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("wheel_of_fortune") ||
                BeyonderData.getSequence(player) != 5) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        var luck = player.getData(ModAttachments.LUCK_COMPONENT.get());
        if(luck.getLuck() < -4000){
            var target = AbilityUtil.getTargetEntity(player, 12, 1.0f, true, true);

            if(target == null) return;

            if(target.hasEffect(MobEffects.INVISIBILITY)
                    || VisionaryHandler.isInvisible(target)) return;

            String path = BeyonderData.getPathway(target);
            int seq = BeyonderData.getSequence(target);

            if((path.equals("tyrant") && seq <= 4) || seq <= 2){
                component.setCompleted(true);
            }
        }

    }
}
