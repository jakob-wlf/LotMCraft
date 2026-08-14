package de.jakob.lotm.addons.rituals.tyrant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.factions.FactionCore;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq0 {

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("tyrant") ||
                BeyonderData.getSequence(player) != 1) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(component.getStage() != 1) return;

        String name = player.getName().getString();
        var factions = BeyonderData.factionStorage.getPartOfFaction(name);

        for(var obj : factions){
            if(obj.getLeader().equals(name) && obj.getLevel() >= FactionCore.getMaxLevel()){
                component.setCompleted(true);
            }
        }
    }

    @SubscribeEvent
    private static void onDamage(LivingDamageEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("tyrant") ||
                BeyonderData.getSequence(player) != 1) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;
        if(component.getStage() != 0) return;

        var sourceEntity = event.getSource().getEntity();
        if(sourceEntity == null) return;
        if(!(sourceEntity instanceof ServerPlayer sourcePlayer)) return;

        if(BeyonderData.getSequence(sourcePlayer) <= 0){
            component.setStage(1);
        }
    }
}
