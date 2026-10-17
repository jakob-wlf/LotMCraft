package de.jakob.lotm.beyonders.abilities.common;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.network.packets.toClient.LuckPerceptionPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LuckPerceptionAbility extends ToggleAbility {

    private static final double SCAN_RADIUS = 24;
    private static final int MAX_ENTRIES = 48;

    public LuckPerceptionAbility(String id){
        super(id);

        canBeUsedByNPC = false;
        autoClear = false;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        if (entity.tickCount % 10 != 0) return;

        int seq = AbilityUtil.getSeqWithArt(entity, this);

        syncToClient(level, entity, seq);
    }

    private boolean canPerceive(int seq, LivingEntity target) {
        int targetSeq = BeyonderData.getSequence(target);
        String path = BeyonderData.getPathway(target);

        if (VisionaryHandler.shouldStayInvisible(seq, target)) return false;
        if ("wheel_of_fortune".equals(path) && targetSeq < seq) return false;
        return !AbilityUtil.isTargetSignificantlyStronger(seq, targetSeq);
    }

    private void syncToClient(Level level, LivingEntity entity, int seq) {
        if (!(entity instanceof ServerPlayer player)) return;

        double selfLuck = entity.getData(ModAttachments.LUCK_COMPONENT.get()).getLuck();

        List<LuckPerceptionPacket.Entry> entries = level.getEntitiesOfClass(
                        LivingEntity.class,
                        entity.getBoundingBox().inflate(SCAN_RADIUS),
                        e -> e != entity
                                && e.isAlive()
                                && !(e instanceof ArmorStand)
                                && e.distanceToSqr(entity) <= SCAN_RADIUS * SCAN_RADIUS
                                && canPerceive(seq, e))
                .stream()
                .sorted(Comparator.comparingDouble(e -> e.distanceToSqr(entity)))
                .limit(MAX_ENTRIES)
                .map(e -> new LuckPerceptionPacket.Entry(
                        e.getId(), e.getData(ModAttachments.LUCK_COMPONENT.get()).getLuck()))
                .toList();

        PacketDistributor.sendToPlayer(player, new LuckPerceptionPacket(true, selfLuck, entries));
    }

    @Override
    public void start(Level level, LivingEntity entity) {}

    @Override
    public void stop(Level level, LivingEntity entity) {
        clearArtifactScaling(entity);
        AbilityUtil.sendActionBar(entity, Component.literal(""));
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, LuckPerceptionPacket.inactive());
        }
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 8, "darkness", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }
}
