package de.jakob.lotm.entity.custom.uniqueness;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.UniquenessComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
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
            AABB hitbox = this.getBoundingBox().inflate(2);
            List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class, hitbox);

            for(LivingEntity entity : nearby) {
                if(BeyonderData.isBeyonder(entity) && BeyonderData.getSequence(entity) <= 3) {
                    spawnUniqueness(entity);
                    return;
                }
            }

            return;
        }

        ticksExisted++;
        String pathway = getPathway();
        if (pathway.isEmpty()) return;

        ParticleUtil.spawnParticles((ServerLevel) level(), new DustParticleOptions(getPathwayColor(), 1), position(), 100, 1, .1, 1, 0);

        // Check nearby entities for hitbox interactions
        AABB hitbox = this.getBoundingBox().inflate(0.5);
        List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class, hitbox);

        for (LivingEntity entity : nearby) {
            handleEntityContact(entity, pathway);
        }
    }

    private void spawnUniqueness(LivingEntity entity) {
        String pathway = BeyonderData.getPathway(entity);
        if (pathway.isEmpty()) return;
        if (UniquenessEntity.existsInWorld((ServerLevel) level(), pathway)) {
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.UNIQUENESS_SPAWN, position().x, position().y, position().z, (ServerLevel) level());
        this.discard();
        ServerScheduler.scheduleDelayed(20 * 3, () -> {
            if (level() instanceof ServerLevel serverLevel) {
                trySpawn(serverLevel, this.position(), pathway);
            }
        });
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

        int seq = BeyonderData.getSequence(entity);
        String entityPathway = BeyonderData.getPathway(entity);

        // Sequence 1 of the matching pathway picks up the uniqueness
        if (seq == 1 && entityPathway.equalsIgnoreCase(pathway) && entity instanceof Player player) {
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

        if (UniquenessEntity.existsInWorld(level, pathway)) return false;

        if (UniquenessEntity.anyPlayerHoldsUniqueness(level, pathway)) return false;

        if (BeyonderData.playerMap != null && BeyonderData.playerMap.count(pathway, 0) > 0) return false;

        UniquenessEntity entity = new UniquenessEntity(level, position, pathway);
        level.addFreshEntity(entity);
        ACTIVE_ENTITIES.put(pathway, entity.getId());

        // Broadcast to online angels (seq <= 2) of this pathway
        int color = BeyonderData.pathwayInfos.containsKey(pathway)
                ? BeyonderData.pathwayInfos.get(pathway).color()
                : 0xFFFFFF;

        for (ServerPlayer player : level.players()) {
            String pPathway = BeyonderData.getPathway(player);
            int pSeq = BeyonderData.getSequence(player);
            if (pPathway.equalsIgnoreCase(pathway) && pSeq <= 2) {
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
