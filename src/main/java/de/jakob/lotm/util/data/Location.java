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
}
