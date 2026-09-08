package de.jakob.lotm.addons.rituals.mother;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq3 {


    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("mother") ||
                BeyonderData.getSequence(player) != 4) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if (component.isCompleted()) return;

        if(component.getStage() >= 1){
            component.setCompleted(true);
        }
    }

    @SubscribeEvent
    private static void onLivingDeath(LivingDeathEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer livingEntity)) return;
        if(!(livingEntity.level() instanceof ServerLevel level)) return;

        if(BeyonderData.getPathway(livingEntity).equals("darkness")
                && BeyonderData.getSequence(livingEntity) <= 2){

            var nearby = AbilityUtil.getNearbyEntities(livingEntity, level, livingEntity.position(), 80);
            for(var obj : nearby){
                if(!(obj instanceof ServerPlayer player)) continue;

                if (!BeyonderData.getPathway(player).equals("mother") ||
                        BeyonderData.getSequence(player) != 4) continue;

                var component = player.getData(ModAttachments.RITUALS.get());
                if(component.isCompleted() || component.getStage() != 0) continue;

                component.setStage(1);
            }

        }
    }

}
