package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ConfinementAbility extends Ability {

    public static final List<ConfinementZone> ACTIVE_ZONES = new CopyOnWriteArrayList<>();

    private static final DustParticleOptions GOLD_DUST      = new DustParticleOptions(new Vector3f(1.0f, 0.80f, 0.0f), 1.3f);
    private static final DustParticleOptions PALE_GOLD_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.92f, 0.45f), 0.85f);
    private static final DustParticleOptions EMBER_DUST     = new DustParticleOptions(new Vector3f(1.0f, 0.35f, 0.05f), 1.0f);

    public ConfinementAbility(String id) {
        super(id, 30f, "confinement");
        interactionRadius = 15;
        hasOptimalDistance = false;
        postsUsedAbilityEventManually = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 600;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        ACTIVE_ZONES.stream()
                .filter(z -> z.ownerId.equals(entity.getUUID()))
                .findFirst()
                .ifPresent(existing -> removeZone(existing, serverLevel));

        int radius   = 6;
        int duration = 1200 * (int) Math.max(multiplier(entity) / 4, 1);
        Vec3 center  = AbilityUtil.getTargetLocation(entity, 12, 2f);

        List<BlockPos> placed = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) != radius && Math.abs(dy) != radius && Math.abs(dz) != radius) continue;
                    BlockPos pos = BlockPos.containing(center.x + dx, center.y + dy, center.z + dz);
                    if (!serverLevel.getBlockState(pos).isAir()) continue;
                    serverLevel.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 3);
                    placed.add(pos);
                }
            }
        }

        ConfinementZone zone = new ConfinementZone(entity.getUUID(), center, radius, serverLevel, placed);
        ACTIVE_ZONES.add(zone);

        spawnSummonEffect(serverLevel, center, radius);

        Location centerLoc = new Location(center, serverLevel);

        ServerScheduler.scheduleForDuration(0, 8, duration, () -> {
                    if (!zone.isActive()) return;
                    spawnEdgeParticles(serverLevel, center, radius);
                    spawnCornerFlares(serverLevel, center, radius);
                }, () -> removeZone(zone, serverLevel), serverLevel,
                () -> AbilityUtil.getTimeInArea(entity, centerLoc));

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, center, entity, this, interactionFlags, radius, 20 * 2));
    }

    private static void spawnSummonEffect(ServerLevel level, Vec3 center, int radius) {
        Location loc = new Location(center, level);

        ParticleUtil.createParticleSpirals(
                GOLD_DUST, loc,
                0.5, radius, radius, 3.0, 2.0, 60, 4, 3
        );

        ServerScheduler.scheduleForDuration(0, 1, 25, () -> {
            for (int corner = 0; corner < 8; corner++) {
                double cx = center.x + ((corner & 1) == 0 ? -radius : radius);
                double cy = center.y + ((corner & 2) == 0 ? -radius : radius);
                double cz = center.z + ((corner & 4) == 0 ? -radius : radius);
                level.sendParticles(GOLD_DUST, cx, cy, cz, 2, 0.15, 0.15, 0.15, 0);
                level.sendParticles(ParticleTypes.ENCHANT, cx, cy, cz, 3, 0.2, 0.2, 0.2, 0.08);
            }
        }, level);
    }

    private static void spawnEdgeParticles(ServerLevel level, Vec3 center, int r) {
        double cx = center.x, cy = center.y, cz = center.z;
        double step = 0.7;
        double pulse = Math.sin(System.currentTimeMillis() * 0.004) * 0.08;

        for (int sx : new int[]{-r, r}) {
            for (int sz : new int[]{-r, r}) {
                for (double dy = -r; dy <= r; dy += step) {
                    level.sendParticles(GOLD_DUST, cx + sx, cy + dy + pulse, cz + sz, 1, 0.03, 0.03, 0.03, 0);
                    if (Math.random() < 0.12) {
                        level.sendParticles(PALE_GOLD_DUST, cx + sx, cy + dy, cz + sz, 1, 0.05, 0.05, 0.05, 0);
                    }
                }
            }
        }
        for (int sy : new int[]{-r, r}) {
            for (int sz : new int[]{-r, r}) {
                for (double dx = -r; dx <= r; dx += step) {
                    level.sendParticles(GOLD_DUST, cx + dx, cy + sy + pulse, cz + sz, 1, 0.03, 0.03, 0.03, 0);
                }
            }
        }
        for (int sx : new int[]{-r, r}) {
            for (int sy : new int[]{-r, r}) {
                for (double dz = -r; dz <= r; dz += step) {
                    level.sendParticles(GOLD_DUST, cx + sx, cy + sy + pulse, cz + dz, 1, 0.03, 0.03, 0.03, 0);
                }
            }
        }
    }

    private static void spawnCornerFlares(ServerLevel level, Vec3 center, int r) {
        for (int corner = 0; corner < 8; corner++) {
            double cx = center.x + ((corner & 1) == 0 ? -r : r);
            double cy = center.y + ((corner & 2) == 0 ? -r : r);
            double cz = center.z + ((corner & 4) == 0 ? -r : r);
            if (Math.random() < 0.4) {
                level.sendParticles(EMBER_DUST, cx, cy, cz, 1, 0.1, 0.15, 0.1, 0);
            }
            if (Math.random() < 0.2) {
                level.sendParticles(ParticleTypes.ENCHANT, cx, cy, cz, 2, 0.15, 0.15, 0.15, 0.05);
            }
        }
    }

    private static void removeZone(ConfinementZone zone, ServerLevel level) {
        for (BlockPos pos : zone.barriers) {
            if (level.getBlockState(pos).is(Blocks.BARRIER)) {
                level.removeBlock(pos, false);
            }
        }
        zone.deactivate();
        ACTIVE_ZONES.remove(zone);
    }

    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        Vec3 destination = new Vec3(event.getTargetX(), event.getTargetY(), event.getTargetZ());

        for (ConfinementZone zone : ACTIVE_ZONES) {
            if (!zone.level.equals(serverLevel) || !zone.isActive()) continue;
            boolean insideNow  = entity.position().distanceTo(zone.center) <= zone.radius + 1.0;
            boolean outsideDest = destination.distanceTo(zone.center) > zone.radius;
            if (insideNow && outsideDest) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onCasterDeath(LivingDeathEvent event) {
        UUID id = event.getEntity().getUUID();
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;

        ACTIVE_ZONES.stream()
                .filter(z -> z.ownerId.equals(id))
                .findFirst()
                .ifPresent(z -> removeZone(z, serverLevel));
    }

    @SubscribeEvent
    public static void onCasterLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;

        ACTIVE_ZONES.stream()
                .filter(z -> z.ownerId.equals(id))
                .findFirst()
                .ifPresent(z -> removeZone(z, serverLevel));
    }

    public static class ConfinementZone {
        public final UUID ownerId;
        public final Vec3 center;
        public final double radius;
        public final ServerLevel level;
        public final List<BlockPos> barriers;
        private boolean active = true;

        public ConfinementZone(UUID ownerId, Vec3 center, double radius, ServerLevel level, List<BlockPos> barriers) {
            this.ownerId  = ownerId;
            this.center   = center;
            this.radius   = radius;
            this.level    = level;
            this.barriers = barriers;
        }

        public boolean isActive() { return active; }
        public void deactivate()  { active = false; }
    }
}