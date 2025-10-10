package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BlackHoleEntity;
import de.jakob.lotm.entity.custom.SpaceCollapseEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class BlackHoleAbility extends AbilityItem {
    public BlackHoleAbility(Properties properties) {
        super(properties, 90);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1500;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 27, 2);
        BlackHoleEntity blackHole = new BlackHoleEntity(ModEntities.BLACK_HOLE.get(), level, targetLoc.x, targetLoc.y, targetLoc.z, 8.5f, (float) (10 * multiplier(entity)), BeyonderData.isGriefingEnabled(entity), entity);
        level.addFreshEntity(blackHole);
    }
}
