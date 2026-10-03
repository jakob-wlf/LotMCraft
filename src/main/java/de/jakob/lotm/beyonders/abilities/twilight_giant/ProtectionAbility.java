package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.common.CurseOfMisfortuneAbility;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.AbilityUseEvent;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.death.SpiritWorldTraversalAbility;
import de.jakob.lotm.beyonders.abilities.door.PlayerTeleportationAbility;
import de.jakob.lotm.beyonders.abilities.door.TeleportationAuthorityAbility;
import de.jakob.lotm.beyonders.abilities.door.TravelersDoorAbility;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ProtectionAbility extends ToggleAbility {

    private static final double HALF_WIDTH = 3.0D;
    private static final double BACK_DEPTH = 8.0D;
    private static final double FRONT_RANGE = 10.0D;
    private static final double WALL_HEIGHT = 4.0D;
    private static final double MIN_FRONT_DISTANCE = 1.0D;
    private static final double MOVE_DECAY_PER_BLOCK = 0.05D;
    private static final double ABILITY_DECAY = 0.2D;
    private static final double PARTICLE_SPACING = 0.8D;
    private static final double DOME_BASE_RADIUS = 15.0D;
    private static final double DOME_RADIUS_PER_SEQUENCE = 5.0D;
    private static final double DOME_MAX_RADIUS = 25.0D;
    private static final double DOME_SCAN_MARGIN = 10.0D;
    private static final double DOME_PUSH = 1.0D;
    private static final float STRONGER_CURSE_BLOCK_CHANCE = 0.5f;
    private static final double CURSE_TARGET_RANGE = 30.0D;
    private static final int BORDER_INTERVAL = 20;
    private static final int NEW_ENTITY_TICKS = 20;
    private static final double BORDER_POINTS_PER_BLOCK = 2.0D;
    private static final int DAWN_TEXT = 0xFFFFF3D6;
    private static final DustParticleOptions DAWN_DUST = new DustParticleOptions(new Vector3f(1f, 0.95f, 0.75f), 1.5f);

    private static final Map<UUID, Guard> guards = new HashMap<>();

    private static ProtectionAbility instance;

    public ProtectionAbility(String id) {
        super(id, "purification", "light_source");
        instance = this;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 8;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!level.isClientSide() && !isActiveForEntity(entity) && !holdsSword(entity)) {
            bar(entity, "ability.lotmcraft.twiligh_giant_dawn.no_sword");
            return;
        }
        super.onAbilityUse(level, entity);
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        Vec3 forward = Vec3.directionFromRotation(0, entity.getYRot());
        int sequence = BeyonderData.getSequence(entity);
        double radius = sequence <= 4 ? Math.min(DOME_MAX_RADIUS, DOME_BASE_RADIUS + DOME_RADIUS_PER_SEQUENCE * (4 - sequence)) : 0;
        if (radius > 0) {
            Dome created = new Dome(entity, forward, entity.position(), radius);
            guards.put(entity.getUUID(), created);
            if (level instanceof ServerLevel serverLevel && created.breakIfOverlapping(serverLevel)) return;
        } else {
            guards.put(entity.getUUID(), new Wall(entity, forward, new Vec3(-forward.z, 0, forward.x)));
        }
        level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE, entity.getSoundSource(), 1.5f, 1.3f);
        if (radius > 0) bar(entity, "ability.lotmcraft.protection.dome_active", (int) radius);
        else bar(entity, "ability.lotmcraft.protection.walls_active");
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Guard guard = guards.get(entity.getUUID());
        if (guard == null || !holdsSword(entity)) {
            cancel(serverLevel, entity);
            return;
        }
        guard.tick(serverLevel, entity);
        if (guard instanceof Dome dome && guards.get(entity.getUUID()) == guard && entity instanceof ServerPlayer player && player.tickCount % BORDER_INTERVAL < tickRate) {
            dome.showBorder(serverLevel, player);
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        guards.remove(entity.getUUID());
        level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_DEACTIVATE, entity.getSoundSource(), 1.5f, 1.3f);
        bar(entity, "ability.lotmcraft.protection.ended");
    }

    @SubscribeEvent
    public static void onAbilityUsed(AbilityUsedEvent event) {
        if (event.getAbility() == null || event.getAbility() instanceof ToggleAbility) return;
        if (guards.get(event.getEntity().getUUID()) instanceof Wall wall) wall.weaken();
    }

    @SubscribeEvent
    public static void onAbilityUse(AbilityUseEvent event) {
        LivingEntity caster = event.getEntity();
        if (caster == null || caster.level().isClientSide() || guards.isEmpty()) return;
        Ability ability = event.getAbility();
        Dome dome = domeContaining(caster);
        if (isSpiritWorldTeleport(ability) && dome != null && !dome.breaksThrough(caster)) {
            bar(caster, "ability.lotmcraft.protection.teleport_blocked");
            event.setCanceled(true);
            return;
        }
        if (!isCurse(ability)) return;
        if (dome == null) {
            LivingEntity target = AbilityUtil.getTargetEntity(caster, (int) CURSE_TARGET_RANGE, 2);
            if (target != null) dome = domeContaining(target);
        }
        if (dome == null || dome.entity == caster) return;
        boolean stronger = BeyonderData.getSequence(caster) < BeyonderData.getSequence(dome.entity);
        if (stronger && caster.getRandom().nextFloat() >= STRONGER_CURSE_BLOCK_CHANCE) return;
        bar(caster, "ability.lotmcraft.protection.curse_blocked");
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onTravelToDimension(EntityTravelToDimensionEvent event) {
        if (guards.isEmpty() || !event.getDimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) return;
        Dome dome = domeContaining(event.getEntity());
        if (dome == null) return;
        if (dome.breaksThrough(event.getEntity())) {
            if (dome.entity.level() instanceof ServerLevel level) dome.shatter(level);
            return;
        }
        event.setCanceled(true);
        if (event.getEntity() instanceof LivingEntity living) bar(living, "ability.lotmcraft.protection.teleport_blocked");
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide() || guards.isEmpty()) return;
        Dome dome = domeContaining(victim);
        if (dome != null && !victim.getUUID().equals(dome.entity.getUUID()) && AllyUtil.areAllies(dome.entity, victim)) {
            event.setCanceled(true);
            return;
        }
        for (Guard guard : guards.values()) {
            if (guard instanceof Wall wall && wall.absorb(victim, event)) return;
        }
    }

    public static boolean isInForeignDome(Entity entity, LivingEntity owner) {
        Dome dome = domeContaining(entity);
        return dome != null && dome.entity != owner;
    }

    private static Dome domeContaining(Entity entity) {
        for (Guard guard : guards.values()) {
            if (guard instanceof Dome dome && dome.holds(entity)) return dome;
        }
        return null;
    }

    private static boolean isSpiritWorldTeleport(Ability ability) {
        return ability instanceof SpiritWorldTraversalAbility || ability instanceof TravelersDoorAbility || ability instanceof PlayerTeleportationAbility || ability instanceof TeleportationAuthorityAbility;
    }

    private static boolean isCurse(Ability ability) {
        if (ability instanceof CurseOfMisfortuneAbility) return true;
        for (String flag : ability.getInteractionFlags()) {
            if ("curse".equals(flag)) return true;
        }
        return false;
    }

    private static boolean holdsSword(LivingEntity entity) {
        return ArsenalOfDawnAbility.is(entity.getMainHandItem(), ArsenalOfDawnAbility.SWORDS);
    }

    private static void bar(LivingEntity entity, String key, Object... args) {
        AbilityUtil.sendActionBar(entity, Component.translatable(key, args).withColor(DAWN_TEXT));
    }

    private static Entity find(ServerLevel level, UUID uuid) {
        Entity entity = level.getEntity(uuid);
        return entity != null ? entity : level.getServer().getPlayerList().getPlayer(uuid);
    }

    private interface Guard {
        void tick(ServerLevel level, LivingEntity entity);
    }

    private static final class Wall implements Guard {
        private final LivingEntity entity;
        private final Vec3 forward;
        private final Vec3 right;
        private Vec3 lastPosition;
        private double strength = 1;

        private Wall(LivingEntity entity, Vec3 forward, Vec3 right) {
            this.entity = entity;
            this.forward = forward;
            this.right = right;
            this.lastPosition = entity.position();
        }

        @Override
        public void tick(ServerLevel level, LivingEntity entity) {
            Vec3 position = entity.position();
            strength -= position.subtract(lastPosition).horizontalDistance() * MOVE_DECAY_PER_BLOCK;
            lastPosition = position;
            if (strength <= 0) {
                if (instance != null) instance.cancel(level, entity);
                return;
            }
            drawWalls(level);
            trapAttackers(level);
        }

        private void weaken() {
            strength -= ABILITY_DECAY;
        }

        private boolean absorb(LivingEntity victim, LivingIncomingDamageEvent event) {
            if (entity == victim || entity.level() != victim.level() || !isBehind(victim.position())) return false;
            if (strength >= 1) event.setCanceled(true);
            else event.setAmount((float) (event.getAmount() * (1 - Math.max(0, strength))));
            return true;
        }

        private boolean isBehind(Vec3 point) {
            Axes axes = axes(point);
            return axes.forward <= 0 && axes.forward >= -BACK_DEPTH && Math.abs(axes.side) <= HALF_WIDTH && Math.abs(axes.y) <= WALL_HEIGHT;
        }

        private void drawWalls(ServerLevel level) {
            Vec3 origin = entity.position();
            for (int side = -1; side <= 1; side += 2) {
                for (double f = -BACK_DEPTH; f <= FRONT_RANGE; f += PARTICLE_SPACING) {
                    for (double y = 0; y <= WALL_HEIGHT; y += PARTICLE_SPACING) {
                        Vec3 pos = origin.add(forward.scale(f)).add(right.scale(side * HALF_WIDTH)).add(0, y, 0);
                        level.sendParticles(DAWN_DUST, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
                    }
                }
                Vec3 top = origin.add(right.scale(side * HALF_WIDTH)).add(0, WALL_HEIGHT, 0);
                level.sendParticles(ParticleTypes.END_ROD, top.x, top.y, top.z, 3, 0.2, 0.2, 0.2, 0.01);
            }
        }

        private void trapAttackers(ServerLevel level) {
            for (LivingEntity target : AbilityUtil.getNearbyEntities(entity, level, entity.position(), FRONT_RANGE + HALF_WIDTH)) {
                if (!AbilityUtil.mayDamage(entity, target)) continue;
                Axes axes = axes(target.position());
                if (axes.forward <= 0 || axes.forward > FRONT_RANGE || Math.abs(axes.side) > HALF_WIDTH + 1 || Math.abs(axes.y) > WALL_HEIGHT) continue;

                double clampedF = Mth.clamp(axes.forward, MIN_FRONT_DISTANCE, FRONT_RANGE);
                double clampedL = Mth.clamp(axes.side, -HALF_WIDTH + 0.5, HALF_WIDTH - 0.5);
                Vec3 pos = entity.position().add(forward.scale(clampedF)).add(right.scale(clampedL)).add(0, axes.y, 0);
                Vec3 look = entity.getEyePosition().subtract(target.getEyePosition());
                float yaw = (float) (Mth.atan2(look.z, look.x) * Mth.RAD_TO_DEG) - 90f;
                float pitch = (float) -(Mth.atan2(look.y, look.horizontalDistance()) * Mth.RAD_TO_DEG);

                if (target instanceof ServerPlayer player) {
                    player.connection.teleport(pos.x, pos.y, pos.z, yaw, pitch);
                    continue;
                }
                if (clampedF != axes.forward || clampedL != axes.side) target.teleportTo(pos.x, pos.y, pos.z);
                target.setYRot(yaw);
                target.setYHeadRot(yaw);
                target.setYBodyRot(yaw);
                target.setXRot(pitch);
                if (target instanceof Mob mob) mob.getLookControl().setLookAt(entity);
            }
        }

        private Axes axes(Vec3 point) {
            Vec3 offset = point.subtract(entity.position());
            return new Axes(offset.dot(forward), offset.dot(right), offset.y);
        }
    }

    private record Axes(double forward, double side, double y) {
    }

    private static final class Dome implements Guard {
        private final LivingEntity entity;
        private final Vec3 forward;
        private final Vec3 center;
        private final double radius;
        private final Map<UUID, Stay> stays = new HashMap<>();
        private boolean initialized;

        private Dome(LivingEntity entity, Vec3 forward, Vec3 center, double radius) {
            this.entity = entity;
            this.forward = forward;
            this.center = center;
            this.radius = radius;
        }

        @Override
        public void tick(ServerLevel level, LivingEntity entity) {
            if (breakIfOverlapping(level)) return;

            Set<UUID> seen = new HashSet<>();
            AABB box = new AABB(center, center).inflate(radius + DOME_SCAN_MARGIN);
            for (Entity scanned : level.getEntities((Entity) null, box, e -> e.isAlive() && !e.isSpectator())) {
                seen.add(scanned.getUUID());
                if (cross(level, scanned, isInside(scanned.position()))) return;
            }

            for (UUID uuid : List.copyOf(stays.keySet())) {
                if (seen.contains(uuid)) continue;
                Entity occupant = stays.get(uuid).inside ? find(level, uuid) : null;
                if (occupant == null || !occupant.isAlive() || occupant instanceof Projectile) {
                    stays.remove(uuid);
                    continue;
                }
                if (occupant.level() == level && isInside(occupant.position())) continue;
                if (cross(level, occupant, false)) return;
            }
            initialized = true;
        }

        private boolean breakIfOverlapping(ServerLevel level) {
            Dome other = overlapping();
            if (other == null) return false;
            shatter(level);
            if (other.entity.level() instanceof ServerLevel otherLevel) other.shatter(otherLevel);
            return true;
        }

        private Dome overlapping() {
            for (Guard guard : guards.values()) {
                if (guard == this || !(guard instanceof Dome other) || other.entity.level() != entity.level()) continue;
                double reach = radius + other.radius;
                if (other.center.distanceToSqr(center) < reach * reach) return other;
            }
            return null;
        }

        private void showBorder(ServerLevel level, ServerPlayer viewer) {
            int points = (int) (radius * BORDER_POINTS_PER_BLOCK);
            for (int i = 0; i < points; i++) {
                double angle = i * Math.PI * 2 / points;
                Vec3 pos = center.add(Math.cos(angle) * radius, 0.2, Math.sin(angle) * radius);
                level.sendParticles(viewer, DAWN_DUST, true, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
            }
        }

        private boolean holds(Entity entity) {
            Stay stay = stays.get(entity.getUUID());
            if (stay != null && stay.inside) return true;
            return this.entity.level() == entity.level() && isInside(entity.position());
        }

        private boolean countsAsOutside(Entity entity, boolean inside) {
            return initialized && inside && entity instanceof LivingEntity && entity.tickCount > NEW_ENTITY_TICKS;
        }

        private boolean cross(ServerLevel level, Entity entity, boolean inside) {
            Stay stay = stays.get(entity.getUUID());
            boolean wasInside = stay == null ? inside && !countsAsOutside(entity, inside) : stay.inside;
            if (wasInside == inside) {
                stays.put(entity.getUUID(), new Stay(inside, inside ? entity.position() : stay == null ? null : stay.last));
                return false;
            }
            if (entity instanceof Projectile) {
                entity.discard();
                stays.remove(entity.getUUID());
                return false;
            }
            if (breaksThrough(entity)) {
                shatter(level);
                return true;
            }
            stays.put(entity.getUUID(), new Stay(wasInside, stay == null ? null : stay.last));
            if (wasInside) returnInside(level, entity);
            else moveTo(level, entity, edge(entity.position(), radius + DOME_PUSH));
            return false;
        }

        private boolean breaksThrough(Entity entity) {
            return entity instanceof LivingEntity living && living != this.entity && BeyonderData.getSequence(living) < BeyonderData.getSequence(this.entity);
        }

        private void shatter(ServerLevel level) {
            if (instance == null || guards.get(entity.getUUID()) != this) return;
            instance.cancel(level, entity);
            level.playSound(null, BlockPos.containing(center), SoundEvents.GLASS_BREAK, entity.getSoundSource(), 2f, 0.6f);
            level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 1, center.z, 80, radius / 3, 2, radius / 3, 0.1);
            bar(entity, "ability.lotmcraft.protection.shattered");
        }

        private void returnInside(ServerLevel level, Entity entity) {
            Stay stay = stays.get(entity.getUUID());
            Vec3 last = stay == null ? null : stay.last;
            Vec3 target = last != null && isInside(last) ? last : edge(entity.position(), radius - DOME_PUSH);
            moveTo(level, entity, target);
        }

        private Vec3 edge(Vec3 from, double distance) {
            Vec3 offset = from.subtract(center);
            Vec3 direction = offset.lengthSqr() < 1.0E-4 ? forward : offset.normalize();
            return center.add(direction.scale(distance));
        }

        private void moveTo(ServerLevel level, Entity entity, Vec3 target) {
            if (entity instanceof ServerPlayer player) {
                if (player.level() != level) player.teleportTo(level, target.x, target.y, target.z, player.getYRot(), player.getXRot());
                else player.connection.teleport(target.x, target.y, target.z, player.getYRot(), player.getXRot());
            } else if (entity.level() == level) {
                entity.teleportTo(target.x, target.y, target.z);
            } else {
                return;
            }
            entity.setDeltaMovement(Vec3.ZERO);
            entity.hurtMarked = true;
            level.sendParticles(ParticleTypes.END_ROD, target.x, target.y + 1, target.z, 12, 0.4, 0.8, 0.4, 0.02);
        }

        private boolean isInside(Vec3 point) {
            return point.distanceToSqr(center) <= radius * radius;
        }
    }

    private record Stay(boolean inside, Vec3 last) {
    }
}
