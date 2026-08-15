package de.jakob.lotm.addons.rituals.red_priest;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq5 {

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("red_priest") ||
        BeyonderData.getSequence(player) != 6) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        var target = AbilityUtil.getTargetEntity(player, 15, 0.5F, true);
        if(!(target instanceof ServerPlayer targetPlayer)) return;

        if(BeyonderData.getSequence(targetPlayer) > 5) return;
        if(targetPlayer.getHealth() > targetPlayer.getMaxHealth() * 0.1f) return;
        if(!target.hasEffect(MobEffects.WEAKNESS) || !target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) return;

        component.setCompleted(true);

    }

}
