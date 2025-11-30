package de.jakob.lotm.dimension;

import net.minecraft.world.phys.Vec3;

public class SpiritWorldHandler {

    public static Vec3 getCoordinatesInSpiritWorld(Vec3 origin) {
        double x = origin.x * Math.sin(1 / origin.x);
        double y = origin.y;
        double z = origin.z * Math.cos(1 / origin.z);

        return new Vec3(x, y, z);
    }

    public static Vec3 getCoordinatesInOverworld(Vec3 origin) {
        double x = origin.x / Math.sin(1 / origin.x);
        double y = origin.y;
        double z = origin.z / Math.cos(1 / origin.z);

        return new Vec3(x, y, z);
    }

}
