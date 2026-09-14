package de.jakob.lotm.addons.rituals.demoness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq5 {
    private static int NEEDED = 160;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("demoness") ||
        BeyonderData.getSequence(player) != 6) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(component.getStage() >= NEEDED){
            component.setCompleted(true);
        }

        if(!player.isOnFire()){
            if (component.getStage() > 0) {
                player.sendSystemMessage(Component.literal("You lost your ritual progress!").withStyle(ChatFormatting.RED));
            }
            component.setStage(0);
        }
    }

    @SubscribeEvent
    private static void onDamage(LivingDamageEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("demoness") ||
                BeyonderData.getSequence(player) != 6) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(player.isOnFire()){
            component.setStage(component.getStage() + 1);
        }
    }

}
