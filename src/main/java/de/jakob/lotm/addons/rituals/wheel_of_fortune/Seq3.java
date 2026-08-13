package de.jakob.lotm.addons.rituals.wheel_of_fortune;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq3 {
    private static HashMap<UUID, String> nameMap = new HashMap<>();
    public static final int FAIL_TIME = 3600;
    private static final HashMap<UUID, Long> timestamp = new HashMap<>();

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("wheel_of_fortune") ||
                BeyonderData.getSequence(player) != 4) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;
        if(!timestamp.containsKey(player.getUUID()) || !nameMap.containsKey(player.getUUID())){
            component.setStage(0);
        }

        if(component.getStage() == 1){
            if(System.currentTimeMillis() >= timestamp.get(player.getUUID()) + (FAIL_TIME*1000)){
                timestamp.remove(player.getUUID());
                nameMap.remove(player.getUUID());
                component.setStage(0);
            }
        }
        else if(component.getStage() == 2){
            component.setCompleted(true);
            timestamp.remove(player.getUUID());
            nameMap.remove(player.getUUID());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    private static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if(!BeyonderData.getPathway(player).equals("wheel_of_fortune") ||
                BeyonderData.getSequence(player) != 4) return;

        var component = player.getData(ModAttachments.RITUALS.get());

        if(component.getStage() != 0) return;
        if(component.isCompleted()) return;

        String msg = event.getRawText();
        var stream = new TokenStream(msg);

        String name = stream.peek();

        UUID id = BeyonderData.playerMap.getKeyByName(name);
        if(id == null) return;

        var seq = BeyonderData.playerMap.get(id).get().sequence();

        if(seq >= 4) return;

        stream.next();
        boolean first = stream.match("will");
        stream.next();
        boolean second = stream.match("die");

        if(first && second){
            Component message = Component.literal("You have set prophecy that " + name + " will die"
                    + "\nYou must make sure this prophecy comes true\nYou have exactly " + FAIL_TIME
                    + " seconds to complete the ritual").withStyle(ChatFormatting.GREEN);

            player.sendSystemMessage(message);
            timestamp.put(player.getUUID(), System.currentTimeMillis());
            component.setStage(1);

            nameMap.put(player.getUUID(), name);
        }
    }

    @SubscribeEvent
    private static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if(!(player.level() instanceof ServerLevel level)) return;

        String name = player.getName().getString();

        List<UUID> buff = new LinkedList<>();

        for(var obj : nameMap.entrySet()){
            if(obj.getValue().equals(name)){
                var target = level.getEntity(obj.getKey());

                if(target == null){
                    buff.add(obj.getKey());
                    continue;
                }

                var component = target.getData(ModAttachments.RITUALS.get());
                component.setStage(2);
            }
        }

        for(var obj : buff){
            nameMap.remove(obj);
            timestamp.remove(obj);
        }
    }
}
