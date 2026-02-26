package de.jakob.lotm.quest.impl;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.QuestComponent;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.entity.custom.goals.KillOutsideRadiusGoal;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.potions.PotionItemHandler;
import de.jakob.lotm.quest.Quest;
import de.jakob.lotm.quest.QuestManager;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class HelpBeyonderQuest extends Quest {

    private static final String QUEST_OWNER_TAG = "lotm_quest_help_beyonder_owner";
    private static final String QUEST_ID_TAG = "lotm_quest_help_beyonder_id";

    private final int killAmount;
    private final Random random = new Random();

    public HelpBeyonderQuest(String id, int sequence, int killAmount) {
        super(id, sequence);
        this.killAmount = killAmount;
    }

    @Override
    public void startQuest(ServerPlayer player) {
        spawnEnemies(player);
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Help fend off the enemy wave!"));
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
    protected void onLivingDeath(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        if (!entity.getPersistentData().hasUUID(QUEST_OWNER_TAG)
                || !id.equals(entity.getPersistentData().getString(QUEST_ID_TAG))) {
            return;
        }

        ServerPlayer player = level.getServer().getPlayerList().getPlayer(entity.getPersistentData().getUUID(QUEST_OWNER_TAG));
        if (player == null) {
            return;
        }

        QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);
        if (!component.getQuestProgress().containsKey(id)) {
            return;
        }

        float progressPerKill = entity.getPersistentData().contains("lotm_quest_help_beyonder_progress_per_kill")
                ? entity.getPersistentData().getFloat("lotm_quest_help_beyonder_progress_per_kill")
                : 0f;
        if (progressPerKill <= 0f) {
            return;
        }

        QuestManager.progressQuest(player, id, progressPerKill);
    }

    @Override
    public List<ItemStack> getRewards(ServerPlayer player) {
        List<ItemStack> rewards = new ArrayList<>();
        Tier tier = tierFor(player);

        QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);
        int completedQuestCount = component.getCompletedQuests().size();

        long randomSeed = (player.getUUID().getLeastSignificantBits() ^ player.getUUID().getMostSignificantBits()) + completedQuestCount;
        Random r = new Random(randomSeed);

        switch (tier) {
            case LOW -> addCharacteristicInRange(rewards, 8, 9, r);
            case MID -> {
                addCharacteristicInRange(rewards, 5, 7, r);
                rewards.add(new ItemStack(Items.DIAMOND, 8));
            }
            case HIGH -> {
                addCharacteristicInRange(rewards, 3, 4, r);
                rewards.add(new ItemStack(Items.NETHERITE_SCRAP, 2));
                rewards.add(new ItemStack(Items.DIAMOND, 10));
                rewards.add(new ItemStack(Items.GOLD_BLOCK, 2));
            }
            case TOP -> {


                int potionSequence = r.nextBoolean() ? 3 : 4;
                BeyonderPotion potion = getSequencePotion(potionSequence, r);
                if (potion != null) {
                    rewards.add(new ItemStack(potion));
                }
                addCharacteristicInRange(rewards, 1, 2, r);
                rewards.add(new ItemStack(Items.NETHER_STAR));
                rewards.add(new ItemStack(Items.BEACON));
            }
        }

        return rewards;
    }

    @Override
    public float getDigestionReward() {
        return .3f;
    }


    private void spawnEnemies(ServerPlayer player) {
        Tier tier = tierFor(player);
        int spawnCount = getScaledKillAmount(player);
        List<Integer> topTierPattern = tier == Tier.TOP ? buildTopTierPattern(spawnCount) : List.of();

        for (int i = 0; i < spawnCount; i++) {
            Entity entity = createScaledMonster(player.serverLevel(), player, tier, topTierPattern, i);
            entity.setPos(
                    player.getX() + (random.nextDouble() - 0.5) * 30,
                    player.getY() + 1,
                    player.getZ() + (random.nextDouble() - 0.5) * 30
            );
            entity.getPersistentData().putUUID(QUEST_OWNER_TAG, player.getUUID());
            entity.getPersistentData().putString(QUEST_ID_TAG, id);
            entity.getPersistentData().putFloat("lotm_quest_help_beyonder_progress_per_kill", 1f / spawnCount);
            player.serverLevel().addFreshEntity(entity);

            if (entity instanceof Mob mob) {
                mob.setPersistenceRequired();
                mob.goalSelector.addGoal(0, new KillOutsideRadiusGoal(mob, player.position(), 45));
                mob.setTarget(player);
            }
        }
    }

    private Entity createScaledMonster(ServerLevel level, ServerPlayer player, Tier tier, List<Integer> topTierPattern, int topTierIndex) {
        return switch (tier) {
            case LOW -> {
                int roll = random.nextInt(3);
                if (roll == 0) yield new Husk(net.minecraft.world.entity.EntityType.HUSK, level);
                if (roll == 1) yield new Spider(net.minecraft.world.entity.EntityType.SPIDER, level);
                yield new Skeleton(net.minecraft.world.entity.EntityType.SKELETON, level);
            }
            case MID -> {
                int roll = random.nextInt(4);
                if (roll == 0) yield new Witch(net.minecraft.world.entity.EntityType.WITCH, level);
                if (roll == 1) yield createPillagerWithCrossbow(level);
                if (roll == 2) yield createVindicator(level, false);
                yield new Ravager(net.minecraft.world.entity.EntityType.RAVAGER, level);
            }
            case HIGH -> {
                int roll = random.nextInt(3);
                if (roll == 0) yield createWarden(level, player);
                yield createVindicator(level, true);
            }
            case TOP -> createHostileBeyonder(level, player, topTierPattern.get(topTierIndex));
        };
    }

    private Entity createPillagerWithCrossbow(ServerLevel level) {
        Pillager pillager = new Pillager(net.minecraft.world.entity.EntityType.PILLAGER, level);
        pillager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
        return pillager;
    }

    private Entity createVindicator(ServerLevel level, boolean enchantedStyle) {
        Vindicator vindicator = new Vindicator(net.minecraft.world.entity.EntityType.VINDICATOR, level);
        ItemStack weapon = enchantedStyle ? new ItemStack(Items.NETHERITE_AXE) : new ItemStack(Items.IRON_AXE);
        weapon.set(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, enchantedStyle);
        vindicator.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        return vindicator;
    }

    private Entity createWarden(ServerLevel level, ServerPlayer player) {
        BlockPos spawnPos = BlockPos.containing(player.getX(), player.getY() + 1, player.getZ());
        Warden warden = net.minecraft.world.entity.EntityType.WARDEN.spawn(level, spawnPos, MobSpawnType.EVENT);
        if (warden == null) {
            warden = new Warden(net.minecraft.world.entity.EntityType.WARDEN, level);
            warden.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        }

        warden.setPersistenceRequired();
        warden.setTarget(player);
        boostWardenAnger(warden, player);
        return warden;
    }

    private void boostWardenAnger(Warden warden, ServerPlayer player) {
        try {
            for (var method : warden.getClass().getMethods()) {
                if (!method.getName().equals("increaseAngerAt")) {
                    continue;
                }
                Class<?>[] params = method.getParameterTypes();
                if (params.length >= 2 && net.minecraft.world.entity.Entity.class.isAssignableFrom(params[0])
                        && (params[1] == int.class || params[1] == Integer.class)) {
                    Object[] args = new Object[params.length];
                    args[0] = player;
                    args[1] = 150;
                    for (int i = 2; i < params.length; i++) {
                        args[i] = params[i] == boolean.class ? Boolean.TRUE : null;
                    }
                    method.invoke(warden, args);
                    return;
                }
            }
        } catch (Exception ignored) {
            // Keep behavior safe if method signature differs in current mappings.
        }
    }

    private Entity createHostileBeyonder(ServerLevel level, ServerPlayer player, int sequence) {
        String pathway = BeyonderData.implementedPathways.get(random.nextInt(BeyonderData.implementedPathways.size()));
        BeyonderNPCEntity npc = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), level, true, pathway, sequence);
        npc.setPersistenceRequired();
        npc.setTarget(player);
        return npc;
    }

    private List<Integer> buildTopTierPattern(int count) {
        List<Integer> pool = new ArrayList<>(List.of(2, 3, 3, 4, 4, 4));
        Collections.shuffle(pool, random);
        return new ArrayList<>(pool.subList(0, Math.min(count, pool.size())));
    }

    private int getScaledKillAmount(ServerPlayer player) {
        return switch (tierFor(player)) {
            case LOW -> Math.max(8, killAmount - 10);
            case MID -> Math.max(10, killAmount - 6);
            case HIGH -> Math.max(12, killAmount - 4);
            case TOP -> random.nextInt(6) + 1;
        };
    }

    private void addCharacteristicInRange(List<ItemStack> rewards, int minSeq, int maxSeq, Random r) {
        List<BeyonderCharacteristicItem> matching = BeyonderCharacteristicItemHandler.ITEMS.getEntries().stream()
                .map(DeferredHolder::get)
                .filter(BeyonderCharacteristicItem.class::isInstance)
                .map(BeyonderCharacteristicItem.class::cast)
                .filter(item -> item.getSequence() >= minSeq && item.getSequence() <= maxSeq)
                .sorted(Comparator.comparingInt(BeyonderCharacteristicItem::getSequence))
                .toList();

        if (!matching.isEmpty()) {
            rewards.add(new ItemStack(matching.get(r.nextInt(matching.size()))));
            return;
        }

        List<BeyonderCharacteristicItem> lowSeqFallback = BeyonderCharacteristicItemHandler.ITEMS.getEntries().stream()
                .map(DeferredHolder::get)
                .filter(BeyonderCharacteristicItem.class::isInstance)
                .map(BeyonderCharacteristicItem.class::cast)
                .filter(item -> item.getSequence() <= maxSeq)
                .toList();
        if (!lowSeqFallback.isEmpty()) {
            rewards.add(new ItemStack(lowSeqFallback.get(r.nextInt(lowSeqFallback.size()))));
        }
    }

    private BeyonderPotion getSequencePotion(int sequence, Random r) {
        List<BeyonderPotion> matching = PotionItemHandler.ITEMS.getEntries().stream()
                .map(DeferredHolder::get)
                .filter(BeyonderPotion.class::isInstance)
                .map(BeyonderPotion.class::cast)
                .filter(item -> item.getSequence() == sequence)
                .sorted(Comparator.comparing(BeyonderPotion::getPathway))
                .toList();

        if (matching.isEmpty()) {
            return PotionItemHandler.selectRandomPotionOfSequence(r, sequence);
        }
        return matching.get(0);
    }

    private Tier tierFor(ServerPlayer player) {
        int seq = BeyonderData.getSequence(player);
        if (seq >= 8) {
            return Tier.LOW;
        }
        if (seq >= 5) {
            return Tier.MID;
        }
        if (seq >= 3) {
            return Tier.HIGH;
        }
        return Tier.TOP;
    }

    private enum Tier {
        LOW,
        MID,
        HIGH,
        TOP
    }
}