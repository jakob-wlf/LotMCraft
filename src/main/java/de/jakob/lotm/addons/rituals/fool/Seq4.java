package de.jakob.lotm.addons.rituals.fool;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq4 {
    private static final int NEEDED_SPECTATORS = 20;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("fool") ||
                BeyonderData.getSequence(player) != 5) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if (component.isCompleted()) return;

        if(component.getStage() >= NEEDED_SPECTATORS){
            component.setCompleted(true);
            component.setStage(0);
        }
    }

    @SubscribeEvent
    private static void onLivingDeath(LivingDeathEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer livingEntity)) return;
        if(!(event.getSource().getEntity() instanceof ServerPlayer fool)) return;
        if (!BeyonderData.getPathway(fool).equals("fool") ||
                BeyonderData.getSequence(fool) != 5) return;

        var component = fool.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(BeyonderData.getSequence(livingEntity) <= 4){

            int totalValue = 0;
            var nearby = AbilityUtil.getNearbyEntities(livingEntity, (ServerLevel) livingEntity.level(), livingEntity.position(), 80);
            for(var obj : nearby){
                if(!(obj instanceof ServerPlayer player)) continue;

                if (BeyonderData.getPathway(player).equals("visionary") &&
                        BeyonderData.getSequence(player) <= 2){
                    if(VisionaryHandler.isInvisible(player)) continue;
                    totalValue+=100;

                    continue;
                }

                totalValue+=1;
            }

            component.setStage(totalValue);
        }
    }

}
