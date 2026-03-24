package de.jakob.lotm.util.data;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Location {
    private Vec3 position;
    private Level level;

    public Location(Vec3 position, Level level) {
        this.position = position;
        this.level = level;
    }

    public Vec3 getPosition() {
        return position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public boolean isInSameLevel(Level levelToCompare) {
        return level == levelToCompare;
    }

    /**
     * Gets the distance to another location. If the other location is in a different level, returns Double.MAX_VALUE.
     */
    public double getDistanceTo(Location other) {
        if(!isInSameLevel(other.getLevel()))
            return Double.MAX_VALUE;

        return position.distanceTo(other.getPosition());
    }

    /**
     * Gets the distance to another location. Only considers the position, does not check if the other location is in the same level.
     */
    public double getDistanceTo(Vec3 position) {
        return this.position.distanceTo(position);
    }
}
