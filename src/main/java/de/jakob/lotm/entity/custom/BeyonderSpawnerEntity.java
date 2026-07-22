package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * An invisible, non-rendered entity that monitors nearby players and spawns
 * a BeyonderNPCEntity when the configured conditions are met.
 *
 * Configurable options:
 *  - triggerRadius       : how close a player must be (blocks)
 *  - minSequence         : players at this sequence or lower (better) may trigger the spawn
 *  - pathway             : specific pathway, or null/empty for a random implemented pathway
 *  - sequenceMin/Max     : when a range is given a random sequence is picked from [sequenceMin, sequenceMax]
 *  - hasQuest / hasTrades: forwarded to BeyonderNPCEntity
 *  - spawnAnimation      : whether to play the mystical-ring-style spiral animation before spawning
 */
public class BeyonderSpawnerEntity extends Entity {

    private static final double DEFAULT_RADIUS   = 16.0;
    private static final int    DEFAULT_MIN_SEQ  = 9;

    private double triggerRadius = DEFAULT_RADIUS;

    private int minSequence = DEFAULT_MIN_SEQ;

    private String pathway = "";

    private int sequenceMin = 5;
    private int sequenceMax = -1;

    private boolean hasQuest  = false;
    private boolean hasTrades = false;
    private boolean spawnAnimation = true;

    private boolean triggered = false;

    private int checkCooldown = 0;
    private static final int CHECK_INTERVAL = 20;

    public BeyonderSpawnerEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvisible(true);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public boolean isPickable()   { return false; }
    @Override
    public boolean isPushable()   { return false; }
    @Override
    public boolean isInvisible()  { return true;  }
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) { return false; }

    @Override
    public void tick() {
        super.tick();

        if (triggered || level().isClientSide()) return;

        if (--checkCooldown > 0) return;
        checkCooldown = CHECK_INTERVAL;

        ServerLevel serverLevel = (ServerLevel) level();
        double radiusSq = triggerRadius * triggerRadius;

        List<ServerPlayer> nearbyPlayers = serverLevel.players().stream()
                .filter(p -> p.distanceToSqr(this) <= radiusSq)
                .toList();

        if (nearbyPlayers.isEmpty()) return;

        Optional<ServerPlayer> triggeringPlayer = nearbyPlayers.stream()
                .filter(p -> BeyonderData.getSequence(p) <= minSequence)
                .filter(p -> !p.isCreative() && !p.isSpectator())
                .findFirst();

        if (triggeringPlayer.isEmpty()) return;

        triggered = true;
        performSpawn(serverLevel, triggeringPlayer.get());
    }

    private void performSpawn(ServerLevel level, Player triggeringPlayer) {
        String resolvedPathway;
        if (pathway == null || pathway.isBlank()) {
            List<String> implemented = BeyonderData.implementedPathways;
            resolvedPathway = implemented.get(new Random().nextInt(implemented.size()));
        } else {
            resolvedPathway = pathway;
        }

        int resolvedSequence;
        if (sequenceMax < 0 || sequenceMax < sequenceMin) {
            resolvedSequence = sequenceMin;
        } else {
            resolvedSequence = sequenceMin + new Random().nextInt(sequenceMax - sequenceMin + 1);
        }

        double spawnX = this.getX();
        double spawnY = this.getY();
        double spawnZ = this.getZ();

        final String finalPathway   = resolvedPathway;
        final int    finalSequence  = resolvedSequence;

        if (spawnAnimation) {
            int colorInt  = BeyonderData.pathwayInfos.get(resolvedPathway).color();
            float red     = ((colorInt >> 16) & 0xFF) / 255.0f;
            float green   = ((colorInt >>  8) & 0xFF) / 255.0f;
            float blue    = ( colorInt        & 0xFF) / 255.0f;

            DustParticleOptions dust = new DustParticleOptions(new Vector3f(red, green, blue), 2f);

            ParticleUtil.createParticleSpirals(
                    level, dust,
                    this.position(),
                    1, 1.75, 2, 0.5, 2,
                    20 * 8, 8, 4
            );

            ServerScheduler.scheduleDelayed(20 * 8, () -> {
                spawnBeyonderNPC(level, spawnX, spawnY, spawnZ, finalPathway, finalSequence, triggeringPlayer.getUUID());
            });
        } else {
            spawnBeyonderNPC(level, spawnX, spawnY, spawnZ, finalPathway, finalSequence, triggeringPlayer.getUUID());
        }

        this.discard();
    }

    private void spawnBeyonderNPC(ServerLevel level,
                                   double x, double y, double z,
                                   String resolvedPathway, int resolvedSequence,
                                   UUID summoner) {
        BeyonderNPCEntity beyonder = new BeyonderNPCEntity(
                ModEntities.BEYONDER_NPC.get(),
                level,
                spawnAnimation,
                BeyonderNPCEntity.getRandomBeyonderSkin(),
                resolvedPathway,
                resolvedSequence,
                hasQuest,
                hasTrades
        );
        beyonder.getPersistentData().putUUID("lotm_beyonder_summoner", summoner);
        beyonder.setPos(x, y, z);
        level.addFreshEntity(beyonder);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        triggerRadius   = tag.getDouble("TriggerRadius");
        minSequence     = tag.getInt("MinSequence");
        pathway         = tag.getString("Pathway");
        sequenceMin     = tag.getInt("SequenceMin");
        sequenceMax     = tag.contains("SequenceMax") ? tag.getInt("SequenceMax") : -1;
        hasQuest        = tag.getBoolean("HasQuest");
        hasTrades       = tag.getBoolean("HasTrades");
        spawnAnimation  = tag.getBoolean("SpawnAnimation");
        triggered       = tag.getBoolean("Triggered");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("TriggerRadius",  triggerRadius);
        tag.putInt("MinSequence",       minSequence);
        tag.putString("Pathway",        pathway  != null ? pathway : "");
        tag.putInt("SequenceMin",       sequenceMin);
        tag.putInt("SequenceMax",       sequenceMax);
        tag.putBoolean("HasQuest",      hasQuest);
        tag.putBoolean("HasTrades",     hasTrades);
        tag.putBoolean("SpawnAnimation",spawnAnimation);
        tag.putBoolean("Triggered",     triggered);
    }

    public void setTriggerRadius(double radius)       { this.triggerRadius    = radius;    }
    public void setMinSequence(int seq)               { this.minSequence      = seq;       }
    public void setPathway(String pathway)            { this.pathway          = pathway;   }
    public void setSequenceMin(int min)               { this.sequenceMin      = min;       }
    public void setSequenceMax(int max)               { this.sequenceMax      = max;       }
    public void setHasQuest(boolean hasQuest)         { this.hasQuest         = hasQuest;  }
    public void setHasTrades(boolean hasTrades)       { this.hasTrades        = hasTrades; }
    public void setSpawnAnimation(boolean anim)       { this.spawnAnimation   = anim;      }
}