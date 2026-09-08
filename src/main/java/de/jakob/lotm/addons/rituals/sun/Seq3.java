package de.jakob.lotm.addons.rituals.sun;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq3 {
    public static final int AMOUNT = 5;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("sun") ||
                BeyonderData.getSequence(player) != 4) return;
        if(!(player.level() instanceof ServerLevel level)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(component.getStage() >= AMOUNT){
            component.setCompleted(true);
        }
    }

    @SubscribeEvent
    private static void onDeath(LivingDeathEvent event){
        if(!(event.getEntity() instanceof ServerPlayer victim)) return;
        if(!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        if(!BeyonderData.getPathway(player).equals("sun") ||
                BeyonderData.getSequence(player) != 4) return;

        String path = BeyonderData.getPathway(victim);
        int seq = BeyonderData.getSequence(victim);
        var component = player.getData(ModAttachments.RITUALS.get());

        if(component.isCompleted()) return;

        if(!BeyonderData.isEvilPathway(path)){
            component.setStage(0);
            return;
        }

        if(seq <= 4){
            component.setStage(component.getStage() + 1);
        }
    }
}
