package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExileOfBalanceAbility extends Ability {

    public static final Map<UUID, Long> EXILED_ENTITIES = new ConcurrentHashMap<>();

    private static final double ZONE_RADIUS = 80.0;

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

        double casterScore = casterSide.stream()
                .filter(BeyonderData::isBeyonder)
                .mapToDouble(BeyonderData::getMultiplier)
                .sum();
        double enemyScore = enemySide.stream()
                .filter(BeyonderData::isBeyonder)
                .mapToDouble(BeyonderData::getMultiplier)
                .sum();

        double total = casterScore + enemyScore;
        if (total == 0 || Math.abs(casterScore - enemyScore) <= total * 0.10) {
            if (entity instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.exile_of_balance.already_balanced")
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }

        boolean enemyDominant = enemyScore > casterScore;
        List<LivingEntity> dominantSide = enemyDominant ? enemySide : new ArrayList<>(casterSide);
        if (!enemyDominant) dominantSide.remove(entity);

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
            if (target instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.exile_of_balance.removed_prefix")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(String.valueOf(durationSeconds)).withStyle(ChatFormatting.WHITE))
                        .append(Component.translatable("ability.lotmcraft.exile_of_balance.removed_suffix").withStyle(ChatFormatting.GOLD)));
            }

            playExileEffect(serverLevel, entity, target, durationTicks);
        }

        if (exiledCount == 0) {
            if (entity instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.exile_of_balance.already_balanced")
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }

        Vec3 center = entity.position();
        Location casterLocation = new Location(center, serverLevel);

        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5f, 0.4f);

        RingEffectManager.createRingForAll(center, (float) ZONE_RADIUS, 80,
                1.0f, 0.92f, 0.6f, 1.0f, 3f, 8f, serverLevel);
        RingEffectManager.createRingForAll(center, (float) (ZONE_RADIUS * 0.5), 60,
                1.0f, 0.85f, 0.45f, 0.7f, 2f, 6f, serverLevel);

        ServerScheduler.scheduleForDuration(0, 3, 60, () -> {
            double angle = (serverLevel.getGameTime() % 360) * Math.PI / 180.0;
            for (int i = 0; i < 6; i++) {
                double a = angle + (2 * Math.PI * i) / 6;
                double x = center.x + ZONE_RADIUS * Math.cos(a);
                double z = center.z + ZONE_RADIUS * Math.sin(a);
                Vec3 edgePos = new Vec3(x, center.y + 0.1, z);
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, edgePos, 2, 0.4, 0.0);
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.DUST_PLUME, edgePos, 3, 0.6, 0.0);
            }
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, center.add(0, 1.5, 0), 3, 0.5, 0.0);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.DUST_PLUME, center.add(0, 1.0, 0), 4, 0.8, 0.0);
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, casterLocation));

        ServerScheduler.scheduleDelayed(20, () ->
                        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.2f, 0.55f),
                serverLevel);

        Component message = Component.translatable("ability.lotmcraft.exile_of_balance.declared")
                .withStyle(ChatFormatting.GOLD);
        serverLevel.getServer().getPlayerList().getPlayers().forEach(p -> {
            if (p.level().equals(serverLevel) && p.distanceTo(entity) <= ZONE_RADIUS) {
                p.sendSystemMessage(message);
            }
        });

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, center, entity, this, interactionFlags, ZONE_RADIUS, 20 * 2));
    }

    private void playExileEffect(ServerLevel serverLevel, LivingEntity caster, LivingEntity target, int durationTicks) {
        Vec3 targetPos = target.position().add(0, 1, 0);
        Location targetLocation = new Location(target.position(), serverLevel);

        serverLevel.playSound(null, target.blockPosition(), SoundEvents.ILLUSIONER_PREPARE_BLINDNESS, SoundSource.PLAYERS, 1.0f, 0.7f);

        ServerScheduler.scheduleForDuration(0, 2, 50, () -> {
            Vec3 pos = targetLocation.getPosition().add(0, 1, 0);
            ParticleUtil.spawnCircleParticles(serverLevel, ParticleTypes.END_ROD, pos, 2.5, 20);
            ParticleUtil.spawnCircleParticles(serverLevel, ParticleTypes.DUST_PLUME, pos.add(0, 0.2, 0), 1.8, 14);
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.DUST_PLUME, pos, 2.8, 5);
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(caster, targetLocation));

        ServerScheduler.scheduleDelayed(48, () -> {
            Vec3 pos = targetLocation.getPosition().add(0, 1, 0);
            serverLevel.playSound(null, target.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.2f, 0.6f);
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.END_ROD, pos, 2.0, 50);
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.DUST_PLUME, pos, 2.8, 40);
            RingEffectManager.createRingForAll(target.position(), 3.5f, 55,
                    1.0f, 0.92f, 0.6f, 1.0f, 2f, 5f, serverLevel);
        }, serverLevel);

        ServerScheduler.scheduleDelayed(durationTicks - 1, () -> {
            Vec3 pos = targetLocation.getPosition().add(0, 1, 0);
            serverLevel.playSound(null, target.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 0.75f);
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.END_ROD, pos, 2.5, 60);
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.DUST_PLUME, pos, 3.0, 45);
            RingEffectManager.createRingForAll(target.position(), 4f, 55,
                    1.0f, 0.92f, 0.6f, 0.7f, 1.5f, 5f, serverLevel);
        }, serverLevel);
    }
}