package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.BlackHoleEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BlackHoleAbility extends Ability {
    public BlackHoleAbility(String id) {
        super(id, 20 * 60 * 2, "space_warp");
        canBeCopied = false;
        canBeShared = false;

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(60000f, 35000f));

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(40, 100));

        baseDamage = 1.5f;
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

        float damage = baseDamage;
        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, baseDistance, 2);

        BlackHoleEntity blackHole = new BlackHoleEntity(
                ModEntities.BLACK_HOLE.get(),
                level,
                targetLoc.x, targetLoc.y, targetLoc.z,
                10f,
                damage,
                BeyonderData.isGriefingEnabled(entity),
                entity);

        level.addFreshEntity(blackHole);
    }
}
