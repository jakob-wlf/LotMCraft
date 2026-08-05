package de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives;

import de.jakob.lotm.attachments.LuckAccumulationComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.ProphecyAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncLuckResourcePacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.LuckManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class PassiveLuckAccumulationAbility extends PassiveAbilityItem {

    private static final int minimumRateDuration = 20 * 60 * 5;
    private static final int maximumRateDuration = 20 * 60 * 30;
    private static final int combatDurationTicks = 20 * 15;
    private static final float outOfCombatMultiplier = 1.5f;
    private static final float inCombatMultiplier = 0.35f;
    private static final String lastCombatTickKey = "lotm_luck_last_combat_tick";


    public PassiveLuckAccumulationAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 7));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LuckAccumulationComponent component = entity.getData(ModAttachments.LUCK_ACCUMULATION_COMPONENT.get());
        long gameTime = serverLevel.getGameTime();
        LuckManager.SequenceLuckStats stats = LuckManager.getSequenceStats(entity);
        if (stats.capacity() <= 0) {
            return;
        }
        float minimumRate = Math.min(stats.minimumRegenerationRate(), stats.maximumRegenerationRate());
        float maximumRate = Math.max(stats.minimumRegenerationRate(), stats.maximumRegenerationRate());
        boolean rateOutsideSequenceRange = component.getRegenerationRate() < minimumRate
                || component.getRegenerationRate() > maximumRate;
        if (rateOutsideSequenceRange || component.getNextRegenerationChangeTick() <= gameTime) {
            component.setRegenerationRate(minimumRate + random.nextFloat() * (maximumRate - minimumRate));
            component.setNextRegenerationChangeTick(gameTime + random.nextInt(minimumRateDuration, maximumRateDuration + 1));
        }

        int maximum = stats.capacity();
        if (LuckManager.getLuck(entity) >= maximum || component.getRegenerationRate() <= 0) {
            syncResource(entity, component, maximum, gameTime);
            return;
        }

        float progress = component.getRegenerationProgress()
            + getEffectiveRegenerationRate(entity, component) * 5f / (20 * 60);
        int generatedLuck = (int) progress;
        if (generatedLuck > 0) {
            LuckManager.addLuck(entity, generatedLuck);
            progress -= generatedLuck;
        }
        component.setRegenerationProgress(progress);
        syncResource(entity, component, maximum, gameTime);
    }

    public static void syncResource(LivingEntity entity, LuckAccumulationComponent component, int maximum, long gameTime) {
        if (entity instanceof ServerPlayer player && gameTime % 20 == 0) {
            PacketHandler.sendToPlayer(player, new SyncLuckResourcePacket(
                    0,
                    LuckManager.getLuck(entity), maximum, getEffectiveRegenerationRate(entity, component)
                        + LuckManager.getLuckGainRatePerMinute(entity),
                    LuckManager.getLuckDrainRatePerMinute(entity), true));
        }
    }

    public static void markInCombat(LivingEntity entity) {
        entity.getPersistentData().putLong(lastCombatTickKey, entity.level().getGameTime());
    }

    public static boolean isInCombat(LivingEntity entity) {
        long lastCombatTick = entity.getPersistentData().getLong(lastCombatTickKey);
        return lastCombatTick > 0 && entity.level().getGameTime() - lastCombatTick <= combatDurationTicks;
    }

    public static float getEffectiveRegenerationRate(LivingEntity entity, LuckAccumulationComponent component) {
        if (!LuckManager.regeneratesWheelLuck(entity)) {
            return 0;
        }
        float combatMultiplier = isInCombat(entity) ? inCombatMultiplier : outOfCombatMultiplier;
        return component.getRegenerationRate()
            * combatMultiplier
            * (float) BeyonderData.getMultiplier(entity)
            * ProphecyAbility.getLuckRegenerationMultiplier(entity);
    }

    public static boolean consumeStoredLuck(LivingEntity entity, int amount) {
        int scaledAmount = LuckManager.getSequenceScaledCost(entity, amount);
        if (ProphecyAbility.interceptPassiveLuckEvent(entity, scaledAmount)) {
            return false;
        }
        return consumeExactStoredLuck(entity, scaledAmount);
    }

    public static boolean consumeExactStoredLuck(LivingEntity entity, int amount) {
        if (!LuckManager.consumeLuck(entity, amount)) {
            return false;
        }

        if (entity instanceof ServerPlayer player) {
            LuckAccumulationComponent component = entity.getData(ModAttachments.LUCK_ACCUMULATION_COMPONENT.get());
            PacketHandler.sendToPlayer(player, new SyncLuckResourcePacket(
                    0,
                    LuckManager.getLuck(entity),
                    LuckManager.getMaximumLuck(entity),
                    getEffectiveRegenerationRate(entity, component)
                        + LuckManager.getLuckGainRatePerMinute(entity),
                    LuckManager.getLuckDrainRatePerMinute(entity),
                    LuckManager.usesWheelLuckResource(entity)));
        }
        return true;
    }

}
