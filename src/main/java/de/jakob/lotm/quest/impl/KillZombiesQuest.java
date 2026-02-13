package de.jakob.lotm.quest.impl;

import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.potions.PotionRecipeItem;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
import de.jakob.lotm.potions.PotionRecipes;
import de.jakob.lotm.quest.Quest;
import de.jakob.lotm.quest.QuestManager;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.minecraft.world.item.Items.IRON_INGOT;

public class KillZombiesQuest extends Quest {

    private final int amount;

    public KillZombiesQuest(String id, int amount, int sequence) {
        super(id, sequence);
        this.amount = amount;
    }

    @Override
    public List<ItemStack> getRewards(ServerPlayer player) {
        ArrayList<ItemStack> rewards = new ArrayList<>();

        if(BeyonderData.isBeyonder(player) && BeyonderData.implementedRecipes.containsKey(BeyonderData.getPathway(player))) {
            String pathway = BeyonderData.getPathway(player);
            PotionRecipeItem potionRecipeItem = PotionRecipeItemHandler.selectRecipeOfPathwayAndSequence(pathway, 9);
            if(potionRecipeItem != null) {
                rewards.add(new ItemStack(potionRecipeItem));
            }
        }
        else {
            Random random = new Random(player.getUUID().getMostSignificantBits() ^ player.getUUID().getLeastSignificantBits());
            PotionRecipeItem recipe = PotionRecipeItemHandler.selectRandomRecipeOfSequence(random, 9);
            if(recipe != null) {
                rewards.add(new ItemStack(recipe));
            }
        }
        rewards.add(new ItemStack(IRON_INGOT, 10));
        return rewards;
    }

    @Override
    public float getDigestionReward() {
        return .2f;
    }

    @Override
    public void tick(ServerPlayer player) {

    }

    @Override
    public boolean canGiveQuest(BeyonderNPCEntity npc) {
        int sequence = BeyonderData.getSequence(npc);
        if(sequence < 7) {
            return false;
        }
        return true;
    }

    @Override
    public MutableComponent getDescription() {
        return Component.translatable("lotm.quest.impl." + id + ".description", amount);
    }

    @Override
    protected void onPlayerKillLiving(ServerPlayer player, LivingEntity victim) {
        if(!(victim instanceof Zombie)) {
            return;
        }

        QuestManager.progressQuest(player, id, 1f / amount);
    }
}
