package de.jakob.lotm.util.data;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;


public record NightmareCenter(Level level, Vec3 pos, double radiusSquared) {
}
