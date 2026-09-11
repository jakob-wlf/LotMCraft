package de.jakob.lotm.addons.rituals.darkness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.rituals.RitualEffectHandlerEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq3 {
    private static Map<UUID, Integer> timer = new HashMap<>();

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("darkness") ||
                BeyonderData.getSequence(player) != 4) return;
        if(!(player.level() instanceof ServerLevel level)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        var sanity = player.getData(ModAttachments.SANITY_COMPONENT.get());

        if(!component.isCompleted() && player.hasEffect(ModEffects.LOOSING_CONTROL)
                && player.getHealth() <= player.getMaxHealth() * 0.2f
                && sanity.getSanity() <= 0.6f)
        {
            component.setCompleted(true);
            timer.put(player.getUUID(), 0);
        }

        if (component.isCompleted() && timer.containsKey(player.getUUID())) {
            int currentTicks = timer.get(player.getUUID()) + 1;
            timer.put(player.getUUID(), currentTicks);

            if (currentTicks >= 15 * 20) {
                RitualEffectHandlerEvent.removeRitualWithMessage(player);
                timer.remove(player.getUUID());
            }
        }
    }
}
