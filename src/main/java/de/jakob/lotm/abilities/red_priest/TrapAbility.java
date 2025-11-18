package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.SyncExplodedTrapPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ClientScheduler;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrapAbility extends AbilityItem {
    public TrapAbility(Properties properties) {
        super(properties, 1);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "red_priest", 9
        ));
    }

    @Override
    protected float getSpiritualityCost() {
        return 10;
    }

    DustParticleOptions dustOptions = new DustParticleOptions(
            new Vector3f(237 / 255f, 50 / 255f, 50 / 255f),
            1.35f
    );

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        final int duration = 20 * 30;
        Vec3 pos = entity.position();
        String trapKey = entity.getUUID() + "_" + pos.x + "_" + pos.y + "_" + pos.z;
        UUID trapId = UUID.nameUUIDFromBytes(trapKey.getBytes());

        AtomicBoolean hasExploded = new AtomicBoolean(false);

        if(!level.isClientSide()) {
            ServerScheduler.scheduleForDuration(0, 1, duration, () -> {
                if(hasExploded.get()) {
                    return;
                }

                if(!AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, pos, 1.35f).isEmpty()) {
                    hasExploded.set(true);

                    // Send packet to all nearby clients to stop particles
                    if(entity instanceof ServerPlayer player) {
                        PacketHandler.sendToPlayer(player, new SyncExplodedTrapPacket(trapId));
                    }

                    AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 3, DamageLookup.lookupDamage(9, .95) * multiplier(entity), pos, true, false, true, 0);

                    if(BeyonderData.isGriefingEnabled(entity)) {
                        level.explode(entity, pos.x, pos.y, pos.z, 4f, true, Level.ExplosionInteraction.MOB);
                    }
                    else {
                        level.explode(entity, pos.x, pos.y, pos.z, 4f, false, Level.ExplosionInteraction.NONE);
                    }
                }
            }, (ServerLevel) level);
        }
        else {
            ClientScheduler.scheduleForDuration(0, 1, duration, () -> {
                if(hasExploded.get() || ClientTrapManager.hasTrapExploded(trapId))
                    return;
                ParticleUtil.spawnCircleParticles((ClientLevel) level, dustOptions, pos, 1.6f, 22);
            }, (ClientLevel) level);

            // Clean up after duration
            ClientScheduler.scheduleDelayed(duration + 10, () -> {
                ClientTrapManager.clearTrapState(trapId);
            }, (ClientLevel) level);
        }
    }

    public class ClientTrapManager {
        private static final Map<UUID, Boolean> explodedTraps = new HashMap<>();

        public static void setTrapExploded(UUID entityId) {
            explodedTraps.put(entityId, true);
        }

        public static boolean hasTrapExploded(UUID entityId) {
            return explodedTraps.getOrDefault(entityId, false);
        }

        public static void clearTrapState(UUID entityId) {
            explodedTraps.remove(entityId);
        }
    }
}
