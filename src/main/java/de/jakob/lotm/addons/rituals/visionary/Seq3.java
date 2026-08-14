package de.jakob.lotm.addons.rituals.visionary;

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
public class Seq3 {
    private static final int AMOUNT = 35;
    private static final Set<UUID> set = new HashSet<>();

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!(player.level() instanceof ServerLevel level))return;
        if(!BeyonderData.getPathway(player).equals("visionary") ||
                BeyonderData.getSequence(player) != 4) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        for(var target : level.getServer().getPlayerList().getPlayers()){
            if(target.isSleeping() || target.hasEffect(ModEffects.ASLEEP))
                set.add(target.getUUID());
            else
                set.remove(target.getUUID());
        }

        if(set.size() >= AMOUNT){
            component.setCompleted(true);
        }
    }


}
