package de.jakob.lotm.util;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.ProphecyAbility;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LuckManager {
    public static final int minimumLuck = -10000;
    public static final int defaultMaximumLuck = 100;
    public static final String prophecySource = "prophecy";
    public static final String borrowedTomorrowSource = "borrowed_tomorrow";
    public static final String debtOfYesterdaySource = "debt_of_yesterday";
    private static final double luckEffectivenessPerSequence = 0.65;
    private static final double minimumLuckEffectiveness = 0.15;
    private static final Map<UUID, Map<String, TimedLuckDrain>> activeLuckDrains = new HashMap<>();
    private static final Map<UUID, Map<String, TimedLuckGain>> activeLuckGains = new HashMap<>();

    private LuckManager() {
    }

    public static SequenceLuckStats getSequenceStats(LivingEntity entity) {
        return getSequenceStats(BeyonderData.getSequence(entity, "wheel_of_fortune"));
    }

    public static SequenceLuckStats getSequenceStats(int sequence) {
        return switch (sequence) {
            case 9 -> new SequenceLuckStats(250, 0, 0);
            case 8 -> new SequenceLuckStats(350, 0, 0);
            case 7 -> new SequenceLuckStats(500, 1f, 3f);
            case 6 -> new SequenceLuckStats(1000, 5f, 10);
            case 5 -> new SequenceLuckStats(2000, 10, 25);
            case 4 -> new SequenceLuckStats(4000, 25, 50);
            case 3 -> new SequenceLuckStats(8000, 50, 100);
            case 2 -> new SequenceLuckStats(12000, 100, 250);
            case 1 -> new SequenceLuckStats(15000, 250, 500);
            case 0 -> new SequenceLuckStats(25000, 500, 1000);
            default -> SequenceLuckStats.none;
        };
    }

    public static int getNonWheelMaximumLuck(int sequence) {
        return switch (sequence) {
            case 9 -> 100;
            case 8 -> 125;
            case 7 -> 150;
            case 6 -> 200;
            case 5 -> 250;
            case 4 -> 400;
            case 3 -> 600;
            case 2 -> 900;
            case 1 -> 1200;
            case 0 -> 2000;
            default -> defaultMaximumLuck;
        };
    }

    public static int getMaximumLuck(LivingEntity entity) {
        int wheelCapacity = getSequenceStats(entity).capacity();
        if (wheelCapacity > 0) {
            return wheelCapacity;
        }
        return BeyonderData.isBeyonder(entity)
                ? getNonWheelMaximumLuck(BeyonderData.getSequence(entity))
                : defaultMaximumLuck;
    }

    public static boolean usesWheelLuckResource(LivingEntity entity) {
        return getSequenceStats(entity).capacity() > 0;
    }

    public static boolean regeneratesWheelLuck(LivingEntity entity) {
        return getSequenceStats(entity).maximumRegenerationRate() > 0;
    }

    public static int getSequenceScaledCost(LivingEntity entity, int baseCost) {
        return Math.max(1, (int) Math.ceil(baseCost * getSequenceCostMultiplier(entity)));
    }

    public static float getSequenceScaledEffectRate(LivingEntity entity, float baseRate) {
        return baseRate / getSequenceCostMultiplier(entity);
    }

    private static float getSequenceCostMultiplier(LivingEntity entity) {
        if (entity == null) {
            return 1.0f;
        }
        return switch (BeyonderData.getSequence(entity)) {
            case 7 -> 1.5f;
            case 6 -> 1.35f;
            case 5 -> 1.2f;
            case 4 -> 1.0f;
            case 3 -> 0.85f;
            case 2 -> 0.7f;
            case 1 -> 0.55f;
            case 0 -> 0.4f;
            default -> 1.5f;
        };
    }

    public static String sourceForCaster(String source, LivingEntity caster) {
        return source + ":" + caster.getUUID();
    }

    public static int setLuck(LivingEntity entity, int value) {
        migrateLegacyLuck(entity);
        entity.getData(ModAttachments.LUCK_ACCUMULATION_COMPONENT.get())
                .setStoredLuck(value, getMaximumLuck(entity));
        return getLuck(entity);
    }

    public static int addLuck(LivingEntity entity, int amount) {
        return addLuck(entity, amount, minimumLuck, getMaximumLuck(entity));
    }

    public static int addLuck(LivingEntity source, LivingEntity target, int amount) {
        int resistedAmount = amount < 0 ? scaleHarmfulLuckChange(source, target, amount) : amount;
        return addLuck(target, resistedAmount);
    }

    public static int addLuck(LivingEntity entity, int amount, int minimum, int maximum) {
        int currentLuck = getLuck(entity);
        amount = ProphecyAbility.modifyPositiveLuckGain(entity, amount);
        int lowerBound = Math.max(minimumLuck, minimum);
        int upperBound = Math.min(getMaximumLuck(entity), maximum);
        return setLuck(entity, Math.clamp(currentLuck + amount, lowerBound, upperBound));
    }

    public static void applyLuckDrain(LivingEntity entity, String sourceId, float ratePerMinute, long durationTicks) {
        applyLuckDrain(entity, sourceId, ratePerMinute, durationTicks, minimumLuck);
    }

    public static void applyLuckDrain(LivingEntity source, LivingEntity target, String sourceId,
                                      float ratePerMinute, long durationTicks) {
        applyLuckDrain(source, target, sourceId, ratePerMinute, durationTicks, minimumLuck);
    }

    public static void applyLuckDrain(LivingEntity source, LivingEntity target, String sourceId,
                                      float ratePerMinute, long durationTicks, int drainMinimumLuck) {
        float resistedRate = ratePerMinute * getHarmfulLuckEffectiveness(source, target);
        applyLuckDrain(target, sourceId, resistedRate, durationTicks, drainMinimumLuck);
    }

    public static void applyLuckDrain(LivingEntity entity, String sourceId, float ratePerMinute,
                                      long durationTicks, int drainMinimumLuck) {
        if (entity.level().isClientSide() || ratePerMinute <= 0 || durationTicks <= 0) {
            return;
        }

        long expiresAt = entity.level().getGameTime() + durationTicks;
        Map<String, TimedLuckDrain> drains = activeLuckDrains.computeIfAbsent(
                entity.getUUID(), ignored -> new HashMap<>());
        TimedLuckDrain existing = drains.get(sourceId);
        double progress = existing == null ? 0 : existing.progress;
        drains.put(sourceId, new TimedLuckDrain(ratePerMinute, expiresAt, progress,
            Math.max(minimumLuck, drainMinimumLuck)));
    }

    public static float getHarmfulLuckEffectiveness(LivingEntity source, LivingEntity target) {
        if (source == null || source == target || !BeyonderData.isBeyonder(source)
                || !BeyonderData.isBeyonder(target)) {
            return 1.0f;
        }

        int sequenceGap = BeyonderData.getSequence(source) - BeyonderData.getSequence(target);
        if (sequenceGap <= 0) return 1.0f;
        return (float) Math.max(minimumLuckEffectiveness,
            Math.pow(luckEffectivenessPerSequence, sequenceGap));
    }

    private static int scaleHarmfulLuckChange(LivingEntity source, LivingEntity target, int amount) {
        float effectiveness = getHarmfulLuckEffectiveness(source, target);
        return Math.min(-1, Math.round(amount * effectiveness));
    }

    public static void clearLuckDrain(LivingEntity entity, String sourceId) {
        Map<String, TimedLuckDrain> drains = activeLuckDrains.get(entity.getUUID());
        if (drains == null) {
            return;
        }
        drains.remove(sourceId);
        if (drains.isEmpty()) {
            activeLuckDrains.remove(entity.getUUID());
        }
    }

    public static void applyLuckGain(LivingEntity entity, String sourceId, float ratePerMinute, long durationTicks) {
        if (entity.level().isClientSide() || ratePerMinute <= 0 || durationTicks <= 0) {
            return;
        }

        long expiresAt = entity.level().getGameTime() + durationTicks;
        Map<String, TimedLuckGain> gains = activeLuckGains.computeIfAbsent(
                entity.getUUID(), ignored -> new HashMap<>());
        TimedLuckGain existing = gains.get(sourceId);
        double progress = existing == null ? 0 : existing.progress;
        gains.put(sourceId, new TimedLuckGain(ratePerMinute, expiresAt, progress));
    }

    public static void clearLuckGain(LivingEntity entity, String sourceId) {
        Map<String, TimedLuckGain> gains = activeLuckGains.get(entity.getUUID());
        if (gains == null) {
            return;
        }
        gains.remove(sourceId);
        if (gains.isEmpty()) {
            activeLuckGains.remove(entity.getUUID());
        }
    }

    public static void tickLuckDrains(LivingEntity entity) {
        Map<String, TimedLuckDrain> drains = getActiveLuckDrains(entity);
        if (drains != null) {
            for (TimedLuckDrain drain : drains.values()) {
                drain.progress += drain.ratePerMinute / (20.0 * 60.0);
                int wholeLuck = (int) drain.progress;
                if (wholeLuck > 0) {
                    addLuck(entity, -wholeLuck, drain.minimumLuck, getMaximumLuck(entity));
                    drain.progress -= wholeLuck;
                }
            }
        }

        Map<String, TimedLuckGain> gains = getActiveLuckGains(entity);
        if (gains != null) {
            for (TimedLuckGain gain : gains.values()) {
                gain.progress += gain.ratePerMinute / (20.0 * 60.0);
                int wholeLuck = (int) gain.progress;
                if (wholeLuck > 0) {
                    addLuck(entity, wholeLuck);
                    gain.progress -= wholeLuck;
                }
            }
        }
    }

    public static float getLuckDrainRatePerMinute(LivingEntity entity) {
        Map<String, TimedLuckDrain> drains = getActiveLuckDrains(entity);
        if (drains == null) {
            return 0;
        }
        return (float) drains.values().stream().mapToDouble(drain -> drain.ratePerMinute).sum();
    }

    public static float getLuckGainRatePerMinute(LivingEntity entity) {
        Map<String, TimedLuckGain> gains = getActiveLuckGains(entity);
        if (gains == null) {
            return 0;
        }
        return (float) gains.values().stream().mapToDouble(gain -> gain.ratePerMinute).sum();
    }

    private static Map<String, TimedLuckDrain> getActiveLuckDrains(LivingEntity entity) {
        Map<String, TimedLuckDrain> drains = activeLuckDrains.get(entity.getUUID());
        if (drains == null) {
            return null;
        }

        long gameTime = entity.level().getGameTime();
        drains.values().removeIf(drain -> drain.expiresAt <= gameTime);
        if (drains.isEmpty()) {
            activeLuckDrains.remove(entity.getUUID());
            return null;
        }
        return drains;
    }

    private static Map<String, TimedLuckGain> getActiveLuckGains(LivingEntity entity) {
        Map<String, TimedLuckGain> gains = activeLuckGains.get(entity.getUUID());
        if (gains == null) {
            return null;
        }

        long gameTime = entity.level().getGameTime();
        gains.values().removeIf(gain -> gain.expiresAt <= gameTime);
        if (gains.isEmpty()) {
            activeLuckGains.remove(entity.getUUID());
            return null;
        }
        return gains;
    }

    public static int cleanseMisfortune(LivingEntity entity, int amount) {
        if (amount <= 0 || getLuck(entity) >= 0) {
            return getLuck(entity);
        }
        return addLuck(entity, amount, minimumLuck, 0);
    }

    public static boolean consumeLuck(LivingEntity entity, int amount) {
        int cost = getLuckCost(entity, amount);
        int currentLuck = getLuck(entity);
        if (amount < 0 || currentLuck < cost) {
            return false;
        }
        setLuck(entity, currentLuck - cost);
        ProphecyAbility.onLuckSpent(entity, cost);
        return true;
    }

    public static int getLuckCost(LivingEntity entity, int amount) {
        if (amount <= 0) return amount;
        return Math.max(1, Math.round(amount * ProphecyAbility.getCostMultiplier(entity)));
    }

    public static int transferAllLuck(LivingEntity source, LivingEntity target) {
        int transferredLuck = getLuck(source);
        addLuck(target, transferredLuck);
        setLuck(source, 0);
        return transferredLuck;
    }

    public static void resetLuck(LivingEntity entity) {
        setLuck(entity, 0);
    }

    public static int getLuck(LivingEntity entity) {
        migrateLegacyLuck(entity);
        var resource = entity.getData(ModAttachments.LUCK_ACCUMULATION_COMPONENT.get());
        int luck = Math.clamp(resource.getStoredLuck(), minimumLuck, getMaximumLuck(entity));
        if (luck != resource.getStoredLuck()) {
            resource.setStoredLuck(luck, getMaximumLuck(entity));
        }
        return luck;
    }

    public static int getNetLuck(LivingEntity entity) {
        return getLuck(entity);
    }

    public static int getEffectiveLuck(LivingEntity entity) {
        return getLuck(entity) + ProphecyAbility.getEffectiveLuckBonus(entity);
    }

    public static int getPositiveLuckAmplifier(int luck) {
        return luck > 0 ? luck / 500 : 0;
    }

    private static void migrateLegacyLuck(LivingEntity entity) {
        int legacyLuck = entity.getData(ModAttachments.LUCK_COMPONENT.get()).getLuck();
        if (legacyLuck == 0) {
            return;
        }

        var resource = entity.getData(ModAttachments.LUCK_ACCUMULATION_COMPONENT.get());
        resource.setStoredLuck(resource.getStoredLuck() + legacyLuck, getMaximumLuck(entity));
        entity.getData(ModAttachments.LUCK_COMPONENT.get()).setLuck(0);
    }

    public record SequenceLuckStats(
            int capacity,
            float minimumRegenerationRate,
            float maximumRegenerationRate) {
        private static final SequenceLuckStats none = new SequenceLuckStats(0, 0, 0);
    }

    private static final class TimedLuckDrain {
        private final float ratePerMinute;
        private final long expiresAt;
        private final int minimumLuck;
        private double progress;

        private TimedLuckDrain(float ratePerMinute, long expiresAt, double progress, int minimumLuck) {
            this.ratePerMinute = ratePerMinute;
            this.expiresAt = expiresAt;
            this.progress = progress;
            this.minimumLuck = minimumLuck;
        }
    }

    private static final class TimedLuckGain {
        private final float ratePerMinute;
        private final long expiresAt;
        private double progress;

        private TimedLuckGain(float ratePerMinute, long expiresAt, double progress) {
            this.ratePerMinute = ratePerMinute;
            this.expiresAt = expiresAt;
            this.progress = progress;
        }
    }
}
