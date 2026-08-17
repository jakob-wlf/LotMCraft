package de.jakob.lotm.addons.rituals.darkness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq5 {

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("darkness") ||
                BeyonderData.getSequence(player) != 6) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

       if(player.level().dimension() == ModDimensions.SPIRIT_WORLD_DIMENSION_KEY){
           component.setCompleted(true);
       }
    }

}
