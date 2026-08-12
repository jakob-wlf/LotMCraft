package de.jakob.lotm.beyonders.abilities.tyrant;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.custom.ability_entities.tyrant_pathway.LightningBranchEntity;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LightningBranchAbility extends Ability {
    public LightningBranchAbility(String id) {
        super(id, 5f);
        canBeShared = false;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(2, 3, 4, 5));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(5000f, 2000f, 1200f, 900f));

        baseDamage = 7f; //3 total attacks
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 900;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 dir = entity.getLookAngle().normalize();
        Vec3 startPos = entity.position().add(dir).add(0, 1.5, 0);

        LightningBranchEntity branch = new LightningBranchEntity(level, entity, startPos, dir, 30, baseDamage);
        level.addFreshEntity(branch);
    }
}
