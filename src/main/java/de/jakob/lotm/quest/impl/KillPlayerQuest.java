package de.jakob.lotm.quest.impl;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.QuestComponent;
import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.potions.PotionItemHandler;
import de.jakob.lotm.quest.Quest;
import de.jakob.lotm.quest.QuestManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.beyonderMap.StoredData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class KillPlayerQuest extends Quest {

    private final java.util.Map<UUID, UUID> targetByPlayer = new java.util.HashMap<>();

    public KillPlayerQuest(String id, int sequence) {
        super(id, sequence);
    }

    @Override
    public void startQuest(ServerPlayer player) {
        int questTakerSeq = BeyonderData.getSequence(player);

        List<UUID> possibleTargets = BeyonderData.beyonderMap.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(player.getUUID()))
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> {
                    int targetSeq = entry.getValue().sequence();
                    return targetSeq >= 0 && targetSeq <= questTakerSeq + 1;
                })
                .map(java.util.Map.Entry::getKey)
                .toList();

        if (possibleTargets.isEmpty()) {
            QuestManager.discardQuest(player, id);
            player.sendSystemMessage(Component.literal("No valid player target found for this mission."));
            return;
        }

        UUID targetUuid = possibleTargets.get(new Random().nextInt(possibleTargets.size()));
        targetByPlayer.put(player.getUUID(), targetUuid);

        ServerPlayer targetOnline = player.server.getPlayerList().getPlayer(targetUuid);
        if (targetOnline != null) {
            player.sendSystemMessage(Component.literal("Target selected: " + targetOnline.getName().getString()));
        } else {
            Optional<StoredData> data = BeyonderData.beyonderMap.get(targetUuid);
            String name = data.isPresent() ? data.get().trueName() : targetUuid.toString();
            player.sendSystemMessage(Component.literal("Target selected: " + name + " (offline)"));
        }
    }

    @Override
    protected void onPlayerKillLiving(ServerPlayer player, LivingEntity victim) {
        if (!(victim instanceof Player killedPlayer)) {
            return;
        }

        UUID targetUuid = targetByPlayer.get(player.getUUID());
        if (targetUuid == null || !targetUuid.equals(killedPlayer.getUUID())) {
            return;
        }

        QuestManager.progressQuest(player, id, 1f);
    }

    @Override
    public List<ItemStack> getRewards(ServerPlayer player) {
        List<ItemStack> rewards = new ArrayList<>();
        int currentSequence = BeyonderData.getSequence(player);
        int rewardSequence = Math.min(9, currentSequence + 1);

        QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);
        int completedQuestCount = component.getCompletedQuests().size();

        long randomSeed = (player.getUUID().getLeastSignificantBits() ^ player.getUUID().getMostSignificantBits()) + completedQuestCount;
        Random random = new Random(randomSeed);

        BeyonderPotion potion = PotionItemHandler.selectRandomPotionOfSequence(random, rewardSequence);
        if (potion != null) {
            rewards.add(new ItemStack(potion));
        }
        return rewards;
    }

    @Override
    public float getDigestionReward() {
        return .5f;
    }

    @Override
    public MutableComponent getDescription() {
        return Component.translatable("lotm.quest.impl." + id + ".description");
    }
}