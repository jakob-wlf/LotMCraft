package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BlackHoleEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class BlackHoleAbility extends Ability {
    public BlackHoleAbility(String id) {
        super(id, 20 * 60 * 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 1));
    }

    @Override
    public float getSpiritualityCost() {
        return 1500;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 27, 2);
        BlackHoleEntity blackHole = new BlackHoleEntity(ModEntities.BLACK_HOLE.get(), level, targetLoc.x, targetLoc.y, targetLoc.z, 10f, (float) DamageLookup.lookupDps(1, 1, 1, 15) * (float) multiplier(entity), BeyonderData.isGriefingEnabled(entity), entity);
        level.addFreshEntity(blackHole);
    }
}
