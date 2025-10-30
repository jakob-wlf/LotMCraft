package de.jakob.lotm.entity.quests.impl;

import de.jakob.lotm.entity.quests.Quest;
import de.jakob.lotm.entity.quests.QuestUpdateEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * Quest type that requires visiting a specific location
 * Places a diamond block as a marker
 */
public class VisitLocationQuest extends Quest {
    private BlockPos targetLocation;
    private int detectionRadius;
    private boolean visited;
    private boolean placeMarker;
    
    public VisitLocationQuest(String questId, String title, String description, BlockPos targetLocation, int detectionRadius, boolean placeMarker) {
        super(questId, title, description);
        this.targetLocation = targetLocation;
        this.detectionRadius = detectionRadius;
        this.visited = false;
        this.placeMarker = placeMarker;
    }
    
    @Override
    public void onAccept(Player player) {
        super.onAccept(player);
        if (placeMarker) {
            player.level().setBlock(targetLocation, Blocks.DIAMOND_BLOCK.defaultBlockState(), 3);
        }
    }
    
    @Override
    public boolean checkCompletion(Player player, Level level) {
        if (!visited) {
            BlockPos playerPos = player.blockPosition();
            double distance = Math.sqrt(playerPos.distSqr(targetLocation));
            if (distance <= detectionRadius) {
                visited = true;
                if (placeMarker) {
                    level.removeBlock(targetLocation, false);
                }
                player.sendSystemMessage(Component.literal("§aLocation reached!"));
            }
        }
        return visited;
    }
    
    @Override
    public Component getProgressText() {
        if (visited) {
            return Component.literal("§aLocation visited");
        }
        return Component.literal("Visit X: " + targetLocation.getX() + ", Y: " + targetLocation.getY() + ", Z: " + targetLocation.getZ());
    }
    
    @Override
    public void updateProgress(Player player, QuestUpdateEvent event) {
        if (event.getType() == QuestUpdateEvent.EventType.LOCATION_VISITED) {
            checkCompletion(player, player.level());
        }
    }
    
    @Override
    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("QuestType", getQuestType());
        tag.putString("QuestId", questId);
        tag.putString("Title", title);
        tag.putString("Description", description);
        tag.putInt("TargetX", targetLocation.getX());
        tag.putInt("TargetY", targetLocation.getY());
        tag.putInt("TargetZ", targetLocation.getZ());
        tag.putInt("DetectionRadius", detectionRadius);
        tag.putBoolean("Visited", visited);
        tag.putBoolean("PlaceMarker", placeMarker);
        tag.putBoolean("Completed", completed);
        tag.putInt("ExperienceReward", experienceReward);
        if (assignedPlayer != null) {
            tag.putUUID("AssignedPlayer", assignedPlayer);
        }
        return tag;
    }
    
    @Override
    public void loadFromNBT(CompoundTag tag) {
        this.questId = tag.getString("QuestId");
        this.title = tag.getString("Title");
        this.description = tag.getString("Description");
        this.targetLocation = new BlockPos(tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ"));
        this.detectionRadius = tag.getInt("DetectionRadius");
        this.visited = tag.getBoolean("Visited");
        this.placeMarker = tag.getBoolean("PlaceMarker");
        this.completed = tag.getBoolean("Completed");
        this.experienceReward = tag.getInt("ExperienceReward");
        if (tag.hasUUID("AssignedPlayer")) {
            this.assignedPlayer = tag.getUUID("AssignedPlayer");
        }
    }
    
    @Override
    public String getQuestType() {
        return "visit_location";
    }
    
    public BlockPos getTargetLocation() {
        return targetLocation;
    }
    
    public int getDetectionRadius() {
        return detectionRadius;
    }
    
    public boolean isVisited() {
        return visited;
    }
}