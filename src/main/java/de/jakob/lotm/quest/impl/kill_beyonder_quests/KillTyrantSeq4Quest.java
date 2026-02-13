package de.jakob.lotm.quest.impl.kill_beyonder_quests;

import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.potions.PotionItemHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KillTyrantSeq4Quest extends KillBeyonderQuest {

    public KillTyrantSeq4Quest(String id, int sequence) {
        super(id, sequence, "tyrant", 4);
    }

    @Override
    public List<ItemStack> getRewards(ServerPlayer player) {
        ArrayList<ItemStack> rewards = new ArrayList<>();
        BeyonderPotion potion = PotionItemHandler.selectPotionOfPathwayAndSequence(new Random(), "tyrant", 4);
        if(potion != null) {
            rewards.add(new ItemStack(potion));
        }
        rewards.add(new ItemStack(Items.DIAMOND, 25));
        rewards.add(new ItemStack(Items.PRISMARINE, 64));
        return rewards;
    }

    @Override
    public float getDigestionReward() {
        return .5f;
    }
}
