package de.jakob.lotm.util.helper;

import net.minecraft.world.phys.Vec3;

import java.net.URI;

public class VectorUtil {
    private static final double EPSILON = Math.ulp(1.0d) * 2.0d;

    private static boolean isSignificant(double value) {
        return Math.abs(value) > EPSILON;
    }

    public static Vec3 getRelativePosition(Vec3 position, Vec3 direction, double forward, double right, double up) {
        Vec3 result = position;

        Vec3 forwardDir;
        if (isSignificant(forward)) {
            forwardDir = direction.normalize().scale(forward);
            result = result.add(forwardDir);
        }

        boolean hasUp = isSignificant(up);

        if (isSignificant(right) || hasUp) {
            Vec3 rightDir;

            // Check if direction is not pointing straight up/down
            if (isSignificant(Math.abs(direction.y) - 1)) {
                // Create a right vector perpendicular to forward and horizontal plane
                double factor = Math.sqrt(1 - direction.y * direction.y);
                double nx = -direction.z / factor;
                double nz = direction.x / factor;
                rightDir = new Vec3(nx, 0, nz);
            } else {
                // Fallback: if direction is vertical, use global right (e.g., +X)
                rightDir = new Vec3(1, 0, 0);
            }

            result = result.add(rightDir.scale(right));

            if (hasUp) {
                // up = right × forward
                Vec3 upDir = rightDir.cross(direction).normalize();
                result = result.add(upDir.scale(up));
            }
        }

        return result;
    }

    public static Vec3 getPerpendicularVector(Vec3 lookAngle) {
        double x = -lookAngle.z;
        double z = lookAngle.x;
        return new Vec3(x, 0, z).normalize();
    }
}
