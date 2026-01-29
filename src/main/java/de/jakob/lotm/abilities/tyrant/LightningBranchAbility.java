package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.entity.custom.LightningBranchEntity;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class LightningBranchAbility extends Ability {
    public LightningBranchAbility(String id) {
        super(id, 1.5f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 450;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 dir = entity.getLookAngle().normalize();
        Vec3 startPos = entity.position().add(dir).add(0, 1.5, 0);

        LightningBranchEntity branch = new LightningBranchEntity(level, entity, startPos, dir, 30, DamageLookup.lookupDamage(3, .5) * multiplier(entity));
        level.addFreshEntity(branch);
    }
}
