package de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncLuckPerceptionPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.LuckManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class LuckPerceptionAbility extends PassiveAbilityItem {
    public LuckPerceptionAbility(Properties properties) {
        super(properties);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide || entity.tickCount % 10 != 0) return;
        if (!(entity instanceof ServerPlayer player)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 1.5f, true);
        if (target == null) {
            clear(player);
            return;
        }

        int sequence = BeyonderData.getSequence(entity);
        int targetSequence = BeyonderData.getSequence(target);
        String targetPathway = BeyonderData.getPathway(target);

        if (VisionaryHandler.shouldStayInvisible(sequence, target)
            || targetPathway.equals("wheel_of_fortune") && targetSequence < sequence
            || AbilityUtil.isTargetSignificantlyStronger(sequence, targetSequence)) {
            clear(player);
            return;
        }

        PacketHandler.sendToPlayer(player, new SyncLuckPerceptionPacket(
            true, target.getId(), LuckManager.getEffectiveLuck(target)));
        }

        private static void clear(ServerPlayer player) {
        PacketHandler.sendToPlayer(player, new SyncLuckPerceptionPacket(false, -1, 0));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 5));
    }
}