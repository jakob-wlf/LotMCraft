package de.jakob.lotm.quest.impl;

import de.jakob.lotm.quest.Quest;
import de.jakob.lotm.quest.QuestManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.StoredData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class KillPlayerQuest extends Quest {

    private final java.util.Map<UUID, UUID> targetByPlayer = new java.util.HashMap<>();

    public KillPlayerQuest(String id, int sequence) {
        super(id, sequence);
    }

    @Override
    public void startQuest(ServerPlayer player) {
        int questTakerSeq = BeyonderData.getSequence(player);

        List<UUID> possibleTargets = BeyonderData.playerMap.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(player.getUUID()))
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> {
                    int targetSeq = entry.getValue().sequence();
                    return targetSeq >= questTakerSeq - 1 && targetSeq <= questTakerSeq + 1;
                })
                .map(java.util.Map.Entry::getKey)
                .toList();

        if (possibleTargets.isEmpty()) {
            QuestManager.discardQuest(player, id);
            player.sendSystemMessage(Component.literal("No valid player target found for this mission."));
            return;
        }

        UUID targetUuid = possibleTargets.get(new Random().nextInt(possibleTargets.size()));
        targetByPlayer.put(player.getUUID(), targetUuid);

        ServerPlayer targetOnline = player.server.getPlayerList().getPlayer(targetUuid);
        if (targetOnline != null) {
            player.sendSystemMessage(Component.literal("Target selected: " + targetOnline.getName().getString()));
        } else {
            Optional<StoredData> data = BeyonderData.playerMap.get(targetUuid);
            String name = data.isPresent() ? data.get().trueName() : targetUuid.toString();
            player.sendSystemMessage(Component.literal("Target selected: " + name + " (offline)"));
        }
    }

    @Override
    protected void onPlayerKillLiving(ServerPlayer player, LivingEntity victim) {
        if (!(victim instanceof Player killedPlayer)) {
            return;
        }

        UUID targetUuid = targetByPlayer.get(player.getUUID());
        if (targetUuid == null || !targetUuid.equals(killedPlayer.getUUID())) {
            return;
        }

        QuestManager.progressQuest(player, id, 1f);
    }

    @Override
    public List<ItemStack> getRewards(ServerPlayer player) {
        int rewardSequence = Math.min(9, BeyonderData.getSequence(player) + 1);
        UUID targetUuid = targetByPlayer.get(player.getUUID());
        if (targetUuid != null) {
            ServerPlayer targetOnline = player.server.getPlayerList().getPlayer(targetUuid);
            if (targetOnline != null) {
                rewardSequence = BeyonderData.getSequence(targetOnline);
            } else {
                Optional<StoredData> data = BeyonderData.playerMap.get(targetUuid);
                if (data.isPresent()) rewardSequence = data.get().sequence();
            }
        }
        return new ArrayList<>(currencyRewardForSequence(rewardSequence, new Random()));
    }

    @Override
    public float getDigestionReward() {
        return .4f;
    }




    @Override
    public MutableComponent getDescription(ServerPlayer player) {
        UUID targetUuid = targetByPlayer.get(player.getUUID());
        if (targetUuid == null) {
            return Component.translatable("lotm.quest.impl." + id + ".description");
        }

        ServerPlayer targetOnline = player.server.getPlayerList().getPlayer(targetUuid);
        String targetName;
        if (targetOnline != null) {
            targetName = targetOnline.getName().getString();
        } else {
            Optional<StoredData> data = BeyonderData.playerMap.get(targetUuid);
            targetName = data.map(StoredData::trueName).orElse(targetUuid.toString());
        }

        return Component.translatable("lotm.quest.impl." + id + ".description").append(" Target: " + targetName);
    }



}