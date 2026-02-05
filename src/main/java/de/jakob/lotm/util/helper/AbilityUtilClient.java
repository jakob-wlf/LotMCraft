package de.jakob.lotm.util.helper;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class AbilityUtilClient {

    public static List<LivingEntity> getNearbyEntities(@Nullable LivingEntity exclude, ClientLevel level,
                                                       Vec3 center, double radius) {
        AABB detectionBox = createDetectionBox(center, radius);
        double radiusSquared = radius * radius;

        return level.getEntitiesOfClass(LivingEntity.class, detectionBox).stream()
                .filter(e -> !(e instanceof Player player) || !player.isCreative())
                .filter(entity -> entity.position().distanceToSqr(center) <= radiusSquared)
                .filter(entity -> entity != exclude)
                .filter(e -> exclude == null || AbilityUtil.mayTarget(exclude, e))
                .toList();
    }

    private static AABB createDetectionBox(Vec3 center, double radius) {
        return new AABB(
                center.subtract(radius, radius, radius),
                center.add(radius, radius, radius)
        );
    }

}
