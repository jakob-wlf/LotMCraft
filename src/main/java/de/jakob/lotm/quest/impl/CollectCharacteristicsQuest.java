package de.jakob.lotm.quest.impl;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.QuestComponent;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.potions.PotionItemHandler;
import de.jakob.lotm.quest.Quest;
import de.jakob.lotm.quest.QuestManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CollectCharacteristicsQuest extends Quest {

    private final int requiredAmount;

    public CollectCharacteristicsQuest(String id, int sequence, int requiredAmount) {
        super(id, sequence);
        this.requiredAmount = requiredAmount;
    }

    @Override
    public void tick(ServerPlayer player) {
        int matchingCharacteristics = countLowSequenceCharacteristics(player);
        if (matchingCharacteristics < requiredAmount) {
            return;
        }

        removeLowSequenceCharacteristics(player, requiredAmount);
        QuestManager.progressQuest(player, id, 1f);
    }

    @Override
    public List<ItemStack> getRewards(ServerPlayer player) {
        List<ItemStack> rewards = new ArrayList<>();

        QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);
        int completedQuestCount = component.getCompletedQuests().size();

        long randomSeed = (player.getUUID().getLeastSignificantBits() ^ player.getUUID().getMostSignificantBits()) + completedQuestCount;
        Random random = new Random(randomSeed);

        int rewardSequence = random.nextBoolean() ? 5 : 6;
        BeyonderPotion potion = PotionItemHandler.selectRandomPotionOfSequence(random, rewardSequence);
        if (potion != null) {
            rewards.add(new ItemStack(potion));
        }
        return rewards;
    }

    @Override
    public float getDigestionReward() {
        return 0.5f;
    }

    @Override
    public MutableComponent getDescription() {
        return Component.translatable("lotm.quest.impl." + id + ".description", requiredAmount);
    }

    private int countLowSequenceCharacteristics(ServerPlayer player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof BeyonderCharacteristicItem characteristic && characteristic.getSequence() >= 7) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void removeLowSequenceCharacteristics(ServerPlayer player, int amountToRemove) {
        int remaining = amountToRemove;
        for (ItemStack stack : player.getInventory().items) {
            if (remaining <= 0) {
                break;
            }
            if (!(stack.getItem() instanceof BeyonderCharacteristicItem characteristic) || characteristic.getSequence() < 7) {
                continue;
            }

            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
        }
    }
}