package de.jakob.lotm.beyonders.rituals;

import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public interface RitualResultHandler {
    void perform(Map<String, Object> params, ServerPlayer player);

    default List<LivingEntity> getTargetEntity(String target, ServerPlayer player, int maxDistance) {
        return switch (target) {
            case "self" -> List.of(player);
            case "nearest" -> {
                LivingEntity nearest = AbilityUtil.getNearbyEntities(player, player.serverLevel(), player.position(), maxDistance)
                        .stream()
                        .min(Comparator.comparing(entity -> entity.distanceToSqr(player)))
                        .orElse(null);
                if (nearest != null) {
                    yield List.of(nearest);
                }
                yield List.of();
            }
            case "nearby" -> AbilityUtil.getNearbyEntities(player, player.serverLevel(), player.position(), maxDistance);
            default -> List.of();
        };
    }
}