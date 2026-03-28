package de.jakob.lotm.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class TeleportationUtil {
    public static Vec3 clampToBorder(ServerLevel level, Vec3 pos){
        var border = level.getWorldBorder();

        double minX = border.getMinX();
        double maxX = border.getMaxX();
        double minZ = border.getMinZ();
        double maxZ = border.getMaxZ();

        double clampedX = Mth.clamp(pos.x, minX + 1, maxX - 1);
        double clampedZ = Mth.clamp(pos.z, minZ + 1, maxZ - 1);

        return new Vec3(clampedX, pos.y, clampedZ);
    }
}
