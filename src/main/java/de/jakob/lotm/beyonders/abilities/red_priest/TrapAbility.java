package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.CancelPhotonBlockEffectPacket;
import de.jakob.lotm.network.packets.toClient.CancelPhotonEntityEffectPacket;
import de.jakob.lotm.network.packets.toClient.PlayCancellablePhotonBlockEffectPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ClientScheduler;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
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

public class TrapAbility extends Ability {
    public TrapAbility(String id) {
        super(id, 6, "explosion");
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

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        final int duration = 20 * 30;
        Vec3 pos = entity.position();

        AtomicBoolean hasExploded = new AtomicBoolean(false);

        UUID effectUUID = UUID.randomUUID();

        BlockPos blockPos = BlockPos.containing(pos);

        double offsetX = pos.x - (blockPos.getX() + 0.5);
        double offsetY = pos.y - (blockPos.getY() + 0.5);
        double offsetZ = pos.z - (blockPos.getZ() + 0.5);


        PacketHandler.sendToNearbyPlayers(
                new PlayCancellablePhotonBlockEffectPacket(
                        "trap", blockPos, offsetX, offsetY, offsetZ, 2.5, null, -1, true, true, null, effectUUID
                ),
                (ServerLevel) level, pos, 128
        );

        ServerScheduler.scheduleForDuration(0, 1, duration, () -> {
            if(hasExploded.get()) {
                return;
            }

            if(!AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, pos, 1.2f).isEmpty()) {
                hasExploded.set(true);
                PacketHandler.sendToNearbyPlayers(new CancelPhotonBlockEffectPacket(effectUUID), (ServerLevel) level, pos, 128);

                if(BeyonderData.isGriefingEnabled(entity)) {
                    level.explode(entity, pos.x, pos.y, pos.z, 4f, true, Level.ExplosionInteraction.MOB);
                }
                else {
                    level.explode(entity, pos.x, pos.y, pos.z, 4f, false, Level.ExplosionInteraction.NONE);
                }
            }
        }, (ServerLevel) level);
    }
}
