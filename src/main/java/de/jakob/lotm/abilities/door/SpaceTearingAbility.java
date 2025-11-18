package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.entity.custom.SpaceCollapseEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class SpaceTearingAbility extends AbilityItem {
    public SpaceTearingAbility(Properties properties) {
        super(properties, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 800;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 27, 2);
        SpaceCollapseEntity collapse = new SpaceCollapseEntity(level, targetLoc, (float) DamageLookup.lookupDps(3, .75, 4, 20) * (float) multiplier(entity), BeyonderData.isGriefingEnabled(entity), entity);
        level.addFreshEntity(collapse);
    }
}
