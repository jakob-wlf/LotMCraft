package de.jakob.lotm.beyonders.abilities.abyss;

import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.entity.custom.ability_entities.MeteorEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.PlayPhotonBlockEffectPacket;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlamesOfTheAbyssAbility extends SelectableAbility {

    public FlamesOfTheAbyssAbility(String id) {
        super(id, 10);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 800;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.flames_of_the_abyss.meteor_rain",
                "ability.lotmcraft.flames_of_the_abyss.abyss_pillars",
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch (abilityIndex) {
            case 0 -> meteorRain(level, entity);
            case 1 -> abyssPillars(level, entity);
        }
    }


    private void meteorRain(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 80, 1.5f);
        boolean griefing = BeyonderData.isGriefingEnabled(entity);
        float damage = (float) (DamageLookup.lookupDamage(1, 0.85) * multiplier(entity));

        level.playSound(null, BlockPos.containing(entity.position()),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 3f, 0.6f);

        // Stagger 5 meteors every 15 ticks
        for (int i = 0; i < 8; i++) {
            final int idx = i;
            ServerScheduler.scheduleDelayed(i * 15, () -> {
                // Slight random scatter around the target
                double ox = (random.nextDouble() - 0.5) * 14;
                double oz = (random.nextDouble() - 0.5) * 14;
                Vec3 landPos = targetPos.add(ox, 0, oz);

                MeteorEntity meteor = new MeteorEntity(serverLevel,
                        1.6f, damage, 2.5f,
                        entity, griefing, 16f, 10f);
                meteor.setColor(0.05f, 1.0f, 0.05f);
                meteor.setCustomColor(true);
                meteor.setAbyssImpact(false);
                meteor.setPosition(landPos);
                serverLevel.addFreshEntity(meteor);
            }, serverLevel);
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, targetPos, entity, this, new String[]{"explosion", "burning"}, 12, 20 * 10));
    }


    private static final int RING_PILLAR_COUNT = 5;
    private static final double RING_RADIUS = 9.0;
    private static final int RANDOM_PILLAR_COUNT = 17;

    private void abyssPillars(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        level.playSound(null, BlockPos.containing(entity.position()),
                SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 3f, 0.5f);

        float damage = (float) (DamageLookup.lookupDamage(1, 0.4) * multiplier(entity));

        List<Vec3> pillarPositions = new ArrayList<>();

        double angleOffset = random.nextDouble() * Math.PI * 2; // optional: random rotation of the ring
        for (int i = 0; i < RING_PILLAR_COUNT; i++) {
            double angle = angleOffset + (i / (double) RING_PILLAR_COUNT) * Math.PI * 2;
            pillarPositions.add(getPillarPosition(level, entity, angle, RING_RADIUS, false));
        }

        for (int i = 0; i < RANDOM_PILLAR_COUNT; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = 10 + random.nextDouble() * 64;
            pillarPositions.add(getPillarPosition(level, entity, angle, dist, true));
        }

        for (int i = 0; i < pillarPositions.size(); i++) {
            Vec3 pos = pillarPositions.get(i);
            int delay = i * 2;

            ServerScheduler.scheduleDelayed(delay, () -> {
                BlockPos blockPos = BlockPos.containing(pos);

                double offsetX = pos.x - (blockPos.getX() + 0.5);
                double offsetY = pos.y - (blockPos.getY() + 0.5) - 1;
                double offsetZ = pos.z - (blockPos.getZ() + 0.5);

                PacketHandler.sendToNearbyPlayers(
                        new PlayPhotonBlockEffectPacket("abyss_pillars", blockPos, offsetX, offsetY, offsetZ, 1.5, null, -1, false, true, new Vec3(1.75, 2, 1.75)),
                        serverLevel, pos, 128
                );
            }, serverLevel);

            ServerScheduler.scheduleForDuration(delay, 4, 20 * 7, () -> {
                AbilityUtil.damageNearbyEntities(serverLevel, entity, 9, damage, pos, true, false);
                AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 1.5).forEach(e -> {
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 20, 4));
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 6, 1));
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 10, 3));
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 5, 0));
                });
            }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(pos, serverLevel)));
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this,
                new String[]{"corruption", "burning"}, 7, 20 * 3));
    }

    private BlockPos findGroundPos(Level level, Vec3 xzPos) {
        int x = (int) Math.floor(xzPos.x);
        int z = (int) Math.floor(xzPos.z);

        if (!level.hasChunkAt(new BlockPos(x, 0, z))) return null;

        int baseY = (int) Math.floor(xzPos.y);
        int startY = baseY + 8;
        int minY = Math.max(level.getMinBuildHeight(), baseY - 24);

        for (int y = startY; y >= minY; y--) {
            BlockPos check = new BlockPos(x, y, z);
            BlockPos below = check.below();
            if (!level.getBlockState(check).blocksMotion()
                    && level.getBlockState(below).blocksMotion()) {
                return check;
            }
        }
        return null; // no ground found
    }

    private Vec3 getPillarPosition(Level level, LivingEntity entity, double angle, double dist, boolean randomRetry) {
        int attempts = randomRetry ? 6 : 1;

        for (int i = 0; i < attempts; i++) {
            double a = (i == 0) ? angle : random.nextDouble() * Math.PI * 2;
            double d = (i == 0) ? dist : 10 + random.nextDouble() * 64;

            Vec3 candidate = new Vec3(
                    entity.getX() + Math.cos(a) * d,
                    entity.getY(),
                    entity.getZ() + Math.sin(a) * d
            );
            BlockPos ground = findGroundPos(level, candidate);
            if (ground != null) {
                return new Vec3(ground.getX(), ground.getY(), ground.getZ());
            }
        }

        if (!randomRetry) {
            return new Vec3(
                    Math.floor(entity.getX() + Math.cos(angle) * dist),
                    Math.floor(entity.getY()),
                    Math.floor(entity.getZ() + Math.sin(angle) * dist)
            );
        }
        return null;
    }
}
