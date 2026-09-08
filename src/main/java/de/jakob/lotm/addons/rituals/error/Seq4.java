package de.jakob.lotm.addons.rituals.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.rituals.RitualEffectHandlerEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq4 {
    public static final int AMOUNT = 9;
    private static final int MIN_AMOUNT_ITEMS = 10;
    private static Map<UUID, Set<UUID>> map = new HashMap<>();

    private static Map<UUID, Integer> inventoryMapInitial = new HashMap<>();
    private static Map<UUID, UUID> lookingAt = new HashMap<>();

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!(player.level() instanceof ServerLevel level))return;
        if(!BeyonderData.getPathway(player).equals("error") ||
                BeyonderData.getSequence(player) != 5) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(component.getStage() != 0 && !map.containsKey(player.getUUID())){
            RitualEffectHandlerEvent.removeRitual(player);
        }

        if(component.getStage() >=AMOUNT){
            component.setCompleted(true);

            for(var obj : map.get(player.getUUID())){
                var target = level.getPlayerByUUID(obj);
                if(target == null) continue;

                target.kill();
            }

            map.remove(player.getUUID());

            return;
        }

        var target = AbilityUtil.getTargetEntity(player, 8, 0, true, true);
        if(!(target instanceof ServerPlayer targetPlayer)) return;
        if(BeyonderData.getSequence(target) > 6) return;

        if(map.containsKey(player.getUUID())){
            if(map.get(player.getUUID()).contains(targetPlayer.getUUID())) return;
        }

        if(lookingAt.containsKey(player.getUUID())){
            if(!lookingAt.get(player.getUUID()).equals(targetPlayer.getUUID())){
                inventoryMapInitial.remove(player.getUUID());
            }
        }

        int occupied = getOccupiedSlots(targetPlayer);

        if(!inventoryMapInitial.containsKey(player.getUUID())){
            if(occupied < MIN_AMOUNT_ITEMS) return;

            inventoryMapInitial.put(player.getUUID(), occupied);
        }
        else{
            if(occupied == 0 && inventoryMapInitial.get(player.getUUID()) >= MIN_AMOUNT_ITEMS){
                component.setStage(component.getStage() + 1);

                if(!map.containsKey(player.getUUID())){
                    map.put(player.getUUID(), new HashSet<>(Set.of(targetPlayer.getUUID())));
                }
                else{
                    var set = map.get(player.getUUID());
                    set.add(targetPlayer.getUUID());
                    map.put(player.getUUID(), set);
                }
            }
        }

        lookingAt.put(player.getUUID(), targetPlayer.getUUID());
    }

        private static int getOccupiedSlots(ServerPlayer player) {
            int occupied = 0;

            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty()) {
                    occupied++;
                }
            }

            return occupied;
        }
}
