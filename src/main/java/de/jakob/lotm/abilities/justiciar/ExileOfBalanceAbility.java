package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExileOfBalanceAbility extends Ability {

    public static final Map<UUID, Long> EXILED_ENTITIES = new ConcurrentHashMap<>();

    private static final double ZONE_RADIUS = 80.0;
    // 2 minutes

    public ExileOfBalanceAbility(String id) {
        super(id, 60f);
        interactionRadius = 40;
        hasOptimalDistance = false;
        postsUsedAbilityEventManually = true;
        canBeShared = false;
        canBeCopied = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1200;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        List<LivingEntity> nearby = AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), (int) ZONE_RADIUS);

        // Split into caster's side (caster + allies) and enemy side (beyonders not allied)
        List<LivingEntity> casterSide = new ArrayList<>();
        List<LivingEntity> enemySide = new ArrayList<>();

        casterSide.add(entity);

        for (LivingEntity e : nearby) {
            if (e == entity) continue;
            if (!BeyonderData.isBeyonder(e)) continue;
            if (AllyUtil.areAllies(entity, e)) {
                casterSide.add(e);
            } else {
                enemySide.add(e);
            }
        }

        // Power score: sum of multiplier per beyonder
        double casterScore = casterSide.stream()
                .filter(BeyonderData::isBeyonder)
                .mapToDouble(BeyonderData::getMultiplier)
                .sum();
        double enemyScore = enemySide.stream()
                .filter(BeyonderData::isBeyonder) // Safety filter
                .mapToDouble(BeyonderData::getMultiplier)
                .sum();

        // Fizzle if sides are within 10% of each other
        double total = casterScore + enemyScore;
        if (total == 0 || Math.abs(casterScore - enemyScore) <= total * 0.10) {
            if (entity instanceof net.minecraft.server.level.ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.exile_of_balance.already_balanced")
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }

        // Identify dominant side and exile members until scores are roughly equal
        boolean enemyDominant = enemyScore > casterScore;
        List<LivingEntity> dominantSide = enemyDominant ? enemySide : new ArrayList<>(casterSide);
        if (!enemyDominant) dominantSide.remove(entity); // never exile caster

        // Sort descending by multiplier (highest multiplier = highest power = exiled first)
        dominantSide.sort((e1, e2) -> Double.compare(BeyonderData.getMultiplier(e2), BeyonderData.getMultiplier(e1)));

        double weakerScore = enemyDominant ? casterScore : enemyScore;
        double dominantScore = enemyDominant ? enemyScore : casterScore;

        long gameTime = serverLevel.getGameTime();
        int exiledCount = 0;
        int MIN_EXILE_DURATION = 2400;
        int MAX_EXILE_DURATION = 4800;

        for (LivingEntity target : dominantSide) {
            if (dominantScore <= weakerScore + (weakerScore * 0.10)) break;

            double power = BeyonderData.getMultiplier(target);
            int durationTicks = MIN_EXILE_DURATION + random.nextInt(MAX_EXILE_DURATION - MIN_EXILE_DURATION + 1);
            EXILED_ENTITIES.put(target.getUUID(), gameTime + durationTicks);
            dominantScore -= power;
            exiledCount++;

            int durationSeconds = durationTicks / 20;
            if (target instanceof net.minecraft.server.level.ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.exile_of_balance.removed_prefix")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(String.valueOf(durationSeconds)).withStyle(ChatFormatting.WHITE))
                        .append(Component.translatable("ability.lotmcraft.exile_of_balance.removed_suffix").withStyle(ChatFormatting.GOLD)));
            }
        }

        if (exiledCount == 0) {
            if (entity instanceof net.minecraft.server.level.ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.exile_of_balance.already_balanced")
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }

        Vec3 center = entity.position();

        // Pale blue/white ring
        RingEffectManager.createRingForAll(center, (float) ZONE_RADIUS, 40,
                0.75f, 0.85f, 1.0f, 0.8f, 2f, 4f, serverLevel);

        Component message = Component.translatable("ability.lotmcraft.exile_of_balance.declared")
                .withStyle(ChatFormatting.GOLD);
        serverLevel.getServer().getPlayerList().getPlayers().forEach(p -> {
            if (p.level().equals(serverLevel) && p.distanceTo(entity) <= ZONE_RADIUS) {
                p.sendSystemMessage(message);
            }
        });

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, center, entity, this, interactionFlags, ZONE_RADIUS, 20 * 2));
    }
}