package de.jakob.lotm.quest.impl.kill_beyonder_quests;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.QuestComponent;
import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.potions.PotionItemHandler;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KillBeyonderGenericQuest extends KillBeyonderQuest {

    private final int beyonderSequence;

    public KillBeyonderGenericQuest(String id, int sequence, int beyonderSequence) {
        super(id, sequence, "", beyonderSequence);
        this.beyonderSequence = beyonderSequence;
    }

    @Override
    public List<ItemStack> getRewards(ServerPlayer player) {
        ArrayList<ItemStack> rewards = new ArrayList<>();

        if(BeyonderData.isBeyonder(player) && BeyonderData.implementedRecipes.containsKey(BeyonderData.getPathway(player))) {
            String pathway = BeyonderData.getPathway(player);
            BeyonderPotion potion = PotionItemHandler.selectPotionOfPathwayAndSequence(new Random(), pathway, beyonderSequence);
            if(potion != null) {
                rewards.add(new ItemStack(potion));
            }
        }
        else {
            QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);
            int completedQuestCount = component.getCompletedQuests().size();

            long randomSeed = (player.getUUID().getLeastSignificantBits() ^ player.getUUID().getMostSignificantBits()) + completedQuestCount;
            Random random = new Random(randomSeed);

            BeyonderPotion potion = PotionItemHandler.selectRandomPotionOfSequence(random, beyonderSequence);
            if(potion != null) {
                rewards.add(new ItemStack(potion));
            }
        }
        return rewards;
    }

    @Override
    public float getDigestionReward() {
        return .5f;
    }
}
