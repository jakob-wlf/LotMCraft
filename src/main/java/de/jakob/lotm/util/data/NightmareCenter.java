package de.jakob.lotm.util.data;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public record NightmareCenter(ServerLevel level, Vec3 pos, double radiusSquared) {
}
