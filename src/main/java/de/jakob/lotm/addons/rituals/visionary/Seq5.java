package de.jakob.lotm.addons.rituals.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.network.packets.toClient.OpenPlayerDivinationScreenPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq5 {
    public static Set<UUID> wakeUp = new HashSet<>();
    private static int MIN_SEC = 30;
    private static int MAX_SEC = 60;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("visionary") ||
                BeyonderData.getSequence(player) != 6) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(player.isSleeping() && player.hasEffect(ModEffects.ASLEEP)){
            if(player.tickCount % 20 == 0) {
                component.setStage(component.getStage() + 1);
                player.addEffect(new MobEffectInstance(ModEffects.ASLEEP, 20 * 10, 10));
            }

            if(component.getStage() > MAX_SEC){
                wakeUp.remove(player.getUUID());
                player.kill();
            }
        }
        else if(component.getStage() != 0){
            if(component.getStage() > MAX_SEC){
                wakeUp.remove(player.getUUID());
                player.kill();
            }
            else if(component.getStage() >= MIN_SEC && component.getStage() <= MAX_SEC){
                wakeUp.remove(player.getUUID());
                component.setCompleted(true);
            }
            else{
                wakeUp.remove(player.getUUID());
                component.setStage(0);
            }
        }
    }

    @SubscribeEvent
    private static void onCanContinueSleeping(CanContinueSleepingEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("visionary") ||
                BeyonderData.getSequence(player) != 6) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if (component.isCompleted()) return;

        if (player.hasEffect(ModEffects.ASLEEP)
        && !wakeUp.contains(player.getUUID())) {
            event.setContinueSleeping(true);
        }
    }

    @SubscribeEvent
    private static void onDamage(LivingDamageEvent.Post event){
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("visionary") ||
                BeyonderData.getSequence(player) != 6) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if (component.isCompleted()) return;

        if(wakeUp.contains(player.getUUID())) return;

        if(player.isSleeping() && player.hasEffect(ModEffects.ASLEEP)){
            wakeUp.add(player.getUUID());
        }
    }

}
