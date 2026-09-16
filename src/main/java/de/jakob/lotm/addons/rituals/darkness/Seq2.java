package de.jakob.lotm.addons.rituals.darkness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq2 {
    private static final int DISTANCE = 250;
    private static final int SECONDS_PER_STAGE = 320;
    private static final int STAGES = 40;
    private static final int MAX_LEVEL_IN_FACTION = 3;

    @SubscribeEvent
    private static void onFoolTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("darkness") ||
                BeyonderData.getSequence(player) != 3) return;
        if(!(player.level() instanceof ServerLevel level)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if (level.dimension() != ServerLevel.OVERWORLD) {
            if (component.getStage() > 0) {
                player.sendSystemMessage(Component.literal("You lost your ritual progress!").withStyle(ChatFormatting.RED));
            }
            component.setStage(0);
            return;
        }

        String name = player.getName().getString();
        var factions = BeyonderData.factionStorage.getPartOfFaction(name);
        for(var obj : factions){
            if(obj.getPlayerLevel(name) > MAX_LEVEL_IN_FACTION)
                return;
        }

        for(var obj : level.getServer().getPlayerList().getPlayers()){
            if(obj.equals(player)) continue;
            if (obj.level() != player.level()) continue;
            if(obj.distanceTo(player) <= DISTANCE) {
                if (component.getStage() > 0) {
                    player.sendSystemMessage(Component.literal("You lost your ritual progress!").withStyle(ChatFormatting.RED));
                }
                component.setStage(0);
            }

        }

        if(player.tickCount % (20 * SECONDS_PER_STAGE) == 0){
            component.setStage(component.getStage() + 1);
        }

        if(component.getStage() >= STAGES){
            component.setCompleted(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    private static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        if(!(player.level() instanceof ServerLevel level)) return;

        for(var obj : level.getServer().getPlayerList().getPlayers()){
            if(BeyonderData.getPathway(obj).equals("darkness") && BeyonderData.getSequence(obj) == 3){
                if (obj.level() != player.level()) continue;
                if(player.distanceTo(obj) <= DISTANCE){
                    var component = obj.getData(ModAttachments.RITUALS.get());
                    if (component.getStage() > 0) {
                        player.sendSystemMessage(Component.literal("You lost your ritual progress!").withStyle(ChatFormatting.RED));
                    }
                    component.setStage(0);
                }
            }
        }
    }
}
