package de.jakob.lotm.entity.custom.ability_entities.justiciar_pathway;

import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class AncientCourtEntity extends Entity {

    public static final List<AncientCourtEntity> ACTIVE_COURTS = new CopyOnWriteArrayList<>();
    public static final int RADIUS = 240;

    private static final EntityDataAccessor<Integer> DURATION =
            SynchedEntityData.defineId(AncientCourtEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(AncientCourtEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> GRIEFING =
            SynchedEntityData.defineId(AncientCourtEntity.class, EntityDataSerializers.BOOLEAN);

    private int lifetime = 0;
    private int swapTimer = 0;
    private int effectTimer = 100;
    public CourtProhibitionType currentProhibition = null;
    private final Random random = new Random();

    public AncientCourtEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        setDuration(20 * 60 * 2);
    }

    public AncientCourtEntity(EntityType<?> entityType, Level level, int ticks, UUID casterUUID, boolean griefing) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        setDuration(ticks);
        setCasterUUID(casterUUID);
        setGriefing(griefing);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!level().isClientSide()) {
            ACTIVE_COURTS.add(this);
        }
    }

    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        ACTIVE_COURTS.remove(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        ServerLevel serverLevel = (ServerLevel) level();

        lifetime++;
        if (lifetime >= getDuration()) {
            onCourtEnd(serverLevel);
            discard();
            return;
        }

        // Initialize prohibition on first tick
        if (currentProhibition == null) {
            currentProhibition = pickRandom(null);
            broadcastProhibition(serverLevel);
        }

        // Swap prohibition every 200 ticks (10 seconds)
        swapTimer++;
        if (swapTimer >= 200) {
            swapTimer = 0;
            CourtProhibitionType old = currentProhibition;
            onProhibitionEnd(serverLevel, old);
            currentProhibition = pickRandom(old);
            broadcastProhibition(serverLevel);
        }

        // Re-fire visual effect every 100 ticks (5 seconds)
        effectTimer++;
        if (effectTimer >= 100) {
            effectTimer = 0;
            EffectManager.playEffect(EffectManager.Effect.ANCIENT_COURT, getX(), getY(), getZ(), serverLevel);
        }

        applyTickProhibitions(serverLevel);
    }

    private List<LivingEntity> getEntitiesInCourt(ServerLevel serverLevel) {
        return AbilityUtil.getNearbyEntities(getCasterEntity(), serverLevel, position(), RADIUS);
    }

    private void applyTickProhibitions(ServerLevel serverLevel) {
        if (currentProhibition == null) return;

        switch (currentProhibition) {
            case MOVING -> getEntitiesInCourt(serverLevel).forEach(e -> {
                e.setDeltaMovement(Vec3.ZERO);
                e.hurtMarked = true;
            });
            case RUNNING -> getEntitiesInCourt(serverLevel).forEach(e -> {
                if (e instanceof Player p) p.setSprinting(false);
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 5, false, false));
            });
            case BREATHING -> getEntitiesInCourt(serverLevel).forEach(e -> {
                if (e instanceof Player p) p.setAirSupply(0);
            });
            case REGENERATION -> getEntitiesInCourt(serverLevel).forEach(e ->
                    e.removeEffect(MobEffects.REGENERATION));
            case HOSTILE_MOBS -> getEntitiesInCourt(serverLevel).forEach(e -> {
                if (e instanceof Monster) pushOut(e);
            });
            case MOBS -> getEntitiesInCourt(serverLevel).forEach(e -> {
                if (!(e instanceof Player)) pushOut(e);
            });
            case UNDEAD -> getEntitiesInCourt(serverLevel).forEach(e -> {
                if (e.getType().is(EntityTypeTags.UNDEAD)) pushOut(e);
            });
            case ESCAPING -> {
                AABB searchBox = AABB.ofSize(position(), (RADIUS + 10) * 2.0, (RADIUS + 10) * 2.0, (RADIUS + 10) * 2.0);
                serverLevel.getEntitiesOfClass(LivingEntity.class, searchBox, e -> {
                    if (getCasterUUID() != null && e.getUUID().equals(getCasterUUID())) return false;
                    double dist = e.position().distanceTo(position());
                    return dist >= RADIUS - 5 && dist <= RADIUS + 5;
                }).forEach(e -> {
                    Vec3 direction = position().subtract(e.position()).normalize();
                    if (direction.lengthSqr() < 0.001) direction = new Vec3(1, 0, 0);
                    e.setDeltaMovement(direction.scale(1.5));
                    e.hurtMarked = true;
                });
            }
            default -> {}
        }
    }

    private void pushOut(LivingEntity entity) {
        Vec3 direction = entity.position().subtract(position()).normalize();
        if (direction.lengthSqr() < 0.001) direction = new Vec3(1, 0, 0);
        entity.setDeltaMovement(direction.scale(1.5));
        entity.hurtMarked = true;
    }

    private void onProhibitionEnd(ServerLevel serverLevel, CourtProhibitionType type) {
        if (type == CourtProhibitionType.BREATHING) {
            getEntitiesInCourt(serverLevel).forEach(e -> {
                if (e instanceof Player p) p.setAirSupply(p.getMaxAirSupply());
            });
        }
    }

    private void onCourtEnd(ServerLevel serverLevel) {
        if (currentProhibition != null) {
            onProhibitionEnd(serverLevel, currentProhibition);
        }
    }

    private CourtProhibitionType pickRandom(CourtProhibitionType exclude) {
        CourtProhibitionType[] values = CourtProhibitionType.values();
        CourtProhibitionType picked;
        do {
            picked = values[random.nextInt(values.length)];
        } while (picked == exclude);
        return picked;
    }

    private void broadcastProhibition(ServerLevel serverLevel) {
        Component msg = Component.literal(
                "[Ancient Court of Judgment] " + currentProhibition.displayName + " is Prohibited here.")
                .withStyle(ChatFormatting.GOLD);
        serverLevel.getServer().getPlayerList().getPlayers().forEach(p -> {
            if (p.level().equals(serverLevel) && p.distanceTo(this) <= RADIUS) {
                p.sendSystemMessage(msg);
            }
        });
    }

    // ── Court membership check ────────────────────────────────────────────────

    public boolean isEntityInCourt(LivingEntity entity) {
        if (getCasterUUID() != null && entity.getUUID().equals(getCasterUUID())) return false;
        if (!(entity.level() instanceof ServerLevel)) return false;
        if (!entity.level().equals(level())) return false;
        if (!isAlive()) return false;
        return entity.position().distanceTo(position()) <= RADIUS;
    }

    // ── Entity boilerplate ────────────────────────────────────────────────────

    @Override
    public boolean shouldRender(double x, double y, double z) { return true; }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) { return true; }

    public void setDuration(int duration) { entityData.set(DURATION, duration); }
    public int getDuration() { return entityData.get(DURATION); }

    public void setCasterUUID(UUID uuid) { entityData.set(OWNER, Optional.ofNullable(uuid)); }
    public UUID getCasterUUID() { return entityData.get(OWNER).orElse(null); }

    public void setGriefing(boolean g) { entityData.set(GRIEFING, g); }
    public boolean getGriefing() { return entityData.get(GRIEFING); }

    public LivingEntity getCasterEntity() {
        if (level().isClientSide()) return null;
        UUID uuid = getCasterUUID();
        if (uuid == null) return null;
        Entity e = ((ServerLevel) level()).getEntity(uuid);
        return e instanceof LivingEntity le ? le : null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DURATION, 20 * 60 * 2);
        builder.define(OWNER, Optional.empty());
        builder.define(GRIEFING, true);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setDuration(tag.getInt("duration"));
        setGriefing(tag.getBoolean("griefing"));
        if (tag.hasUUID("owner")) setCasterUUID(tag.getUUID("owner"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("duration", getDuration());
        tag.putBoolean("griefing", getGriefing());
        if (getCasterUUID() != null) tag.putUUID("owner", getCasterUUID());
    }

    // ── Prohibition type enum ─────────────────────────────────────────────────

    public enum CourtProhibitionType {
        TELEPORTING("Teleporting"),
        PROJECTILES("Projectiles"),
        SLEEPING("Sleeping"),
        MOVING("Moving"),
        RUNNING("Running"),
        EATING("Eating"),
        DRINKING("Drinking"),
        BUILDING("Building"),
        DESTRUCTION("Destruction"),
        EXPLOSIONS("Explosions"),
        FALL_DAMAGE("Fall Damage"),
        ESCAPING("Escaping"),
        SPEAKING("Speaking"),
        SWAPPING_ABILITIES("Swapping Abilities"),
        HEALING("Healing"),
        REGENERATION("Regeneration"),
        POSITIVE_EFFECTS("Positive Effects"),
        NEGATIVE_EFFECTS("Negative Effects"),
        HOSTILE_MOBS("Hostile Mobs"),
        MOBS("Mobs"),
        UNDEAD("Undead Creatures"),
        INTERACTION("Interaction"),
        BLINKING("Blinking"),
        CONCEALMENT("Concealment"),
        SEALING("Sealing"),
        BREATHING("Breathing");

        public final String displayName;

        CourtProhibitionType(String name) {
            this.displayName = name;
        }
    }
}
