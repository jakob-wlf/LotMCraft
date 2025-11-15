package de.jakob.lotm.attachments;

import de.jakob.lotm.entity.quests.Quest;
import de.jakob.lotm.entity.quests.QuestRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Stores quest data for a player
 * Attach this to the player using an attachment or capability
 */
public class WaypointComponent {
    private List<Waypoint> waypoints;
    public WaypointComponent() {
        this.waypoints = new ArrayList<>();
    }

    private int selectedWaypoint = 0;

    public Waypoint getSelectedWaypoint() {
        return waypoints.size() <= selectedWaypoint ? null : waypoints.get(selectedWaypoint);
    }

    public void selectNextWaypoint() {
        if(waypoints.isEmpty() || waypoints.size() < 2) {
            return;
        }

        selectedWaypoint++;
        if(selectedWaypoint >= waypoints.size()) {
            selectedWaypoint = 0;
        }
    }

    public void createWaypoint(double x, double y, double z, ServerLevel serverLevel) {
        waypoints.add(new Waypoint(x, y, z, serverLevel));
    }

    public void deleteWaypoint(Waypoint waypoint) {
        waypoints.remove(waypoint);
        selectNextWaypoint();
    }

    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();

        ListTag waypointList = new ListTag();
        for (Waypoint waypoint : waypoints) {
            waypointList.add(waypoint.saveToNBT());
        }
        tag.put("Waypoints", waypointList);

        return tag;
    }

    public void loadFromNBT(CompoundTag tag, IAttachmentHolder holder) {
        waypoints.clear();
        MinecraftServer server = getServerFromAttachmentHolder(holder);

        if(server == null) {
            return;
        }

        if (tag.contains("Waypoints")) {
            ListTag waypointList = tag.getList("Waypoints", Tag.TAG_COMPOUND);
            for (Tag waypointTag : waypointList) {
                Waypoint waypoint = Waypoint.loadFromNBT((CompoundTag) waypointTag, server);
                if (waypoint != null) {
                    waypoints.add(waypoint);
                }
            }
        }
    }

    private MinecraftServer getServerFromAttachmentHolder(IAttachmentHolder holder) {
        if (holder instanceof Entity entity) {
            return entity.getServer();
        }

        return null;
    }

    public record Waypoint(double x, double y, double z, ServerLevel serverLevel) {
        public CompoundTag saveToNBT() {
            CompoundTag compoundTag = new CompoundTag();

            compoundTag.putDouble("xCoordinate", x());
            compoundTag.putDouble("yCoordinate", y());
            compoundTag.putDouble("zCoordinate", z());

            ResourceKey<Level> levelKey = serverLevel.dimension();
            compoundTag.putString("levelKey", levelKey.location().toString());

            return compoundTag;
        }

        public static Waypoint loadFromNBT(@NonNull CompoundTag compoundTag, @NonNull MinecraftServer server) {
            double x = compoundTag.getDouble("xCoordinate");
            double y = compoundTag.getDouble("yCoordinate");
            double z = compoundTag.getDouble("zCoordinate");

            String levelString = compoundTag.getString("levelKey");
            ResourceLocation levelLocation = ResourceLocation.parse(levelString);
            ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, levelLocation);

            ServerLevel level = server.getLevel(levelKey);
            if(level == null) {
                return null;
            }

            return new Waypoint(x, y, z, level);
        }
    }
}