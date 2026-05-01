package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ClientScheduler;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class JurisdictionAbility extends Ability {

    public static final List<JurisdictionZone> ACTIVE_ZONES = new CopyOnWriteArrayList<>();

    private static final DustParticleOptions GOLD_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.75f, 0.0f), 1.2f);
    private static final DustParticleOptions PALE_GOLD_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.9f, 0.4f), 0.8f);
    private static final DustParticleOptions ENTITY_HIGHLIGHT_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.6f, 0.0f), 1.0f);

    public JurisdictionAbility(String id) {
        super(id, 0f, "jurisdiction");
        hasOptimalDistance = false;
        this.doesNotIncreaseDigestion = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("justiciar", 8);
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;
        Optional<JurisdictionZone> existing = ACTIVE_ZONES.stream()
                .filter(z -> z.ownerId.equals(entity.getUUID()))
                .findFirst();

        if (existing.isPresent()) {
            JurisdictionZone zone = existing.get();
            zone.deactivate();
            ACTIVE_ZONES.remove(zone);

            serverLevel.playSound(null, entity.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 0.6f);
            AbilityUtil.sendActionBar(entity, Component.literal("§6⚖ §eJurisdiction §7dismissed §6⚖"));
            NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, 0, 0));
            return;
        }

        int radius = 200 * (int) multiplier(entity);
        JurisdictionZone zone = new JurisdictionZone(entity.getUUID(), serverLevel, (int) entity.getX(), (int) entity.getZ(), radius);
        ACTIVE_ZONES.add(zone);

        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 0.5f);
        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.6f, 0.4f);
        AbilityUtil.sendActionBar(entity, Component.literal("§6⚖ §eJurisdiction §festablished §6⚖"));

        spawnEstablishEffect(serverLevel, entity.position(), radius);
        startZoneTasks(zone, entity);

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, radius, 0));
    }

    private void spawnEstablishEffect(ServerLevel level, Vec3 center, int radius) {
        Location loc = new Location(center, level);
        double displayRadius = Math.min(radius, 24);

        ParticleUtil.createParticleSpirals(
                GOLD_DUST, loc,
                1.0, displayRadius, 5.0, 2.5, 1.8,
                60, 6, 4
        );

        ServerScheduler.scheduleForDuration(0, 1, 50, () -> {
            for (int deg = 0; deg < 360; deg += 8) {
                double rad = Math.toRadians(deg);
                double x = center.x + displayRadius * Math.cos(rad);
                double z = center.z + displayRadius * Math.sin(rad);
                double pulse = Math.sin(System.currentTimeMillis() * 0.005) * 0.3;
                level.sendParticles(GOLD_DUST, x, center.y + pulse, z, 1, 0, 0.15, 0, 0);
                if (deg % 24 == 0) {
                    level.sendParticles(ParticleTypes.ENCHANT, x, center.y, z, 2, 0, 0.4, 0, 0.04);
                }
            }
        }, level);

        ServerScheduler.scheduleForDuration(0, 2, 50, () -> {
            for (int i = 0; i < 6; i++) {
                double angle = Math.toRadians((System.currentTimeMillis() / 20.0 + i * 60) % 360);
                double x = center.x + displayRadius * 0.5 * Math.cos(angle);
                double z = center.z + displayRadius * 0.5 * Math.sin(angle);
                level.sendParticles(PALE_GOLD_DUST, x, center.y + 0.5, z, 1, 0.1, 0.2, 0.1, 0);
            }
        }, level);
    }

    private void startZoneTasks(JurisdictionZone zone, LivingEntity owner) {
        Location ownerLoc = new Location(owner.position(), zone.level);

        ServerScheduler.scheduleRepeating(
                0, 10, -1,
                () -> {
                    if (!zone.active) return;
                    double y = owner.getY();
                    double time = System.currentTimeMillis() * 0.003;
                    for (int deg = 0; deg < 360; deg += 5) {
                        double rad = Math.toRadians(deg);
                        double x = zone.centerX + zone.radius * Math.cos(rad);
                        double z = zone.centerZ + zone.radius * Math.sin(rad);
                        double yOff = Math.sin(time + Math.toRadians(deg)) * 0.3;
                        zone.level.sendParticles(GOLD_DUST, x, y + yOff, z, 1, 0, 0.05, 0, 0);
                        if (deg % 30 == 0) {
                            zone.level.sendParticles(ParticleTypes.ENCHANT, x, y + 0.5, z, 1, 0, 0.3, 0, 0.02);
                        }
                    }
                },
                zone.level,
                () -> zone.active
        );

        ServerScheduler.scheduleRepeating(
                0, 5, -1,
                () -> {
                    if (!zone.active) return;

                    List<Player> nearby = zone.level.getEntitiesOfClass(
                            Player.class,
                            owner.getBoundingBox().inflate(zone.radius),
                            e -> e != owner
                    );

                    Set<UUID> seen = new HashSet<>();
                    for (Player e : nearby) {
                        boolean inside = zone.contains(e);
                        UUID id = e.getUUID();
                        seen.add(id);

                        if (inside && !zone.inside.contains(id)) {
                            zone.inside.add(id);
                            owner.sendSystemMessage(Component.literal(
                                    "§6⚖ §e" + e.getName().getString() + " §fhas entered your §eJurisdiction §6⚖"
                            ));
                            zone.level.playSound(null, owner.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.4f, 1.5f);
                        }

                        if (!inside && zone.inside.contains(id)) {
                            zone.inside.remove(id);
                            owner.sendSystemMessage(Component.literal(
                                    "§6⚖ §e" + e.getName().getString() + " §fhas left your §eJurisdiction §6⚖"
                            ));
                            zone.level.playSound(null, owner.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.4f, 0.8f);
                        }
                    }

                    zone.inside.removeIf(id -> !seen.contains(id));
                },
                zone.level,
                () -> zone.active
        );

        ClientScheduler.scheduleUntil(null, () -> {
            Level clientLevel = owner.level();
            if (!clientLevel.isClientSide) return;
            ClientLevel cl = (ClientLevel) clientLevel;

            for (JurisdictionZone z : ACTIVE_ZONES) {
                if (!z.ownerId.equals(owner.getUUID())) continue;

                List<LivingEntity> entities = cl.getEntitiesOfClass(
                        LivingEntity.class,
                        owner.getBoundingBox().inflate(z.radius),
                        e -> e != owner && z.contains(e)
                );

                for (LivingEntity target : entities) {
                    double height = target.getBbHeight();
                    Vec3 pos = target.position();
                    for (double yOff = 0; yOff <= height; yOff += 0.35) {
                        double angle = (System.currentTimeMillis() * 0.003 + yOff * 40) % (2 * Math.PI);
                        double ox = Math.cos(angle) * 0.45;
                        double oz = Math.sin(angle) * 0.45;
                        cl.addParticle(ENTITY_HIGHLIGHT_DUST,
                                pos.x + ox, pos.y + yOff, pos.z + oz,
                                0, 0, 0);
                    }

                    ParticleUtil.spawnCircleParticles(cl, GOLD_DUST,
                            pos.add(0, 0.05, 0), 0.5, 8);
                }
            }
        }, null, zone.stopCondition);
    }

    public static boolean isInsideJurisdiction(LivingEntity e) {
        for (JurisdictionZone z : ACTIVE_ZONES) {
            if (z.ownerId.equals(e.getUUID()) && z.contains(e)) return true;
        }
        return false;
    }

    public static int getModifiedEyeRadius(LivingEntity e, int original) {
        return isInsideJurisdiction(e) ? original * 2 : original;
    }

    public static boolean isEyeFree(LivingEntity e) {
        return isInsideJurisdiction(e);
    }

    public static class JurisdictionZone {
        public final UUID ownerId;
        public final ServerLevel level;
        public final int centerX, centerZ;
        public final int radius;

        public boolean active = true;
        public final Set<UUID> inside = new HashSet<>();
        public final AtomicBoolean stopCondition = new AtomicBoolean(false);

        public JurisdictionZone(UUID ownerId, ServerLevel level, int x, int z, int radius) {
            this.ownerId = ownerId;
            this.level = level;
            this.centerX = x;
            this.centerZ = z;
            this.radius = radius;
        }

        public void deactivate() {
            active = false;
            stopCondition.set(true);
        }

        public boolean contains(LivingEntity e) {
            double dx = e.getX() - centerX;
            double dz = e.getZ() - centerZ;
            return (dx * dx + dz * dz) <= (radius * radius);
        }
    }
}