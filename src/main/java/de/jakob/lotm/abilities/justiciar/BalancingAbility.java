package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class BalancingAbility extends Ability {

    public static final List<BalancingZone> ACTIVE_ZONES = new CopyOnWriteArrayList<>();

    private static final double ZONE_RADIUS = 120.0;

    public BalancingAbility(String id) {
        super(id, 30f);
        interactionRadius = 1;
        hasOptimalDistance = false;
        postsUsedAbilityEventManually = true;
        canBeCopied = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1600;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;
        int ZONE_DURATION = 3600 * (int) Math.max(multiplier(entity) / 4, 1);
        long expiryTick = serverLevel.getGameTime() + ZONE_DURATION;
        BalancingZone zone = new BalancingZone(entity.getUUID(), entity.position(), serverLevel, expiryTick);
        ACTIVE_ZONES.add(zone);

        final Vec3 center = zone.center;

        List<LivingEntity> initialEntities = AbilityUtil.getNearbyEntities(null, serverLevel, center, (int) ZONE_RADIUS);
        double totalMultiplier = 0;
        int beyonderCount = 0;

        for (LivingEntity e : initialEntities) {
            if (BeyonderData.isBeyonder(e)) {
                totalMultiplier += BeyonderData.getMultiplier(e);
                beyonderCount++;
            }
        }

        final double TARGET_MULTIPLIER = beyonderCount > 0 ? (totalMultiplier / beyonderCount) : BeyonderData.getMultiplier(entity);

        final String MODIFIER_ID = "balancing_zone_" + zone.ownerId;

        ServerScheduler.scheduleForDuration(0, 5, ZONE_DURATION, () -> {
            if (!zone.isActive()) return;
            AbilityUtil.getNearbyEntities(null, serverLevel, center, (int) ZONE_RADIUS).forEach(e -> {
                if (!BeyonderData.isBeyonder(e)) return;
                double base = BeyonderData.getMultiplierForSequence(BeyonderData.getSequence(e));
                double needed = TARGET_MULTIPLIER / base;
                BeyonderData.removeModifier(e, MODIFIER_ID);
                BeyonderData.addModifier(e, MODIFIER_ID, needed);
            });
        }, () -> {
            // On expiry, remove the modifier from anyone still in range
            AbilityUtil.getNearbyEntities(null, serverLevel, center, (int) ZONE_RADIUS)
                    .forEach(e -> BeyonderData.removeModifier(e, MODIFIER_ID));
            ACTIVE_ZONES.remove(zone);
        }, serverLevel);

        // Single ring that expands to the 60-block boundary and stays visible for the full zone duration
        RingEffectManager.createRingForAll(center, (float) ZONE_RADIUS, ZONE_DURATION,
                0.95f, 0.95f, 1.0f, 0.6f, 2f, 5f, 100f, false, false, serverLevel);

        Component message = Component.translatable("ability.lotmcraft.balancing.declared")
                .withStyle(ChatFormatting.GOLD);
        serverLevel.getServer().getPlayerList().getPlayers().forEach(p -> {
            if (p.level().equals(serverLevel) && p.distanceTo(entity) <= ZONE_RADIUS) {
                p.sendSystemMessage(message);
            }
        });

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, ZONE_RADIUS, 20 * 2));
    }

    public static class BalancingZone {
        public final UUID ownerId;
        public final Vec3 center;
        public final ServerLevel level;
        public final long expiryTick;

        public BalancingZone(UUID ownerId, Vec3 center, ServerLevel level, long expiryTick) {
            this.ownerId = ownerId;
            this.center = center;
            this.level = level;
            this.expiryTick = expiryTick;
        }

        public boolean isActive() {
            return level.getGameTime() < expiryTick;
        }

        public boolean isInZone(Vec3 pos, ServerLevel lvl) {
            return lvl.equals(level) && pos.distanceTo(center) <= ZONE_RADIUS;
        }
    }
}