package de.jakob.lotm.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;


public class SpiritWorldHandler {

    public static Vec3 getCoordinatesInSpiritWorld(Vec3 origin, Level level) {
        double x = origin.x * Math.sin(1 / origin.x);
        double y = origin.y;
        double z = origin.z * Math.cos(1 / origin.z);

        if(!level.isInWorldBounds(BlockPos.containing(x, y, z))) {
            if(x < level.getWorldBorder().getMinX()) x = level.getWorldBorder().getMinX() + 1;
            if(x > level.getWorldBorder().getMaxX()) x = level.getWorldBorder().getMaxX() - 1;
            if(z < level.getWorldBorder().getMinZ()) z = level.getWorldBorder().getMinZ() + 1;
            if(z > level.getWorldBorder().getMaxZ()) z = level.getWorldBorder().getMaxZ() - 1;
        }

        return new Vec3(x, y, z);
    }

    public static Vec3 getCoordinatesInOverworld(Vec3 origin, Level level) {
        double x = origin.x / Math.sin(1 / origin.x);
        double y = origin.y;
        double z = origin.z / Math.cos(1 / origin.z);

        if (!level.isInWorldBounds(BlockPos.containing(x, y, z))) {
            if(x < level.getWorldBorder().getMinX()) x = level.getWorldBorder().getMinX() + 1;
            if(x > level.getWorldBorder().getMaxX()) x = level.getWorldBorder().getMaxX() - 1;
            if(z < level.getWorldBorder().getMinZ()) z = level.getWorldBorder().getMinZ() + 1;
            if(z > level.getWorldBorder().getMaxZ()) z = level.getWorldBorder().getMaxZ() - 1;
        }

        return new Vec3(x, y, z);
    }

}
