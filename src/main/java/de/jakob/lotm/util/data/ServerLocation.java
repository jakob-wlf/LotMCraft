package de.jakob.lotm.util.data;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class ServerLocation {
    private Vec3 position;
    private ServerLevel level;

    public ServerLocation(Vec3 position, ServerLevel level) {
        this.position = position;
        this.level = level;
    }


    public Vec3 getPosition() {
        return position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public void setLevel(ServerLevel level) {
        this.level = level;
    }
}
