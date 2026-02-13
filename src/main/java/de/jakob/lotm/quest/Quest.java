package de.jakob.lotm.quest;

import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public abstract class Quest {
    protected final String id;
    protected final int sequence;

    public Quest(String id, int sequence) {
        this.id = id;
        this.sequence = sequence;
    }

    public void tick(ServerPlayer player) {

    };

    protected void onPlayerKillLiving(ServerPlayer player, LivingEntity victim) {
    }

    protected void onLivingDeath(LivingEntity entity) {
    }

    public void startQuest(ServerPlayer player) {
    }


    public String getId() {
        return id;
    }

    public int getSequence() {
        return sequence;
    }

    public abstract List<ItemStack> getRewards(ServerPlayer player);

    public abstract float getDigestionReward();

    public MutableComponent getDescription() {
        return Component.translatable("lotm.quest.impl." + id + ".description");
    }

    public MutableComponent getName() {
        return Component.translatable("lotm.quest.impl." + id);
    }

    public boolean canAccept(ServerPlayer player) {
        return true;
    }

    public boolean canGiveQuest(BeyonderNPCEntity npc) {
        return true;
    }

    public MutableComponent getLore() {
        return Component.translatable("lotm.quest.impl." + id + ".lore");
    }
}