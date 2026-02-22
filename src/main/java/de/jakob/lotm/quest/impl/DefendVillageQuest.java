package de.jakob.lotm.quest.impl;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.QuestComponent;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.entity.custom.goals.KillOutsideRadiusGoal;
import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.potions.PotionItemHandler;
import de.jakob.lotm.potions.PotionRecipeItem;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
import de.jakob.lotm.quest.Quest;
import de.jakob.lotm.quest.QuestManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.minecraft.world.item.Items.DIAMOND;

public class DefendVillageQuest extends Quest {

    int monsterAmount = 42;

    public DefendVillageQuest(String id, int sequence) {
        super(id, sequence);
    }

    @Override
    public List<ItemStack> getRewards(ServerPlayer player) {
        ArrayList<ItemStack> rewards = new ArrayList<>();

        if(BeyonderData.isBeyonder(player) && BeyonderData.implementedRecipes.containsKey(BeyonderData.getPathway(player))) {
            String pathway = BeyonderData.getPathway(player);
            BeyonderPotion potion = PotionItemHandler.selectPotionOfPathwayAndSequence(new Random(), pathway, 8);
            if(potion != null) {
                rewards.add(new ItemStack(potion));
            }
        }
        else {
            Random random = new Random(player.getUUID().getMostSignificantBits() ^ player.getUUID().getLeastSignificantBits());
            BeyonderPotion potion = PotionItemHandler.selectRandomPotionOfSequence(random, 8);
            if(potion != null) {
                rewards.add(new ItemStack(potion));
            }
        }

        rewards.add(new ItemStack(DIAMOND, 5));
        return rewards;
    }

    @Override
    public float getDigestionReward() {
        return .3f;
    }

    @Override
    public void startQuest(ServerPlayer player) {
        for(int i = 0; i < 40; i++) {
            Entity entity = createRandomMonster(player.serverLevel());
            entity.setPos(player.getX() + (new Random().nextDouble() - 0.5) * 50, player.getY() + 1, player.getZ() + (new Random().nextDouble() - 0.5) * 50);
            entity.getPersistentData().putUUID("lotm_quest_defend_village", player.getUUID());
            player.serverLevel().addFreshEntity(entity);

            if(entity instanceof Mob mob) {
                mob.goalSelector.addGoal(0, new KillOutsideRadiusGoal(mob, player.position(), 40));
                mob.setTarget(player);
            }
        }
    }

    @Override
    protected void onLivingDeath(LivingEntity entity) {
        if(!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if(!entity.getPersistentData().hasUUID("lotm_quest_defend_village")) {
            return;
        }

        Entity uuidEntity = serverLevel.getEntity(entity.getPersistentData().getUUID("lotm_quest_defend_village"));
        if(!(uuidEntity instanceof ServerPlayer player)) {
            return;
        }
        QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);
        if(!component.getQuestProgress().containsKey(id)) {
            return;
        }

        float progress = 1f / (monsterAmount - 5);
        QuestManager.progressQuest(player, id, progress);
    }

    private Entity createRandomMonster(ServerLevel level) {
        return switch (new Random().nextInt(7)) {
            default -> new Husk(EntityType.HUSK, level);
            case 1 -> new Pillager(EntityType.PILLAGER, level);
            case 2 -> new Spider(EntityType.SPIDER, level);
            case 3 -> new EnderMan(EntityType.ENDERMAN, level);
            case 4 -> new Witch(EntityType.WITCH, level);
        };
    }


    @Override
    public boolean canGiveQuest(BeyonderNPCEntity npc) {
        if(!(npc.level() instanceof ServerLevel level))
            return false;

        int sequence = BeyonderData.getSequence(npc);
        if(sequence < 6) {
            return false;
        }

        if(AbilityUtil.getNearbyEntities(null, level, npc.position(), 60).stream().filter(e -> e instanceof Villager).count() < 3) {
            return false;
        }

        return true;
    }
}
