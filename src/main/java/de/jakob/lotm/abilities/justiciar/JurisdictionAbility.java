package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class JurisdictionAbility extends Ability {

    public static final List<JurisdictionZone> ACTIVE_ZONES = new CopyOnWriteArrayList<>();

    public JurisdictionAbility(String id) {
        super(id, 0f, "jurisdiction");
        hasOptimalDistance = false;
        this.doesNotIncreaseDigestion=false;
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

        Optional<JurisdictionZone> existing = ACTIVE_ZONES.stream()
                .filter(z -> z.ownerId.equals(entity.getUUID()))
                .findFirst();
        int RADIUS = 300 *(int) multiplier(entity);


        if (existing.isPresent()) {
            JurisdictionZone zone = existing.get();
            zone.active = false;
            ACTIVE_ZONES.remove(zone);
            entity.sendSystemMessage(Component.literal("Your Jurisdiction has been dismissed."));
            NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, entity.position(), entity, this, interactionFlags, 0, 0));
            return;
        }

        JurisdictionZone zone = new JurisdictionZone(
                entity.getUUID(),
                (ServerLevel) level,
                (int) entity.getX(),
                (int) entity.getZ(),
                RADIUS
        );
        ACTIVE_ZONES.add(zone);

        entity.sendSystemMessage(Component.literal("You have established your Jurisdiction."));

        startZoneTasks(zone, entity);

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, entity.position(), entity, this, interactionFlags, RADIUS, 0));
    }

    private void startZoneTasks(JurisdictionZone zone, LivingEntity owner) {
        int BOUNDARY_TICK_RATE = 10;
        ServerScheduler.scheduleRepeating(
                0,
                BOUNDARY_TICK_RATE,
                -1,
                () -> {
                    if (!zone.active) return;
                    double y = owner.getY();
                    for (int deg = 0; deg < 360; deg += 5) {
                        double rad = Math.toRadians(deg);
                        double x = zone.centerX + zone.radius * Math.cos(rad);
                        double z = zone.centerZ + zone.radius * Math.sin(rad);
                        zone.level.sendParticles(ParticleTypes.GLOW, x, y, z, 1, 0, 0, 0, 0);
                    }
                },
                zone.level,
                () -> zone.active
        );
        int TRACK_TICK_RATE = 5;
        ServerScheduler.scheduleRepeating(
                0,
                TRACK_TICK_RATE,
                -1,
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
                            owner.sendSystemMessage(Component.literal(e.getName().getString() + " has entered your Jurisdiction."));
                        }

                        if (!inside && zone.inside.contains(id)) {
                            zone.inside.remove(id);
                            owner.sendSystemMessage(Component.literal(e.getName().getString() + " has left your Jurisdiction."));
                        }
                    }

                    zone.inside.removeIf(id -> !seen.contains(id));
                },
                zone.level,
                () -> zone.active
        );
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

        public JurisdictionZone(UUID ownerId, ServerLevel level, int x, int z, int radius) {
            this.ownerId = ownerId;
            this.level = level;
            this.centerX = x;
            this.centerZ = z;
            this.radius = radius;
        }

        public boolean contains(LivingEntity e) {
            double dx = e.getX() - centerX;
            double dz = e.getZ() - centerZ;
            return (dx * dx + dz * dz) <= (radius * radius);
        }
    }
}