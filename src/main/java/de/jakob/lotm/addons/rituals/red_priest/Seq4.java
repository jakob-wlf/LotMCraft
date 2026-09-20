package de.jakob.lotm.addons.rituals.red_priest;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AllyComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq4 {
    public static final int MIN_ALLY_AMOUNT = 5;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("red_priest") ||
        BeyonderData.getSequence(player) != 5) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(AllyUtil.getAllyCount(player) < MIN_ALLY_AMOUNT) return;
        var list = AllyUtil.getAllAllies(player);

        for(var obj : list){
            var data = BeyonderData.playerMap.get(obj.uuid());
            if(data == null || data.isEmpty()) continue;

            if(data.get().sequence() <= 4){
                component.setStage(component.getStage() + 1);
            }
        }

        if(component.getStage() >= MIN_ALLY_AMOUNT){
            component.setCompleted(true);
        }
        else{
            component.setStage(0);
        }
    }

}
