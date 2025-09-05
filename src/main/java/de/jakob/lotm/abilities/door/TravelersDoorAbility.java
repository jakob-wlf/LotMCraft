package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.TravelersDoorEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class TravelersDoorAbility extends AbilityItem {
    public TravelersDoorAbility(Properties properties) {
        super(properties, 3);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 65;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 5, .01f, true);
        if(!level.getBlockState(BlockPos.containing(targetLoc)).getCollisionShape(entity.level(), BlockPos.containing(targetLoc)).isEmpty()) {
            return;
        }

        TravelersDoorEntity door = new TravelersDoorEntity(ModEntities.TRAVELERS_DOOR.get(), level, entity.getLookAngle().normalize().scale(-1), targetLoc);
        level.addFreshEntity(door);
    }
}
