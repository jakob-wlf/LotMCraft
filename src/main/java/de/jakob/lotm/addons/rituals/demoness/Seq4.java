package de.jakob.lotm.addons.rituals.demoness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq4 {
    private static int NEEDED = 8;
    private static Map<UUID, Set<UUID>> map = new HashMap<>();

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("demoness") ||
        BeyonderData.getSequence(player) != 5) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(!map.containsKey(player.getUUID())){
            map.put(player.getUUID(), new HashSet<>());
        }

        var set = map.get(player.getUUID());
        if(set.size() >= NEEDED){
            component.setCompleted(true);
            map.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    private static void onDeath(LivingDeathEvent event){
        if(!(event.getEntity() instanceof ServerPlayer targetPlayer)) return;
        if(!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("demoness") ||
                BeyonderData.getSequence(player) != 5) return;

        var source = event.getSource();
        if(source.is(ModDamageTypes.PLAGUE)){
            var set = map.get(player.getUUID());
            set.add(targetPlayer.getUUID());
            map.put(player.getUUID(), set);
        }
    }

}
