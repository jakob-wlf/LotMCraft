package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.justiciar_pathway.AncientCourtEntity;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class AncientCourtOfJudgmentAbility extends Ability {

    public AncientCourtOfJudgmentAbility(String id) {
        super(id, 20 * 60 * 2, "purification", "light_source", "light_strong", "light_weak");
        canBeCopied = false;
        canBeUsedByNPC = false;
        canBeReplicated = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 0));
    }

    @Override
    protected float getSpiritualityCost() {
        return 10000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        AncientCourtEntity court = new AncientCourtEntity(
                ModEntities.ANCIENT_COURT.get(), level,
                20 * 60 * 2, entity.getUUID(),
                BeyonderData.isGriefingEnabled(entity));
        court.setPos(entity.getX(), entity.getY(), entity.getZ());
        serverLevel.addFreshEntity(court);
    }
}
