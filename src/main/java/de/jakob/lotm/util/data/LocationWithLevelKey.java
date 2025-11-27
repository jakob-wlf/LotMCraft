package de.jakob.lotm.util.data;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LocationWithLevelKey {
    private Vec3 position;
    private String levelKey;

    public LocationWithLevelKey(Vec3 position, String levelKey) {
        this.position = position;
        this.levelKey = levelKey;
    }


    public Vec3 getPosition() {
        return position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }

    public String getLevelKey() {
        return levelKey;
    }

    public void setLevelKey(String levelKey) {
        this.levelKey = levelKey;
    }
}
