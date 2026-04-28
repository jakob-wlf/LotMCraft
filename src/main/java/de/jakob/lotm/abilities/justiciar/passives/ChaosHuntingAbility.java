package de.jakob.lotm.abilities.justiciar.passives;

import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.red_priest.CullAbility;
import de.jakob.lotm.abilities.visionary.PsychologicalInvisibilityAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChaosHuntingAbility extends PassiveAbilityItem {

    private static final Map<UUID, Set<Entity>> TRACKED = new ConcurrentHashMap<>();

    private static final double SCAN_RADIUS = 60.0;

    public ChaosHuntingAbility(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 3));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(entity instanceof ServerPlayer player)) return;

        int casterSeq = BeyonderData.getSequence(entity);
        ServerLevel serverLevel = (ServerLevel) level;

        List<LivingEntity> nearby = AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), (int) SCAN_RADIUS*BeyonderData.getMultiplier(entity));

        Set<Entity> newTargets = new HashSet<>();
        for (LivingEntity candidate : nearby) {
            if (!BeyonderData.isBeyonder(candidate)) continue;
            int candidateSeq = BeyonderData.getSequence(candidate);
            String pathway = BeyonderData.getPathway(candidate);
            boolean higherRank = candidateSeq < casterSeq;
            boolean disasterPathway = "demoness".equals(pathway);
            if (higherRank || disasterPathway) {
                newTargets.add(candidate);
                CullAbility.setGlowingForPlayer(candidate, player, true);
            }
        }

        // Remove glow from entities no longer matching
        Set<Entity> previous = TRACKED.getOrDefault(player.getUUID(), Collections.emptySet());
        for (Entity old : previous) {
            if (!newTargets.contains(old)) {
                CullAbility.setGlowingForPlayer(old, player, false);
            }
        }

        TRACKED.put(player.getUUID(), newTargets);
    }

    @Override
    public void onPassiveAbilityRemoved(LivingEntity entity, ServerLevel serverLevel) {
        if (!(entity instanceof ServerPlayer player)) return;
        Set<Entity> tracked = TRACKED.remove(player.getUUID());
        if (tracked == null) return;
        tracked.forEach(e -> CullAbility.setGlowingForPlayer(e, player, false));
    }
}
