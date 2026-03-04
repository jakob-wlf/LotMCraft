package de.jakob.lotm.abilities.core;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tracks recent ability uses globally so that Recording and Replicating abilities
 * can detect nearby ability casts. Entries expire after a short time.
 */
public class AbilityUseTracker {

    private static final long MAX_AGE_MS = 2000;
    private static final List<AbilityUseRecord> recentUses = new ArrayList<>();

    public record AbilityUseRecord(LivingEntity entity, Ability ability, Vec3 position, Level level, long timestamp) {}

    /**
     * Track that an ability was used at a specific location.
     */
    public static void trackUse(LivingEntity entity, Ability ability, Vec3 position, Level level) {
        cleanOldEntries();
        recentUses.add(new AbilityUseRecord(entity, ability, position, level, System.currentTimeMillis()));
    }

    /**
     * Find the most recent ability use within a radius of the given position.
     * Excludes abilities used by the specified entity (the recorder/replicator).
     */
    public static AbilityUseRecord getRecentUseInArea(Vec3 center, Level level, double radius, LivingEntity excludeEntity) {
        cleanOldEntries();

        double radiusSq = radius * radius;

        for (int i = recentUses.size() - 1; i >= 0; i--) {
            AbilityUseRecord record = recentUses.get(i);
            if (record.level() != level) continue;
            if (record.entity() == excludeEntity) continue;

            double distSq = record.position().distanceToSqr(center);
            if (distSq <= radiusSq) {
                return record;
            }
        }

        return null;
    }

    private static void cleanOldEntries() {
        long now = System.currentTimeMillis();
        Iterator<AbilityUseRecord> iterator = recentUses.iterator();
        while (iterator.hasNext()) {
            if (now - iterator.next().timestamp() > MAX_AGE_MS) {
                iterator.remove();
            }
        }
    }
}
