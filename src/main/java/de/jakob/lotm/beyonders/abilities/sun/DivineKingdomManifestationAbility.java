package de.jakob.lotm.beyonders.abilities.sun;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.sun_pathway.SunKingdomEntity;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DivineKingdomManifestationAbility extends Ability {
    public DivineKingdomManifestationAbility(String id) {
        super(id, 20 * 60*2, "purification", "light_source", "light_strong", "light_weak", "purification_holy");
        canBeCopied = false;
        canBeReplicated = false;
        canBeShared = false;
        cannotBeStolen = true;
        canBeUsedInArtifact = false;
        canBeShared = false;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(100, 360));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(25000f, 10000f));

        baseDamage = 3f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 8000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(serverLevel.isNight())
            serverLevel.setDayTime(6000);

        SunKingdomEntity sunKingdomEntity = new SunKingdomEntity(ModEntities.SUN_KINGDOM.get(), level, 20 * 50, entity.getUUID(), BeyonderData.isGriefingEnabled(entity), baseDamage);
        sunKingdomEntity.setPos(entity.getX(), entity.getY() + .5, entity.getZ());
        serverLevel.addFreshEntity(sunKingdomEntity);
    }
}
