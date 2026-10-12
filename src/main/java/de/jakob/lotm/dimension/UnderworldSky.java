package de.jakob.lotm.dimension;

import net.minecraft.world.phys.Vec3;

public final class UnderworldSky {

    private static final Vec3 TOP     = new Vec3(0.015, 0.015, 0.02);
    private static final Vec3 HORIZON = new Vec3(0.13, 0.125, 0.10);

    private static final Vec3 PALE_YELLOW = new Vec3(0.34, 0.32, 0.24);
    private static final Vec3 WHITE       = new Vec3(0.30, 0.30, 0.30);
    private static final Vec3 SOFT_WHITE  = new Vec3(0.38, 0.37, 0.33);

    private UnderworldSky() {}

    public static Vec3 top()     { return TOP; }
    public static Vec3 horizon() { return HORIZON; }

    public static Vec3 paleYellow() { return PALE_YELLOW; }
    public static Vec3 white()      { return WHITE; }
    public static Vec3 softWhite()  { return SOFT_WHITE; }
}