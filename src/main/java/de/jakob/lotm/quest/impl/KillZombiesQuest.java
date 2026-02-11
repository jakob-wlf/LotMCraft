package de.jakob.lotm.quest.impl;

import de.jakob.lotm.quest.Quest;
import de.jakob.lotm.quest.QuestManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static net.minecraft.world.item.Items.IRON_INGOT;

public class KillZombiesQuest extends Quest {

    private final int amount;

    public KillZombiesQuest(String id, int amount) {
        super(id);
        this.amount = amount;
    }

    @Override
    public List<ItemStack> getRewards() {
        return List.of(
                new ItemStack(IRON_INGOT, 5)
        );
    }

    @Override
    public float getDigestionReward() {
        return 0.05f;
    }

    @Override
    public void tick(ServerPlayer player) {

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
