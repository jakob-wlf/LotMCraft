package de.jakob.lotm.quest;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.QuestComponent;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenQuestAcceptanceScreenPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

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

        for(ItemStack reward : quest.getRewards(player)) {
            if(!player.addItem(reward)) {
                player.drop(reward, false);
            }
        }

        float digestionReward = quest.getDigestionReward();
        if(BeyonderData.getSequence(player) < quest.sequence) {
            int sequenceDifference = quest.sequence - BeyonderData.getSequence(player);
            digestionReward *= (1 / (float) Math.pow(sequenceDifference, 2.25) + 1);
        }
        BeyonderData.digest(player, digestionReward, true);
        player.sendSystemMessage(Component.translatable("lotm.quest.completed", Component.translatable("lotm.quest.impl." + questId).getString()).withColor(0x2196F3));
    }

    /**
     * Opens the quest acceptance dialog for the player.
     * This sends a packet to the client to display the GUI.
     */
    public static boolean openQuestDialog(ServerPlayer player, String questId) {
        Quest quest = QuestRegistry.getQuest(questId);
        if(quest == null)
            return false;

        QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);

        // Check if player already has an active quest
        if(!component.getQuestProgress().isEmpty()) {
            player.sendSystemMessage(Component.translatable("lotm.quest.already_active").withColor(0xFF5722));
            return false;
        }

        // Get quest information
        List<ItemStack> rewards = quest.getRewards(player);
        float digestionReward = quest.getDigestionReward();
        int questSequence = quest.getSequence();

        // Send packet to open the GUI on client
        PacketHandler.sendToPlayer(player, new OpenQuestAcceptanceScreenPacket(
                questId,
                rewards,
                digestionReward,
                questSequence
        ));

        return true;
    }

    /**
     * Internal method called by the acceptance packet.
     * This is separated so the dialog can call it after the player confirms.
     */
    public static boolean acceptQuestInternal(ServerPlayer player, String questId) {
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

    /**
     * @deprecated Use openQuestDialog instead to show the acceptance GUI
     */
    @Deprecated
    public static boolean acceptQuest(ServerPlayer player, String questId) {
        return openQuestDialog(player, questId);
    }

    public static void progressQuest(ServerPlayer player, String questId, float progress) {
        QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);
        if(!component.getQuestProgress().containsKey(questId))
            return;

        float newProgress = (component.getQuestProgress().get(questId) + progress);
        component.getQuestProgress().put(questId, Math.min(1f, newProgress));
        ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("lotm.quest.progress", (int)(newProgress * 100) + "%").withColor(0x2196F3));
        player.connection.send(packet);
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