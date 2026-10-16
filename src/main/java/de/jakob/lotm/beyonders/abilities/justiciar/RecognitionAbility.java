package de.jakob.lotm.beyonders.abilities.justiciar;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.PlayPhotonEntityEffectPacket;
import de.jakob.lotm.network.packets.toClient.RecognitionSyncPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RecognitionAbility extends ToggleAbility {

    public RecognitionAbility(String id) {
        super(id);
        canBeUsedByNPC = false;
        doesNotIncreaseDigestion = true;
        canBeUsedInArtifact = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 8));
    }

    @Override
    public float getSpiritualityCost() {
        return 0;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.5f, 1.6f);
        send(entity, RecognitionSyncPacket.noTarget());
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        if(entity instanceof ServerPlayer player)
            PacketHandler.sendToPlayer(player, new PlayPhotonEntityEffectPacket("eye_of_order", player.getId(), 0, 0, 0, 1, null, true, true, UUID.randomUUID(), true));

        LivingEntity target = AbilityUtil.getTargetEntity(entity, (int) (40 * multiplier(entity)), .75f, true);

        if (target == null) {
            send(entity, RecognitionSyncPacket.noTarget());
            return;
        }

        int seq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        String path = BeyonderData.getPathway(target);

        if (VisionaryHandler.shouldStayInvisible(seq, target)) {
            send(entity, RecognitionSyncPacket.noTarget());
            return;
        }

        if ((path.equalsIgnoreCase("justiciar") && targetSeq < seq) || seq > targetSeq) {
            send(entity, RecognitionSyncPacket.noTarget());
            return;
        }

        String name = target.hasCustomName()
                ? target.getCustomName().getString()
                : target.getType().getDescription().getString();

        send(entity, RecognitionSyncPacket.target(target.getId(), name, path, targetSeq));

        if (entity.tickCount % 20 == 0) {
            level.playSound(null, entity.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3f, 1.8f);
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        clearArtifactScaling(entity);
        level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.5f, 1.6f);
        send(entity, RecognitionSyncPacket.off());
    }

    private static void send(LivingEntity entity, RecognitionSyncPacket packet) {
        if (entity instanceof ServerPlayer player) {
            PacketHandler.sendToPlayer(player, packet);
        }
    }
}