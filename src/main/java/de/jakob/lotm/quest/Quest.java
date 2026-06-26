package de.jakob.lotm.quest;

import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    protected List<ItemStack> currencyRewardForSequence(int seq, Random random) {
        int[] charPriceInSoli = {0, 2560, 1920, 1600, 1100, 400, 200, 100, 30, 10};
        int fullPrice = charPriceInSoli[Math.clamp(seq, 1, 9)];

        long rewardSoli = (long) (fullPrice * 0.22);

        int variancePct = 80 + random.nextInt(41); // 80..120
        rewardSoli = Math.max(1, rewardSoli * variancePct / 100);

        final int SOLI_PER_POUND = 20;
        final int MAX_STACK = 64;

        int pounds = (int) Math.min(rewardSoli / SOLI_PER_POUND, MAX_STACK);
        long remainingSoli = rewardSoli - ((long) pounds * SOLI_PER_POUND);
        int soli = (int) Math.min(remainingSoli, MAX_STACK);

        List<ItemStack> result = new ArrayList<>();
        if (pounds > 0) result.add(new ItemStack(ModItems.ONE_POUND.get(), pounds));
        if (soli > 0)   result.add(new ItemStack(ModItems.ONE_SOLI.get(), soli));
        return result;
    }

    public String getId() {
        return id;
    }

    public int getSequence() {
        return sequence;
    }

    public abstract List<ItemStack> getRewards(ServerPlayer player);

    public abstract float getDigestionReward();

    public float getDigestionReward(ServerPlayer player) {
        return getDigestionReward();
    }

    public boolean shouldScaleDigestionBySequence() {
        return true;
    }

    public MutableComponent getDescription() {
        return Component.translatable("lotm.quest.impl." + id + ".description");
    }

    public MutableComponent getDescription(ServerPlayer player) {
        return getDescription();
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