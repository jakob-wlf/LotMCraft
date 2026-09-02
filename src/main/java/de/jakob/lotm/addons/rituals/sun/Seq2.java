package de.jakob.lotm.addons.rituals.sun;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.rituals.RitualEffectHandlerEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.item.custom.SunItem;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq2 {
    private static Map<UUID, Integer> timer = new HashMap<>();
    private static final int TIME_SEC = 20 * 60 * 10;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("sun") ||
                BeyonderData.getSequence(player) != 3) return;
        if(!(player.level() instanceof ServerLevel level)) return;

        var component = player.getData(ModAttachments.RITUALS.get());

        if(!timer.containsKey(player.getUUID())){
            timer.put(player.getUUID(), 0);
        }

        boolean haveChar = false;
        boolean haveSun = false;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof BeyonderCharacteristicItem bchar) {
                if(bchar.getPathway().equals("sun") && bchar.getSequence() <= 2){
                    haveChar = true;
                }
            }

            if(stack.getItem() instanceof SunItem sunItem){
                haveSun = true;
            }
        }

        timer.put(player.getUUID(), timer.get(player.getUUID()) + 1);

        if((haveChar || haveSun)
        && timer.get(player.getUUID()) >= TIME_SEC){
            if(!component.isCompleted()){
                component.setCompleted(true);
            }

            timer.remove(player.getUUID());
        }
        else{
            if(component.isCompleted())
                RitualEffectHandlerEvent.removeRitualWithMessage(player);
        }
    }

}
