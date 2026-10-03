package de.jakob.lotm.util.data;

import net.minecraft.world.phys.Vec3;

public class LocationSupplier{

    private Vec3 pos;

    public LocationSupplier(Vec3 pos) {
        this.pos = pos;
    }

    public Vec3 getPos() {
        return pos;
    }

    public void setPos(Vec3 pos) {
        this.pos = pos;
    }
}
