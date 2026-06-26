package de.jakob.lotm.quest.impl;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.QuestComponent;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.beyonders.potions.PotionRecipeItem;
import de.jakob.lotm.beyonders.potions.PotionRecipeItemHandler;
import de.jakob.lotm.quest.Quest;
import de.jakob.lotm.quest.QuestManager;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
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
        return new ArrayList<>(currencyRewardForSequence(9, new Random()));
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
