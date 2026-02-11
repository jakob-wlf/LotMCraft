package de.jakob.lotm.quest;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;

public abstract class Quest {

    protected final String id;

    public Quest(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract List<ItemStack> getRewards();
    public abstract float getDigestionReward();

    public abstract void tick(ServerPlayer player);

    protected void onPlayerKillLiving(ServerPlayer player, LivingEntity victim) {
    }

    public MutableComponent getDescription() {
        return Component.translatable("lotm.quest.impl." + id + ".description");
    }

    public MutableComponent getName() {
        return Component.translatable("lotm.quest.impl." + id);
    }
}
