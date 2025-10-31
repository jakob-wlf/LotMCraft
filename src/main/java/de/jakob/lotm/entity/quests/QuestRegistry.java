package de.jakob.lotm.entity.quests;

import de.jakob.lotm.entity.quests.impl.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Registry and factory for creating quests
 * Add your custom quests here!
 */
public class QuestRegistry {
    private static final HashMap<String, Quest> registeredQuests = new HashMap<>();
    private static final List<String> questPool = new ArrayList<>();
    
    /**
     * Initialize all quests here
     * This is where you configure your quests!
     */
    public static void registerQuests() {
        // Example: Kill zombies quest
        Quest zombieQuest = new KillMobsQuest(
                "kill_zombies",
                "Zombie Hunter",
                "Kill 10 zombies",
                EntityType.ZOMBIE,
                10
        ).addReward(new ItemStack(Items.DIAMOND, 3))
         .setExperienceReward(100);
        
        register(zombieQuest);
        
        // Example: Kill skeletons quest
        Quest skeletonQuest = new KillMobsQuest(
                "kill_skeletons",
                "Bone Collector",
                "Defeat 5 skeletons",
                EntityType.SKELETON,
                5
        ).addReward(new ItemStack(Items.EMERALD, 5))
         .setExperienceReward(75);
        
        register(skeletonQuest);
        
        // Example: Collect items quest
        Quest collectIronQuest = new CollectItemsQuest(
                "collect_iron",
                "Iron Delivery",
                "Bring me 16 iron ingots",
                Items.IRON_INGOT,
                16,
                true // Items will be consumed
        ).addReward(new ItemStack(Items.DIAMOND, 2))
         .setExperienceReward(50);
        
        register(collectIronQuest);
        
        // Example: Visit location quest (location will be random)
        Quest visitLocationQuest = new VisitLocationQuest(
                "visit_location",
                "Exploration",
                "Visit the marked location",
                new BlockPos(0, 64, 0), // This will be randomized when assigned
                10, // Detection radius
                true // Place diamond block marker
        ).addReward(new ItemStack(Items.GOLDEN_APPLE, 2))
         .setExperienceReward(100);
        
        register(visitLocationQuest);
        
        // Example: Talk to farmer quest
        Quest talkToFarmerQuest = new TalkToVillagerQuest(
                "talk_to_farmer",
                "Village Gossip",
                "Talk to a farmer villager",
                VillagerProfession.FARMER
        ).addReward(new ItemStack(Items.EMERALD, 3))
         .setExperienceReward(25);
        
        register(talkToFarmerQuest);
        
        // Example: Compound quest (kill zombies and collect items)
//        Quest compoundQuest1 = new CompoundQuest(
//                "zombie_and_iron",
//                "Supply Run",
//                "Kill zombies and collect iron"
//        ).addSubQuest(new KillMobsQuest(
//                "sub_kill_zombies",
//                "",
//                "",
//                EntityType.ZOMBIE,
//                5
//        )).addSubQuest(new CollectItemsQuest(
//                "sub_collect_iron",
//                "",
//                "",
//                Items.IRON_INGOT,
//                8,
//                true
//        )).addReward(new ItemStack(Items.DIAMOND, 5))
//          .addReward(new ItemStack(Items.ENCHANTED_BOOK))
//          .setExperienceReward(200);
//
//        register(compoundQuest1);
        
        // Example: Compound quest (visit location and get item)
//        Quest compoundQuest2 = new CompoundQuest(
//                "exploration_and_retrieval",
//                "Treasure Hunt",
//                "Go to the marked location and retrieve the treasure"
//        ).addSubQuest(new VisitLocationQuest(
//                "sub_visit",
//                "",
//                "",
//                new BlockPos(0, 64, 0),
//                10,
//                true
//        )).addSubQuest(new CollectItemsQuest(
//                "sub_get_gold",
//                "",
//                "",
//                Items.GOLD_INGOT,
//                4,
//                false // Don't consume, just check
//        )).addReward(new ItemStack(Items.DIAMOND_BLOCK))
//          .setExperienceReward(150);
//
//        register(compoundQuest2);
    }
    
