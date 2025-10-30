package de.jakob.lotm.events;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.quests.PlayerQuestData;
import de.jakob.lotm.entity.quests.QuestUpdateEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Handles game events and updates quest progress
 * Register this class with the event bus
 */
@EventBusSubscriber
public class QuestEventHandler {
    
    /**
     * Track mob kills for quests
     */
    @SubscribeEvent
    public static void onEntityKilled(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (!player.level().isClientSide) {
                PlayerQuestData questData = player.getData(ModAttachments.PLAYER_QUEST_DATA.get());
                QuestUpdateEvent questEvent = QuestUpdateEvent.mobKilled(event.getEntity());
                questData.updateQuests(player, questEvent);
            }
        }
    }
    
    /**
     * Track villager interactions for quests
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof Villager villager) {
            Player player = event.getEntity();
            if (!player.level().isClientSide) {
                PlayerQuestData questData = player.getData(ModAttachments.PLAYER_QUEST_DATA.get());
                QuestUpdateEvent questEvent = QuestUpdateEvent.villagerTalked(villager.getVillagerData().getProfession());
                questData.updateQuests(player, questEvent);
            }
        }
    }
    
    /**
     * Track player movement for location-based quests
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player.tickCount % 20 == 0) { // Check every second
            PlayerQuestData questData = player.getData(ModAttachments.PLAYER_QUEST_DATA.get());
            questData.checkQuestCompletions(player);
        }
    }
    
    /**
     * You can add more event handlers here for item collection, etc.
     */
}