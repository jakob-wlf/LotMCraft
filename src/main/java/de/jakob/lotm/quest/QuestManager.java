package de.jakob.lotm.quest;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.QuestComponent;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class QuestManager {

    public static void completeQuest(ServerPlayer player, String questId) {
        Quest quest = QuestRegistry.getQuest(questId);
        if(quest == null)
            return;

        QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);

        if(!component.getQuestProgress().containsKey(questId))
            return;

        component.getQuestProgress().remove(questId);
        component.getCompletedQuests().add(questId);

        for(ItemStack reward : quest.getRewards()) {
            if(!player.addItem(reward)) {
                player.drop(reward, false);
            }
        }

        BeyonderData.digest(player, quest.getDigestionReward(), false);
        player.sendSystemMessage(Component.translatable("lotm.quest.completed", Component.translatable("lotm.quest.impl." + questId).getString()).withColor(0x2196F3));
    }

    public static boolean acceptQuest(ServerPlayer player, String questId) {
        Quest quest = QuestRegistry.getQuest(questId);
        if(quest == null)
            return false;

        QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);

        if(!component.getQuestProgress().isEmpty()) {
            player.sendSystemMessage(Component.translatable("lotm.quest.already_active").withColor(0xFF5722));
            return false;
        }

        component.getQuestProgress().put(questId, 0f);
        player.sendSystemMessage(Component.translatable("lotm.quest.accepted", Component.translatable("lotm.quest.impl." + questId).getString()).withColor(0x4CAF50));
        player.sendSystemMessage(quest.getDescription().withColor(0x4CAF50));
        return true;
    }

    public static void progressQuest(ServerPlayer player, String questId, float progress) {
        QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);
        if(!component.getQuestProgress().containsKey(questId))
            return;

        float newProgress = (component.getQuestProgress().get(questId) + progress);
        component.getQuestProgress().put(questId, Math.min(1f, newProgress));
        player.sendSystemMessage(Component.translatable("lotm.quest.progress", (int)(newProgress * 100) + "%").withColor(0x2196F3));
        if(newProgress >= 1f) {
            completeQuest(player, questId);
        }
    }

    @SubscribeEvent
    public static void onServerPlayerTick(PlayerTickEvent.Post event) {
        if(event.getEntity().level().isClientSide || !(event.getEntity() instanceof ServerPlayer player))
            return;

        QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);
        for(String questId : component.getQuestProgress().keySet()) {
            Quest quest = QuestRegistry.getQuest(questId);
            if(quest != null) {
                quest.tick(player);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if(!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);
        for(String questId : component.getQuestProgress().keySet()) {
            Quest quest = QuestRegistry.getQuest(questId);
            if(quest != null) {
                quest.onPlayerKillLiving(player, event.getEntity());
            }
        }
    }

    // TODO: Add animations for quest completion and quest progress

}
