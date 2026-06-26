package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.BlackHoleEntity;
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
        super(id, 20 * 60 * 2, "space_warp");
        canBeCopied = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 1));
    }

    @Override
    public float getSpiritualityCost() {
        return 25000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 27*(int) multiplier(entity), 2);
        BlackHoleEntity blackHole = new BlackHoleEntity(ModEntities.BLACK_HOLE.get(), level, targetLoc.x, targetLoc.y, targetLoc.z, 10f*multiplier(entity), (float) DamageLookup.lookupDps(1, 1, 1, 10) *multiplier(entity), BeyonderData.isGriefingEnabled(entity), entity);
        level.addFreshEntity(blackHole);
    }
}
