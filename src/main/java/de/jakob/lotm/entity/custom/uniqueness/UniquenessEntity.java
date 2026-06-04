package de.jakob.lotm.entity.custom.uniqueness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.UniquenessComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.playerMap.Characteristic;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniquenessEntity extends Entity {

    private static final EntityDataAccessor<String> PATHWAY =
            SynchedEntityData.defineId(UniquenessEntity.class, EntityDataSerializers.STRING);

    private static final float UNIQUENESS_ANGEL_DAMAGE = 50.0f;


    private static final int MAX_RENDER_DISTANCE_BLOCKS = 64;

    public static final Map<String, Integer> ACTIVE_ENTITIES = new HashMap<>();

    private int ticksExisted = 0;

    public UniquenessEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public UniquenessEntity(Level level, Vec3 position, String pathway) {
        this(ModEntities.UNIQUENESS_ENTITY.get(), level);
        this.setPos(position.x, position.y, position.z);
        this.setPathway(pathway);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(PATHWAY, "");
    }

    public String getPathway() {
        return entityData.get(PATHWAY);
    }

    public void setPathway(String pathway) {
        entityData.set(PATHWAY, pathway);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            ticksExisted++;
            return;
        }

        if(!ACTIVE_ENTITIES.containsValue(this.getId()) || getPathway().isEmpty() || !ACTIVE_ENTITIES.containsKey(getPathway())) {
            ACTIVE_ENTITIES.remove(getPathway());
            this.discard();
            return;
        }

        ticksExisted++;
        String pathway = getPathway();
        if (pathway.isEmpty()) return;

        ParticleUtil.spawnSphereParticles((ServerLevel) level(), new DustParticleOptions(getPathwayColor(), 1), position().add(0, .5, 0), 2.3, 50);

        // Check nearby entities for hitbox interactions
        AABB hitbox = this.getBoundingBox().inflate(0.5);
        List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class, hitbox);

        for (LivingEntity entity : nearby) {
            handleEntityContact(entity, pathway);
        }
    }

    private Vector3f getPathwayColor() {
        int color = BeyonderData.pathwayInfos.containsKey(getPathway())
                ? BeyonderData.pathwayInfos.get(getPathway()).color()
                : 0xFFFFFF;

        return new Vector3f(
                ((color >> 16) & 0xFF) / 255f,
                ((color >> 8) & 0xFF) / 255f,
                (color & 0xFF) / 255f
        );
    }

    private void handleEntityContact(LivingEntity entity, String pathway) {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        int seq = BeyonderData.getCharList(entity).stream()
                .filter(c -> c.pathway().equalsIgnoreCase(pathway))
                .mapToInt(Characteristic::sequence)
                .min()
                .orElse(-1);
        boolean hasPathway = BeyonderData.getCharList(entity).stream().anyMatch(c -> c.pathway().equalsIgnoreCase(pathway));
        LOTMCraft.LOGGER.info("Entity {} matches, {}: {}", hasPathway, pathway, seq);
        // Sequence 1 of the matching pathway picks up the uniqueness
        if (seq == 1 && hasPathway && entity instanceof Player player) {
            pickUp(player, pathway, serverLevel);
            return;
        }

        // Others are harmed
        if (seq <= 2 && BeyonderData.isBeyonder(entity)) {
            // Angels (seq <= 2) take massive damage
            entity.hurt(ModDamageTypes.source(level(), ModDamageTypes.BEYONDER_GENERIC), UNIQUENESS_ANGEL_DAMAGE);
        } else if (BeyonderData.isBeyonder(entity)) {
            // Seq > 2 die instantly
            entity.hurt(ModDamageTypes.source(level(), ModDamageTypes.BEYONDER_GENERIC), Float.MAX_VALUE);
        }
    }

    private void pickUp(Player player, String pathway, ServerLevel serverLevel) {
        UniquenessComponent component = player.getData(ModAttachments.UNIQUENESS_COMPONENT);
        component.setHasUniqueness(true);
        component.setUniquenessPathway(pathway);
        BeyonderData.playerMap.setUniqueness(player, pathway);

        int color = BeyonderData.pathwayInfos.containsKey(pathway)
                ? BeyonderData.pathwayInfos.get(pathway).color()
                : 0xFFFFFF;

        serverLevel.players().forEach(p ->
                p.displayClientMessage(
                        Component.literal(player.getName().getString())
                                .append(Component.translatable("lotm.uniqueness.picked_up"))
                                .withColor(color),
                        false
                )
        );

        player.playSound(SoundEvents.ITEM_PICKUP);

        // Sync uniqueness component to client
        if (player instanceof ServerPlayer serverPlayer) {
            de.jakob.lotm.network.PacketHandler.syncUniquenessToPlayer(serverPlayer);
        }

        // Remove from active map and discard entity
        ACTIVE_ENTITIES.remove(pathway);
        this.discard();
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (!level().isClientSide) {
            String pathway = getPathway();
            if (!pathway.isEmpty()) {
                ACTIVE_ENTITIES.remove(pathway);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setPathway(tag.getString("Pathway"));
        if (!level().isClientSide && !getPathway().isEmpty()) {
            ACTIVE_ENTITIES.put(getPathway(), this.getId());
        }
    }

    public static boolean anyPlayerHoldsUniqueness(ServerLevel level, String pathway) {
        for (ServerPlayer player : level.players()) {
            UniquenessComponent comp = player.getData(ModAttachments.UNIQUENESS_COMPONENT);
            if (comp.hasUniqueness() && pathway.equalsIgnoreCase(comp.getUniquenessPathway())) {
                return true;
            }
        }

        if(BeyonderData.playerMap.anyPlayerHoldsUniqueness(pathway)) {
            return true;
        }

        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("Pathway", getPathway());
    }

    /**
     * Spawns a UniquenessEntity in the world at the given position for the given pathway.
     * Only spawns if no entity for this pathway already exists.
     */
    public static boolean trySpawn(ServerLevel level, Vec3 position, String pathway) {
        if (ACTIVE_ENTITIES.containsKey(pathway)) {
            // Check that entity is still valid
            Entity existing = level.getEntity(ACTIVE_ENTITIES.get(pathway));
            if (existing != null && !existing.isRemoved()) {
                return false;
            }
            ACTIVE_ENTITIES.remove(pathway);
        }

        UniquenessEntity entity = new UniquenessEntity(level, position, pathway);
        level.addFreshEntity(entity);
        ACTIVE_ENTITIES.put(pathway, entity.getId());

        // Broadcast to online angels (seq <= 2) of this pathway
        int color = BeyonderData.pathwayInfos.containsKey(pathway)
                ? BeyonderData.pathwayInfos.get(pathway).color()
                : 0xFFFFFF;

        for (ServerPlayer player : level.players()) {
            boolean hasPath = BeyonderData.getCharList(player).stream().anyMatch(c -> c.pathway().equalsIgnoreCase(pathway) && c.sequence() >= 2);
            int pSeq = BeyonderData.getSequence(player);
            if (hasPath && pSeq <= 2) {
                player.displayClientMessage(
                        Component.translatable("lotm.uniqueness.spawned").withColor(color),
                        false
                );
                player.playSound(SoundEvents.WITHER_SPAWN, 0.5f, 1.5f);
            }
        }

        return true;
    }

    /**
     * Returns true if an active uniqueness entity for this pathway exists in the given level.
     */
    public static boolean existsInWorld(ServerLevel level, String pathway) {
        if (!ACTIVE_ENTITIES.containsKey(pathway)) return false;
        Entity entity = level.getEntity(ACTIVE_ENTITIES.get(pathway));
        return entity != null && !entity.isRemoved();
    }

    public static boolean anySeq0Presence(ServerLevel level) {
        return anySeq0Presence(level, null);
    }

    public static boolean anySeq0Presence(ServerLevel level, Entity ignore) {
        if (level == null || level.getServer() == null) return false;

        String ignorePathway = null;
        boolean ignoreIsSeq0 = false;
        Map<String, Integer> ignoreStoredCounts = new HashMap<>();
        if (ignore instanceof LivingEntity living && BeyonderData.isBeyonder(living)) {
            if (BeyonderData.getSequence(living) == 0) {
                ignoreIsSeq0 = true;
                ignorePathway = BeyonderData.getPathway(living);
            }
        }
        if (ignore instanceof ServerPlayer player) {
            ignoreStoredCounts = countSeq0StoredSouls(player);
        }

        for (String pathway : BeyonderData.implementedPathways) {
            int count = BeyonderData.countTotalSequence(level, pathway, 0);
            if (ignoreIsSeq0 && pathway.equalsIgnoreCase(ignorePathway)) {
                count = Math.max(0, count - 1);
            }
            int ignoreStored = ignoreStoredCounts.getOrDefault(pathway, 0);
            if (ignoreStored > 0) {
                count = Math.max(0, count - ignoreStored);
            }
            if (count > 0) {
                return true;
            }
        }

        for (ServerLevel world : level.getServer().getAllLevels()) {
            for (Entity entity : world.getAllEntities()) {
                if (entity == ignore) continue;
                if (!(entity instanceof LivingEntity living)) continue;
                if (!BeyonderData.isBeyonder(living)) continue;
                if (BeyonderData.getSequence(living) == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private static Map<String, Integer> countSeq0StoredSouls(ServerPlayer player) {
        Map<String, Integer> counts = new HashMap<>();
        CompoundTag data = player.getPersistentData();
        if (!data.contains("InternalUnderworldSouls", Tag.TAG_LIST)) {
            return counts;
        }

        ListTag list = data.getList("InternalUnderworldSouls", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag soul = list.getCompound(i);
            if (!soul.contains("Sequence", Tag.TAG_INT)) continue;
            if (soul.getInt("Sequence") != 0) continue;

            String pathway = soul.getString("Pathway");
            if (pathway == null || pathway.isEmpty() || "none".equals(pathway)) continue;

            counts.put(pathway, counts.getOrDefault(pathway, 0) + 1);
        }

        return counts;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distSq) {
        return distSq < MAX_RENDER_DISTANCE_BLOCKS * MAX_RENDER_DISTANCE_BLOCKS;
    }

    public int getTicksExisted() {
        return ticksExisted;
    }
}
