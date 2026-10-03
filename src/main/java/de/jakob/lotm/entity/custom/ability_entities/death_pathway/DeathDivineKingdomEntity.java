package de.jakob.lotm.entity.custom.ability_entities.death_pathway;

import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeathDivineKingdomEntity extends Entity {

    public static final int RADIUS = 120;
    public static final int DURATION_TICKS = 20 * 60 * 2; // 2 minutes

    private static final int BASE_COUNTDOWN_SECONDS = 45;
    private static final int COUNTDOWN_REDUCTION_PER_SEQ = 5;
    private static final float BASE_DEBUFF_MULTIPLIER = 0.70f;
    private static final float DEBUFF_SCALE_PER_SEQ = 0.05f;
    private static final int DURABILITY_DRAIN_PER_SECOND = 15;
    private static final String MODIFIER_KEY = "divine_kingdom_debuff";
    private static final DustParticleOptions VOID_DUST =
            new DustParticleOptions(new Vector3f(0.05f, 0.0f, 0.1f), 1.2f);

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(DeathDivineKingdomEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    // Per-entity countdown timers scoped to this entity instance
    private final Map<UUID, Integer> countdowns = new ConcurrentHashMap<>();

    private int lifetime = 0;
    private int casterSeq = 9;

    public DeathDivineKingdomEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public DeathDivineKingdomEntity(EntityType<?> entityType, Level level, LivingEntity caster) {
        this(entityType, level);
        this.entityData.set(OWNER_UUID, Optional.of(caster.getUUID()));
        this.casterSeq = BeyonderData.getSequence(caster);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_UUID, Optional.empty());
    }

    public void setOwnerUUID(UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    public LivingEntity getOwner() {
        if (!(level() instanceof ServerLevel serverLevel)) return null;
        UUID uuid = getOwnerUUID();
        if (uuid == null) return null;
        Entity e = serverLevel.getEntity(uuid);
        return e instanceof LivingEntity living ? living : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel serverLevel)) return;

        lifetime++;
        if (lifetime >= DURATION_TICKS) {
            expire(serverLevel);
            return;
        }

        LivingEntity caster = getOwner();
        if (caster == null || !caster.isAlive()) {
            expire(serverLevel);
            return;
        }

        Vec3 center = position();

        // Only Flaring Sun, Pure White Light, Sword of Justice, Divine Kingdom Manifestation can cancel this domain
        Location loc = new Location(center, serverLevel);
        if (InteractionHandler.isInteractionPossibleStrictlyHigher(loc, "purification_holy", casterSeq, -1)) {
            expire(serverLevel);
            return;
        }

        tickVisuals(serverLevel, center);
        tickProjectileDestruction(serverLevel, center, caster);
        tickEntityEffects(serverLevel, center, caster);
    }

    private void tickVisuals(ServerLevel level, Vec3 center) {
        // Soul fire flames — white flames drifting upward throughout the domain, every tick
        spawnInteriorParticles(level, center, ParticleTypes.SOUL_FIRE_FLAME, RADIUS * 0.85, 6, 0.05);

        // Soul particles rising from the ground layer
        if (lifetime % 2 == 0) {
            spawnGroundParticles(level, center, ParticleTypes.SOUL, RADIUS * 0.85, 4);
        }

        // Ash/smoke wisps drifting slowly
        if (lifetime % 3 == 0) {
            spawnInteriorParticles(level, center, ParticleTypes.LARGE_SMOKE, RADIUS * 0.75, 3, 0.005);
        }

        // Void dust — deep purple motes swirling inside
        if (lifetime % 4 == 0) {
            spawnInteriorParticles(level, center, VOID_DUST, RADIUS * 0.8, 5, 0.0);
        }

        // Ash particles drifting in mid-air
        if (lifetime % 5 == 0) {
            spawnInteriorParticles(level, center, ParticleTypes.ASH, RADIUS * 0.9, 8, 0.01);
        }

        // End rod sparks near the boundary surface
        if (lifetime % 10 == 0) {
            ParticleUtil.spawnSphereParticles(level, ParticleTypes.END_ROD, center, RADIUS * 0.95, 30);
        }
    }

    /** Scatter particles at random positions inside a sphere of the given radius. */
    private static void spawnInteriorParticles(ServerLevel level, Vec3 center,
                                               net.minecraft.core.particles.ParticleOptions type,
                                               double radius, int count, double speed) {
        net.minecraft.util.RandomSource rng = level.random;
        for (int i = 0; i < count; i++) {
            // Uniform random point inside sphere
            double u = rng.nextDouble();
            double cosTheta = rng.nextDouble() * 2 - 1;
            double sinTheta = Math.sqrt(1 - cosTheta * cosTheta);
            double phi = rng.nextDouble() * 2 * Math.PI;
            double r = radius * Math.cbrt(u);

            double x = center.x + r * sinTheta * Math.cos(phi);
            double y = center.y + r * cosTheta;
            double z = center.z + r * sinTheta * Math.sin(phi);

            level.sendParticles(type, x, y, z, 1, 0, speed > 0 ? speed : 0, 0, speed);
        }
    }

    /** Scatter particles at ground level (y = center.y) inside a circle. */
    private static void spawnGroundParticles(ServerLevel level, Vec3 center,
                                             net.minecraft.core.particles.ParticleOptions type,
                                             double radius, int count) {
        net.minecraft.util.RandomSource rng = level.random;
        for (int i = 0; i < count; i++) {
            double angle = rng.nextDouble() * 2 * Math.PI;
            double r = radius * Math.sqrt(rng.nextDouble());
            double x = center.x + r * Math.cos(angle);
            double z = center.z + r * Math.sin(angle);
            double y = center.y + (rng.nextDouble() * 2 - 1); // slight vertical scatter
            level.sendParticles(type, x, y, z, 1, 0, 0.05, 0, 0.02);
        }
    }

    private void tickProjectileDestruction(ServerLevel level, Vec3 center, LivingEntity caster) {
        AABB domainBox = AABB.ofSize(center, RADIUS * 2, RADIUS * 2, RADIUS * 2);
        List<Projectile> projectiles = level.getEntitiesOfClass(Projectile.class, domainBox);
        for (Projectile projectile : projectiles) {
            var shooter = projectile.getOwner();
            if (shooter instanceof LivingEntity shooterLiving) {
                if (caster != null && (shooterLiving == caster || AllyUtil.areAllies(caster, shooterLiving))) continue;
            }
            level.sendParticles(ParticleTypes.SMOKE,
                    projectile.getX(), projectile.getY(), projectile.getZ(),
                    3, 0.1, 0.1, 0.1, 0.01);
            projectile.discard();
        }
    }

    private void tickEntityEffects(ServerLevel level, Vec3 center, LivingEntity caster) {
        List<LivingEntity> entitiesInRange = AbilityUtil.getNearbyEntities(caster, level, center, RADIUS);
        Set<UUID> inRangeIds = new HashSet<>();

        entitiesInRange.forEach(target -> {
            if (caster != null && AllyUtil.areAllies(caster, target)) return;

            int targetSeq = BeyonderData.getSequence(target);
            int seqDiff = targetSeq - casterSeq; // positive = target is weaker

            inRangeIds.add(target.getUUID());

            // Instant kill for targets 2+ sequences weaker
            if (seqDiff >= 2) {
                downEntity(target, caster, level);
                countdowns.remove(target.getUUID());
                return;
            }

            // Damage debuff
            float debuffMultiplier = BASE_DEBUFF_MULTIPLIER - (seqDiff * DEBUFF_SCALE_PER_SEQ);
            debuffMultiplier = Math.max(0.0f, Math.min(1.0f, debuffMultiplier));
            BeyonderData.addModifier(target, MODIFIER_KEY, debuffMultiplier);

            // Durability drain every second
            if (lifetime % 20 == 0) {
                drainDurability(target, level);
            }

            // Countdown — tick every second
            if (lifetime % 20 == 0) {
                int current = countdowns.computeIfAbsent(target.getUUID(), k -> {
                    int base = BASE_COUNTDOWN_SECONDS - (seqDiff * COUNTDOWN_REDUCTION_PER_SEQ);
                    return Math.max(1, base);
                });

                int newCount = current - 1;
                countdowns.put(target.getUUID(), newCount);

                if (target instanceof ServerPlayer targetPlayer) {
                    Component msg = newCount > 0
                            ? Component.literal("☠ Divine Kingdom: " + newCount + "s ☠").withColor(0xFF334f23)
                            : Component.literal("☠ Divine Kingdom: 0s ☠").withColor(0xFF334f23);
                    AbilityUtil.sendActionBar(targetPlayer, msg);
                }

                if (newCount <= 0) {
                    downEntity(target, caster, level);
                    countdowns.remove(target.getUUID());
                }
            }
        });

        // Remove debuff for entities that left the domain this tick
        countdowns.keySet().removeIf(uuid -> {
            if (inRangeIds.contains(uuid)) return false;
            Entity e = level.getEntity(uuid);
            if (e instanceof LivingEntity living) BeyonderData.removeModifier(living, MODIFIER_KEY);
            return true;
        });
    }

    private void expire(ServerLevel level) {
        // Clean up debuffs for all remaining tracked entities
        countdowns.keySet().forEach(uuid -> {
            Entity e = level.getEntity(uuid);
            if (e instanceof LivingEntity living) BeyonderData.removeModifier(living, MODIFIER_KEY);
        });
        countdowns.clear();

        this.discard();
    }

    // -------------------------------------------------------------------------
    // Durability drain
    // -------------------------------------------------------------------------

    private static void drainDurability(LivingEntity target, ServerLevel level) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item = target.getItemBySlot(slot);
            if (!item.isEmpty() && item.isDamageableItem()) {
                item.hurtAndBreak(DURABILITY_DRAIN_PER_SECOND, target, slot);
            }
        }
        if (target instanceof ServerPlayer player) {
            ItemStack offhand = player.getOffhandItem();
            if (!offhand.isEmpty() && offhand.isDamageableItem()) {
                offhand.hurtAndBreak(DURABILITY_DRAIN_PER_SECOND, player, EquipmentSlot.OFFHAND);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Kill on countdown expiry
    // -------------------------------------------------------------------------

    private static void downEntity(LivingEntity target, LivingEntity caster, ServerLevel level) {
        ModDamageTypes.trueDamage(target, Float.MAX_VALUE, level, caster);
        level.playSound(null, target.blockPosition(),
                SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.5f, 0.6f);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, target.position().add(0, 1, 0), 2, 60);
    }

    // -------------------------------------------------------------------------
    // NBT — no save needed, domain is transient
    // -------------------------------------------------------------------------

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("owner")) setOwnerUUID(tag.getUUID("owner"));
        casterSeq = tag.getInt("caster_seq");
        lifetime = tag.getInt("lifetime");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        UUID uuid = getOwnerUUID();
        if (uuid != null) tag.putUUID("owner", uuid);
        tag.putInt("caster_seq", casterSeq);
        tag.putInt("lifetime", lifetime);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) { return true; }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) { return true; }

    @Override
    public boolean isPickable() { return false; }
}
