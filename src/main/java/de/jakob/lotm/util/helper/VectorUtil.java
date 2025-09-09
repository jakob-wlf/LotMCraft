package de.jakob.lotm.util.helper;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VectorUtil {
    private static final double EPSILON = Math.ulp(1.0d) * 2.0d;
    private static final Random RAND = new Random();

    private static boolean isSignificant(double value) {
        return Math.abs(value) > EPSILON;
    }

    public static List<Vec3> createBezierCurve(Vec3 start, Vec3 end, float step, int controlPointNumber) {
        Vec3 perp = getRandomPerpendicular(end.subtract(start));
        return createBezierCurve(start, end, perp, step, controlPointNumber);
    }

    public static List<Vec3> createBezierCurve(Vec3 start, Vec3 end, float step, float pointOffset, int controlPointNumber) {
        Vec3 perp = getRandomPerpendicular(end.subtract(start));
        return createBezierCurve(start, end, perp, step, pointOffset, controlPointNumber);
    }

    public static Vec3 getRandomPerpendicular(Vec3 v) {
        Vec3 dir = v.normalize();

        while (true) {
            Vec3 randomVec = new Vec3(
                    RAND.nextDouble() * 2 - 1, // [-1, 1]
                    RAND.nextDouble() * 2 - 1,
                    RAND.nextDouble() * 2 - 1
            );

            // Cross product to ensure perpendicular
            Vec3 perp = dir.cross(randomVec);

            if (perp.lengthSqr() > 1e-8) {
                return perp.normalize();
            }
        }
    }

    public static List<Vec3> createBezierCurve(Vec3 start, Vec3 end, Vec3 pointDirection, float step, int controlPointNumber) {
        return createBezierCurve(start, end, pointDirection, step, 8, controlPointNumber);
    }


    public static List<Vec3> createBezierCurve(Vec3 start, Vec3 end, Vec3 pointDirection, float step, float pointOffset, int controlPointNumber) {
        if(controlPointNumber <= 0)
            return List.of();

        float distance = (float) start.distanceTo(end);
        float distributionStep = (distance / (controlPointNumber + 1));
        Vec3 dir = end.subtract(start);

        Vec3[] points = new Vec3[controlPointNumber + 2];
        points[0] = start;
        points[controlPointNumber + 1] = end;
        for(int i = 1; i < controlPointNumber + 1; i++) {
            boolean right = i % 2 == 0;
            points[i] = start.add(dir.normalize().scale(distributionStep * i)).add(pointDirection.normalize().scale(right ? pointOffset : -pointOffset));
        }

        List<Vec3> pointsOnCurve = new ArrayList<>();

        for(float i = 0; i < (1 + step); i+=step) {
            pointsOnCurve.add(calculatePoint(i, points));
        }

        return pointsOnCurve;
    }

    private static Vec3 calculatePoint(float t, Vec3... points) {
        if (points.length == 1) {
            return points[0];
        }

        Vec3[] next = new Vec3[points.length - 1];
        for (int i = 0; i < points.length - 1; i++) {
            // Linear interpolation: (1 - t) * P[i] + t * P[i+1]
            next[i] = points[i].scale(1 - t).add(points[i + 1].scale(t));
        }

        return calculatePoint(t, next);
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
            Vec3 rightDir = getRightDir(direction);

            result = result.add(rightDir.scale(right));

            if (hasUp) {
                // up = right × forward
                Vec3 upDir = rightDir.cross(direction).normalize();
                result = result.add(upDir.scale(up));
            }
        }

        return result;
    }

    private static @NotNull Vec3 getRightDir(Vec3 direction) {
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
        return rightDir;
    }

    public static Vec3 getPerpendicularVector(Vec3 lookAngle) {
        double x = -lookAngle.z;
        double z = lookAngle.x;
        return new Vec3(x, 0, z).normalize();
    }
}
