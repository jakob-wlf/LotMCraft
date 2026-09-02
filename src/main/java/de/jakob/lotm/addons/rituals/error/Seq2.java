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

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq2 {
    private static final int MIN_LEVEL = 3;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!(player.level() instanceof ServerLevel level))return;
        if(!BeyonderData.getPathway(player).equals("error") ||
                BeyonderData.getSequence(player) != 3) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        String name = player.getName().getString();
        var factions = BeyonderData.factionStorage.getPartOfFaction(name);

        for(var obj : factions){
            if(obj.getLevel() < MIN_LEVEL) continue;
            if(obj.getCreatedBy().equals(name)) continue;

            if(obj.getLeader().equals(name)){
                component.setCompleted(true);
                return;
            }
        }
    }


}
