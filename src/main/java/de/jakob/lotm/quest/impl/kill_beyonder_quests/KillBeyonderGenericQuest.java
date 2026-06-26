package de.jakob.lotm.quest.impl.kill_beyonder_quests;

import de.jakob.lotm.beyonders.potions.BeyonderPotion;
import de.jakob.lotm.beyonders.potions.PotionItemHandler;
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
        return new ArrayList<>(currencyRewardForSequence(beyonderSequence, new Random()));
    }

    @Override
    public float getDigestionReward() {
        return .2f;
    }
}
