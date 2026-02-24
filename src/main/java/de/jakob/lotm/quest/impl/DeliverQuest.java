package de.jakob.lotm.quest.impl;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.QuestComponent;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.quest.Quest;
import de.jakob.lotm.quest.QuestManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DeliverQuest extends Quest {

    public DeliverQuest(String id, int sequence) {
        super(id, sequence);
    }

    @Override
    public void startQuest(ServerPlayer player) {
        BlockPos chestPos = createDeliveryChest(player.serverLevel(), player.blockPosition());
        if (chestPos == null) {
            QuestManager.discardQuest(player, id);
            player.sendSystemMessage(Component.literal("Could not find delivery location for chest."));
            return;
        }

        QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);
        component.getQuestLocation().put(id, Vec3.atCenterOf(chestPos));

        player.addItem(new ItemStack(Items.AMETHYST_SHARD));

        // Show only one coordinate as requested.
        if (new Random().nextBoolean()) {
            player.sendSystemMessage(Component.literal("Delivery clue: X = " + chestPos.getX()));
        } else {
            player.sendSystemMessage(Component.literal("Delivery clue: Z = " + chestPos.getZ()));
        }
    }

    @Override
    public boolean canAccept(ServerPlayer player) {
        return true;
    }

    @Override
    public boolean canGiveQuest(BeyonderNPCEntity npc) {
        return true;
    }

    @Override
    public void tick(ServerPlayer player) {
        QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);
        if (!component.getQuestLocation().containsKey(id)) {
            return;
        }

        BlockPos chestPos = BlockPos.containing(component.getQuestLocation().get(id));
        if (!(player.serverLevel().getBlockEntity(chestPos) instanceof ChestBlockEntity chest)) {
            return;
        }

        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (stack.is(Items.AMETHYST_SHARD) && !stack.isEmpty()) {
                stack.shrink(1);
                QuestManager.progressQuest(player, id, 1f);
                return;
            }
        }
    }

    @Override
    public List<ItemStack> getRewards(ServerPlayer player) {
        List<ItemStack> rewards = new ArrayList<>();
        rewards.add(new ItemStack(Items.DIAMOND, 3));
        return rewards;
    }

    @Override
    public float getDigestionReward() {
        return .25f;
    }

    @Override
    public MutableComponent getDescription() {
        return Component.translatable("lotm.quest.impl." + id + ".description");
    }

    private BlockPos createDeliveryChest(ServerLevel level, BlockPos origin) {
        Random random = new Random();
        for (int attempt = 0; attempt < 120; attempt++) {
            // Match KillBeyonderQuest structure distance style: radius 75-150.
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = 75 + random.nextDouble() * 75;
            int x = origin.getX() + (int) (Math.cos(angle) * distance);
            int z = origin.getZ() + (int) (Math.sin(angle) * distance);

            BlockPos topPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, origin.getY(), z));
            BlockPos chestPos = topPos.above();

            if (!level.getBlockState(chestPos).canBeReplaced()) {
                continue;
            }
            if (level.getBlockState(chestPos.below()).isAir()) {
                continue;
            }

            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
            return chestPos;
        }
        return null;
    }
}