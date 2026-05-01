package de.jakob.lotm.abilities.death;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.death_pathway.UnderworldGateEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.helper.subordinates.SubordinateUtils;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class DoorToTheUnderworldAbility extends SelectableAbility {

    private static final int DURATION_TICKS = 20 * 20;
    private static final int SPAWN_INTERVAL = 20 * 4;

    private static final String[] MODES = {
            "ability.lotmcraft.door_to_the_underworld.spirits",
            "ability.lotmcraft.door_to_the_underworld.tentacles",
            "ability.lotmcraft.door_to_the_underworld.release"
    };

    private static final HashMap<UUID, List<Mob>> summonedMobs = new HashMap<>();
    private static final HashMap<UUID, UnderworldGateEntity> openPortal = new HashMap<>();

    private static final HashMap<UUID, UUID> activePortalTasks = new HashMap<>();

    private static final DustParticleOptions SOUL_DUST =
            new DustParticleOptions(new Vector3f(0.15f, 0.85f, 0.75f), 1.5f);
    private static final DustParticleOptions DARK_DUST =
            new DustParticleOptions(new Vector3f(0.05f, 0.0f, 0.15f), 2.0f);

    public DoorToTheUnderworldAbility(String id) {
        super(id, 10f, "death");
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 600;
    }

    @Override
    protected String[] getAbilityNames() {
        return MODES;
    }

    @Override
    public void useAbility(ServerLevel serverLevel, LivingEntity entity, boolean consumeSpirituality, boolean hasToHaveAbility, boolean hasToMeetRequirements) {
        // Release sub-ability bypasses cooldown and spirituality cost
        if (getSelectedAbilityIndex(entity.getUUID()) == 1) {
            onAbilityUse(serverLevel, entity);
            return;
        }
        super.useAbility(serverLevel, entity, consumeSpirituality, hasToHaveAbility, hasToMeetRequirements);
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer)) selectedAbility = 1;

        switch (selectedAbility) {
            case 0 -> {
                if (InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(entity.position(), serverLevel), "purification", BeyonderData.getSequence(entity), -1)) return;
                spiritSummonPortal(serverLevel, (ServerPlayer) entity);
            }
            case 1 -> {
                if (InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(entity.position(), serverLevel), "purification", BeyonderData.getSequence(entity), -1))
                    return;
                tentaclePortal(serverLevel, entity);
            }
            case 2 -> release((ServerPlayer) entity);
        }
    }

    private void tentaclePortal(ServerLevel serverLevel, LivingEntity caster) {
        if(openPortal.containsKey(caster.getUUID())) {
            openPortal.get(caster.getUUID()).discard();
            openPortal.remove(caster.getUUID());
            return;
        }

        Vec3 portalCenter = AbilityUtil.getTargetLocation(caster, 6, 1);
        LivingEntity portalLookAtEntity = AbilityUtil.getTargetEntity(caster, 8, 2);
        if(portalLookAtEntity != null) {
            portalCenter = VectorUtil.getRelativePosition(portalCenter,VectorUtil.getPerpendicularVector(caster.getLookAngle()), -4, 0, 0);
        } else {
            portalLookAtEntity = caster;
        }

        UnderworldGateEntity entity = new UnderworldGateEntity(serverLevel, true);
        entity.setPos(portalCenter.x, portalCenter.y - .6, portalCenter.z);
        Vec3 lookVec = portalLookAtEntity.position().subtract(portalCenter).normalize();
        entity.setYRot(yawFromVector(lookVec));
        entity.setXRot(0);
        serverLevel.addFreshEntity(entity);

        openPortal.put(caster.getUUID(), entity);

        Vec3 finalPortalCenter = portalCenter;
        ServerScheduler.scheduleForDuration(10, 10, DURATION_TICKS, () -> {
            AbilityUtil.damageNearbyEntities(serverLevel,
                    caster,
                    6.5,
                    DamageLookup.lookupDamage(5, .85) * multiplier(caster),
                    finalPortalCenter.add(entity.getLookAngle().normalize().scale(2)),
                    true,
                    false
            );
        }, () -> {
            activePortalTasks.remove(caster.getUUID());
            entity.discard();
            openPortal.remove(caster.getUUID());
        }, serverLevel, () -> 1.0);
    }

    private void spiritSummonPortal(ServerLevel serverLevel, ServerPlayer player) {
        if(openPortal.containsKey(player.getUUID())) {
            openPortal.get(player.getUUID()).discard();
            openPortal.remove(player.getUUID());
            return;
        }

        Vec3 portalCenter = AbilityUtil.getTargetLocation(player, 6, 1);
        LivingEntity portalLookAtEntity = AbilityUtil.getTargetEntity(player, 6, 1);
        if(portalLookAtEntity == null) portalLookAtEntity = player;

        AtomicInteger spawnTick = new AtomicInteger(0);

        UnderworldGateEntity entity = new UnderworldGateEntity(serverLevel, false);
        entity.setPos(portalCenter.x, portalCenter.y - .6, portalCenter.z);
        Vec3 lookVec = portalLookAtEntity.position().subtract(portalCenter).normalize();
        entity.setYRot(yawFromVector(lookVec));
        entity.setXRot(0);
        serverLevel.addFreshEntity(entity);

        openPortal.put(player.getUUID(), entity);

        UUID taskId = ServerScheduler.scheduleForDuration(0, 1, DURATION_TICKS, () -> {
            int tick = spawnTick.getAndIncrement();
            if (tick % SPAWN_INTERVAL == 0) {
                spawnWave(serverLevel, player, portalCenter);
            }
        }, () -> {
            activePortalTasks.remove(player.getUUID());
            entity.discard();
            openPortal.remove(player.getUUID());
        }, serverLevel, () -> AbilityUtil.getTimeInArea(player, new Location(portalCenter, serverLevel)));

        activePortalTasks.put(player.getUUID(), taskId);
    }

    private static float yawFromVector(Vec3 dir) {
        if (dir.lengthSqr() < 1.0E-6) return 0.0F;
        return (float)(Math.toDegrees(Math.atan2(-dir.x, dir.z)));
    }

    private void release(ServerPlayer player) {
        UUID taskId = activePortalTasks.remove(player.getUUID());
        List<Mob> mobs = summonedMobs.remove(player.getUUID());

        if (taskId == null && (mobs == null || mobs.isEmpty())) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.door_to_the_underworld.none_summoned").withColor(0xFF334f23));
            return;
        }

        if (taskId != null) {
            ServerScheduler.cancel(taskId);
        }

        if(openPortal.containsKey(player.getUUID())) {
            openPortal.get(player.getUUID()).discard();
            openPortal.remove(player.getUUID());
        }

        if (mobs != null) {
            for (Mob mob : mobs) {
                if (mob.isAlive()) mob.discard();
            }
        }

        AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.door_to_the_underworld.released").withColor(0xFF334f23));
    }


    private void spawnWave(ServerLevel level, ServerPlayer player, Vec3 portalCenter) {
        spawnMob(level, player, portalCenter, pickUndead(level));
        spawnMob(level, player, portalCenter, pickSpirit(level));
    }

    private void spawnMob(ServerLevel level, ServerPlayer player, Vec3 portalCenter, Mob mob) {
        if (mob == null) return;

        // Spread mobs horizontally around the portal base
        double angle = random.nextDouble() * Math.PI * 2;
        double r = random.nextDouble() * 1.25;
        Vec3 spawnPos = portalCenter.add(Math.cos(angle) * r, 0, Math.sin(angle) * r);

        net.minecraft.core.BlockPos blockPos = net.minecraft.core.BlockPos.containing(spawnPos);
        while (blockPos.getY() > level.getMinBuildHeight() && level.getBlockState(blockPos).isAir()) {
            blockPos = blockPos.below();
        }
        Vec3 groundPos = Vec3.atBottomCenterOf(blockPos.above());

        mob.setPos(groundPos.x, groundPos.y, groundPos.z);
        level.addFreshEntity(mob);

        SubordinateUtils.turnEntityIntoSubordinate(mob, player, false);

        summonedMobs.computeIfAbsent(player.getUUID(), k -> new ArrayList<>()).add(mob);

        ParticleUtil.spawnSphereParticles(level, DARK_DUST, groundPos.add(0, 1, 0), 0.8, 20);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                groundPos.x, groundPos.y + 0.5, groundPos.z, 8, 0.3, 0.4, 0.3, 0.05);

        ServerScheduler.scheduleDelayed(20 * 60, () -> {
            if (mob.isAlive()) {
                mob.discard();
            }
        }, level);
    }

    private Mob pickUndead(ServerLevel level) {
        return switch (random.nextInt(6)) {
            case 0 -> new Zombie(EntityType.ZOMBIE, level);
            case 1 -> new Skeleton(EntityType.SKELETON, level);
            case 2 -> new Husk(EntityType.HUSK, level);
            case 3 -> new Drowned(EntityType.DROWNED, level);
            case 4 -> new Stray(EntityType.STRAY, level);
            default -> new WitherSkeleton(EntityType.WITHER_SKELETON, level);
        };
    }

    private Mob pickSpirit(ServerLevel level) {
        return switch (random.nextInt(5)) {
            case 0 -> (Mob) ModEntities.SPIRIT_GHOST.get().create(level);
            case 1 -> (Mob) ModEntities.SPIRIT_DERVISH_ENTITY.get().create(level);
            case 2 -> (Mob) ModEntities.SPIRIT_BUBBLES_ENTITY.get().create(level);
            case 3 -> (Mob) ModEntities.SPIRIT_BLUE_WIZARD.get().create(level);
            default -> (Mob) ModEntities.SPIRIT_TRANSLUCENT_WIZARD.get().create(level);
        };
    }
}