    /**
     * Register a quest to the system
     */
    public static void register(Quest quest) {
        registeredQuests.put(quest.getQuestId(), quest);
        questPool.add(quest.getQuestId());
    }
    
    /**
     * Get a random quest from the pool
     */
    public static Quest getRandomQuest(Random random, BlockPos npcPosition) {
        if (questPool.isEmpty()) {
            return null;
        }
        
        String questId = questPool.get(random.nextInt(questPool.size()));
        Quest template = registeredQuests.get(questId);
        
        // Create a new instance with randomized parameters
        return createQuestInstance(template, random, npcPosition);
    }
    
    /**
     * Create a quest instance from a template, randomizing location-based quests
     */
    private static Quest createQuestInstance(Quest template, Random random, BlockPos npcPosition) {
        if (template instanceof VisitLocationQuest visitQuest) {
            // Randomize location near NPC
            int offsetX = random.nextInt(200) - 100;
            int offsetZ = random.nextInt(200) - 100;
            BlockPos newLocation = npcPosition.offset(offsetX, 0, offsetZ);
            
            return new VisitLocationQuest(
                    visitQuest.getQuestId(),
                    visitQuest.getTitle(),
                    visitQuest.getDescription(),
                    newLocation,
                    visitQuest.getDetectionRadius(),
                    true
            ).setExperienceReward(visitQuest.getExperienceReward());
            
        } else if (template instanceof CompoundQuest compoundQuest) {
            CompoundQuest newCompound = new CompoundQuest(
                    compoundQuest.getQuestId(),
                    compoundQuest.getTitle(),
                    compoundQuest.getDescription()
            );
            
            for (Quest subQuest : compoundQuest.getSubQuests()) {
                newCompound.addSubQuest(createQuestInstance(subQuest, random, npcPosition));
            }
            
            for (ItemStack reward : compoundQuest.getRewards()) {
                newCompound.addReward(reward.copy());
            }
            newCompound.setExperienceReward(compoundQuest.getExperienceReward());
            
            return newCompound;
        } else if (template instanceof KillMobsQuest killQuest) {
            return new KillMobsQuest(
                    killQuest.getQuestId(),
                    killQuest.getTitle(),
                    killQuest.getDescription(),
                    killQuest.getTargetMob(),
                    killQuest.getRequiredKills()
            ).setExperienceReward(killQuest.getExperienceReward());
            
        } else if (template instanceof CollectItemsQuest collectQuest) {
            return new CollectItemsQuest(
                    collectQuest.getQuestId(),
                    collectQuest.getTitle(),
                    collectQuest.getDescription(),
                    collectQuest.getTargetItem(),
                    collectQuest.getRequiredAmount(),
                    collectQuest.shouldConsumeItems()
            ).setExperienceReward(collectQuest.getExperienceReward());
            
        } else if (template instanceof TalkToVillagerQuest talkQuest) {
            return new TalkToVillagerQuest(
                    talkQuest.getQuestId(),
                    talkQuest.getTitle(),
                    talkQuest.getDescription(),
                    talkQuest.getTargetProfession()
            ).setExperienceReward(talkQuest.getExperienceReward());
        }
        
        return template;
    }
    
    /**
     * Load a quest from NBT
     */
    public static Quest loadQuestFromNBT(CompoundTag tag) {
        String questType = tag.getString("QuestType");
        
        Quest quest = switch (questType) {
            case "kill_mobs" -> new KillMobsQuest("", "", "", EntityType.ZOMBIE, 0);
            case "collect_items" -> new CollectItemsQuest("", "", "", Items.AIR, 0, false);
            case "visit_location" -> new VisitLocationQuest("", "", "", BlockPos.ZERO, 0, false);
            case "talk_to_villager" -> new TalkToVillagerQuest("", "", "", VillagerProfession.NONE);
            case "compound" -> new CompoundQuest("", "", "");
            default -> null;
        };
        
        if (quest != null) {
            quest.loadFromNBT(tag);
        }
        
        return quest;
    }
    
    public static Quest getQuestById(String questId) {
        return registeredQuests.get(questId);
    }
}