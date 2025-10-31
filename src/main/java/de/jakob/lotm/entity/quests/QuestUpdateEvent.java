package de.jakob.lotm.entity.quests;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;

/**
 * Event data passed to quests when something happens that might update progress
 */
public class QuestUpdateEvent {
    private final EventType type;
    private LivingEntity killedEntity;
    private ItemStack collectedItem;
    private BlockPos visitedLocation;
    private VillagerProfession talkedToProfession;
    private EntityType<?> entityType;
    
    private QuestUpdateEvent(EventType type) {
        this.type = type;
    }
    
    public static QuestUpdateEvent mobKilled(LivingEntity entity) {
        QuestUpdateEvent event = new QuestUpdateEvent(EventType.MOB_KILLED);
        event.killedEntity = entity;
        event.entityType = entity.getType();
        return event;
    }
    
    public static QuestUpdateEvent itemCollected(ItemStack item) {
        QuestUpdateEvent event = new QuestUpdateEvent(EventType.ITEM_COLLECTED);
        event.collectedItem = item;
        return event;
    }
    
    public static QuestUpdateEvent locationVisited(BlockPos pos) {
        QuestUpdateEvent event = new QuestUpdateEvent(EventType.LOCATION_VISITED);
        event.visitedLocation = pos;
        return event;
    }
    
    public static QuestUpdateEvent villagerTalked(VillagerProfession profession) {
        QuestUpdateEvent event = new QuestUpdateEvent(EventType.VILLAGER_TALKED);
        event.talkedToProfession = profession;
        return event;
    }
    
    public EventType getType() {
        return type;
    }
    
    public LivingEntity getKilledEntity() {
        return killedEntity;
    }
    
    public ItemStack getCollectedItem() {
        return collectedItem;
    }
    
    public BlockPos getVisitedLocation() {
        return visitedLocation;
    }
    
    public VillagerProfession getTalkedToProfession() {
        return talkedToProfession;
    }
    
    public EntityType<?> getEntityType() {
        return entityType;
    }
    
    public enum EventType {
        MOB_KILLED,
        ITEM_COLLECTED,
        LOCATION_VISITED,
        VILLAGER_TALKED
    }
}