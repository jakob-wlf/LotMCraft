package de.jakob.lotm.addons.rituals.mother;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq4 {
    private static final int NEED_AMOUNT = 1000;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("mother") ||
                BeyonderData.getSequence(player) != 5) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if (component.isCompleted()) return;

        if (component.getStage() >= NEED_AMOUNT) {
            component.setCompleted(true);
        }
    }

    @SubscribeEvent
    private static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if(!BeyonderData.isBeyonder(livingEntity)) return;

        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("mother") ||
                BeyonderData.getSequence(player) != 5) return;

        int value = 1;
        if(livingEntity instanceof ServerPlayer){
            value = 100;
        }

        var component = player.getData(ModAttachments.RITUALS.get());
        component.setStage(component.getStage() + value);

    }

}
