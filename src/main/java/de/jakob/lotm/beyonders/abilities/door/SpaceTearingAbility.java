package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.SpaceCollapseEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SpaceTearingAbility extends Ability {
    public SpaceTearingAbility(String id) {
        super(id, 6);
        canBeCopied = false;
        canBeShared = false;

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(22500f, 12000f, 7500f, 5000f));

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(3, 5, 7, 10));

        baseDamage = 2.5f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 1500;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, baseDistance, 2);
        SpaceCollapseEntity collapse = new SpaceCollapseEntity(level, targetLoc, baseDamage, BeyonderData.isGriefingEnabled(entity), entity);
        level.addFreshEntity(collapse);
    }
}
