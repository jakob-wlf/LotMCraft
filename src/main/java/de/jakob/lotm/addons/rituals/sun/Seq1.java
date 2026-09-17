package de.jakob.lotm.addons.rituals.sun;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.rituals.RitualEffectHandlerEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.item.custom.SunItem;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq1 {
    private static final Integer AMOUNT = 300;
    private static final HashMap<UUID, Long> timestamp = new HashMap<>();
    public static final int FAIL_TIME = 60 * 60 * 24; // 24 hours

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("sun") ||
                BeyonderData.getSequence(player) != 2) return;
        if(!(player.level() instanceof ServerLevel level)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

       if(component.getStage() >= AMOUNT){
           component.setCompleted(true);
           timestamp.remove(player.getUUID());
       }

       if(!timestamp.containsKey(player.getUUID())){
           timestamp.put(player.getUUID(), System.currentTimeMillis());
       }
       else{
           var saved = timestamp.get(player.getUUID());

           if(System.currentTimeMillis() >= saved + (FAIL_TIME*1000)){
               if (component.getStage() > 0) {
                   player.sendSystemMessage(Component.literal("You lost your ritual progress!").withStyle(ChatFormatting.RED));
               }
               component.setStage(0);
           }
       }

        if (component.getStage() > 0) {
            long currentTime = System.currentTimeMillis();

            if (!timestamp.containsKey(player.getUUID())) {
                timestamp.put(player.getUUID(), currentTime);
            } else {
                long startTime = timestamp.get(player.getUUID());

                if (currentTime >= startTime + (FAIL_TIME * 1000)) {
                    if (component.getStage() > 0) {
                        player.sendSystemMessage(Component.literal("You lost your ritual progress!").withStyle(ChatFormatting.RED));
                    }
                    component.setStage(0);
                    timestamp.remove(player.getUUID());
                }
            }
        } else {
            timestamp.remove(player.getUUID());
        }
    }

}
